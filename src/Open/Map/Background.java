package Open.Map;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.ArrayList;
import java.util.Random;

import main.Assets;
import main.GameObject;

public class Background {
	private int tileSize = 200;
	private int rows = 50, cols = 50;
	private int[][] tiles = new int[rows][cols];
	private int stage;

	private int x = 0;
	private int y = 0;

	BufferedImage img;
	private BufferedImage tintedStage3Bg;

	public final int WIDTH = tileSize * rows;
	public final int HEIGHT = tileSize * cols;

	private GameObject gameObj;

	public Background(GameObject gameObj) {
		this.gameObj = gameObj;
		img = Assets.background1;
		setStage(1);
		makeTrees();
		precomputeTints();
	}

	private void precomputeTints() {
		BufferedImage src = new BufferedImage(Assets.background2.getWidth(), Assets.background2.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		src.createGraphics().drawImage(Assets.background2, 0, 0, null);

		float[] scales = { 0.85f, 0.75f, 1.1f, 1.0f }; // slight purple: reduce R/G, boost B
		float[] offsets = { 0, 0, 0, 0 };
		tintedStage3Bg = new RescaleOp(scales, offsets, null).filter(src, null);
	}

	private void makeTrees() {
		gameObj.setTrees(new ArrayList<Tree>());
		Random rand = new Random();
		for (int i = 0; i < 500; i++) {
			gameObj.getTrees().add(new Tree(gameObj, rand.nextInt(WIDTH), rand.nextInt(HEIGHT)));
		}
	}

	public void draw(Graphics2D g) {
		int drawX = x - gameObj.getCameraX();
		int drawY = y - gameObj.getCameraY();
		BufferedImage toDraw;
		if (stage == 3) {
			toDraw = tintedStage3Bg;
		} else {
			toDraw = img;
		}
		g.drawImage(toDraw, drawX, drawY, tileSize * cols, tileSize * rows, null);
	}

	public void nextMap() {
		setStage(getStage() + 1);
		img = getBackground(getStage());
		makeTrees();
	}

	public BufferedImage getBackground(int stage) {
		switch (stage) {
		case 1:
			return Assets.background1;
		case 2:
			return Assets.background2;
		case 3:
			return Assets.background1;
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