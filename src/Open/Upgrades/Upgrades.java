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
import main.MusicManager.SFX;
import main.enums.WeaponRarity;
import main.enums.WeaponTypes;
import main.enums.WeaponUpgrades;

public class Upgrades {
	private GameObject gameObj;
	private GameButton[] boxes = new GameButton[3];
	private WeaponRarity[] boxRarities = new WeaponRarity[3];
	private boolean[] boxIsBook = new boolean[3];

	private EnumMap<WeaponTypes, Weapon> allWeapons;

	private final int rectWidth = AppPanel.WIDTH * 2/3;
	private final int rectHeight = AppPanel.HEIGHT / 5;

	private List<PixelParticle> particles = new ArrayList<>();
	private float flashAlpha = 0f;
	private long timer = 0;
	private Random rand = new Random();

	private static final float BOOK_CHANCE = 0.50f;

	private final String[] bookPool = { "EXP Book", "Size Book", "Quantity Book", "Projectile Speed Book",
			"Crit Rate Book", "Max HP Book", "Gold Book", "Damage Book", "Cooldown Book" };

	// ── Reroll state ──────────────────────────────────────────────────────────
	private GameButton rerollButton;
	private static int rerollCount = 0;
	private static final int MAX_REROLLS = 5;
	private boolean isRerolling = false;

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
	// REROLL HELPERS
	// =========================================================================

	private int getRerollCost() {
		int base = (int) (Open.Entities.Interactible.Chest.BASE_COST / 2.0 * Math.pow(gameObj.getMap().getStage(), 2));
		return (int) (base * Math.pow(1.5, rerollCount));
	}

	private void tryReroll() {
		if (rerollCount >= MAX_REROLLS)
			return;
		int cost = getRerollCost();
		if (gameObj.getPlayer().getGold() < cost)
			return;
		gameObj.getPlayer().setGold(gameObj.getPlayer().getGold() - cost);
		rerollCount++;
		isRerolling = true;
		shuffleUpgrades();
		isRerolling = false;
	}

	private void buildRerollButton() {
		boolean maxed = rerollCount >= MAX_REROLLS;
		boolean canAfford = !maxed && gameObj.getPlayer().getGold() >= getRerollCost();

		String label = maxed ? "NO REROLLS LEFT"
				: "REROLL   $" + getRerollCost() + "  (" + (MAX_REROLLS - rerollCount) + " left)";

		Color bg = canAfford ? new Color(80, 40, 0) : new Color(40, 40, 40);
		Color border = canAfford ? Color.ORANGE : Color.GRAY;

		rerollButton = new GameButton(AppPanel.WIDTH / 2 - 200, AppPanel.HEIGHT - 110, 400, 65, label, this::tryReroll,
				bg, border);
	}

	// =========================================================================
	// SHUFFLE
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

	    // --- uniqueness set: prevent same (item, upgrade, rarity) triple or same NEW item from appearing twice
	    Set<String> seenCombos = new HashSet<>();

