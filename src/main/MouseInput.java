package main;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MouseInput extends MouseAdapter {

	//mouse positionn
    private static int mouseX;
    private static int mouseY;

    //mouse click
    private static boolean mousePressed = false;

    //if clicked = true
    @Override
    public void mousePressed(MouseEvent e) {
        setMousePressed(true);
    }

    //whenever moved, update position
    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    //also update position when moved
    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }

    // called once per frame to set pressed to false
    public static void update() {
        setMousePressed(false);
    }

	public static int getMouseX() {
		return mouseX;
	}

	public static int getMouseY() {
		return mouseY;
	}

	public static boolean isMousePressed() {
		return mousePressed;
	}

	public static void setMousePressed(boolean mousePressed) {
		MouseInput.mousePressed = mousePressed;
	}
    
}