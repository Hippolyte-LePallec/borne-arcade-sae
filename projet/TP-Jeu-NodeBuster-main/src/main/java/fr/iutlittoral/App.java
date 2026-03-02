package fr.iutlittoral;

import fr.iutlittoral.components.Spawner;
import fr.iutlittoral.components.Target;
import fr.iutlittoral.components.spawntypes.MovingBoxSpawnType;
import fr.iutlittoral.components.spawntypes.SimpleBoxSpawnType;
// import fr.iutlittoral.components.spawntypes.SlimeBoxSpawnType;
import fr.iutlittoral.events.TargetDestroyed;
import fr.iutlittoral.events.TargetMissed;
import fr.iutlittoral.systems.*;
import fr.iutlittoral.systems.spawners.MovingboxSpawnerSystem;
import fr.iutlittoral.systems.spawners.SimpleBoxSpawnerSystem;
import fr.iutlittoral.systems.spawners.SlimeBoxSpawnerSystem;
import fr.iutlittoral.utils.*;
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
import com.badlogic.ashley.signals.Listener;

public class App extends Application {

    enum GameState {
        MENU, PLAYING, GAME_OVER
    }

    // aiming cursor coordinates
    private double cursorX;
    private double cursorY;
    // simple keyboard wrapper for arrow keys / Z button
    private fr.iutlittoral.utils.Keyboard keyboard;
    // game state
    private GameState gameState = GameState.MENU;
    // has the player lost ?
    private boolean gameOver = false;
    private boolean gameWon = false;
    // menu selection: 0 = Play, 1 = Quit
    private int menuSelection = 0;
    // track key presses to prevent repeated action
    private boolean enterPrev = false;
    private boolean upPrev = false;
    private boolean downPrev = false;
    // score required to win
    private static final int WIN_SCORE = 30;
    // score required to not lose (must reach this before time runs out)
    private static final int MIN_SCORE = 10;
    // time limit in seconds (must reach MIN_SCORE before this)
    private static final long TIME_LIMIT_SECONDS = 60;
    // radius used when drawing the aiming cursor
    private static final double CURSOR_RADIUS = 12;
    // game start time tracking
    private long gameStartTimeMs = 0;
    // Game components - accessible between menu and gameplay
    private Canvas canvas;
    private Font font;
    private Mouse mouse;
    private Engine world;
    private Score score;
    private EntityCreator creator;
    private GameLoopTimer gameplayTimer;

    @Override
    public void start(Stage stage) {
        /* Standard JavaFX stage creation */
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

        /* Helper objects initialization */
        font = new Font("Vera.ttf", 25);
        keyboard = new fr.iutlittoral.utils.Keyboard(scene);
        mouse = new Mouse(canvas);
        cursorX = canvas.getWidth() / 2;
        cursorY = canvas.getHeight() / 2;

        /* Main menu loop */
        GameLoopTimer mainTimer = new GameLoopTimer() {
            @Override
            public void tick(float secondsSinceLastFrame) {
                if (gameState == GameState.MENU) {
                    renderMainMenu();
                    handleMenuInput();
                } else if (gameState == GameState.GAME_OVER) {
                    renderGameOverMenu();
                    handleGameOverMenuInput();
                }
            }
        };
        mainTimer.start();

        /* Gameplay loop - created but inactive until game starts */
        gameplayTimer = new GameLoopTimer() {
            boolean zPrev = false;

            @Override
            public void tick(float secondsSinceLastFrame) {
                if (gameState != GameState.PLAYING) {
                    return;
                }

                // Initialize game start time on first tick
                if (gameStartTimeMs == 0) {
                    gameStartTimeMs = System.currentTimeMillis();
                }

                // win check: reached WIN_SCORE
                if (!gameOver && score.getScore() >= WIN_SCORE) {
                    gameOver = true;
                    gameWon = true;
                    gameState = GameState.GAME_OVER;
                    menuSelection = 0;
                    return;
                }

                // Calculate elapsed time
                long elapsedMs = System.currentTimeMillis() - gameStartTimeMs;
                long elapsedSeconds = elapsedMs / 1000;
                long remainingSeconds = Math.max(0, TIME_LIMIT_SECONDS - elapsedSeconds);

                // lose check: time limit reached and score insufficient
                if (elapsedSeconds >= TIME_LIMIT_SECONDS && score.getScore() < MIN_SCORE) {
                    gameOver = true;
                    gameWon = false;
                    gameState = GameState.GAME_OVER;
                    menuSelection = 0;
                    return;
                }

                // move cursor with arrow keys
                double speed = 400 * secondsSinceLastFrame;
                if (keyboard.isKeyPressed(KeyCode.LEFT))
                    cursorX -= speed;
                if (keyboard.isKeyPressed(KeyCode.RIGHT))
                    cursorX += speed;
                if (keyboard.isKeyPressed(KeyCode.UP))
                    cursorY -= speed;
                if (keyboard.isKeyPressed(KeyCode.DOWN))
                    cursorY += speed;
                // clamp inside canvas
                cursorX = Math.max(0, Math.min(cursorX, canvas.getWidth()));
                cursorY = Math.max(0, Math.min(cursorY, canvas.getHeight()));

                // shooting: mouse click or Z key pressed
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
                // draw score
                gc.setFill(Color.WHITE);
                gc.setFont(font);
                gc.fillText("Score " + score.getScore(), 10, 35);
                // draw timer
                gc.fillText("Time: " + remainingSeconds + "s", 10, 65);
                // draw target score info
                gc.setFont(new Font("Vera.ttf", 16));
                gc.fillText("Reach " + MIN_SCORE + " points in " + TIME_LIMIT_SECONDS + "s", 10, 85);
                gc.setFont(font);
                // draw aiming cursor (larger)
                gc.setStroke(Color.RED);
                gc.strokeOval(cursorX - CURSOR_RADIUS, cursorY - CURSOR_RADIUS,
                        CURSOR_RADIUS * 2, CURSOR_RADIUS * 2);
                gc.restore();

                world.update(secondsSinceLastFrame);
            }
        };
        gameplayTimer.start();
    }

