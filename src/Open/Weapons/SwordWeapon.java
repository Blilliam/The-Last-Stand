package Open.Weapons;

import java.util.EnumMap;

import Open.Entities.Enemies.Enemy;
import Open.Weapons.WeaponProjectile.BananaProjectile;
import Open.Weapons.WeaponProjectile.SwordProjectile;
import main.Assets;
import main.GameObject;
import main.Vec2;
import main.enums.WeaponTypes;
import main.enums.WeaponUpgrades;

public class SwordWeapon extends Weapon {

    public SwordWeapon(GameObject gameObj) {
        super(gameObj, WeaponTypes.Sword);
        //this.sprite = Assets.katanaSlashSprite; // A crescent moon slash sprite
        stats.put(WeaponUpgrades.AttackDamage, (double) 10);
		stats.put(WeaponUpgrades.ProjectileCount, (double) 1);
		stats.put(WeaponUpgrades.AttackSize, (double) 1);
		stats.put(WeaponUpgrades.AttackSpeed, (double) 70);
		stats.put(WeaponUpgrades.CriticalDamage, (double) 1);
		stats.put(WeaponUpgrades.CriticalChance, (double) 0.1);
		
		baseStats = stats.clone();
		
		this.icon = Assets.SwordIcon;
    }

    protected void fireProjectile() {
        var target = gameObj.getPlayer().closestEnemy((double) 500);
        if (target != null) {
            Vec2 direction = Vec2.between(gameObj.getPlayer(), target);
            gameObj.addProjectiles(new SwordProjectile(gameObj, this, direction, 
                                   gameObj.getPlayer().getX(), gameObj.getPlayer().getY()));
        }
    }
}