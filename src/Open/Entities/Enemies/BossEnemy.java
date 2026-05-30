package Open.Entities.Enemies;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;

import Open.Entities.Entity;
import Open.Entities.Interactible.Teleporter;
import main.DamageResult;
import main.GameObject;

public class BossEnemy extends Enemy {

	// ─── Boss Identity ────────────────────────────────────────────────
	private static final String BOSS_NAME = "The Lich King";
	private Teleporter tel;

	// ─── Boss Stats ───────────────────────────────────────────────────
	private static final int BOSS_MAX_HP = 2000; // 2k
	private static final int BOSS_ATK = 40;
	private static final int BOSS_SPEED = 2;
	private static final int BOSS_WIDTH = 250;
	private static final int BOSS_HEIGHT = 250;

	// ─── Phase ────────────────────────────────────────────────────────
	private int phase = 1; // 1 = normal, 2 = enraged (<50% hp)

	// ─── Custom drawing state ─────────────────────────────────────────
	private double animTick = 0; // drives all sinusoidal animations
	private double cloakFlare = 0; // 0‒1 pulse for the cloak hem
	private int orbAngle = 0; // degrees; rotating orbs

	// ─── Enrage flash ────────────────────────────────────────────────
	private boolean enrageTriggered = false;
	private int enrageFlashTimer = 0;
	private static final int ENRAGE_FLASH_DURATION = 60;

	// ─── Spawn animation ─────────────────────────────────────────────
	private int spawnTimer = 90;
	private static final int MAX_SPAWN = 90;

	// ─── Death fade ───────────────────────────────────────────────────
	private float deathAlpha = 1.0f;
	private boolean deathStarted = false;

	// ─── Internal hp/dead tracking (mirror parent fields via methods) ─
	private int bossMaxHp;
	private int bossCurrHp;
	private boolean bossDead = false;
	private boolean bossDying = false;
	private int bossAtk;
	private int bossSpeed;

	// ─── Hitbox ───────────────────────────────────────────────────────
	// Slightly smaller than sprite so hits feel fair
	private Rectangle2D.Double hitBox;

	// ─── Reference ───────────────────────────────────────────────────
	private final GameObject gameObj;

	// ═════════════════════════════════════════════════════════════════
	public BossEnemy(GameObject gameObj, Teleporter teleporter, int x, int y, double statMultiplier) {
		// Pass type=1 to satisfy super constructor; we override everything below
		super(gameObj, x, y, 1, 1.0);
		this.gameObj = gameObj;
		this.tel = teleporter;

		// Override position & size
		this.x = x;
		this.y = y;
		this.width = BOSS_WIDTH;
		this.height = BOSS_HEIGHT;
		
		this.isDead = false;

		// Override stats with boss values scaled by multiplier
		this.bossMaxHp = (int) (BOSS_MAX_HP * statMultiplier);
		this.bossCurrHp = this.bossMaxHp;
		this.bossAtk = (int) (BOSS_ATK * statMultiplier);
		this.bossSpeed = (int) Math.max(1, BOSS_SPEED * (1.0 + (statMultiplier - 1.0) * 0.2));

		// Custom hitbox: 80% of sprite, centred
		int hbW = (int) (BOSS_WIDTH * 0.80);
		int hbH = (int) (BOSS_HEIGHT * 0.85);
		this.hitBox = new Rectangle2D.Double(x - hbW / 2.0, y - hbH / 2.0, hbW, hbH);
	}

	// ═════════════════════════════════════════════════════════════════
	// UPDATE
	// ═════════════════════════════════════════════════════════════════
	@Override
	public void update() {
		if (bossDead)
			return;

		// ── Spawn fade-in ──
		if (spawnTimer > 0) {
			spawnTimer--;
			return;
		}

		// ── Advance animation tick ──
		animTick += 0.06;
		cloakFlare = (Math.sin(animTick * 1.5) + 1.0) / 2.0; // 0‒1
		orbAngle = (orbAngle + (phase == 2 ? 4 : 2)) % 360;

		// ── Phase check ──
		if (!enrageTriggered && bossCurrHp < bossMaxHp / 2) {
			enrageTriggered = true;
			enrageFlashTimer = ENRAGE_FLASH_DURATION;
			phase = 2;
			bossSpeed = Math.max(bossSpeed + 1, (int) (bossSpeed * 1.4));
		}
		if (enrageFlashTimer > 0)
			enrageFlashTimer--;

		// ── Death sequence ──
		if (bossDying) {
			deathAlpha -= 0.012f;
			if (deathAlpha <= 0) {
				deathAlpha = 0;
				bossDead = true;
				isDead = true;
			}
			return;
		}

		// ── Hitbox sync ──
		int hbW = (int) (BOSS_WIDTH * 0.80);
		int hbH = (int) (BOSS_HEIGHT * 0.85);
		hitBox.setFrame(x - hbW / 2.0, y - hbH / 2.0, hbW, hbH);

		// ── Move toward player ──
		followBossPlayer();

		// ── Attack player on contact ──
		if (Entity.checkCollision(this, gameObj.getPlayer())) {
			gameObj.getPlayer().damage(bossAtk);
		}
	}

