package Open.Weapons;

import java.awt.Graphics2D;

import Open.Weapons.WeaponProjectile.AuraProjectile;
import main.Assets;
import main.GameObject;
import main.enums.WeaponTypes;
import main.enums.WeaponUpgrades;

public class AuraWeapon extends Weapon {
    boolean created;
    private AuraProjectile currentAuraEntity;

    public AuraWeapon(GameObject gameObj) {
        super(gameObj, WeaponTypes.Aura);
        // Aura hits all nearby enemies on a per-enemy cooldown (HIT_COOLDOWN=15 frames)
        // At 1.5 dmg per hit per enemy, it's a sustained AoE option, not a burst weapon.
        // Size kept at 1 so upgrades matter more.
        stats.put(WeaponUpgrades.AttackDamage, 1.5);
        stats.put(WeaponUpgrades.AttackSize,   1.0);
        stats.put(WeaponUpgrades.CriticalDamage, 1.0);
        stats.put(WeaponUpgrades.CriticalChance, 0.05); // Lower crit — it hits everything

        baseStats = stats.clone();
        this.icon = Assets.AuraIcon;
        created = false;
    }

    public void update() {
        if (currentAuraEntity == null || currentAuraEntity.isDead()) {
            currentAuraEntity = new AuraProjectile(gameObj, this);
            gameObj.addProjectiles(currentAuraEntity);
        }
    }

    @Override
    protected void fireProjectile() {
        // Aura manages its own projectile lifecycle above
    }

    @Override
    public void onUpgrade() {
        if (currentAuraEntity != null) {
            currentAuraEntity.setDead(true);
        }
    }
}