	    for (int i = 0; i < boxes.length; i++) {
	        int yPos = 100 + (i * 300);

	        boxes[i] = null;
	        boxRarities[i] = null;
	        boxIsBook[i] = false;

	        boolean preferBook = rand.nextFloat() < BOOK_CHANCE;

	        boolean hasBookContent = (canAddBook && !availableBooks.isEmpty()) || !ownedBooks.isEmpty();
	        boolean hasWeaponContent = (canAddWeapon && !availableWeapons.isEmpty()) || !ownedWeapons.isEmpty();

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
	            continue;

	        boxIsBook[i] = useBook;

	        if (useBook) {
	            boolean offerNewBook = canAddBook && !availableBooks.isEmpty()
	                    && (ownedBooks.isEmpty() || rand.nextFloat() < 0.75f);

	            if (offerNewBook) {
	                // Try to pick a unique NEW book for this shuffle
	                final int MAX_ATTEMPTS_NEW_BOOK = 30;
	                String bookName = null;
	                Book book = null;
	                boolean foundNewBook = false;

	                List<String> availBooks = new ArrayList<>(availableBooks);

	                for (int attempt = 0; attempt < MAX_ATTEMPTS_NEW_BOOK && !availBooks.isEmpty(); attempt++) {
	                    int idx = rand.nextInt(availBooks.size());
	                    bookName = availBooks.get(idx);
	                    String newKey = "NEW|BOOK|" + bookName;
	                    if (!seenCombos.contains(newKey)) {
	                        seenCombos.add(newKey);
	                        foundNewBook = true;
	                        book = new Book(bookName, WeaponRarity.BRONZE);
	                        break;
	                    } else {
	                        availBooks.remove(idx);
	                    }
	                }

	                if (!foundNewBook) {
	                    // no unique new book found; skip this slot
	                    continue;
	                }

	                final Book bookForLambda = book;
	                String label = "NEW: " + bookName + "   [" + bookForLambda.getDescription() + "]";

	                boxes[i] = new GameButton(AppPanel.WIDTH / 2 - rectWidth / 2, yPos, rectWidth, rectHeight, label,
	                        () -> {
	                            gameObj.getPlayer().addOrUpgradeBook(bookForLambda);
	                            finishUpgrade();
	                        });
	                boxes[i].setImage(getIconForBook(bookName));

	            } else if (!ownedBooks.isEmpty()) {
	                // try to pick a unique (bookName, rarity)
	                final int MAX_ATTEMPTS = 20;
	                WeaponRarity rarity = null;
	                String bookName = null;
	                boolean found = false;

	                List<String> ownedList = new ArrayList<>(ownedBooks.keySet());

	                for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
	                    rarity = randomRarity();
	                    bookName = ownedList.get(rand.nextInt(ownedList.size()));
	                    String key = "BOOK|" + bookName + "|" + rarity.name();
	                    if (!seenCombos.contains(key)) {
	                        seenCombos.add(key);
	                        found = true;
	                        break;
	                    }
	                }

	                if (!found) {
	                    // fallback: skip this slot to avoid duplicates
	                    continue;
	                }

	                boxRarities[i] = rarity;

	                double currentTotal = ownedBooks.get(bookName).getValue();
	                double increment = Book.incrementForRarity(bookName, rarity);
	                double newTotal = currentTotal + increment;

	                Book upgradeBook = new Book(bookName, rarity);
	                final Book upgradeBookForLambda = upgradeBook;

	                String label = String.format("[%s] UPGRADE: %s   %s  →  %s", rarity, bookName,
	                        formatBookValue(bookName, currentTotal), formatBookValue(bookName, newTotal));

	                boxes[i] = new GameButton(AppPanel.WIDTH / 2 - rectWidth / 2, yPos, rectWidth, rectHeight, label,
	                        () -> {
	                            gameObj.getPlayer().addOrUpgradeBook(upgradeBookForLambda);
	                            finishUpgrade();
	                        });
	                boxes[i].setImage(getIconForBook(bookName));
	            }

	        } else {
	            boolean offerNewWeapon = canAddWeapon && !availableWeapons.isEmpty()
	                    && (rand.nextBoolean() || ownedWeapons.isEmpty());

	            if (offerNewWeapon) {
	                // Try to pick a unique NEW weapon for this shuffle
	                final int MAX_ATTEMPTS_NEW = 30;
	                WeaponTypes type = null;
	                Weapon w = null;
	                boolean foundNew = false;

	                List<WeaponTypes> avail = new ArrayList<>(availableWeapons);

	                for (int attempt = 0; attempt < MAX_ATTEMPTS_NEW && !avail.isEmpty(); attempt++) {
	                    int idx = rand.nextInt(avail.size());
	                    type = avail.get(idx);
	                    String newKey = "NEW|WEAPON|" + type.name();
	                    if (!seenCombos.contains(newKey)) {
	                        seenCombos.add(newKey);
	                        foundNew = true;
	                        w = allWeapons.get(type);
	                        break;
	                    } else {
	                        avail.remove(idx);
	                    }
	                }

	                if (!foundNew) {
	                    // no unique new weapon found; skip offering a duplicate
	                    continue;
	                }

	                final WeaponTypes typeForLambda = type;
	                final Weapon wForLambda = w;
	                String label = "GET NEW WEAPON: " + formatName(typeForLambda.name());

	                boxes[i] = new GameButton(AppPanel.WIDTH / 2 - rectWidth / 2, yPos, rectWidth, rectHeight, label,
	                        () -> {
	                            gameObj.getPlayer().addWeapon(typeForLambda, wForLambda);
	                            finishUpgrade();
	                        });
	                boxes[i].setImage(wForLambda.getIcon());

	            } else if (!ownedWeapons.isEmpty()) {
	                // try to pick a unique (weaponType, stat, rarity)
	                final int MAX_ATTEMPTS = 30;
	                WeaponRarity rarity = null;
	                WeaponTypes type = null;
	                WeaponUpgrades stat = null;
	                Weapon w = null;
	                boolean found = false;

	                List<WeaponTypes> ownedList = new ArrayList<>(ownedWeapons);

	                for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
	                    rarity = randomRarity();
	                    type = ownedList.get(rand.nextInt(ownedList.size()));
	                    w = gameObj.getPlayer().getWeapons().get(type);
	                    stat = getRandomStatForWeapon(w);
	                    String key = "WEAPON|" + type.name() + "|" + stat.name() + "|" + rarity.name();
	                    if (!seenCombos.contains(key)) {
	                        seenCombos.add(key);
	                        found = true;
	                        break;
	                    }
	                }

	                if (!found) {
	                    // no unique weapon upgrade found after attempts; skip this slot
	                    continue;
	                }

	             // ... after computing `stat`, `w`, and `after`
	                boxRarities[i] = rarity;

	                double before = w.getStats().getOrDefault(stat, 0.0);
	                double after = calculateProjectedValue(w, stat, rarity);

	                String text = String.format("[%s] %s   %s: %s  →  %s", rarity, formatName(type.name()),
	                        getDisplayName(stat), formatStatValue(stat, before), formatStatValue(stat, after));

	                final Weapon wForLambda = w;
	                final WeaponUpgrades statForLambda = stat;
	                final double finalAfter = after;

	                boxes[i] = new GameButton(AppPanel.WIDTH / 2 - rectWidth / 2, yPos, rectWidth, rectHeight, text,
	                        () -> {
	                            wForLambda.getStats().put(statForLambda, finalAfter);
	                            wForLambda.onUpgrade();
	                            finishUpgrade();
	                        });
	                boxes[i].setImage(wForLambda.getIcon());

	            }
	        }

	        if (boxes[i] != null)
	            boxes[i].setTransparent(true);
	    }

	    buildRerollButton();
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

		if (rerollButton != null)
			rerollButton.update();
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

		// Reroll button
		buildRerollButton(); // rebuild each frame so label/color stays fresh
		if (rerollButton != null)
			rerollButton.draw(g);

		// Particles
		
		for (int i = 0; i < particles.size(); i ++) {
			PixelParticle p = particles.get(i);
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

		g.setColor(new Color(0, 0, 0, 160));
		g.fillRoundRect(lx - pad, ly, tw + pad * 2, th, 6, 6);

		g.setColor(color);
		g.drawString(text, lx, ly + th - 4);
	}

	private Color getSlotColor(int i) {
		if (boxRarities[i] == null)
			return new Color(100, 100, 100);
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
			return String.format("+%.1f", val);
		default:
			return String.format("%.1f", val);
		}
	}

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
		if (valid.isEmpty())
			return WeaponUpgrades.AttackDamage;
		return valid.get(rand.nextInt(valid.size()));
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
		/// CALL SOUND HERE
		gameObj.getMusicManager().playSFX(SFX.LEVELUP);

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