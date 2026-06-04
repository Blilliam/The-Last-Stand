package Open.Weapons;

import Open.Weapons.WeaponProjectile.DexecutionerProjectile;
import main.Assets;
import main.GameObject;
import main.Vec2;
import main.enums.WeaponTypes;
import main.enums.WeaponUpgrades;

public class DexecutionerWeapon extends Weapon {

	public DexecutionerWeapon(GameObject gameObj) {
		super(gameObj, WeaponTypes.Dexecutioner);
		this.icon = Assets.DexecutionerIcon;
		// Dexecutioner: slow, high-damage directional spike. Pierces through enemies
		// in a line (diesAfterHit=false). The slow cooldown and tight hitbox cone
		// demand the player stay aimed at enemies — high risk, high reward.
		// Slightly reduced cooldown from 140 to give it a clearer advantage over Sword.
		stats.put(WeaponUpgrades.AttackDamage, 22.0); // up from 20 — rewarding the slow speed
		stats.put(WeaponUpgrades.AttackSpeed, 120.0); // down from 140 — less punishing
		stats.put(WeaponUpgrades.AttackSize, 1.0);
		stats.put(WeaponUpgrades.ProjectileCount, 1.0);
		stats.put(WeaponUpgrades.CriticalChance, 0.10);
		stats.put(WeaponUpgrades.CriticalDamage, 1.5); // up from 1.0 — big crits fit the theme

		setBaseStats();
		this.delayCounter = 0.0;
	}

	@Override
	protected void fireProjectile() {
		var target = gameObj.getPlayer().closestEnemy(500.0);
		if (target != null) {
			Vec2 direction = Vec2.between(gameObj.getPlayer(), target);
			gameObj.addProjectiles(new DexecutionerProjectile(gameObj, this, direction, gameObj.getPlayer().getX(),
					gameObj.getPlayer().getY()));
		}
	}

	@Override
	public void onUpgrade() {
		// Hook for subclass upgrade effects
	}
}