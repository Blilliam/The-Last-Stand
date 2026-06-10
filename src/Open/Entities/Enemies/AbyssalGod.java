package Open.Entities.Enemies;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import Open.Entities.Entity;
import Open.Entities.Interactible.Teleporter;
import main.DamageResult;
import main.GameObject;

/**
 * AbyssalGod — True Final Boss
 *
 * ANTI-OP MECHANICS: 1. REFLECT SHIELD — periodic shield that sends damage
 * back; spam-fire = suicide 2. IMMUNITY PHASE — must destroy "soul cores"
 * around the boss to re-enable damage 3. DEATH ZONES — spreading void pools
 * that linger; punishes standing still 4. FORCED TELEPORT — if player stays
 * >700px away for 3s, boss teleports on top of them 5. HP DRAIN AURA — passive
 * %HP drain per second at close range 6. DEATH NOVA — explodes void shards on
 * death; don't burst it from range
 *
 * Phases (by boss HP%): Phase 1 (100-70%): Slow, telegraphed attacks. Shield
 * every 8s. Phase 2 (70-40%): Faster, soul cores appear, void pools start.
 * Shield every 5s. Phase 3 (<40%): Enrage — constant pressure, rapid teleport,
 * HP drain, death nova on kill.
 */
public class AbyssalGod extends Enemy {

	private static final String BOSS_NAME = "The Abyssal God";
	private static final int BOSS_MAX_HP = 8000;
	private static final int BOSS_ATK = 60;
	private static final int BOSS_SPEED = 3;
	private static final int BOSS_W = 300;
	private static final int BOSS_H = 300;

	private final GameObject gameObj;
	private final Teleporter teleporter;

	// ── Internal state ────────────────────────────────────────────────────
	private int bossMaxHp, bossCurrHp, bossAtk, bossSpeed;
	private boolean bossDying = false, bossDead = false;
	private float deathAlpha = 1f;
	private int spawnTimer = 120;

	private int phase = 1;
	private boolean p2Triggered = false, p3Triggered = false;
	private int phaseFlashTimer = 0;

	private double animTick = 0;

	// ── Hitbox ────────────────────────────────────────────────────────────
	private Rectangle2D.Double hitBox;

	// ── MECHANIC 1: Reflect Shield ────────────────────────────────────────
	private boolean shieldActive = false;
	private int shieldTimer = 0; // counts up to shieldInterval before activating
	private int shieldDuration = 0; // how long shield stays up
	private static final int SHIELD_INTERVAL_P1 = 480; // 8s
	private static final int SHIELD_INTERVAL_P2 = 300; // 5s
	private static final int SHIELD_INTERVAL_P3 = 180; // 3s
	private static final int SHIELD_HOLD = 120; // 2s active
	private float shieldPulse = 0;

	// ── MECHANIC 2: Soul Cores (immunity phase) ───────────────────────────
	private List<SoulCore> soulCores = new ArrayList<>();
	private boolean immunityActive = false; // immune until all cores destroyed
	private int coreSpawnCooldown = 0;
	private static final int CORE_SPAWN_INTERVAL = 600; // 10s between core waves

	// ── MECHANIC 3: Void Pools ────────────────────────────────────────────
	private List<VoidPool> voidPools = new ArrayList<>();
	private int poolSpawnCooldown = 0;
	private static final int POOL_SPAWN_INTERVAL_P2 = 300;
	private static final int POOL_SPAWN_INTERVAL_P3 = 150;

	// ── MECHANIC 4: Forced Teleport ───────────────────────────────────────
	private int farAwayTimer = 0; // counts up if player is >700px away
	private static final int TELEPORT_THRESHOLD = 180; // 3s at 60fps

	// ── MECHANIC 5: HP Drain Aura ─────────────────────────────────────────
	private int drainTick = 0;

	// ── Projectiles ───────────────────────────────────────────────────────
	private List<AbyssalShard> shards = new ArrayList<>();
	private int shootCooldown = 0;

	// ─────────────────────────────────────────────────────────────────────
	// INNER CLASSES
	// ─────────────────────────────────────────────────────────────────────

	/** Orbiting destructible core. Boss is immune while any core lives. */
	private class SoulCore {
		double angle;
		double orbitRadius;
		double hp = 80;
		double maxHp = 80;
		boolean dead = false;
		int flashTimer = 0;

