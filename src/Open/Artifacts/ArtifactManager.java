package Open.Artifacts;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Random;

import Open.Artifacts.Common.Battery;
import Open.Artifacts.Common.ChunkyOats;
import Open.Artifacts.Common.Clover;
import Open.Artifacts.Common.Key;
import Open.Artifacts.Common.MidasHand;
import Open.Artifacts.Common.Watch;
import Open.Artifacts.Legendary.Anvil;
import Open.Artifacts.Legendary.BigFork;
import Open.Artifacts.Legendary.BigHammer;
import Open.Artifacts.Legendary.Magnet;
import Open.Artifacts.Uncommon.BackPack;
import Open.Artifacts.Uncommon.Beer;
import Open.Artifacts.Uncommon.BloodyDagger;
import Open.Artifacts.Uncommon.EchoShard;
import Open.Artifacts.Uncommon.GoldenShield;
import Open.Artifacts.Uncommon.Kevin;
import Open.Artifacts.Rare.ChunkyRing;
import Open.Artifacts.Rare.CreditCard;
import Open.Artifacts.Rare.DemonSoul;
import Open.Artifacts.Rare.Mirror;
import main.AppPanel;
import main.GameObject;
import main.MouseInput;

public class ArtifactManager {
	private GameObject gameObj;
	private ArrayList<Artifact> artifacts;

	// Pools for random generation
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

	private void fillPools() {
		commonPool.add(new MidasHand(gameObj));
		commonPool.add(new Watch(gameObj));
		commonPool.add(new Key(gameObj));
		commonPool.add(new Battery(gameObj));
		commonPool.add(new ChunkyOats(gameObj));

		uncommonPool.add(new Beer(gameObj));
		uncommonPool.add(new BackPack(gameObj));
		uncommonPool.add(new EchoShard(gameObj));
		uncommonPool.add(new Kevin(gameObj));
		uncommonPool.add(new BloodyDagger(gameObj));
		uncommonPool.add(new GoldenShield(gameObj));

		rarePool.add(new ChunkyRing(gameObj));
		rarePool.add(new CreditCard(gameObj));
		rarePool.add(new Mirror(gameObj));
		rarePool.add(new DemonSoul(gameObj));

		legendaryPool.add(new Magnet(gameObj));
		legendaryPool.add(new BigFork(gameObj));
		legendaryPool.add(new BigHammer(gameObj));
		legendaryPool.add(new Anvil(gameObj));
	}

	// =========================================================================
	// ARTIFACT RETRIEVAL
	// =========================================================================

