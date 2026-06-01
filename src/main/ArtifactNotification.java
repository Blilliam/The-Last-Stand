package main;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import Open.Artifacts.Artifact;

/**
 * A single artifact notification popup (Risk of Rain 2 style)
 */
public class ArtifactNotification {
	private Artifact artifact;
	private int lifespan;           // Total frame duration
	private int remainingFrames;    // Countdown
	
	private final int DISPLAY_WIDTH = 400;
	private final int DISPLAY_HEIGHT = 100;
	private final int PADDING = 12;
	private final int ICON_SIZE = 80;
	
	public ArtifactNotification(Artifact artifact, int durationFrames) {
		this.artifact = artifact;
		this.lifespan = durationFrames;
		this.remainingFrames = durationFrames;
	}
	
	public void update() {
		remainingFrames--;
	}
	
	public boolean isAlive() {
		return remainingFrames > 0;
	}
	
	public void draw(Graphics2D g2, int x, int y) {
		// Calculate fade (alpha)
		float fadeStart = lifespan * 0.15f; // Last 15% of lifetime fades out
		float alpha = 1.0f;
		
		if (remainingFrames <= fadeStart) {
			alpha = remainingFrames / fadeStart;
		}
		
		// Save original composite
		AlphaComposite originalComposite = (AlphaComposite) g2.getComposite();
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		
		// Draw background panel
		g2.setColor(new Color(20, 20, 30, 200));
		g2.fillRoundRect(x, y, DISPLAY_WIDTH, DISPLAY_HEIGHT, 10, 10);
		
		// Draw border (gold/yellow for artifact theme)
		g2.setStroke(new BasicStroke(2.5f));
		g2.setColor(new Color(220, 150, 50, 200));
		g2.drawRoundRect(x, y, DISPLAY_WIDTH, DISPLAY_HEIGHT, 10, 10);
		
		// Draw artifact icon background circle
		int iconX = x + PADDING + ICON_SIZE / 2;
		int iconY = y + DISPLAY_HEIGHT / 2;
		g2.setColor(new Color(100, 60, 20, 150));
		g2.fillOval(iconX - ICON_SIZE / 2, iconY - ICON_SIZE / 2, ICON_SIZE, ICON_SIZE);
		
		// Draw artifact icon
		BufferedImage icon = artifact.getIcon();
		if (icon != null) {
			g2.drawImage(icon, iconX - ICON_SIZE / 2, iconY - ICON_SIZE / 2, ICON_SIZE, ICON_SIZE, null);
		}
		
		// Draw text section
		int textStartX = x + PADDING + ICON_SIZE + PADDING;
		int textStartY = y + PADDING;
		int maxTextWidth = DISPLAY_WIDTH - textStartX - PADDING - x;
		
		// Draw artifact name
		g2.setFont(new Font("Monospaced", Font.BOLD, 14));
		g2.setColor(new Color(255, 215, 50));
		String name = artifact.getName();
		g2.drawString(name, textStartX, textStartY + 18);
		
		// Draw artifact description (2-3 lines max)
		g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
		g2.setColor(new Color(200, 200, 200));
		String desc = artifact.getDescription();
		if (desc != null) {
			String[] lines = desc.split("\n");
			int descY = textStartY + 42;
			for (int i = 0; i < Math.min(lines.length, 2); i++) {
				String line = lines[i];
				if (line.length() > 45) {
					line = line.substring(0, 42) + "...";
				}
				g2.drawString(line, textStartX, descY);
				descY += 16;
			}
		}
		
		// Restore composite
		g2.setComposite(originalComposite);
	}
	
	public Artifact getArtifact() {
		return artifact;
	}
}
