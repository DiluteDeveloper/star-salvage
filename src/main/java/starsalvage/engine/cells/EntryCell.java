package starsalvage.engine.cells;

import starsalvage.engine.Vec2;
import starsalvage.engine.interfaces.ISafeEngineAccess;

/**
 * The cell that the player enters the game on top of
 */
public final class EntryCell extends Cell {

    /**
     * Sets the cell position
     * @param pos The position of the cell
     */
    public EntryCell(Vec2 pos) {
        super(pos);
    }

    /**
     * Copy the contents of EntryCell
     * @return The copied EntryCell as a Cell
     */
    @Override
    public Cell copy() {
        return new EntryCell(new Vec2(pos));
    }

    /**
     * @return The CellType of the current cell class
     */
    @Override
    public CellType getCellType() {
        return CellType.ENTRY_CELL;
    }

    /**
     * @return The CLI character representation of the cell type
     */
    @Override
    public String getCharacter() { return "E"; }

}
