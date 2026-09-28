package starsalvage.engine.eventsystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores a list of event listeners and dispatches event information
 * to subscribed functions when an event type is published
 */
public class EventBus {

    /**
     * Event subscribers to receive events
     */
    private final Map<Class<?>, List<EventListener<?>>> listeners = new HashMap<>();

    /**
     * Subscribes the listener to the eventType
     * @param eventType The type of event
     * @param listener The EventListener interface of the function to subscribe
     */
    public <T> void subscribe(Class<T> eventType, EventListener<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
    }
    /**
     * Unsubscribes the listener from the eventType
     * @param eventType The type of event
     * @param listener The EventListener interface of the subscribed function
     */
    public <T> void unsubscribe(Class<T> eventType, EventListener<T> listener) {
        List<EventListener<?>> list = listeners.get(eventType);
        if (list != null) {
            list.remove(listener);
        }
    }

    /**
     * Publishes an event to all EventListeners subscribed to that event type
     * @param event The type of event
     */
    @SuppressWarnings("unchecked")
    public <T> void publish(T event) {
        List<EventListener<?>> list = listeners.get(event.getClass());
        if (list != null) {
            for (EventListener<?> listener : new ArrayList<>(list)) {
                ((EventListener<T>) listener).onEvent(event);
            }
        }
    }
}
