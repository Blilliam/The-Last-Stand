package Open.Weapons;

import java.awt.Taskbar.State;

import Open.Entities.Enemies.Enemy;
import Open.Weapons.WeaponProjectile.BananaProjectile;
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
		stats.put(WeaponUpgrades.AttackDamage, (double) 12);
		stats.put(WeaponUpgrades.ProjectileCount, (double) 2);
		stats.put(WeaponUpgrades.AttackSize, (double) 1);
		stats.put(WeaponUpgrades.AttackSpeed, (double) 100);
		stats.put(WeaponUpgrades.ProjectileSpeed, (double) 4);
		stats.put(WeaponUpgrades.ProjectileBounce, (double) 3);
		stats.put(WeaponUpgrades.CriticalDamage, (double) 1);
		stats.put(WeaponUpgrades.CriticalChance, (double) 0.1);

		baseStats = stats.clone();
		
		this.icon = Assets.BoneIcon;
	}

	@Override

	protected void fireProjectile() {
		var target = gameObj.getPlayer().closestEnemy((double) 500);
		if (target != null) {
			Vec2 direction = Vec2.between(gameObj.getPlayer(), target);
			gameObj.addProjectiles(new BouncingProjectile(gameObj, this, direction, gameObj.getPlayer().getX(),
					gameObj.getPlayer().getY()));
		}
	}
}