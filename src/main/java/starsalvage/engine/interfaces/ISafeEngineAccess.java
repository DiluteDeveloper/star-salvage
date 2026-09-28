package starsalvage.engine.interfaces;

import starsalvage.engine.Sector;
import starsalvage.engine.cells.Cell;
import starsalvage.engine.GameEngine;
import starsalvage.engine.Result;
import starsalvage.engine.Vec2;
import starsalvage.engine.cells.reactivecells.activecells.movingcells.Ship;
import starsalvage.engine.eventsystem.EventBus;
import starsalvage.engine.eventsystem.EventListener;

/**
 * An interface to a game engine's functionality without allowing access
 * to core processes
 */
public interface ISafeEngineAccess {
    Ship getShip();

    /**
     * see {@link GameEngine#tryBeginNextSector()}
     */
    void tryBeginNextSector();
    int getSectorIdx();

    /**
     * see {@link GameEngine#tryDestroyCell(Vec2, Cell.CellType)}
     */
    Result<?> tryDestroyCell(Vec2 pos, Cell.CellType destroyerType);
    /**
     * see {@link Sector#getCell(Vec2)}
     */
    Result<Cell> getCell(Vec2 pos);
    /**
     * see {@link Sector#getOverlayCell(Vec2)}
     */
    Result<Cell> getOverlayCell(Vec2 pos);

    /**
     * see {@link EventBus#publish(Object)}
     */
    <T> void publishEvent(T event);
    /**
     * see {@link EventBus#subscribe(Class, EventListener)}
     */
    <T> void subscribeToEvent(Class<T> eventType, EventListener<T> listener);
    /**
     * see {@link EventBus#unsubscribe(Class, EventListener)}
     */
    <T> void unsubscribeFromEvent(Class<T> eventType, EventListener<T> listener);

    int getSeed();
    int getDifficulty();
    GameEngine.GameState getGameState();

}
