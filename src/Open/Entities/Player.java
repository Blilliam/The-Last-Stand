package Open.Entities;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import Open.Artifacts.ArtifactManager;
import Open.Artifacts.Legendary.Magnet;
import Open.Artifacts.Uncommon.GoldenShield;
import Open.Artifacts.Uncommon.Kevin;
import Open.Entities.Enemies.Enemy;
import Open.Entities.Interactible.Portal;
import Open.Entities.Interactible.Teleporter;
import Open.Upgrades.Book;
import Open.Weapons.Weapon;
import main.Animation;
import main.AppPanel;
import main.Assets;
import main.DamageResult;
import main.GameObject;
import main.MouseInput;
import main.enums.ChestState;
import main.enums.WeaponTypes;
import main.enums.WeaponUpgrades;

public class Player extends Entity {
	private ArtifactManager artifactManager;
	private EnumMap<WeaponTypes, Weapon> weapons;

	// --- Book and Passive System ---
	private Map<String, Book> ownedBooks = new HashMap<>();
	private final int MAX_BOOKS = 4;

	private int baseMaxHp;
	private int kills;
	private int expNeededToUpgrade = 10;
	private int currExp;
	private int gold;
	private int invincibilityFrames;
	private final int HIT_DELAY = 30;

	private boolean isRight;
	private final int MAX_WEAPONS = 4;
	
	private int countAfterHit;
	private int countBeforeHeal = 180;

	private BufferedImage[] walkFrames;
	private Animation walkAnim;
	private int frameWidth = 192;
	private int frameHeight = 192;

	private Animation coinAnimation;

	public Player(GameObject gameObj) {
		super(gameObj);
		setArtifactManager(new ArtifactManager(gameObj));
		artifactManager.addArtifact(new Magnet(gameObj));
		artifactManager.addArtifact(new Kevin(gameObj));
		artifactManager.addArtifact(new GoldenShield(gameObj));

		weapons = new EnumMap<WeaponTypes, Weapon>(WeaponTypes.class);

		baseMaxHp = 100;
		currHp = getMaxHp();

		x = gameObj.getMap().HEIGHT / 2;
		y = gameObj.getMap().WIDTH / 2;

		speed = 6;
		isRight = true;

		this.width = 70;
		this.height = 70;

		invincibilityFrames = 0;
		currExp = 0;
		
		countAfterHit = 0;

		this.setHitBox(new Rectangle2D.Double(this.x, this.y, this.width, this.height));

		int frameCount = 4;
		walkFrames = new BufferedImage[frameCount];
		for (int i = 0; i < frameCount; i++) {
			walkFrames[i] = Assets.playerSheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
		}
		walkAnim = new Animation(walkFrames, 100);
		coinAnimation = new Animation(Assets.coin, 100);
	}

	// =========================================================================
	// UPDATE
	// =========================================================================

	public void update() {
		if (countAfterHit > countBeforeHeal && countAfterHit % 30 == 0) {
			currHp ++;
			if (currHp > getMaxHp()) {
				currHp = getMaxHp();
			}
		}
		countAfterHit++;
		
		if (currHp <= 0) {
			isDead = true;
			gameObj.setState(gameObj.getStateDead());
		}

		artifactManager.onUpdate();
		updateOpenMovement();

		((RectangularShape) this.getHitBox()).setFrame(this.x - width / 2, this.y - height / 2, this.width,
				this.height);

		if (invincibilityFrames > 0)
			invincibilityFrames--;

		for (Weapon w : weapons.values())
			w.update();

		if (currExp >= expNeededToUpgrade) {
			gameObj.getUpgrades().shuffleUpgrades();
			gameObj.setState(gameObj.getStateUpgrade());
			currExp -= expNeededToUpgrade;
			expNeededToUpgrade = (int) (expNeededToUpgrade * 1.3);
		}
	}

	// =========================================================================
	// BOOK SYSTEM
	// =========================================================================

