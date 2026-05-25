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

	private double speed = 8.0;

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

		double maxSpeed = 10.0;
		if (velocity.length() > maxSpeed)
			velocity = velocity.normalize().scale(maxSpeed);

		position = position.add(velocity);
		angle += 0.2;

		this.x = (int) position.getX();
		this.y = (int) position.getY();
		((RectangularShape) this.getHitBox()).setFrame(x - width / 2, y - height / 2, this.width, this.height);
	}

	@Override
	public void draw(Graphics2D g) {
		drawImpact(g);

		int screenX = x - gameObj.getPlayer().getX() + AppPanel.WIDTH / 2 - width / 2;
		int screenY = y - gameObj.getPlayer().getY() + AppPanel.HEIGHT / 2 - height / 2;

		AffineTransform old = g.getTransform();
		g.rotate(angle, screenX + width / 2.0, screenY + height / 2.0);
		g.drawImage(sprite, screenX, screenY, width, height, null);
		g.setTransform(old);
	}
}