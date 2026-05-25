package Open.Weapons.WeaponProjectile;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;

import Open.Entities.Enemies.Enemy;
import Open.Entities.Entity;
import Open.Weapons.Weapon;
import main.AppPanel;
import main.GameObject;
import main.Vec2;

public class DexecutionerProjectile extends WeaponEntity {

	private final int MAX_LIFETIME = 10;

	public DexecutionerProjectile(GameObject gameObj, Weapon weapon, Vec2 direction, int x, int y) {
		super(gameObj, weapon, direction, x, y);

		// Use getEffectiveSize() so the Size Book bonus applies
		double sizeMult = weapon.getEffectiveSize() * 2;
		this.width = (int) (180 * sizeMult);
		this.height = (int) (100 * sizeMult);

		this.velocity = new Vec2(0, 0);
		this.angle = Math.atan2(direction.getY(), direction.getX());
		this.diesAfterHit = false;

		syncHitboxToPlayer();
	}

	private void syncHitboxToPlayer() {
		double pCenterX = gameObj.getPlayer().getX();
		double pCenterY = gameObj.getPlayer().getY();

		double offsetX = Math.cos(angle) * (width / 2.0);
		double offsetY = Math.sin(angle) * (width / 2.0);
		this.position.setX(pCenterX + offsetX - (this.width / 2.0));
		this.position.setY(pCenterY + offsetY - (this.height / 2.0));
		this.x = (int) position.getX();
		this.y = (int) position.getY();

		double progress = (double) duration / MAX_LIFETIME;
		int currentLength = (int) (width * (0.4 + 0.6 * progress));
		int currentWidth = (int) (height * (1.0 - 0.5 * progress));

		int[] xPoints = { currentLength, 0, 0 };
		int[] yPoints = { 0, currentWidth / 2, -currentWidth / 2 };
		Polygon localSpike = new Polygon(xPoints, yPoints, 3);

		AffineTransform tx = new AffineTransform();
		tx.translate(pCenterX, pCenterY);
		tx.rotate(this.angle);
		this.setHitBox(tx.createTransformedShape(localSpike));
	}

	@Override
	protected void updatePhysics() {
		syncHitboxToPlayer();
		duration++;
		if (duration > MAX_LIFETIME)
			isDead = true;
	}

	@Override
	public void update() {
		updatePhysics();

		hitCooldowns.entrySet().removeIf(entry -> {
			entry.setValue(entry.getValue() - 1);
			return entry.getValue() <= 0;
		});

		for (Enemy e : gameObj.getEnemies()) {
			if (e != null && !e.isDying() && !e.isDead() && Entity.checkCollision(this, e)) {
				if (!hitCooldowns.containsKey(e)) {
					e.damage(weapon.getDmg());
					this.impactX = e.getX();
					this.impactY = e.getY();
					this.drawingImpact = true;
					this.impactAnim.setFrame(0);
					hitCooldowns.put(e, HIT_COOLDOWN);
				}
			}
		}
	}

	@Override
	public void draw(Graphics2D g) {
		drawImpact(g);

		int screenX = AppPanel.WIDTH / 2;
		int screenY = AppPanel.HEIGHT / 2;

		AffineTransform oldTransform = g.getTransform();
		g.translate(screenX, screenY);
		g.rotate(this.angle);

		double progress = (double) duration / MAX_LIFETIME;
		float alpha = Math.max(0, 1.0f - (float) progress);
		int currentLength = (int) (width * (0.4 + 0.6 * progress));
		int currentWidth = (int) (height * (1.0 - 0.5 * progress));

		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

		int[] xPoints = { currentLength, 0, 0 };
		int[] yPoints = { 0, currentWidth / 2, -currentWidth / 2 };
		Polygon spike = new Polygon(xPoints, yPoints, 3);

		g.setColor(new Color(160, 50, 255));
		g.fill(spike);
		g.setColor(Color.WHITE);
		g.drawLine(0, 0, currentLength, 0);

		g.setTransform(oldTransform);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
	}
}