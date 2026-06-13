package States;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import main.AppPanel;
import main.GameButton;
import main.GameObject;

public class DifficultyState extends BaseState {

	private GameButton easyBtn;
	private GameButton normalBtn;
	private GameButton hardBtn;

	private final int BUTTON_W = 300;
	private final int BUTTON_H = 100;

	public DifficultyState(GameObject gameObj) {
		super(gameObj);

		int centerX = AppPanel.WIDTH / 2 - BUTTON_W / 2;
		int startY = AppPanel.HEIGHT / 2 - BUTTON_H - 30;

		easyBtn = new GameButton(centerX, startY, BUTTON_W, BUTTON_H, "EASY", () -> {
			gameObj.startGameWithDifficulty("EASY");
		}, new Color(30, 130, 30), Color.BLACK);

		normalBtn = new GameButton(centerX, startY + BUTTON_H + 20, BUTTON_W, BUTTON_H, "NORMAL", () -> {
			gameObj.startGameWithDifficulty("NORMAL");
		}, new Color(0, 60, 60), Color.BLACK);

		hardBtn = new GameButton(centerX, startY + (BUTTON_H + 20) * 2, BUTTON_W, BUTTON_H, "HARD", () -> {
			gameObj.startGameWithDifficulty("HARD");
		}, new Color(130, 30, 30), Color.BLACK);
	}

	@Override
	public void draw(Graphics2D g) {
		// Draw title
		g.setFont(new Font("Monospaced", Font.BOLD, 64));
		String title = "SELECT DIFFICULTY";
		int xTitle = AppPanel.WIDTH / 2 - g.getFontMetrics().stringWidth(title) / 2;
		g.setColor(Color.BLACK);
		g.drawString(title, xTitle + 5, 160);
		g.setColor(new Color(255, 200, 50));
		g.drawString(title, xTitle, 155);

		// Draw buttons
		easyBtn.draw(g);
		normalBtn.draw(g);
		hardBtn.draw(g);
	}

	@Override
	public void upadate() {
		easyBtn.update();
		normalBtn.update();
		hardBtn.update();
	}

}
