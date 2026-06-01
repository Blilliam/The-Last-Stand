package Open.Map;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

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

		this.setHitBox(new Rectangle2D.Double(this.x + 2 * width / 5, this.y + height / 2 - 10, this.width / 5,
				this.height / 2));
	}

	@Override
	public void update() {
		playerColision = Entity.checkCollision(this, gameObj.getPlayer());

	}

	@Override
	public void draw(Graphics2D g) {
		if (playerColision) {
			Composite old = g.getComposite();
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,  0.45f));
			g.drawImage(sprite, x - gameObj.getCameraX(), y - gameObj.getCameraY(), width, height, null);
			g.setComposite(old);
		} else {
			g.drawImage(sprite, x - gameObj.getCameraX(), y - gameObj.getCameraY(), width, height, null);
		}
		
		//super.drawHitBox(g);
		
	}
}
