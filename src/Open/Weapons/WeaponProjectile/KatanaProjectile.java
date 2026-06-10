package Open.Weapons.WeaponProjectile;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.RectangularShape;
import java.util.Random;

import Open.Entities.Entity;
import Open.Entities.Enemies.Enemy;
import Open.Weapons.Weapon;
import main.GameObject;
import main.Vec2;

public class KatanaProjectile extends WeaponEntity {

	private int lifeTimer = 0;
	private final int MAX_LIFE = 15;
	private final int SLASH_COUNT = 6;
	private Line2D.Double[] lines;
	private Random rand = new Random();

	public KatanaProjectile(GameObject gameObj, Weapon weapon, Vec2 direction, int x, int y) {
		super(gameObj, weapon, direction, x, y);

		// Use getEffectiveSize() so the Size Book bonus applies
		double size = 60 * weapon.getEffectiveSize();
		this.width = (int) (size * 1.5);
		this.height = (int) (size * 1.5);

		this.diesAfterHit = false;
		this.setHitBox(new Ellipse2D.Double(this.position.getX(), this.position.getY(), this.width, this.height));

		// Build random slash lines relative to centre
		lines = new Line2D.Double[SLASH_COUNT];
		for (int i = 0; i < SLASH_COUNT; i++) {
			double xOff = rand.nextInt(40) - 20;
			double yOff = rand.nextInt(40) - 20;
			double angle = rand.nextDouble() * Math.PI * 2;

			double x1 = xOff + Math.cos(angle) * size;
			double y1 = yOff + Math.sin(angle) * size;
			double x2 = xOff + Math.cos(angle + Math.PI) * size;
			double y2 = yOff + Math.sin(angle + Math.PI) * size;

			lines[i] = new Line2D.Double(x1, y1, x2, y2);
		}
	}

	@Override
	protected void updatePhysics() {
		lifeTimer++;
		if (lifeTimer >= MAX_LIFE)
			isDead = true;
	}

	@Override
	public void update() {
		updatePhysics();

		x = (int) position.getX();
		y = (int) position.getY();

		// Tick hit cooldowns
		hitCooldowns.entrySet().removeIf(entry -> {
			entry.setValue(entry.getValue() - 1);
			return entry.getValue() <= 0;
		});

		for (Enemy e : gameObj.getEnemies()) {
			if (!e.isDying() && Entity.checkCollision(this, e) && !hitCooldowns.containsKey(e)) {
				e.damage(weapon.getDmg());
				impactX = e.getX();
				impactY = e.getY();
				drawingImpact = true;
				hitCooldowns.put(e, HIT_COOLDOWN);
			}
		}

		((RectangularShape) this.getHitBox()).setFrame(x - width / 2, y - height / 2, this.width, this.height);
	}

	@Override
	public void draw(Graphics2D g) {
		int screenX = x - gameObj.getCameraX();
		int screenY = y - gameObj.getCameraY();

		g.setColor(Color.WHITE);
		for (int i = 0; i < SLASH_COUNT; i++) {
			if (lifeTimer > i * 2) {
				g.setStroke(new BasicStroke(Math.max(1, 3 - (lifeTimer / 5))));
				Line2D.Double l = lines[i];
				g.drawLine(screenX + (int) l.x1, screenY + (int) l.y1, screenX + (int) l.x2, screenY + (int) l.y2);
			}
		}
		g.setStroke(new BasicStroke(1));
	}
}