		// Hitbox for player weapon collision
		Ellipse2D.Double hitbox;

		SoulCore(double angle, double radius) {
			this.angle = angle;
			this.orbitRadius = radius;
			hitbox = new Ellipse2D.Double(0, 0, 40, 40);
		}

		void update(double bossX, double bossY) {
			if (phase == 3) {
				angle += 0.04;
			} else {
				angle += 0.025;
			}
			double cx = bossX + Math.cos(angle) * orbitRadius;
			double cy = bossY + Math.sin(angle) * orbitRadius * 0.5;
			hitbox.setFrame(cx - 20, cy - 20, 40, 40);
			if (flashTimer > 0)
				flashTimer--;

			// Check if player weapon projectiles hit this core
			for (var proj : gameObj.getProjectiles()) {
				if (!proj.isDead() && Entity.checkCollision(proj, new Entity(gameObj) {
					{
						setHitBox(hitbox);
						this.x = (int) cx;
						this.y = (int) cy;
						width = 40;
						height = 40;
					}

					public void update() {
					}

					public void draw(Graphics2D g) {
					}
				})) {
					hp -= 5;
					flashTimer = 6;
					if (hp <= 0)
						dead = true;
				}
			}
		}

		void draw(Graphics2D g, double bossX, double bossY, int camX, int camY) {
			double cx = bossX + Math.cos(angle) * orbitRadius;
			double cy = bossY + Math.sin(angle) * orbitRadius * 0.5;
			int sx = (int) cx - camX, sy = (int) cy - camY;

			// Glow
			Color glow;
			if (flashTimer > 0) {
				glow = new Color(255, 100, 0, 100);
			} else {
				glow = new Color(80, 255, 200, 80);
			}
			g.setColor(glow);
			g.fillOval(sx - 28, sy - 28, 56, 56);

			// Core body
			Color core;
			if (flashTimer > 0) {
				core = new Color(255, 150, 0);
			} else {
				core = new Color(80, 255, 200);
			}
			g.setColor(core);
			g.fillOval(sx - 16, sy - 16, 32, 32);

			// HP bar
			float pct = (float) (hp / maxHp);
			g.setColor(new Color(0, 0, 0, 160));
			g.fillRect(sx - 20, sy - 26, 40, 6);
			if (pct > 0.5f) {
				g.setColor(new Color(80, 255, 80));
			} else {
				g.setColor(new Color(255, 80, 80));
			}
			g.fillRect(sx - 20, sy - 26, (int) (40 * pct), 6);
		}
	}

	/** Lingering void pool on the ground. Damages player who stands in it. */
	private static class VoidPool {
		double x, y;
		int radius;
		int life;
		int maxLife;
		int damageTick = 0;

		VoidPool(double x, double y, int radius, int life) {
			this.x = x;
			this.y = y;
			this.radius = radius;
			this.life = this.maxLife = life;
		}

		boolean contains(double px, double py) {
			double dx = px - x, dy = py - y;
			return dx * dx + dy * dy < (double) radius * radius;
		}

		void draw(Graphics2D g, int camX, int camY) {
			int sx = (int) x - camX, sy = (int) y - camY;
			float alpha = Math.min(1f, (float) life / maxLife) * 0.5f;
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
			g.setColor(new Color(60, 0, 120));
			g.fillOval(sx - radius, sy - radius, radius * 2, radius * 2);
			g.setColor(new Color(140, 0, 255, 180));
			g.setStroke(new BasicStroke(3f));
			g.drawOval(sx - radius, sy - radius, radius * 2, radius * 2);
			g.setStroke(new BasicStroke(1f));
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
		}
	}

	/** Fast-moving shard projectile. */
	private static class AbyssalShard {
		double x, y, vx, vy;
		int life = 350;
		float size;
		boolean homing;
		double homingTarget; // unused; we pass gameObj

		AbyssalShard(double x, double y, double vx, double vy, float size, boolean homing) {
			this.x = x;
			this.y = y;
			this.vx = vx;
			this.vy = vy;
			this.size = size;
			this.homing = homing;
		}

