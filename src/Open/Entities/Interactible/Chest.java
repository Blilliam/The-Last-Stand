package Open.Entities.Interactible;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Random;

import Open.Artifacts.WorldItem;
import Open.Artifacts.Common.ChunkyOats;
import main.GameObject;
import main.enums.ChestState;

public class Chest extends Interactible {

	private int shakeTimer = 0;
	private int openProgress = 0;
	private int cost;
	private boolean spawnedItem;

	private Random rand = new Random();

	public Chest(GameObject gameObj, int x, int y) {
		super(gameObj, x, y);
		width = 80;
		height = 60;
		cost = 25 * gameObj.getMap().getStage();
		spawnedItem = false;
		
		setHitBox(new Rectangle2D.Double(this.x, this.y, this.width, this.height));
	}

	public void update() {
	    updateInteract();

	    switch (getState()) {
	        case SHAKING:
	            shakeTimer++;
	            if (shakeTimer > 60) {
	                setState(ChestState.OPENING);
	            }
	            break;

	        case OPENING:
	            openProgress++;
	            if (openProgress > 30) {
	                setState(ChestState.OPEN);
	            }
	            break;

	        case OPEN:
	            // MOVE SPAWNING LOGIC HERE
	            if (!spawnedItem) {
	                gameObj.getGroundItems().add(new WorldItem(gameObj, gameObj.getPlayer().getArtifactManager().getRandomArtifact(), x + width/2, y));
	                spawnedItem = true;
	            }
	            break;

	        default:
	            break;
	    }
	}

	public void draw(Graphics2D g) {
		int drawX = x - gameObj.getCameraX();
		int drawY = y - gameObj.getCameraY() + height/2;

		// --- PIXEL ART INTERACT PROMPT ---
		if (playerInRange && getState() == ChestState.CLOSED) {
			String msg = "[E] OPEN: " + cost + "$";
			g.setFont(new Font("Monospaced", Font.BOLD, 14)); // Monospaced looks more "pixel"
			FontMetrics fm = g.getFontMetrics();

			int msgWidth = fm.stringWidth(msg);
			int msgHeight = fm.getHeight();
			int padding = 8;

			// Position prompt above the chest
			int promptX = drawX + (width / 2) - (msgWidth / 2);
			int promptY = drawY - 60;

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

		// --- CHEST DRAWING LOGIC ---
		if (getState() == ChestState.SHAKING) {
			drawX += rand.nextInt(6) - 3;
			drawY += rand.nextInt(6) - 3;
		}

		// Draw Chest Base
		g.setColor(new Color(120, 70, 20));
		g.fillRect(drawX, drawY, width, height / 2);

		int lidHeight = height / 2;
		if (getState() == ChestState.OPENING || getState() == ChestState.OPEN) {
			lidHeight = height / 2 - openProgress;
			if (lidHeight < 0)
				lidHeight = 0;
		}

		// Draw Lid
		g.setColor(new Color(160, 100, 40));
		g.fillRect(drawX, drawY - lidHeight, width, lidHeight);

		// Draw Lock / Detail (Pixel style)
		g.setColor(new Color(200, 150, 0)); // Gold
		g.fillRect(drawX + (width / 2) - 4, drawY - lidHeight + (lidHeight / 2) - 4, 8, 8);

		// Loot Glow
		if (getState() == ChestState.OPEN || getState() == ChestState.OPENING && !tookItem) {
			g.setColor(new Color(255, 255, 100, 120));
			g.fillOval(drawX - 20, drawY - 40, width + 40, height);

		}
	}

	public void open() {
		if (gameObj.getPlayer().getGold() >= cost || Math.random() < gameObj.getPlayer().getArtifactManager().getPercentFreeChest()) {
			gameObj.getPlayer().setGold(gameObj.getPlayer().getGold() - cost);
			setState(ChestState.SHAKING);
			
			// Trigger artifact effects on chest opening (CreditCard, etc.)
			gameObj.getPlayer().getArtifactManager().onChestOpened();
		}
	}
}