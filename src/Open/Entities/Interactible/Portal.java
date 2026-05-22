package Open.Entities.Interactible;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import Open.Map.Background;
import main.GameObject;
import main.enums.ChestState;

public class Portal extends Interactible{

	public Portal(GameObject gameObj, int x, int y) {
		super(gameObj, x, y);
		// TODO Auto-generated constructor stub
		width = 50;
		height = 100;
		this.setHitBox(new Ellipse2D.Double(this.x, this.y, this.width, this.height));
	}

	@Override
	public void open() {
		System.out.println("teleop");
		gameObj.nextMap();
		setState(ChestState.OPEN);
		
	}

	@Override
	public void update() {
		updateInteract();
	}

	@Override
	public void draw(Graphics2D g) {
		int drawX = x - gameObj.getCameraX();
		int drawY = y - gameObj.getCameraY();
		
		g.setColor(Color.blue);
		g.fillOval(drawX, drawY, width, height);
		
	}

}
