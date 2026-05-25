package States;

import java.awt.Color;
import java.awt.Graphics2D;

import main.AppPanel;
import main.GameObject;

public class UpgradeState extends BaseState{

	public UpgradeState(GameObject gameObj) {
		super(gameObj);
	}

	@Override
	public void draw(Graphics2D g2) {
		
		
		gameObj.getStateOpen().draw(g2);
		g2.setColor(new Color(0, 0, 0, 150)); // dark semi-transparent overlay
		g2.fillRect(0, 0, AppPanel.WIDTH, AppPanel.HEIGHT);
		gameObj.getUpgrades().draw(g2);
		
		
	}

	@Override
	public void upadate() {
		
		gameObj.getUpgrades().update();
	}

}
