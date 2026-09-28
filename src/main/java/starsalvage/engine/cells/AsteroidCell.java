package starsalvage.engine.cells;

import starsalvage.engine.Vec2;
import starsalvage.engine.interfaces.ISafeEngineAccess;

/**
 * A blocked cell that cannot be moved onto
 */
public final class AsteroidCell extends Cell {

    /**
     * Sets the cell position
     * @param pos The position of the cell
     */
    public AsteroidCell(Vec2 pos) {
        super(pos);
    }
    /**
     * Copy the contents of AsteroidCell
     * @return The copied AsteroidCell as a Cell
     */
    @Override
    public Cell copy() {
        return new AsteroidCell(new Vec2(pos));
    }

    /**
     * @return The CellType of the current cell class
     */
    @Override
    public CellType getCellType() {
        return CellType.ASTEROID_CELL;
    }

    /**
     * @return The CLI character representation of the cell type
     */
    @Override
    public String getCharacter() { return "A"; }

    /**
     * @param type The cell asking to move onto blocked cell,
     *             so only certain cells can be disallowed or allowed to move onto a blocked cell
     * @return Whether the cell cannot be entered or moved onto
     */
    public boolean isBlocked(CellType type) { return true; }

    /**
     * @param type The cell destroying this cell, so this cell
     *             can only be destroyed by certain cells
     * @return Whether the cell can be destroyed
     */
    public boolean isDestructible(CellType type) { return true; }
}
