package starsalvage.engine.eventsystem;

/**
 * An interface used for functions that wish to subscribe to any event,
 * see {@link EventBus} for more information
 * @param <T> The event type
 */
public interface EventListener<T> {
    void onEvent(T event);
}
