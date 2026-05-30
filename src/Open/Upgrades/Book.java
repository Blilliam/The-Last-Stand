package Open.Upgrades;

import main.enums.WeaponRarity;

public class Book {
	private String name;
	private double value; // CUMULATIVE total — grows with every upgrade
	private String description;

	// ── Constructor 1: brand-new book at a given rarity ──────────────────────
	// Used when the player picks up a book for the first time.
	public Book(String name, WeaponRarity rarity) {
		this.name = name;
		this.value = incrementForRarity(name, rarity);
		this.description = generateDescription(name, this.value);
	}

	// ── Constructor 2: upgraded book with a pre-computed cumulative value ─────
	// Used by Player.addOrUpgradeBook to store the running total.
	public Book(String name, double cumulativeValue) {
		this.name = name;
		this.value = cumulativeValue;
		this.description = generateDescription(name, cumulativeValue);
	}

	// =========================================================================
	// INCREMENT PER RARITY
	// Returns only the AMOUNT TO ADD for this upgrade tier.
	// The caller is responsible for adding this to the existing total.
	// =========================================================================
	public static double incrementForRarity(String name, WeaponRarity r) {
		int index = r.ordinal(); // 0=BRONZE, 1=SILVER, 2=GOLD, 3=DIAMOND

		switch (name) {
		case "EXP Book": {
			double[] values = { 7, 8, 11, 14 };
			return values[index];
		}
		case "Size Book": {
			return r.sizeIncrease * 200;
		}
		case "Quantity Book": {
		    double[] values = { 1.0, 1.2, 1.4, 2.0 };
		    return values[index];
		}
		case "Projectile Speed Book": {
			double[] values = { 5, 6, 8, 10 };
			return values[index];
		}
		case "Crit Rate Book": {
			return r.critChanceAdd * 100;
		}
		case "Luck Book": {
			double[] values = { 5, 6, 8, 10 };
			return values[index];
		}
		case "Max HP Book": {
			double[] values = { 20, 25, 35, 50 };
			return values[index];
		}
		case "Gold Book": {
			double[] values = { 10, 12, 16, 20 };
			return values[index];
		}
		case "Damage Book": {
			double[] values = { 0.10, 0.20, 0.30, 0.5 };
			return values[index];
		}
		case "Cooldown Book": {
			return r.speedReduction * 100;
		}
		default:
			return 0;
		}
	}

	// =========================================================================
	// DESCRIPTION
	// Always reflects the current cumulative value, not a single rarity tier.
	// =========================================================================
	private String generateDescription(String name, double val) {
		switch (name) {
		case "Quantity Book":
		    return "\n+" + val + " Projectile(s)";
		case "Damage Book":
			return "+" + (int) (val * 100) + "% Damage";
		case "Max HP Book":
			return "+" + (int) val + " HP";
		default:
			String cleanName = name.replace(" Book", "");
			return "+" + (int) val + "% " + cleanName;
		}
	}

	// =========================================================================
	// GETTERS
	// =========================================================================
	public String getName() {
		return name;
	}

	public double getValue() {
		return value;
	}

	public String getDescription() {
		return description;
	}
}