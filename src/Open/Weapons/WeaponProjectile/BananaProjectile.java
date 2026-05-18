package Open.Weapons.WeaponProjectile;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

import Open.Entities.Entity;
import Open.Weapons.Weapon;
import main.AppPanel;
import main.GameObject;
import main.Vec2;
import main.enums.WeaponUpgrades;

public class BananaProjectile extends WeaponEntity {

	private double speed = 8.0; // Initial launch speed
	

	public BananaProjectile(GameObject gameObj, Weapon weapon, Vec2 direction, int x, int y) {
		super(gameObj, weapon, direction, x, y);
		this.width = (int) (50 * weapon.getStats().get(WeaponUpgrades.AttackSize));
		this.height = (int) (50 * weapon.getStats().get(WeaponUpgrades.AttackSize));
		this.angle = 0;
		this.duration = 0;
		this.diesAfterHit = false;

		// Initialize velocity based on direction
		this.velocity = direction.normalize().scale(speed);
		
		this.hitBox = new Rectangle2D.Double(this.x, this.y, this.width, this.height);
	}

	@Override
	public void updatePhysics() {
		// 1. Get the vector from Banana to Player
		duration++;
		
		if (duration >= weapon.getStats().get(WeaponUpgrades.Duration)) {
			isDead = true;
		}
		
		Vec2 playerPos = new Vec2(gameObj.getPlayer().getX(), gameObj.getPlayer().getY());
		Vec2 toPlayer = playerPos.sub(this.position).normalize();


		double returnStrength = 0.08;


		velocity = velocity.add(toPlayer.scale(returnStrength));

		// Cap the speed
		double maxSpeed = 10.0;
		if (velocity.length() > maxSpeed) {
			velocity = velocity.normalize().scale(maxSpeed);
		}

		// Apply Movement
		position = position.add(velocity);
		angle += 0.2; // The spin
		
		

		this.x = (int) position.getX();
		this.y = (int) position.getY();
		
		((RectangularShape) this.hitBox).setFrame(x - width/2, y - height/2, this.width, this.height);
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