package Open.Weapons.WeaponProjectile;

import java.awt.geom.Ellipse2D;
import java.awt.geom.RectangularShape;

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
        this.hitBox = new Ellipse2D.Double(this.position.getX(), this.position.getY(), this.width, this.height);
    }

    @Override
    protected void updatePhysics() {
    	position.setX(position.getX() + velocity.getX());
        position.setY(position.getY() + velocity.getY());
        ((RectangularShape) this.hitBox).setFrame(position.getX() - width/2, position.getY() - height/2, this.width, this.height);
        // Fireballs die after traveling a certain distance
        duration++;
        if (duration > 100) isDead = true;
    }
}