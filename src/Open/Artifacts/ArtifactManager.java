package Open.Artifacts;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Random;

import Open.Artifacts.Common.*;
import main.AppPanel;
import main.MouseInput;
//import Open.Artifacts.Uncommon.*;
//import Open.Artifacts.Rare.*;
//import Open.Artifacts.Legendary.*;
import main.GameObject;

public class ArtifactManager {
	private GameObject gameObj;
	private ArrayList<Artifact> artifacts; // The player's current inventory

	// Pools for random generation (Items available in the game)
	private ArrayList<Artifact> commonPool;
	private ArrayList<Artifact> uncommonPool;
	private ArrayList<Artifact> rarePool;
	private ArrayList<Artifact> legendaryPool;

	private Random rand = new Random();

	// UI Constants
	private final int ICON_SIZE = 32;
	private final int PADDING = 6;
	private final int START_X = 25;
	private final int START_Y = 25;

	public ArtifactManager(GameObject gameObject) {
		this.gameObj = gameObject;
		this.artifacts = new ArrayList<>();

		this.commonPool = new ArrayList<>();
		this.uncommonPool = new ArrayList<>();
		this.rarePool = new ArrayList<>();
		this.legendaryPool = new ArrayList<>();

		fillPools();
	}

	/**
	 * Populates the master lists of all possible items.
	 */
	private void fillPools() {
		// Common Pool
		commonPool.add(new MidasHand(gameObj));
		commonPool.add(new Watch(gameObj));
		commonPool.add(new Key(gameObj));
		commonPool.add(new Battery(gameObj));
		commonPool.add(new Clover(gameObj));
		commonPool.add(new ChunkyOats(gameObj));

		// Uncommon Pool
//		uncommonPool.add(new Beer(gameObj));
//		uncommonPool.add(new ExperienceShard(gameObj));
//		uncommonPool.add(new BloodyDagger(gameObj));
//		uncommonPool.add(new BackPack(gameObj));
//		uncommonPool.add(new Kevin(gameObj));
//		uncommonPool.add(new GoldenShield(gameObj));
//
//		// Rare Pool
//		rarePool.add(new ChunkyRing(gameObj));
//		rarePool.add(new CreditCard(gameObj));
//		rarePool.add(new Mirror(gameObj));
//		rarePool.add(new DemonSoul(gameObj));
//
//		// Legendary Pool
//		legendaryPool.add(new Anvil(gameObj));
//		legendaryPool.add(new BigFork(gameObj));
//		legendaryPool.add(new BigHammer(gameObj));
		legendaryPool.add(new Magnet(gameObj));
	}

	/**
	 * Returns a random artifact instance based on ROR2 weights.
	 */
	public Artifact getRandomArtifact() {
		double roll = rand.nextDouble() * 100;

		if (roll < 1) { // 1% Legendary
			return createNewInstance(legendaryPool);
		} else if (roll < 20) { // 19% Uncommon
			return createNewInstance(uncommonPool);
		} else { // 80% Common
			return createNewInstance(commonPool);
		}
	}

	public ArrayList<Artifact> getArtifacts() {
		return artifacts;
	}

	/**
	 * Uses reflection to return a fresh instance of the artifact.
	 */
	private Artifact createNewInstance(ArrayList<Artifact> pool) {
		if (pool.isEmpty())
			return createNewInstance(commonPool);
		Artifact template = pool.get(rand.nextInt(pool.size()));
		try {
			return template.getClass().getConstructor(GameObject.class).newInstance(gameObj);
		} catch (Exception e) {
			System.out.println("Error instantiating artifact: " + template.name);
			return null;
		}
	}

	public void addArtifact(Artifact a) {
		if (a == null)
			return;

		boolean exists = false;
		for (Artifact artifact : artifacts) {
			// Check if classes match to increment stack
			if (artifact.getClass().equals(a.getClass())) {
				artifact.addCount();
				exists = true;
				break;
			}
		}

		if (!exists) {
			artifacts.add(a);
		}
	}

	public void draw(Graphics2D g2) {
		// Offset Y so it doesn't overlap the XP bar (which is 30px high)
		int startX = 15;
		int startY = 45;
		int x = startX;
		int y = startY;

		// Use a slightly smaller icon size for a cleaner look
		int displaySize = 60;
		int spacing = 8;

		int mx = MouseInput.getMouseX();
		int my = MouseInput.getMouseY();
		Artifact hoveredArtifact = null;

		for (int i = 0; i < artifacts.size(); i++) {
			Artifact artifact = artifacts.get(i);

			// Draw Shadow/Background for icon slot
			g2.setColor(new Color(0, 0, 0, 100));
			g2.fillRoundRect(x, y, displaySize, displaySize, 8, 8);

			if (artifact.getIcon() != null) {
				g2.drawImage(artifact.getIcon(), x, y, displaySize, displaySize, null);
			}

			// RoR2 style stack count in bottom right
			if (artifact.count > 1) {
				g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
				String count = "x" + artifact.count;
				int strW = g2.getFontMetrics().stringWidth(count);

				// Text Shadow
				g2.setColor(Color.BLACK);
				g2.drawString(count, x + displaySize - strW, y + displaySize + 1);
				// Text White
				g2.setColor(Color.WHITE);
				g2.drawString(count, x + displaySize - strW - 1, y + displaySize);
			}

			// CHECK FOR HOVER IN TOP LEFT CORNER ROW
			if (mx >= x && mx <= x + displaySize && my >= y && my <= y + displaySize) {
				hoveredArtifact = artifact;
			}

			x += (displaySize + spacing);

			// Wrap to next row if too many items
			if (x > AppPanel.WIDTH - 100) {
				x = startX;
				y += (displaySize + spacing);
			}
		}

		// Draw the matching tooltip on top of gameplay layers
		if (hoveredArtifact != null) {
			drawArtifactTooltip(g2, hoveredArtifact, mx, my);
		}
	}

