package Open.Entities.Interactible;

import java.awt.Graphics2D;
import java.util.Random;

import Open.Entities.Entity;
import main.GameObject;
import main.enums.ChestState;

public abstract class Interactible extends Entity{
	    protected boolean playerInRange = false;
	    protected int cost;
	    protected boolean tookItem;
	    
	    protected ChestState state;

	    protected Random rand = new Random();

	    public Interactible(GameObject gameObj, int x, int y) {
	        super(gameObj);
	        this.x = x;
	        this.y = y;
	        state = ChestState.CLOSED;
	    }
	    
	    protected void updateInteract() {
	    	// Calculate distance to player
	        int distance = Entity.getDistance(this, gameObj.getPlayer());
	        
	        if (state == ChestState.CLOSED) {
	            if (distance < 100) {
	                playerInRange = true;
	                // Check for interaction key (assuming your KeyHandler has an 'interact' boolean or similar)
	                // If you use a generic 'enter' or specific 'e' key:
	                if (gameObj.getKeyH().interact) { 
	                    open();
	                }
	            } else {
	                playerInRange = false;
	            }
	        }
	    }
	    
	    public abstract void open();

	    public abstract void update();

	    public abstract void draw(Graphics2D g);
	}

