package Open.Weapons.WeaponProjectile;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.RectangularShape;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import Open.Entities.Entity;
import Open.Entities.Enemies.Enemy;
import Open.Weapons.Weapon;
import main.Animation;
import main.AppPanel;
import main.Assets;
import main.GameObject;
import main.Vec2;
import main.enums.WeaponUpgrades;

public abstract class WeaponEntity extends Entity {
	protected BufferedImage sprite;
	protected HashMap<Enemy, Integer> hitCooldowns;
	protected Weapon weapon;
	protected Vec2 position;
	protected Vec2 velocity;
	protected double currProjectileBounces;
	protected double angle;
	protected double duration;

	protected boolean diesAfterHit;

	protected boolean drawingImpact;
	protected Animation impactAnim;
	protected int impactX, impactY, impactWidth, impactHeight;
	protected final int HIT_COOLDOWN = 15;

	public WeaponEntity(GameObject gameObj, Weapon weapon, Vec2 direction, int x, int y) {
		super(gameObj);
		this.impactAnim = new Animation(Assets.impact, 25);
		this.impactWidth = 50;
		this.impactHeight = 50;
		this.hitCooldowns = new HashMap<>();
		this.weapon = weapon;
		this.sprite = weapon.getSprite();
		this.position = new Vec2(x, y);
		diesAfterHit = true;
		if (weapon.getStats().get(WeaponUpgrades.ProjectileSpeed) != null) {
			this.velocity = direction.normalize().scale(weapon.getStats().get(WeaponUpgrades.ProjectileSpeed));
		} else {
			this.velocity = new Vec2(0, 0);
		}

		this.isDead = false;
		// Initial angle based on launch

		this.angle = Math.atan2(velocity.getY(), velocity.getX());
	}

	// MAKE THIS ABSTRACT so children MUST implement movement
	protected abstract void updatePhysics();

	public void update() {
		updatePhysics(); // This calls the BouncingProjectile's code!

		// Update Entity superclass x/y for collision math
		this.x = (int) position.getX();
		this.y = (int) position.getY();

		// Handle hit cooldowns
		hitCooldowns.entrySet().removeIf(entry -> {
			entry.setValue(entry.getValue() - 1);
			return entry.getValue() <= 0;
		});

		for (Enemy e : gameObj.getEnemies()) {
			if (e != null && !e.isDying() && !e.isDead() && Entity.checkCollision(this, e)) {

				// Only trigger if we aren't on cooldown for this specific enemy
				if (!hitCooldowns.containsKey(e)) {
					e.damage(weapon.getDmg());

					// Trigger the impact particles/effects
					onHitEffect(e);
					impactX = x;
					impactY = y;
					drawingImpact = true;

					hitCooldowns.put(e, HIT_COOLDOWN);

					if (!weapon.getStats().containsKey(WeaponUpgrades.ProjectileBounce) || !diesAfterHit) {
						continue;
					}
					currProjectileBounces--;
					if (currProjectileBounces < 0) { // < 0 because 0 bounces means 1 hit total
						isDead = true;
						break;
					}
				}
			}
		}
	}

	protected void onHitEffect(Enemy e) {
	}

	@Override
	public void draw(Graphics2D g) {
		drawImpact(g);

		int screenX = x - gameObj.getCameraX();
		int screenY = y - gameObj.getCameraY();

		AffineTransform old = g.getTransform();
		g.translate(screenX, screenY);
		g.rotate(Math.atan2(velocity.getY(), velocity.getX())); // Point in direction of travel
		g.drawImage(sprite, -width / 2, -height / 2, width, height, null);
		g.setTransform(old);
	}

	protected void drawImpact(Graphics2D g) {
		if (drawingImpact) {
			int ix = impactX - gameObj.getCameraX() - impactWidth / 2;
			int iy = impactY - gameObj.getCameraY() - impactHeight / 2;
			g.drawImage(impactAnim.getFrame(), ix, iy, impactWidth, impactHeight, null);
			impactAnim.update();
			if (impactAnim.getCurrentFrameIndex() == impactAnim.getFrameLength() - 1) {
				drawingImpact = false;
				impactAnim.setFrame(0);
			}
		}
	}
}