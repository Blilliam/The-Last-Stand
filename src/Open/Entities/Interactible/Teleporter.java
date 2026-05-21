package Open.Entities.Interactible;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import Open.Entities.Enemies.BossEnemy;
import main.GameObject;
import main.enums.ChestState;

public class Teleporter  extends Interactible{

	public Teleporter(GameObject gameObj, int x, int y) {
		super(gameObj, x, y);
		// TODO Auto-generated constructor stub
		width = 50;
		height = 50;
		setHitBox(new Rectangle2D.Double(this.x, this.y, this.width, this.height));
	}

	@Override
	public void open() {
		//gameObj.getEnemies().add(new BossEnemy(gameObj, x, y, 1));
		gameObj.getInteractibles().add(new Portal(gameObj, x, y - 200));
		state = ChestState.OPEN;
		
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
		g.fillRect(drawX, drawY, width, height);
		
	}

}