		void update(double px, double py) {
			if (homing) {
				double dx = px - x, dy = py - y;
				double len = Math.sqrt(dx * dx + dy * dy);
				if (len > 0) {
					vx += (dx / len) * 0.15;
					vy += (dy / len) * 0.15;
				}
				double spd = Math.sqrt(vx * vx + vy * vy);
				if (spd > 6) {
					vx = vx / spd * 6;
					vy = vy / spd * 6;
				}
			}
			x += vx;
			y += vy;
			life--;
		}

		boolean isDead() {
			return life <= 0;
		}
	}

	// ─────────────────────────────────────────────────────────────────────
	// CONSTRUCTOR
	// ─────────────────────────────────────────────────────────────────────

	public AbyssalGod(GameObject gameObj, Teleporter teleporter, int x, int y, double statMult) {
		super(gameObj, x, y, 1, 1.0);
		this.gameObj = gameObj;
		this.teleporter = teleporter;

		this.x = x;
		this.y = y;
		this.width = BOSS_W;
		this.height = BOSS_H;
		this.isDead = false;

		bossMaxHp = (int) (BOSS_MAX_HP * statMult);
		bossCurrHp = bossMaxHp;
		bossAtk = (int) (BOSS_ATK * statMult);
		bossSpeed = (int) Math.max(1, BOSS_SPEED * (1 + (statMult - 1) * 0.2));

		int hbW = (int) (BOSS_W * 0.6), hbH = (int) (BOSS_H * 0.65);
		hitBox = new Rectangle2D.Double(x - hbW / 2.0, y - hbH / 2.0, hbW, hbH);
	}

	// ─────────────────────────────────────────────────────────────────────
	// UPDATE
	// ─────────────────────────────────────────────────────────────────────

	@Override
	public void update() {
		if (bossDead)
			return;
		if (spawnTimer > 0) {
			spawnTimer--;
			return;
		}

		animTick += 0.06;

		checkPhaseTransitions();
		if (phaseFlashTimer > 0)
			phaseFlashTimer--;

		if (bossDying) {
			deathAlpha -= 0.008f;
			if (deathAlpha <= 0) {
				deathAlpha = 0;
				bossDead = true;
				isDead = true;
				if (teleporter != null)
					teleporter.setBossIsDefeated(true);
				// MECHANIC 6: Death nova
				spawnDeathNova();
			}
			updateShards();
			updateVoidPools();
			return;
		}

		syncHitbox();
		// updateShield();
		updateCores();
		updateVoidPools();
		updateForcedTeleport();
		updateDrainAura();
		updateShooting();
		updateShards();
		moveTowardPlayer();
		checkMeleeContact();
	}

	private void checkPhaseTransitions() {
		if (!p2Triggered && bossCurrHp < bossMaxHp * 0.70) {
			p2Triggered = true;
			phase = 2;
			phaseFlashTimer = 90;
			bossSpeed = Math.max(bossSpeed + 1, (int) (bossSpeed * 1.2));
			spawnCoreWave();
		}
		if (!p3Triggered && bossCurrHp < bossMaxHp * 0.40) {
			p3Triggered = true;
			phase = 3;
			phaseFlashTimer = 120;
			bossSpeed = Math.max(bossSpeed + 1, (int) (bossSpeed * 1.4));
			soulCores.clear();
			immunityActive = false; // clear cores, then re-trigger
			spawnCoreWave();
		}
	}

	// ── Shield ────────────────────────────────────────────────────────────

	private void updateShield() {
		int interval;
		if (phase == 3) {
			interval = SHIELD_INTERVAL_P3;
		} else if (phase == 2) {
			interval = SHIELD_INTERVAL_P2;
		} else {
			interval = SHIELD_INTERVAL_P1;
		}
		if (!shieldActive) {
			shieldTimer++;
			if (shieldTimer >= interval) {
				shieldActive = true;
				shieldDuration = SHIELD_HOLD;
				shieldTimer = 0;
			}
		} else {
			shieldPulse = (float) ((Math.sin(animTick * 8) + 1) / 2);
			shieldDuration--;
			if (shieldDuration <= 0)
				shieldActive = false;
		}
	}

	// ── Soul Cores ────────────────────────────────────────────────────────

