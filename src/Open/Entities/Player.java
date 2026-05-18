package Open.Entities;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import Open.Artifacts.ArtifactManager;
import Open.Artifacts.Magnet;
import Open.Entities.Enemies.Enemy;
import Open.Upgrades.Book;
import Open.Weapons.AuraWeapon;
import Open.Weapons.BananaWeapon;
import Open.Weapons.PewPewWeapon;
import Open.Weapons.Weapon;
import main.Animation;
import main.AppPanel;
import main.Assets;
import main.GameObject;
import main.MouseInput;
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

	private BufferedImage[] walkFrames;
	private Animation walkAnim;
	private int frameWidth = 192, frameHeight = 192;

	public Player(GameObject gameObj) {
		super(gameObj);
		setArtifactManager(new ArtifactManager(gameObj));
		artifactManager.addArtifact(new Magnet(gameObj));

		weapons = new EnumMap<WeaponTypes, Weapon>(WeaponTypes.class);
		weapons.put(WeaponTypes.PewPew, new PewPewWeapon(gameObj));

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

		// Set the initial hitbox frame directly to these coordinates
		this.hitBox = new Rectangle2D.Double(this.x, this.y, this.width, this.height);

		int frameCount = 4;
		walkFrames = new BufferedImage[frameCount];
		for (int i = 0; i < frameCount; i++) {
			walkFrames[i] = Assets.playerSheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
		}
		walkAnim = new Animation(walkFrames, 100);
	}

	public void update() {
		if (currHp <= 0) {
			isDead = true;
			gameObj.setState(gameObj.getStateDead());
		}

		artifactManager.onUpdate();
		updateOpenMovement();

		((RectangularShape) this.hitBox).setFrame(this.x - width/2, this.y - height/2, this.width, this.height);

		if (invincibilityFrames > 0) {
			invincibilityFrames--;
		}

		for (Weapon w : weapons.values()) {
			w.update();
		}

		if (currExp >= expNeededToUpgrade) {
			gameObj.getUpgrades().shuffleUpgrades();
			gameObj.setState(gameObj.getStateUpgrade());
			currExp -= expNeededToUpgrade;
			expNeededToUpgrade *= 1.3;
		}
	}

	// --- Book Integration Logic ---
	public void addOrUpgradeBook(Book newBook) {
		ownedBooks.put(newBook.getName(), newBook);

		if (newBook.getName().equals("Max HP Book")) {
			currHp += (int) newBook.getValue();
		}
	}

	public double getStatBonus(String bookName) {
		Book b = ownedBooks.get(bookName);
		if (b != null) {
			return b.getValue();
		}
		return 0.0;
	}

	@Override
	public double getMaxHp() {
		double bookBonus = getStatBonus("Max HP Book");
		return (int) ((baseMaxHp + getArtifactManager().getFlatHealth() + bookBonus)
				* (1 + getArtifactManager().getPercentHealth()));
	}

	public void addExp(int i) {
		double bookBonus = getStatBonus("EXP Book") / 100.0;
		currExp += (i * (1 + artifactManager.getPercentBonusExp() + bookBonus));
	}

	// --- Original Movement and Combat Methods ---
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
	}

	public void damage(int amount) {
		if (invincibilityFrames <= 0) {
			currHp -= amount;
			currHp = Math.max(currHp, 0);
			invincibilityFrames = HIT_DELAY;
		}
	}

	public boolean isInvincible() {
		return invincibilityFrames > 0;
	}

	@Override
	public void draw(Graphics2D g2) {

		int screenX = (AppPanel.WIDTH / 2) - (width / 2);
		int screenY = (AppPanel.HEIGHT / 2) - (height / 2);


		int visualW = (int) (width * 1.5);
		int visualH = (int) (height * 1.5);

		int drawX = screenX - (visualW - width) / 2;
		int drawY = screenY - (visualH - height) / 2;

		// 4. Render the sprite centered over the hitbox
		if (!(isInvincible() && invincibilityFrames % 6 < 3)) {
			if (isRight) {
				g2.drawImage(walkAnim.getFrame(), drawX, drawY, visualW, visualH, null);
			} else {
				g2.drawImage(walkAnim.getFrame(), drawX + visualW, drawY, -visualW, visualH, null);
			}
		}

		// --- Core Systems Rendering Pass ---
		artifactManager.draw(g2);
		drawInventoryPanel(g2);
		drawXPBar(g2);
		drawHpBar(g2);
		drawOwnedBooks(g2);
		drawActiveWeapons(g2);
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

				// Draw Weapon Slot
				g2.setColor(new Color(40, 40, 40, 200));
				g2.fillRoundRect(drawX, startY, iconSize, iconSize, 8, 8);
				g2.drawImage(icon, drawX + 4, startY + 4, iconSize - 8, iconSize - 8, null);
				g2.setColor(Color.GRAY);
				g2.drawRoundRect(drawX, startY, iconSize, iconSize, 8, 8);

				// CHECK FOR HOVER
				if (mx >= drawX && mx <= drawX + iconSize && my >= startY && my <= startY + iconSize) {
					hoveredWeapon = w;
					// Optional: Highlight the hovered icon
					g2.setColor(new Color(255, 255, 255, 100));
					g2.fillRoundRect(drawX, startY, iconSize, iconSize, 8, 8);
				}
				index++;
			}
		}

		// Draw the tooltip LAST so it appears on top of everything
		if (hoveredWeapon != null) {
			drawWeaponTooltip(g2, hoveredWeapon, mx, my);
		}
	}

	private void drawWeaponTooltip(Graphics2D g2, Weapon w, int mouseX, int mouseY) {
		EnumMap<WeaponUpgrades, Double> stats = w.getStats();
		int rowHeight = 25; // Increased for better readability
		int padding = 15;
		int width = 220;

		// Count active stats to set height
		int activeStatCount = 0;
		for (Double val : stats.values())
			if (val > 0)
				activeStatCount++;
		int height = (activeStatCount * rowHeight) + (padding * 2);

		int drawX = mouseX + 20;
		int drawY = mouseY + 20;

		// 1. Draw the "Pixel" Background Box
		g2.setColor(new Color(20, 20, 20, 230)); // Near black
		g2.fillRoundRect(drawX, drawY, width, height, 5, 5);

		// 2. Draw a thick border (like your buttons)
		g2.setStroke(new BasicStroke(3));
		g2.setColor(new Color(100, 100, 100)); // Grey border
		g2.drawRoundRect(drawX, drawY, width, height, 5, 5);

		// 3. Draw Stats with "Upgrade Menu" Styling
		g2.setFont(new Font("Monospaced", Font.BOLD, 16));
		int i = 0;
		for (var entry : stats.entrySet()) {
			double value = entry.getValue();
			if (value <= 0 && entry.getKey() != WeaponUpgrades.AttackDamage)
				continue;

			String label = getDisplayName(entry.getKey());
			String formattedValue = formatStatValue(entry.getKey(), value);

			int textY = drawY + padding + (i * rowHeight) + 15;

			// A. Draw Shadow (The "Visual Stuff")
			g2.setColor(Color.BLACK);
			g2.drawString(label + ":", drawX + padding + 2, textY + 2);
			g2.drawString(formattedValue, drawX + width - 70 + 2, textY + 2);

			// B. Draw Main Text
			g2.setColor(new Color(200, 200, 200)); // Silver/Grey for labels
			g2.drawString(label + ":", drawX + padding, textY);

			g2.setColor(new Color(255, 215, 0)); // Gold for the actual numbers
			g2.drawString(formattedValue, drawX + width - 70, textY);

			i++;
		}
	}

	private String formatStatValue(WeaponUpgrades stat, double val) {
		// Check if it's a percentage stat
		if (stat == WeaponUpgrades.CriticalChance || stat == WeaponUpgrades.CriticalDamage
				|| stat == WeaponUpgrades.AttackSize) {
			return String.format("%.0f%%", val * 100);
		}
		// Check if it's the Cooldown/AttackSpeed (showing frames to seconds conversion)
		if (stat == WeaponUpgrades.AttackSpeed) {
			return String.format("%.2fs", val / 60.0);
		}
		// Standard number
		return String.format("%.1f", val);
	}

	// Add your display name helper if it's not already in Player
	private String getDisplayName(WeaponUpgrades stat) {
		return switch (stat) {
		case AttackSpeed -> "Cooldown";
		case AttackDamage -> "Damage";
		case AttackSize -> "Area";
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
		int startY = 230; // Positioned safely below the XP bar
		int iconSize = 70;
		int spacing = 8;
		int index = 0;

		// Use a simpler approach: just the icon and a subtle backing
		for (Book book : ownedBooks.values()) {
			BufferedImage icon = getIconForBook(book.getName());

			if (icon != null) {
				int drawX = startX + (index * (iconSize + spacing));

				// 1. Semi-transparent backing so icons are visible over the map
				g2.setColor(new Color(0, 0, 0, 100));
				g2.fillRect(drawX, startY, iconSize, iconSize);

				// 2. The Tome Icon
				g2.drawImage(icon, drawX, startY, iconSize, iconSize, null);

				// 3. Optional: A very thin white border to define the slot
				g2.setColor(new Color(255, 255, 255, 50));
				g2.drawRect(drawX, startY, iconSize, iconSize);

				index++;
			}
		}
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
		int startX = 10; // Slightly outside the first icon
		int startY = 130; // Just above the weapon row
		int iconSize = 70;
		int spacing = 8;

		// Calculate width based on max slots
		// Using MAX_WEAPONS (4) as the guide
		int panelWidth = (MAX_WEAPONS * (iconSize + spacing)) + 10;
		int panelHeight = (iconSize * 2) + spacing + 30; // Enough for 2 rows + padding

		// 1. Draw the main grey box
		g2.setColor(new Color(60, 60, 60, 220)); // Dark grey, slightly transparent
		g2.fillRoundRect(startX, startY, panelWidth, panelHeight, 15, 15);

		// 2. Draw a lighter grey border
		g2.setStroke(new BasicStroke(3));
		g2.setColor(new Color(100, 100, 100));
		g2.drawRoundRect(startX, startY, panelWidth, panelHeight, 15, 15);
		g2.setStroke(new BasicStroke(1)); // Reset stroke
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
		int barWidth = 200;
		int barHeight = 30;
		int x = (AppPanel.WIDTH - barWidth) / 2;
		int y = AppPanel.HEIGHT - barHeight - 100;
		float percent = (float) Math.min(1.0f, (float) currHp / getMaxHp());
		g2.setColor(new Color(50, 50, 50, 180));
		g2.fillRect(x, y, barWidth, barHeight);
		g2.setColor(new Color(255, 0, 0));
		g2.fillRect(x, y, (int) (barWidth * percent), barHeight);
		g2.setColor(Color.WHITE);
		g2.drawRect(x, y, barWidth, barHeight);
		g2.setFont(new Font("Malgun Gothic", Font.PLAIN, 20));
		String text = "Hp: " + (int) (currHp) + " / " + (int) (getMaxHp());
		g2.drawString(text, x + (barWidth - g2.getFontMetrics().stringWidth(text)) / 2, y + barHeight - 8);
	}

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

	// --- Getters and Setters ---
	public Map<String, Book> getOwnedBooks() {
		return ownedBooks;
	}

	public int getMAX_BOOKS() {
		return MAX_BOOKS;
	}

	public EnumMap<WeaponTypes, Weapon> getWeapons() {
		return weapons;
	}

	public void addWeapon(WeaponTypes type, Weapon w) {
		weapons.put(type, w);
	}

	public int getMAX_WEAPONS() {
		return MAX_WEAPONS;
	}

	public int getGold() {
		return gold;
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