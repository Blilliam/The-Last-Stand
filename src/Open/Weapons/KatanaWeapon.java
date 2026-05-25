package Open.Weapons;

import Open.Weapons.WeaponProjectile.KatanaProjectile;
import main.Assets;
import main.GameObject;
import main.Vec2;
import main.enums.WeaponTypes;
import main.enums.WeaponUpgrades;

public class KatanaWeapon extends Weapon {
	public KatanaWeapon(GameObject gameObj) {
		super(gameObj, WeaponTypes.Sword);
		this.icon = Assets.KatanaIcon;
		// Katana teleports a strike directly onto the target — zero positioning skill
		// required.
		// Compensated by: lower base damage than Sword, longer cooldown, very high crit
		// rate
		// so the identity is "bursty crit assassin" rather than just a free-hit
		// machine.
		stats.put(WeaponUpgrades.AttackDamage, 10.0); // down from 12 — teleport is too reliable
		stats.put(WeaponUpgrades.AttackSpeed, 70.0); // up from 50 — slower to balance reliability
		stats.put(WeaponUpgrades.AttackSize, 1.0);
		stats.put(WeaponUpgrades.ProjectileCount, 1.0);
		stats.put(WeaponUpgrades.CriticalChance, 0.25); // up from 0.20 — crit is the identity
		stats.put(WeaponUpgrades.CriticalDamage, 1.5); // up from 1.0 — crits should feel impactful

		setBaseStats();
	}

	@Override
	protected void fireProjectile() {
		var target = gameObj.getPlayer().closestEnemy(400.0);
		if (target != null) {
			gameObj.addProjectiles(
					new KatanaProjectile(gameObj, this, new Vec2(0, 0), (int) target.getX(), (int) target.getY()));
		}
	}
}