	public Artifact getRandomArtifact() {
		double roll = rand.nextDouble() * 100;
		if (roll < 5)
			return createNewInstance(legendaryPool); // 5 % legendary
		if (roll < 20)
			return createNewInstance(rarePool); // 15 % rare
		if (roll < 50)
			return createNewInstance(uncommonPool); // 30 % uncommon
		return createNewInstance(commonPool); // 50 % common
	}

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
		for (Artifact artifact : artifacts) {
			if (artifact.getClass().equals(a.getClass())) {
				artifact.addCount();
				return;
			}
		}
		artifacts.add(a);
	}

	public ArrayList<Artifact> getArtifacts() {
		return artifacts;
	}

	// =========================================================================
	// DRAW
	// =========================================================================

	public void draw(Graphics2D g2) {
		int startX = 15;
		int startY = 45;
		int x = startX;
		int y = startY;
		int displaySize = 60;
		int spacing = 8;

		int mx = MouseInput.getMouseX();
		int my = MouseInput.getMouseY();
		Artifact hoveredArtifact = null;

		for (Artifact artifact : artifacts) {
			g2.setColor(new Color(0, 0, 0, 100));
			g2.fillRoundRect(x, y, displaySize, displaySize, 8, 8);

			if (artifact.getIcon() != null)
				g2.drawImage(artifact.getIcon(), x, y, displaySize, displaySize, null);

			if (artifact.count > 1) {
				g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
				String count = "x" + artifact.count;
				int strW = g2.getFontMetrics().stringWidth(count);
				g2.setColor(Color.BLACK);
				g2.drawString(count, x + displaySize - strW, y + displaySize + 1);
				g2.setColor(Color.WHITE);
				g2.drawString(count, x + displaySize - strW - 1, y + displaySize);
			}

			if (mx >= x && mx <= x + displaySize && my >= y && my <= y + displaySize)
				hoveredArtifact = artifact;

			x += (displaySize + spacing);
			if (x > AppPanel.WIDTH - 100) {
				x = startX;
				y += (displaySize + spacing);
			}
		}

		if (hoveredArtifact != null)
			drawArtifactTooltip(g2, hoveredArtifact, mx, my);
	}

	private void drawArtifactTooltip(Graphics2D g2, Artifact a, int mouseX, int mouseY) {
		String[] lines = a.desc != null ? a.desc.split("\n") : new String[] { "No description available." };

		int rowHeight = 25;
		int padding = 15;

		Font titleFont = new Font("Monospaced", Font.BOLD, 16);
		Font descFont = new Font("Monospaced", Font.PLAIN, 14);

		FontMetrics fmTitle = g2.getFontMetrics(titleFont);
		FontMetrics fmDesc = g2.getFontMetrics(descFont);

		int maxTextWidth = fmTitle.stringWidth(a.name);
		for (String line : lines)
			maxTextWidth = Math.max(maxTextWidth, fmDesc.stringWidth(line));

		int width = maxTextWidth + (padding * 2) + 12;
		int height = ((lines.length + 1) * rowHeight) + (padding * 2);
		int drawX = mouseX + 20;
		int drawY = mouseY + 20;

		g2.setColor(new Color(20, 20, 20, 230));
		g2.fillRoundRect(drawX, drawY, width, height, 5, 5);
		g2.setStroke(new BasicStroke(3));
		g2.setColor(new Color(100, 100, 100));
		g2.drawRoundRect(drawX, drawY, width, height, 5, 5);
		g2.setStroke(new BasicStroke(1));

		g2.setFont(titleFont);
		int textY = drawY + padding + 15;
		g2.setColor(Color.BLACK);
		g2.drawString(a.name, drawX + padding + 2, textY + 2);
		g2.setColor(new Color(255, 215, 0));
		g2.drawString(a.name, drawX + padding, textY);

		g2.setFont(descFont);
		for (String line : lines) {
			textY += rowHeight;
			g2.setColor(Color.BLACK);
			g2.drawString(line, drawX + padding + 2, textY + 2);
			g2.setColor(new Color(200, 200, 200));
			g2.drawString(line, drawX + padding, textY);
		}
	}

	// =========================================================================
	// STAT AGGREGATORS
	// =========================================================================

	public double GetPercentDamage() {
		double out = 0;
		for (Artifact a : artifacts)
			out += a.getPercentDamage();
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
		for (Artifact a : artifacts)
			out += a.getPercentBonusGold();
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

	/**
	 * Free-chest chance from artifacts PLUS the Luck Book bonus. Luck is currently
	 * expressed as an extra flat free-chest probability.
	 */
	public double getPercentFreeChest() {
		double out = 0;
		for (Artifact a : artifacts)
			out += a.getPercentFreeChest();
		// Luck Book: each percentage point of luck = 1 % extra free-chest probability
		out += gameObj.getPlayer().getStatBonus("Luck Book") / 100.0;
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

	public double getBonusExpDropChance() {
		double out = 0;
		for (Artifact a : artifacts)
			out += a.getBonusExpDropChance();
		return out;
	}

	public void onUpdate() {
		for (Artifact a : artifacts)
			a.update();
	}

	public void onDamageTaken(int damageAmount) {
		for (Artifact a : artifacts)
			a.onDamageTaken(damageAmount);
	}

	public void onKill(int goldReward) {
		for (Artifact a : artifacts)
			a.onKill(goldReward);
	}

	public double getReflectionChance() {
		double out = 0;
		for (Artifact a : artifacts)
			out += a.getReflectionChance();
		return out;
	}

	public double getHealthScalingBonus() {
		double out = 0;
		for (Artifact a : artifacts)
			out += a.getHealthScalingBonus();
		return out;
	}

	public double getThickCritChance() {
		double out = 0;
		for (Artifact a : artifacts)
			out += a.getThickCritChance();
		return out;
	}

	public double getBonkChance() {
		double out = 0;
		for (Artifact a : artifacts)
			out += a.getBonkChance();
		return out;
	}

	public int getStatBonusCount() {
		int out = 0;
		for (Artifact a : artifacts)
			out += a.getStatBonusCount();
		return out;
	}

	public double getMagnetCooldownMultiplier() {
		double out = 0;
		for (Artifact a : artifacts)
			out = Math.max(out, a.getMagnetCooldownMultiplier());
		return out;
	}
	
	public void onHitEffect() {
		for (Artifact a: artifacts) {
			a.onHitEffect();
		}
	}

	public void onChestOpened() {
		for (Artifact a : artifacts) {
			if (a instanceof Open.Artifacts.Rare.CreditCard) {
				((CreditCard) a).onChestOpened();
			}
		}
	}

	public int getAnvilMultiplier() {
		for (Artifact a : artifacts) {
			if (a instanceof Open.Artifacts.Legendary.Anvil) {
				return a.count + 1;
			}
		}
		return 1;
		
	}
}