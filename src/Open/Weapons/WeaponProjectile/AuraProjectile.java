package Open.Weapons.WeaponProjectile;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RectangularShape;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import Open.Entities.Entity;
import Open.Entities.Player;
import Open.Entities.Enemies.Enemy;
import Open.Weapons.Weapon;
import main.AppPanel;
import main.GameObject;
import main.Vec2;
import main.enums.WeaponUpgrades;

public class AuraProjectile extends WeaponEntity {

	public AuraProjectile(GameObject gameObj, Weapon weapon) {
		super(gameObj, weapon, new Vec2(0, 0), 0, 0);

		this.hitCooldowns = new HashMap<>();
		this.diesAfterHit = false;

		this.weapon = weapon;
		this.sprite = weapon.getSprite();
		this.position = new Vec2(AppPanel.WIDTH / 2, AppPanel.HEIGHT / 2);
		isDead = false;

		this.width = (int) (weapon.getStats().get(WeaponUpgrades.AttackSize) * 300);
		this.height = (int) (weapon.getStats().get(WeaponUpgrades.AttackSize) * 300);

		// Initialize position and hitbox immediately so it's not at (0,0) on frame 1
		updatePhysics();
		this.x = (int) position.getX();
		this.y = (int) position.getY();
		this.hitBox = new Ellipse2D.Double(this.x, this.y, this.width, this.height);
	}

	protected void updatePhysics() {
		Entity player = gameObj.getPlayer();
		// Calculates the precise top-left position to center the aura around the player
		position.setX(player.getX() + player.getWidth() / 2.0 - width / 2.0);
		position.setY(player.getY() + player.getHeight() / 2.0 - height / 2.0);
	}

	@Override
	public void update() {
		// 1. Calculate new vector positions
		updatePhysics();

		// 2. Map vector positions directly to the entity's integer coordinates
		this.x = (int) position.getX();
		this.y = (int) position.getY();

		// 3. Align hitbox directly to x and y (no offsets needed!)
		((RectangularShape) this.hitBox).setFrame(this.x - gameObj.getPlayer().getWidth()/2, this.y - gameObj.getPlayer().getHeight()/2, this.width, this.height);

		// 4. Update internal cooldown timers safely using an Iterator to prevent
		// crashes
		Iterator<Map.Entry<Enemy, Integer>> it = hitCooldowns.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Enemy, Integer> entry = it.next();
			int timeLeft = entry.getValue() - 1;
			if (timeLeft <= 0) {
				it.remove(); // Safely removes from the map during iteration
			} else {
				entry.setValue(timeLeft);
			}
		}

		// 5. Collision detection loop
		for (Enemy e : gameObj.getEnemies()) {
			if (!e.isDying() && Entity.checkCollision(this, e) && !hitCooldowns.containsKey(e)) {
				e.damage(weapon.getDmg());

				impactX = e.getX();
				impactY = e.getY();
				drawingImpact = true;

				// set cooldown
				hitCooldowns.put(e, HIT_COOLDOWN);
			}
		}
	}

	@Override
	public void draw(Graphics2D g) {
		drawImpact(g);

		// Drawing relative to the screen center since the player is anchored
		// center-screen
		int centerX = AppPanel.WIDTH / 2;
		int centerY = AppPanel.HEIGHT / 2;

		g.setColor(Color.BLUE);

		for (int i = 0; i < 5; i++) {
			double scale = Math.pow(0.6, i); // exponential shrink

			int w = (int) (width * scale);
			int h = (int) (height * scale);

			int drawX = centerX - w / 2;
			int drawY = centerY - h / 2;

			g.drawOval(drawX, drawY, w, h);
		}
	}
}