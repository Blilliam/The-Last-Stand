package Open.Map;

import java.awt.Color;
import java.awt.Graphics2D;

import main.AppPanel;
import main.GameObject;

public class Background {
	private int tileSize = 100;
	private int rows = 50, cols = 50;
	private int[][] tiles = new int[rows][cols];

	public final int WIDTH = tileSize * rows;
	public final int HEIGHT = tileSize * cols;

	private GameObject gameObj;

	public Background(GameObject gameObj) {
		this.gameObj = gameObj;

		// Random map generation
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				tiles[r][c] = (Math.random() < 0.1) ? 1 : 0; // 10% walls
			}
		}
	}

	public void draw(Graphics2D g) {
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				if (!gameObj.isOnScreen(c * tileSize, r * tileSize, tileSize, tileSize))
					continue;
				int screenX = c * tileSize - gameObj.getPlayer().getX() + AppPanel.WIDTH / 2;
				int screenY = r * tileSize - gameObj.getPlayer().getY() + AppPanel.HEIGHT / 2;

				if (tiles[r][c] == 0)
					g.setColor(Color.GREEN);
				else
					g.setColor(Color.GRAY);

				g.fillRect(screenX, screenY, tileSize - 2, tileSize - 2);
			}
		}
	}
}