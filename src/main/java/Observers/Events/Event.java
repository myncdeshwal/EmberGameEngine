package Observers.Events;

public class Event {
    public EventType type;

    public Event() {
        type = EventType.UserEvent;
    }

    public Event(EventType type) {
        this.type = type;
    }
}