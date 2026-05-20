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
        
        stats.put(WeaponUpgrades.AttackDamage, 12.0);
        stats.put(WeaponUpgrades.AttackSpeed, 50.0);
        stats.put(WeaponUpgrades.AttackSize, 1.0);
        stats.put(WeaponUpgrades.ProjectileCount, 1.0);
        stats.put(WeaponUpgrades.CriticalChance, 0.20);
        stats.put(WeaponUpgrades.CriticalDamage, 1.0);
        
        setBaseStats();
    }

    @Override
    protected void fireProjectile() {
        var target = gameObj.getPlayer().closestEnemy(400.0);
        if (target != null) {
            // Spawn directly at the enemy's coordinates
            gameObj.addProjectiles(new KatanaProjectile(gameObj, this, new Vec2(0, 0), 
                                 (int)target.getX(), (int)target.getY()));
        }
    }
}