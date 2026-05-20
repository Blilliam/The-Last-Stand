package Open.Weapons;

import Open.Entities.Enemies.Enemy;
import Open.Weapons.WeaponProjectile.BananaProjectile;
import Open.Weapons.WeaponProjectile.FireStaffProjectile;
import main.Assets;
import main.GameObject;
import main.Vec2;
import main.enums.WeaponTypes;
import main.enums.WeaponUpgrades;

public class FireStaffWeapon extends Weapon {
    public FireStaffWeapon(GameObject gameObj) {
        super(gameObj, WeaponTypes.FireStaff);
        this.setSprite(Assets.ProjectileFireBall);
        stats.put(WeaponUpgrades.AttackDamage, (double) 8);
		stats.put(WeaponUpgrades.ProjectileCount, (double) 1);
		stats.put(WeaponUpgrades.AttackSize, (double) 1);
		stats.put(WeaponUpgrades.AttackSpeed, (double) 300);
		stats.put(WeaponUpgrades.ProjectileSpeed, (double) 3);
		stats.put(WeaponUpgrades.CriticalDamage, (double) 1);
		stats.put(WeaponUpgrades.CriticalChance, (double) 0.1);
		
		baseStats = stats.clone();
		
		this.icon = Assets.FireStaffIcon;
    }

    protected void fireProjectile() {
        var target = gameObj.getPlayer().closestEnemy((double) 500);
        if (target != null) {
            Vec2 direction = Vec2.between(gameObj.getPlayer(), target);
            gameObj.addProjectiles(new FireStaffProjectile(gameObj, this, direction, 
                                   gameObj.getPlayer().getX(), gameObj.getPlayer().getY()));
        }
    }
}