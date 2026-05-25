package Open.Weapons;

import java.awt.image.BufferedImage;
import java.util.EnumMap;

import main.GameObject;
import main.DamageResult;
import main.enums.WeaponRarity;
import main.enums.WeaponTypes;
import main.enums.WeaponUpgrades;

public abstract class Weapon {
	private BufferedImage sprite;
	protected GameObject gameObj;
	protected BufferedImage icon;
	protected Double delayCounter;
	protected EnumMap<WeaponUpgrades, Double> stats;
	protected EnumMap<WeaponUpgrades, Double> baseStats;

	protected int projectilesToFire = 0;
	protected int subDelayCounter = 0;
	protected final int BURST_DELAY = 10;

	public Weapon(GameObject gameObj, WeaponTypes type) {
		this.gameObj = gameObj;
		this.stats = new EnumMap<>(WeaponUpgrades.class);
		this.baseStats = new EnumMap<>(WeaponUpgrades.class);
		this.delayCounter = 0.0;
	}

	// =========================================================================
	// DAMAGE
	// =========================================================================

	/**
	 * Calculates damage for a single hit, factoring in all artifact and book
	 * bonuses. Returns a DamageResult with the final number and a crit flag.
	 */
	public DamageResult getDmg() {
		// --- Damage ---
		double bookDamageBonus = gameObj.getPlayer().getStatBonus("Damage Book"); // e.g. 0.20 = +20 %
		
		// ChunkyRing: health-based damage bonus
		double healthScaling = gameObj.getPlayer().getArtifactManager().getHealthScalingBonus();

		double baseDmg = (stats.getOrDefault(WeaponUpgrades.AttackDamage, 0.0)
				+ gameObj.getPlayer().getArtifactManager().GetFlatDamage())
				* (1 + gameObj.getPlayer().getArtifactManager().GetPercentDamage() + bookDamageBonus + healthScaling);

		// --- Crit ---
		double bookCritBonus = gameObj.getPlayer().getStatBonus("Crit Rate Book") / 100.0;
		double critChance = stats.getOrDefault(WeaponUpgrades.CriticalChance, 0.0) + bookCritBonus;
		double critBonus = stats.getOrDefault(WeaponUpgrades.CriticalDamage, 0.0);

		double totalMult = 1.0;
		boolean isCrit = false;

		// Guaranteed crits (e.g. 200 % crit chance applies the multiplier twice)
		int guaranteedCrits = (int) critChance;
		if (guaranteedCrits > 0) {
			totalMult += (guaranteedCrits * critBonus);
			isCrit = true;
			
		}

		// Roll for the remaining partial chance
		double partialChance = critChance % 1;
		if (Math.random() < partialChance) {
			totalMult += critBonus;
			isCrit = true;
		}

		if (isCrit) {
			gameObj.getPlayer().getArtifactManager().onCritEffect();
		}
		
		// --- ThickCrit (BigFork) ---
		double thickCritChance = gameObj.getPlayer().getArtifactManager().getThickCritChance();
		if (Math.random() < thickCritChance) {
			totalMult *= 4.0; // ThickCrit deals 4x damage
			isCrit = true;
		}
		
		// --- Bonk (BigHammer) ---
		double bonkChance = gameObj.getPlayer().getArtifactManager().getBonkChance();
		if (Math.random() < bonkChance) {
			totalMult *= 20.0; // Bonk deals 20x damage
			isCrit = true;
		}
		
		return new DamageResult(baseDmg * totalMult, isCrit);
	}

	// =========================================================================
	// UPDATE – handles cooldown and burst firing
	// =========================================================================

	public void update() {
		// Cooldown Book reduces attack-speed counter (lower = faster)
		double bookCooldownReduction = gameObj.getPlayer().getStatBonus("Cooldown Book") / 100.0;
		double attackSpeed = stats.getOrDefault(WeaponUpgrades.AttackSpeed, 100.0)
				* (1 - gameObj.getPlayer().getArtifactManager().getPercentAttackSpeed()) * (1 - bookCooldownReduction);

		double range = stats.getOrDefault(WeaponUpgrades.Range, 500.0);

		if (delayCounter >= attackSpeed) {
			if (gameObj.getPlayer().closestEnemy(range) != null) {
				// Quantity Over Quality Book adds flat projectiles to each burst
				int bookProjectileBonus = (int) gameObj.getPlayer().getStatBonus("Quantity Over Quality Book");
				projectilesToFire = stats.getOrDefault(WeaponUpgrades.ProjectileCount, 1.0).intValue()
						+ gameObj.getPlayer().getArtifactManager().getBonusProjectiles() + bookProjectileBonus;
				delayCounter = 0.0;
			}
		}

		// Staggered burst firing
		if (projectilesToFire > 0) {
			subDelayCounter++;
			if (subDelayCounter >= BURST_DELAY) {
				fireProjectile();
				projectilesToFire--;
				subDelayCounter = 0;
			}
		}

		delayCounter++;
	}

	protected abstract void fireProjectile();

	// =========================================================================
	// EFFECTIVE STAT HELPERS
	// Used by projectile constructors so book bonuses apply at spawn time.
	// =========================================================================

	/**
	 * AttackSize stat multiplied by the Size Book percentage bonus. All projectile
	 * constructors should use this instead of reading WeaponUpgrades.AttackSize
	 * directly.
	 */
	public double getEffectiveSize() {
		double bookBonus = gameObj.getPlayer().getStatBonus("Size Book") / 100.0;
		return stats.getOrDefault(WeaponUpgrades.AttackSize, 1.0) * (1.0 + bookBonus);
	}

	/**
	 * ProjectileSpeed stat multiplied by the Projectile Speed Book percentage
	 * bonus. All projectile constructors that read ProjectileSpeed should use this
	 * instead.
	 */
	public double getEffectiveProjectileSpeed() {
		double bookBonus = gameObj.getPlayer().getStatBonus("Projectile Speed Book") / 100.0;
		return stats.getOrDefault(WeaponUpgrades.ProjectileSpeed, 5.0) * (1.0 + bookBonus);
	}

	// =========================================================================
	// STANDARD GETTERS / SETTERS
	// =========================================================================

	public EnumMap<WeaponUpgrades, Double> getStats() {
		return stats;
	}

	protected double getBaseValueFor(WeaponUpgrades upgrade) {
		return baseStats.getOrDefault(upgrade, 0.0);
	}

	protected void setBaseStats() {
		baseStats.putAll(stats);
	}

	public BufferedImage getIcon() {
		return icon;
	}

	public void onUpgrade() {
		// Optional hook for subclasses
	}

	public BufferedImage getSprite() {
		return sprite;
	}

	public void setSprite(BufferedImage sprite) {
		this.sprite = sprite;
	}

	public EnumMap<WeaponUpgrades, Double> getBaseStats() {
		return baseStats;
	}
}