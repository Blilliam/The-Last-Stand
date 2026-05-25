package Open.Entities.Enemies;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

import Open.Entities.Entity;
import main.Animation;
import main.Assets;
import main.DamageResult;
import main.GameObject;

public class Enemy extends Entity {
    private Animation walkAnim;
    private Animation deathAnim;
    private int  deathHoldTimer = 0;
    private boolean dying = false;

    private int value;

    // Spawn animation
    private int spawnInTimer        = 60;
    private final int MAX_SPAWN_TIME = 60;

    // Hit flash
    private int damageFlashTimer    = 0;
    private final int FLASH_DURATION = 6;

    private int atk;

    public Enemy(GameObject gameObj, int x, int y, int type, double statMultiplier) {
        super(gameObj);
        this.x = x;
        this.y = y;
        this.width  = 100;
        this.height = 100;

        setHitBox(new Rectangle2D.Double(this.x, this.y, this.width, this.height));

        loadEnemy(type);

        // Apply difficulty scaling
        this.maxHp = (int)(this.maxHp * statMultiplier);
        this.atk   = (int)(this.atk   * statMultiplier);
        this.speed = (int)(this.speed  * (1.0 + (statMultiplier - 1.0) * 0.2));
        this.currHp = maxHp;

        this.value = (int) Math.max(1, (maxHp / 10.0) * statMultiplier);
    }

    private void loadEnemy(int num) {
        BufferedImage[] wFrames = null;
        BufferedImage[] dFrames = null;

        switch (num) {
            case 1 -> { wFrames = Assets.zombieWalk;      dFrames = Assets.zombieDeath;      speed = 3; maxHp = 15; atk = 10; }
            case 2 -> { wFrames = Assets.skeletonWalk;    dFrames = Assets.skeletonDeath;    speed = 2; maxHp = 12; atk = 12; }
            case 3 -> { wFrames = Assets.mudmanWalk;      dFrames = Assets.mudmanDeath;      speed = 1; maxHp = 40; atk = 20; }
            case 4 -> { wFrames = Assets.batWalk;         dFrames = Assets.batDeath;         speed = 4; maxHp =  8; atk =  5; }
            case 5 -> { wFrames = Assets.glowingBatWalk;  dFrames = Assets.glowingBatDeath;  speed = 5; maxHp = 35; atk = 25; }
        }

        this.walkAnim  = new Animation(wFrames, 100);
        this.deathAnim = new Animation(dFrames, 120);
    }

    // =========================================================================
    // UPDATE
    // =========================================================================

    public void update() {
        ((RectangularShape) this.getHitBox()).setFrame(
                this.x - (width / 2), this.y - (height / 2), this.width, this.height);

        if (spawnInTimer > 0) { spawnInTimer--; return; }

        if (damageFlashTimer > 0) damageFlashTimer--;

        if (!isDying()) {
            followPlayer();
            walkAnim.update();
            if (Entity.checkCollision(this, gameObj.getPlayer())) {
                gameObj.getPlayer().damage(atk);
            }
        } else {
            updateDeathAnimation();
        }
    }

    private void updateDeathAnimation() {
        if (deathAnim.getCurrentFrameIndex() < deathAnim.getFrameLength() - 1) {
            deathAnim.update();
        } else {
            deathHoldTimer++;
            if (deathHoldTimer > 40) isDead = true;
        }
    }

    // =========================================================================
    // DRAW
    // =========================================================================

    public void draw(Graphics2D g) {
        int drawX = (int) x - gameObj.getCameraX() - (width / 2);
        int drawY = (int) y - gameObj.getCameraY() - (height / 2);

        BufferedImage img = isDying() ? deathAnim.getFrame() : walkAnim.getFrame();
        if (img == null) return;

        if (spawnInTimer > 0) {
            float percent = 1.0f - ((float) spawnInTimer / MAX_SPAWN_TIME);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, percent)));
            g.drawImage(img, drawX, drawY, width, height, null);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            return;
        }

        if (damageFlashTimer > 0 && !isDying()) {
            drawFlash(g, img, drawX, drawY, width, height);
        } else {
            g.drawImage(img, drawX, drawY, width, height, null);
        }

        if (!isDying() && currHp < maxHp) {
            drawHealthBar(g, drawX, drawY);
        }
    }

    private void drawFlash(Graphics2D g, BufferedImage img, int x, int y, int w, int h) {
        float[] scales  = { 1.0f, 0f, 0f, 1.0f };
        float[] offsets = { 100f,  0f, 0f, 0f };
        RescaleOp tint = new RescaleOp(scales, offsets, null);
        g.drawImage(tint.filter(img, null), x, y, w, h, null);
    }

    private void drawHealthBar(Graphics2D g, int screenX, int screenY) {
        int barWidth = 40, barHeight = 5;
        int xPos = screenX + (width / 2) - (barWidth / 2);
        int yPos = screenY - 10;

        g.setColor(Color.BLACK);
        g.fillRect(xPos - 1, yPos - 1, barWidth + 2, barHeight + 2);
        g.setColor(Color.RED);
        g.fillRect(xPos, yPos, barWidth, barHeight);
        g.setColor(Color.GREEN);
        g.fillRect(xPos, yPos, (int)(barWidth * ((double) currHp / maxHp)), barHeight);
    }

    // =========================================================================
    // DAMAGE / DEATH
    // =========================================================================

    public void damage(DamageResult result) {
        if (isDying() || spawnInTimer > 0) return;

        currHp -= result.damage;
        damageFlashTimer = FLASH_DURATION;
        gameObj.addDamageText(x, y, result.damage, result.isCrit);

        if (currHp <= 0) die();
    }

    private void die() {
        setDying(true);
        deathAnim.setFrame(0);

        // --- EXP reward (with artifact + EXP Book bonus) ---
        double expMult = 1 + gameObj.getPlayer().getArtifactManager().getPercentBonusExp();
        int expReward  = (int)(value * expMult);

        gameObj.addExp(expReward, x, y);

        // Echo Shard / bonus exp drop
        if (Math.random() < gameObj.getPlayer().getArtifactManager().getBonusExpDropChance()) {
            gameObj.addExp(expReward, x + 5, y + 5);
        }

        // --- Gold reward (with artifact + Gold Book bonus) ---
        double bookGoldBonus = gameObj.getPlayer().getStatBonus("Gold Book") / 100.0;
        double goldMult = 1 + gameObj.getPlayer().getArtifactManager().getPercentBonusGold() + bookGoldBonus;
        gameObj.getPlayer().addGold((int)(value * goldMult));
    }

    // =========================================================================
    // MOVEMENT
    // =========================================================================

    private void followPlayer() {
        double dx   = gameObj.getPlayer().getX() - x;
        double dy   = gameObj.getPlayer().getY() - y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist > 0) {
            x += (dx / dist) * speed * 0.5;
            y += (dy / dist) * speed * 0.5;
        }
    }

    // =========================================================================
    // GETTERS / SETTERS
    // =========================================================================

    public boolean isDying()              { return dying; }
    public void    setDying(boolean dying) { this.dying = dying; }
}