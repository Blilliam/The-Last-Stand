package Open.Upgrades;

import main.enums.WeaponRarity;

public class Book {
	private String name;
	private WeaponRarity rarity;
	private double value;
	private String description;

	public Book(String name, WeaponRarity rarity) {
		this.name = name;
		this.rarity = rarity;
		this.value = assignValueBasedOnRarity(name, rarity);
		this.description = generateDescription(name, rarity, value);
	}

	private double assignValueBasedOnRarity(String name, WeaponRarity r) {
		int index = r.ordinal(); // 0: Bronze, 1: Silver, 2: Gold, 3: Diamond

		if (name.equals("EXP Book")) {
			double[] values = { 7, 8, 11, 14 };
			return values[index];
		}

		if (name.equals("Size Book")) {
			return r.sizeIncrease * 100;
		}

		if (name.equals("Quantity Over Quality Book")) {
			return r.flatBonus;
		}

		if (name.equals("Projectile Speed Book")) {
			double[] values = { 5, 6, 8, 10 };
			return values[index];
		}

		if (name.equals("Crit Rate Book")) {
			return r.critChanceAdd * 100;
		}

		if (name.equals("Luck Book")) {
			double[] values = { 5, 6, 8, 10 };
			return values[index];
		}

		if (name.equals("Max HP Book")) {
			double[] values = { 20, 25, 35, 50 };
			return values[index];
		}

		if (name.equals("Gold Book")) {
			double[] values = { 10, 12, 16, 20 };
			return values[index];
		}

		if (name.equals("Damage Book")) {
			return r.multiplier - 1.0;
		}

		if (name.equals("Cooldown Book")) {
			return r.speedReduction * 100;
		}

		return 0;
	}

	private String generateDescription(String name, WeaponRarity r, double val) {
		if (name.equals("Quantity Over Quality Book")) {
			return "+" + (int) val + " Projectile(s) [" + r.name() + "]";
		}

		if (name.equals("Damage Book")) {
			return "+" + (int) (val * 100) + "% Damage [" + r.name() + "]";
		}

		if (name.equals("Max HP Book")) {
			return "+" + (int) val + " HP [" + r.name() + "]";
		}

		// Default formatting for percentage-based books
		String cleanName = name.replace(" Book", "");
		return "+" + (int) val + "% " + cleanName + " [" + r.name() + "]";
	}

	public String getName() {
		return name;
	}

	public double getValue() {
		return value;
	}

	public WeaponRarity getRarity() {
		return rarity;
	}

	public String getDescription() {
		return description;
	}
}