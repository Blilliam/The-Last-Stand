package States;

import java.awt.Color;
import java.awt.Graphics2D;

import main.AppPanel;
import main.GameObject;

public class DeadState extends BaseState{

	public DeadState(GameObject gameObj) {
		super(gameObj);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void draw(Graphics2D g2) {
		g2.setColor(new Color(0, 0, 0, 100));
		g2.fillRect(0, 0, AppPanel.WIDTH, AppPanel.HEIGHT);
		gameObj.getExitControlButton().draw(g2); // draw exit button
		
	}

	@Override
	public void upadate() {
		gameObj.getExitControlButton().update(); // button for going back to menu
		
	}

}
