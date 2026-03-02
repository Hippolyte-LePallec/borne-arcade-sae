package fr.iutlittoral.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

import fr.iutlittoral.components.LimitedLifespan;
import fr.iutlittoral.components.Target;
import fr.iutlittoral.events.TargetMissed;
import com.badlogic.ashley.signals.Signal;

/**
 * Limited Lifespan System
 * Manages the life cycle of entities that have a LimitedLifespan component
 * Targeted entities:
 * - LimitedLifespan Component
 */
public class LimitedLifespanSystem extends IteratingSystem {

    private ComponentMapper<LimitedLifespan> lifespans = ComponentMapper.getFor(LimitedLifespan.class);
    private ComponentMapper<Target> targetMapper = ComponentMapper.getFor(Target.class);
    private Signal<TargetMissed> targetMissedSignal = new Signal<>();

    public Signal<TargetMissed> getTargetMissedSignal() {
        return targetMissedSignal;
    }

    public LimitedLifespanSystem() {
        super(Family.all(LimitedLifespan.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        LimitedLifespan lifespan = lifespans.get(entity);
        lifespan.elapsedLifespan += deltaTime;
        if (lifespan.elapsedLifespan >= lifespan.totalLifespan) {
            // if it's a target, fire missed signal before removal
            if (targetMapper.has(entity)) {
                targetMissedSignal.dispatch(new TargetMissed());
            }
            this.getEngine().removeEntity(entity);
        }
    }

}
