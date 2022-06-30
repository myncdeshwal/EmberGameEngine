package Observers;

import EntityComponent.GameObject;
import Observers.Events.Event;

public interface Observer {
    void onNotify(GameObject object, Event event);
}
