package Open.Entities.Enemies;

import java.awt.AlphaComposite;
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
	private int deathHoldTimer = 0;
	private boolean dying = false;

	// Spawn Animation Variables
	private int spawnInTimer = 60;
	private final int MAX_SPAWN_TIME = 60;
	private int damageFlashTimer = 0;
	private final int FLASH_DURATION = 6;
	private int atk;

	public Enemy(GameObject gameObj, int x, int y, int type, double statMultiplier) {
		super(gameObj);
		this.x = x;
		this.y = y;
		this.width = 100;
		this.height = 100;
		
		hitBox = new Rectangle2D.Double(this.x, this.y, this.width, this.height);

		loadEnemy(type);

		// Apply scaling
		this.maxHp = (int) (this.maxHp * statMultiplier);
		this.atk = (int) (this.atk * statMultiplier);
		this.speed = (int) (this.speed * (1.0 + (statMultiplier - 1.0) * 0.2));
		this.currHp = maxHp;
	}

	private void loadEnemy(int num) {
		BufferedImage[] wFrames = null;
		BufferedImage[] dFrames = null;

		switch (num) {
		case 1 -> {
			wFrames = Assets.zombieWalk;
			dFrames = Assets.zombieDeath;
			speed = 3;
			maxHp = 15;
			atk = 10;
		}
		case 2 -> {
			wFrames = Assets.skeletonWalk;
			dFrames = Assets.skeletonDeath;
			speed = 2;
			maxHp = 12;
			atk = 12;
		}
		case 3 -> {
			wFrames = Assets.mudmanWalk;
			dFrames = Assets.mudmanDeath;
			speed = 1;
			maxHp = 40;
			atk = 20;
		}
		case 4 -> {
			wFrames = Assets.batWalk;
			dFrames = Assets.batDeath;
			speed = 4;
			maxHp = 8;
			atk = 5;
		}
		case 5 -> {
			wFrames = Assets.glowingBatWalk;
			dFrames = Assets.glowingBatDeath;
			speed = 5;
			maxHp = 35;
			atk = 25;
		}
		}

		// Initialize Animation objects (100ms per frame)
		this.walkAnim = new Animation(wFrames, 100);
		this.deathAnim = new Animation(dFrames, 120);
	}

	public void update() {
		((RectangularShape) this.hitBox).setFrame(this.x - (width/2), this.y - (height/2), this.width, this.height);
		
		if (spawnInTimer > 0) {
			spawnInTimer--;
			return;
		}

		if (damageFlashTimer > 0)
			damageFlashTimer--;

		if (!isDying()) {
			followPlayer();
			walkAnim.update();

			if (Entity.rectCollision(this, gameObj.getPlayer())) {
				gameObj.getPlayer().damage(atk);
			}
		} else {
			updateDeathAnimation();
		}
	}

	private void updateDeathAnimation() {
		// Play the animation until it hits the final frame
		if (deathAnim.getCurrentFrameIndex() < deathAnim.getFrameLength() - 1) {
			deathAnim.update();
		} else {
			// Once at the end, stop updating the animation and wait to despawn
			deathHoldTimer++;
			if (deathHoldTimer > 40) {
				isDead = true;
			}
		}
	}

	public void draw(Graphics2D g) {
		// Coordinate Math: World Position - Camera Position - Half the sprite size
		int drawX = (int) x - gameObj.getCameraX() - (width/2);
		int drawY = (int) y - gameObj.getCameraY() - (height/2);

		BufferedImage img = isDying() ? deathAnim.getFrame() : walkAnim.getFrame();

		if (img == null)
			return;

		if (spawnInTimer > 0) {
			// Handle Fading in
			float percent = 1.0f - ((float) spawnInTimer / MAX_SPAWN_TIME);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, percent)));
			g.drawImage(img, drawX, drawY, width, height, null);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
			return;
		}

		// Handle Damage Flash vs Normal Draw
		if (damageFlashTimer > 0 && !isDying()) {
			drawFlash(g, img, drawX, drawY, width, height);
		} else {
			g.drawImage(img, drawX, drawY, width, height, null);
		}

		// Health Bar (Only if alive)
		if (!isDying() && currHp < maxHp) {
			drawHealthBar(g, drawX, drawY);
		}
	}

	private void drawFlash(Graphics2D g, BufferedImage img, int x, int y, int w, int h) {
	    // 1. Create a tinted version of the image
	    float[] scales = {1.0f, 0f, 0f, 1.0f}; 
	    float[] offsets = {100f, 0f, 0f, 0f}; // Adds extra red brightness to the pixels
	    RescaleOp tint = new RescaleOp(scales, offsets, null);
	    
	    BufferedImage tintedImg = tint.filter(img, null);
	    
	    // 2. Draw the tinted image
	    g.drawImage(tintedImg, x, y, w, h, null);
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
		g.fillRect(xPos, yPos, (int) (barWidth * ((double) currHp / maxHp)), barHeight);
	}

	public void damage(DamageResult result) {
		if (isDying() || (spawnInTimer > 0))
			return;

		currHp -= result.damage;
		damageFlashTimer = FLASH_DURATION;

		// SPAWN DAMAGE NUMBER
		gameObj.addDamageText(x, y, result.damage, result.isCrit);

		if (currHp <= 0)
			die();
	}

	private void die() {
		setDying(true);
		deathAnim.setFrame(0); // Ensure death starts at first frame
		gameObj.addExp(1, x, y);
	}

	private void followPlayer() {
		double dx = gameObj.getPlayer().getX() - x;
		double dy = gameObj.getPlayer().getY() - y;
		double dist = Math.sqrt(dx * dx + dy * dy);

		if (dist > 0) {
			x += (dx / dist) * speed * 0.5;
			y += (dy / dist) * speed * 0.5;
		}
	}

	public boolean isDying() {
		return dying;
	}

	public void setDying(boolean dying) {
		this.dying = dying;
	}
}