package States;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import main.AppPanel;
import main.GameButton;
import main.GameObject;

public class TutorialState extends BaseState {

	private int currentPage = 0;
	private List<TutorialPage> pages;
	private GameButton nextButton;
	private GameButton prevButton;
	private GameButton backButton;

	public TutorialState(GameObject gameObj) {
		super(gameObj);
		initializePages();
		createButtons();
	}

	private void initializePages() {
		pages = new ArrayList<>();

		// Page 0: Welcome
		pages.add(new TutorialPage("WELCOME TO VAMPIRE SURVIVORS",
				new String[] {
						"This is an action roguelike game where you fight endless waves",
						"of enemies to survive as long as possible.",
						"",
						"Defeat enemies, collect experience orbs, and unlock new weapons",
						"and upgrades to become stronger!",
						"",
						"Navigate with WASD or Arrow Keys. Press E to interact."
				}));

		// Page 1: Movement & Interaction
		pages.add(new TutorialPage("CONTROLS & MOVEMENT",
				new String[] {
						"W - Move Up",
						"S - Move Down",
						"A - Move Left",
						"D - Move Right",
						"",
						"E - Interact with chests, shops, and portals",
						"ESC - Pause / Unpause the game",
						"",
						"Stay mobile! Kite enemies by moving while attacking."
				}));

		// Page 2: Combat & Weapons
		pages.add(new TutorialPage("WEAPONS & COMBAT",
				new String[] {
						"• SWORD: Classic melee weapon with AoE crescent attacks",
						"• AURA: Passive ring that damages enemies around you",
						"• KATANA: Quick slashing attacks that hit nearby enemies",
						"• PEW PEW: Bouncing bullets that ricochet off enemies",
						"• BANANA: Homing projectile that returns to you",
						"• BONE: Bouncy bone projectile with more bounces",
						"• FIRE STAFF: Explosive projectile with short duration",
						"• DEXECUTIONER: Large piercing spike attack",
						"",
						"Each weapon can be upgraded! Max 4 weapons active."
				}));

		// Page 3: Leveling Up
		pages.add(new TutorialPage("LEVELING UP & UPGRADES",
				new String[] {
						"Defeat enemies to collect EXPERIENCE ORBs (blue spheres).",
						"Fill the XP bar at the top to level up!",
						"",
						"When you level up, choose from 3 upgrade options:",
						"• Unlock a new WEAPON (if you have less than 4)",
						"• Upgrade an existing weapon's stats",
						"• Acquire a BOOK (passive bonus)",
						"",
						"Plan your upgrades carefully for synergy!"
				}));

		// Page 4: Passive Books/Tomes
		pages.add(new TutorialPage("PASSIVE BOOKS & TOMES",
				new String[] {
						"Books provide permanent passive bonuses. You can own up to 4:",
						"• DAMAGE BOOK: +X% weapon damage",
						"• SIZE BOOK: +X% projectile size",
						"• PROJECTILE SPEED BOOK: +X% projectile speed",
						"• COOLDOWN BOOK: +X% attack speed",
						"• CRIT RATE BOOK: +X% critical chance",
						"• QUANTITY OVER QUALITY: +X flat projectiles per shot",
						"• MAX HP BOOK: +X max health",
						"• GOLD BOOK: +X% gold from drops",
						"• EXP BOOK: +X% experience gain",
						"",
						"Books stack, so multiple upgrades multiply their effects!"
				}));

		// Page 5: Artifacts & Items
		pages.add(new TutorialPage("ARTIFACTS & RELICS",
				new String[] {
						"Artifacts are special items that drop from chests and shops.",
						"They provide unique bonuses and synergies:",
						"",
						"• Damage multipliers and health scaling",
						"• Critical strike bonuses",
						"• Projectile path modifications",
						"• Life steal and healing effects",
						"• And many more unique effects!",
						"",
						"Some artifacts have LEGENDARY rarity and powerful effects.",
						"Collect different artifacts to build unique synergies!"
				}));

		// Page 6: Map Elements
		pages.add(new TutorialPage("MAP ELEMENTS & INTERACTIBLES",
				new String[] {
						"CHEST (Gold chest):",
						"  Contains gold and occasionally artifacts.",
						"  Approach and press E to open.",
						"",
						"SHOP (Tri-colored icon):",
						"  Trade gold for weapons, books, or artifacts.",
						"  Buy items strategically to enhance your build.",
						"",
						"TELEPORTER (Cyan portal):",
						"  Advance to the next stage (enemies reset).",
						"  Use when current wave becomes too difficult.",
						"",
						"The MINIMAP (top right) shows all these elements."
				}));

		// Page 7: Survival Strategy
		pages.add(new TutorialPage("SURVIVAL STRATEGY",
				new String[] {
						"POSITIONING: Keep distance from enemies while attacking.",
						"KITING: Move in circles to dodge while dealing damage.",
						"CROWD CONTROL: Use multiple weapons for AoE coverage.",
						"WEAPON SYNERGY: Pick weapons that complement each other.",
						"",
						"SCALING: Prioritize damage upgrades early game.",
						"LATE GAME: Mix damage with health for sustainability.",
						"",
						"SHOPS: Save gold for crucial upgrades in shops.",
						"PROGRESSION: Use new stages to get stronger!"
				}));

		// Page 8: Advanced Tips
		pages.add(new TutorialPage("ADVANCED TIPS & TRICKS",
				new String[] {
						"• Critical strikes appear with special visual effects.",
						"• Artifacts stack - combine them for synergies.",
						"• Some artifacts have special effects like health scaling.",
						"• Weapon rarity affects stat gains:",
						"  BRONZE < SILVER < GOLD < DIAOMOND",
						"",
						"• Try different weapon combinations!",
						"• Each playthrough is unique - adapt your strategy.",
						"• The minimap helps you navigate the world.",
						"• Use the pause menu to think strategically."
				}));

		// Page 9: Final Tips
		pages.add(new TutorialPage("GOOD LUCK!",
				new String[] {
						"You now have all the basics to survive!",
						"",
						"Remember:",
						"• Stay mobile and avoid getting surrounded",
						"• Collect all experience orbs",
						"• Plan your upgrades for synergy",
						"• Use shops to buy powerful items",
						"• Advance to new stages when ready",
						"",
						"Most importantly: Have fun!"
				}));
	}

