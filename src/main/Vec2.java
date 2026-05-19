package main;

import Open.Entities.Entity;

public class Vec2 {
	private double x, y;

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public Vec2(double x, double y) {
		this.x = x;
		this.y = y;
	}

	// Basic operations
	public Vec2 add(Vec2 o) {
		return new Vec2(x + o.x, y + o.y);
	}

	public Vec2 sub(Vec2 o) {
		return new Vec2(x - o.x, y - o.y);
	}

	public Vec2 scale(double s) {
		return new Vec2(x * s, y * s);
	}

	public double dot(Vec2 o) {
		return x * o.x + y * o.y;
	}

	// Length and normalization
	public double length() {
		return Math.sqrt(x * x + y * y);
	}

	public Vec2 normalize() {
		double len = length();
		return len == 0 ? new Vec2(0, 0) : scale(1.0 / len);
	}

	// Useful for projectiles
	public Vec2 perpendicular() {
		return new Vec2(-y, x);
	} // 90 degree rotation

	public double angleTo(Vec2 o) {
		return Math.atan2(o.y - y, o.x - x);
	}

	public static Vec2 fromAngle(double radians) {
		return new Vec2(Math.cos(radians), Math.sin(radians));
	}

	public static Vec2 between(Entity from, Entity to) {
		return new Vec2(to.getX() - from.getX(), to.getY() - from.getY()).normalize();
	}

	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}
}
