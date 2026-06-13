package States;

import java.awt.Graphics2D;

import main.AppPanel;
import main.Assets;
import main.GameObject;

public class MenuState extends BaseState{

	public MenuState(GameObject gameObj) {
		super(gameObj);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void draw(Graphics2D g2) {
		// TODO Auto-generated method stub
		g2.drawImage(Assets.menuBackground, 0, 0, AppPanel.WIDTH, AppPanel.HEIGHT, null);
		
		gameObj.getStartButton().draw(g2);
		gameObj.getControlButton().draw(g2);
		gameObj.getItemButton().draw(g2);
	}

	@Override
	public void upadate() {
		gameObj.getStartButton().update();
		gameObj.getControlButton().update();
		gameObj.getItemButton().update();
		
	}

}
