package fr.iutlittoral.utils;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;

import fr.iutlittoral.components.AlphaDecay;
import fr.iutlittoral.components.Bullet;
import fr.iutlittoral.components.LimitedLifespan;
import fr.iutlittoral.components.Position;
import fr.iutlittoral.components.Shade;
import fr.iutlittoral.components.Cursor;
import fr.iutlittoral.components.BoxShape;
import javafx.scene.paint.Color;

/**
 * Helper class to create entities with a list of components
 */
public class EntityCreator {

    private Engine world;

    public EntityCreator(Engine world) {
        this.world = world;
    }

    public Entity create(Component... components) {
        Entity entity = new Entity();

        for (Component component : components) {
            entity.add(component);
        }

        world.addEntity(entity);

        return entity;
    }

    public void createBullet(double centerX, double centerY) {
        // bullet size is defined by cursor dimensions so the shot covers
        // exactly the area of the red square cursor
        double size = Cursor.SIZE;
        double half = size / 2.0;

        // top‑left corner of the bullet hitbox
        double x = centerX - half;
        double y = centerY - half;

        Entity bulletEntity = new Entity();
        bulletEntity.add(new Position(x, y));
        bulletEntity.add(new Bullet(x, y, size, size));
        // bullets are not rendered visually; they exist only for collision detection
        // very short lifespan: 1ms (despawn immediately if no collision)
        bulletEntity.add(new LimitedLifespan(0.001));

        world.addEntity(bulletEntity);
    }
}
