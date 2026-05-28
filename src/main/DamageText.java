package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class DamageText {
    private double x, y;
    private String text;
    private Color color;
    private int life = 40; 
    private double vy = -3.0; 

    public DamageText(double x, double y, double damage, boolean isCrit) {
        this.x = x;
        this.y = y;
        text = String.format("%.1f", damage);
        if(text.indexOf(".")>-1) {
        	char tenth = text.charAt(text.indexOf(".")+1);
        	text = text.substring(0,text.indexOf("."));
        	if(tenth!='0')
        		text+="."+tenth;
        }

        
        // Visual Style: Red for crit, White for normal
        this.color = isCrit ? Color.RED : Color.WHITE;
        
        if (isCrit) {
            this.vy = -5.0;   // Crits jump higher
            this.x += (Math.random() * 30) - 15; // Random spread
        }
    }

    public void update() {
        y += vy;
        vy *= 0.92; // Gravity effect
        life--;
    }

    public void draw(Graphics2D g2, int camX, int camY) {
        g2.setFont(new Font("Monospaced", Font.BOLD, color == Color.RED ? 28 : 20));
        
        int drawX = (int)x - camX;
        int drawY = (int)y - camY;

        // Draw Shadow
        g2.setColor(new Color(0, 0, 0, 150));
        g2.drawString(text, drawX + 2, drawY + 2);
        
        // Draw Main Number
        g2.setColor(color);
        g2.drawString(text, drawX, drawY);
    }

    public boolean isDead() { return life <= 0; }
}