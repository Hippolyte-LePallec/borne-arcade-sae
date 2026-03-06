package fr.iutlittoral;

import fr.iutlittoral.components.Spawner;
import fr.iutlittoral.components.Target;
import fr.iutlittoral.components.spawntypes.MovingBoxSpawnType;
import fr.iutlittoral.components.spawntypes.SimpleBoxSpawnType;
import fr.iutlittoral.components.spawntypes.PenaltySpawnType;
import fr.iutlittoral.components.spawntypes.HourglassSpawnType;
import fr.iutlittoral.events.TargetDestroyed;
import fr.iutlittoral.systems.*;
import fr.iutlittoral.systems.spawners.MovingboxSpawnerSystem;
import fr.iutlittoral.systems.spawners.SimpleBoxSpawnerSystem;
import fr.iutlittoral.systems.spawners.SlimeBoxSpawnerSystem;
import fr.iutlittoral.systems.spawners.PenaltySpawnerSystem;
import fr.iutlittoral.systems.spawners.HourglassSpawnerSystem;
import fr.iutlittoral.ui.MenuRenderer;
import fr.iutlittoral.ui.HighScoreNameInputRenderer;
import fr.iutlittoral.systems.Score;
import fr.iutlittoral.utils.*;
import fr.iutlittoral.components.Cursor;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.signals.Signal;
import java.util.ArrayList;

public class App extends Application {

    enum GameState {
        MENU, HIGHSCORES, INSTRUCTIONS, PLAYING, GAME_OVER, ENTERING_NAME
    }

    private double cursorX;
    private double cursorY;
    private fr.iutlittoral.utils.Keyboard keyboard;
    private GameState gameState = GameState.MENU;
    private boolean gameOver = false;
    private boolean gameWon = false;
    private int menuSelection = 0;
    private boolean enterPrev = false;
    private boolean upPrev = false;
    private boolean downPrev = false;
    private static final int INITIAL_TIME_SECONDS = 30;
    private static final String SCORES_FILE = "nodebusters_scores.txt";
    // CURSOR: size is defined in Cursor.SIZE to avoid sprinkling magic numbers
    private long gameStartTimeMs = 0;
    private float remainingTimeSeconds = INITIAL_TIME_SECONDS; // Temps dynamique qui change avec les sabliers
    private Canvas canvas;
    private Font font;
    private Mouse mouse;
    private Engine world;
    private Score score;
    private EntityCreator creator;
    private GameLoopTimer gameplayTimer;
    private HighScoreNameInputRenderer nameInputRenderer;
    private int finalScore;
    private int scorePosition;
    private boolean leftPrev = false;
    private boolean rightPrev = false;
    private boolean upPrevInput = false;
    private boolean downPrevInput = false;
    private HourglassListener hourglassListener;
    private Score scoreSystem = new Score();

