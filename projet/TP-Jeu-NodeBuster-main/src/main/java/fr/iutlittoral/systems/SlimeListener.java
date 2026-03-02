package fr.iutlittoral.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.signals.Listener;
import com.badlogic.ashley.signals.Signal;

import fr.iutlittoral.components.*;
import fr.iutlittoral.events.TargetDestroyed;
import javafx.scene.paint.Color;


public class SlimeListener implements Listener<TargetDestroyed>{

    private Engine engine;

    public SlimeListener(Engine engine) {
        this.engine = engine;
        
    }

    @Override
    public void receive(Signal<TargetDestroyed> signal, TargetDestroyed event) {
        
            double x = event.x;
            double y = event.y;

            // Création de 4 entités alant dans les 4 directions en diagonale
            for (int i = 0; i < 4; i++) {
                Entity entity = new Entity();
                entity.add(new Position(x, y));
                entity.add(new BoxShape(50, 50));
                entity.add(new Shade(Color.GREEN));
                entity.add(new LimitedLifespan(5));
                entity.add(new BoxCollider(50, 50));
                entity.add(new Velocity(0,0));
                entity.add(new Target(3));
                entity.add(new AlphaDecay());

                switch (i) {
                    case 0:
                        entity.add(new Velocity(-100, -100));
                        break;
                    case 1:
                        entity.add(new Velocity(100, -100));
                        break;
                    case 2:
                        entity.add(new Velocity(-100, 100));
                        break;
                    case 3:
                        entity.add(new Velocity(100, 100));
                        break;
                }

                engine.addEntity(entity);
        }


    }
}

