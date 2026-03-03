package fr.iutlittoral.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * MenuRenderer - Handles all menu display logic
 * Renders a beautiful main menu and game over screen
 */
public class MenuRenderer {

    private static final Color BACKGROUND_DARK = Color.web("#0a0e27");
    private static final Color BACKGROUND_GRADIENT_TOP = Color.web("#232a4d");
    private static final Color BACKGROUND_GRADIENT_BOTTOM = Color.web("#0a0e27");
    private static final Color ACCENT_YELLOW = Color.web("#ffd700");
    private static final Color TEXT_WHITE = Color.web("#ffffff");
    private static final Color TEXT_GRAY = Color.web("#cccccc");

    /**
     * Render the main menu
     * 
     * @param canvas        Canvas to render on
     * @param menuSelection Current selected option (0 = Play, 1 = Quit)
     */
    public static void renderMainMenu(Canvas canvas, int menuSelection) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.save();

        // Background – vertical two‑tone gradient
        gc.setFill(BACKGROUND_GRADIENT_TOP);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight() / 2);
        gc.setFill(BACKGROUND_GRADIENT_BOTTOM);
        gc.fillRect(0, canvas.getHeight() / 2, canvas.getWidth(), canvas.getHeight() / 2);

        // Title with shadow
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 120));
        gc.setFill(Color.web("#222222", 0.6));
        gc.fillText("NODEBUSTER", canvas.getWidth() / 2 - 445, 185); // shadow
        gc.setFill(ACCENT_YELLOW);
        gc.fillText("NODEBUSTER", canvas.getWidth() / 2 - 450, 180);

        // Decorative line
        gc.setStroke(ACCENT_YELLOW);
        gc.setLineWidth(3);
        gc.strokeLine(canvas.getWidth() / 2 - 500, 220, canvas.getWidth() / 2 + 500, 220);

        // Menu options
        double startY = 320;
        double spacing = 130;

        // option JOUER
        drawMenuOption(gc, "JOUER", canvas.getWidth() / 2, startY, menuSelection == 0);

        // option INSTRUCTIONS
        drawMenuOption(gc, "EXPLICATION", canvas.getWidth() / 2, startY + spacing, menuSelection == 1);

        // option QUITTER
        drawMenuOption(gc, "QUITTER", canvas.getWidth() / 2, startY + spacing * 2, menuSelection == 2);

        // consigne en bas
        gc.setFont(Font.font("Arial", 20));
        gc.setFill(TEXT_GRAY);
        gc.fillText("↑ ↓ Se déplacer  |  ENTRÉE Sélectionner", canvas.getWidth() / 2 - 200, canvas.getHeight() - 50);

        gc.restore();
    }

    /**
     * Render the instructions/story screen
     */
    public static void renderInstructions(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.save();

        // Background gradient
        gc.setFill(BACKGROUND_GRADIENT_TOP);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight() / 2);
        gc.setFill(BACKGROUND_GRADIENT_BOTTOM);
        gc.fillRect(0, canvas.getHeight() / 2, canvas.getWidth(), canvas.getHeight() / 2);

        // Titre
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 80));
        gc.setFill(ACCENT_YELLOW);
        gc.fillText("À PROPOS DU JEU", canvas.getWidth() / 2 - 350, 100);

        // ligne décorative
        gc.setStroke(ACCENT_YELLOW);
        gc.setLineWidth(2);
        gc.strokeLine(canvas.getWidth() / 2 - 400, 130, canvas.getWidth() / 2 + 400, 130);

        // Histoire
        gc.setFont(Font.font("Arial", 18));
        gc.setFill(TEXT_WHITE);
        double y = 180;
        double lineHeight = 30;

        drawWrappedText(gc, "Un VIRUS dangereux a infecté votre borne d'arcade ! Des ronds violets",
                canvas.getWidth() / 2 - 600, y, 1200);
        y += lineHeight;
        drawWrappedText(gc, "se répandent dans le système. Détruisez les boîtes colorées pour nettoyer",
                canvas.getWidth() / 2 - 600, y, 1200);
        y += lineHeight;
        drawWrappedText(gc, "la borne, mais ÉVITEZ les nœuds viraux violets ou vous perdrez des points !",
                canvas.getWidth() / 2 - 600, y, 1200);

        y += lineHeight * 2;
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        gc.setFill(ACCENT_YELLOW);
        gc.fillText("RÈGLES DU JEU :", canvas.getWidth() / 2 - 600, y);

        y += lineHeight * 1.5;
        gc.setFont(Font.font("Arial", 18));
        gc.setFill(TEXT_WHITE);
        drawWrappedText(gc, "• Boîtes DORÉES : +1 point | Boîtes ROUGE FONCÉ : +2 points", canvas.getWidth() / 2 - 600,
                y,
                1200);
        y += lineHeight;
        drawWrappedText(gc, "• Slimes CYAN : +3 points (se divisent en 4 boîtes lorsqu'on les touche)",
                canvas.getWidth() / 2 - 600, y, 1200);
        y += lineHeight;
        drawWrappedText(gc, "• Cercles VIRUS VIOLETS : -10 points (taille aléatoire, évitez-les !)",
                canvas.getWidth() / 2 - 600,
                y, 1200);
        y += lineHeight * 1.5;
        drawWrappedText(gc, "• Objectif : atteindre 300 points en 60 secondes pour GAGNER !",
                canvas.getWidth() / 2 - 600, y, 1200);
        y += lineHeight;
        drawWrappedText(gc, "• Minimum 100 points pour ne pas perdre au temps écoulé.", canvas.getWidth() / 2 - 600, y,
                1200);

        // Consigne retour
        gc.setFont(Font.font("Arial", 20));
        gc.setFill(TEXT_GRAY);
        gc.fillText("Appuyez sur ENTRÉE pour retourner au menu", canvas.getWidth() / 2 - 200, canvas.getHeight() - 50);

        gc.restore();
    }

    /**
     * Helper to draw text with word wrapping
     */
    private static void drawWrappedText(GraphicsContext gc, String text, double x, double y, double maxWidth) {
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            String test = line.toString().isEmpty() ? word : line.toString() + " " + word;
            if (gc.getFont().getSize() * test.length() * 0.5 > maxWidth && !line.toString().isEmpty()) {
                gc.fillText(line.toString(), x, y);
                y += 25;
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(test);
            }
        }
        if (!line.toString().isEmpty()) {
            gc.fillText(line.toString(), x, y);
        }
    }

    /**
     * Render the game over / win screen with menu
     * 
     * @param canvas        Canvas to render on
     * @param won           True if player won, false if lost
     * @param menuSelection Current selected option
     */
    public static void renderGameOverMenu(Canvas canvas, boolean won, int menuSelection) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.save();

        // Background gradient
        gc.setFill(BACKGROUND_GRADIENT_TOP);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight() / 2);
        gc.setFill(BACKGROUND_GRADIENT_BOTTOM);
        gc.fillRect(0, canvas.getHeight() / 2, canvas.getWidth(), canvas.getHeight() / 2);

        // Title with shadow
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 100));
        if (won) {
            gc.setFill(Color.web("#222222", 0.6));
            gc.fillText("VOUS AVEZ GAGNÉ !", canvas.getWidth() / 2 - 345, 185);
            gc.setFill(Color.web("#00ff00"));
            gc.fillText("VOUS AVEZ GAGNÉ !", canvas.getWidth() / 2 - 350, 180);
        } else {
            gc.setFill(Color.web("#222222", 0.6));
            gc.fillText("JEU TERMINÉ", canvas.getWidth() / 2 - 295, 185);
            gc.setFill(Color.web("#ff3333"));
            gc.fillText("JEU TERMINÉ", canvas.getWidth() / 2 - 300, 180);
        }

        // Decorative line
        gc.setStroke(won ? Color.web("#00ff00") : Color.web("#ff3333"));
        gc.setLineWidth(3);
        gc.strokeLine(canvas.getWidth() / 2 - 400, 220, canvas.getWidth() / 2 + 400, 220);

        // Menu options
        double startY = 350;
        double spacing = 140;

        // option REJOUER
        drawMenuOption(gc, "REJOUER", canvas.getWidth() / 2, startY, menuSelection == 0);

        // option QUITTER
        drawMenuOption(gc, "QUITTER", canvas.getWidth() / 2, startY + spacing, menuSelection == 1);

        // consigne en bas
        gc.setFont(Font.font("Arial", 20));
        gc.setFill(TEXT_GRAY);
        gc.fillText("↑ ↓ Se déplacer  |  ENTRÉE Sélectionner", canvas.getWidth() / 2 - 200, canvas.getHeight() - 50);

        gc.restore();
    }

    /**
     * Draw a single menu option with highlight
     */
    private static void drawMenuOption(GraphicsContext gc, String text, double centerX, double y, boolean selected) {
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 70));

        if (selected) {
            // pulsing highlight box
            double time = System.currentTimeMillis() % 1200;
            double alpha = 0.6 + 0.4 * Math.sin(time / 150.0);
            gc.setFill(Color.web("#ffd700", alpha));
            gc.fillRect(centerX - 250, y - 70, 500, 100);

            // text with shadow on highlighted background
            gc.setFill(Color.web("#222222", 0.5));
            drawCenteredText(gc, text, centerX, y + 4);
            gc.setFill(BACKGROUND_DARK);
            drawCenteredText(gc, text, centerX, y);

            // animated border
            gc.setStroke(ACCENT_YELLOW);
            gc.setLineWidth(4);
            gc.strokeRect(centerX - 250, y - 70, 500, 100);
        } else {
            // regular option
            gc.setFill(Color.web("#222222", 0.5));
            drawCenteredText(gc, text, centerX, y + 3);
            gc.setFill(TEXT_WHITE);
            drawCenteredText(gc, text, centerX, y);

            gc.setStroke(TEXT_WHITE);
            gc.setLineWidth(2);
            gc.strokeRect(centerX - 250, y - 70, 500, 100);
        }
    }

    /**
     * Helper to draw centered text
     */
    private static void drawCenteredText(GraphicsContext gc, String text, double centerX, double y) {
        gc.fillText(text, centerX - text.length() * 20, y + 20);
    }
}
