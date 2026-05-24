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

public class FireDemonBoss extends Enemy {

    // ─── Identity ────────────────────────────────────────────────────
    private static final String BOSS_NAME = "Ignis, the Eternal Flame";
    private Teleporter teleporter;

    // ─── Stats ───────────────────────────────────────────────────────
    private static final int BOSS_MAX_HP    = 1800;
    private static final int BOSS_ATK       = 25;
    private static final int BOSS_SPEED     = 2;
    private static final int BOSS_WIDTH     = 220;
    private static final int BOSS_HEIGHT    = 220;

    // ─── Phase ───────────────────────────────────────────────────────
    private int phase = 1; // 1 = normal, 2 = enraged (<40% hp)

    // ─── Animation ───────────────────────────────────────────────────
    private double animTick   = 0;
    private int    wingAngle  = 0; // drives wing flap
    private double flamePulse = 0; // 0-1 for outer fire ring

    // ─── Enrage ──────────────────────────────────────────────────────
    private boolean enrageTriggered    = false;
    private int     enrageFlashTimer   = 0;
    private static final int ENRAGE_FLASH_DURATION = 60;

    // ─── Spawn ───────────────────────────────────────────────────────
    private int spawnTimer          = 90;
    private static final int MAX_SPAWN = 90;

    // ─── Death ───────────────────────────────────────────────────────
    private float   deathAlpha   = 1.0f;
    private boolean deathStarted = false;

    // ─── Internal state ──────────────────────────────────────────────
    private int     bossMaxHp;
    private int     bossCurrHp;
    private boolean bossDead  = false;
    private boolean bossDying = false;
    private int     bossAtk;
    private int     bossSpeed;

    // ─── Hitbox ──────────────────────────────────────────────────────
    private Rectangle2D.Double hitBox;

    // ─── Projectile attack ───────────────────────────────────────────
    private int  shootCooldown     = 0;
    private int  SHOOT_RATE        = 120; // frames between volleys (2s at 60fps)
    private int  SHOOT_RATE_ENRAGE = 60;  // faster in phase 2
    private static final int FIREBALL_COUNT = 6; // number of fireballs per volley

    // Simple inner class — no external WeaponEntity needed
    private List<Fireball> fireballs = new ArrayList<>();

    private static class Fireball {
        double x, y, vx, vy;
        int life = 300;
        float size;

        Fireball(double x, double y, double vx, double vy, float size) {
            this.x = x; this.y = y;
            this.vx = vx; this.vy = vy;
            this.size = size;
        }

        void update() { x += vx; y += vy; life--; }
        boolean isDead() { return life <= 0; }
    }

    // ─── Reference ───────────────────────────────────────────────────
    private final GameObject gameObj;

    // ═════════════════════════════════════════════════════════════════
    public FireDemonBoss(GameObject gameObj, Teleporter tel, int x, int y, double statMultiplier) {
        super(gameObj, x, y, 1, 1.0); // pass type=1 to satisfy Enemy super
        
        this.teleporter = tel;
        this.gameObj = gameObj;

        this.x = x;
        this.y = y;
        this.width  = BOSS_WIDTH;
        this.height = BOSS_HEIGHT;
        this.isDead = false;

        this.bossMaxHp  = (int) (BOSS_MAX_HP * statMultiplier);
        this.bossCurrHp = this.bossMaxHp;
        this.bossAtk    = (int) (BOSS_ATK  * statMultiplier);
        this.bossSpeed  = (int) Math.max(1, BOSS_SPEED * (1.0 + (statMultiplier - 1.0) * 0.2));

        int hbW = (int) (BOSS_WIDTH  * 0.75);
        int hbH = (int) (BOSS_HEIGHT * 0.80);
        this.hitBox = new Rectangle2D.Double(x - hbW / 2.0, y - hbH / 2.0, hbW, hbH);
    }

