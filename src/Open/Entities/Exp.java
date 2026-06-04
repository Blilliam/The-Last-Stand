package Open.Entities;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import main.Animation;
import main.Assets;
import main.GameObject;
import main.Vec2;

public class Exp extends Entity {
	private int value;
	private static double valueMult = 1;
	private double maxSpeed = 22;
	private double magnetRange = 200;
	private int size;
	private Vec2 velocity;
	private boolean isCollected;
	private Animation expAnimation;

	// Pop-burst state
	private int burstTimer = 18;       // frames the orb flies outward before being attracted
	private boolean bursting = true;   // true during initial pop phase

	public Exp(GameObject gameObj, int value, int x, int y) {
		super(gameObj);
		expAnimation = new Animation(Assets.exp, 100);
		this.value = (int) Math.ceil(value * valueMult);
		size = (int) (5 + this.value * 0.05);
		width = size * 4;
		height = size * 4;
		this.setHitBox(new Ellipse2D.Double(x, y, this.width, this.height));
		setX(x);
		setY(y);
		isCollected = false;

		// Random outward burst on spawn
		velocity = new Vec2(0, 0);
	}

	public void draw(Graphics2D g2) {
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
		((Ellipse2D) this.getHitBox()).setFrame(this.x - width / 2, this.y - height / 2, this.width, this.height);
		expAnimation.update();
	}

	private void updatePhysics() {
		Vec2 playerPos = new Vec2(gameObj.getPlayer().getX(), gameObj.getPlayer().getY());
		Vec2 myPos = new Vec2(getX(), getY());
		Vec2 toPlayer = playerPos.sub(myPos);
		double distance = toPlayer.length();

		if (isCollected) magnetRange = 2000;

		if (bursting) {
			// Burst phase: just apply friction, count down timer
			burstTimer--;
			velocity = velocity.scale(0.88);
			if (burstTimer <= 0) bursting = false;
		} else {
			// Settle + attract phase
			velocity = velocity.scale(0.85); // stronger friction to kill burst momentum fast

			if (distance < magnetRange && distance > 0.001) {
				Vec2 dir = toPlayer.normalize();

				// Strength grows as distance SHRINKS — slow start, rocket finish
				double t = 1.0 - (distance / magnetRange);
				double pull = 0.8 + (t * t * t) * 14.0;

				velocity = velocity.add(dir.scale(pull));

				// Cap speed
				double speed = velocity.length();
				if (speed > maxSpeed) {
					velocity = velocity.normalize().scale(maxSpeed);
				}
			}
		}

		setX((int) (getX() + velocity.getX()));
		setY((int) (getY() + velocity.getY()));
	}

	public int getSize() { return size; }
	public void setSize(int newSize) { size = newSize; }
	public void setCollected(boolean b) { isCollected = b; }
}