    @Override
    public void start(Stage stage) {
        canvas = new Canvas(1600, 900);
        var stack = new StackPane(canvas);
        var scene = new Scene(stack, 1600, 900);
        canvas.widthProperty().bind(scene.widthProperty());
        canvas.heightProperty().bind(scene.heightProperty());
        stage.setScene(scene);
        scene.setCursor(javafx.scene.Cursor.NONE);
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
        stage.setAlwaysOnTop(true);
        stage.show();
        stage.toFront();

        font = new Font("Vera.ttf", 25);
        keyboard = new fr.iutlittoral.utils.Keyboard(scene);
        mouse = new Mouse(canvas);
        cursorX = canvas.getWidth() / 2;
        cursorY = canvas.getHeight() / 2;

        // Initialiser le renderer de saisie de nom
        nameInputRenderer = new HighScoreNameInputRenderer();

        // Boucle du menu principal
        GameLoopTimer mainTimer = new GameLoopTimer() {
            @Override
            public void tick(float secondsSinceLastFrame) {
                if (gameState == GameState.MENU) {
                    MenuRenderer.renderMainMenu(canvas, menuSelection);
                    handleMenuInput();
                } else if (gameState == GameState.HIGHSCORES) {
                    // render highscores and handle input to return
                    MenuRenderer.renderHighScores(canvas, fr.iutlittoral.utils.ScoreManager.loadScores(SCORES_FILE));
                    handleHighscoresInput();
                } else if (gameState == GameState.INSTRUCTIONS) {
                    MenuRenderer.renderInstructions(canvas);
                    handleInstructionsInput();
                } else if (gameState == GameState.GAME_OVER) {
                    MenuRenderer.renderGameOverMenu(canvas, gameWon, menuSelection);
                    handleGameOverMenuInput();
                } else if (gameState == GameState.ENTERING_NAME) {
                    handleNameInput();
                }
            }
        };
        mainTimer.start();

        // Boucle de jeu
        gameplayTimer = new GameLoopTimer() {
            boolean zPrev = false;

            @Override
            public void tick(float secondsSinceLastFrame) {
                if (gameState != GameState.PLAYING) {
                    return;
                }

                if (gameStartTimeMs == 0) {
                    gameStartTimeMs = System.currentTimeMillis();
                    remainingTimeSeconds = INITIAL_TIME_SECONDS;
                }

                // Décrémenter le temps au fur et à mesure
                remainingTimeSeconds -= secondsSinceLastFrame;

                // Vérification de défaite: le temps arrive à zéro
                if (remainingTimeSeconds <= 0) {
                    gameOver = true;
                    gameWon = false; // Plus de condition de victoire
                    finalScore = score.getScore();

                    // Calculer la position du score
                    ArrayList<ScoreManager.ScoreLine> scores = ScoreManager.loadScores(SCORES_FILE);
                    scorePosition = scores.size(); // Par défaut, position en dernier
                    for (int i = 0; i < scores.size(); i++) {
                        if (finalScore > scores.get(i).score) {
                            scorePosition = i;
                            break;
                        }
                    }

                    // Réinitialiser le renderer de saisie de nom
                    nameInputRenderer = new HighScoreNameInputRenderer();

                    // Passer à l'écran d'enregistrement du nom
                    gameState = GameState.ENTERING_NAME;
                    return;
                }

                // Cursor movement (cursorX/Y represent the centre of the square)
                double speed = 400 * secondsSinceLastFrame;
                if (keyboard.isKeyPressed(KeyCode.LEFT))
                    cursorX -= speed;
                if (keyboard.isKeyPressed(KeyCode.RIGHT))
                    cursorX += speed;
                if (keyboard.isKeyPressed(KeyCode.UP))
                    cursorY -= speed;
                if (keyboard.isKeyPressed(KeyCode.DOWN))
                    cursorY += speed;

                // ensure cursor stays inside the window
                cursorX = Math.max(0, Math.min(cursorX, canvas.getWidth()));
                cursorY = Math.max(0, Math.min(cursorY, canvas.getHeight()));

                // Shooting: convert centre coords to bullet spawn coordinates
                boolean zNow = keyboard.isKeyPressed(KeyCode.Z);
                if (mouse.isJustPressed(MouseButton.PRIMARY) || (zNow && !zPrev)) {
                    creator.createBullet(cursorX, cursorY);
                    mouse.resetJustPressed();
                }
                zPrev = zNow;

                GraphicsContext gc = canvas.getGraphicsContext2D();
                gc.save();
                gc.setFill(Color.BLACK);
                gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
                gc.restore();

                // update game world (all systems including renderers run here)
                world.update(secondsSinceLastFrame);

                // Le score et le chronomètre sont dessinés après le monde pour rester au
                // premier plan
                // Le score est maintenu par l'écouteur Score qui s'incrémente
                // lorsque des événements TargetDestroyed sont envoyés par le système de
                // collision.
                // L'UI interroge simplement score.getScore() à chaque image.
                gc.save();
                gc.setFill(Color.WHITE);
                gc.setFont(font);
                gc.fillText("Score : " + score.getScore(), 10, 35);
                // Afficher le temps restant
                gc.fillText("Temps : " + String.format("%.1f", Math.max(0, remainingTimeSeconds)) + "s", 10, 65);
                gc.setFont(new Font("Vera.ttf", 16));
                gc.fillText("Collecte les sabliers pour ajouter du temps !", 10, 85);
                gc.setFont(font);

                // cursor render: draw square centered at current coordinates
                double halfSize = Cursor.SIZE / 2;
                gc.setStroke(Color.RED);
                gc.setLineWidth(2);
                gc.strokeRect(cursorX - halfSize, cursorY - halfSize, Cursor.SIZE, Cursor.SIZE);
                gc.restore();
            }
        };
        gameplayTimer.start();

    }

