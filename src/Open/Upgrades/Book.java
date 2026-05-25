package Open.Upgrades;

import main.enums.WeaponRarity;

public class Book {
	private String name;
	private int level;
	private String description;

	public static final int MAX_LEVEL = 99;

	// Flat / percent gain per level
	private static final double EXP_PER_LEVEL = 7.0;
	private static final double SIZE_PER_LEVEL = 10.0;
	private static final double QUANTITY_PER_LEVEL = 1.0;
	private static final double PROJ_SPEED_PER_LEVEL = 5.0;
	private static final double CRIT_RATE_PER_LEVEL = 5.0;
	private static final double MAX_HP_PER_LEVEL = 20.0;
	private static final double GOLD_PER_LEVEL = 10.0;
	private static final double DAMAGE_PER_LEVEL = 10.0;
	private static final double COOLDOWN_PER_LEVEL = 5.0;

	public Book(String name) {
		this.name = name;
		this.level = 1;
		this.description = buildDescription();
	}

	// ── Rarity → levels gained ────────────────────────────────────────────────
	/** How many levels a given rarity grants when the player picks this upgrade. */
	public static int levelsForRarity(WeaponRarity rarity) {
		return switch (rarity) {
		case BRONZE -> 1;
		case SILVER -> 2;
		case GOLD -> 3;
		case DIAMOND -> 5;
		};
	}

	/**
	 * Called when the player picks a rarity-levelled upgrade for this tome. Grants
	 * levelsForRarity(rarity) levels, clamped to MAX_LEVEL. Returns the actual
	 * number of levels gained (useful for HP delta calc).
	 */
	public int levelUp(WeaponRarity rarity) {
		int gain = Math.min(levelsForRarity(rarity), MAX_LEVEL - level);
		level += gain;
		description = buildDescription();
		return gain;
	}

	// ── Stat accessors (total value at current level) ─────────────────────────

	public double getExpBonus() {
		return name.equals("EXP Book") ? EXP_PER_LEVEL * level / 100.0 : 0;
	}

	public double getSizeBonus() {
		return name.equals("Size Book") ? SIZE_PER_LEVEL * level / 100.0 : 0;
	}

	public int getQuantityBonus() {
		return name.equals("Quantity Over Quality Book") ? (int) (QUANTITY_PER_LEVEL * level) : 0;
	}

	public double getProjSpeedBonus() {
		return name.equals("Projectile Speed Book") ? PROJ_SPEED_PER_LEVEL * level / 100.0 : 0;
	}

	public double getCritRateBonus() {
		return name.equals("Crit Rate Book") ? CRIT_RATE_PER_LEVEL * level / 100.0 : 0;
	}

	public int getMaxHpBonus() {
		return name.equals("Max HP Book") ? (int) (MAX_HP_PER_LEVEL * level) : 0;
	}

	public double getGoldBonus() {
		return name.equals("Gold Book") ? GOLD_PER_LEVEL * level / 100.0 : 0;
	}

	public double getDamageBonus() {
		return name.equals("Damage Book") ? DAMAGE_PER_LEVEL * level / 100.0 : 0;
	}

	public double getCooldownBonus() {
		return name.equals("Cooldown Book") ? COOLDOWN_PER_LEVEL * level / 100.0 : 0;
	}

