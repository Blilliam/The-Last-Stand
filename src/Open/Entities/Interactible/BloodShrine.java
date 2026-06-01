package Open.Entities.Interactible;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import main.Assets;
import main.GameObject;
import main.enums.ChestState;

public class BloodShrine extends Interactible {
	public static final int InteractLimit = 3;
	private int interactCount = 0;

	public BloodShrine(GameObject gameObj, int x, int y) {
		super(gameObj, x, y);
		// TODO Auto-generated constructor stub
		width = 100;
		height = 100;
		this.setHitBox(new Rectangle2D.Double(this.x, this.y, this.width / 2, this.height / 2));

	}

	@Override
	public void open() {
	if (gameObj.getPlayer().getCurrHp() > getHpCost()) {
		if (interactCount < InteractLimit) {
			gameObj.getPlayer().addGold(getGold());
			gameObj.getPlayer().damage(getHpCost());

			interactCount++;
		}
		if (interactCount > InteractLimit) {
			setState(ChestState.OPEN);
		}
	}

	}

	@Override
	public void update() {
		updateInteract();
	}

	@Override
	public void draw(Graphics2D g) {
		int drawX = x - gameObj.getCameraX();
		int drawY = y - gameObj.getCameraY();

		g.drawImage(Assets.BloodShrine, drawX, drawY, width, height, null);

		if (playerInRange && getState() != ChestState.OPEN) {
			String msg = "[E] OPEN: +" + getGold() + "$ for -" + getHpCost() + " HP";
			g.setFont(new Font("Monospaced", Font.BOLD, 14)); // Monospaced looks more "pixel"
			FontMetrics fm = g.getFontMetrics();

			int msgWidth = fm.stringWidth(msg);
			int msgHeight = fm.getHeight();
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

	public int getGold() {
		switch (interactCount) {
		case 0:
			return (int) (Chest.BASE_COST * Math.pow(gameObj.getMap().getStage(), 2));
		case 1:
			return (int) (Chest.BASE_COST * Math.pow(gameObj.getMap().getStage(), 4) * 1.5);
		case 2:
			return (int) (Chest.BASE_COST * Math.pow(gameObj.getMap().getStage(), 2) * 2);
		default:
			return 0;
		}
	}

	public int getHpCost() {
		switch (interactCount) {
		case 0:
			return (int) (gameObj.getPlayer().getMaxHp() / 2);
		case 1:
			return (int) (3 * gameObj.getPlayer().getMaxHp() / 4);
		case 2:
			return (int) (gameObj.getPlayer().getMaxHp() - 1);
		default:
			return 0;
		}
	}

}
