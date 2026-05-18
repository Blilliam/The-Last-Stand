package Open.Weapons.WeaponProjectile;

import Open.Weapons.Weapon;
import main.GameObject;
import main.Vec2;
import main.enums.WeaponUpgrades;

public class FireStaffProjectile extends WeaponEntity {
    public FireStaffProjectile(GameObject gameObj, Weapon weapon, Vec2 direction, int x, int y) {
        super(gameObj, weapon, direction, x, y);
        this.width = (int)(40 * weapon.getStats().get(WeaponUpgrades.AttackSize));
        this.height = (int)(40 * weapon.getStats().get(WeaponUpgrades.AttackSize));
        this.diesAfterHit = false;
    }

    @Override
    protected void updatePhysics() {
    	position.setX(position.getX() + velocity.getX());
        position.setY(position.getY() + velocity.getY());
        // Fireballs die after traveling a certain distance
        duration++;
        if (duration > 100) isDead = true;
    }
}