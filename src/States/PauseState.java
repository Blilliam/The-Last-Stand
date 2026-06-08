package States;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import main.AppPanel;
import main.GameButton;
import main.GameObject;

public class PauseState extends BaseState {
	
	private GameButton testerButton;

	public PauseState(GameObject gameObj) {
		super(gameObj);
		
		// Create tester button (right side, below pause text)
		int btnWidth = 200;
		int btnHeight = 60;
		int btnX = AppPanel.WIDTH / 2 + 150;
		int btnY = AppPanel.HEIGHT / 2 + 100;
		
		testerButton = new GameButton(btnX, btnY, btnWidth, btnHeight, "STAGE 3 TESTER",
				() -> {
					gameObj.setupTestingScenario();
					gameObj.setState(gameObj.getStateOpen());
				},
				new Color(100, 60, 120), Color.MAGENTA);
	}

	@Override
	public void upadate() {
		// FIX 1: Input handling and state transitions belong strictly in update
		if (gameObj.getKeyH().pause) {
			gameObj.setState(gameObj.getStateOpen());
			gameObj.getKeyH().pause = false;
		}
		
		// Update tester button
		testerButton.update();
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
		
		// 4. Draw tester button
		testerButton.draw(g);
	}
}