    /**
     * Ajoute du temps au compteur de jeu
     * Utilisé quand les sabliers sont détruits
     */
    public void addTimeBonus(int seconds) {
        if (gameState == GameState.PLAYING) {
            remainingTimeSeconds += seconds;
        }
    }

    private void handleMenuInput() {
        boolean upNow = keyboard.isKeyPressed(KeyCode.UP);
        boolean downNow = keyboard.isKeyPressed(KeyCode.DOWN);
        boolean enterNow = keyboard.isKeyPressed(KeyCode.ENTER) || keyboard.isKeyPressed(KeyCode.Z);

        if (upNow && !upPrev) {
            menuSelection = (menuSelection - 1 + 4) % 4; // 0=Play,1=Highscores,2=Instructions,3=Quit
        }
        if (downNow && !downPrev) {
            menuSelection = (menuSelection + 1) % 4;
        }
        if (enterNow && !enterPrev) {
            if (menuSelection == 0) {
                startNewGame();
            } else if (menuSelection == 1) {
                // show highscores
                gameState = GameState.HIGHSCORES;
            } else if (menuSelection == 2) {
                gameState = GameState.INSTRUCTIONS;
            } else {
                System.exit(0);
            }
        }

        upPrev = upNow;
        downPrev = downNow;
        enterPrev = enterNow;
    }

    private void handleInstructionsInput() {
        boolean enterNow = keyboard.isKeyPressed(KeyCode.ENTER) || keyboard.isKeyPressed(KeyCode.Z);
        if (enterNow && !enterPrev) {
            gameState = GameState.MENU;
            menuSelection = 0;
        }
        enterPrev = enterNow;
    }

    private void handleNameInput() {
        // Charger les scores pour afficher le contexte
        ArrayList<ScoreManager.ScoreLine> scores = ScoreManager.loadScores(SCORES_FILE);

        String previousPlayer = "";
        int previousScore = 0;
        if (scorePosition > 0 && scorePosition - 1 < scores.size()) {
            previousPlayer = scores.get(scorePosition - 1).playerName;
            previousScore = scores.get(scorePosition - 1).score;
        }

        String nextPlayer = "";
        int nextScore = 0;
        if (scorePosition < scores.size()) {
            nextPlayer = scores.get(scorePosition).playerName;
            nextScore = scores.get(scorePosition).score;
        }

        nameInputRenderer.render(canvas, finalScore, scorePosition, previousPlayer, previousScore, nextPlayer,
                nextScore);

        // Traiter les entrées
        boolean rightNow = keyboard.isKeyPressed(KeyCode.RIGHT);
        boolean leftNow = keyboard.isKeyPressed(KeyCode.LEFT);
        boolean upNow = keyboard.isKeyPressed(KeyCode.UP);
        boolean downNow = keyboard.isKeyPressed(KeyCode.DOWN);
        boolean enterNow = keyboard.isKeyPressed(KeyCode.ENTER) || keyboard.isKeyPressed(KeyCode.Z);

        // Traiter les changements de position
        nameInputRenderer.handleKeyPress(rightNow, rightPrev, leftNow, leftPrev, upNow, upPrevInput, downNow,
                downPrevInput);

        // Validation
        if ((enterNow && !enterPrev) || nameInputRenderer.isValidated()) {
            String playerName = nameInputRenderer.getPlayerName();
            int position = ScoreManager.saveScore(SCORES_FILE, playerName, finalScore);
            if (position == -1) {
                // on force l'enregistrement même si pas qualifiant
                position = ScoreManager.forceSaveScore(SCORES_FILE, playerName, finalScore);
            }

            // Créer le fichier highscore pour la borne
            createBorneScoreFile();

            // Retourner au menu
            gameState = GameState.MENU;
            menuSelection = 0;
            nameInputRenderer = null;
        }

        rightPrev = rightNow;
        leftPrev = leftNow;
        upPrevInput = upNow;
        downPrevInput = downNow;
        enterPrev = enterNow;
    }

    private void handleHighscoresInput() {
        boolean enterNow = keyboard.isKeyPressed(KeyCode.ENTER) || keyboard.isKeyPressed(KeyCode.Z);
        if (enterNow && !enterPrev) {
            gameState = GameState.MENU;
            menuSelection = 1; // position on highscores option when returning
        }
        enterPrev = enterNow;
    }

