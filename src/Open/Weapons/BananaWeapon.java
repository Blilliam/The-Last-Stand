package Open.Weapons;

import Open.Weapons.WeaponProjectile.BananaProjectile;
import main.Assets;
import main.GameObject;
import main.Vec2;
import main.enums.WeaponTypes;
import main.enums.WeaponUpgrades;

public class BananaWeapon extends Weapon {

	public BananaWeapon(GameObject gameObj) {
		super(gameObj, WeaponTypes.Banana);
		setSprite(Assets.ProjectileBanana);
		this.icon = Assets.BananaIcon;
		// Banana orbits out then boomerangs back, with diesAfterHit=false (multi-hit).
		// It can hit the same enemy on the way out AND back — roughly 2x hits per
		// throw.
		// Buffed damage slightly; kept cooldown moderate. The skill is in positioning
		// so the banana passes through dense groups.
		stats.put(WeaponUpgrades.AttackDamage, 8.0);
		stats.put(WeaponUpgrades.ProjectileCount, 1.0);
		stats.put(WeaponUpgrades.AttackSize, 1.0);
		stats.put(WeaponUpgrades.AttackSpeed, 110.0); // down from 120 — slightly faster
		stats.put(WeaponUpgrades.CriticalDamage, 1.0);
		stats.put(WeaponUpgrades.CriticalChance, 0.10);

		baseStats = stats.clone();
		delayCounter = 0.0;
	}

	protected void fireProjectile() {
		var target = gameObj.getPlayer().closestEnemy(500.0);
		if (target != null) {
			Vec2 direction = Vec2.between(gameObj.getPlayer(), target);
			gameObj.addProjectiles(new BananaProjectile(gameObj, this, direction, gameObj.getPlayer().getX(),
					gameObj.getPlayer().getY()));
		}
	}
}