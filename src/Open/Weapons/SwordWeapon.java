package Open.Weapons;

import Open.Weapons.WeaponProjectile.SwordProjectile;
import main.Assets;
import main.GameObject;
import main.Vec2;
import main.enums.WeaponTypes;
import main.enums.WeaponUpgrades;

public class SwordWeapon extends Weapon {

    public SwordWeapon(GameObject gameObj) {
        super(gameObj, WeaponTypes.Sword);
        // Sword has a wide crescent AoE, so damage is moderate but it hits multiple enemies.
        // Cooldown: 80 frames (~1.3s). Compensates for the crescent being hard to aim
        // by giving it a generous crit rate — rewarding good positioning.
        stats.put(WeaponUpgrades.AttackDamage,   14.0); // up from 10 — crescent is harder to land
        stats.put(WeaponUpgrades.ProjectileCount, 1.0);
        stats.put(WeaponUpgrades.AttackSize,      1.0);
        stats.put(WeaponUpgrades.AttackSpeed,    80.0); // up from 70 — slight nerf to compensate dmg buff
        stats.put(WeaponUpgrades.CriticalDamage,  1.0);
        stats.put(WeaponUpgrades.CriticalChance,  0.15); // up from 0.10 — rewards landing the swing

        baseStats = stats.clone();
        this.icon = Assets.SwordIcon;
    }

    protected void fireProjectile() {
        var target = gameObj.getPlayer().closestEnemy(500.0);
        if (target != null) {
            Vec2 direction = Vec2.between(gameObj.getPlayer(), target);
            gameObj.addProjectiles(new SwordProjectile(gameObj, this, direction,
                                   gameObj.getPlayer().getX(), gameObj.getPlayer().getY()));
        }
    }
}