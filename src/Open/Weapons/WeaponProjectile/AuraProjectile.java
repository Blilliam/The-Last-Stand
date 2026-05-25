package Open.Weapons.WeaponProjectile;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RectangularShape;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import Open.Entities.Entity;
import Open.Entities.Enemies.Enemy;
import Open.Weapons.Weapon;
import main.AppPanel;
import main.GameObject;
import main.Vec2;

public class AuraProjectile extends WeaponEntity {

	public AuraProjectile(GameObject gameObj, Weapon weapon) {
		super(gameObj, weapon, new Vec2(0, 0), 0, 0);

		this.hitCooldowns = new HashMap<>();
		this.diesAfterHit = false;
		this.weapon = weapon;
		this.sprite = weapon.getSprite();
		this.position = new Vec2(AppPanel.WIDTH / 2, AppPanel.HEIGHT / 2);
		this.isDead = false;

		// Use getEffectiveSize() so the Size Book bonus applies
		this.width = (int) (weapon.getEffectiveSize() * 300);
		this.height = (int) (weapon.getEffectiveSize() * 300);

		// Initialise position and hitbox immediately so it's not at (0,0) on frame 1
		updatePhysics();
		this.x = (int) position.getX();
		this.y = (int) position.getY();
		this.setHitBox(new Ellipse2D.Double(this.x, this.y, this.width, this.height));
	}

	@Override
	protected void updatePhysics() {
		Entity player = gameObj.getPlayer();
		position.setX(player.getX() + player.getWidth() / 2.0 - width / 2.0);
		position.setY(player.getY() + player.getHeight() / 2.0 - height / 2.0);
	}

	@Override
	public void update() {
		updatePhysics();

		this.x = (int) position.getX();
		this.y = (int) position.getY();

		((RectangularShape) this.getHitBox()).setFrame(this.x - gameObj.getPlayer().getWidth() / 2,
				this.y - gameObj.getPlayer().getHeight() / 2, this.width, this.height);

		// Tick cooldowns safely with an Iterator
		Iterator<Map.Entry<Enemy, Integer>> it = hitCooldowns.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Enemy, Integer> entry = it.next();
			int timeLeft = entry.getValue() - 1;
			if (timeLeft <= 0)
				it.remove();
			else
				entry.setValue(timeLeft);
		}

		for (Enemy e : gameObj.getEnemies()) {
			if (!e.isDying() && Entity.checkCollision(this, e) && !hitCooldowns.containsKey(e)) {
				e.damage(weapon.getDmg());
				impactX = e.getX();
				impactY = e.getY();
				drawingImpact = true;
				hitCooldowns.put(e, HIT_COOLDOWN);
			}
		}
	}

	@Override
	public void draw(Graphics2D g) {
		drawImpact(g);

		int screenX = (int) position.getX() - gameObj.getCameraX();
		int screenY = (int) position.getY() - gameObj.getCameraY();

		g.setColor(Color.BLUE);
		for (int i = 0; i < 5; i++) {
			double scale = Math.pow(0.6, i);
			int w = (int) (width * scale);
			int h = (int) (height * scale);
			int drawX = screenX - w / 2;
			int drawY = screenY - h / 2;
			g.drawOval(drawX, drawY, w, h);
		}
	}
}