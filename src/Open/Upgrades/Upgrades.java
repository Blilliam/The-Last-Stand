package Open.Upgrades;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

import Open.Weapons.*;
import main.AppPanel;
import main.Assets;
import main.GameButton;
import main.GameObject;
import main.enums.WeaponRarity;
import main.enums.WeaponTypes;
import main.enums.WeaponUpgrades;

public class Upgrades {
	private GameObject gameObj;
	private GameButton[] boxes = new GameButton[3];
	private WeaponRarity[] boxRarities = new WeaponRarity[3]; // null = new item (grey)
	private boolean[] boxIsBook = new boolean[3]; // true = tome slot

	private EnumMap<WeaponTypes, Weapon> allWeapons;

	private final int rectWidth = 900;
	private final int rectHeight = 200;

	private List<PixelParticle> particles = new ArrayList<>();
	private float flashAlpha = 0f;
	private long timer = 0;
	private Random rand = new Random();

	// 50 % of slots will be book/tome offers
	private static final float BOOK_CHANCE = 0.50f;

	private final String[] bookPool = { "EXP Book", "Size Book", "Quantity Book", "Projectile Speed Book",
			"Crit Rate Book", "Max HP Book", "Gold Book", "Damage Book", "Cooldown Book" };

	public Upgrades(GameObject gameObj) {
		this.gameObj = gameObj;
		allWeapons = new EnumMap<>(WeaponTypes.class);
		allWeapons.put(WeaponTypes.Aura, new AuraWeapon(gameObj));
		allWeapons.put(WeaponTypes.Banana, new BananaWeapon(gameObj));
		allWeapons.put(WeaponTypes.Bone, new BoneWeapon(gameObj));
		allWeapons.put(WeaponTypes.FireStaff, new FireStaffWeapon(gameObj));
		allWeapons.put(WeaponTypes.PewPew, new PewPewWeapon(gameObj));
		allWeapons.put(WeaponTypes.Sword, new SwordWeapon(gameObj));
		allWeapons.put(WeaponTypes.Katana, new KatanaWeapon(gameObj));
		allWeapons.put(WeaponTypes.Dexecutioner, new DexecutionerWeapon(gameObj));
	}

