package Observers;

import EntityComponent.GameObject;
import Observers.Events.Event;

import java.util.ArrayList;
import java.util.List;

public class EventSystem {
    private static List<Observer> observers = new ArrayList<>();

    public static void addObservers(Observer observer){
        observers.add(observer);
    }

    public static void notify(GameObject go, Event event){
       for(Observer observer: observers){
           observer.onNotify(go,event);
       }
    }

}