	/**
	 * Adds a new book or accumulates its value on top of the existing total.
	 *
	 * The incoming Book carries only the INCREMENT for this upgrade tier (e.g. Gold
	 * EXP Book = +11). We add that increment to whatever the player already has,
	 * then store a new Book with the cumulative total so getValue() always returns
	 * the full running sum.
	 *
	 * Max HP books also immediately apply the new HP so it isn't wasted.
	 */
	public void addOrUpgradeBook(Book newBook) {
		Book existing = ownedBooks.get(newBook.getName());
		double previousValue = existing != null ? existing.getValue() : 0.0;
		double increment = newBook.getValue(); // amount added by this upgrade tier
		double newTotal = previousValue + increment;

		// For Max HP, heal the player by the increment immediately
		if (newBook.getName().equals("Max HP Book")) {
			currHp += increment;
		}
		if (newBook.getName().equals("Size Book") && weapons.containsKey(WeaponTypes.Aura)) {
			weapons.get(WeaponTypes.Aura).onUpgrade(); // Aura needs to refresh its hitbox when size changes
		}

		// Store a book whose value is the full cumulative total
		ownedBooks.put(newBook.getName(), new Book(newBook.getName(), newTotal));
	}

	/**
	 * Returns the current value of a book by name, or 0.0 if not owned. Used
	 * throughout the codebase to apply book bonuses.
	 */
	public double getStatBonus(String bookName) {
		Book b = ownedBooks.get(bookName);
		return b != null ? b.getValue() : 0.0;
	}

	// =========================================================================
	// STATS
	// =========================================================================

	@Override
	public double getMaxHp() {
		double bookBonus = getStatBonus("Max HP Book");
		return (int) ((baseMaxHp + artifactManager.getFlatHealth() + bookBonus)
				* (1 + artifactManager.getPercentHealth()));
	}

	/**
	 * Adds experience, factoring in the EXP Book bonus on top of artifact bonuses.
	 */
	public void addExp(int i) {
		double artifactBonus = artifactManager.getPercentBonusExp();
		double bookBonus = getStatBonus("EXP Book") / 100.0;
		currExp += (int) (i * (1 + artifactBonus + bookBonus));
	}

	// =========================================================================
	// MOVEMENT
	// =========================================================================

	public void updateOpenMovement() {
		if (gameObj.getKeyH().isMoving)
			walkAnim.update();
		else
			walkAnim.setFrame(3);

		double dx = 0, dy = 0;
		if (gameObj.getKeyH().up)
			dy -= 1;
		if (gameObj.getKeyH().down)
			dy += 1;
		if (gameObj.getKeyH().left) {
			dx -= 1;
			isRight = false;
		}
		if (gameObj.getKeyH().right) {
			dx += 1;
			isRight = true;
		}

		double length = Math.sqrt(dx * dx + dy * dy);
		if (length > 0) {
			dx /= length;
			dy /= length;
			x += dx * speed;
			y += dy * speed;
		}
		
		if (x < 0) {
			x = width/2;
		}
		if (gameObj.getMap().HEIGHT < y) {
			y = gameObj.getMap().HEIGHT - height/2;
		}
		if (gameObj.getMap().WIDTH < x) {
			x = gameObj.getMap().WIDTH - width/2;
		}
		if (y < 0) {
			y = height/2;
		}
	}

	// =========================================================================
	// COMBAT
	// =========================================================================

	public void damage(int amount) {
		boolean atMax = false;
		if (invincibilityFrames <= 0) {
			if (currHp == getMaxHp()) {
				atMax = true;
			}
			
			currHp -= amount;
			
			if (currHp <= 0 && atMax) {
				currHp = 1;
			}
			
			currHp = Math.max(currHp, 0);
			
			// Mirror: Chance to reflect damage
			double reflectionChance = artifactManager.getReflectionChance();
			if (Math.random() < reflectionChance) {
				// Find nearest enemy and deal reflection damage
				Enemy nearestEnemy = closestEnemy(500.0);
				if (nearestEnemy != null && !nearestEnemy.isDying()) {
					DamageResult reflectionDamage = new DamageResult(amount, false);
					nearestEnemy.damage(reflectionDamage);
				}
			}
			
			// Increase invincibility frames from Mirror
			int extraInvulnFrames = (int) artifactManager.getBonusInvinsibilityFrames();
			invincibilityFrames = HIT_DELAY + extraInvulnFrames;
			countAfterHit = 0;
			
			// Trigger artifact effects on damage taken (GoldenShield, etc.)
			artifactManager.onDamageTaken(amount);
		}
	}

