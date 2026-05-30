package States;

import java.awt.Graphics2D;

import main.GameObject;

public abstract class BaseState {
	GameObject gameObj;
	
	public BaseState(GameObject gameObj) {
		this.gameObj = gameObj;
	}
	
	public abstract void draw(Graphics2D g);
	public abstract void upadate();
	
}
