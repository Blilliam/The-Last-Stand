package Open.Weapons.WeaponProjectile;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.util.Random;

import Open.Entities.Entity;
import Open.Entities.Enemies.Enemy;
import Open.Weapons.Weapon;
import main.GameObject;
import main.Vec2;
import main.enums.WeaponUpgrades;

public class KatanaProjectile extends WeaponEntity {
    private int lifeTimer = 0;
    private final int MAX_LIFE = 15;
    private final int SLASH_COUNT = 6;
    private Line2D.Double[] lines;
    private Random rand = new Random();

    public KatanaProjectile(GameObject gameObj, Weapon weapon, Vec2 direction, int x, int y) {
        super(gameObj, weapon, direction, x, y);
        double size = 60 * weapon.getStats().getOrDefault(WeaponUpgrades.AttackSize, 1.0);
        this.width = (int) (size/2);
		this.height = (int) (size/2);
        this.diesAfterHit = false;

        lines = new Line2D.Double[SLASH_COUNT];
        for (int i = 0; i < SLASH_COUNT; i++) {
            // Create random diagonal start and end points relative to the center (0,0)
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
        if (lifeTimer >= MAX_LIFE) isDead = true;
    }
    @Override
	public void update() {
		updatePhysics();

		x = (int) position.getX();
		y = (int) position.getY();

		for (Enemy e : gameObj.getEnemies()) {

			if (hitCooldowns.containsKey(e)) {
				int time = hitCooldowns.get(e) - 1;

				if (time <= 0) {
					hitCooldowns.remove(e);
				} else {
					hitCooldowns.put(e, time);
				}
			}

			if (!e.isDying() && Entity.circleCollision(this, e) && !hitCooldowns.containsKey(e)) {
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
        int screenX = x - gameObj.getCameraX();
        int screenY = y - gameObj.getCameraY();

        g.setColor(Color.WHITE);
        for (int i = 0; i < SLASH_COUNT; i++) {
            if (lifeTimer > i * 2) {
                // Thicker lines for the more recent slashes
                g.setStroke(new BasicStroke(Math.max(1, 3 - (lifeTimer / 5))));
                Line2D.Double l = lines[i];
                g.drawLine(screenX + (int)l.x1, screenY + (int)l.y1, 
                           screenX + (int)l.x2, screenY + (int)l.y2);
            }
        }
        g.setStroke(new BasicStroke(1)); // Reset stroke
    }
}