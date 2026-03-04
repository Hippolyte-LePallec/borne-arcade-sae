package fr.iutlittoral.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Interface de saisie du nom pour enregistrer un highscore.
 * Permet au joueur d'entrer son nom (3 caractères) après avoir atteint un
 * highscore.
 */
public class HighScoreNameInputRenderer {

    private static final Color BACKGROUND_DARK = Color.web("#0a0e27");
    private static final Color ACCENT_YELLOW = Color.web("#ffd700");
    private static final Color TEXT_WHITE = Color.web("#ffffff");
    private static final Color TEXT_GRAY = Color.web("#cccccc");

    private char[] chars = { 'A', ' ', ' ' };
    private int currentIndex = 0;
    private boolean validating = false;

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ .";

    public HighScoreNameInputRenderer() {
        this.chars[0] = 'A';
        this.chars[1] = ' ';
        this.chars[2] = ' ';
        this.currentIndex = 0;
    }

    /**
     * Affiche l'interface de saisie du nom
     */
    public void render(Canvas canvas, int score, int position, String previousPlayer, int previousScore,
            String nextPlayer, int nextScore) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.save();

        // Background
        gc.setFill(BACKGROUND_DARK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Title
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 80));
        gc.setFill(ACCENT_YELLOW);
        gc.fillText("HIGHSCORE !", canvas.getWidth() / 2 - 250, 100);

        // Score achieved
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 50));
        gc.setFill(TEXT_WHITE);
        gc.fillText("Score : " + score, canvas.getWidth() / 2 - 150, 200);

        // Position
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        gc.setFill(ACCENT_YELLOW);
        String positionText = getPositionText(position);
        gc.fillText("Position : " + positionText, canvas.getWidth() / 2 - 180, 270);

        // "Enter your name" instruction
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 35));
        gc.setFill(TEXT_WHITE);
        gc.fillText("Entrez votre nom", canvas.getWidth() / 2 - 200, 380);

        // Character input boxes
        double boxX = canvas.getWidth() / 2 - 200;
        double boxY = 420;
        double boxSize = 80;
        double spacing = 100;

        for (int i = 0; i < 3; i++) {
            drawCharacterBox(gc, chars[i], boxX + i * spacing, boxY, boxSize, i == currentIndex);
        }

        // Draw validation button
        drawValidationButton(gc, canvas.getWidth() / 2 + 200, boxY, validating);

        // Context: previous score
        if (!previousPlayer.isEmpty()) {
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 25));
            gc.setFill(TEXT_GRAY);
            gc.fillText("Dessus : " + previousPlayer + " - " + previousScore, canvas.getWidth() / 2 - 250, 600);
        }

        // Context: next score
        if (!nextPlayer.isEmpty()) {
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 25));
            gc.setFill(TEXT_GRAY);
            gc.fillText("Dessous : " + nextPlayer + " - " + nextScore, canvas.getWidth() / 2 - 250, 650);
        }

        // Instructions
        gc.setFont(Font.font("Arial", 18));
        gc.setFill(TEXT_GRAY);
        gc.fillText("↑ ↓ : Changer lettre  |  ← → : Naviguer  |  Z/ENTRÉE : Valider",
                canvas.getWidth() / 2 - 400, canvas.getHeight() - 50);

        gc.restore();
    }

    /**
     * Traite les entrées clavier avec capture d'état précédent
     */
    public void handleKeyPress(boolean rightPressed, boolean rightPrevious,
            boolean leftPressed, boolean leftPrevious,
            boolean upPressed, boolean upPrevious,
            boolean downPressed, boolean downPrevious) {
        // Navigation avec RIGHT
        if (rightPressed && !rightPrevious) {
            if (validating) {
                // Déjà sur OK
            } else if (currentIndex < 2) {
                currentIndex++;
            } else {
                validating = true;
            }
        }
        // Navigation avec LEFT
        else if (leftPressed && !leftPrevious) {
            if (validating) {
                validating = false;
            } else if (currentIndex > 0) {
                currentIndex--;
            }
        }

        // Changer les caractères sur pression (détection front montant)
        if (!validating) {
            if (upPressed && !upPrevious) {
                chars[currentIndex] = getNextChar(chars[currentIndex]);
            } else if (downPressed && !downPrevious) {
                chars[currentIndex] = getPreviousChar(chars[currentIndex]);
            }
        }
    }

    /**
     * Vérifie si le joueur a validé son nom
     */
    public boolean isValidated() {
        return validating;
    }

    /**
     * Retourne le nom saisi
     */
    public String getPlayerName() {
        return "" + chars[0] + chars[1] + chars[2];
    }

    /**
     * Réinitialise l'interface
     */
    public void reset() {
        chars[0] = 'A';
        chars[1] = ' ';
        chars[2] = ' ';
        currentIndex = 0;
        validating = false;
    }

    private void drawCharacterBox(GraphicsContext gc, char character, double x, double y, double size,
            boolean selected) {
        if (selected) {
            // Boîte sélectionnée - surbrillance pulsante
            double time = System.currentTimeMillis() % 1200;
            double alpha = 0.6 + 0.4 * Math.sin(time / 150.0);
            gc.setFill(Color.web("#ffd700", alpha));
            gc.fillRect(x, y, size, size);

            gc.setStroke(ACCENT_YELLOW);
            gc.setLineWidth(4);
        } else {
            gc.setFill(Color.web("#333333"));
            gc.fillRect(x, y, size, size);

            gc.setStroke(TEXT_WHITE);
            gc.setLineWidth(2);
        }

        gc.strokeRect(x, y, size, size);

        // Caractère
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 50));
        gc.setFill(TEXT_WHITE);
        gc.fillText(String.valueOf(character), x + size / 2 - 15, y + size / 2 + 20);
    }

    private void drawValidationButton(GraphicsContext gc, double x, double y, boolean selected) {
        double size = 80;

        if (selected) {
            // Bouton sélectionné - surbrillance pulsante
            double time = System.currentTimeMillis() % 1200;
            double alpha = 0.6 + 0.4 * Math.sin(time / 150.0);
            gc.setFill(Color.web("#00ff00", alpha));
            gc.fillRect(x - size / 2, y, size, size);

            gc.setStroke(Color.web("#00ff00"));
            gc.setLineWidth(4);
        } else {
            gc.setFill(Color.web("#333333"));
            gc.fillRect(x - size / 2, y, size, size);

            gc.setStroke(TEXT_WHITE);
            gc.setLineWidth(2);
        }

        gc.strokeRect(x - size / 2, y, size, size);

        // Texte "OK"
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        gc.setFill(TEXT_WHITE);
        gc.fillText("OK", x - 20, y + 50);
    }

    private char getNextChar(char current) {
        int index = ALPHABET.indexOf(current);
        if (index == -1)
            index = 0;
        return ALPHABET.charAt((index + 1) % ALPHABET.length());
    }

    private char getPreviousChar(char current) {
        int index = ALPHABET.indexOf(current);
        if (index == -1)
            index = 0;
        int newIndex = (index - 1 + ALPHABET.length()) % ALPHABET.length();
        return ALPHABET.charAt(newIndex);
    }

    private String getPositionText(int position) {
        if (position == 0)
            return "1er";
        else if (position == 1)
            return "2ème";
        else if (position == 2)
            return "3ème";
        else
            return (position + 1) + "ème";
    }
}
