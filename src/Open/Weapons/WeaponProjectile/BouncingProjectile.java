package Open.Weapons.WeaponProjectile;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

import Open.Entities.Entity;
import Open.Entities.Enemies.Enemy;
import Open.Weapons.BoneWeapon;
import Open.Weapons.PewPewWeapon;
import Open.Weapons.Weapon;
import main.AppPanel;
import main.GameObject;
import main.Vec2;
import main.enums.WeaponUpgrades;

public class BouncingProjectile extends WeaponEntity {

	public BouncingProjectile(GameObject gameObj, Weapon weapon, Vec2 direction, int x, int y) {
		super(gameObj, weapon, direction, x, y);
		this.width = 25;
		this.height = 25;

		this.currProjectileBounces = weapon.getStats().get(WeaponUpgrades.ProjectileBounce);

		if (weapon.getClass().equals(PewPewWeapon.class))
			height /= 2;

		this.diesAfterHit = false;

		// Override velocity with book-adjusted speed
		this.velocity = direction.normalize().scale(weapon.getEffectiveProjectileSpeed());

		this.setHitBox(new Rectangle2D.Double(this.x, this.y, this.width, this.height));
	}

	@Override
	protected void updatePhysics() {
		position = position.add(velocity);
		duration++;
		if (duration > 300)
			isDead = true;
		((RectangularShape) this.getHitBox()).setFrame(x - width / 2, y - height / 2, this.width, this.height);
	}

	@Override
	protected void onHitEffect(Enemy hitEnemy) {
		if (currProjectileBounces > 0) {
			bounce(hitEnemy);
			currProjectileBounces--;
		}
	}

	private void bounce(Enemy currentTarget) {
		Enemy nextTarget = null;
		double closestDist = 1000;

		for (Enemy e : gameObj.getEnemies()) {
			if (e == currentTarget || e.isDying())
				continue;
			double dist = Entity.getDistance(this, e);
			if (dist < closestDist) {
				closestDist = dist;
				nextTarget = e;
			}
		}

		if (nextTarget != null) {
			// Keep book-adjusted speed on bounce
			velocity = Vec2.between(this, nextTarget).normalize().scale(weapon.getEffectiveProjectileSpeed());
		}
	}

	@Override
	public void draw(Graphics2D g) {
		drawImpact(g);

		int screenX = x - gameObj.getPlayer().getX() + AppPanel.WIDTH / 2;
		int screenY = y - gameObj.getPlayer().getY() + AppPanel.HEIGHT / 2;

		AffineTransform old = g.getTransform();
		g.translate(screenX, screenY);
		g.rotate(Math.atan2(velocity.getY(), velocity.getX()));

		if (weapon.getClass().equals(BoneWeapon.class)) {
			g.drawImage(sprite, -width / 2, -height / 2, (int) (width * 1.5), (int) (height * 1.5), null);
		} else {
			g.drawImage(sprite, -width / 2, -height / 2, width, height, null);
		}

		g.setTransform(old);
	}
}