    private void renderMainMenu() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.save();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.WHITE);
        gc.setFont(new Font("Vera.ttf", 100));
        gc.fillText("NODEBUSTER", canvas.getWidth() / 2 - 350, 150);

        // Draw menu options
        gc.setFont(new Font("Vera.ttf", 60));
        if (menuSelection == 0) {
            gc.setFill(Color.YELLOW);
        } else {
            gc.setFill(Color.WHITE);
        }
        gc.fillText("PLAY", canvas.getWidth() / 2 - 80, 350);

        if (menuSelection == 1) {
            gc.setFill(Color.YELLOW);
        } else {
            gc.setFill(Color.WHITE);
        }
        gc.fillText("QUIT", canvas.getWidth() / 2 - 100, 500);

        gc.setFont(new Font("Vera.ttf", 24));
        gc.setFill(Color.LIGHTGRAY);
        gc.fillText("Use UP/DOWN to move, ENTER to select", canvas.getWidth() / 2 - 300, canvas.getHeight() - 50);
        gc.restore();
    }

    private void renderGameOverMenu() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.save();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFont(new Font("Vera.ttf", 72));
        if (gameWon) {
            gc.setFill(Color.GREEN);
            gc.fillText("YOU WIN", canvas.getWidth() / 2 - 200, 200);
        } else {
            gc.setFill(Color.RED);
            gc.fillText("GAME OVER", canvas.getWidth() / 2 - 250, 200);
        }

        // Draw menu options
        gc.setFont(new Font("Vera.ttf", 60));
        if (menuSelection == 0) {
            gc.setFill(Color.YELLOW);
        } else {
            gc.setFill(Color.WHITE);
        }
        gc.fillText("PLAY AGAIN", canvas.getWidth() / 2 - 200, 400);

        if (menuSelection == 1) {
            gc.setFill(Color.YELLOW);
        } else {
            gc.setFill(Color.WHITE);
        }
        gc.fillText("QUIT", canvas.getWidth() / 2 - 100, 550);

        gc.setFont(new Font("Vera.ttf", 24));
        gc.setFill(Color.LIGHTGRAY);
        gc.fillText("Use UP/DOWN to move, ENTER to select", canvas.getWidth() / 2 - 300, canvas.getHeight() - 50);
        gc.restore();
    }

    private void handleMenuInput() {
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
        // Reset game state
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

        /* Adds spawners */
        creator.create(
                new Spawner(1, 0, 0, 1550, 850),
                new SimpleBoxSpawnType());
        creator.create(
                new Spawner(1, 0, 0, 1550, 850),
                new MovingBoxSpawnType());

        /* System registration */
        world.addSystem(new SimpleBoxSpawnerSystem(Color.GOLDENROD));
        world.addSystem(new MovingboxSpawnerSystem(Color.DARKRED));
        world.addSystem(new SlimeBoxSpawnerSystem(Color.LIGHTBLUE));
        BulletCollisionSystem bulletCollisionSystem = new BulletCollisionSystem();
        world.addSystem(bulletCollisionSystem);
        world.addSystem(new VelocitySystem());

        /* Score and signals */
        Signal<TargetDestroyed> targetDestroyedSignal = bulletCollisionSystem.getTargetDestroyedSignal();
        targetDestroyedSignal.add(score);

        /* Explosion */
        ExplosionListener explosionListener = new ExplosionListener(Color.ORANGE, world);
        targetDestroyedSignal.add(explosionListener);

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
