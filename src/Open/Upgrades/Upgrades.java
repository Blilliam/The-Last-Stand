package Open.Upgrades;

import java.awt.*;
import java.util.*;
import java.util.List;

import Open.Weapons.*;
import main.AppPanel;
import main.GameButton;
import main.GameObject;
import main.enums.WeaponRarity;
import main.enums.WeaponTypes;
import main.enums.WeaponUpgrades;

public class Upgrades {
    private GameObject gameObj;
    private GameButton[] boxes      = new GameButton[3];
    private WeaponRarity[] boxRarities = new WeaponRarity[3];
    private EnumMap<WeaponTypes, Weapon> allWeapons;

    private final int rectWidth  = 900;
    private final int rectHeight = 200;

    private List<PixelParticle> particles = new ArrayList<>();
    private float flashAlpha = 0f;
    private long  timer      = 0;
    private Random rand      = new Random();

    private static final String[] BOOK_POOL = {
        "EXP Book", "Size Book", "Quantity Over Quality Book",
        "Projectile Speed Book", "Crit Rate Book", "Max HP Book",
        "Gold Book", "Damage Book", "Cooldown Book"
    };

    public Upgrades(GameObject gameObj) {
        this.gameObj = gameObj;
        allWeapons   = new EnumMap<>(WeaponTypes.class);
        allWeapons.put(WeaponTypes.Aura,          new AuraWeapon(gameObj));
        allWeapons.put(WeaponTypes.Banana,         new BananaWeapon(gameObj));
        allWeapons.put(WeaponTypes.Bone,           new BoneWeapon(gameObj));
        allWeapons.put(WeaponTypes.FireStaff,      new FireStaffWeapon(gameObj));
        allWeapons.put(WeaponTypes.PewPew,         new PewPewWeapon(gameObj));
        allWeapons.put(WeaponTypes.Sword,          new SwordWeapon(gameObj));
        allWeapons.put(WeaponTypes.Katana,         new KatanaWeapon(gameObj));
        allWeapons.put(WeaponTypes.Dexecutioner,   new DexecutionerWeapon(gameObj));
    }

