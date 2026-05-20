package Open.Weapons;

import java.awt.Graphics2D;

import Open.Weapons.WeaponProjectile.AuraProjectile;
import Open.Weapons.WeaponProjectile.BananaProjectile;
import main.Assets;
import main.GameObject;
import main.Vec2;
import main.enums.WeaponTypes;
import main.enums.WeaponUpgrades;

public class AuraWeapon extends Weapon{
	boolean created;
	private AuraProjectile currentAuraEntity;

	public AuraWeapon(GameObject gameObj) {
		super(gameObj, WeaponTypes.Aura);
		stats.put(WeaponUpgrades.AttackDamage, (double) 2.0);
		stats.put(WeaponUpgrades.AttackSize, (double) 1);
		stats.put(WeaponUpgrades.CriticalDamage, (double) 1);
		stats.put(WeaponUpgrades.CriticalChance, (double) 0.1);
		
		baseStats = stats.clone();
		this.icon = Assets.AuraIcon;
		created = false;
	}
	
	public void update() {
        if (currentAuraEntity == null || currentAuraEntity.isDead()) {
            currentAuraEntity = new AuraProjectile(gameObj, this);
            gameObj.addProjectiles(currentAuraEntity);
        }
    }

	@Override
	protected void fireProjectile() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onUpgrade() {
		if (currentAuraEntity != null) {
            currentAuraEntity.setDead(true); 
        }
    }

}