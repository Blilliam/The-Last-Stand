package Open.Weapons;

import Open.Weapons.WeaponProjectile.BouncingProjectile;
import main.Assets;
import main.GameObject;
import main.Vec2;
import main.enums.WeaponTypes;
import main.enums.WeaponUpgrades;

public class BoneWeapon extends Weapon {
    public BoneWeapon(GameObject gameObj) {
        super(gameObj, WeaponTypes.Bone);
        this.setSprite(Assets.bone);
        // Bone fires 2 projectiles each with 3 bounces — potentially hitting 8 targets total.
        // This high multi-target output justifies lower damage per hit and a slower cooldown.
        // Nerfed from 12 dmg / 100 spd to 8 dmg / 130 spd.
        stats.put(WeaponUpgrades.AttackDamage,    8.0); // down from 12 — 2 proj * 3 bounces is a lot
        stats.put(WeaponUpgrades.ProjectileCount,  2.0);
        stats.put(WeaponUpgrades.AttackSize,       1.0);
        stats.put(WeaponUpgrades.AttackSpeed,    130.0); // up from 100 — slow, hard-hitting feel
        stats.put(WeaponUpgrades.ProjectileSpeed,  4.0);
        stats.put(WeaponUpgrades.ProjectileBounce, 2.0); // down from 3 — 3 was too many
        stats.put(WeaponUpgrades.CriticalDamage,   1.0);
        stats.put(WeaponUpgrades.CriticalChance,   0.10);

        baseStats = stats.clone();
        this.icon = Assets.BoneIcon;
    }

    @Override
    protected void fireProjectile() {
        var target = gameObj.getPlayer().closestEnemy(500.0);
        if (target != null) {
            Vec2 direction = Vec2.between(gameObj.getPlayer(), target);
            gameObj.addProjectiles(new BouncingProjectile(gameObj, this, direction,
                                   gameObj.getPlayer().getX(), gameObj.getPlayer().getY()));
        }
    }
}