	public boolean isInvincible() {
		return invincibilityFrames > 0;
	}

	// =========================================================================
	// DRAW
	// =========================================================================

	@Override
	public void draw(Graphics2D g2) {
		int screenX = x - gameObj.getCameraX() - (width / 2);
		int screenY = y - gameObj.getCameraY() - (height / 2);

		int visualW = (int) (width * 1.5);
		int visualH = (int) (height * 1.5);
		int drawX = screenX - (visualW - width) / 2;
		int drawY = screenY - (visualH - height) / 2;

		if (!(isInvincible() && invincibilityFrames % 6 < 3)) {
			if (isRight) {
				g2.drawImage(walkAnim.getFrame(), drawX, drawY, visualW, visualH, null);
			} else {
				g2.drawImage(walkAnim.getFrame(), drawX + visualW, drawY, -visualW, visualH, null);
			}
		}

		drawInventoryPanel(g2);
		drawXPBar(g2);
		drawHpBar(g2);
		drawOwnedBooks(g2);
		drawActiveWeapons(g2);
		artifactManager.draw(g2);
		drawGoldCounter(g2);
		drawMinimap(g2);
	}

	// =========================================================================
	// HUD DRAWING
	// =========================================================================

	private void drawGoldCounter(Graphics2D g2) {
		int barWidth = 200;
		int barHeight = 30;
		int hpX = -20;
		int hpY = AppPanel.HEIGHT - 70;

		int coinSize = 28;
		int counterH = 30;
		int counterW = 110;
		int cx = hpX + (barWidth - counterW) / 2;
		int cy = hpY - counterH - 6;

		g2.setColor(new Color(30, 25, 10, 210));
		g2.fillRoundRect(cx, cy, counterW, counterH, 10, 10);
		g2.setStroke(new BasicStroke(2f));
		g2.setColor(new Color(200, 160, 40));
		g2.drawRoundRect(cx, cy, counterW, counterH, 10, 10);
		g2.setStroke(new BasicStroke(1f));

		coinAnimation.update();
		if (coinAnimation.getFrame() != null) {
			g2.drawImage(coinAnimation.getFrame(), cx + 4, cy + (counterH - coinSize) / 2, coinSize, coinSize, null);
		}

		String goldStr = String.valueOf(gold);
		g2.setFont(new Font("Monospaced", Font.BOLD, 16));
		FontMetrics fm = g2.getFontMetrics();
		int textX = cx + coinSize + 8;
		int textY = cy + (counterH + fm.getAscent()) / 2 - 3;

		g2.setColor(new Color(0, 0, 0, 180));
		g2.drawString(goldStr, textX + 1, textY + 1);
		g2.setColor(gold >= 15 ? new Color(255, 215, 50) : new Color(200, 80, 80));
		g2.drawString(goldStr, textX, textY);
	}

