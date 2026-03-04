package fr.iutlittoral.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.signals.Signal;
import com.badlogic.ashley.utils.ImmutableArray;

import fr.iutlittoral.components.Bullet;
import fr.iutlittoral.components.BoxCollider;
import fr.iutlittoral.components.Position;
import fr.iutlittoral.components.Target;
import fr.iutlittoral.components.Slime;
import fr.iutlittoral.events.TargetDestroyed;

/**
 * Bullet System
 * A system that detects collisions between bullets and collidable entities
 * Targeted Entities:
 * - "bullets" that have:
 * - Bullet Component
 * - Position Component
 * - "targets" that have:
 * - BoxCollider Component
 * - Position Component
 * - Target Component
 */
public class BulletCollisionSystem extends EntitySystem {

    // private int score = 0;

    private Signal<TargetDestroyed> targetDestroyedSignal;

    private ComponentMapper<Bullet> bulletMapper = ComponentMapper.getFor(Bullet.class);
    private ComponentMapper<Position> positionMapper = ComponentMapper.getFor(Position.class);
    private ComponentMapper<BoxCollider> colliderMapper = ComponentMapper.getFor(BoxCollider.class);

    ImmutableArray<Entity> bulletEntities;
    ImmutableArray<Entity> targetEntities;

    @Override
    public void addedToEngine(Engine engine) {
        this.bulletEntities = engine.getEntitiesFor(Family.all(Bullet.class).get());
        this.targetEntities = engine.getEntitiesFor(Family.all(Position.class, Target.class, BoxCollider.class).get());
    }

    public BulletCollisionSystem() {
        this.targetDestroyedSignal = new Signal<TargetDestroyed>();
    }

    public Signal<TargetDestroyed> getTargetDestroyedSignal() {
        return this.targetDestroyedSignal;
    }

    // public int getScore() {
    // return this.score;
    // }

    // public void setScore(int score) {
    // this.score += score;
    // }

    @Override
    public void update(float deltaTime) {
        // iterate over every bullet and test against all targets
        this.bulletEntities.forEach(bulletEntity -> {
            Bullet bullet = bulletMapper.get(bulletEntity);
            this.targetEntities.forEach(targetEntity -> {
                BoxCollider collider = colliderMapper.get(targetEntity);
                Position position = positionMapper.get(targetEntity);

                // collide square bullet with rectangular target using AABB overlap test
                double bx = bullet.x;
                double by = bullet.y;
                double bw = bullet.width;
                double bh = bullet.height;

                boolean overlaps = bx < position.x + collider.width &&
                        bx + bw > position.x &&
                        by < position.y + collider.height &&
                        by + bh > position.y;

                if (overlaps) {
                    // SCORE SYSTEM: the destroyed target knows how many points it is worth
                    int score = targetEntity.getComponent(Target.class).value;

                    // SCORE SYSTEM: notify listeners that a target was destroyed
                    // TargetDestroyed contains the score value and the location of
                    // the explosion (center of the bullet) so that other systems can
                    // react (e.g. show particles).
                    double centerX = bx + bw / 2.0;
                    double centerY = by + bh / 2.0;
                    // determine if the removed target was a slime
                    boolean wasSlime = targetEntity.getComponent(fr.iutlittoral.components.Slime.class) != null;
                    targetDestroyedSignal
                            .dispatch(new TargetDestroyed(score, centerX, centerY, wasSlime, targetEntity));

                    // remove target from world immediately
                    getEngine().removeEntity(targetEntity);

                    // BULLET SYSTEM: remove bullet so it doesn't hit anything else
                    getEngine().removeEntity(bulletEntity);
                }
            });
        });
    }
}
