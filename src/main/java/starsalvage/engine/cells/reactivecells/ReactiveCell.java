package starsalvage.engine.cells.reactivecells;

import starsalvage.engine.Vec2;
import starsalvage.engine.cells.Cell;
import starsalvage.engine.interfaces.ISafeEngineAccess;

/**
 * Abstract cell class to represent cells that are not subscribed to any events, and do not move,
 * but have custom behaviour that acts on the game engine
 */
public abstract class ReactiveCell extends Cell {

    /**
     * Reference to engine interface
     */
    protected transient ISafeEngineAccess engine;

    /**
     * Sets the cell position and engine interface reference
     * @param pos The position of the cell
     * @param engine The engine interface
     */
    public ReactiveCell(ISafeEngineAccess engine, Vec2 pos) {
        super(pos);
        this.engine = engine;
    }

    /**
     * Set the engine reference to a new engine reference for use
     * when loading game state from save file
     * @param engine New engine reference
     */
    public void updateEngineRef(ISafeEngineAccess engine) {
        this.engine = engine;
    }

    /**
     * Called by GameEngine when this cell is entered by another cell
     * @param type CellType that is entering this cell
     */
    public abstract void onEntered(CellType type);
}