	private void drawActiveWeapons(Graphics2D g2) {
		int startX = 20;
		int startY = 140;
		int iconSize = 70;
		int spacing = 8;
		int index = 0;

		int mx = MouseInput.getMouseX();
		int my = MouseInput.getMouseY();
		Weapon hoveredWeapon = null;

		for (Weapon w : weapons.values()) {
			BufferedImage icon = w.getIcon();
			if (icon != null) {
				int drawX = startX + (index * (iconSize + spacing));

				g2.setColor(new Color(40, 40, 40, 200));
				g2.fillRoundRect(drawX, startY, iconSize, iconSize, 8, 8);
				g2.drawImage(icon, drawX + 4, startY + 4, iconSize - 8, iconSize - 8, null);
				g2.setColor(Color.GRAY);
				g2.drawRoundRect(drawX, startY, iconSize, iconSize, 8, 8);

				if (mx >= drawX && mx <= drawX + iconSize && my >= startY && my <= startY + iconSize) {
					hoveredWeapon = w;
					g2.setColor(new Color(255, 255, 255, 100));
					g2.fillRoundRect(drawX, startY, iconSize, iconSize, 8, 8);
				}
				index++;
			}
		}

		if (hoveredWeapon != null)
			drawWeaponTooltip(g2, hoveredWeapon, mx, my);
	}

	private void drawWeaponTooltip(Graphics2D g2, Weapon w, int mouseX, int mouseY) {
		EnumMap<WeaponUpgrades, Double> stats = w.getStats();
		int rowHeight = 25;
		int padding = 15;
		int width = 220;

		int activeStatCount = 0;
		for (Double val : stats.values())
			if (val > 0)
				activeStatCount++;
		int height = (activeStatCount * rowHeight) + (padding * 2);

		int drawX = mouseX + 20;
		int drawY = mouseY + 20;

		g2.setColor(new Color(20, 20, 20, 230));
		g2.fillRoundRect(drawX, drawY, width, height, 5, 5);
		g2.setStroke(new BasicStroke(3));
		g2.setColor(new Color(100, 100, 100));
		g2.drawRoundRect(drawX, drawY, width, height, 5, 5);

		g2.setFont(new Font("Monospaced", Font.BOLD, 16));
		int i = 0;
		for (var entry : stats.entrySet()) {
			double value = entry.getValue();
			if (value <= 0 && entry.getKey() != WeaponUpgrades.AttackDamage)
				continue;

			String label = getDisplayName(entry.getKey());
			String formattedValue = formatStatValue(entry.getKey(), value);
			int textY = drawY + padding + (i * rowHeight) + 15;

			g2.setColor(Color.BLACK);
			g2.drawString(label + ":", drawX + padding + 2, textY + 2);
			g2.drawString(formattedValue, drawX + width - 70 + 2, textY + 2);

			g2.setColor(new Color(200, 200, 200));
			g2.drawString(label + ":", drawX + padding, textY);
			g2.setColor(new Color(255, 215, 0));
			g2.drawString(formattedValue, drawX + width - 70, textY);

			i++;
		}
	}

	private String formatStatValue(WeaponUpgrades stat, double val) {
		if (stat == WeaponUpgrades.CriticalChance || stat == WeaponUpgrades.CriticalDamage
				|| stat == WeaponUpgrades.AttackSize) {
			return String.format("%.0f%%", val * 100);
		}
		if (stat == WeaponUpgrades.AttackSpeed) {
			return String.format("%.2fs", val / 60.0);
		}
		return String.format("%.1f", val);
	}

	private String getDisplayName(WeaponUpgrades stat) {
		return switch (stat) {
		case AttackSpeed -> "Cooldown";
		case AttackDamage -> "Damage";
		case AttackSize -> "Size";
		case ProjectileCount -> "Amount";
		case ProjectileSpeed -> "Speed";
		case ProjectileBounce -> "Bounces";
		case CriticalChance -> "Crit Rate";
		case CriticalDamage -> "Crit Damage";
		default -> stat.toString();
		};
	}