	private void updateCores() {
		if (phase < 2)
			return;

		coreSpawnCooldown++;
		if (coreSpawnCooldown >= CORE_SPAWN_INTERVAL && soulCores.isEmpty()) {
			coreSpawnCooldown = 0;
			spawnCoreWave();
		}

		Iterator<SoulCore> it = soulCores.iterator();
		while (it.hasNext()) {
			SoulCore c = it.next();
			c.update(x, y);
			if (c.dead)
				it.remove();
		}

		immunityActive = !soulCores.isEmpty();
	}

	private void spawnCoreWave() {
		int count;
		if (phase == 3) {
			count = 5;
		} else {
			count = 3;
		}
		double spacing = (2 * Math.PI) / count;
		int radius;
		if (phase == 3) {
			radius = 160;
		} else {
			radius = 130;
		}
		for (int i = 0; i < count; i++) {
			soulCores.add(new SoulCore(i * spacing, radius));
		}
		immunityActive = true;
	}

	// ── Void Pools ───────────────────────────────────────────────────────

	private void updateVoidPools() {
		if (phase < 2)
			return;
		int interval;
		if (phase == 3) {
			interval = POOL_SPAWN_INTERVAL_P3;
		} else {
			interval = POOL_SPAWN_INTERVAL_P2;
		}
		poolSpawnCooldown++;
		if (poolSpawnCooldown >= interval) {
			poolSpawnCooldown = 0;
			// Spawn under player's current position (forces movement)
			int vpRadius;
			int vpLife;
			if (phase == 3) {
				vpRadius = 120;
				vpLife = 480;
			} else {
				vpRadius = 90;
				vpLife = 360;
			}
			voidPools.add(new VoidPool(gameObj.getPlayer().getX(), gameObj.getPlayer().getY(), vpRadius, vpLife));
			// Phase 3: also spawn near boss
			if (phase == 3) {
				voidPools.add(new VoidPool(x + (Math.random() - 0.5) * 200, y + (Math.random() - 0.5) * 200, 80, 300));
			}
		}

		Iterator<VoidPool> it = voidPools.iterator();
		while (it.hasNext()) {
			VoidPool vp = it.next();
			vp.life--;
			if (vp.life <= 0) {
				it.remove();
				continue;
			}

			// Damage player standing in pool
			if (vp.contains(gameObj.getPlayer().getX(), gameObj.getPlayer().getY())) {
				vp.damageTick++;
				if (vp.damageTick >= 30) { // every 0.5s
					gameObj.getPlayer().damage((int) (bossAtk * 0.3));
					vp.damageTick = 0;
				}
			}
		}
	}

	// ── Forced Teleport ───────────────────────────────────────────────────

	private void updateForcedTeleport() {
		if (phase < 2)
			return;
		double dx = gameObj.getPlayer().getX() - x;
		double dy = gameObj.getPlayer().getY() - y;
		double dist = Math.sqrt(dx * dx + dy * dy);

		if (dist > 700) {
			farAwayTimer++;
			if (farAwayTimer >= TELEPORT_THRESHOLD) {
				farAwayTimer = 0;
				// Teleport right next to player
				double angle = Math.random() * Math.PI * 2;
				x = (int) (gameObj.getPlayer().getX() + Math.cos(angle) * 150);
				y = (int) (gameObj.getPlayer().getY() + Math.sin(angle) * 150);
				// Flash
				phaseFlashTimer = 20;
				// Immediately shoot a volley
				fireVolley();
			}
		} else {
			farAwayTimer = 0;
		}
	}

	// ── HP Drain Aura (Phase 3) ───────────────────────────────────────────

	private void updateDrainAura() {
		if (phase < 3)
			return;
		double dx = gameObj.getPlayer().getX() - x;
		double dy = gameObj.getPlayer().getY() - y;
		double dist = Math.sqrt(dx * dx + dy * dy);

		if (dist < 250) { // close range drain
			drainTick++;
			if (drainTick >= 60) {
				drainTick = 0;
				// Drain 2% max HP per second
				int drain = (int) (gameObj.getPlayer().getMaxHp() * 0.02);
				gameObj.getPlayer().damage(Math.max(1, drain));
			}
		}
	}

	// ── Shooting ─────────────────────────────────────────────────────────