    // ═════════════════════════════════════════════════════════════════
    // UPDATE
    // ═════════════════════════════════════════════════════════════════
    @Override
    public void update() {
        if (bossDead) return;

        if (spawnTimer > 0) { spawnTimer--; return; }

        animTick  += 0.07;
        wingAngle  = (wingAngle + (phase == 2 ? 5 : 3)) % 360;
        flamePulse = (Math.sin(animTick * 2.0) + 1.0) / 2.0;

        // ── Phase check ──────────────────────────────────────────────
        if (!enrageTriggered && bossCurrHp < bossMaxHp * 0.40) {
            enrageTriggered  = true;
            enrageFlashTimer = ENRAGE_FLASH_DURATION;
            phase            = 2;
            bossSpeed        = Math.max(bossSpeed + 1, (int) (bossSpeed * 1.5));
        }
        if (enrageFlashTimer > 0) enrageFlashTimer--;

        // ── Death sequence ───────────────────────────────────────────
        if (bossDying) {
            deathAlpha -= 0.015f;
            if (deathAlpha <= 0) {
                deathAlpha = 0;
                bossDead   = true;
                isDead     = true;
                teleporter.setBossIsDefeated(true);
            }
            return;
        }

        // ── Hitbox sync ──────────────────────────────────────────────
        int hbW = (int) (BOSS_WIDTH  * 0.75);
        int hbH = (int) (BOSS_HEIGHT * 0.80);
        hitBox.setFrame(x - hbW / 2.0, y - hbH / 2.0, hbW, hbH);

        // ── Move toward player ───────────────────────────────────────
        followPlayer();

        // ── Melee contact damage ─────────────────────────────────────
        if (Entity.checkCollision(this, gameObj.getPlayer())) {
            gameObj.getPlayer().damage(bossAtk);
        }

        // ── Projectile volley ────────────────────────────────────────
        int currentShootRate = (phase == 2) ? SHOOT_RATE_ENRAGE : SHOOT_RATE;
        shootCooldown++;
        if (shootCooldown >= currentShootRate) {
            shootCooldown = 0;
            spawnFireballVolley();
        }

        // ── Update fireballs ─────────────────────────────────────────
        for (int i = fireballs.size() - 1; i >= 0; i--) {
            Fireball fb = fireballs.get(i);
            fb.update();

            // Hit player
            double dx  = fb.x - gameObj.getPlayer().getX();
            double dy  = fb.y - gameObj.getPlayer().getY();
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist < fb.size + gameObj.getPlayer().getWidth() / 2.0) {
                gameObj.getPlayer().damage(bossAtk / 2);
                fireballs.remove(i);
                continue;
            }

            if (fb.isDead()) fireballs.remove(i);
        }
    }

    private void spawnFireballVolley() {
        int count = (phase == 2) ? FIREBALL_COUNT + 2 : FIREBALL_COUNT;
        float size = phase == 2 ? 18f : 14f;
        double speed = phase == 2 ? 4.5 : 3.5;

        // Spread fireballs evenly around a circle
        for (int i = 0; i < count; i++) {
            double angle = (2 * Math.PI / count) * i;
            // Add slight aim toward player for the first ring
            double aimAngle = Math.atan2(
                gameObj.getPlayer().getY() - y,
                gameObj.getPlayer().getX() - x
            );
            double finalAngle = aimAngle + (angle - Math.PI); // spread around aimed direction
            fireballs.add(new Fireball(x, y,
                Math.cos(finalAngle) * speed,
                Math.sin(finalAngle) * speed,
                size));
        }
    }

    // ═════════════════════════════════════════════════════════════════
    // DRAW
    // ═════════════════════════════════════════════════════════════════
    @Override
    public void draw(Graphics2D g) {
        if (bossDead) return;

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int cx = (int) x - gameObj.getCameraX();
        int cy = (int) y - gameObj.getCameraY();

        float alpha = 1.0f;
        if (spawnTimer > 0)  alpha = 1.0f - ((float) spawnTimer / MAX_SPAWN);
        else if (bossDying)  alpha = deathAlpha;

        // ── Enrage flash ─────────────────────────────────────────────
        if (enrageFlashTimer > 0) {
            float flashAlpha = (float) enrageFlashTimer / ENRAGE_FLASH_DURATION * 0.5f;
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, flashAlpha));
            g.setColor(new Color(255, 100, 0));
            g.fillRect(cx - BOSS_WIDTH, cy - BOSS_HEIGHT, BOSS_WIDTH * 2, BOSS_HEIGHT * 2);
        }

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        int bob = (int) (Math.sin(animTick) * 6);

        // ── Ground ember glow ────────────────────────────────────────
        g.setColor(new Color(255, 80, 0, 50));
        g.fillOval(cx - 55, cy + BOSS_HEIGHT / 2 - 10, 110, 22);

        // ── Phase 2 lava aura ────────────────────────────────────────
        if (phase == 2) drawLavaAura(g, cx, cy + bob);

        // ── Outer flame ring ─────────────────────────────────────────
        drawFlameRing(g, cx, cy + bob);

        // ── Wings ────────────────────────────────────────────────────
        drawWings(g, cx, cy + bob);

        // ── Body ─────────────────────────────────────────────────────
        drawBody(g, cx, cy + bob);

        // ── Head ─────────────────────────────────────────────────────
        drawHead(g, cx, cy + bob);

        // ── Horns ────────────────────────────────────────────────────
        drawHorns(g, cx, cy + bob);

        // ── Eyes ─────────────────────────────────────────────────────
        drawEyes(g, cx, cy + bob);

        // ── Fireballs ────────────────────────────────────────────────
        drawFireballs(g);

        // ── Health bar ───────────────────────────────────────────────
        if (!bossDying) drawBossHealthBar(g, cx, cy + bob);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);

        // ── Hitbox debug ─────────────────────────────────────────────
        g.setColor(new Color(255, 80, 0, 150));
        g.setStroke(new BasicStroke(1.5f));
        g.draw(new Rectangle2D.Double(
            hitBox.getX() - gameObj.getCameraX(),
            hitBox.getY() - gameObj.getCameraY(),
            hitBox.getWidth(), hitBox.getHeight()
        ));
    }

    // ─── Draw helpers ────────────────────────────────────────────────

    private void drawLavaAura(Graphics2D g, int cx, int cy) {
        for (int i = 3; i >= 1; i--) {
            int radius = 60 + i * 15 + (int) (Math.sin(animTick * 2.5 + i) * 6);
            int a = 55 - i * 12;
            g.setColor(new Color(255, 60 + i * 20, 0, a));
            g.fillOval(cx - radius, cy - radius / 2, radius * 2, radius);
        }
    }

    private void drawFlameRing(Graphics2D g, int cx, int cy) {
        int flameCount = phase == 2 ? 12 : 8;
        int baseRadius = 58;
        for (int i = 0; i < flameCount; i++) {
            double angle  = Math.toRadians((wingAngle * 2) + i * (360.0 / flameCount));
            int flameR    = baseRadius + (int) (flamePulse * 10);
            int fx        = cx + (int) (Math.cos(angle) * flameR);
            int fy        = cy + (int) (Math.sin(angle) * flameR * 0.55);
            int flameSize = 12 + (int) (flamePulse * 8);

            g.setColor(new Color(255, 200, 0, 130));
            g.fillOval(fx - flameSize, fy - flameSize, flameSize * 2, flameSize * 2);
            g.setColor(new Color(255, 80, 0, 80));
            g.fillOval(fx - flameSize - 4, fy - flameSize - 4, (flameSize + 4) * 2, (flameSize + 4) * 2);
        }
    }

    private void drawWings(Graphics2D g, int cx, int cy) {
        double flapOffset = Math.sin(Math.toRadians(wingAngle)) * 18;
        Color wingColor   = phase == 2 ? new Color(180, 30, 0) : new Color(140, 20, 0);
        Color wingEdge    = new Color(255, 100, 0, 160);

        // Left wing
        int[] lx = { cx - 30, cx - 90, cx - 110 - (int) flapOffset, cx - 80, cx - 45 };
        int[] ly = { cy - 20, cy - 60, cy,                            cy + 30, cy + 10 };
        g.setColor(wingColor);
        g.fillPolygon(lx, ly, 5);
        g.setColor(wingEdge);
        g.setStroke(new BasicStroke(2));
        g.drawPolygon(lx, ly, 5);

        // Right wing
        int[] rx = { cx + 30, cx + 90, cx + 110 + (int) flapOffset, cx + 80, cx + 45 };
        int[] ry = ly;
        g.setColor(wingColor);
        g.fillPolygon(rx, ry, 5);
        g.setColor(wingEdge);
        g.drawPolygon(rx, ry, 5);
    }

    private void drawBody(Graphics2D g, int cx, int cy) {
        Color bodyBase = phase == 2 ? new Color(200, 40, 0) : new Color(160, 30, 0);
        Color bodyShad = new Color(80, 10, 0);

        // Torso
        int[] bx = { cx - 40, cx - 50, cx - 35, cx + 35, cx + 50, cx + 40 };
        int[] by = { cy - 15,  cy + 50, cy + 70, cy + 70, cy + 50, cy - 15 };
        g.setColor(bodyBase);
        g.fillPolygon(bx, by, 6);

        // Belly highlight
        g.setColor(new Color(255, 120, 0, 80));
        g.fillOval(cx - 18, cy, 36, 40);

        // Arm stumps
        g.setColor(bodyShad);
        g.fillOval(cx - 55, cy - 5, 22, 35);
        g.fillOval(cx + 33, cy - 5, 22, 35);

        // Tail
        int[] tx = { cx - 10, cx - 25, cx - 50, cx - 45 };
        int[] ty = { cy + 68,  cy + 90, cy + 100, cy + 75 };
        g.setColor(bodyBase);
        g.fillPolygon(tx, ty, 4);
        g.setColor(new Color(255, 140, 0));
        g.fillOval(cx - 55, cy + 92, 14, 14); // tail tip flame
    }

    private void drawHead(Graphics2D g, int cx, int cy) {
        Color headColor = phase == 2 ? new Color(210, 50, 0) : new Color(175, 35, 0);
        // Skull-like head
        g.setColor(headColor);
        g.fillOval(cx - 30, cy - 75, 60, 55);

        // Jaw
        g.setColor(new Color(140, 20, 0));
        g.fillRoundRect(cx - 22, cy - 38, 44, 16, 10, 10);

        // Teeth (jagged white fangs)
        g.setColor(new Color(255, 240, 200));
        int[] teethX = { cx - 16, cx - 8, cx, cx + 8 };
        for (int tx : teethX) {
            g.fillPolygon(
                new int[]{ tx,     tx + 5, tx + 10 },
                new int[]{ cy - 38, cy - 30, cy - 38 },
                3
            );
        }

        // Head outline
        g.setColor(new Color(60, 10, 0, 200));
        g.setStroke(new BasicStroke(1.5f));
        g.drawOval(cx - 30, cy - 75, 60, 55);
    }

    private void drawHorns(Graphics2D g, int cx, int cy) {
        Color hornBase   = new Color(80, 10, 0);
        Color hornGlow   = phase == 2 ? new Color(255, 60, 0) : new Color(255, 140, 0);

        // Left horn
        int[] hx1 = { cx - 25, cx - 45, cx - 18 };
        int[] hy1 = { cy - 72, cy - 110, cy - 62 };
        g.setColor(hornBase);
        g.fillPolygon(hx1, hy1, 3);
        g.setColor(hornGlow);
        g.setStroke(new BasicStroke(1f));
        g.drawPolygon(hx1, hy1, 3);

        // Right horn
        int[] hx2 = { cx + 25, cx + 45, cx + 18 };
        int[] hy2 = hy1;
        g.setColor(hornBase);
        g.fillPolygon(hx2, hy2, 3);
        g.setColor(hornGlow);
        g.drawPolygon(hx2, hy2, 3);

        // Inner horn glow streaks
        g.setColor(new Color(255, 200, 0, 120));
        g.drawLine(cx - 28, cy - 72, cx - 40, cy - 105);
        g.drawLine(cx + 28, cy - 72, cx + 40, cy - 105);
    }

    private void drawEyes(Graphics2D g, int cx, int cy) {
        Color glowColor = phase == 2 ? new Color(255, 255, 0, 140) : new Color(255, 160, 0, 120);
        g.setColor(glowColor);
        g.fillOval(cx - 22, cy - 65, 14, 10);
        g.fillOval(cx + 8,  cy - 65, 14, 10);

        g.setColor(new Color(10, 5, 0));
        g.fillOval(cx - 20, cy - 64, 11, 8);
        g.fillOval(cx + 9,  cy - 64, 11, 8);

        Color irisColor = phase == 2 ? new Color(255, 255, 0) : new Color(255, 180, 0);
        g.setColor(irisColor);
        g.fillOval(cx - 18, cy - 62, 7, 6);
        g.fillOval(cx + 11, cy - 62, 7, 6);

        g.setColor(Color.BLACK);
        g.fillOval(cx - 17, cy - 61, 4, 4);
        g.fillOval(cx + 13, cy - 61, 4, 4);
    }

    private void drawFireballs(Graphics2D g) {
        for (Fireball fb : fireballs) {
            int sx = (int) fb.x - gameObj.getCameraX();
            int sy = (int) fb.y - gameObj.getCameraY();
            int s  = (int) fb.size;

            // Outer glow
            g.setColor(new Color(255, 100, 0, 80));
            g.fillOval(sx - s - 4, sy - s - 4, (s + 4) * 2, (s + 4) * 2);
            // Core
            g.setColor(new Color(255, 200, 50));
            g.fillOval(sx - s, sy - s, s * 2, s * 2);
            // Hot center
            g.setColor(new Color(255, 255, 200));
            g.fillOval(sx - s / 2, sy - s / 2, s, s);
        }
    }

    private void drawBossHealthBar(Graphics2D g, int cx, int cy) {
        int barW = 200;
        int barH = 10;
        int xPos = cx - barW / 2;
        int yPos = cy - BOSS_HEIGHT / 2 - 26;

        g.setColor(new Color(0, 0, 0, 180));
        g.fillRoundRect(xPos - 2, yPos - 2, barW + 4, barH + 4, 6, 6);

        g.setColor(new Color(80, 10, 0));
        g.fillRoundRect(xPos, yPos, barW, barH, 4, 4);

        double pct = (double) bossCurrHp / bossMaxHp;
        Color fillColor = phase == 2 ? new Color(255, 60, 0) : new Color(220, 100, 0);
        g.setColor(fillColor);
        g.fillRoundRect(xPos, yPos, (int) (barW * pct), barH, 4, 4);

        g.setColor(new Color(180, 50, 0));
        g.setStroke(new BasicStroke(1f));
        g.drawRoundRect(xPos, yPos, barW, barH, 4, 4);

        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.setColor(Color.WHITE);
        FontMetrics fm = g.getFontMetrics();
        String label = phase == 2 ? BOSS_NAME + "  \uD83D\uDD25 INFERNO" : BOSS_NAME;
        g.drawString(label, cx - fm.stringWidth(label) / 2, yPos - 4);
    }

    // ═════════════════════════════════════════════════════════════════
    // DAMAGE / DEATH
    // ═════════════════════════════════════════════════════════════════
    @Override
    public void damage(DamageResult result) {
        if (bossDying || spawnTimer > 0) return;

        bossCurrHp -= result.damage;
        gameObj.addDamageText(x, y, result.damage, result.isCrit);

        if (bossCurrHp <= 0) {
            bossCurrHp = 0;
            bossDying  = true;
            for (int i = 0; i < 25; i++) {
                gameObj.addExp(5,
                    (int) (x + (Math.random() * 80 - 40)),
                    (int) (y + (Math.random() * 80 - 40)));
            }
        }
    }

    @Override
    public boolean isDying() { return bossDying; }

    // ═════════════════════════════════════════════════════════════════
    // MOVEMENT
    // ═════════════════════════════════════════════════════════════════
    private void followPlayer() {
        double dx   = gameObj.getPlayer().getX() - x;
        double dy   = gameObj.getPlayer().getY() - y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist > 0) {
            x += (dx / dist) * bossSpeed * 0.5;
            y += (dy / dist) * bossSpeed * 0.5;
        }
    }

    // ═════════════════════════════════════════════════════════════════
    // GETTERS
    // ═════════════════════════════════════════════════════════════════
    public boolean isBossDead()     { return bossDead; }
    public int     getPhase()       { return phase; }
    public Rectangle2D getHitBox()  { return hitBox; }
}