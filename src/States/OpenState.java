package States;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Comparator;

import Open.Artifacts.WorldItem;
import Open.Entities.Exp;
import Open.Entities.playerHud;
import Open.Entities.Enemies.Enemy;
import Open.Entities.Interactible.Interactible;
import Open.Map.Tree;
import Open.Weapons.WeaponProjectile.WeaponEntity;
import main.DamageText;
import main.GameObject;
import main.Renderable;
import main.ScoreManager;

public class OpenState extends BaseState {
	playerHud pHud;

	public OpenState(GameObject gameObj) {
		super(gameObj);
		pHud = new playerHud(gameObj);
	}

	@Override
	public void draw(Graphics2D g2) {
		gameObj.getMap().draw(g2); // draw map

		ArrayList<Renderable> entities = new ArrayList<Renderable>();

		
		
		for (int i = 0; i < gameObj.getExp().size(); i++) {
			Exp e = gameObj.getExp().get(i);
			if (gameObj.isOnScreen(e.getX(), e.getY(), e.getWidth(), e.getHeight())) {
				// e.draw(g2);
				// e.drawHitBox(g2);
				entities.add(e);
			}
		}

		for (int i = 0; i < gameObj.getProjectiles().size(); i++) {
			WeaponEntity e = gameObj.getProjectiles().get(i);
			if (gameObj.isOnScreen(e.getX(), e.getY(), e.getWidth(), e.getHeight())) {
				// e.draw(g2); // draw every enemy
				// e.drawHitBox(g2);
				entities.add(e);
			}
		}
		
		for (int i = 0; i < gameObj.getTrees().size(); i++) {
			Tree e = gameObj.getTrees().get(i);
			if (gameObj.isOnScreen(e.getX(), e.getY(), e.getWidth(), e.getHeight())) {
				// e.draw(g2); // draw every enemy
				// e.drawHitBox(g2);
				entities.add(e);
			}
		}
		for (int i = 0; i < gameObj.getEnemies().size(); i++) {
			Enemy e = gameObj.getEnemies().get(i);
			if (gameObj.isOnScreen(e.getX(), e.getY(), e.getWidth(), e.getHeight())) {
				// e.draw(g2); // draw every enemy
				// e.drawHitBox(g2);
				entities.add(e);
			}
		}
		
		for (int i = 0; i < gameObj.getInteractibles().size(); i++) {
			Interactible e = gameObj.getInteractibles().get(i);
			if (gameObj.isOnScreen(e.getX(), e.getY(), e.getWidth(), e.getHeight())) {
				// e.draw(g2); // draw every enemy
				// e.drawHitBox(g2);
				entities.add(e);
			}
		}
		entities.add(gameObj.getPlayer());
		
		entities.sort(Comparator.comparingDouble(Renderable::getSort));
		
		for (int i = 0; i < gameObj.getGroundItems().size(); i++) {
			WorldItem e = gameObj.getGroundItems().get(i);
			if (gameObj.isOnScreen(e.getX(), e.getY(), e.getWidth(), e.getHeight())) {
				// e.draw(g2); // draw every enemy
				// e.drawHitBox(g2);
				entities.add(e);
			}
		}

		
		for (int i = 0; i < entities.size(); i++) {
			entities.get(i).draw(g2);
		}

		for (int i = 0; i < gameObj.getDamageTexts().size(); i++) {
			DamageText e = gameObj.getDamageTexts().get(i);
			e.draw(g2, gameObj.getCameraX(), gameObj.getCameraY()); // draw every enemy

		}
		
		pHud.draw(g2);

		// Draw artifact notifications at the bottom
		if (gameObj.getNotificationManager() != null) {
			gameObj.getNotificationManager().draw(g2);
		}

	}

	@Override
	public void upadate() {
		ScoreManager.checkAndUpdateHighScore(gameObj.getPlayer().getKills());// update the score

		// updatePlayer
		gameObj.getPlayer().update();

		for (int i = gameObj.getEnemies().size() - 1; i >= 0; i--) { // for every enemy (going backwards)
			Enemy e = gameObj.getEnemies().get(i);

			e.update(); // update each enemy

			if (e.isDead()) {
				gameObj.getEnemies().remove(i); // removes dead enemies
			}
		}
		for (int i = gameObj.getDamageTexts().size() - 1; i >= 0; i--) {
			gameObj.getDamageTexts().get(i).update();
			if (gameObj.getDamageTexts().get(i).isDead())
				gameObj.getDamageTexts().remove(i);
		}

		for (int i = gameObj.getExp().size() - 1; i >= 0; i--) { // for every enemy (going backwards)
			Exp e = gameObj.getExp().get(i);

			e.update(); // update each enemy

			if (e.isDead()) {
				gameObj.getExp().remove(i); // removes dead enemies
			}
		}

		for (int i = gameObj.getInteractibles().size() - 1; i >= 0; i--) {
			Interactible e = gameObj.getInteractibles().get(i);

			e.update();

			if (e.isDead()) {
				gameObj.getInteractibles().remove(i);
			}
		}

		for (int i = gameObj.getProjectiles().size() - 1; i >= 0; i--) { // for every enemy (going backwards)
			WeaponEntity e = gameObj.getProjectiles().get(i);

			e.update(); // update each enemy

			if (e.isDead()) {
				gameObj.getProjectiles().remove(i); // removes dead enemies
			}
		}
		for (int i = gameObj.getGroundItems().size() - 1; i >= 0; i--) { // for every enemy (going backwards)
			WorldItem e = gameObj.getGroundItems().get(i);

			e.update(); // update each enemy
		}
		for (int i = gameObj.getTrees().size() - 1; i >= 0; i--) { // for every enemy (going backwards)
			Tree e = gameObj.getTrees().get(i);

			e.update(); // update each enemy
		}

		gameObj.getWaves().update(); // update enemy spawning

		if (gameObj.getKeyH().pause) {
			gameObj.setState(gameObj.getStatePause());
			gameObj.getKeyH().pause = false;
		}

		gameObj.getCam().update();

	}

}
