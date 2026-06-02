package Open.Entities;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.EnumMap;

import Open.Weapons.Weapon;
import main.AppPanel;
import main.GameObject;
import main.MouseInput;
import main.enums.WeaponUpgrades;

public class playerHud {
	private GameObject gameObj;

	public playerHud(GameObject gameObj) {
		this.gameObj = gameObj;
	}

	public void draw(Graphics2D g) {
		gameObj.getPlayer().drawInventoryPanel(g);
		gameObj.getPlayer().drawXPBar(g);
		gameObj.getPlayer().drawHpBar(g);
		gameObj.getPlayer().drawStatSidebar(g);
		gameObj.getPlayer().drawOwnedBooks(g);
		gameObj.getPlayer().drawActiveWeapons(g);
		gameObj.getPlayer().getArtifactManager().draw(g);
		gameObj.getPlayer().drawGoldCounter(g);
		gameObj.getPlayer().drawMinimap(g);
	}

	

}