	private void updateShooting() {
		int rate;
		if (phase == 3) {
			rate = 55;
		} else if (phase == 2) {
			rate = 75;
		} else {
			rate = 100;
		}
		shootCooldown++;
		if (shootCooldown >= rate) {
			shootCooldown = 0;
			fireVolley();
		}
	}

	private void fireVolley() {
		int count;
		if (phase == 3) {
			count = 20;
		} else if (phase == 2) {
			count = 14;
		} else {
			count = 10;
		}
		double speed;
		if (phase == 3) {
			speed = 5.5;
		} else if (phase == 2) {
			speed = 4.5;
		} else {
			speed = 3.5;
		}
		float size;
		if (phase == 3) {
			size = 14f;
		} else {
			size = 11f;
		}
		boolean addHoming = phase >= 2;

		double aimAngle = Math.atan2(gameObj.getPlayer().getY() - y, gameObj.getPlayer().getX() - x);

		speed = (int) (speed * (1.0 + (gameObj.getWaves().getDifficultyMult() - 1.0) * 0.2));

		for (int i = 0; i < count; i++) {
			double angle = aimAngle + (2 * Math.PI / count) * i;
			shards.add(new AbyssalShard(x, y, Math.cos(angle) * speed, Math.sin(angle) * speed, size, false));
		}

		// Phase 2+: also fire 3 homing shards
		if (addHoming) {
			for (int i = 0; i < 3; i++) {
				double a = aimAngle + (Math.random() - 0.5) * 0.6;
				shards.add(new AbyssalShard(x, y, Math.cos(a) * 2.5, Math.sin(a) * 2.5, 10f, true));
			}
		}
	}

	private void updateShards() {
		double px = gameObj.getPlayer().getX(), py = gameObj.getPlayer().getY();
		Iterator<AbyssalShard> it = shards.iterator();
		while (it.hasNext()) {
			AbyssalShard s = it.next();
			s.update(px, py);
			if (s.isDead()) {
				it.remove();
				continue;
			}

			double dx = s.x - px, dy = s.y - py;
			double dist = Math.sqrt(dx * dx + dy * dy);
			if (dist < s.size + gameObj.getPlayer().getWidth() / 2.0) {
				gameObj.getPlayer().damage((int) (bossAtk * 0.4));
				it.remove();
			}
		}
	}

	private void spawnDeathNova() {
		int count = 36;
		double speed = 7;
		for (int i = 0; i < count; i++) {
			double angle = (2 * Math.PI / count) * i;
			shards.add(new AbyssalShard(x, y, Math.cos(angle) * speed, Math.sin(angle) * speed, 16f, false));
		}
		// Also spawn lingering pools in a ring
		for (int i = 0; i < 8; i++) {
			double angle = (2 * Math.PI / 8) * i;
			voidPools.add(new VoidPool(x + Math.cos(angle) * 200, y + Math.sin(angle) * 200, 100, 600));
		}
	}

	// ── Movement ─────────────────────────────────────────────────────────

	private void moveTowardPlayer() {
		double dx = gameObj.getPlayer().getX() - x;
		double dy = gameObj.getPlayer().getY() - y;
		double dist = Math.sqrt(dx * dx + dy * dy);
		double speedMult;
		if (phase == 3) {
			speedMult = 0.8;
		} else {
			speedMult = 0.5;
		}
		if (dist > 80 && dist > 0) {
			x += (dx / dist) * bossSpeed * speedMult;
			y += (dy / dist) * bossSpeed * speedMult;
		}
	}

	private void checkMeleeContact() {
		if (Entity.checkCollision(this, gameObj.getPlayer())) {
			gameObj.getPlayer().damage(bossAtk);
		}
	}

	private void syncHitbox() {
		int hbW = (int) (BOSS_W * 0.6), hbH = (int) (BOSS_H * 0.65);
		hitBox.setFrame(x - hbW / 2.0, y - hbH / 2.0, hbW, hbH);
	}

	// ─────────────────────────────────────────────────────────────────────
	// DRAW
	// ─────────────────────────────────────────────────────────────────────

