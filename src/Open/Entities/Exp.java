package Open.Entities;

import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RectangularShape;

import main.Animation;
import main.Assets;
import main.GameObject;
import main.Vec2;

public class Exp extends Entity {
	private int value;
	private static double valueMult = 1;

	private double maxSpeed = 10;
	private double accelStrength = 0.5;

	private double magnetRange = 200;
	private int size;

	private int dx;
	private int dy;

	private Vec2 velocity;

	private boolean isCollected;

	private Animation expAnimation;

	public Exp(GameObject gameObj, int value, int x, int y) {
		super(gameObj);

		expAnimation = new Animation(Assets.exp, 100);
		this.value = (int) Math.ceil(value * valueMult);

		size = (int) (5 + this.value * 1.3);

		width = size * 4;
		height = size * 4;

		this.setHitBox(new Ellipse2D.Double(this.x, this.y, this.width, this.height));

		dx = 0;
		dy = 0;

		setX(x);
		setY(y);

		isCollected = false;

		velocity = new Vec2(0, 0);
	}

	public void draw(Graphics2D g2) {

		// Draw coin
		int drawX = x - gameObj.getCameraX() - width / 2;
		int drawY = y - gameObj.getCameraY() - height / 2;

		g2.drawImage(expAnimation.getFrame(), drawX, drawY, width, height, null);
	}

	public void update() {

		updatePhysics();

		if (Entity.checkCollision(this, gameObj.getPlayer())) {
			gameObj.getPlayer().addExp(value);
			isDead = true;
		}
		((Ellipse2D) this.getHitBox()).setFrame(this.x - width/2, this.y - height/2, this.width, this.height);

		expAnimation.update();
	}

	private void updatePhysics() {
		// Player center
		Vec2 playerPos = new Vec2(gameObj.getPlayer().getX(), gameObj.getPlayer().getY());

		// Exp center
		Vec2 myPos = new Vec2(getX() + size / 2, getY() + size / 2);

		// Direction to player
		Vec2 toPlayer = playerPos.sub(myPos);
		double distance = toPlayer.length();

		// 1. FRICTION
		velocity = velocity.scale(0.92);

		if (isCollected) {
			magnetRange = 2000;
		}

		// 2. MAGNET BEHAVIOR
		if ((distance < magnetRange && distance > 0.001)) {

			Vec2 dir = toPlayer.normalize();

			double strength = 1.0 - (distance / magnetRange);
			strength = strength * strength;

			double pull = 4.0 * strength; // stronger feel

			velocity = velocity.add(dir.scale(pull));
		}

		// 3. APPLY MOVEMENT

		setX((int) (getX() + velocity.getX()));
		setY((int) (getY() + velocity.getY()));
	}

	public int getSize() {
		return size;
	}

	public void setSize(int newSize) {
		size = newSize;
	}

	public void setCollected(boolean b) {
		isCollected = b;

	}

}
