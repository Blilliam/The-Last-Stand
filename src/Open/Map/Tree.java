package Open.Map;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

import Open.Entities.Entity;
import main.Assets;
import main.GameObject;

public class Tree extends Entity {
	private BufferedImage sprite = Assets.tree;
	private boolean playerColision;

	public Tree(GameObject gameObj, int x, int y) {
		super(gameObj);
		this.x = x;
		this.y = y;
		this.width = 300;
		this.height = 300;
		playerColision = false;

		this.setHitBox(new Rectangle2D.Double(this.x + 2 * width / 5, this.y + height / 2 - 20, this.width / 5,
				this.height / 2 + 20));
//		if (gameObj.getMap().getStage() == 3) {
//			
//		}
	}

	@Override
	public void update() {
		playerColision = Entity.checkCollision(this, gameObj.getPlayer());

	}

	@Override
	public void draw(Graphics2D g) {
		Composite old = g.getComposite();
		float alpha;
		if (playerColision) {
			alpha = 0.45f; // semi-transparent when colliding
		} else {
			alpha = 1.0f; // fully opaque otherwise
		}

		BufferedImage toDraw = sprite;

		if (gameObj.getMap().getStage() == 3) {
			// Tint the sprite purple by boosting red channel, reducing green/blue
			float[] scales = { 0.85f, 0.75f, 1.1f, 1.0f }; // slight purple: reduce R/G, boost B
			float[] offsets = { 0, 0, 0, 0 };
			toDraw = new RescaleOp(scales, offsets, null).filter(sprite, null);
		}

		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		g.drawImage(toDraw, x - gameObj.getCameraX(), y - gameObj.getCameraY(), width, height, null);
		g.setComposite(old);

		// super.drawHitBox(g);
	}
}
