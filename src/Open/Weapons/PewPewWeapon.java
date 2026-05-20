package Open.Weapons;

import Open.Entities.Enemies.Enemy;
import Open.Weapons.WeaponProjectile.BananaProjectile;
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
        stats.put(WeaponUpgrades.AttackDamage, (double) 6.5);
		stats.put(WeaponUpgrades.ProjectileCount, (double) 1);
		stats.put(WeaponUpgrades.AttackSize, (double) 1);
		stats.put(WeaponUpgrades.AttackSpeed, (double) 60);
		stats.put(WeaponUpgrades.ProjectileSpeed, (double) 10);
		stats.put(WeaponUpgrades.ProjectileBounce, (double) 2);
		stats.put(WeaponUpgrades.CriticalDamage, (double) 1);
		stats.put(WeaponUpgrades.CriticalChance, (double) 0.1);
		
		baseStats = stats.clone();
		
		this.icon = Assets.RevolverIcon;
    }

    protected void fireProjectile() {
        var target = gameObj.getPlayer().closestEnemy((double) 700);
        if (target != null) {
            Vec2 direction = Vec2.between(gameObj.getPlayer(), target);
            gameObj.addProjectiles(new BouncingProjectile(gameObj, this, direction, 
                                   gameObj.getPlayer().getX(), gameObj.getPlayer().getY()));
        }
    }
}