package Open.Map;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import main.AppPanel;
import main.Assets;
import main.GameObject;

public class Background {
	private int tileSize = 100;
	private int rows = 50, cols = 50;
	private int[][] tiles = new int[rows][cols];
	
	private int x =0;
	private int y =0;
	
	BufferedImage img;

	public final int WIDTH = tileSize * rows;
	public final int HEIGHT = tileSize * cols;

	private GameObject gameObj;

	public Background(GameObject gameObj) {
		this.gameObj = gameObj;

		img = Assets.background;
	}

	public void draw(Graphics2D g) {
		int drawX = x - gameObj.getCameraX();
		int drawY = y - gameObj.getCameraY();
		
		g.drawImage(img, drawX, drawY, tileSize*cols, tileSize*rows, null);
//		for (int r = 0; r < rows; r++) {
//			for (int c = 0; c < cols; c++) {
//				if (!gameObj.isOnScreen(c * tileSize, r * tileSize, tileSize, tileSize))
//					continue;
//				int screenX = c * tileSize - gameObj.getPlayer().getX() + AppPanel.WIDTH / 2;
//				int screenY = r * tileSize - gameObj.getPlayer().getY() + AppPanel.HEIGHT / 2;
//
//				if (tiles[r][c] == 0)
//					g.setColor(Color.GREEN);
//				else
//					g.setColor(Color.GRAY);
//
//				g.fillRect(screenX, screenY, tileSize - 2, tileSize - 2);
//			}
//		}
	}
}