	// =========================================================================
	// SHUFFLE – builds 3 upgrade option buttons
	// =========================================================================
	public void shuffleUpgrades() {
		startLevelUpEffect();

		Set<WeaponTypes> ownedWeapons = gameObj.getPlayer().getWeapons().keySet();
		Map<String, Book> ownedBooks = gameObj.getPlayer().getOwnedBooks();
		boolean canAddWeapon = gameObj.getPlayer().getWeapons().size() < gameObj.getPlayer().getMAX_WEAPONS();
		boolean canAddBook = ownedBooks.size() < gameObj.getPlayer().getMAX_BOOKS();

		List<WeaponTypes> availableWeapons = new ArrayList<>();
		for (WeaponTypes t : allWeapons.keySet())
			if (!ownedWeapons.contains(t))
				availableWeapons.add(t);

		List<String> availableBooks = new ArrayList<>();
		for (String s : bookPool)
			if (!ownedBooks.containsKey(s))
				availableBooks.add(s);

		for (int i = 0; i < boxes.length; i++) {
			int yPos = 100 + (i * 300);

			// Reset slot state
			boxes[i] = null;
			boxRarities[i] = null;
			boxIsBook[i] = false;

			boolean preferBook = rand.nextFloat() < BOOK_CHANCE;

			boolean hasBookContent = (canAddBook && !availableBooks.isEmpty()) || !ownedBooks.isEmpty();
			boolean hasWeaponContent = (canAddWeapon && !availableWeapons.isEmpty()) || !ownedWeapons.isEmpty();

			// Decide category; fall back if preferred one has nothing to offer
			boolean useBook;
			if (preferBook && hasBookContent)
				useBook = true;
			else if (!preferBook && hasWeaponContent)
				useBook = false;
			else if (hasBookContent)
				useBook = true;
			else if (hasWeaponContent)
				useBook = false;
			else
				continue; // nothing at all – skip slot

			boxIsBook[i] = useBook;

			if (useBook) {
				// ── BOOK / TOME SLOT ──────────────────────────────────────
				boolean offerNewBook = canAddBook && !availableBooks.isEmpty()
						&& (rand.nextBoolean() || ownedBooks.isEmpty());

				if (offerNewBook) {
					// New tome (no rarity colour – stays grey/purple)
					String bookName = availableBooks.get(rand.nextInt(availableBooks.size()));
					Book book = new Book(bookName, WeaponRarity.BRONZE);
					String label = "NEW: " + bookName + "   [" + book.getDescription() + "]";

					final int slot = i;
					boxes[i] = new GameButton(AppPanel.WIDTH / 2 - rectWidth / 2, yPos, rectWidth, rectHeight, label,
							() -> {
								gameObj.getPlayer().addOrUpgradeBook(book);
								finishUpgrade();
							});
					boxes[i].setImage(getIconForBook(bookName));

				} else if (!ownedBooks.isEmpty()) {
					// Upgrade existing tome
					WeaponRarity rarity = randomRarity();
					boxRarities[i] = rarity;

					List<String> ownedList = new ArrayList<>(ownedBooks.keySet());
					String bookName = ownedList.get(rand.nextInt(ownedList.size()));

					double currentTotal = ownedBooks.get(bookName).getValue();
					double increment = Book.incrementForRarity(bookName, rarity);
					double newTotal = currentTotal + increment;

					// The Book passed to addOrUpgradeBook carries only the INCREMENT.
					// addOrUpgradeBook adds it on top of the existing total itself.
					Book upgradeBook = new Book(bookName, rarity);

					String label = String.format("[%s] UPGRADE: %s   %s  →  %s", rarity, bookName,
							formatBookValue(bookName, currentTotal), formatBookValue(bookName, newTotal));

					boxes[i] = new GameButton(AppPanel.WIDTH / 2 - rectWidth / 2, yPos, rectWidth, rectHeight, label,
							() -> {
								gameObj.getPlayer().addOrUpgradeBook(upgradeBook);
								finishUpgrade();
							});
					boxes[i].setImage(getIconForBook(bookName));
				}

			} else {
				// ── WEAPON SLOT ───────────────────────────────────────────
				boolean offerNewWeapon = canAddWeapon && !availableWeapons.isEmpty()
						&& (rand.nextBoolean() || ownedWeapons.isEmpty());

				if (offerNewWeapon) {
					// New weapon (no rarity colour – stays grey)
					WeaponTypes type = availableWeapons.get(rand.nextInt(availableWeapons.size()));
					Weapon w = allWeapons.get(type);
					String label = "GET NEW WEAPON: " + formatName(type.name());

					boxes[i] = new GameButton(AppPanel.WIDTH / 2 - rectWidth / 2, yPos, rectWidth, rectHeight, label,
							() -> {
								gameObj.getPlayer().addWeapon(type, w);
								finishUpgrade();
							});
					boxes[i].setImage(w.getIcon());

				} else if (!ownedWeapons.isEmpty()) {
					// Upgrade existing weapon
					WeaponRarity rarity = randomRarity();
					boxRarities[i] = rarity;

					List<WeaponTypes> ownedList = new ArrayList<>(ownedWeapons);
					WeaponTypes type = ownedList.get(rand.nextInt(ownedList.size()));
					Weapon w = gameObj.getPlayer().getWeapons().get(type);
					WeaponUpgrades stat = getRandomStatForWeapon(w);

					double before = w.getStats().getOrDefault(stat, 0.0);
					double after = calculateProjectedValue(w, stat, rarity);

					String text = String.format("[%s] %s   %s: %s  →  %s", rarity, formatName(type.name()),
							getDisplayName(stat), formatStatValue(stat, before), formatStatValue(stat, after));

					final double finalAfter = after;
					boxes[i] = new GameButton(AppPanel.WIDTH / 2 - rectWidth / 2, yPos, rectWidth, rectHeight, text,
							() -> {
								w.getStats().put(stat, finalAfter);
								w.onUpgrade();
								finishUpgrade();
							});
					boxes[i].setImage(w.getIcon());
				}
			}

			if (boxes[i] != null)
				boxes[i].setTransparent(true);
		}
	}

