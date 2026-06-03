package Open.Weapons;

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
		// FireStaff fires a slow persistent projectile (duration=100 frames,
		// diesAfterHit=false)
		// so it can tick multiple enemies. Cooldown reduced to 90 frames (~1.5s).
		// Damage lowered slightly to compensate for the multi-hit nature.
		stats.put(WeaponUpgrades.AttackDamage, 6.0);
		stats.put(WeaponUpgrades.ProjectileCount, 1.0);
		stats.put(WeaponUpgrades.AttackSize, 1.0);
		stats.put(WeaponUpgrades.AttackSpeed, 90.0); // was 300 — way too slow
		stats.put(WeaponUpgrades.ProjectileSpeed, 4.0); // slightly faster so it reaches enemies
		stats.put(WeaponUpgrades.CriticalDamage, 1.0);
		stats.put(WeaponUpgrades.CriticalChance, 0.10);

		baseStats = stats.clone();
		this.icon = Assets.FireStaffIcon;
	}

	protected void fireProjectile() {
		var target = gameObj.getPlayer().closestEnemy(500.0);
		if (target != null) {
			Vec2 direction = Vec2.between(gameObj.getPlayer(), target);
			gameObj.addProjectiles(new FireStaffProjectile(gameObj, this, direction, gameObj.getPlayer().getX(),
					gameObj.getPlayer().getY()));
		}
	}
}