package starsalvage.engine.cells;

import starsalvage.engine.Vec2;

import java.io.Serializable;
import java.util.logging.Logger;

/**
 * Abstract class to host shared functionality of all Cells;
 * Cells have custom behaviour that mix up the game
 */
public abstract class Cell implements Serializable {

    /**
     * Identifier for each class extending Cell
     */
    public static enum CellType {
        WORMHOLE_CELL(0),
        ASTEROID_CELL(1),
        DRILL_CHARGE_CELL(2),
        HULL_PATCH_CELL(3),
        GRAVITY_WELL_CELL(4),
        SCRAP_CARGO_CELL(5),
        ENTRY_CELL(6),
        SCOUT_DRONE_CELL(7),
        SPACE_MINE_CELL(8),
        SENTRY_TURRET_CELL(9),
        SHIP(10),
        EMPTY_CELL(11);

        /**
         * Integral identifier for each class extending Cell
         */
        public final int id;

        /**
         * @param id The integral identifier for the cell type
         */
        CellType(int id) {
            this.id = id;
        }
    }

    /**
     * Used for every cell to be able to log to console
     */
    protected transient final static Logger LOGGER = Logger.getLogger(Cell.class.getName());
    /**
     * The x and y position of the cell within the sector
     */
    public Vec2 pos;

    /**
     * Sets the cell position
     * @param pos The position of the cell
     */
    public Cell(Vec2 pos) {
        this.pos = pos;
    }
    /**
     * Copies the content of the cell
     * @return The cell copy
     */
    public abstract Cell copy();
    /**
     * @return The CellType of the current cell class
     */
    public abstract CellType getCellType();

    /**
     * @return The CLI character representation of the cell type
     */
    public abstract String getCharacter();

    /**
     * @param type The cell asking to move onto blocked cell,
     *             so only certain cells can be disallowed or allowed to move onto a blocked cell
     * @return Whether the cell cannot be entered or moved onto
     */
    public boolean isBlocked(CellType type) { return false; }
    /**
     * @param type The cell consuming this cell, so this cell
     *             can only be consumed by certain cells
     * @return Whether the cell can be consumed
     */
    public boolean isConsumable(CellType type) { return false; }
    /**
     * @param type The cell destroying this cell, so this cell
     *             can only be destroyed by certain cells
     * @return Whether the cell can be destroyed
     */
    public boolean isDestructible(CellType type) { return false; }

}
