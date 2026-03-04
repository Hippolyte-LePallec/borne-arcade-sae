package fr.iutlittoral.systems.spawners;

import com.badlogic.ashley.core.Entity;

import fr.iutlittoral.components.*;
import fr.iutlittoral.components.spawntypes.MovingBoxSpawnType;
import javafx.scene.paint.Color;


public class SlimeBoxSpawnerSystem extends AbstractSpawnerSystem {
    
    private Color color;

    public SlimeBoxSpawnerSystem(Color color) {
        super(MovingBoxSpawnType.class);
        this.color = color;
    }

    @Override
    public void spawn(double x, double y) {
        Entity entity = new Entity();
        entity.add(new Position(x, y));
        entity.add(new BoxShape(100, 100));
        entity.add(new Shade(color));
        entity.add(new LimitedLifespan(3));
        entity.add(new BoxCollider(100, 100));
        entity.add(new Velocity(0,0));
        entity.add(new Target(3));
        entity.add(new AlphaDecay());
        entity.add(new Slime());

        getEngine().addEntity(entity);
    }
}
