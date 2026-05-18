package Open.Weapons;

import Open.Weapons.WeaponProjectile.BananaProjectile;
import main.Assets;
import main.GameObject;
import main.Vec2;
import main.enums.WeaponTypes;
import main.enums.WeaponUpgrades;

public class BananaWeapon extends Weapon{

	public BananaWeapon(GameObject gameObj) {
		super(gameObj, WeaponTypes.Banana);
		setSprite(Assets.ProjectileBanana);
		this.icon = Assets.BananaIcon;
		stats.put(WeaponUpgrades.AttackDamage, (double) 8); 
		stats.put(WeaponUpgrades.ProjectileCount, (double) 1);
		stats.put(WeaponUpgrades.AttackSize, (double) 1);
		stats.put(WeaponUpgrades.AttackSpeed, (double) 120);
		stats.put(WeaponUpgrades.ProjectileSpeed, (double) 10);
		stats.put(WeaponUpgrades.Duration, (double) 100);
		stats.put(WeaponUpgrades.CriticalDamage, (double) 1);
		stats.put(WeaponUpgrades.CriticalChance, (double) 0.1);
		baseStats = stats.clone();
		delayCounter = 0.0;
	}

    protected void fireProjectile() {
        var target = gameObj.getPlayer().closestEnemy((double) 500);
        if (target != null) {
            Vec2 direction = Vec2.between(gameObj.getPlayer(), target);
            gameObj.addProjectiles(new BananaProjectile(gameObj, this, direction, 
                                   gameObj.getPlayer().getX(), gameObj.getPlayer().getY()));
        }
    }
	
}