	private void createButtons() {
		int buttonWidth = 200;
		int buttonHeight = 60;
		int yPos = AppPanel.HEIGHT - buttonHeight - 20;

		prevButton = new GameButton(20, yPos, buttonWidth, buttonHeight, "< PREVIOUS",
				() -> previousPage(), new Color(100, 100, 150), Color.BLACK);

		nextButton = new GameButton(AppPanel.WIDTH - buttonWidth - 20, yPos, buttonWidth, buttonHeight, "NEXT >",
				() -> nextPage(), new Color(100, 100, 150), Color.BLACK);

		backButton = new GameButton(AppPanel.WIDTH / 2 - 100, yPos, 200, buttonHeight, "EXIT TUTORIAL",
				() -> backToMenu(), new Color(150, 80, 80), Color.BLACK);
	}

	@Override
	public void draw(Graphics2D g2) {
		// Dark background
		g2.setColor(new Color(20, 20, 40, 240));
		g2.fillRect(0, 0, AppPanel.WIDTH, AppPanel.HEIGHT);

		// Title
		g2.setFont(new Font("Malgun Gothic", Font.BOLD, 48));
		g2.setColor(new Color(100, 220, 255));
		String title = pages.get(currentPage).getTitle();
		FontMetrics fm = g2.getFontMetrics();
		int titleX = (AppPanel.WIDTH - fm.stringWidth(title)) / 2;
		g2.drawString(title, titleX, 80);

		// Page indicator
		g2.setFont(new Font("Malgun Gothic", Font.PLAIN, 20));
		g2.setColor(new Color(150, 150, 200));
		String pageIndicator = "Page " + (currentPage + 1) + " of " + pages.size();
		int pageX = (AppPanel.WIDTH - fm.stringWidth(pageIndicator)) / 2;
		g2.drawString(pageIndicator, pageX, 120);

		// Content lines
		g2.setFont(new Font("Malgun Gothic", Font.PLAIN, 24));
		g2.setColor(Color.WHITE);
		String[] lines = pages.get(currentPage).getContent();
		int startY = 200;
		int lineHeight = 45;

		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			if (line.isEmpty()) {
				continue; // Skip empty lines in spacing
			}
			int y = startY + (i * lineHeight);
			if (y > AppPanel.HEIGHT - 150) {
				break; // Don't draw over buttons
			}
			g2.drawString(line, 80, y);
		}

		// Draw buttons
		prevButton.draw(g2);
		nextButton.draw(g2);
		backButton.draw(g2);

		// Bottom indicator
		g2.setFont(new Font("Malgun Gothic", Font.ITALIC, 18));
		g2.setColor(new Color(150, 150, 150));
		String hint = "Use arrow keys or buttons to navigate";
		int hintX = (AppPanel.WIDTH - g2.getFontMetrics().stringWidth(hint)) / 2;
		g2.drawString(hint, hintX, AppPanel.HEIGHT - 70);
	}

	@Override
	public void upadate() {
		prevButton.update();
		nextButton.update();
		backButton.update();

		// Keyboard navigation
		if (gameObj.getKeyH().up || gameObj.getKeyH().right) {
			previousPage();
			gameObj.getKeyH().up = false;
			gameObj.getKeyH().right = false;
		}
		if (gameObj.getKeyH().down || gameObj.getKeyH().left) {
			nextPage();
			gameObj.getKeyH().down = false;
			gameObj.getKeyH().left = false;
		}
	}

	private void nextPage() {
		if (currentPage < pages.size() - 1) {
			currentPage++;
		}
	}

	private void previousPage() {
		if (currentPage > 0) {
			currentPage--;
		}
	}

	private void backToMenu() {
		gameObj.setState(gameObj.getStateMenu());
	}

	// Inner class to represent a tutorial page
	private static class TutorialPage {
		private String title;
		private String[] content;

		public TutorialPage(String title, String[] content) {
			this.title = title;
			this.content = content;
		}

		public String getTitle() {
			return title;
		}

		public String[] getContent() {
			return content;
		}
	}
}
