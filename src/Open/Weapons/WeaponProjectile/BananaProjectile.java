package Open.Weapons.WeaponProjectile;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import Open.Entities.Entity;
import Open.Weapons.Weapon;
import main.AppPanel;
import main.GameObject;
import main.Vec2;

public class BananaProjectile extends WeaponEntity {
	private double speed = 6.0;

	public BananaProjectile(GameObject gameObj, Weapon weapon, Vec2 direction, int x, int y) {
		super(gameObj, weapon, direction, x, y);
		// Use getEffectiveSize() so the Size Book bonus applies
		this.width = (int) (50 * weapon.getEffectiveSize());
		this.height = (int) (50 * weapon.getEffectiveSize());
		this.angle = 0;
		this.duration = 0;
		this.diesAfterHit = false;
		this.velocity = direction.normalize().scale(speed);
		this.setHitBox(new Rectangle2D.Double(this.x, this.y, this.width, this.height));
	}

	@Override
	public void updatePhysics() {
		duration++;
		if ((duration >= 100 && Entity.checkCollision(this, gameObj.getPlayer())) || duration >= 500) {
			isDead = true;
		}
		// Homing arc back to player
		Vec2 playerPos = new Vec2(gameObj.getPlayer().getX(), gameObj.getPlayer().getY());
		Vec2 toPlayer = playerPos.sub(this.position).normalize();
		velocity = velocity.add(toPlayer.scale(0.08));
		double maxSpeed = 7.0;
		if (velocity.length() > maxSpeed)
			velocity = velocity.normalize().scale(maxSpeed);
		position = position.add(velocity);
		angle += 0.2;
		this.x = (int) position.getX();
		this.y = (int) position.getY();
		((RectangularShape) this.getHitBox()).setFrame(x - width / 2, y - height / 2, this.width, this.height);
	}
	/*
	 * Vec2 playerPos = new Vec2(gameObj.getPlayer().getX(),
	 * gameObj.getPlayer().getY()); Vec2 toPlayer = playerPos.sub(this.position);
	 * double dist = toPlayer.length(); Vec2 toPlayerNorm = toPlayer.normalize();
	 * double homingStrength = Math.max(0.4, Math.min(1.5, dist / 80.0)); double
	 * alignment = velocity.normalize().dot(toPlayerNorm); 
	 * if (alignment < 0)
	 * homingStrength *= 2.5; velocity =
	 * velocity.add(toPlayerNorm.scale(homingStrength)); double maxSpeed = 5.0 * (1
	 * + gameObj.getPlayer().getStatBonus("Projectile Speed Book")); if
	 * (velocity.length() > maxSpeed) velocity =
	 * velocity.normalize().scale(maxSpeed);
	 */

	@Override
	public void draw(Graphics2D g) {
		drawImpact(g);
		int screenX = x - gameObj.getCameraX() - width / 2;
		int screenY = y - gameObj.getCameraY() - height / 2;
		AffineTransform old = g.getTransform();
		g.rotate(angle, screenX + width / 2.0, screenY + height / 2.0);
		g.drawImage(sprite, screenX, screenY, width, height, null);
		g.setTransform(old);
	}
}