	private void drawOwnedBooks(Graphics2D g2) {
		int startX = 20;
		int startY = 230;
		int iconSize = 70;
		int spacing = 8;
		int index = 0;

		int mx = MouseInput.getMouseX();
		int my = MouseInput.getMouseY();
		Book hoveredBook = null;
		int hoveredX = 0;
		int hoveredY = 0;

		for (Book book : ownedBooks.values()) {
			BufferedImage icon = getIconForBook(book.getName());
			if (icon != null) {
				int drawX = startX + (index * (iconSize + spacing));

				g2.setColor(new Color(0, 0, 0, 100));
				g2.fillRect(drawX, startY, iconSize, iconSize);
				g2.drawImage(icon, drawX, startY, iconSize, iconSize, null);
				g2.setColor(new Color(255, 255, 255, 50));
				g2.drawRect(drawX, startY, iconSize, iconSize);

				if (mx >= drawX && mx <= drawX + iconSize && my >= startY && my <= startY + iconSize) {
					hoveredBook = book;
					hoveredX = mx;
					hoveredY = my;
				}

				index++;
			}
		}

		if (hoveredBook != null)
			drawBookTooltip(g2, hoveredBook, hoveredX, hoveredY);
	}

	/**
	 * Tooltip shown when the player hovers over a tome in the HUD. Displays the
	 * book name, current value, and rarity.
	 */
	private void drawBookTooltip(Graphics2D g2, Book book, int mouseX, int mouseY) {
		String title = book.getName();
		String desc = book.getDescription();

		Font titleFont = new Font("Monospaced", Font.BOLD, 16);
		Font descFont = new Font("Monospaced", Font.PLAIN, 14);

		FontMetrics fmTitle = g2.getFontMetrics(titleFont);
		FontMetrics fmDesc = g2.getFontMetrics(descFont);

		int padding = 15;
		int rowHeight = 25;
		int width = Math.max(fmTitle.stringWidth(title), fmDesc.stringWidth(desc)) + padding * 2 + 12;
		int height = rowHeight * 2 + padding * 2;

		int drawX = mouseX + 20;
		int drawY = mouseY + 20;

		g2.setColor(new Color(20, 20, 20, 230));
		g2.fillRoundRect(drawX, drawY, width, height, 5, 5);
		g2.setStroke(new BasicStroke(3));
		g2.setColor(new Color(100, 100, 100));
		g2.drawRoundRect(drawX, drawY, width, height, 5, 5);
		g2.setStroke(new BasicStroke(1));

		// Title
		g2.setFont(titleFont);
		int textY = drawY + padding + 15;
		g2.setColor(Color.BLACK);
		g2.drawString(title, drawX + padding + 2, textY + 2);
		g2.setColor(new Color(255, 215, 0));
		g2.drawString(title, drawX + padding, textY);

		// Description
		g2.setFont(descFont);
		textY += rowHeight;
		g2.setColor(Color.BLACK);
		g2.drawString(desc, drawX + padding + 2, textY + 2);
		g2.setColor(new Color(200, 200, 200));
		g2.drawString(desc, drawX + padding, textY);
	}

	private BufferedImage getIconForBook(String name) {
		return switch (name) {
		case "Gold Book" -> Assets.GoldTomeIcon;
		case "Max HP Book" -> Assets.HpTomeIcon;
		case "Luck Book" -> Assets.LuckTomeIcon;
		case "Crit Rate Book" -> Assets.CritTomeIcon;
		case "Projectile Speed Book" -> Assets.ProjectileSpeedTomeIcon;
		case "Quantity Over Quality Book" -> Assets.ProjectileCountTomeIcon;
		case "Size Book" -> Assets.SizeTomeIcon;
		case "EXP Book" -> Assets.XpTomeIcon;
		case "Cooldown Book" -> Assets.CooldownTomeIcon;
		case "Damage Book" -> Assets.DamageTomeIcon;
		default -> null;
		};
	}

	private void drawInventoryPanel(Graphics2D g2) {
		int startX = 10;
		int startY = 130;
		int iconSize = 70;
		int spacing = 8;
		int panelWidth = (MAX_WEAPONS * (iconSize + spacing)) + 10;
		int panelHeight = (iconSize * 2) + spacing + 30;

		g2.setColor(new Color(60, 60, 60, 220));
		g2.fillRoundRect(startX, startY, panelWidth, panelHeight, 15, 15);
		g2.setStroke(new BasicStroke(3));
		g2.setColor(new Color(100, 100, 100));
		g2.drawRoundRect(startX, startY, panelWidth, panelHeight, 15, 15);
		g2.setStroke(new BasicStroke(1));
	}

