package main;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import Open.Artifacts.Artifact;

/**
 * Manages all artifact notifications on screen
 * Risk of Rain 2 style - stacks at bottom with fade out
 */
public class NotificationManager {
	private List<ArtifactNotification> notifications;
	private final int NOTIFICATION_DURATION = 180; // 3 seconds at 60 FPS
	private final int NOTIFICATION_WIDTH = 400;
	private final int NOTIFICATION_HEIGHT = 100;
	private final int SPACING = 10;
	private final int PADDING_BOTTOM = 20;
	
	public NotificationManager() {
		this.notifications = new ArrayList<>();
	}
	
	/**
	 * Add a new artifact notification to the queue
	 */
	public void addNotification(Artifact artifact) {
		notifications.add(new ArtifactNotification(artifact, NOTIFICATION_DURATION));
	}
	
	/**
	 * Update all notifications and remove dead ones
	 */
	public void update() {
		for (ArtifactNotification notif : notifications) {
			notif.update();
		}
		// Remove dead notifications
		notifications.removeIf(notif -> !notif.isAlive());
	}
	
	/**
	 * Draw all notifications stacked at the bottom center
	 */
	public void draw(Graphics2D g2) {
		int displayCount = 0;
		
		// Draw from newest (bottom) to oldest (top)
		// Iterate backwards so newest is at bottom
		for (int i = notifications.size() - 1; i >= 0 && displayCount < 3; i--) {
			ArtifactNotification notif = notifications.get(i);
			
			// Calculate position: center horizontally, stack vertically from bottom
			int centerX = (AppPanel.WIDTH - NOTIFICATION_WIDTH) / 2;
			int notifY = AppPanel.HEIGHT - PADDING_BOTTOM - (displayCount + 1) * (NOTIFICATION_HEIGHT + SPACING);
			
			notif.draw(g2, centerX, notifY);
			displayCount++;
		}
	}
	
	public int getNotificationCount() {
		return notifications.size();
	}
}
