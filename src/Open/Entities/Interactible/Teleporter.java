package Open.Entities.Interactible;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import Open.Entities.Enemies.BossEnemy;
import Open.Entities.Enemies.FireDemonBoss;
import Open.Entities.Enemies.VoidLichBoss;
import main.GameObject;
import main.enums.ChestState;

public class Teleporter extends Interactible {

	private boolean bossIsDefeated = false;
	private boolean bossSpawned = false;

	public Teleporter(GameObject gameObj, int x, int y) {
		super(gameObj, x, y);
		width = 60;
		height = 80;
		setHitBox(new Rectangle2D.Double(this.x, this.y, this.width, this.height));
		setBossIsDefeated(false);
	}

	@Override
	public void open() {
		if (bossSpawned) return;

		setState(ChestState.OPEN);
		bossSpawned = true;

		int stage = gameObj.getMap().getStage();
		double diff = gameObj.getWaves().getDifficultyMult();

		switch (stage) {
			case 1 -> gameObj.getEnemies().add(new BossEnemy(gameObj, this, x, y, diff));
			case 2 -> gameObj.getEnemies().add(new FireDemonBoss(gameObj, this, x, y, diff));
			case 3 -> gameObj.getEnemies().add(new VoidLichBoss(gameObj, this, x, y, diff));
			default -> gameObj.getEnemies().add(new BossEnemy(gameObj, this, x, y, diff));
		}

		// ── Trigger boss music ──
		gameObj.onBossSpawn();
	}

	@Override
	public void update() {
		updateInteract();

		if (bossIsDefeated) {
			gameObj.getInteractibles().add(new Portal(gameObj, x, y + 200));
			bossIsDefeated = false;
		}
	}

	@Override
	public void draw(Graphics2D g) {
		int drawX = x - gameObj.getCameraX();
		int drawY = y - gameObj.getCameraY();

		int stage = gameObj.getMap().getStage();

		Color portalOuter, portalInner, glowColor;
		String label;
		switch (stage) {
			case 2 -> {
				portalOuter = new Color(200, 50, 0);
				portalInner = new Color(255, 120, 30);
				glowColor   = new Color(255, 80, 0, 80);
				label       = "BOSS";
			}
			case 3 -> {
				portalOuter = new Color(100, 0, 200);
				portalInner = new Color(200, 80, 255);
				glowColor   = new Color(160, 0, 255, 90);
				label       = "FINAL BOSS";
			}
			default -> {
				portalOuter = new Color(0, 60, 180);
				portalInner = new Color(40, 140, 255);
				glowColor   = new Color(0, 100, 255, 80);
				label       = "BOSS";
			}
		}

		double pulse = (Math.sin(System.currentTimeMillis() * 0.004) + 1.0) / 2.0;
		int glowR = (int) (35 + pulse * 12);

		g.setColor(glowColor);
		g.fillOval(drawX + width / 2 - glowR, drawY + height / 2 - glowR, glowR * 2, glowR * 2);

		g.setColor(portalOuter);
		g.fillOval(drawX, drawY, width, height);
		g.setColor(portalInner);
		g.fillOval(drawX + 10, drawY + 10, width - 20, height - 20);
		g.setColor(new Color(255, 255, 255, 100));
		g.fillOval(drawX + 20, drawY + 15, 16, 14);

		g.setFont(new Font("Monospaced", Font.BOLD, 12));
		g.setColor(Color.WHITE);
		FontMetrics fm = g.getFontMetrics();
		int lx = drawX + (width - fm.stringWidth(label)) / 2;
		int ly = drawY - 8;
		g.setColor(new Color(0, 0, 0, 160));
		g.drawString(label, lx + 1, ly + 1);
		g.setColor(Color.WHITE);
		g.drawString(label, lx, ly);

		if (playerInRange && getState() != ChestState.OPEN) {
			String msg = "[E] OPEN: Spawn " + label;
			g.setFont(new Font("Monospaced", Font.BOLD, 14)); // Monospaced looks more "pixel"
			FontMetrics fm1 = g.getFontMetrics();

			int msgWidth = fm1.stringWidth(msg);
			int msgHeight = fm1.getHeight();
			int padding = 8;

			// Position prompt above the chest
			int promptX = drawX + (width / 2) - (msgWidth / 2);
			int promptY = drawY - 30;

			// 1. Draw Outer Shadow/Border (Black)
			g.setColor(Color.BLACK);
			g.fillRect(promptX - padding - 2, promptY - msgHeight - 2, msgWidth + (padding * 2) + 4, msgHeight + 8);

			// 2. Draw Main Background (Dark Brown to match chest)
			g.setColor(new Color(60, 30, 10));
			g.fillRect(promptX - padding, promptY - msgHeight, msgWidth + (padding * 2), msgHeight + 4);

			// 3. Draw Pixel Highlight (Lighter border on top and left)
			g.setColor(new Color(160, 100, 40));
			g.fillRect(promptX - padding, promptY - msgHeight, msgWidth + (padding * 2), 2); // Top
			g.fillRect(promptX - padding, promptY - msgHeight, 2, msgHeight + 4); // Left

			// 4. Draw Text
			g.setColor(Color.WHITE);
			g.drawString(msg, promptX, promptY);
		}
	}

	public boolean isBossIsDefeated() { return bossIsDefeated; }

	public void setBossIsDefeated(boolean v) { this.bossIsDefeated = v; }
}