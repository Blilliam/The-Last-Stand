package States;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import main.AppPanel;
import main.GameObject;

public class PauseState extends BaseState {

	public PauseState(GameObject gameObj) {
		super(gameObj);
	}

	@Override
	public void upadate() {
		// FIX 1: Input handling and state transitions belong strictly in update
		if (gameObj.getKeyH().pause) {
			gameObj.setState(gameObj.getStateOpen());
			gameObj.getKeyH().pause = false;
		}
	}

	@Override
	public void draw(Graphics2D g) {
		// 1. Draw the underlying game world (frozen in place)
		gameObj.getStateOpen().draw(g);
		
		// 2. Apply dark semi-transparent overlay
		g.setColor(new Color(0, 0, 0, 150)); 
		g.fillRect(0, 0, AppPanel.WIDTH, AppPanel.HEIGHT);
		
		// 3. BONUS: Draw "PAUSED" text to clearly indicate the state
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", Font.BOLD, 50));
		String text = "PAUSED";
		int x = AppPanel.WIDTH / 2 - g.getFontMetrics().stringWidth(text) / 2;
		int y = AppPanel.HEIGHT / 2;
		g.drawString(text, x, y);
	}
}