package Open.Artifacts;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Color;
import Open.Entities.Entity;
import main.GameObject;

public class WorldItem extends Entity {
    private Artifact artifact;
    
    // Physics & State
    private float velY = -8.0f;     // Initial jump force
    private float gravity = 0.4f;   // Pulls item back to floorY
    private float floorY;           // The target resting height
    
    private boolean isLanding = true;
    private boolean isPickedUp = false;
    
    // Animation Variables
    private float animScale = 0.0f;  // Current scale multiplier
    private float animAlpha = 1.0f;  // Transparency (0.0 to 1.0)
    private float animYOffset = 0;   // Vertical shift for bobbing/popping
    private float floatTimer = 0.0f; // Time tracker for sine/exp math

    public WorldItem(GameObject gameObj, Artifact artifact, int x, int y) {
        super(gameObj);
        this.artifact = artifact;
        this.x = x;
        this.y = y;
        this.floorY = y;
        this.width = 32;
        this.height = 32;
    }

    public void update() {
        if (isLanding) {
            // --- SPAWN: Arcs up and falls to floor ---
            y += velY;
            velY += gravity;
            
            // Grow to full size while in the air
            if (animScale < 1.0f) animScale += 0.1f;

            if (y >= floorY) {
                y = (int) floorY;
                isLanding = false;
                animScale = 1.0f;
            }
        } else if (!isPickedUp) {
            // --- IDLE: Hovering in place ---
            floatTimer += 0.05f;
            animYOffset = (float) Math.sin(floatTimer) * 5; // Soft bobbing

            if (Entity.rectCollision(this, gameObj.getPlayer())) {
                isPickedUp = true;
                gameObj.getPlayer().getArtifactManager().addArtifact(artifact);
                floatTimer = 0; // Reset timer to reuse for pickup math
            }
        } else {
            // --- PICKUP: The RoR2 "Burst Pop" ---
            floatTimer += 0.15f; 
            
            // 1. Exponential Rise (High initial speed, quick deceleration)
            animYOffset -= (12.0f * Math.exp(-floatTimer * 0.6f));
            
            // 2. Scale Overshoot (Expands fast, then shrinks slightly)
            animScale = 1.0f + (float) Math.sin(floatTimer * 2.0f) * 1.5f;
            
            // 3. Fast Fade Out
            animAlpha -= 0.07f;
            
            if (animAlpha <= 0) {
                gameObj.getGroundItems().remove(this);
            }
        }
    }

    @Override
    public void draw(Graphics2D g) {
        if (gameObj.isOnScreen(x, y, width, height)) {
            // Set transparency for the pop effect
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, animAlpha)));

            // Calculate draw coordinates including the animation offsets
            int drawX = (int) (x - gameObj.getCameraX());
            int drawY = (int) (y - gameObj.getCameraY() + animYOffset);

            // Scale calculations (Standard size is 1.5x original)
            int finalW = (int) (width * 1.5 * animScale);
            int finalH = (int) (height * 1.5 * animScale);

            // Draw a slight "flare" or glow during the first few frames of pickup
            if (isPickedUp && animAlpha > 0.7f) {
                g.setColor(new Color(255, 255, 255, (int)(150 * animAlpha)));
                g.fillOval(drawX - finalW/2, drawY - finalH/2, finalW, finalH);
            }

            // Draw the artifact icon centered
            g.drawImage(artifact.getIcon(), 
                        drawX - finalW / 2, 
                        drawY - finalH / 2, 
                        finalW, finalH, null);

            // Always reset composite so other objects aren't transparent
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
    }
}