	// ── Description ───────────────────────────────────────────────────────────
	private String buildDescription() {
		return switch (name) {
		case "EXP Book" -> String.format("+%.0f%% EXP Gain  [Lv %d]", EXP_PER_LEVEL * level, level);
		case "Size Book" -> String.format("+%.0f%% Attack Size  [Lv %d]", SIZE_PER_LEVEL * level, level);
		case "Quantity Over Quality Book" ->
			String.format("+%d Projectile(s)  [Lv %d]", (int) QUANTITY_PER_LEVEL * level, level);
		case "Projectile Speed Book" ->
			String.format("+%.0f%% Projectile Speed  [Lv %d]", PROJ_SPEED_PER_LEVEL * level, level);
		case "Crit Rate Book" -> String.format("+%.0f%% Crit Chance  [Lv %d]", CRIT_RATE_PER_LEVEL * level, level);
		case "Max HP Book" -> String.format("+%d Max HP  [Lv %d]", (int) (MAX_HP_PER_LEVEL * level), level);
		case "Gold Book" -> String.format("+%.0f%% Gold Gain  [Lv %d]", GOLD_PER_LEVEL * level, level);
		case "Damage Book" -> String.format("+%.0f%% Damage  [Lv %d]", DAMAGE_PER_LEVEL * level, level);
		case "Cooldown Book" -> String.format("-%.0f%% Cooldown  [Lv %d]", COOLDOWN_PER_LEVEL * level, level);
		default -> name + "  [Lv " + level + "]";
		};
	}

	// ── Preview: what the description looks like AFTER a rarity upgrade ───────
	public String previewDescription(WeaponRarity rarity) {
		int previewLevel = Math.min(level + levelsForRarity(rarity), MAX_LEVEL);
		return switch (name) {
		case "EXP Book" -> String.format("+%.0f%% EXP Gain  [Lv %d]", EXP_PER_LEVEL * previewLevel, previewLevel);
		case "Size Book" -> String.format("+%.0f%% Attack Size  [Lv %d]", SIZE_PER_LEVEL * previewLevel, previewLevel);
		case "Quantity Over Quality Book" ->
			String.format("+%d Projectile(s)  [Lv %d]", (int) QUANTITY_PER_LEVEL * previewLevel, previewLevel);
		case "Projectile Speed Book" ->
			String.format("+%.0f%% Projectile Speed  [Lv %d]", PROJ_SPEED_PER_LEVEL * previewLevel, previewLevel);
		case "Crit Rate Book" ->
			String.format("+%.0f%% Crit Chance  [Lv %d]", CRIT_RATE_PER_LEVEL * previewLevel, previewLevel);
		case "Max HP Book" ->
			String.format("+%d Max HP  [Lv %d]", (int) (MAX_HP_PER_LEVEL * previewLevel), previewLevel);
		case "Gold Book" -> String.format("+%.0f%% Gold Gain  [Lv %d]", GOLD_PER_LEVEL * previewLevel, previewLevel);
		case "Damage Book" -> String.format("+%.0f%% Damage  [Lv %d]", DAMAGE_PER_LEVEL * previewLevel, previewLevel);
		case "Cooldown Book" ->
			String.format("-%.0f%% Cooldown  [Lv %d]", COOLDOWN_PER_LEVEL * previewLevel, previewLevel);
		default -> name + "  [Lv " + previewLevel + "]";
		};
	}

	// ── Getters ───────────────────────────────────────────────────────────────
	public String getName() {
		return name;
	}

	public int getLevel() {
		return level;
	}

	public String getDescription() {
		return description;
	}

	public boolean isMaxLevel() {
		return level >= MAX_LEVEL;
	}

	/** Primary numeric value for tooltip/display (raw, not as multiplier). */
	public double getValue() {
		return switch (name) {
		case "EXP Book" -> EXP_PER_LEVEL * level;
		case "Size Book" -> SIZE_PER_LEVEL * level;
		case "Quantity Over Quality Book" -> QUANTITY_PER_LEVEL * level;
		case "Projectile Speed Book" -> PROJ_SPEED_PER_LEVEL * level;
		case "Crit Rate Book" -> CRIT_RATE_PER_LEVEL * level;
		case "Max HP Book" -> MAX_HP_PER_LEVEL * level;
		case "Gold Book" -> GOLD_PER_LEVEL * level;
		case "Damage Book" -> DAMAGE_PER_LEVEL * level;
		case "Cooldown Book" -> COOLDOWN_PER_LEVEL * level;
		default -> 0;
		};
	}
}