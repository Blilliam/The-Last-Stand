package main;

import java.awt.Color;

public class GameSwitchButton extends GameButton {
    
    private boolean state;

    // Constructor with default colors
    public GameSwitchButton(int x, int y, int w, int h, String text, boolean initialState) {
        // Pass an empty dummy lambda () -> {} so the super constructor is happy
        super(x, y, w, h, text, () -> {}); 
        this.state = initialState;
    }

    // Constructor with custom colors
    public GameSwitchButton(int x, int y, int w, int h, String text, boolean initialState, Color mainColor, Color borderColor) {
        super(x, y, w, h, text, () -> {}, mainColor, borderColor);
        this.state = initialState;
    }

    @Override
    public void update() {
        // Re-implementing the click logic to flip our boolean instead of running a Runnable
        if (isHovering() && MouseInput.isMousePressed()) {
            this.state = !this.state; // Toggles true to false, or false to true
            MouseInput.update();     // Clears/updates the mouse state just like the parent class
        }
    }

    /**
     * @return current toggle state (true for ON, false for OFF)
     */
    public boolean getState() {
        return state;
    }

    /**
     * Manually set the switch state if needed
     */
    public void setState(boolean state) {
        this.state = state;
    }
}