	// ═════════════════════════════════════════════════════════════════
	// DRAW — fully custom, no sprite sheet
	// ═════════════════════════════════════════════════════════════════
	@Override
	public void draw(Graphics2D g) {
		if (bossDead)
			return;

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		int cx = (int) x - gameObj.getCameraX(); // screen centre X
		int cy = (int) y - gameObj.getCameraY(); // screen centre Y

		// ── Spawn fade-in alpha ──
		float alpha = 1.0f;
		if (spawnTimer > 0) {
			alpha = 1.0f - ((float) spawnTimer / MAX_SPAWN);
		} else if (bossDying) {
			alpha = deathAlpha;
		}

		// ── Enrage flash overlay ──
		if (enrageFlashTimer > 0) {
			float flashAlpha = (float) enrageFlashTimer / ENRAGE_FLASH_DURATION * 0.5f;
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, flashAlpha));
			g.setColor(Color.RED);
			g.fillRect(cx - BOSS_WIDTH, cy - BOSS_HEIGHT, BOSS_WIDTH * 2, BOSS_HEIGHT * 2);
		}

		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

		// ── Floating bob offset ──
		int bob = (int) (Math.sin(animTick) * 5);

		// ── Shadow ──
		g.setColor(new Color(0, 0, 0, 80));
		g.fillOval(cx - 45, cy + BOSS_HEIGHT / 2 - 8, 90, 18);

		// ── Phase 2: dark aura rings ──
		if (phase == 2) {
			drawAura(g, cx, cy + bob);
		}

		// ── Orbiting skulls / orbs ──
		drawOrbs(g, cx, cy + bob);

		// ── Cloak / body ──
		drawCloak(g, cx, cy + bob);

		// ── Skull head ──
		drawSkullHead(g, cx, cy + bob);

		// ── Crown ──
		drawCrown(g, cx, cy + bob);

		// ── Glowing eyes ──
		drawEyes(g, cx, cy + bob);

		// ── Boss health bar (wide, at top of sprite) ──
		if (!bossDying)
			drawBossHealthBar(g, cx, cy + bob);

		// Reset composite
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
		
