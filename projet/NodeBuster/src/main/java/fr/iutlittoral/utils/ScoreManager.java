package fr.iutlittoral.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

public class ScoreManager {
    public static class ScoreLine implements Comparable<ScoreLine> {
        public String playerName;
        public int score;

        public ScoreLine(String playerName, int score) {
            this.playerName = playerName;
            this.score = score;
        }

        @Override
        public int compareTo(ScoreLine o) {
            return Integer.compare(o.score, this.score);
        }
    }

    public static ArrayList<ScoreLine> loadScores(String path) {
        ArrayList<ScoreLine> scores = new ArrayList<>();
        File file = new File(path);
        if (!file.exists())
            return scores;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    scores.add(new ScoreLine(parts[0], Integer.parseInt(parts[1])));
                }
            }
        } catch (Exception e) {
        }
        Collections.sort(scores);
        return scores;
    }

    public static int saveScore(String path, String name, int score) {
        ArrayList<ScoreLine> scores = loadScores(path);
        ScoreLine newEntry = new ScoreLine(name, score);
        scores.add(newEntry);
        Collections.sort(scores);
        if (scores.size() > 10)
            scores = new ArrayList<>(scores.subList(0, 10));

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            for (ScoreLine s : scores) {
                bw.write(s.playerName + ":" + s.score);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return scores.indexOf(newEntry);
    }

    public static int forceSaveScore(String path, String name, int score) {
        return saveScore(path, name, score);
    }
}