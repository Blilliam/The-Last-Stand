package Open.Weapons.WeaponProjectile;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Arc2D;
import Open.Entities.Enemies.Enemy;
import Open.Entities.Entity;
import Open.Weapons.Weapon;
import main.AppPanel;
import main.GameObject;
import main.Vec2;
import main.enums.WeaponUpgrades;

public class SwordProjectile extends WeaponEntity {

    private final int MAX_LIFETIME = 12;

    public SwordProjectile(GameObject gameObj, Weapon weapon, Vec2 direction, int x, int y) {
        super(gameObj, weapon, direction, x, y);

        // Define the reach of the crescent
        this.width = (int) (260 * weapon.getStats().get(WeaponUpgrades.AttackSize));
        this.height = (int) (260 * weapon.getStats().get(WeaponUpgrades.AttackSize));

        this.velocity = new Vec2(0, 0); // Stationary relative to player
        this.angle = Math.atan2(direction.getY(), direction.getX());

        // Allow hitting multiple enemies in one swing
        this.diesAfterHit = false;

        syncHitboxToPlayer();
    }

    private void syncHitboxToPlayer() {
        // Center hitbox on the player's center
        double pCenterX = gameObj.getPlayer().getX() + (gameObj.getPlayer().getWidth() / 2.0);
        double pCenterY = gameObj.getPlayer().getY() + (gameObj.getPlayer().getHeight() / 2.0);

        this.position.setX(pCenterX - (this.width / 2.0));
        this.position.setY(pCenterY - (this.height / 2.0));

        this.x = (int) position.getX();
        this.y = (int) position.getY();
    }

    @Override
    protected void updatePhysics() {
        syncHitboxToPlayer();
        duration++;
        if (duration > MAX_LIFETIME) {
            isDead = true;
        }
    }

    @Override
    public void update() {
        updatePhysics();

        // Update Entity superclass x/y for collision math
        this.x = (int) position.getX();
        this.y = (int) position.getY();

        // Handle hit cooldowns (logic from super)
        hitCooldowns.entrySet().removeIf(entry -> {
            entry.setValue(entry.getValue() - 1);
            return entry.getValue() <= 0;
        });

        // Custom collision to ensure impact hits the enemy center
        for (Enemy e : gameObj.getEnemies()) {
            if (e != null && !e.isDying() && !e.isDead() && Entity.rectCollision(this, e)) {
                if (!hitCooldowns.containsKey(e)) {
                    e.damage(weapon.getDmg());
                    onHitEffect(e);

                    // HIT LOCATION FIX: Set impact coordinates to enemy center
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
        // 1. Draw the impact first so it appears under/at the slash
        drawImpact(g);

        // 2. Draw the Crescent Moon centered on the player
        int screenX = AppPanel.WIDTH / 2;
        int screenY = AppPanel.HEIGHT / 2;

        AffineTransform oldTransform = g.getTransform();
        g.translate(screenX, screenY);
        g.rotate(this.angle);

        double progress = (double) duration / MAX_LIFETIME;
        float alpha = Math.max(0, 1.0f - (float) progress);

        // Visual growth effect
        double grow = 0.8 + (0.4 * progress);
        int dW = (int) (width * grow);
        int dH = (int) (height * grow);

        // Crescent math: Subtract inner arc from outer arc
        int thickness = (int) (dW * 0.18 * (1.0 - progress));

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        Arc2D outer = new Arc2D.Double(-dW / 2.0, -dH / 2.0, dW, dH, -70, 140, Arc2D.PIE);
        Arc2D inner = new Arc2D.Double(-dW / 2.0 + thickness, -dH / 2.0 + thickness,
                dW - (thickness * 2), dH - (thickness * 2), -100, 200, Arc2D.PIE);

        Area crescent = new Area(outer);
        crescent.subtract(new Area(inner));

        // Colors (Moonlight Blue/White)
        g.setColor(new Color(170, 210, 255));
        g.fill(crescent);
        g.setColor(Color.WHITE);
        g.draw(crescent);

        g.setTransform(oldTransform);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }
}