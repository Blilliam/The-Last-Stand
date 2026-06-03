package main;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

public class AppPanel extends JPanel implements Runnable {
	static Toolkit tk = Toolkit.getDefaultToolkit();

	public static final int WIDTH = (int) tk.getScreenSize().getWidth();
	public static final int HEIGHT = (int) tk.getScreenSize().getHeight();
	public Dimension d = new Dimension(WIDTH, HEIGHT);

	public Thread t = new Thread(this);

	MouseInput mouseHandler = new MouseInput();
	KeyboardInput keyH = new KeyboardInput();

	GameObject gameObj = new GameObject(keyH, mouseHandler);

	private BufferedImage buffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

	public AppPanel() {
		setPreferredSize(d);
		addKeyListener(keyH);
		setFocusable(true);
		addMouseListener(mouseHandler);
		addMouseMotionListener(mouseHandler);
		t.start();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2 = buffer.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

		g2.clearRect(0, 0, WIDTH, HEIGHT);

		gameObj.draw(g2);

		gameObj.getPixelTransition().draw(g2, WIDTH, HEIGHT);
		g2.dispose();

		g.drawImage(buffer, 0, 0, null);

		gameObj.setLastFrame(buffer);
	}

	@Override
	public void run() {
		while (true) {
			repaint();
			gameObj.update();
			try {
				Thread.sleep(17);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}