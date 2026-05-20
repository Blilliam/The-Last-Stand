package Open.Entities;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;

import main.GameObject;

public abstract class Entity { // main parent class of all moving objects on screen (weapon effects, player,
								// enemies)

	protected int x, y; // world coordinates

	// width and heigh
	protected int width;
	protected int height;

	// if youre dead
	protected boolean isDead;

	protected Shape hitBox;

	protected int speed; // pixels per update

	protected double maxHp; // maximum health
	protected double currHp; // current health

	protected GameObject gameObj;

	public Entity(GameObject gameObj) {
		this.gameObj = gameObj;
	}

	// abscract/required methods to be an entity
	public abstract void update();

	// Draw relative to player
	public abstract void draw(Graphics2D g);
	
	public void drawHitBox(Graphics2D g) {
		AffineTransform oldTransform = g.getTransform();

		// 2. Translate the graphics context to account for the camera
		g.translate(-gameObj.getCameraX(), -gameObj.getCameraY());

		// 3. Set a bright debug color and draw the raw shape
		g.setColor(Color.RED);
		g.draw(this.hitBox); // This automatically handles the centered Rectangle2D!

		// 4. Restore the graphics context back to normal
		g.setTransform(oldTransform);
	}

	// colision of 2 entities
	public static boolean rectCollision(Entity e1, Entity e2) {
		return e1.x < e2.x + e2.width && e1.x + e1.width > e2.x && e1.y < e2.y + e2.height && e1.y + e1.height > e2.y;
	}

	public static boolean checkCollision(Entity e1, Entity e2) {
		if (e1.hitBox == null || e2.hitBox == null)
			return false;

		// 1. BROAD PHASE: Fast AABB check using bounding rectangles
		if (!e1.hitBox.getBounds2D().intersects(e2.hitBox.getBounds2D())) {
			return false;
		}

		// 2. NARROW PHASE: Precise check using Area (only runs if bounds overlap)
		Area a1 = new Area(e1.hitBox);
		Area a2 = new Area(e2.hitBox);
		a1.intersect(a2);

		return !a1.isEmpty();
	}

	public static boolean circleCollision(Entity e1, Entity e2) {

		// center points
		double e1CenterX = e1.getX() + e1.getWidth() / 2.0;
		double e1CenterY = e1.getY() + e1.getHeight() / 2.0;

		double e2CenterX = e2.getX() + e2.getWidth() / 2.0;
		double e2CenterY = e2.getY() + e2.getHeight() / 2.0;

		// radii (assuming width = diameter)
		double r1 = e1.getWidth() / 2.0;
		double r2 = e2.getWidth() / 2.0;

		double dx = e1CenterX - e2CenterX;
		double dy = e1CenterY - e2CenterY;

		double distanceSquared = dx * dx + dy * dy;
		double radiusSum = r1 + r2;

		return distanceSquared <= radiusSum * radiusSum;
	}

	public static int getDistance(Entity e1, Entity e2) {
		double e1x = e1.getX() + e1.getWidth();
		double e1y = e1.getY() + e1.getHeight();

		double e2x = e2.getX() + e2.getWidth();
		double e2y = e2.getY() + e2.getHeight();

		double dxToEnemy = e1x - e2x;
		double dyToEnemy = e1y - e2y;

		return (int) (Math.sqrt((dxToEnemy * dxToEnemy) + (dyToEnemy * dyToEnemy)));
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public boolean isDead() {
		return isDead;
	}

	public void setDead(boolean isDead) {
		this.isDead = isDead;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public double getMaxHp() {
		return maxHp;
	}

	public void setMaxHp(int maxHp) {
		this.maxHp = maxHp;
	}

	public double getCurrHp() {
		return currHp;
	}

	public void setCurrHp(int currHp) {
		this.currHp = currHp;
	}

}