	private void drawXPBar(Graphics2D g2) {
		int barWidth = AppPanel.WIDTH;
		int barHeight = 30;
		float percent = Math.min(1.0f, (float) currExp / expNeededToUpgrade);

		g2.setColor(new Color(50, 50, 50, 180));
		g2.fillRect(0, 0, barWidth, barHeight);
		g2.setColor(new Color(0, 200, 255));
		g2.fillRect(0, 0, (int) (barWidth * percent), barHeight);
		g2.setColor(Color.WHITE);
		g2.drawRect(0, 0, barWidth, barHeight);

		g2.setFont(new Font("Malgun Gothic", Font.PLAIN, 20));
		String text = "Exp: " + currExp + " / " + expNeededToUpgrade;
		g2.drawString(text, (barWidth - g2.getFontMetrics().stringWidth(text)) / 2, barHeight - 5);
	}

	private void drawHpBar(Graphics2D g2) {
		int barWidth = 240;
		int barHeight = 40;
		int x = 10;
		int y = AppPanel.HEIGHT - barHeight - barHeight/2;
		float percent = Math.min(1.0f, (float) currHp / (float) getMaxHp());

		// Background panel
		g2.setColor(new Color(30, 20, 20, 210));
		g2.fillRoundRect(x - 5, y - 5, barWidth + 10, barHeight + 10, 12, 12);
		g2.setStroke(new BasicStroke(2f));
		g2.setColor(new Color(180, 60, 60, 200));
		g2.drawRoundRect(x - 5, y - 5, barWidth + 10, barHeight + 10, 12, 12);

		// Background bar
		g2.setColor(new Color(40, 30, 30, 200));
		g2.fillRoundRect(x, y, barWidth, barHeight, 8, 8);

		// HP fill bar with gradient effect
		int fillWidth = (int) (barWidth * percent);
		if (fillWidth > 0) {
			// Determine color based on HP percentage
			Color hpColor;
			if (percent > 0.5f) {
				hpColor = new Color(50, 220, 80);
			} else if (percent > 0.25f) {
				hpColor = new Color(255, 220, 50);
			} else {
				hpColor = new Color(255, 80, 80);
			}
			g2.setColor(hpColor);
			g2.fillRoundRect(x, y, fillWidth, barHeight, 8, 8);

			// Highlight on top
			g2.setColor(new Color(255, 255, 255, 80));
			g2.fillRoundRect(x, y, fillWidth, barHeight / 3, 8, 8);
		}

		// Border
		g2.setStroke(new BasicStroke(2f));
		g2.setColor(new Color(180, 80, 80, 220));
		g2.drawRoundRect(x, y, barWidth, barHeight, 8, 8);
		g2.setStroke(new BasicStroke(1f));

		// Text
		g2.setFont(new Font("Monospaced", Font.BOLD, 18));
		String text = "HP: " + (int) currHp + " / " + (int) getMaxHp();
		FontMetrics fm = g2.getFontMetrics();
		int textX = x + (barWidth - fm.stringWidth(text)) / 2;
		int textY = y + ((barHeight + fm.getAscent()) / 2) - 3;

		// Text shadow
		g2.setColor(new Color(0, 0, 0, 200));
		g2.drawString(text, textX + 1, textY + 1);

		// Text color
		g2.setColor(new Color(255, 255, 255, 255));
		g2.drawString(text, textX, textY);
	}

