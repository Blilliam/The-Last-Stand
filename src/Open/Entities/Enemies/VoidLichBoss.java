package Open.Entities.Enemies;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import Open.Entities.Entity;
import Open.Entities.Interactible.Teleporter;
import main.DamageResult;
import main.GameObject;

/**
 * VoidLichBoss — Stage 3 Final Boss
 *
 * Phases:
 *   1 (100%–60% HP) : Slow orbit of void orbs, fires 8-way spread
 *   2 (60%–30% HP)  : Faster, fires 12-way spread + spawns mini portals that shoot back
 *   3 (<30% HP)     : "Collapse" — implodes toward player, fires 16-way spread, orbs spiral inward
 *
 * Drawing: Fully procedural — no sprite sheet.
 */
public class VoidLichBoss extends Enemy {

    private static final String BOSS_NAME = "Nyarathul, the Void Eternal";

    private static final int BOSS_MAX_HP  = 4000;
    private static final int BOSS_ATK     = 50;
    private static final int BOSS_SPEED   = 2;
    private static final int BOSS_WIDTH   = 280;
    private static final int BOSS_HEIGHT  = 280;

    private int phase = 1;

    private double animTick   = 0;
    private int    orbAngle   = 0;
    private double voidPulse  = 0;

    private boolean phase2Triggered = false;
    private boolean phase3Triggered = false;
    private int     phaseFlashTimer = 0;
    private static final int PHASE_FLASH_DURATION = 80;

    private int spawnTimer       = 120;
    private static final int MAX_SPAWN = 120;

    private float   deathAlpha   = 1.0f;
    private boolean bossDying    = false;
    private boolean bossDead     = false;

    private int bossMaxHp;
    private int bossCurrHp;
    private int bossAtk;
    private int bossSpeed;

    private Rectangle2D.Double hitBox;

    private int shootCooldown = 0;
    private List<VoidShard> shards = new ArrayList<>();

    private final Teleporter teleporter;
    private final GameObject gameObj;

    private static class VoidShard {
        double x, y, vx, vy;
        int life = 280;
        float size;
        boolean isMini;

        VoidShard(double x, double y, double vx, double vy, float size, boolean isMini) {
            this.x = x; this.y = y;
            this.vx = vx; this.vy = vy;
            this.size = size;
            this.isMini = isMini;
        }

        void update() {
            x += vx;
            y += vy;
            life--;
            if (isMini) {
                vx *= 0.98;
                vy *= 0.98;
            }
        }

        boolean isDead() { return life <= 0; }
    }

    public VoidLichBoss(GameObject gameObj, Teleporter teleporter, int x, int y, double statMultiplier) {
        super(gameObj, x, y, 1, 1.0);
        this.gameObj    = gameObj;
        this.teleporter = teleporter;

        this.x      = x;
        this.y      = y;
        this.width  = BOSS_WIDTH;
        this.height = BOSS_HEIGHT;
        this.isDead = false;

        this.bossMaxHp  = (int) (BOSS_MAX_HP * statMultiplier);
        this.bossCurrHp = this.bossMaxHp;
        this.bossAtk    = (int) (BOSS_ATK  * statMultiplier);
        this.bossSpeed  = (int) Math.max(1, BOSS_SPEED * (1.0 + (statMultiplier - 1.0) * 0.2));

        int hbW = (int) (BOSS_WIDTH  * 0.70);
        int hbH = (int) (BOSS_HEIGHT * 0.75);
        this.hitBox = new Rectangle2D.Double(x - hbW / 2.0, y - hbH / 2.0, hbW, hbH);
    }

