package main;

import java.awt.Graphics2D;

public interface Renderable {
	
	public abstract void draw(Graphics2D g);
	
	public abstract int getSort();
}
