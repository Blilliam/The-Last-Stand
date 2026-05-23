package Open.Entities.Interactible;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import Open.Entities.Enemies.BossEnemy;
import Open.Entities.Enemies.Enemy;
import main.GameObject;
import main.enums.ChestState;

public class Teleporter  extends Interactible{
	private boolean bossIsDefeated;

	public Teleporter(GameObject gameObj, int x, int y) {
		super(gameObj, x, y);
		// TODO Auto-generated constructor stub
		width = 50;
		height = 50;
		setHitBox(new Rectangle2D.Double(this.x, this.y, this.width, this.height));
		setBossIsDefeated(false);
	}

	@Override
	public void open() {
		//gameObj.getEnemies().add(new BossEnemy(gameObj, x, y, 1));
		
		setState(ChestState.OPEN);
		
		
		gameObj.getEnemies().add(new BossEnemy(gameObj, this, x, y, gameObj.getWaves().getDifficultyMult()));
		
		
		
		
	}

	@Override
	public void update() {
		updateInteract();
		if (isBossIsDefeated()) {
			gameObj.getInteractibles().add(new Portal(gameObj, x, y + 200));
			bossIsDefeated = false;
		}
	}

	@Override
	public void draw(Graphics2D g) {
		int drawX = x - gameObj.getCameraX();
		int drawY = y - gameObj.getCameraY();
		
		g.setColor(Color.blue);
		g.fillRect(drawX, drawY, width, height);
		
	}

	public boolean isBossIsDefeated() {
		return bossIsDefeated;
	}

	public void setBossIsDefeated(boolean bossIsDefeated) {
		this.bossIsDefeated = bossIsDefeated;
	}

}
