package Open.Weapons;

import Open.Weapons.WeaponProjectile.DexecutionerProjectile;
import main.Assets;
import main.GameObject;
import main.Vec2;
import main.enums.WeaponTypes;
import main.enums.WeaponUpgrades;

public class DexecutionerWeapon extends Weapon {

    public DexecutionerWeapon(GameObject gameObj) {
        super(gameObj, WeaponTypes.Dexecutioner); // You can use a specific type if Axe doesn't fit
        this.icon = Assets.DexecutionerIcon;
        
        // Stats: High damage, slower attack speed, significant size
        stats.put(WeaponUpgrades.AttackDamage, 20.0); 
        stats.put(WeaponUpgrades.AttackSpeed, 140.0);
        stats.put(WeaponUpgrades.AttackSize, 1.0);
        stats.put(WeaponUpgrades.ProjectileCount, 1.0);
        stats.put(WeaponUpgrades.CriticalChance, 0.10);
        stats.put(WeaponUpgrades.CriticalDamage, 1.0);
        
        // Required for scaling logic
        setBaseStats();
        this.delayCounter = 0.0;
    }

    @Override
    protected void fireProjectile() {
        // Find the closest enemy within a reasonable range for a jab
        var target = gameObj.getPlayer().closestEnemy(500.0);
        
        if (target != null) {
            // Calculate the vector from player to target
            Vec2 direction = Vec2.between(gameObj.getPlayer(), target);
            
            // Add the PurpleStab entity to the game
            // It uses the player's current X and Y as the origin point
            gameObj.addProjectiles(new DexecutionerProjectile(
                gameObj, 
                this, 
                direction, 
                gameObj.getPlayer().getX(), 
                gameObj.getPlayer().getY()
            ));
        }
    }
    
    @Override
    public void onUpgrade() {
        // Any specific logic you want to trigger when this weapon levels up
    }
}