	private void drawMinimap(Graphics2D g2) {
		int mapW = 180, mapH = 180;
		int padding = 15;
		int mapX = AppPanel.WIDTH - mapW - padding;
		int mapY = padding + 30;
		int worldW = gameObj.getMap().WIDTH;
		int worldH = gameObj.getMap().HEIGHT;

		g2.setColor(new Color(0, 0, 0, 160));
		g2.fillRoundRect(mapX - 2, mapY - 2, mapW + 4, mapH + 4, 8, 8);
		g2.setColor(new Color(10, 10, 20, 200));
		g2.fillRoundRect(mapX, mapY, mapW, mapH, 6, 6);

		// Interactibles
		for (var interactible : gameObj.getInteractibles()) {
			if (interactible.getState() == ChestState.OPEN)
				continue;

			int ix = mapX + (int) ((double) interactible.getX() / worldW * mapW);
			int iy = mapY + (int) ((double) interactible.getY() / worldH * mapH);
			ix = Math.max(mapX + 1, Math.min(mapX + mapW - 5, ix));
			iy = Math.max(mapY + 1, Math.min(mapY + mapH - 5, iy));

			boolean isTeleporter = interactible.getClass().equals(Teleporter.class);
			boolean isPortal = interactible.getClass().equals(Portal.class);
			g2.setColor(isTeleporter ? Color.CYAN : new Color(255, 215, 0, 200));
			g2.setColor(isPortal ? Color.GREEN : new Color(255, 215, 0, 200));
			g2.fillRect(ix, iy, 6, 5);
			g2.setColor(isTeleporter ? Color.BLUE : new Color(255, 255, 255, 120));
			g2.setColor(isPortal ? Color.GREEN : new Color(255, 215, 0, 200));
			g2.drawRect(ix, iy, 6, 5);
		}

		// Player dot
		int px = mapX + (int) ((double) x / worldW * mapW);
		int py = mapY + (int) ((double) y / worldH * mapH);
		px = Math.max(mapX + 2, Math.min(mapX + mapW - 6, px));
		py = Math.max(mapY + 2, Math.min(mapY + mapH - 6, py));

		g2.setColor(new Color(0, 220, 255, 80));
		g2.fillOval(px - 4, py - 4, 14, 14);
		g2.setColor(new Color(0, 220, 255));
		g2.fillOval(px - 2, py - 2, 10, 10);
		g2.setColor(Color.WHITE);
		g2.fillOval(px, py, 4, 4);

		// Border
		g2.setColor(new Color(80, 130, 180, 200));
		g2.setStroke(new BasicStroke(1.5f));
		g2.drawRoundRect(mapX, mapY, mapW, mapH, 6, 6);
		g2.setStroke(new BasicStroke(1f));

		g2.setFont(new Font("Monospaced", Font.BOLD, 10));
		g2.setColor(new Color(80, 130, 180, 220));
		g2.drawString("MAP", mapX + 4, mapY + 11);
	}

	// =========================================================================
	// ENEMY TARGETING
	// =========================================================================

	public Enemy closestEnemy(Double range) {
		ArrayList<Enemy> enemies = gameObj.getEnemies();
		double minDistance = Double.MAX_VALUE;
		Enemy closestEnemy = null;

		for (Enemy e : enemies) {
			int dist = Entity.getDistance(this, e);
			if (dist < range && !e.isDying() && dist < minDistance) {
				minDistance = dist;
				closestEnemy = e;
			}
		}
		return closestEnemy;
	}

	// =========================================================================
	// GETTERS / SETTERS
	// =========================================================================

	public Map<String, Book> getOwnedBooks() {
		return ownedBooks;
	}
	public int getMAX_BOOKS() {
		return MAX_BOOKS;
	}

	public int getMAX_WEAPONS() {
		return MAX_WEAPONS;
	}

	public EnumMap<WeaponTypes, Weapon> getWeapons() {
		return weapons;
	}

	public void addWeapon(WeaponTypes type, Weapon w) {
		weapons.put(type, w);
	}

	public int getGold() {
		return gold;
	}

	public void addGold(int g) {
		this.gold += g;
	}

	public void setGold(int gold) {
		this.gold = gold;
	}

	public int getKills() {
		return kills;
	}

	public void addKills(int count) {
		kills += count;
	}

	public ArtifactManager getArtifactManager() {
		return artifactManager;
	}

	public void setArtifactManager(ArtifactManager am) {
		this.artifactManager = am;
	}
}