package Open.Map;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Taskbar.State;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

import Open.Entities.Interactible.Chest;
import Open.Entities.Interactible.Interactible;
import Open.Entities.Interactible.Teleporter;
import Open.Entities.Interactible.TriShop;
import main.AppPanel;
import main.Assets;
import main.GameObject;

public class Background {
	private int tileSize = 200;
	private int rows = 50, cols = 50;
	private int[][] tiles = new int[rows][cols];
	private int stage;
	
	private int x =0;
	private int y =0;
	
	BufferedImage img;

	public final int WIDTH = tileSize * rows;
	public final int HEIGHT = tileSize * cols;

	private GameObject gameObj;

	public Background(GameObject gameObj) {
		this.gameObj = gameObj;

		img = Assets.background1;
		
		setStage(1);
		makeTrees();
		
	}
	private void makeTrees() {
		// Clear out or initialize a fresh list of interactibles
		gameObj.setTrees(new ArrayList<Tree>());

		Random rand = new Random();

		// TODO: Change these to your actual map dimensions (e.g., map.getWidth())
		int mapWidth = WIDTH;
		int mapHeight = HEIGHT;

		int centerX = mapWidth / 2;
		int centerY = mapHeight / 2;

		int totalItems = 500; // Total items to spawn per stage

		for (int i = 0; i < totalItems; i++) {
			int spawnX = rand.nextInt(mapWidth);
			int spawnY = rand.nextInt(mapHeight);
			gameObj.getTrees().add(new Tree(gameObj, spawnX, spawnY));
		}
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
	public void nextMap() {
		setStage(getStage() + 1);
		img = getBackground(getStage());
		makeTrees();
	}
	
	public BufferedImage getBackground(int stage) {
		switch (stage) {
		case 1: {
			return Assets.background1;
		}
		case 2: {
			return Assets.background2;
		}
		case 3: {
			return Assets.background3;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + stage);
		}
	}

	public int getStage() {
		return stage;
	}

	public void setStage(int stage) {
		this.stage = stage;
	}
}