    // ─────────────────────────────────────────────────────────────────────────
    public void shuffleUpgrades() {
        startLevelUpEffect();

        Set<WeaponTypes>    ownedWeapons = gameObj.getPlayer().getWeapons().keySet();
        Map<String, Book>   ownedBooks   = gameObj.getPlayer().getOwnedBooks();

        // Weapons not yet owned by the player
        List<WeaponTypes> availableWeapons = new ArrayList<>();
        for (WeaponTypes t : allWeapons.keySet())
            if (!ownedWeapons.contains(t)) availableWeapons.add(t);

        // Books not yet owned (first-pick candidates)
        List<String> availableBooks = new ArrayList<>();
        for (String s : BOOK_POOL)
            if (!ownedBooks.containsKey(s)) availableBooks.add(s);

        // ── Deduplication trackers ────────────────────────────────────────────
        Set<WeaponTypes> offeredNewWeapons      = new HashSet<>();
        Set<String>      offeredWeaponUpgrades  = new HashSet<>(); // "Type:Stat"
        Set<String>      offeredNewBooks        = new HashSet<>();
        Set<String>      offeredBookUpgrades    = new HashSet<>(); // "BookName:Rarity"

        for (int i = 0; i < boxes.length; i++) {
            int yPos = 100 + (i * 300);
            boxRarities[i] = null;

            // ── Decide: weapon slot or book slot ─────────────────────────────
            // (currently always weapon — keep the original true-branch logic)
            if (true) {

                boolean canAddWeapon = gameObj.getPlayer().getWeapons().size()
                                       < gameObj.getPlayer().getMAX_WEAPONS()
                                       && !availableWeapons.isEmpty();

                List<WeaponTypes> freshWeapons = new ArrayList<>(availableWeapons);
                freshWeapons.removeAll(offeredNewWeapons);

                if (canAddWeapon && (rand.nextBoolean() || ownedWeapons.isEmpty()) && !freshWeapons.isEmpty()) {
                    // ── NEW WEAPON ────────────────────────────────────────────
                    WeaponTypes type = freshWeapons.get(rand.nextInt(freshWeapons.size()));
                    offeredNewWeapons.add(type);

                    boxes[i] = new GameButton(
                        AppPanel.WIDTH / 2 - rectWidth / 2, yPos, rectWidth, rectHeight,
                        "GET NEW WEAPON: " + formatName(type.name()),
                        () -> { gameObj.getPlayer().addWeapon(type, allWeapons.get(type)); finishUpgrade(); });
                    boxes[i].setImage(allWeapons.get(type).getIcon());

                } else if (!ownedWeapons.isEmpty()) {
                    // ── UPGRADE WEAPON ────────────────────────────────────────
                    WeaponRarity rarity = rollRarity();
                    boxRarities[i] = rarity;

                    List<WeaponTypes> ownedList = new ArrayList<>(ownedWeapons);
                    Collections.shuffle(ownedList, rand);

                    WeaponTypes type = null;
                    WeaponUpgrades stat = null;

                    for (WeaponTypes t : ownedList) {
                        Weapon w = gameObj.getPlayer().getWeapons().get(t);
                        WeaponUpgrades s = getRandomStatForWeapon(w);
                        String key = t.name() + ":" + s.name();
                        if (!offeredWeaponUpgrades.contains(key)) {
                            type = t; stat = s; break;
                        }
                    }
                    if (type == null) { // all combos exhausted — fall back
                        type = ownedList.get(rand.nextInt(ownedList.size()));
                        stat = getRandomStatForWeapon(gameObj.getPlayer().getWeapons().get(type));
                    }
                    offeredWeaponUpgrades.add(type.name() + ":" + stat.name());

                    Weapon w      = gameObj.getPlayer().getWeapons().get(type);
                    double before = w.getStats().getOrDefault(stat, 0.0);
                    double after  = calculateProjectedValue(w, stat, rarity);

                    String text = String.format("[%s] %s %s: %s → %s",
                        rarity, formatName(type.name()), getDisplayName(stat),
                        formatStatValue(stat, before), formatStatValue(stat, after));

                    final WeaponTypes  fType = type;
                    final WeaponUpgrades fStat = stat;
                    final double       fAfter = after;
                    boxes[i] = new GameButton(
                        AppPanel.WIDTH / 2 - rectWidth / 2, yPos, rectWidth, rectHeight, text,
                        () -> { w.getStats().put(fStat, fAfter); w.onUpgrade(); finishUpgrade(); });
                    boxes[i].setImage(w.getIcon());
                }

            } else {
                // ── BOOK SLOT ─────────────────────────────────────────────────
                WeaponRarity rarity = rollRarity();
                boxRarities[i] = rarity;

                List<String> freshBooks = new ArrayList<>(availableBooks);
                freshBooks.removeAll(offeredNewBooks);

                boolean canAddBook = ownedBooks.size() < gameObj.getPlayer().getMAX_BOOKS()
                                     && !freshBooks.isEmpty();

                if (canAddBook && (rand.nextBoolean() || ownedBooks.isEmpty())) {
                    // ── NEW BOOK at level 1 ───────────────────────────────────
                    String bookName = freshBooks.get(rand.nextInt(freshBooks.size()));
                    offeredNewBooks.add(bookName);

                    Book book = new Book(bookName);
                    String label = buildBookLabel(book, rarity, true);

                    boxes[i] = new GameButton(
                        AppPanel.WIDTH / 2 - rectWidth / 2, yPos, rectWidth, rectHeight, label,
                        () -> { gameObj.getPlayer().addOrUpgradeBook(book, rarity); finishUpgrade(); });
                    boxes[i].setImage(getIconForBook(bookName));

                } else if (!ownedBooks.isEmpty()) {
                    // ── LEVEL UP EXISTING BOOK ────────────────────────────────
                    List<String> ownedBookList = new ArrayList<>(ownedBooks.keySet());
                    // Filter out max-level books and already-offered combos
                    ownedBookList.removeIf(n -> ownedBooks.get(n).isMaxLevel());
                    Collections.shuffle(ownedBookList, rand);

                    String bookName = null;
                    for (String candidate : ownedBookList) {
                        String key = candidate + ":" + rarity.name();
                        if (!offeredBookUpgrades.contains(key)) {
                            bookName = candidate; break;
                        }
                    }
                    if (bookName == null && !ownedBookList.isEmpty()) {
                        bookName = ownedBookList.get(rand.nextInt(ownedBookList.size()));
                    }
                    if (bookName == null) { // everything maxed or exhausted
                        if (boxes[i] != null) boxes[i].setTransparent(true);
                        continue;
                    }

                    offeredBookUpgrades.add(bookName + ":" + rarity.name());

                    Book existing = ownedBooks.get(bookName);
                    String label  = buildBookLabel(existing, rarity, false);
                    final String fBookName = bookName;
                    final WeaponRarity fRarity = rarity;

                    boxes[i] = new GameButton(
                        AppPanel.WIDTH / 2 - rectWidth / 2, yPos, rectWidth, rectHeight, label,
                        () -> {
                            gameObj.getPlayer().addOrUpgradeBook(new Book(fBookName), fRarity);
                            finishUpgrade();
                        });
                    boxes[i].setImage(getIconForBook(bookName));
                }
            }

            if (boxes[i] != null) boxes[i].setTransparent(true);
        }
    }

