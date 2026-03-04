package fr.iutlittoral.systems.spawners;

import com.badlogic.ashley.core.Entity;

import java.util.Random;

import fr.iutlittoral.components.*;
import fr.iutlittoral.components.spawntypes.PenaltySpawnType;
import javafx.scene.paint.Color;

/**
 * Spawner system for penalty circles (ronds).
 * These are rendered as circles and make the player lose points when destroyed.
 */
public class PenaltySpawnerSystem extends AbstractSpawnerSystem {

    private Color color;

    public PenaltySpawnerSystem(Color color) {
        super(PenaltySpawnType.class);
        this.color = color;
    }

    /**
     * Generate a random radius between 25 and 100 pixels.
     * Diameter ranges from 50 to 200 pixels.
     */
    private double randomRadius() {
        Random random = new Random();
        return 25 + random.nextDouble() * 75; // 25 to 100
    }

    @Override
    public void spawn(double x, double y) {
        double radius = randomRadius();
        double diameter = radius * 2;

        Entity entity = new Entity();
        entity.add(new Position(x, y));
        // use CircleShape for rendering as a circle with random radius
        entity.add(new CircleShape(radius));
        entity.add(new Shade(color));
        entity.add(new LimitedLifespan(3));
        // use BoxCollider with dimensions matching the circle diameter
        entity.add(new BoxCollider(diameter, diameter));
        // negative value: -2 points when destroyed
        entity.add(new Target(-10));
        entity.add(new AlphaDecay());
        entity.add(new Penalty());

        getEngine().addEntity(entity);
    }
}
