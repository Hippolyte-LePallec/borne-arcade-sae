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

    // aiming cursor coordinates
    private double cursorX;
    private double cursorY;
    // simple keyboard wrapper for arrow keys / Z button
    private fr.iutlittoral.utils.Keyboard keyboard;
    // has the player lost ?
    private boolean gameOver = false;
    private boolean gameWon = false;
    // score required to win
    private static final int WIN_SCORE = 30;
    // radius used when drawing the aiming cursor
    private static final double CURSOR_RADIUS = 12;

    @Override
    public void start(Stage stage) {
        /* Standard JavaFX stage creation */
        var canvas = new Canvas(1600, 900);
        // make canvas resize with window (important for full screen)
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

        /* Ashley engine initialization */
        Engine world = new Engine();

        /* Helper objects initialization */
        Font font = new Font("Vera.ttf", 25);
        keyboard = new fr.iutlittoral.utils.Keyboard(scene);
        Mouse mouse = new Mouse(canvas);
        // set initial cursor to centre of canvas
        cursorX = canvas.getWidth() / 2;
        cursorY = canvas.getHeight() / 2;
        EntityCreator creator = new EntityCreator(world);

        /* Adds a target spawner */
        creator.create(
                new Spawner(1, 0, 0, 1550, 850),
                new SimpleBoxSpawnType());

        /* Adds a moving box spawner */
        creator.create(
                new Spawner(1, 0, 0, 1550, 850),
                new MovingBoxSpawnType());

        // creator.create(
        // new Spawner(1, 0, 0, 1550, 850),
        // new SlimeBoxSpawnType()
        // );

        /* System registration */
        world.addSystem(new SimpleBoxSpawnerSystem(Color.GOLDENROD));
        world.addSystem(new MovingboxSpawnerSystem(Color.DARKRED));
        world.addSystem(new SlimeBoxSpawnerSystem(Color.LIGHTBLUE));
        BulletCollisionSystem bulletCollisionSystem = new BulletCollisionSystem();
        world.addSystem(bulletCollisionSystem);
        world.addSystem(new VelocitySystem());

        /* Score */
        Score score = new Score();
        Signal<TargetDestroyed> targetDestroyedSignal = bulletCollisionSystem.getTargetDestroyedSignal();
        targetDestroyedSignal.add(score);

        // lose when a target disappears because of lifespan
        LimitedLifespanSystem lifeSys = new LimitedLifespanSystem();
        world.addSystem(lifeSys);
        lifeSys.getTargetMissedSignal().add((sig, ev) -> {
            gameOver = true;
        });

        /* Explosion */
        ExplosionListener explosionListener = new ExplosionListener(Color.ORANGE, world);
        targetDestroyedSignal.add(explosionListener);

        // /* Slime */
        // SlimeListener slimeListener = new SlimeListener(world);
        // targetDestroyedSignal.add(slimeListener);

        AlphaDecaySystem alphaSystem = new AlphaDecaySystem();
        world.addEntityListener(Family.all(Target.class).get(), alphaSystem);
        world.addSystem(alphaSystem);
        world.addSystem(new BoxShapeRenderer(canvas));
        world.addSystem(new CircleShapeRenderer(canvas));

        GameLoopTimer timer = new GameLoopTimer() {
            boolean zPrev = false;

            @Override
            public void tick(float secondsSinceLastFrame) {
                if (gameOver) {
                    GraphicsContext gc = canvas.getGraphicsContext2D();
                    gc.save();
                    gc.setFill(Color.BLACK);
                    gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
                    gc.setFill(Color.RED);
                    gc.setFont(new Font("Vera.ttf", 72));
                    if (gameWon) {
                        gc.fillText("YOU WIN", canvas.getWidth() / 2 - 150, canvas.getHeight() / 2);
                    } else {
                        gc.fillText("GAME OVER", canvas.getWidth() / 2 - 200, canvas.getHeight() / 2);
                    }
                    gc.restore();
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

                // win check
                if (score.getScore() >= WIN_SCORE) {
                    gameOver = true;
                    gameWon = true;
                }

                GraphicsContext gc = canvas.getGraphicsContext2D();
                gc.save();
                gc.setFill(Color.BLACK);
                gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
                // draw score
                gc.setFill(Color.WHITE);
                gc.setFont(font);
                gc.fillText("Score " + score.getScore(), 10, 35);
                // draw aiming cursor (larger)
                gc.setStroke(Color.RED);
                gc.strokeOval(cursorX - CURSOR_RADIUS, cursorY - CURSOR_RADIUS,
                        CURSOR_RADIUS * 2, CURSOR_RADIUS * 2);
                gc.restore();

                world.update(secondsSinceLastFrame);
            }
        };

        timer.start();
    }

    public static void main(String[] args) {
        launch();
    }
}