    // ─── Helper: build the button label for a book offer ─────────────────────
    private String buildBookLabel(Book book, WeaponRarity rarity, boolean isNew) {
        int levels   = Book.levelsForRarity(rarity);
        int curLevel = book.getLevel();
        int newLevel = Math.min(curLevel + levels, Book.MAX_LEVEL);
        String prefix = isNew
            ? String.format("[%s] NEW TOME: ", rarity)
            : String.format("[%s] TOME LV UP (+%d): ", rarity, levels);
        // Show current stat → after stat
        String after = book.previewDescription(rarity);
        return prefix + book.getName() + "  →  " + after;
    }

    // ─── Rarity roller (weighted) ─────────────────────────────────────────────
    private WeaponRarity rollRarity() {
        int r = rand.nextInt(100);
        if (r < 5)  return WeaponRarity.DIAMOND; //  5%
        if (r < 20) return WeaponRarity.GOLD;    // 15%
        if (r < 50) return WeaponRarity.SILVER;  // 30%
        return WeaponRarity.BRONZE;              // 50%
    }

    // ─── Icon lookup (mirrors Player.getIconForBook) ──────────────────────────
    private java.awt.image.BufferedImage getIconForBook(String name) {
        return switch (name) {
            case "Gold Book"                    -> main.Assets.GoldTomeIcon;
            case "Max HP Book"                  -> main.Assets.HpTomeIcon;
            case "Crit Rate Book"               -> main.Assets.CritTomeIcon;
            case "Projectile Speed Book"        -> main.Assets.ProjectileSpeedTomeIcon;
            case "Quantity Over Quality Book"   -> main.Assets.ProjectileCountTomeIcon;
            case "Size Book"                    -> main.Assets.SizeTomeIcon;
            case "EXP Book"                     -> main.Assets.XpTomeIcon;
            case "Cooldown Book"                -> main.Assets.CooldownTomeIcon;
            case "Damage Book"                  -> main.Assets.DamageTomeIcon;
            default -> null;
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Draw / Update (unchanged from original)
    // ─────────────────────────────────────────────────────────────────────────

    public void update() {
        timer++;
        if (flashAlpha > 0) flashAlpha -= 0.05f;
        for (int i = particles.size() - 1; i >= 0; i--) {
            particles.get(i).update();
            if (particles.get(i).life <= 0) particles.remove(i);
        }
        for (GameButton b : boxes) if (b != null) b.update();
    }

    public void draw(Graphics2D g) {
        g.setColor(new Color(255, 215, 0, 30));
        for (int i = 0; i < 8; i++) {
            double angle = (timer * 0.02) + (i * Math.PI / 4);
            int[] xPoints = { AppPanel.WIDTH / 2,
                AppPanel.WIDTH / 2 + (int)(Math.cos(angle - 0.1) * 1500),
                AppPanel.WIDTH / 2 + (int)(Math.cos(angle + 0.1) * 1500) };
            int[] yPoints = { AppPanel.HEIGHT / 2,
                AppPanel.HEIGHT / 2 + (int)(Math.sin(angle - 0.1) * 1500),
                AppPanel.HEIGHT / 2 + (int)(Math.sin(angle + 0.1) * 1500) };
            g.fillPolygon(xPoints, yPoints, 3);
        }

        for (int i = 0; i < boxes.length; i++) {
            GameButton b = boxes[i];
            if (b == null) continue;

            Color baseColor  = getRarityColor(boxRarities[i]);
            int   pixelSize  = 10;
            int   half       = b.getHeight() / 2;
            for (int py = 0; py < b.getHeight(); py += pixelSize) {
                float dist       = Math.abs(py - half) / (float) half;
                float darkFactor = 0.5f + (dist * 0.5f);
                if (b.isHovering()) darkFactor += 0.15f;
                int r  = (int)(baseColor.getRed()   * Math.min(1, darkFactor));
                int gr = (int)(baseColor.getGreen() * Math.min(1, darkFactor));
                int bl = (int)(baseColor.getBlue()  * Math.min(1, darkFactor));
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
        String msg  = "LEVEL UP";
        int    xTxt = AppPanel.WIDTH / 2 - g.getFontMetrics().stringWidth(msg) / 2;
        g.setColor(Color.BLACK);  g.drawString(msg, xTxt + 5, 85);
        g.setColor(Color.YELLOW); g.drawString(msg, xTxt,     80);

        if (flashAlpha > 0) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, flashAlpha));
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, AppPanel.WIDTH, AppPanel.HEIGHT);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers (unchanged)
    // ─────────────────────────────────────────────────────────────────────────

    private Color getRarityColor(WeaponRarity rarity) {
        if (rarity == null)                    return new Color(100, 100, 100);
        if (rarity == WeaponRarity.BRONZE)     return new Color(140,  70,  30);
        if (rarity == WeaponRarity.SILVER)     return new Color(160, 165, 175);
        if (rarity == WeaponRarity.GOLD)       return new Color(210, 170,   0);
        if (rarity == WeaponRarity.DIAMOND)    return new Color(  0, 180, 220);
        return new Color(80, 80, 80);
    }

    private String formatStatValue(WeaponUpgrades stat, double val) {
        if (stat == WeaponUpgrades.AttackSpeed || stat == WeaponUpgrades.Duration)
            return String.format("%.2fs", val / 60.0);
        if (isPercentStat(stat)) return String.format("%.0f%%", val * 100);
        return String.format("%.1f", val);
    }

    private String formatName(String name) {
        String[] words = name.split("_");
        StringBuilder sb = new StringBuilder();
        for (String w : words)
            sb.append(w.substring(0, 1).toUpperCase()).append(w.substring(1).toLowerCase()).append(" ");
        return sb.toString().trim();
    }

    private String getDisplayName(WeaponUpgrades stat) {
        return switch (stat) {
            case AttackSpeed     -> "Cooldown";
            case AttackDamage    -> "Damage";
            case AttackSize      -> "Area";
            case ProjectileCount -> "Amount";
            case ProjectileSpeed -> "Speed";
            case ProjectileBounce-> "Bounces";
            case CriticalChance  -> "Crit Rate";
            case CriticalDamage  -> "Crit Damage";
            default -> stat.toString();
        };
    }

    private boolean isPercentStat(WeaponUpgrades stat) {
        return stat == WeaponUpgrades.CriticalChance
            || stat == WeaponUpgrades.CriticalDamage
            || stat == WeaponUpgrades.AttackSize;
    }

    private double calculateProjectedValue(Weapon w, WeaponUpgrades upgrade, WeaponRarity rarity) {
        double cur  = w.getStats().getOrDefault(upgrade, 0.0);
        double mult = switch (rarity) {
            case SILVER  -> 1.2;
            case GOLD    -> 1.4;
            case DIAMOND -> 2.0;
            default      -> 1.0;
        };
        return switch (upgrade) {
            case ProjectileCount, ProjectileBounce -> cur + (1.0 * mult);
            case ProjectileSpeed                   -> cur + (2.0 * mult);
            case AttackDamage -> {
                double step = w.getBaseStats().getOrDefault(WeaponUpgrades.AttackDamage, 10.0) / 5.0;
                yield cur + (step * mult);
            }
            case AttackSpeed -> {
                double red = switch (rarity) {
                    case SILVER  -> 0.05; case GOLD -> 0.08; case DIAMOND -> 0.12; default -> 0.03;
                };
                yield cur * (1.0 - red);
            }
            case AttackSize      -> cur + (mult * 0.20);
            case CriticalChance  -> cur + (rarity.ordinal() * 0.02 + 0.03);
            case CriticalDamage  -> cur + (rarity.ordinal() * 0.15 + 0.10);
            case Duration        -> cur + (mult * 10);
            default              -> cur;
        };
    }

    private WeaponUpgrades getRandomStatForWeapon(Weapon w) {
        List<WeaponUpgrades> valid = new ArrayList<>();
        for (WeaponUpgrades u : WeaponUpgrades.values())
            if (w.getStats().containsKey(u) && w.getStats().get(u) > 0 && u != WeaponUpgrades.Range)
                valid.add(u);
        return valid.isEmpty() ? WeaponUpgrades.AttackDamage : valid.get(rand.nextInt(valid.size()));
    }

    private void startLevelUpEffect() {
        particles.clear();
        flashAlpha = 1.0f;
        for (int i = 0; i < 60; i++)
            particles.add(new PixelParticle(AppPanel.WIDTH / 2, AppPanel.HEIGHT / 2, rand));
    }

    private void finishUpgrade() {
        gameObj.setState(gameObj.getStateOpen());
    }

    private static class PixelParticle {
        float x, y, vx, vy;
        int size, life;
        Color color;
        PixelParticle(int sx, int sy, Random rand) {
            x = sx; y = sy;
            vx = (rand.nextFloat() - 0.5f) * 15f;
            vy = (rand.nextFloat() - 0.5f) * 15f;
            size = rand.nextInt(6) + 4;
            life = 30 + rand.nextInt(30);
            color = rand.nextBoolean() ? Color.YELLOW : Color.WHITE;
        }
        void update() { x += vx; y += vy; vy += 0.3f; life--; }
    }
}