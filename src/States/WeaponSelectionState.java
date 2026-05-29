package States;

import java.awt.*;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import Open.Weapons.*;
import main.AppPanel;
import main.GameButton;
import main.GameObject;
import main.enums.WeaponTypes;

public class WeaponSelectionState extends BaseState {

    private EnumMap<WeaponTypes, Weapon> allWeapons;
    private List<GameButton> weaponButtons;
    private long timer = 0;

    // Layout configuration variables
    private final int BUTTON_WIDTH = 300;
    private final int BUTTON_HEIGHT = 120;
    private final int COLUMNS = 4;
    private final int GAP = 40;

    public WeaponSelectionState(GameObject gameObj) {
        super(gameObj);
        this.weaponButtons = new ArrayList<>();
        initWeapons();
        createButtons();
    }

    private void initWeapons() {
        allWeapons = new EnumMap<>(WeaponTypes.class);
        allWeapons.put(WeaponTypes.Aura, new AuraWeapon(gameObj));
        allWeapons.put(WeaponTypes.Banana, new BananaWeapon(gameObj));
        allWeapons.put(WeaponTypes.Bone, new BoneWeapon(gameObj));
        allWeapons.put(WeaponTypes.FireStaff, new FireStaffWeapon(gameObj));
        allWeapons.put(WeaponTypes.PewPew, new PewPewWeapon(gameObj));
        allWeapons.put(WeaponTypes.Sword, new SwordWeapon(gameObj));
        allWeapons.put(WeaponTypes.Katana, new KatanaWeapon(gameObj));
        allWeapons.put(WeaponTypes.Dexecutioner, new DexecutionerWeapon(gameObj));
    }

    private void createButtons() {
        int totalWidth = (COLUMNS * BUTTON_WIDTH) + ((COLUMNS - 1) * GAP);
        int startX = (AppPanel.WIDTH - totalWidth) / 2;
        int startY = 350; // Leave room for title headers

        int index = 0;
        for (WeaponTypes type : allWeapons.keySet()) {
            int col = index % COLUMNS;
            int row = index / COLUMNS;

            int btnX = startX + (col * (BUTTON_WIDTH + GAP));
            int btnY = startY + (row * (BUTTON_HEIGHT + GAP));

            Weapon selectedWeapon = allWeapons.get(type);
            String cleanName = formatName(type.name());

            GameButton btn = new GameButton(btnX, btnY, BUTTON_WIDTH, BUTTON_HEIGHT, cleanName, () -> {
                // Give player the chosen weapon
                gameObj.getPlayer().addWeapon(type, selectedWeapon);
                
                // Transition out into your active game state loop
                gameObj.setState(gameObj.getStateOpen()); 
            });

            btn.setImage(selectedWeapon.getIcon());
            btn.setTransparent(true);
            weaponButtons.add(btn);

            index++;
        }
    }

    @Override
    public void upadate() { // Matches the spelling typo from your BaseState abstract base!
        timer++;
        for (GameButton btn : weaponButtons) {
            if (btn != null) {
                btn.update();
            }
        }
    }

    @Override
    public void draw(Graphics2D g) {
        // Draw Dark Background Overlay
        g.setColor(new Color(15, 15, 25));
        g.fillRect(0, 0, AppPanel.WIDTH, AppPanel.HEIGHT);

        // Ambient Background Energy Beam Effects (repurposed from your Upgrades menu art)
        g.setColor(new Color(147, 50, 230, 15)); 
        for (int i = 0; i < 8; i++) {
            double angle = (timer * 0.01) + (i * Math.PI / 4);
            int[] xPoints = { AppPanel.WIDTH / 2, AppPanel.WIDTH / 2 + (int) (Math.cos(angle - 0.15) * 1600),
                    AppPanel.WIDTH / 2 + (int) (Math.cos(angle + 0.15) * 1600) };
            int[] yPoints = { AppPanel.HEIGHT / 2, AppPanel.HEIGHT / 2 + (int) (Math.sin(angle - 0.15) * 1600),
                    AppPanel.HEIGHT / 2 + (int) (Math.sin(angle + 0.15) * 1600) };
            g.fillPolygon(xPoints, yPoints, 3);
        }

        // Draw Title text 
        g.setFont(new Font("Monospaced", Font.BOLD, 65));
        String title = "CHOOSE YOUR WEAPON";
        int xTitle = AppPanel.WIDTH / 2 - g.getFontMetrics().stringWidth(title) / 2;
        g.setColor(Color.BLACK);
        g.drawString(title, xTitle + 5, 165);
        g.setColor(new Color(255, 90, 50));
        g.drawString(title, xTitle, 160);

        // Subtitle instructions
        g.setFont(new Font("Monospaced", Font.PLAIN, 24));
        String sub = "Select a starting tool of destruction to enter the field.";
        int xSub = AppPanel.WIDTH / 2 - g.getFontMetrics().stringWidth(sub) / 2;
        g.setColor(Color.LIGHT_GRAY);
        g.drawString(sub, xSub, 220);

        // Draw individual interactive weapon boxes
        for (GameButton btn : weaponButtons) {
            if (btn == null) continue;

            // Retro gradient dark background styling matching your engine theme
            Color baseColor = btn.isHovering() ? new Color(65, 35, 110) : new Color(30, 30, 45);
            int pixelSize = 6;
            int halfHeight = btn.getHeight() / 2;

            for (int py = 0; py < btn.getHeight(); py += pixelSize) {
                float dist = Math.abs(py - halfHeight) / (float) halfHeight;
                float shading = 0.6f + (dist * 0.4f);

                int r = (int) (baseColor.getRed() * Math.min(1, shading));
                int gr = (int) (baseColor.getGreen() * Math.min(1, shading));
                int bl = (int) (baseColor.getBlue() * Math.min(1, shading));

                g.setColor(new Color(r, gr, bl));
                g.fillRect(btn.getX(), btn.getY() + py, btn.getWidth(), pixelSize);
            }

            // Draw clean bounding borders around options
            g.setColor(btn.isHovering() ? Color.YELLOW : new Color(75, 75, 100));
            g.setStroke(new BasicStroke(3));
            g.drawRect(btn.getX(), btn.getY(), btn.getWidth(), btn.getHeight());

            btn.draw(g);
        }
    }

    private String formatName(String name) {
        String[] words = name.split("_");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            sb.append(w.substring(0, 1).toUpperCase()).append(w.substring(1).toLowerCase()).append(" ");
        }
        return sb.toString().trim();
    }
}