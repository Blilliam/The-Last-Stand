package Open.Upgrades;

import java.awt.*;
import java.util.*;
import java.util.List;

import javax.security.auth.x500.X500Principal;

import Open.Upgrades.Book;
import Open.Weapons.*;
import main.AppPanel;
import main.GameButton;
import main.GameObject;
import main.enums.WeaponRarity;
import main.enums.WeaponTypes;
import main.enums.WeaponUpgrades;

public class Upgrades {
	private GameObject gameObj;
	private GameButton[] boxes = new GameButton[3];
	private WeaponRarity[] boxRarities = new WeaponRarity[3]; // null = New Item (Grey)
	private EnumMap<WeaponTypes, Weapon> allWeapons;

	private final int rectWidth = 900;
	private final int rectHeight = 200;

	private List<PixelParticle> particles = new ArrayList<>();
	private float flashAlpha = 0f;
	private long timer = 0;
	private Random rand = new Random();

	private String[] bookPool = { "EXP Book", "Size Book", "Quantity Over Quality Book", "Projectile Speed Book",
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

	public void shuffleUpgrades() {
		startLevelUpEffect();

		Set<WeaponTypes> ownedWeapons = gameObj.getPlayer().getWeapons().keySet();
		Map<String, Book> ownedBooks = gameObj.getPlayer().getOwnedBooks();

		List<WeaponTypes> availableWeapons = new ArrayList<>();
		for (WeaponTypes t : allWeapons.keySet()) {
			if (!ownedWeapons.contains(t)) {
				availableWeapons.add(t);
			}
		}

		List<String> availableBooks = new ArrayList<>();
		for (String s : bookPool) {
			if (!ownedBooks.containsKey(s)) {
				availableBooks.add(s);
			}
		}

		for (int i = 0; i < boxes.length; i++) {
			int yPos = 100 + (i * 300);
			boxRarities[i] = null;

			float categoryRoll = rand.nextFloat();
//CHANGE LATER

			if (/* categoryRoll < 0.50f */ true) {
				// --- WEAPON SLOT ---
				boolean canBuy = false;
				if (gameObj.getPlayer().getWeapons().size() < gameObj.getPlayer().getMAX_WEAPONS()) {
					if (!availableWeapons.isEmpty()) {
						canBuy = true;
					}
				}

				if (canBuy && (rand.nextBoolean() || ownedWeapons.isEmpty())) {
					// NEW WEAPON (Grey Box)
					WeaponTypes type = availableWeapons.get(rand.nextInt(availableWeapons.size()));
					boxes[i] = new GameButton(AppPanel.WIDTH / 2 - rectWidth / 2, yPos, rectWidth, rectHeight,
							"GET NEW WEAPON: " + formatName(type.name()), () -> {
								gameObj.getPlayer().addWeapon(type, allWeapons.get(type));
								finishUpgrade();
							});
					boxes[i].setImage(allWeapons.get(type).getIcon());
				} else if (!ownedWeapons.isEmpty()) {
					// UPGRADE WEAPON (Rarity Box)
					WeaponRarity rarity = WeaponRarity.values()[rand.nextInt(WeaponRarity.values().length)];
					boxRarities[i] = rarity;

					List<WeaponTypes> ownedList = new ArrayList<>(ownedWeapons);
					WeaponTypes type = ownedList.get(rand.nextInt(ownedList.size()));
					Weapon w = gameObj.getPlayer().getWeapons().get(type);
					WeaponUpgrades stat = getRandomStatForWeapon(w);

					double before = w.getStats().getOrDefault(stat, 0.0);
					double after = calculateProjectedValue(w, stat, rarity);

					String text = String.format("[%s] %s %s: %s -> %s", rarity, formatName(type.name()),
							getDisplayName(stat), formatStatValue(stat, before), formatStatValue(stat, after));

					boxes[i] = new GameButton(AppPanel.WIDTH / 2 - rectWidth / 2, yPos, rectWidth, rectHeight, text,
							() -> {
								w.getStats().put(stat, after);
								w.onUpgrade();
								finishUpgrade();
							});
					boxes[i].setImage(w.getIcon());
				}
				
			} else {
				// --- BOOK SLOT ---
				boolean canBuyBook = false;
				if (ownedBooks.size() < gameObj.getPlayer().getMAX_BOOKS()) {
					if (!availableBooks.isEmpty()) {
						canBuyBook = true;
					}
				}

				if (canBuyBook && (rand.nextBoolean() || ownedBooks.isEmpty())) {
					// NEW BOOK (Grey Box)
					String bookName = availableBooks.get(rand.nextInt(availableBooks.size()));
					Book book = new Book(bookName, WeaponRarity.BRONZE);
					boxes[i] = new GameButton(AppPanel.WIDTH / 2 - rectWidth / 2, yPos, rectWidth, rectHeight,
							"GET NEW BOOK: " + bookName, () -> {
								gameObj.getPlayer().addOrUpgradeBook(book);
								finishUpgrade();
							});
				} else if (!ownedBooks.isEmpty()) {
					// UPGRADE BOOK (Rarity Box with Preview)
					WeaponRarity rarity = WeaponRarity.values()[rand.nextInt(WeaponRarity.values().length)];
					boxRarities[i] = rarity;

					List<String> ownedBookList = new ArrayList<>(ownedBooks.keySet());
					String bookName = ownedBookList.get(rand.nextInt(ownedBookList.size()));

					double cur = ownedBooks.get(bookName).getValue();
					double next = getProjectedBookValue(bookName, rarity);

					String label = "[" + rarity + "] " + bookName + ": ";
					if (bookName.contains("EXP") || bookName.contains("Crit") || bookName.contains("Gold")) {
						label += String.format("%.0f%% -> %.0f%%", cur, next);
					} else {
						label += String.format("%.1f -> %.1f", cur, next);
					}

					Book upgradedBook = new Book(bookName, rarity);
					boxes[i] = new GameButton(AppPanel.WIDTH / 2 - rectWidth / 2, yPos, rectWidth, rectHeight, label,
							() -> {
								gameObj.getPlayer().addOrUpgradeBook(upgradedBook);
								finishUpgrade();
							});
				}
			}

			if (boxes[i] != null) {
				boxes[i].setTransparent(true);
			}
		}
	}

	private double getProjectedBookValue(String name, WeaponRarity rarity) {
		double multiplier = 1.0;
		if (rarity == WeaponRarity.SILVER)
			multiplier = 1.5;
		else if (rarity == WeaponRarity.GOLD)
			multiplier = 2.5;
		else if (rarity == WeaponRarity.DIAMOND)
			multiplier = 4.0;

		if (name.equals("Max HP Book"))
			return 5.0 * multiplier;
		if (name.equals("EXP Book"))
			return 10.0 * multiplier;
		if (name.equals("Damage Book"))
			return 2.0 * multiplier;
		if (name.equals("Cooldown Book"))
			return 1.5 * multiplier;
		if (name.equals("Crit Rate Book"))
			return 5.0 * multiplier;
		if (name.equals("Gold Book"))
			return 15.0 * multiplier;

		return 1.0 * multiplier;
	}

	private Color getRarityColor(WeaponRarity rarity) {
		if (rarity == null) {
			return new Color(100, 100, 100);
		}
		if (rarity == WeaponRarity.BRONZE)
			return new Color(140, 70, 30);
		if (rarity == WeaponRarity.SILVER)
			return new Color(160, 165, 175);
		if (rarity == WeaponRarity.GOLD)
			return new Color(210, 170, 0);
		if (rarity == WeaponRarity.DIAMOND)
			return new Color(0, 180, 220);
		return new Color(80, 80, 80);
	}

	public void update() {
		timer++;
		if (flashAlpha > 0)
			flashAlpha -= 0.05f;
		for (int i = particles.size() - 1; i >= 0; i--) {
			particles.get(i).update();
			if (particles.get(i).life <= 0)
				particles.remove(i);
		}
		for (GameButton b : boxes) {
			if (b != null)
				b.update();
		}
	}

	public void draw(Graphics2D g) {
		g.setColor(new Color(255, 215, 0, 30));
		for (int i = 0; i < 8; i++) {
			double angle = (timer * 0.02) + (i * Math.PI / 4);
			int[] xPoints = { AppPanel.WIDTH / 2, AppPanel.WIDTH / 2 + (int) (Math.cos(angle - 0.1) * 1500),
					AppPanel.WIDTH / 2 + (int) (Math.cos(angle + 0.1) * 1500) };
			int[] yPoints = { AppPanel.HEIGHT / 2, AppPanel.HEIGHT / 2 + (int) (Math.sin(angle - 0.1) * 1500),
					AppPanel.HEIGHT / 2 + (int) (Math.sin(angle + 0.1) * 1500) };
			g.fillPolygon(xPoints, yPoints, 3);
		}

		for (int i = 0; i < boxes.length; i++) {
			GameButton b = boxes[i];
			if (b == null)
				continue;

			Color baseColor = getRarityColor(boxRarities[i]);
			int pixelSize = 10;
			int half = b.getHeight() / 2;
			for (int py = 0; py < b.getHeight(); py += pixelSize) {
				float dist = Math.abs(py - half) / (float) half;
				float darkFactor = 0.5f + (dist * 0.5f);
				if (b.isHovering())
					darkFactor += 0.15f;

				int r = (int) (baseColor.getRed() * Math.min(1, darkFactor));
				int gr = (int) (baseColor.getGreen() * Math.min(1, darkFactor));
				int bl = (int) (baseColor.getBlue() * Math.min(1, darkFactor));

				g.setColor(new Color(r, gr, bl));
				g.fillRect(b.getX(), b.getY() + py, b.getWidth(), pixelSize);
			}
			b.draw(g);
		}

		for (PixelParticle p : particles) {
			g.setColor(p.color);
			g.fillRect((int) p.x, (int) p.y, p.size, p.size);
		}

		g.setFont(new Font("Monospaced", Font.BOLD, 75));
		String msg = "LEVEL UP";
		int xText = AppPanel.WIDTH / 2 - g.getFontMetrics().stringWidth(msg) / 2;
		g.setColor(Color.BLACK);
		g.drawString(msg, xText + 5, 85);
		g.setColor(Color.YELLOW);
		g.drawString(msg, xText, 80);

		if (flashAlpha > 0) {
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, flashAlpha));
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, AppPanel.WIDTH, AppPanel.HEIGHT);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
		}
	}

	private String formatStatValue(WeaponUpgrades stat, double val) {
		if (stat == WeaponUpgrades.AttackSpeed || stat == WeaponUpgrades.Duration)
			return String.format("%.2fs", val / 60.0);
		if (isPercentStat(stat))
			return String.format("%.0f%%", val * 100);
		return String.format("%.1f", val);
	}

	private String formatName(String name) {
		String[] words = name.split("_");
		StringBuilder sb = new StringBuilder();
		for (String w : words) {
			sb.append(w.substring(0, 1).toUpperCase()).append(w.substring(1).toLowerCase()).append(" ");
		}
		return sb.toString().trim();
	}

	private String getDisplayName(WeaponUpgrades stat) {
		if (stat == WeaponUpgrades.AttackSpeed)
			return "Cooldown";
		if (stat == WeaponUpgrades.AttackDamage)
			return "Damage";
		if (stat == WeaponUpgrades.AttackSize)
			return "Area";
		if (stat == WeaponUpgrades.ProjectileCount)
			return "Amount";
		if (stat == WeaponUpgrades.ProjectileSpeed)
			return "Speed";
		if (stat == WeaponUpgrades.ProjectileBounce)
			return "Bounces";
		if (stat == WeaponUpgrades.CriticalChance)
			return "Crit Rate";
		if (stat == WeaponUpgrades.CriticalDamage)
			return "Crit Damage";
		return stat.toString();
	}

	private boolean isPercentStat(WeaponUpgrades stat) {
		if (stat == WeaponUpgrades.CriticalChance || stat == WeaponUpgrades.CriticalDamage
				|| stat == WeaponUpgrades.AttackSize) {
			return true;
		}
		return false;
	}

	private double calculateProjectedValue(Weapon w, WeaponUpgrades upgrade, WeaponRarity rarity) {
		double currentVal = w.getStats().getOrDefault(upgrade, 0.0);
		double multiplier = 1.0;
		if (rarity == WeaponRarity.SILVER)
			multiplier = 1.2;
		else if (rarity == WeaponRarity.GOLD)
			multiplier = 1.4;
		else if (rarity == WeaponRarity.DIAMOND)
			multiplier = 2.0;

		if (upgrade == WeaponUpgrades.ProjectileCount || upgrade == WeaponUpgrades.ProjectileBounce)
			return currentVal + (1.0 * multiplier);
		if (upgrade == WeaponUpgrades.ProjectileSpeed)
			return currentVal + (2.0 * multiplier);
		if (upgrade == WeaponUpgrades.AttackDamage) {
			double step = w.getBaseStats().getOrDefault(WeaponUpgrades.AttackDamage, 10.0) / 5.0;
			return currentVal + (step * multiplier);
		}
		if (upgrade == WeaponUpgrades.AttackSpeed) {
			double red = 0.03;
			if (rarity == WeaponRarity.SILVER)
				red = 0.05;
			else if (rarity == WeaponRarity.GOLD)
				red = 0.08;
			else if (rarity == WeaponRarity.DIAMOND)
				red = 0.12;
			return currentVal * (1.0 - red);
		}
		if (upgrade == WeaponUpgrades.AttackSize)
			return currentVal + (multiplier * +0.20);
		if (upgrade == WeaponUpgrades.CriticalChance)
			return currentVal + (rarity.ordinal() * 0.02 + 0.03);
		if (upgrade == WeaponUpgrades.CriticalDamage)
			return currentVal + (rarity.ordinal() * 0.15 + 0.10);
		if (upgrade == WeaponUpgrades.Duration)
			return currentVal + (multiplier * 10);

		return currentVal;
	}

	private WeaponUpgrades getRandomStatForWeapon(Weapon w) {
		List<WeaponUpgrades> validStats = new ArrayList<>();
		for (WeaponUpgrades upgrade : WeaponUpgrades.values()) {
			if (w.getStats().containsKey(upgrade) && w.getStats().get(upgrade) > 0 && upgrade != WeaponUpgrades.Range) {
				validStats.add(upgrade);
			}
		}
		if (validStats.isEmpty())
			return WeaponUpgrades.AttackDamage;
		return validStats.get(rand.nextInt(validStats.size()));
	}

	private void startLevelUpEffect() {
		particles.clear();
		flashAlpha = 1.0f;
		for (int i = 0; i < 60; i++) {
			particles.add(new PixelParticle(AppPanel.WIDTH / 2, AppPanel.HEIGHT / 2, rand));
		}
	}

	private void finishUpgrade() {
		gameObj.setState(gameObj.getStateOpen());
	}

	private static class PixelParticle {
		float x, y, vx, vy;
		int size, life;
		Color color;

		public PixelParticle(int startX, int startY, Random rand) {
			x = startX;
			y = startY;
			vx = (rand.nextFloat() - 0.5f) * 15f;
			vy = (rand.nextFloat() - 0.5f) * 15f;
			size = rand.nextInt(6) + 4;
			life = 30 + rand.nextInt(30);
			if (rand.nextBoolean())
				color = Color.YELLOW;
			else
				color = Color.WHITE;
		}

		public void update() {
			x += vx;
			y += vy;
			vy += 0.3f;
			life--;
		}
	}
}