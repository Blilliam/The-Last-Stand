package main;

import java.awt.image.BufferedImage;

public class Animation {

    private BufferedImage[] frames;
    private int currentFrame;

    private long lastTime;
    private long delay; // milliseconds between frames

    /**
     * creates an animation object
     * @param images to be animated
     * @param delay between frames
     */
    public Animation(BufferedImage[] frames, long delay) {
        this.frames = frames;
        this.delay = delay;
        currentFrame = 0;
        lastTime = System.currentTimeMillis();
    }

    /**
     * updates the animation
     */
    public void update() {

        long now = System.currentTimeMillis();

        if(now - lastTime > delay) { // if time has been more than the delay amount, go to next frame
            currentFrame++;
            lastTime = now;

            if(currentFrame >= frames.length) // if going out of bounds, go back to one
                currentFrame = 0;
        }
    }
    
    /**
     * get animtion frame
     * @return returns the current frame the animation is on
     */
    public BufferedImage getFrame() { 
        return frames[currentFrame];
    }
    
    public int getCurrentFrameIndex() {
    	return currentFrame;
    }
    
    /**
     * set frame to parameter
     * @param frame number to set it to
     */
    public void setFrame(int num) {
    	currentFrame = num;
    }
    
    public int getFrameLength() {
    	return frames.length;
    }
}