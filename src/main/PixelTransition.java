package main;

import java.awt.*;
import java.awt.image.BufferedImage;

public class PixelTransition {

	public enum Phase {
		IDLE, PIXELATE_IN, HOLD, PIXELATE_OUT, DONE
	}

	private Phase phase = Phase.IDLE;

	// how many update ticks each phase lasts
	private static final int IN_TICKS = 20;
	private static final int HOLD_TICKS = 8;
	private static final int OUT_TICKS = 20;

	private int tick = 0;

	// pixel size goes from 1 → MAX_PIXEL during pixelate-in
	private static final int MAX_PIXEL = 64;

	// snapshot of the screen taken the moment the transition starts
	private BufferedImage snapshot;

	// the action to run mid-transition (swapping the map)
	private Runnable midAction;

	// color used for the solid "hold" frame
	private static final Color HOLD_COLOR = new Color(10, 10, 20);

	public void start(BufferedImage currentFrame, Runnable midAction) {
		this.snapshot = currentFrame;
		this.midAction = midAction;
		this.phase = Phase.PIXELATE_IN;
		this.tick = 0;
	}

	public boolean isRunning() {
		return phase != Phase.IDLE && phase != Phase.DONE;
	}

	public void update() {
		if (!isRunning())
			return;
		tick++;

		switch (phase) {
		case PIXELATE_IN:
			if (tick >= IN_TICKS) {
				tick = 0;
				phase = Phase.HOLD;
				if (midAction != null)
					midAction.run(); // swap map here
			}
			break;

		case HOLD:
			if (tick >= HOLD_TICKS) {
				tick = 0;
				phase = Phase.PIXELATE_OUT;
			}
			break;

		case PIXELATE_OUT:
			if (tick >= OUT_TICKS) {
				tick = 0;
				phase = Phase.DONE;
			}
			break;

		default:
			break;
		}
	}

	public void draw(Graphics2D g2, int screenW, int screenH) {
		if (!isRunning())
			return;

		switch (phase) {
		case PIXELATE_IN: {
			// progress 0→1
			float t = (float) tick / IN_TICKS;
			int pixelSize = Math.max(1, (int) (t * MAX_PIXEL));
			drawPixelated(g2, snapshot, screenW, screenH, pixelSize);
			break;
		}
		case HOLD: {
			// full black/dark frame
			g2.setColor(HOLD_COLOR);
			g2.fillRect(0, 0, screenW, screenH);
			break;
		}
		case PIXELATE_OUT: {
			// progress 0→1, pixel size shrinks MAX→1
			float t = (float) tick / OUT_TICKS;
			int pixelSize = Math.max(1, (int) ((1f - t) * MAX_PIXEL));
			// draw the NEW scene (already painted beneath), overlay dark tiles
			drawPixelatedDark(g2, screenW, screenH, pixelSize);
			break;
		}
		default:
			break;
		}
	}

	/** Renders a pixelated version of `src` onto g2 */
	private void drawPixelated(Graphics2D g2, BufferedImage src, int w, int h, int pixelSize) {
		if (src == null)
			return;
		// sample the snapshot at each tile and fill with that color
		for (int y = 0; y < h; y += pixelSize) {
			for (int x = 0; x < w; x += pixelSize) {
				int sx = Math.min(x, src.getWidth() - 1);
				int sy = Math.min(y, src.getHeight() - 1);
				int rgb = src.getRGB(sx, sy);
				g2.setColor(new Color(rgb));
				g2.fillRect(x, y, Math.min(pixelSize, w - x), Math.min(pixelSize, h - y));
			}
		}
	}

	/**
	 * Pixelate-out: overlay dark tiles that shrink away, revealing the new scene
	 * beneath.
	 */
	private void drawPixelatedDark(Graphics2D g2, int w, int h, int pixelSize) {
		g2.setColor(HOLD_COLOR);
		for (int y = 0; y < h; y += pixelSize) {
			for (int x = 0; x < w; x += pixelSize) {
				g2.fillRect(x, y, Math.min(pixelSize, w - x), Math.min(pixelSize, h - y));
			}
		}
	}
}