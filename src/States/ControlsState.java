package States;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;

import main.AppPanel;
import main.GameObject;

public class ControlsState extends BaseState {

	public ControlsState(GameObject gameObj) {
		super(gameObj);
	}

	@Override
	public void draw(Graphics2D g2) {

		// 1. Dark Background Overlay
		g2.setColor(new Color(30, 30, 80, 200));
		g2.fillRect(0, 0, AppPanel.WIDTH, AppPanel.HEIGHT);

		// 2. Text Settings
		g2.setColor(Color.WHITE); // Changed to White for better visibility on dark blue
		g2.setFont(new Font("Malgun Gothic", Font.BOLD, 35));
		FontMetrics fm = g2.getFontMetrics();

		ArrayList<String> str = new ArrayList<String>();
		str.add("Up: W");
		str.add("Down: S");
		str.add("Left: A");
		str.add("Right: D");
		str.add("Interact: E"); // Added your new interaction key!

		// 3. Drawing the controls centered
		int startY = AppPanel.HEIGHT / 2 - ((str.size() * 80) / 2); // Start higher to center the whole block

		for (int i = 0; i < str.size(); i++) {
			String text = str.get(i);

			// Center X math: (ScreenWidth / 2) - (TextWidth / 2)
			int x = (AppPanel.WIDTH / 2) - (fm.stringWidth(text) / 2);

			// Y math: start position + offset per line
			int y = startY + (i * 60);

			// Draw shadow for "Pixel" look
			g2.setColor(Color.BLACK);
			g2.drawString(text, x + 2, y + 2);

			// Draw main text
			g2.setColor(Color.WHITE);
			g2.drawString(text, x, y);
		}

		// 4. Back Button
		if (gameObj.getExitControlButton() != null) {
			gameObj.getExitControlButton().draw(g2);
		}
	}

	@Override
	public void upadate() {
		if (gameObj.getExitControlButton() != null) {
			gameObj.getExitControlButton().update();
		}
	}
}