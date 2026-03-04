package fr.iutlittoral.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Gestionnaire de scores pour le jeu NodeBuster.
 * 
 * Permet de :
 * - Sauvegarder les scores dans un fichier
 * - Charger les scores existants
 * - Gérer le classement des highscores (top 10)
 * - Déterminer si un score est qualifiant
 */
public class ScoreManager {

    public static class ScoreLine {
        public String playerName;
        public int score;
        public long timestamp;

        public ScoreLine(String playerName, int score, long timestamp) {
            this.playerName = playerName;
            this.score = score;
            this.timestamp = timestamp;
        }

        public ScoreLine(String line) {
            // Format: "NAME:SCORE:TIMESTAMP"
            String[] parts = line.split(":");
            if (parts.length >= 2) {
                this.playerName = parts[0];
                this.score = Integer.parseInt(parts[1]);
                this.timestamp = parts.length > 2 ? Long.parseLong(parts[2]) : System.currentTimeMillis();
            }
        }

        @Override
        public String toString() {
            return playerName + ":" + score + ":" + timestamp;
        }
    }

    private static final String DEFAULT_SCORE_FILE = "nodebusters_scores.txt";
    private static final int MAX_SCORES = 10;

    /**
     * Charge tous les scores depuis le fichier de highscores.
     */
    public static ArrayList<ScoreLine> loadScores(String filePath) {
        ArrayList<ScoreLine> scores = new ArrayList<>();

        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return scores;
            }

            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    try {
                        scores.add(new ScoreLine(line));
                    } catch (Exception e) {
                        System.err.println("Erreur lors du parsing de la ligne: " + line);
                    }
                }
            }
            reader.close();

            // Trier par score décroissant
            Collections.sort(scores, new Comparator<ScoreLine>() {
                @Override
                public int compare(ScoreLine a, ScoreLine b) {
                    return Integer.compare(b.score, a.score);
                }
            });

        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture du fichier de scores: " + e.getMessage());
        }

        return scores;
    }

    /**
     * Sauvegarde un nouveau score et retourne sa position dans le classement.
     * Retourne -1 si le score n'est pas qualifiant.
     */
    public static int saveScore(String filePath, String playerName, int score) {
        ArrayList<ScoreLine> scores = loadScores(filePath);

        // Déterminer la position du nouveau score
        int position = -1;
        for (int i = 0; i < scores.size(); i++) {
            if (score > scores.get(i).score) {
                position = i;
                break;
            }
        }

        // Si pas trouvé et il y a moins de 10 scores
        if (position == -1 && scores.size() < MAX_SCORES) {
            position = scores.size();
        }

        // Score non qualifiant
        if (position == -1 || position >= MAX_SCORES) {
            return -1;
        }

        // Insérer le nouveau score
        scores.add(position, new ScoreLine(playerName, score, System.currentTimeMillis()));

        // Garder seulement les 10 meilleurs
        while (scores.size() > MAX_SCORES) {
            scores.remove(scores.size() - 1);
        }

        // Sauvegarder dans le fichier
        try {
            File file = new File(filePath);
            file.getParentFile().mkdirs(); // Créer les répertoires s'ils n'existent pas

            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            for (int i = 0; i < scores.size(); i++) {
                writer.write(scores.get(i).toString());
                if (i < scores.size() - 1) {
                    writer.write("\n");
                }
            }
            writer.close();
        } catch (Exception e) {
            System.err.println("Erreur lors de la sauvegarde des scores: " + e.getMessage());
            return -1;
        }

        return position;
    }

    /**
     * Retourne le score à atteindre pour la victoire.
     */
    public static int getWinScore() {
        return 150;
    }

    /**
     * Retourne le score minimum requis pour ne pas perdre.
     */
    public static int getMinScore() {
        return 100;
    }

    /**
     * Retourne la limite de temps en secondes.
     */
    public static long getTimeLimit() {
        return 60;
    }

    /**
     * Charge les 10 meilleurs scores.
     */
    public static ArrayList<ScoreLine> getTopScores(String filePath) {
        return loadScores(filePath);
    }

    /**
     * Sauvegarde un score en forçant son insertion même s'il n'est pas qualifiant.
     * Renvoie la position dans le classement après tri (0..MAX_SCORES-1).
     */
    public static int forceSaveScore(String filePath, String playerName, int score) {
        ArrayList<ScoreLine> scores = loadScores(filePath);
        scores.add(new ScoreLine(playerName, score, System.currentTimeMillis()));
        // tri décroissant
        Collections.sort(scores, new Comparator<ScoreLine>() {
            @Override
            public int compare(ScoreLine a, ScoreLine b) {
                return Integer.compare(b.score, a.score);
            }
        });
        // trouver position
        int position = scores.indexOf(scores.stream().filter(s -> s.playerName.equals(playerName) && s.score == score).findFirst().orElse(null));
        // tronquer
        while (scores.size() > MAX_SCORES) {
            scores.remove(scores.size() - 1);
        }
        // sauvegarde
        try {
            File file = new File(filePath);
            file.getParentFile().mkdirs();
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            for (int i = 0; i < scores.size(); i++) {
                writer.write(scores.get(i).toString());
                if (i < scores.size() - 1) {
                    writer.write("\n");
                }
            }
            writer.close();
        } catch (Exception e) {
            System.err.println("Erreur lors de la sauvegarde des scores: " + e.getMessage());
            return -1;
        }
        return position;
    }
}
