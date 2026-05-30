package main;

public class Camera {
	private GameObject gameObj;
	private Vec2 pos;
	
	public Camera(GameObject gameObj) {
		this.gameObj = gameObj;
		pos = new Vec2(0, 0);
	}
	
	public void update() {
		pos.setY(gameObj.getPlayer().getY() - AppPanel.HEIGHT/2);
		pos.setX(gameObj.getPlayer().getX() - AppPanel.WIDTH/2);
		
		if (pos.getX() < 0) {
			pos.setX(0);
		}
		if (gameObj.getMap().HEIGHT - AppPanel.HEIGHT < pos.getY()) {
			pos.setY(gameObj.getMap().HEIGHT - AppPanel.HEIGHT);
		}
		if (gameObj.getMap().WIDTH - AppPanel.WIDTH < pos.getX()) {
			pos.setX(gameObj.getMap().WIDTH - AppPanel.WIDTH);
		}
		if (pos.getY() < 0) {
			pos.setY(0);
		}
		
	}
	
	public void draw() {
		
	}
	
	public Vec2 getPos() {
		return pos;
	}
}
