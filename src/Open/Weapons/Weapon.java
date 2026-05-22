package Open.Weapons;

import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;

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
    protected final int BURST_DELAY = 10; // Frames between each projectile in a burst

    public Weapon(GameObject gameObj, WeaponTypes type) {
        this.gameObj = gameObj;
        this.stats = new EnumMap<>(WeaponUpgrades.class);
        this.baseStats = new EnumMap<>(WeaponUpgrades.class);
        this.delayCounter = 0.0;

        // Core Defaults: Only initialize what every weapon uses.
        // This prevents the UI from showing empty/zero stats for things like "Bounces" on a Sword.
    }

    /**
     * Calculates damage and determines if the hit is a Critical Strike.
     * Returns a DamageResult containing the final number and a red/white flag.
     */
    public DamageResult getDmg() {
        double baseDmg = (stats.getOrDefault(WeaponUpgrades.AttackDamage, 0.0) + gameObj.getPlayer().getArtifactManager().GetFlatDamage()) * (1 + gameObj.getPlayer().getArtifactManager().GetPercentDamage());
        double critChance = stats.getOrDefault(WeaponUpgrades.CriticalChance, 0.0);
        double critBonus = stats.getOrDefault(WeaponUpgrades.CriticalDamage, 0.0);

        double totalMult = 1.0;
        boolean isCrit = false;

        // Handle guaranteed crits (e.g., 200% crit chance adds multiplier twice)
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

        double finalDamage = baseDmg * totalMult;
        return new DamageResult(finalDamage, isCrit);
    }

    public void update() {
        // 1. Check if the main weapon cooldown is ready
        double attackSpeed = stats.getOrDefault(WeaponUpgrades.AttackSpeed, 100.0) * (1-gameObj.getPlayer().getArtifactManager().getPercentAttackSpeed());
        double range = stats.getOrDefault(WeaponUpgrades.Range, 500.0);

        if (delayCounter >= attackSpeed) {
            if (gameObj.getPlayer().closestEnemy(range) != null) {
                // "Load" the burst based on ProjectileCount stat
                projectilesToFire = stats.getOrDefault(WeaponUpgrades.ProjectileCount, 1.0).intValue() + gameObj.getPlayer().getArtifactManager().getBonusProjectiles();
                delayCounter = 0.0;
            }
        }

        // 2. Handle the Burst Logic (staggered firing)
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

    public EnumMap<WeaponUpgrades, Double> getStats() {
        return stats;
    }

    protected double getBaseValueFor(WeaponUpgrades upgrade) {
        return baseStats.getOrDefault(upgrade, 0.0);
    }

    protected void setBaseStats() {
        baseStats.putAll(stats);
    }

    /**
     * Applies upgrades and updates the current stats map.
     */

    private double getRarityMultiplier(WeaponRarity rarity) {
        return switch (rarity) {
            case SILVER -> 1.2;
            case GOLD -> 1.4;
            case DIAMOND -> 2.0;
            default -> 1.0; // BRONZE
        };
    }
    
    public BufferedImage getIcon() {
        return icon;
    }
    
    public void onUpgrade() {
        // Optional hook for subclasses
    }

    public BufferedImage getSprite() { return sprite; }
    public void setSprite(BufferedImage sprite) { this.sprite = sprite; }

    public EnumMap<WeaponUpgrades, Double> getBaseStats() {
        return baseStats;
    }
}