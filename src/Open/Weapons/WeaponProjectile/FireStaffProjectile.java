package Open.Weapons.WeaponProjectile;

import java.awt.geom.Ellipse2D;
import java.awt.geom.RectangularShape;

import Open.Weapons.Weapon;
import main.GameObject;
import main.Vec2;

public class FireStaffProjectile extends WeaponEntity {

    public FireStaffProjectile(GameObject gameObj, Weapon weapon, Vec2 direction, int x, int y) {
        super(gameObj, weapon, direction, x, y);

        // Use getEffectiveSize() so the Size Book bonus applies
        this.width  = (int)(40 * weapon.getEffectiveSize());
        this.height = (int)(40 * weapon.getEffectiveSize());

        // Velocity is already built from getEffectiveProjectileSpeed() via the
        // super constructor path — but FireStaff reads ProjectileSpeed from stats,
        // so we override velocity here to apply the book bonus correctly.
        this.velocity = direction.normalize().scale(weapon.getEffectiveProjectileSpeed());

        this.diesAfterHit = false;
        this.setHitBox(new Ellipse2D.Double(this.position.getX(), this.position.getY(), this.width, this.height));
    }

    @Override
    protected void updatePhysics() {
        position.setX(position.getX() + velocity.getX());
        position.setY(position.getY() + velocity.getY());
        ((RectangularShape) this.getHitBox()).setFrame(
                position.getX() - width / 2, position.getY() - height / 2, this.width, this.height);
        duration++;
        if (duration > 100) isDead = true;
    }
}