    private void handleGameOverMenuInput() {
        boolean upNow = keyboard.isKeyPressed(KeyCode.UP);
        boolean downNow = keyboard.isKeyPressed(KeyCode.DOWN);
        boolean enterNow = keyboard.isKeyPressed(KeyCode.ENTER) || keyboard.isKeyPressed(KeyCode.Z);

        // three options: 0=save,1=replay,2=quit
        if (upNow && !upPrev) {
            menuSelection = (menuSelection - 1 + 3) % 3;
        }
        if (downNow && !downPrev) {
            menuSelection = (menuSelection + 1) % 3;
        }
        if (enterNow && !enterPrev) {
            if (menuSelection == 0) {
                // Force save immediately even without name, then show highscores
                // Use empty name when no name provided
                String forcedName = "";
                int position = ScoreManager.forceSaveScore(SCORES_FILE, forcedName, finalScore);
                // ensure borne file is updated
                createBorneScoreFile();
                // show highscores after saving
                gameState = GameState.HIGHSCORES;
            } else if (menuSelection == 1) {
                startNewGame();
            } else {
                System.exit(0);
            }
        }

        upPrev = upNow;
        downPrev = downNow;
        enterPrev = enterNow;
    }

    private void startNewGame() {
        gameState = GameState.PLAYING;
        gameOver = false;
        gameWon = false;
        gameStartTimeMs = 0;
        menuSelection = 0;
        cursorX = canvas.getWidth() / 2;
        cursorY = canvas.getHeight() / 2;

        // Create new world
        world = new Engine();
        score = new Score();
        creator = new EntityCreator(world);

        // Add spawners
        creator.create(
                new Spawner(1, 0, 0, 1550, 850),
                new SimpleBoxSpawnType());
        creator.create(
                new Spawner(1, 0, 0, 1550, 850),
                new MovingBoxSpawnType());
        creator.create(
                new Spawner(1, 0, 0, 1550, 850),
                new PenaltySpawnType());
        creator.create(
                new Spawner(5, 0, 0, 1550, 850),
                new HourglassSpawnType());

        // Register systems
        world.addSystem(new SimpleBoxSpawnerSystem(Color.GOLDENROD));
        world.addSystem(new MovingboxSpawnerSystem(Color.DARKBLUE));
        world.addSystem(new SlimeBoxSpawnerSystem(Color.LIGHTBLUE));
        world.addSystem(new PenaltySpawnerSystem(Color.PURPLE));
        world.addSystem(new HourglassSpawnerSystem());
        BulletCollisionSystem bulletCollisionSystem = new BulletCollisionSystem();
        world.addSystem(bulletCollisionSystem);
        world.addSystem(new VelocitySystem());

        // Score signal
        Signal<TargetDestroyed> targetDestroyedSignal = bulletCollisionSystem.getTargetDestroyedSignal();
        targetDestroyedSignal.add(score);

        // Explosion
        ExplosionListener explosionListener = new ExplosionListener(Color.ORANGE, world);
        targetDestroyedSignal.add(explosionListener);
        // Slime splitting behaviour
        SlimeListener slimeListener = new SlimeListener(world);
        targetDestroyedSignal.add(slimeListener);

        // Hourglass time bonus
        hourglassListener = new HourglassListener(() -> {
            addTimeBonus(hourglassListener.getTimeBonus());
        });
        targetDestroyedSignal.add(hourglassListener);

        AlphaDecaySystem alphaSystem = new AlphaDecaySystem();
        world.addEntityListener(Family.all(Target.class).get(), alphaSystem);
        world.addSystem(alphaSystem);
        world.addSystem(new BoxShapeRenderer(canvas));
        world.addSystem(new CircleShapeRenderer(canvas));
    }

    /**
     * Crée le fichier de scores pour la borne d'arcade.
     * Copie le fichier de scores du jeu vers le répertoire de la borne.
     */
    private void createBorneScoreFile() {
        try {
            String gameScoreFile = SCORES_FILE;
            String borneScoreFile = "../../../projet/TP-Jeu-NodeBuster-main/highscore";

            java.nio.file.Path sourcePath = java.nio.file.Paths.get(gameScoreFile);
            java.nio.file.Path targetPath = java.nio.file.Paths.get(borneScoreFile);

            if (java.nio.file.Files.exists(sourcePath)) {
                // Créer les répertoires parents s'ils n'existent pas
                java.nio.file.Files.createDirectories(targetPath.getParent());

                // Copier le fichier
                java.nio.file.Files.copy(sourcePath, targetPath,
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la création du fichier de scores pour la borne: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
