package main;

import java.io.*;

public class ScoreManager {
    private static final String FILE_NAME = "kills.txt";

    public static int loadHighScore() {
        File file = new File(FILE_NAME);

        if (!file.exists()) {
            saveHighScore(0);
            return 0;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            return Integer.parseInt(br.readLine());
        } catch (Exception e) {
            return 0;
        }
    }

    public static void saveHighScore(int score) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME))) {
            bw.write(String.valueOf(score));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void checkAndUpdateHighScore(int score) {
        int highScore = loadHighScore();
        if (score > highScore) {
            saveHighScore(score);
        }
    }
}