	// =========================================================================
	// UPDATE / DRAW
	// =========================================================================
	public void update() {
		timer++;
		if (flashAlpha > 0)
			flashAlpha -= 0.05f;

		for (int i = particles.size() - 1; i >= 0; i--) {
			particles.get(i).update();
			if (particles.get(i).life <= 0)
				particles.remove(i);
		}
		for (GameButton b : boxes)
			if (b != null)
				b.update();
	}

	public void draw(Graphics2D g) {
		// Rotating golden rays
		g.setColor(new Color(255, 215, 0, 30));
		for (int i = 0; i < 8; i++) {
			double angle = (timer * 0.02) + (i * Math.PI / 4);
			int[] xPts = { AppPanel.WIDTH / 2, AppPanel.WIDTH / 2 + (int) (Math.cos(angle - 0.1) * 1500),
					AppPanel.WIDTH / 2 + (int) (Math.cos(angle + 0.1) * 1500) };
			int[] yPts = { AppPanel.HEIGHT / 2, AppPanel.HEIGHT / 2 + (int) (Math.sin(angle - 0.1) * 1500),
					AppPanel.HEIGHT / 2 + (int) (Math.sin(angle + 0.1) * 1500) };
			g.fillPolygon(xPts, yPts, 3);
		}

		// Boxes
		for (int i = 0; i < boxes.length; i++) {
			GameButton b = boxes[i];
			if (b == null)
				continue;

			Color baseColor = getSlotColor(i);
			drawBoxBackground(g, b, baseColor);
			drawSlotTypeLabel(g, b, boxIsBook[i]);
			b.draw(g);
		}

		// Particles
		for (PixelParticle p : particles) {
			g.setColor(p.color);
			g.fillRect((int) p.x, (int) p.y, p.size, p.size);
		}

		// Title
		g.setFont(new Font("Monospaced", Font.BOLD, 75));
		String msg = "LEVEL UP";
		int xText = AppPanel.WIDTH / 2 - g.getFontMetrics().stringWidth(msg) / 2;
		g.setColor(Color.BLACK);
		g.drawString(msg, xText + 5, 85);
		g.setColor(Color.YELLOW);
		g.drawString(msg, xText, 80);

		// Flash overlay
		if (flashAlpha > 0) {
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, flashAlpha));
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, AppPanel.WIDTH, AppPanel.HEIGHT);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
		}
	}

	// =========================================================================
	// DRAW HELPERS
	// =========================================================================

	/**
	 * Gradient-shaded box background. Book slots tinted purple, weapon slots
	 * grey/gold.
	 */
	private void drawBoxBackground(Graphics2D g, GameButton b, Color baseColor) {
		int pixelSize = 10;
		int half = b.getHeight() / 2;
		for (int py = 0; py < b.getHeight(); py += pixelSize) {
			float dist = Math.abs(py - half) / (float) half;
			float darkFactor = 0.5f + (dist * 0.5f);
			if (b.isHovering())
				darkFactor += 0.15f;

			int r = (int) (baseColor.getRed() * Math.min(1f, darkFactor));
			int gr = (int) (baseColor.getGreen() * Math.min(1f, darkFactor));
			int bl = (int) (baseColor.getBlue() * Math.min(1f, darkFactor));

			g.setColor(new Color(r, gr, bl));
			g.fillRect(b.getX(), b.getY() + py, b.getWidth(), pixelSize);
		}
	}

	/** Small "TOME" or "WEAPON" badge in the top-right corner of the box. */
	private void drawSlotTypeLabel(Graphics2D g, GameButton b, boolean isBook) {
		String text = isBook ? "TOME" : "WEAPON";
		Color color = isBook ? new Color(200, 150, 255) : new Color(130, 200, 255);

		g.setFont(new Font("Monospaced", Font.BOLD, 13));
		FontMetrics fm = g.getFontMetrics();
		int tw = fm.stringWidth(text);
		int th = fm.getHeight();
		int pad = 6;

		int lx = b.getX() + b.getWidth() - tw - pad * 2 - 6;
		int ly = b.getY() + pad;

		// Dark pill background
		g.setColor(new Color(0, 0, 0, 160));
		g.fillRoundRect(lx - pad, ly, tw + pad * 2, th, 6, 6);

		// Coloured text
		g.setColor(color);
		g.drawString(text, lx, ly + th - 4);
	}

	private Color getSlotColor(int i) {
		if (boxRarities[i] == null) {
			// New item: purple tint for tomes, grey for weapons
			return new Color(100, 100, 100);
		}
		return getRarityColor(boxRarities[i]);
	}

	private Color getRarityColor(WeaponRarity rarity) {
		if (rarity == null)
			return new Color(100, 100, 100);
		switch (rarity) {
		case BRONZE:
			return new Color(140, 70, 30);
		case SILVER:
			return new Color(160, 165, 175);
		case GOLD:
			return new Color(210, 170, 0);
		case DIAMOND:
			return new Color(0, 180, 220);
		default:
			return new Color(80, 80, 80);
		}
	}

	// =========================================================================
	// BOOK HELPERS
	// =========================================================================

	/**
	 * Returns a human-readable value string for a book stat (used in upgrade
	 * preview).
	 */
	private String formatBookValue(String bookName, double val) {
		switch (bookName) {
		case "EXP Book":
		case "Projectile Speed Book":
		case "Crit Rate Book":
		case "Luck Book":
		case "Gold Book":
		case "Cooldown Book":
		case "Size Book":
			return String.format("+%.0f%%", val);
		case "Damage Book":
			return String.format("+%.0f%%", val * 100.0);
		case "Max HP Book":
			return String.format("+%.0f HP", val);
		case "Quantity Book":
			return String.format("+%.0f projectile(s)", val);
		default:
			return String.format("%.1f", val);
		}
	}

	/**
	 * Mirrors Player.getIconForBook — both must stay in sync if new books are
	 * added.
	 */
	private BufferedImage getIconForBook(String name) {
		switch (name) {
		case "Gold Book":
			return Assets.GoldTomeIcon;
		case "Max HP Book":
			return Assets.HpTomeIcon;
		case "Luck Book":
			return Assets.LuckTomeIcon;
		case "Crit Rate Book":
			return Assets.CritTomeIcon;
		case "Projectile Speed Book":
			return Assets.ProjectileSpeedTomeIcon;
		case "Quantity Book":
			return Assets.ProjectileCountTomeIcon;
		case "Size Book":
			return Assets.SizeTomeIcon;
		case "EXP Book":
			return Assets.XpTomeIcon;
		case "Cooldown Book":
			return Assets.CooldownTomeIcon;
		case "Damage Book":
			return Assets.DamageTomeIcon;
		default:
			return Assets.CursedTomeIcon;
		}
	}

	// =========================================================================
	// WEAPON STAT HELPERS
	// =========================================================================

	private double calculateProjectedValue(Weapon w, WeaponUpgrades upgrade, WeaponRarity rarity) {
		double currentVal = w.getStats().getOrDefault(upgrade, 0.0);
		double mult = rarityMult(rarity);
		
		int anvilMult = gameObj.getPlayer().getArtifactManager().getAnvilMultiplier();

		switch (upgrade) {
		case ProjectileCount:
		case ProjectileBounce:
			return currentVal + (1.0 * mult * anvilMult);
		case ProjectileSpeed:
			return currentVal + (2.0 * mult * anvilMult);
		case AttackDamage: {
			double step = w.getBaseStats().getOrDefault(WeaponUpgrades.AttackDamage, 10.0) / 5.0;
			return currentVal + (step * mult * anvilMult);
		}
		case AttackSpeed: {
			double red;
			switch (rarity) {
			case SILVER:
				red = 0.05;
				break;
			case GOLD:
				red = 0.08;
				break;
			case DIAMOND:
				red = 0.12;
				break;
			default:
				red = 0.03;
				break;
			}
			return currentVal * (1.0 - red * anvilMult);
		}
		case AttackSize:
			return currentVal + (mult * 0.20 * anvilMult);
		case CriticalChance:
			return currentVal + (anvilMult * ((rarity.ordinal() * 0.02) + 0.03));
		case CriticalDamage:
			return currentVal + (((rarity.ordinal() * 0.15) + 0.10) * anvilMult);
		case Duration:
			return currentVal + (mult * 10 * anvilMult);
		default:
			return currentVal * anvilMult;
		}
	}

	private double rarityMult(WeaponRarity rarity) {
		switch (rarity) {
		case SILVER:
			return 1.2;
		case GOLD:
			return 1.4;
		case DIAMOND:
			return 2.0;
		default:
			return 1.0;
		}
	}

	private WeaponRarity randomRarity() {
		return WeaponRarity.values()[rand.nextInt(WeaponRarity.values().length)];
	}

	private WeaponUpgrades getRandomStatForWeapon(Weapon w) {
		List<WeaponUpgrades> valid = new ArrayList<>();
		for (WeaponUpgrades u : WeaponUpgrades.values()) {
			if (w.getStats().containsKey(u) && w.getStats().get(u) > 0 && u != WeaponUpgrades.Range)
				valid.add(u);
		}
		return valid.isEmpty() ? WeaponUpgrades.AttackDamage : valid.get(rand.nextInt(valid.size()));
	}

	private String formatStatValue(WeaponUpgrades stat, double val) {
		if (stat == WeaponUpgrades.AttackSpeed || stat == WeaponUpgrades.Duration)
			return String.format("%.2fs", val / 60.0);
		if (isPercentStat(stat))
			return String.format("%.0f%%", val * 100);
		return String.format("%.1f", val);
	}

	private boolean isPercentStat(WeaponUpgrades stat) {
		return stat == WeaponUpgrades.CriticalChance || stat == WeaponUpgrades.CriticalDamage
				|| stat == WeaponUpgrades.AttackSize;
	}

	private String getDisplayName(WeaponUpgrades stat) {
		switch (stat) {
		case AttackSpeed:
			return "Cooldown";
		case AttackDamage:
			return "Damage";
		case AttackSize:
			return "Area";
		case ProjectileCount:
			return "Amount";
		case ProjectileSpeed:
			return "Speed";
		case ProjectileBounce:
			return "Bounces";
		case CriticalChance:
			return "Crit Rate";
		case CriticalDamage:
			return "Crit Damage";
		default:
			return stat.toString();
		}
	}

	private String formatName(String name) {
		String[] words = name.split("_");
		StringBuilder sb = new StringBuilder();
		for (String w : words)
			sb.append(w.substring(0, 1).toUpperCase()).append(w.substring(1).toLowerCase()).append(" ");
		return sb.toString().trim();
	}

	// =========================================================================
	// LEVEL-UP EFFECTS
	// =========================================================================

	private void startLevelUpEffect() {
		particles.clear();
		flashAlpha = 1.0f;
		for (int i = 0; i < 60; i++)
			particles.add(new PixelParticle(AppPanel.WIDTH / 2, AppPanel.HEIGHT / 2, rand));
	}

	private void finishUpgrade() {
		gameObj.setState(gameObj.getStateOpen());
	}

	// =========================================================================
	// PIXEL PARTICLE
	// =========================================================================
	private static class PixelParticle {
		float x, y, vx, vy;
		int size, life;
		Color color;

		PixelParticle(int startX, int startY, Random rand) {
			x = startX;
			y = startY;
			vx = (rand.nextFloat() - 0.5f) * 15f;
			vy = (rand.nextFloat() - 0.5f) * 15f;
			size = rand.nextInt(6) + 4;
			life = 30 + rand.nextInt(30);
			color = rand.nextBoolean() ? Color.YELLOW : Color.WHITE;
		}

		void update() {
			x += vx;
			y += vy;
			vy += 0.3f;
			life--;
		}
	}
}