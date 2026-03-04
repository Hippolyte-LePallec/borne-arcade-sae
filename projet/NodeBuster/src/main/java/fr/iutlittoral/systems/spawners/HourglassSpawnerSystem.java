package fr.iutlittoral.systems.spawners;

import com.badlogic.ashley.core.Entity;

import java.util.Random;

import fr.iutlittoral.components.*;
import fr.iutlittoral.components.spawntypes.HourglassSpawnType;
import javafx.scene.paint.Color;

/**
 * Spawner system for hourglasses (sabliers).
 * These add random time (2-10 seconds) when destroyed.
 */
public class HourglassSpawnerSystem extends AbstractSpawnerSystem {

    public HourglassSpawnerSystem() {
        super(HourglassSpawnType.class);
    }

    @Override
    public void spawn(double x, double y) {
        Random random = new Random();
        int timeBonus = 2 + random.nextInt(9); // Entre 2 et 10 secondes
        double size = 30;

        Entity entity = new Entity();
        entity.add(new Position(x, y));
        // Use BoxShape for rendering as a square (hourglass representation)
        entity.add(new BoxShape(size, size));
        // Jaune pour le sablier (hourglass)
        entity.add(new Shade(Color.YELLOW));
        entity.add(new LimitedLifespan(5)); // Durée de vie limitée
        // BoxCollider matching the size
        entity.add(new BoxCollider(size, size));
        // Positive value: gives points when destroyed
        entity.add(new Target(5));
        entity.add(new AlphaDecay());
        // Hourglass component with time bonus
        entity.add(new Hourglass(timeBonus));

        getEngine().addEntity(entity);
    }
}
