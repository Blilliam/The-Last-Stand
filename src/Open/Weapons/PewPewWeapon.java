package Open.Weapons;

import Open.Weapons.WeaponProjectile.BouncingProjectile;
import main.Assets;
import main.GameObject;
import main.Vec2;
import main.enums.WeaponTypes;
import main.enums.WeaponUpgrades;

public class PewPewWeapon extends Weapon {
    public PewPewWeapon(GameObject gameObj) {
        super(gameObj, WeaponTypes.PewPew);
        this.setSprite(Assets.ProjectileBullet);
        // PewPew bounces between enemies (2 bounces = hits up to 3 targets).
        // Damage is lower per hit to offset the multi-target potential.
        // Fast cooldown makes it a sustained spray weapon.
        stats.put(WeaponUpgrades.AttackDamage,    5.0); // down from 6.5 — bounces compensate
        stats.put(WeaponUpgrades.ProjectileCount,  1.0);
        stats.put(WeaponUpgrades.AttackSize,       1.0);
        stats.put(WeaponUpgrades.AttackSpeed,     55.0); // up from 60 frames — slightly faster
        stats.put(WeaponUpgrades.ProjectileSpeed, 12.0); // faster bullet = more responsive
        stats.put(WeaponUpgrades.ProjectileBounce, 2.0);
        stats.put(WeaponUpgrades.CriticalDamage,   1.0);
        stats.put(WeaponUpgrades.CriticalChance,   0.10);

        baseStats = stats.clone();
        this.icon = Assets.RevolverIcon;
    }

    protected void fireProjectile() {
        var target = gameObj.getPlayer().closestEnemy(700.0);
        if (target != null) {
            Vec2 direction = Vec2.between(gameObj.getPlayer(), target);
            gameObj.addProjectiles(new BouncingProjectile(gameObj, this, direction,
                                   gameObj.getPlayer().getX(), gameObj.getPlayer().getY()));
        }
    }
}