    @Override
    public void update() {
        if (bossDead) return;
        if (spawnTimer > 0) { spawnTimer--; return; }

        animTick  += 0.05;
        voidPulse  = (Math.sin(animTick * 1.8) + 1.0) / 2.0;

        int orbSpeed = phase == 3 ? 6 : (phase == 2 ? 4 : 2);
        orbAngle = (orbAngle + orbSpeed) % 360;

        checkPhaseTransitions();

        if (phaseFlashTimer > 0) phaseFlashTimer--;

        if (bossDying) {
            deathAlpha -= 0.010f;
            if (deathAlpha <= 0) {
                deathAlpha = 0;
                bossDead   = true;
                isDead     = true;
            }
            return;
        }

        syncHitBox();
        followPlayer();

        if (Entity.checkCollision(this, gameObj.getPlayer())) {
            gameObj.getPlayer().damage(bossAtk);
        }

        int shootRate = phase == 3 ? 45 : (phase == 2 ? 65 : 90);
        shootCooldown++;
        if (shootCooldown >= shootRate) {
            shootCooldown = 0;
            fireVoidVolley();
        }

        for (int i = shards.size() - 1; i >= 0; i--) {
            VoidShard s = shards.get(i);
            s.update();

            double dx   = s.x - gameObj.getPlayer().getX();
            double dy   = s.y - gameObj.getPlayer().getY();
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist < s.size + gameObj.getPlayer().getWidth() / 2.0) {
                gameObj.getPlayer().damage(bossAtk / 3);
                shards.remove(i);
                continue;
            }

            if (s.isDead()) shards.remove(i);
        }
    }

    private void checkPhaseTransitions() {
        if (!phase2Triggered && bossCurrHp < bossMaxHp * 0.60) {
            phase2Triggered  = true;
            phaseFlashTimer  = PHASE_FLASH_DURATION;
            phase            = 2;
            bossSpeed        = Math.max(bossSpeed + 1, (int) (bossSpeed * 1.3));
        }
        if (!phase3Triggered && bossCurrHp < bossMaxHp * 0.30) {
            phase3Triggered  = true;
            phaseFlashTimer  = PHASE_FLASH_DURATION;
            phase            = 3;
            bossSpeed        = Math.max(bossSpeed + 1, (int) (bossSpeed * 1.5));
            shards.clear();
        }
    }

    private void fireVoidVolley() {
        int count = phase == 3 ? 16 : (phase == 2 ? 12 : 8);
        double speed = phase == 3 ? 5.5 : (phase == 2 ? 4.0 : 3.0);
        float  size  = phase == 3 ? 16f  : (phase == 2 ? 13f  : 10f);

        double aimAngle = Math.atan2(
            gameObj.getPlayer().getY() - y,
            gameObj.getPlayer().getX() - x
        );

        for (int i = 0; i < count; i++) {
            double angle = aimAngle + (2 * Math.PI / count) * i;
            shards.add(new VoidShard(x, y,
                Math.cos(angle) * speed,
                Math.sin(angle) * speed,
                size, false));
        }

        if (phase == 3) {
            for (int i = 0; i < 4; i++) {
                double angle = aimAngle + (Math.PI / 2.0 * i) + Math.PI / 4.0;
                shards.add(new VoidShard(x, y,
                    Math.cos(angle) * (speed * 1.5),
                    Math.sin(angle) * (speed * 1.5),
                    size * 1.3f, false));
            }
        }
    }

    private void syncHitBox() {
        int hbW = (int) (BOSS_WIDTH  * 0.70);
        int hbH = (int) (BOSS_HEIGHT * 0.75);
        hitBox.setFrame(x - hbW / 2.0, y - hbH / 2.0, hbW, hbH);
    }

    private void followPlayer() {
        double dx   = gameObj.getPlayer().getX() - x;
        double dy   = gameObj.getPlayer().getY() - y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist > 0) {
            double mult = phase == 3 ? 0.8 : 0.5;
            x += (dx / dist) * bossSpeed * mult;
            y += (dy / dist) * bossSpeed * mult;
        }
    }

    @Override
    public void draw(Graphics2D g) {
        if (bossDead) return;

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int cx = (int) x - gameObj.getCameraX();
        int cy = (int) y - gameObj.getCameraY();

        float alpha = 1.0f;
        if (spawnTimer > 0)  alpha = 1.0f - ((float) spawnTimer / MAX_SPAWN);
        else if (bossDying)  alpha = deathAlpha;

        if (phaseFlashTimer > 0) {
            float fa = (float) phaseFlashTimer / PHASE_FLASH_DURATION * 0.55f;
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fa));
            g.setColor(new Color(80, 0, 180));
            g.fillRect(cx - BOSS_WIDTH, cy - BOSS_HEIGHT, BOSS_WIDTH * 2, BOSS_HEIGHT * 2);
        }

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        int bob = (int) (Math.sin(animTick * 0.8) * 7);

        drawVoidAura(g, cx, cy + bob);
        drawOrbRing(g, cx, cy + bob);
        drawCloak(g, cx, cy + bob);
        drawSkull(g, cx, cy + bob);
        drawCrown(g, cx, cy + bob);
        drawEyes(g, cx, cy + bob);

        drawShards(g);

        if (!bossDying) drawBossHPBar(g, cx, cy + bob);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
    }

    private void drawVoidAura(Graphics2D g, int cx, int cy) {
        int rings = phase == 3 ? 6 : (phase == 2 ? 4 : 3);
        for (int i = rings; i >= 1; i--) {
            int radius = 55 + i * 18 + (int) (Math.sin(animTick * 2.2 + i) * 8);
            int a      = 70 - i * 10;
            Color c = phase == 3
                ? new Color(180, 0, 255, Math.max(0, a))
                : (phase == 2
                    ? new Color(120, 0, 200, Math.max(0, a))
                    : new Color(60,  0, 140, Math.max(0, a)));
            g.setColor(c);
            g.fillOval(cx - radius, cy - radius / 2, radius * 2, radius);
        }
    }

    private void drawOrbRing(Graphics2D g, int cx, int cy) {
        int orbCount = phase == 3 ? 10 : (phase == 2 ? 7 : 5);
        int orbitR   = phase == 3 ? 80 : 70;

        double spiralFactor = phase == 3 ? (voidPulse * 0.3) : 0;

        for (int i = 0; i < orbCount; i++) {
            double angle = Math.toRadians(orbAngle + i * (360.0 / orbCount));
            int r  = (int) (orbitR - spiralFactor * 20);
            int ox = cx + (int) (Math.cos(angle) * r);
            int oy = cy + (int) (Math.sin(angle) * r * 0.45);

            Color glow = phase == 3
                ? new Color(255, 100, 255, 70)
                : new Color(140, 60,  255, 60);
            Color core = phase == 3
                ? new Color(230, 160, 255)
                : new Color(160, 100, 255);
            Color pupil = new Color(30, 0, 60);

            g.setColor(glow);  g.fillOval(ox - 12, oy - 12, 24, 24);
            g.setColor(core);  g.fillOval(ox - 6,  oy - 6,  12, 12);
            g.setColor(pupil); g.fillOval(ox - 3,  oy - 3,  6,  6);

            if (phase >= 2) {
                g.setColor(new Color(200, 120, 255, 80));
                g.setStroke(new BasicStroke(1f));
                g.drawLine(cx, cy, ox, oy);
            }
        }
    }

    private void drawCloak(Graphics2D g, int cx, int cy) {
        int hem = (int) (voidPulse * 10);
        Color cloakColor = phase == 3
            ? new Color(50, 0, 90)
            : new Color(20, 0, 50);

        int[] fullX = { cx - 50, cx - 22, cx + 22, cx + 50,
                        cx + 52 + hem, cx + 32, cx, cx - 32, cx - 52 - hem };
        int[] fullY = { cy - 10, cy - 40, cy - 40, cy - 10,
                        cy + 75, cy + 85 + hem, cy + 80 + hem / 2, cy + 85 + hem, cy + 75 };
        g.setColor(cloakColor);
        g.fillPolygon(fullX, fullY, fullX.length);

        g.setColor(new Color(120, 40, 200, 100));
        g.setStroke(new BasicStroke(2));
        g.drawLine(cx, cy - 40, cx, cy + 65);

        g.setColor(new Color(80, 0, 140));
        g.fillOval(cx - 24, cy - 46, 48, 22);
    }

    private void drawSkull(Graphics2D g, int cx, int cy) {
        Color skullColor = phase == 3
            ? new Color(200, 180, 240)
            : new Color(220, 215, 235);

        g.setColor(skullColor);
        g.fillOval(cx - 30, cy - 85, 60, 55);

        g.setColor(new Color(180, 160, 210));
        g.fillRoundRect(cx - 22, cy - 44, 44, 20, 10, 10);

        g.setColor(new Color(240, 230, 255));
        int[] teethX = { cx - 18, cx - 9, cx, cx + 9 };
        for (int tx : teethX) {
            g.fillPolygon(
                new int[]{ tx,     tx + 6, tx + 12 },
                new int[]{ cy - 44, cy - 34, cy - 44 },
                3
            );
        }

        g.setColor(new Color(60, 20, 100, 180));
        g.setStroke(new BasicStroke(1.5f));
        g.drawOval(cx - 30, cy - 85, 60, 55);
        g.drawRoundRect(cx - 22, cy - 44, 44, 20, 10, 10);
    }

    private void drawCrown(Graphics2D g, int cx, int cy) {
        Color crownBase  = new Color(180, 100, 255);
        Color crownDark  = new Color(100, 30, 180);
        Color gemColor   = phase == 3 ? new Color(255, 200, 255) : new Color(220, 140, 255);

        g.setColor(crownBase);
        g.fillRect(cx - 28, cy - 98, 56, 15);

        int[] bx    = { cx - 28, cx - 14, cx, cx + 14, cx + 28 };
        int[] hgts  = { 12, 22, 30, 22, 12 };
        for (int i = 0; i < 5; i++) {
            g.setColor(crownBase);
            int[] sx = { bx[i] - 7, bx[i], bx[i] + 7 };
            int[] sy = { cy - 98, cy - 98 - hgts[i], cy - 98 };
            g.fillPolygon(sx, sy, 3);
            g.setColor(crownDark);
            g.setStroke(new BasicStroke(1f));
            g.drawPolygon(sx, sy, 3);
        }

        g.setColor(crownDark);
        g.setStroke(new BasicStroke(1.5f));
        g.drawRect(cx - 28, cy - 98, 56, 15);

        g.setColor(gemColor);
        g.fillOval(cx - 6, cy - 104, 12, 12);
        g.setColor(new Color(255, 240, 255));
        g.fillOval(cx - 3, cy - 102, 5, 5);
    }

    private void drawEyes(Graphics2D g, int cx, int cy) {
        Color glowColor = phase == 3
            ? new Color(255, 80, 255, 130)
            : (phase == 2
                ? new Color(200, 80, 255, 110)
                : new Color(140, 60, 255, 100));
        Color irisColor = phase == 3
            ? new Color(255, 150, 255)
            : new Color(200, 120, 255);

        g.setColor(glowColor);
        g.fillOval(cx - 24, cy - 72, 16, 12);
        g.fillOval(cx +  8, cy - 72, 16, 12);

        g.setColor(new Color(10, 0, 25));
        g.fillOval(cx - 22, cy - 70, 12, 9);
        g.fillOval(cx +  9, cy - 70, 12, 9);

        g.setColor(irisColor);
        g.fillOval(cx - 19, cy - 68, 7, 6);
        g.fillOval(cx + 11, cy - 68, 7, 6);

        g.setColor(Color.BLACK);
        g.fillOval(cx - 17, cy - 67, 4, 4);
        g.fillOval(cx + 13, cy - 67, 4, 4);

        if (phase == 3) {
            g.setColor(new Color(255, 100, 255, 80));
            g.fillOval(cx - 32, cy - 80, 28, 20);
            g.fillOval(cx +  4, cy - 80, 28, 20);
        }
    }

    private void drawShards(Graphics2D g) {
        for (VoidShard s : shards) {
            int sx = (int) s.x - gameObj.getCameraX();
            int sy = (int) s.y - gameObj.getCameraY();
            int sz = (int) s.size;

            g.setColor(new Color(140, 0, 200, 90));
            g.fillOval(sx - sz - 5, sy - sz - 5, (sz + 5) * 2, (sz + 5) * 2);

            g.setColor(new Color(210, 140, 255));
            g.fillOval(sx - sz, sy - sz, sz * 2, sz * 2);

            g.setColor(new Color(255, 220, 255));
            g.fillOval(sx - sz / 2, sy - sz / 2, sz, sz);
        }
    }

    private void drawBossHPBar(Graphics2D g, int cx, int cy) {
        int barW = 220;
        int barH = 12;
        int xPos = cx - barW / 2;
        int yPos = cy - BOSS_HEIGHT / 2 - 32;

        g.setColor(new Color(0, 0, 0, 200));
        g.fillRoundRect(xPos - 2, yPos - 2, barW + 4, barH + 4, 6, 6);

        g.setColor(new Color(40, 0, 80));
        g.fillRoundRect(xPos, yPos, barW, barH, 4, 4);

        double pct = (double) bossCurrHp / bossMaxHp;
        Color fill = phase == 3
            ? new Color(220, 80,  255)
            : (phase == 2
                ? new Color(160, 50,  220)
                : new Color(100, 30,  180));
        g.setColor(fill);
        g.fillRoundRect(xPos, yPos, (int) (barW * pct), barH, 4, 4);

        g.setColor(new Color(140, 60, 200));
        g.setStroke(new BasicStroke(1f));
        g.drawRoundRect(xPos, yPos, barW, barH, 4, 4);

        String phaseLabel = phase == 3 ? "  \u2734 VOID COLLAPSE" : (phase == 2 ? "  \u2734 ASCENDING" : "");
        String label = BOSS_NAME + phaseLabel;
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.setColor(new Color(220, 180, 255));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(label, cx - fm.stringWidth(label) / 2, yPos - 4);
    }

    @Override
    public void damage(DamageResult result) {
        if (bossDying || spawnTimer > 0) return;

        bossCurrHp -= result.damage;
        gameObj.addDamageText(x, y, result.damage, result.isCrit);

        if (bossCurrHp <= 0) {
            bossCurrHp = 0;
            bossDying  = true;
            if (teleporter != null) teleporter.setBossIsDefeated(true);
            for (int i = 0; i < 35; i++) {
                gameObj.addExp(8,
                    (int) (x + (Math.random() * 100 - 50)),
                    (int) (y + (Math.random() * 100 - 50)));
            }
        }
    }

    @Override
    public boolean isDying() { return bossDying; }

    public boolean isBossDead()  { return bossDead; }
    public int     getPhase()    { return phase; }
    public Rectangle2D getHitBox() { return hitBox; }
}