	private void drawArtifactTooltip(Graphics2D g2, Artifact a, int mouseX, int mouseY) {
		// Splits description explicitly by line break instructions "\n"
		String[] lines = a.desc != null ? a.desc.split("\n")
				: new String[] { "No description available." };

		int rowHeight = 25;
		int padding = 15;

		// Define tool tip layout fonts
		Font titleFont = new Font("Monospaced", Font.BOLD, 16);
		Font descFont = new Font("Monospaced", Font.PLAIN, 14);

		// Calculate the max pixel width needed based on the text contents
		FontMetrics fmTitle = g2.getFontMetrics(titleFont);
		FontMetrics fmDesc = g2.getFontMetrics(descFont);

		int maxTextWidth = fmTitle.stringWidth(a.name);
		for (String lineText : lines) {
			int lineWidth = fmDesc.stringWidth(lineText);
			if (lineWidth > maxTextWidth) {
				maxTextWidth = lineWidth;
			}
		}

		// Add padding and a minor layout threshold safety margin to structural width
		int width = maxTextWidth + (padding * 2) + 12;
		int height = ((lines.length + 1) * rowHeight) + (padding * 2);

		int drawX = mouseX + 20;
		int drawY = mouseY + 20;

		// 1. Draw Background Box Frame
		g2.setColor(new Color(20, 20, 20, 230));
		g2.fillRoundRect(drawX, drawY, width, height, 5, 5);

		// 2. Draw Thick Grey Box Borders
		g2.setStroke(new BasicStroke(3));
		g2.setColor(new Color(100, 100, 100));
		g2.drawRoundRect(drawX, drawY, width, height, 5, 5);
		g2.setStroke(new BasicStroke(1));

		// 3. Draw Artifact Name Title Header
		g2.setFont(titleFont);
		int textY = drawY + padding + 15;

		// Title Drop Shadow
		g2.setColor(Color.BLACK);
		g2.drawString(a.name, drawX + padding + 2, textY + 2);
		// Title Main Foreground Text (Gold style like your stats values)
		g2.setColor(new Color(255, 215, 0));
		g2.drawString(a.name, drawX + padding, textY);

		// 4. Draw Split Description Text Fields
		g2.setFont(descFont);
		for (int i = 0; i < lines.length; i++) {
			textY += rowHeight;
			String lineText = lines[i];

			// Line Drop Shadow
			g2.setColor(Color.BLACK);
			g2.drawString(lineText, drawX + padding + 2, textY + 2);
			// Line Main Foreground Text (Silver grey text like your labels)
			g2.setColor(new Color(200, 200, 200));
			g2.drawString(lineText, drawX + padding, textY);
		}
	}

	// --- STAT CALCULATIONS ---

	public double GetPercentDamage() {
		double out = 0;
		for (Artifact a : artifacts)
			out += a.getPercentDamage();
		return out;
	}

	public double getBonusExpDropChance() {
		double out = 0;
		for (Artifact a : artifacts)
			out += a.getBonusExpDropChance();
		return out;
	}

	public int GetFlatDamage() {
		int out = 0;
		for (Artifact a : artifacts)
			out += a.getFlatDamage();
		return out;
	}

	public int getBonusProjectiles() {
		int out = 0;
		for (Artifact a : artifacts)
			out += a.getBonusProjectiles();
		return out;
	}

	public double getPercentBonusGold() {
		double out = 0;
		for (Artifact a : artifacts) {
			out += a.getPercentBonusGold();
		}
		return out;
	}

	public double getPercentHealth() {
		double out = 0;
		for (Artifact a : artifacts)
			out += a.getPercentHealth();
		return out;
	}

	public int getFlatHealth() {
		int out = 0;
		for (Artifact a : artifacts)
			out += a.getFlatHealth();
		return out;
	}

	public void onCritEffect() {
		for (Artifact a : artifacts)
			a.onCritEffect();
	}

	public double getPercentAttackSpeed() {
		double out = 0;
		for (Artifact a : artifacts)
			out += a.getPercentAttackSpeed();
		return out;
	}

	public double getPercentLuck() {
		double out = 0;
		for (Artifact a : artifacts)
			out += a.getPercentLuck();
		return out;
	}

	public double getPercentFreeChest() {
		double out = 0;
		for (Artifact a : artifacts)
			out += a.getPercentFreeChest();
		return out;
	}

	public double getPercentBonusExp() {
		double out = 0;
		for (Artifact a : artifacts)
			out += a.getPercentBonusExp();
		return out;
	}

	public double getBonusInvinsibilityFrames() {
		double out = 0;
		for (Artifact a : artifacts)
			out += a.getBonusInvinsibilityFrames();
		return out;
	}

	public void onUpdate() {
		for (Artifact a : artifacts)
			a.update();
	}
}