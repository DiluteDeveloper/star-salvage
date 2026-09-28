package starsalvage.engine.cells.reactivecells.activecells;

import starsalvage.engine.*;
import starsalvage.engine.cells.reactivecells.ReactiveCell;
import starsalvage.engine.eventsystem.EventListener;
import starsalvage.engine.interfaces.ISafeEngineAccess;

/**
 * Abstract cell class to represent cells that act every tick/are subscribed to events
 */
public abstract class ActiveCell extends ReactiveCell {

    /**
     * Sets the cell position and engine interface reference
     * @param pos The position of the cell
     * @param engine The engine interface
     */
    public ActiveCell(ISafeEngineAccess engine, Vec2 pos) {
        super(engine, pos);
    }
    /**
     * Set the engine reference to a new engine reference and
     * subscribe to events for use
     * when loading game state from save file
     * @param engine New engine reference
     */
    @Override
    public void updateEngineRef(ISafeEngineAccess engine) {
        this.engine = engine;
        subscribe();
    }

    /**
     * Subscribe cell to requested events
     */
    public void subscribe() {
        gameTickEventListener = this::onGameTickEvent;
        engine.subscribeToEvent(GameEngine.GameTickEvent.class, gameTickEventListener);
    }
    /**
     * Unsubscribe cell from subscribed events
     */
    public void unsubscribe() {
        engine.unsubscribeFromEvent(GameEngine.GameTickEvent.class, gameTickEventListener);
    }

    /**
     * An instance of an event listener, required to be able to unsubscribe the cell
     */
    private transient EventListener<GameEngine.GameTickEvent> gameTickEventListener;

    /**
     * Called on GameTickEvent
     */
    public void onGameTickEvent(GameEngine.GameTickEvent event) {};
}
