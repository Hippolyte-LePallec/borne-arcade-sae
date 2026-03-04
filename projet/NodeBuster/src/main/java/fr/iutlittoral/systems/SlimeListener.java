package fr.iutlittoral.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.signals.Listener;
import com.badlogic.ashley.signals.Signal;

import fr.iutlittoral.components.*;
import fr.iutlittoral.events.TargetDestroyed;
import javafx.scene.paint.Color;

public class SlimeListener implements Listener<TargetDestroyed> {

    private Engine engine;

    public SlimeListener(Engine engine) {
        this.engine = engine;

    }

    @Override
    public void receive(Signal<TargetDestroyed> signal, TargetDestroyed event) {
        // only split slimes
        if (!event.slime) {
            return;
        }

        double x = event.x;
        double y = event.y;

        // spawn four smaller moving boxes that carry value 2
        double size = 40;
        for (int i = 0; i < 4; i++) {
            Entity entity = new Entity();
            entity.add(new Position(x, y));
            entity.add(new BoxShape(size, size));
            entity.add(new Shade(Color.GREEN));
            entity.add(new LimitedLifespan(3));
            entity.add(new BoxCollider(size, size));
            entity.add(new Target(2));
            entity.add(new AlphaDecay());

            switch (i) {
                case 0:
                    entity.add(new Velocity(-150, -150));
                    break;
                case 1:
                    entity.add(new Velocity(150, -150));
                    break;
                case 2:
                    entity.add(new Velocity(-150, 150));
                    break;
                case 3:
                    entity.add(new Velocity(150, 150));
                    break;
            }

            engine.addEntity(entity);
        }
    }
}