//		g.setColor(new Color(255, 0, 0, 150));
//		g.setStroke(new BasicStroke(1.5f));
//		g.draw(new Rectangle2D.Double(
//		    hitBox.getX() - gameObj.getCameraX(),
//		    hitBox.getY() - gameObj.getCameraY(),
//		    hitBox.getWidth(),
//		    hitBox.getHeight()
//		));
	}

	// ─── Drawing helpers ─────────────────────────────────────────────

	private void drawAura(Graphics2D g, int cx, int cy) {
		for (int i = 3; i >= 1; i--) {
			int radius = 55 + i * 12 + (int) (Math.sin(animTick * 2 + i) * 5);
			int auraAlpha = 60 - i * 12;
			g.setColor(new Color(120, 0, 200, auraAlpha));
			g.fillOval(cx - radius, cy - radius / 2, radius * 2, radius);
		}
	}

	private void drawOrbs(Graphics2D g, int cx, int cy) {
		int orbCount = phase == 2 ? 6 : 4;
		int orbitR = 65;
		for (int i = 0; i < orbCount; i++) {
			double angle = Math.toRadians(orbAngle + i * (360.0 / orbCount));
			int ox = cx + (int) (Math.cos(angle) * orbitR);
			int oy = cy + (int) (Math.sin(angle) * orbitR * 0.4); // elliptical orbit

			// Glow
			g.setColor(new Color(180, 100, 255, 60));
			g.fillOval(ox - 10, oy - 10, 20, 20);
			// Core
			g.setColor(new Color(220, 160, 255));
			g.fillOval(ox - 5, oy - 5, 10, 10);
			// Pupil
			g.setColor(new Color(80, 0, 140));
			g.fillOval(ox - 2, oy - 2, 5, 5);
		}
	}

	private void drawCloak(Graphics2D g, int cx, int cy) {
		// Hem flare (animated)
		int hemFlare = (int) (cloakFlare * 8);

		int[] cloakX = { cx - 48 - hemFlare, cx - 30, cx, cx + 30, cx + 48 + hemFlare };
		int[] cloakY = { cy + 72, cy + 80 + hemFlare, cy + 75 + hemFlare / 2, cy + 80 + hemFlare, cy + 72 };

		// Main cloak body
		Color cloakColor = phase == 2 ? new Color(80, 0, 30) : new Color(30, 0, 60);
		g.setColor(cloakColor);
		int[] bodyX = { cx - 45, cx - 20, cx + 20, cx + 45 };
		int[] bodyY = { cy - 10, cy - 35, cy - 35, cy - 10 };
		// Shoulders + body polygon
		int[] fullCloakX = { cx - 48, cx - 20, cx + 20, cx + 48, cx + 48 + hemFlare, cx + 30, cx, cx - 30,
				cx - 48 - hemFlare };
		int[] fullCloakY = { cy - 12, cy - 38, cy - 38, cy - 12, cy + 70, cy + 78 + hemFlare, cy + 74 + hemFlare / 2,
				cy + 78 + hemFlare, cy + 70 };
		g.fillPolygon(fullCloakX, fullCloakY, fullCloakX.length);

		// Cloak highlight seam
		g.setColor(new Color(100, 30, 150, 120));
		g.setStroke(new BasicStroke(2));
		g.drawLine(cx, cy - 35, cx, cy + 60);

		// Collar
		g.setColor(new Color(60, 0, 100));
		g.fillOval(cx - 22, cy - 42, 44, 20);
	}

	private void drawSkullHead(Graphics2D g, int cx, int cy) {
		// Skull base
		g.setColor(new Color(230, 225, 210));
		g.fillOval(cx - 28, cy - 80, 56, 52);

		// Jaw
		g.setColor(new Color(210, 205, 190));
		g.fillRoundRect(cx - 20, cy - 42, 40, 18, 10, 10);

		// Skull shading
		g.setColor(new Color(180, 170, 155, 100));
		g.fillOval(cx + 2, cy - 76, 20, 36);

		// Teeth
		g.setColor(new Color(245, 240, 225));
		int[] teethX = { cx - 16, cx - 8, cx, cx + 8 };
		for (int tx : teethX) {
			g.fillRoundRect(tx, cy - 40, 6, 10, 3, 3);
		}

		// Skull outline
		g.setColor(new Color(80, 70, 60, 180));
		g.setStroke(new BasicStroke(1.5f));
		g.drawOval(cx - 28, cy - 80, 56, 52);
		g.drawRoundRect(cx - 20, cy - 42, 40, 18, 10, 10);
	}

	private void drawCrown(Graphics2D g, int cx, int cy) {
		Color goldBase = new Color(255, 200, 30);
		Color goldDark = new Color(180, 130, 0);
		Color gemColor = phase == 2 ? new Color(255, 60, 60) : new Color(120, 0, 200);

		// Crown band
		g.setColor(goldBase);
		g.fillRect(cx - 26, cy - 92, 52, 14);

		// Crown spikes (5)
		int[] spikeBaseX = { cx - 26, cx - 13, cx, cx + 13, cx + 26 };
		int[] spikeHeights = { 14, 20, 26, 20, 14 };
		for (int i = 0; i < 5; i++) {
			int bx = spikeBaseX[i];
			int sh = spikeHeights[i];
			g.setColor(goldBase);
			int[] sx = { bx - 6, bx, bx + 6 };
			int[] sy = { cy - 92, cy - 92 - sh, cy - 92 };
			g.fillPolygon(sx, sy, 3);
			g.setColor(goldDark);
			g.setStroke(new BasicStroke(1f));
			g.drawPolygon(sx, sy, 3);
		}

		// Crown band outline
		g.setColor(goldDark);
		g.setStroke(new BasicStroke(1.5f));
		g.drawRect(cx - 26, cy - 92, 52, 14);

		// Centre gem
		g.setColor(gemColor);
		g.fillOval(cx - 5, cy - 96, 10, 10);
		g.setColor(gemColor.brighter());
		g.fillOval(cx - 3, cy - 95, 4, 4);
	}

	private void drawEyes(Graphics2D g, int cx, int cy) {
		// Glow
		Color glowColor = phase == 2 ? new Color(255, 80, 0, 100) : new Color(0, 200, 255, 100);
		g.setColor(glowColor);
		g.fillOval(cx - 22, cy - 68, 16, 12);
		g.fillOval(cx + 6, cy - 68, 16, 12);

		// Eye socket dark
		g.setColor(new Color(20, 10, 40));
		g.fillOval(cx - 20, cy - 67, 13, 10);
		g.fillOval(cx + 7, cy - 67, 13, 10);

		// Iris
		Color irisColor = phase == 2 ? new Color(255, 120, 0) : new Color(0, 230, 255);
		g.setColor(irisColor);
		g.fillOval(cx - 17, cy - 65, 8, 7);
		g.fillOval(cx + 9, cy - 65, 8, 7);

		// Pupil
		g.setColor(Color.BLACK);
		g.fillOval(cx - 15, cy - 64, 4, 4);
		g.fillOval(cx + 11, cy - 64, 4, 4);
	}

	private void drawBossHealthBar(Graphics2D g, int cx, int cy) {
		int barW = 180;
		int barH = 10;
		int xPos = cx - barW / 2;
		int yPos = cy - BOSS_HEIGHT / 2 - 24;

		// Background
		g.setColor(new Color(0, 0, 0, 180));
		g.fillRoundRect(xPos - 2, yPos - 2, barW + 4, barH + 4, 6, 6);

		// Empty bar
		g.setColor(new Color(80, 0, 0));
		g.fillRoundRect(xPos, yPos, barW, barH, 4, 4);

		// Filled portion
		double pct = (double) bossCurrHp / bossMaxHp;
		Color fillColor = phase == 2 ? new Color(255, 60, 0) : new Color(200, 30, 30);
		g.setColor(fillColor);
		g.fillRoundRect(xPos, yPos, (int) (barW * pct), barH, 4, 4);

		// Bar border
		g.setColor(new Color(160, 0, 0));
		g.setStroke(new BasicStroke(1f));
		g.drawRoundRect(xPos, yPos, barW, barH, 4, 4);

		// Boss name label
		g.setFont(new Font("SansSerif", Font.BOLD, 11));
		g.setColor(Color.WHITE);
		FontMetrics fm = g.getFontMetrics();
		String label = phase == 2 ? BOSS_NAME + "  ☠ ENRAGED" : BOSS_NAME;
		g.drawString(label, cx - fm.stringWidth(label) / 2, yPos - 4);
	}

	// ═════════════════════════════════════════════════════════════════
	// DAMAGE / DEATH
	// ═════════════════════════════════════════════════════════════════
	@Override
	public void damage(DamageResult result) {
		if (bossDying || spawnTimer > 0)
			return;

		bossCurrHp -= result.damage;
		gameObj.addDamageText(x, y, result.damage, result.isCrit);

		if (bossCurrHp <= 0) {
			bossCurrHp = 0;
			bossDying = true;
			tel.setBossIsDefeated(true);
			gameObj.onBossDeath(); 
			// Reward a big exp burst
			for (int i = 0; i < 20; i++) {
				gameObj.addExp(5, (int) (x + (Math.random() * 60 - 30)), (int) (y + (Math.random() * 60 - 30)));
			}
		}
	}

	@Override
	public boolean isDying() {
		return bossDying;
	}

	// ═════════════════════════════════════════════════════════════════
	// MOVEMENT
	// ═════════════════════════════════════════════════════════════════
	private void followBossPlayer() {
		double dx = gameObj.getPlayer().getX() - x;
		double dy = gameObj.getPlayer().getY() - y;
		double dist = Math.sqrt(dx * dx + dy * dy);
		if (dist > 0) {
			x += (dx / dist) * bossSpeed * 0.5;
			y += (dy / dist) * bossSpeed * 0.5;
		}
	}

	// ═════════════════════════════════════════════════════════════════
	// GETTERS
	// ═════════════════════════════════════════════════════════════════
	public boolean isBossDead() {
		return bossDead;
	}

	public int getPhase() {
		return phase;
	}

	public Rectangle2D getHitBox() {
		return hitBox;
	}
}