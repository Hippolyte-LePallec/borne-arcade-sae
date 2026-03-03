package fr.iutlittoral;

import fr.iutlittoral.components.Spawner;
import fr.iutlittoral.components.Target;
import fr.iutlittoral.components.spawntypes.MovingBoxSpawnType;
import fr.iutlittoral.components.spawntypes.SimpleBoxSpawnType;
import fr.iutlittoral.components.spawntypes.PenaltySpawnType;
import fr.iutlittoral.events.TargetDestroyed;
import fr.iutlittoral.systems.*;
import fr.iutlittoral.systems.spawners.MovingboxSpawnerSystem;
import fr.iutlittoral.systems.spawners.SimpleBoxSpawnerSystem;
import fr.iutlittoral.systems.spawners.SlimeBoxSpawnerSystem;
import fr.iutlittoral.systems.spawners.PenaltySpawnerSystem;
import fr.iutlittoral.ui.MenuRenderer;
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

public class App extends Application {

    enum GameState {
        MENU, INSTRUCTIONS, PLAYING, GAME_OVER
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
    private static final int WIN_SCORE = 300;
    private static final int MIN_SCORE = 100;
    private static final long TIME_LIMIT_SECONDS = 60;
    // CURSOR: size is defined in Cursor.SIZE to avoid sprinkling magic numbers
    private long gameStartTimeMs = 0;
    private Canvas canvas;
    private Font font;
    private Mouse mouse;
    private Engine world;
    private Score score;
    private EntityCreator creator;
    private GameLoopTimer gameplayTimer;

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

        // Boucle du menu principal
        GameLoopTimer mainTimer = new GameLoopTimer() {
            @Override
            public void tick(float secondsSinceLastFrame) {
                if (gameState == GameState.MENU) {
                    MenuRenderer.renderMainMenu(canvas, menuSelection);
                    handleMenuInput();
                } else if (gameState == GameState.INSTRUCTIONS) {
                    MenuRenderer.renderInstructions(canvas);
                    handleInstructionsInput();
                } else if (gameState == GameState.GAME_OVER) {
                    MenuRenderer.renderGameOverMenu(canvas, gameWon, menuSelection);
                    handleGameOverMenuInput();
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
                }

                // Vérification de victoire
                if (!gameOver && score.getScore() >= WIN_SCORE) {
                    gameOver = true;
                    gameWon = true;
                    gameState = GameState.GAME_OVER;
                    menuSelection = 0;
                    return;
                }

                long elapsedMs = System.currentTimeMillis() - gameStartTimeMs;
                long elapsedSeconds = elapsedMs / 1000;
                long remainingSeconds = Math.max(0, TIME_LIMIT_SECONDS - elapsedSeconds);

                // Vérification de défaite
                if (elapsedSeconds >= TIME_LIMIT_SECONDS && score.getScore() < MIN_SCORE) {
                    gameOver = true;
                    gameWon = false;
                    gameState = GameState.GAME_OVER;
                    menuSelection = 0;
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
                // remainingSeconds is calculated earlier from the start timestamp
                gc.fillText("Temps : " + remainingSeconds + "s", 10, 65);
                gc.setFont(new Font("Vera.ttf", 16));
                gc.fillText("Objectif : " + MIN_SCORE + " points en " + TIME_LIMIT_SECONDS + "s", 10, 85);
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

    private void handleMenuInput() {
        boolean upNow = keyboard.isKeyPressed(KeyCode.UP);
        boolean downNow = keyboard.isKeyPressed(KeyCode.DOWN);
        boolean enterNow = keyboard.isKeyPressed(KeyCode.ENTER) || keyboard.isKeyPressed(KeyCode.Z);

        if (upNow && !upPrev) {
            menuSelection = (menuSelection - 1 + 3) % 3; // 0=Play, 1=Instructions, 2=Quit
        }
        if (downNow && !downPrev) {
            menuSelection = (menuSelection + 1) % 3;
        }
        if (enterNow && !enterPrev) {
            if (menuSelection == 0) {
                startNewGame();
            } else if (menuSelection == 1) {
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

    private void handleGameOverMenuInput() {
        boolean upNow = keyboard.isKeyPressed(KeyCode.UP);
        boolean downNow = keyboard.isKeyPressed(KeyCode.DOWN);
        boolean enterNow = keyboard.isKeyPressed(KeyCode.ENTER) || keyboard.isKeyPressed(KeyCode.Z);

        if (upNow && !upPrev) {
            menuSelection = (menuSelection - 1 + 2) % 2;
        }
        if (downNow && !downPrev) {
            menuSelection = (menuSelection + 1) % 2;
        }
        if (enterNow && !enterPrev) {
            if (menuSelection == 0) {
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

        // Register systems
        world.addSystem(new SimpleBoxSpawnerSystem(Color.GOLDENROD));
        world.addSystem(new MovingboxSpawnerSystem(Color.DARKBLUE));
        world.addSystem(new SlimeBoxSpawnerSystem(Color.LIGHTBLUE));
        world.addSystem(new PenaltySpawnerSystem(Color.PURPLE));
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

        AlphaDecaySystem alphaSystem = new AlphaDecaySystem();
        world.addEntityListener(Family.all(Target.class).get(), alphaSystem);
        world.addSystem(alphaSystem);
        world.addSystem(new BoxShapeRenderer(canvas));
        world.addSystem(new CircleShapeRenderer(canvas));
    }

    public static void main(String[] args) {
        launch();
    }
}
