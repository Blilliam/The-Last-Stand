package States;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import Open.Artifacts.Artifact;
import Open.Artifacts.Common.Battery;
import Open.Artifacts.Common.ChunkyOats;
import Open.Artifacts.Common.Key;
import Open.Artifacts.Common.MidasHand;
import Open.Artifacts.Common.TurboSocks;
import Open.Artifacts.Common.Watch;
import Open.Artifacts.Legendary.Anvil;
import Open.Artifacts.Legendary.BigFork;
import Open.Artifacts.Legendary.BigHammer;
import Open.Artifacts.Legendary.Magnet;
import Open.Artifacts.Rare.ChunkyRing;
import Open.Artifacts.Rare.CreditCard;
import Open.Artifacts.Rare.DemonSoul;
import Open.Artifacts.Rare.Mirror;
import Open.Artifacts.Uncommon.BackPack;
import Open.Artifacts.Uncommon.Beer;
import Open.Artifacts.Uncommon.BloodyDagger;
import Open.Artifacts.Uncommon.EchoShard;
import Open.Artifacts.Uncommon.GoldenShield;
import Open.Artifacts.Uncommon.Kevin;
import Open.Entities.Player;
import main.AppPanel;
import main.GameObject;
import main.MouseInput;

public class ItemState extends BaseState{
	Artifact[] artifacts = new Artifact[20];
	
	public ItemState(GameObject gameObj) {
		super(gameObj);
		artifacts[0] = new Key(gameObj);
		artifacts[1] = new Watch(gameObj);
		artifacts[2] = new Battery(gameObj);
		artifacts[3] = new TurboSocks(gameObj);
		artifacts[4] = new ChunkyOats(gameObj);
		artifacts[5] = new MidasHand(gameObj);
		
		artifacts[6] = new BackPack(gameObj);
		artifacts[7] = new Beer(gameObj);
		artifacts[8] = new BloodyDagger(gameObj);
		artifacts[9] = new EchoShard(gameObj);
		artifacts[10] = new GoldenShield(gameObj);
		artifacts[11] = new Kevin(gameObj);
		
		artifacts[12] = new ChunkyRing(gameObj);
		artifacts[13] = new CreditCard(gameObj);
		artifacts[14] = new DemonSoul(gameObj);
		artifacts[15] = new Mirror(gameObj);
		
		artifacts[16] = new Anvil(gameObj);
		artifacts[17] = new BigFork(gameObj);
			artifacts[18] = new BigHammer(gameObj);
			artifacts[19] = new Magnet(gameObj);

	}

	@Override
	public void upadate() {
		gameObj.getExitControlButton().update();
		
	}
	@Override
	public void draw(Graphics2D g2) {
		int startX = 15;
		int startY = 45;
		int x = startX;
		int y = startY;
		int displaySize = 200;
		int spacing = 8;

		int mx = MouseInput.getMouseX();
		int my = MouseInput.getMouseY();
		Artifact hoveredArtifact = null;

		for (Artifact artifact : artifacts) {
			g2.setColor(new Color(0, 0, 0, 100));
			g2.fillRoundRect(x, y, displaySize, displaySize, 8, 8);

			if (artifact.getIcon() != null)
				g2.drawImage(artifact.getIcon(), x, y, displaySize, displaySize, null);

			if (artifact.getCount() > 1) {
				g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
				String count = "x" + artifact.getCount();
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
		gameObj.getExitControlButton().draw(g2);
	}

	private void drawArtifactTooltip(Graphics2D g2, Artifact a, int mouseX, int mouseY) {
		String[] lines;
		if (a.getDesc() != null) {
			lines = a.getDesc().split("\n");
		} else {
			lines = new String[] { "No description available." };
		}

		int rowHeight = 25;
		int padding = 15;

		Font titleFont = new Font("Monospaced", Font.BOLD, 16);
		Font descFont = new Font("Monospaced", Font.PLAIN, 14);

		FontMetrics fmTitle = g2.getFontMetrics(titleFont);
		FontMetrics fmDesc = g2.getFontMetrics(descFont);

		int maxTextWidth = fmTitle.stringWidth(a.getName());
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
		g2.drawString(a.getName(), drawX + padding + 2, textY + 2);
		g2.setColor(new Color(255, 215, 0));
		g2.drawString(a.getName(), drawX + padding, textY);

		g2.setFont(descFont);
		for (String line : lines) {
			textY += rowHeight;
			g2.setColor(Color.BLACK);
			g2.drawString(line, drawX + padding + 2, textY + 2);
			g2.setColor(new Color(200, 200, 200));
			g2.drawString(line, drawX + padding, textY);
		}
	}

}