	@Override
	public void draw(Graphics2D g) {
		if (bossDead)
			return;

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		int cx = x - gameObj.getCameraX();
		int cy = y - gameObj.getCameraY();

		float alpha;
		if (spawnTimer > 0) {
			alpha = 1f - ((float) spawnTimer / 120);
		} else {
			alpha = deathAlpha;
		}

		// Phase flash
		if (phaseFlashTimer > 0) {
			float fa = (float) phaseFlashTimer / 120 * 0.5f;
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(fa, 0.5f)));
			g.setColor(new Color(180, 0, 255));
			g.fillRect(cx - BOSS_W, cy - BOSS_H, BOSS_W * 2, BOSS_H * 2);
		}

		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, alpha)));

		int bob = (int) (Math.sin(animTick * 0.7) * 9);

		// Draw void pools first (ground layer)
		for (VoidPool vp : voidPools)
			vp.draw(g, gameObj.getCameraX(), gameObj.getCameraY());

		// Immunity ring (visible indicator)
		if (immunityActive) {
			double pulse = (Math.sin(animTick * 4) + 1) / 2;
			int r = (int) (100 + pulse * 20);
			g.setColor(new Color(80, 255, 200, 60));
			g.fillOval(cx - r, cy - r / 2, r * 2, r);
			g.setStroke(new BasicStroke(4f));
			g.setColor(new Color(80, 255, 200, 200));
			g.drawOval(cx - r, cy - r / 2, r * 2, r);
			g.setStroke(new BasicStroke(1f));
		}

		drawVoidAura(g, cx, cy + bob);
		drawBody(g, cx, cy + bob);
		drawFace(g, cx, cy + bob);
		drawCrown(g, cx, cy + bob);

		// Reflect shield overlay
		if (shieldActive) {
			int sr = 100 + (int) (shieldPulse * 15);
			g.setColor(new Color(255, 50, 50, (int) (80 + shieldPulse * 80)));
			g.fillOval(cx - sr, cy - sr, sr * 2, sr * 2);
			g.setStroke(new BasicStroke(5f));
			g.setColor(new Color(255, 100, 0));
			g.drawOval(cx - sr, cy - sr, sr * 2, sr * 2);
			g.setStroke(new BasicStroke(1f));
			// Warning text
			g.setFont(new Font("Monospaced", Font.BOLD, 14));
			g.setColor(new Color(255, 200, 0));
			String warn = "⚠ REFLECT ⚠";
			FontMetrics fm = g.getFontMetrics();
			g.drawString(warn, cx - fm.stringWidth(warn) / 2, cy - BOSS_H / 2 - 60);
		}

		// Immunity warning text
		if (immunityActive) {
			g.setFont(new Font("Monospaced", Font.BOLD, 13));
			g.setColor(new Color(80, 255, 200));
			String warn = "DESTROY SOUL CORES";
			FontMetrics fm = g.getFontMetrics();
			g.drawString(warn, cx - fm.stringWidth(warn) / 2, cy - BOSS_H / 2 - 45);
		}

		// Drain warning
		if (phase == 3) {
			double dx = gameObj.getPlayer().getX() - x;
			double dy2 = gameObj.getPlayer().getY() - y;
			double dist = Math.sqrt(dx * dx + dy2 * dy2);
			if (dist < 250) {
				g.setFont(new Font("Monospaced", Font.BOLD, 13));
				g.setColor(new Color(255, 60, 60, 200));
				String drain = "▼ HP DRAIN";
				FontMetrics fm = g.getFontMetrics();
				g.drawString(drain, cx - fm.stringWidth(drain) / 2, cy + BOSS_H / 2 + 25);
			}
		}

		// Soul cores
		for (SoulCore c : soulCores)
			c.draw(g, x, y, gameObj.getCameraX(), gameObj.getCameraY());

		// Shards
		drawShards(g);

		if (!bossDying)
			drawHPBar(g, cx, cy + bob);

		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
	}

	private void drawVoidAura(Graphics2D g, int cx, int cy) {
		int rings;
		if (phase == 3) {
			rings = 7;
		} else if (phase == 2) {
			rings = 5;
		} else {
			rings = 4;
		}
		for (int i = rings; i >= 1; i--) {
			int r = 70 + i * 22 + (int) (Math.sin(animTick * 2 + i) * 10);
			int a = 65 - i * 8;
			int ri;
			if (phase == 3) {
				ri = 200;
			} else if (phase == 2) {
				ri = 140;
			} else {
				ri = 80;
			}
			int bi = 255;
			g.setColor(new Color(ri, 0, bi, Math.max(0, a)));
			g.fillOval(cx - r, cy - r / 2, r * 2, r);
		}
	}

	private void drawBody(Graphics2D g, int cx, int cy) {
		// Dark robe
		Color bodyColor;
		if (phase == 3) {
			bodyColor = new Color(80, 0, 160);
		} else if (phase == 2) {
			bodyColor = new Color(50, 0, 120);
		} else {
			bodyColor = new Color(30, 0, 80);
		}
		int[] bx = { cx - 60, cx - 75, cx - 50, cx + 50, cx + 75, cx + 60 };
		int[] by = { cy - 20, cy + 60, cy + 100, cy + 100, cy + 60, cy - 20 };
		g.setColor(bodyColor);
		g.fillPolygon(bx, by, 6);

		// Hem tendrils
		double pulse = (Math.sin(animTick * 2) + 1) / 2;
		int hemFlare = (int) (pulse * 12);
		for (int i = -2; i <= 2; i++) {
			int tx = cx + i * 22;
			g.setColor(new Color(120, 0, 200, 160));
			g.fillPolygon(new int[] { tx - 6, tx, tx + 6 },
					new int[] { cy + 96, cy + 110 + hemFlare + (i % 2) * 8, cy + 96 }, 3);
		}

		// Collar
		g.setColor(new Color(100, 0, 200, 200));
		g.fillOval(cx - 32, cy - 34, 64, 26);
	}

	private void drawFace(Graphics2D g, int cx, int cy) {
		// Skull
		g.setColor(new Color(200, 180, 230));
		g.fillOval(cx - 38, cy - 95, 76, 68);
		g.setColor(new Color(160, 140, 180));
		g.fillRoundRect(cx - 28, cy - 48, 56, 22, 10, 10);

		// Teeth
		g.setColor(new Color(230, 220, 245));
		int[] tx = { cx - 22, cx - 11, cx, cx + 11 };
		for (int t : tx)
			g.fillPolygon(new int[] { t, t + 5, t + 10 }, new int[] { cy - 46, cy - 33, cy - 46 }, 3);

		// Eyes — glow based on phase
		Color eyeGlow;
		if (phase == 3) {
			eyeGlow = new Color(255, 50, 255, 160);
		} else if (phase == 2) {
			eyeGlow = new Color(200, 0, 255, 140);
		} else {
			eyeGlow = new Color(140, 0, 255, 120);
		}
		g.setColor(eyeGlow);
		g.fillOval(cx - 30, cy - 82, 20, 14);
		g.fillOval(cx + 9, cy - 82, 20, 14);

		g.setColor(new Color(20, 0, 40));
		g.fillOval(cx - 28, cy - 80, 16, 11);
		g.fillOval(cx + 11, cy - 80, 16, 11);

		Color iris;
		if (phase == 3) {
			iris = new Color(255, 100, 255);
		} else {
			iris = new Color(180, 80, 255);
		}
		g.setColor(iris);
		g.fillOval(cx - 26, cy - 78, 10, 8);
		g.fillOval(cx + 14, cy - 78, 10, 8);
	}

	private void drawCrown(Graphics2D g, int cx, int cy) {
		Color gold = new Color(200, 80, 255);
		g.setColor(gold);
		g.fillRect(cx - 34, cy - 108, 68, 16);
		int[] bases = { cx - 34, cx - 17, cx, cx + 17, cx + 34 };
		int[] hgts = { 16, 24, 32, 24, 16 };
		for (int i = 0; i < 5; i++) {
			g.setColor(gold);
			g.fillPolygon(new int[] { bases[i] - 8, bases[i], bases[i] + 8 },
					new int[] { cy - 108, cy - 108 - hgts[i], cy - 108 }, 3);
		}
		// Gem pulses with phase
		Color gem;
		if (phase == 3) {
			gem = new Color(255, 100, 255);
		} else {
			gem = new Color(180, 100, 255);
		}
		g.setColor(gem);
		g.fillOval(cx - 7, cy - 116, 14, 14);
	}

	private void drawShards(Graphics2D g) {
		for (int i = 0; i < shards.size(); i++) {
			AbyssalShard s = shards.get(i);
			int sx = (int) s.x - gameObj.getCameraX();
			int sy = (int) s.y - gameObj.getCameraY();
			int sz = (int) s.size;
			Color glow;
			if (s.homing) {
				glow = new Color(255, 100, 0, 90);
			} else {
				glow = new Color(180, 0, 255, 80);
			}
			Color core;
			if (s.homing) {
				core = new Color(255, 180, 0);
			} else {
				core = new Color(220, 120, 255);
			}
			g.setColor(glow);
			g.fillOval(sx - sz - 4, sy - sz - 4, (sz + 4) * 2, (sz + 4) * 2);
			g.setColor(core);
			g.fillOval(sx - sz, sy - sz, sz * 2, sz * 2);
			g.setColor(new Color(255, 240, 255));
			g.fillOval(sx - sz / 2, sy - sz / 2, sz, sz);
		}
	}

	private void drawHPBar(Graphics2D g, int cx, int cy) {
		int bw = 260, bh = 14;
		int bx = cx - bw / 2, by = cy - BOSS_H / 2 - 36;

		g.setColor(new Color(0, 0, 0, 200));
		g.fillRoundRect(bx - 2, by - 2, bw + 4, bh + 4, 6, 6);
		g.setColor(new Color(40, 0, 80));
		g.fillRoundRect(bx, by, bw, bh, 4, 4);

		double pct = (double) bossCurrHp / bossMaxHp;
		Color fill;
		if (phase == 3) {
			fill = new Color(255, 50, 255);
		} else if (phase == 2) {
			fill = new Color(200, 0, 255);
		} else {
			fill = new Color(140, 0, 255);
		}
		g.setColor(fill);
		g.fillRoundRect(bx, by, (int) (bw * pct), bh, 4, 4);

		// Shield indicator on HP bar
		if (shieldActive) {
			g.setColor(new Color(255, 100, 0, 180));
			g.fillRoundRect(bx, by, (int) (bw * pct), bh, 4, 4);
		}
		if (immunityActive) {
			g.setColor(new Color(80, 255, 200, 120));
			g.fillRoundRect(bx, by, (int) (bw * pct), bh, 4, 4);
		}

		g.setColor(new Color(160, 60, 220));
		g.setStroke(new BasicStroke(1f));
		g.drawRoundRect(bx, by, bw, bh, 4, 4);

		String phaseLabel;
		if (phase == 3) {
			phaseLabel = " ✦ VOID COLLAPSE";
		} else if (phase == 2) {
			phaseLabel = " ✦ ASCENDING";
		} else {
			phaseLabel = "";
		}
		g.setFont(new Font("SansSerif", Font.BOLD, 12));
		g.setColor(new Color(220, 180, 255));
		FontMetrics fm = g.getFontMetrics();
		String label = BOSS_NAME + phaseLabel;
		g.drawString(label, cx - fm.stringWidth(label) / 2, by - 5);
	}

	// ─────────────────────────────────────────────────────────────────────
	// DAMAGE
	// ─────────────────────────────────────────────────────────────────────

	@Override
	public void damage(DamageResult result) {
		if (bossDying || spawnTimer > 0)
			return;

		// MECHANIC 1: Reflect shield — damage is reflected back
		if (shieldActive) {
			double reflected = result.damage * 0.8;
			gameObj.getPlayer().damage((int) reflected);
			gameObj.addDamageText(x, y, 0, false); // show 0 on boss
			return; // no damage to boss
		}

		// MECHANIC 2: Immunity — no HP damage while cores alive
		if (immunityActive) {
			gameObj.addDamageText(x, y, 0, false);
			return;
		}

		bossCurrHp -= (int) result.damage;
		gameObj.addDamageText(x, y, result.damage, result.isCrit);

		if (bossCurrHp <= 0) {
			bossCurrHp = 0;
			bossDying = true;
			gameObj.onBossDeath();
			// Exp burst
			for (int i = 0; i < 50; i++) {
				gameObj.addExp(10, (int) (x + (Math.random() * 120 - 60)), (int) (y + (Math.random() * 120 - 60)));
			}
		}
	}

	@Override
	public boolean isDying() {
		return bossDying;
	}

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