package starsalvage.engine.cells.reactivecells;

import starsalvage.engine.cells.Cell;
import starsalvage.engine.Vec2;
import starsalvage.engine.interfaces.ISafeEngineAccess;

/**
 * Increases the player's HP when entered and is consumed
 */
public class HullPatchCell extends ReactiveCell {

    /**
     * Amount to increase player's HP by when entered
     */
    public static final int HULL_PATCH_RESTORATION_AMOUNT = 4;

    /**
     * Sets the cell position and engine interface reference
     * @param pos The position of the cell
     * @param engine The engine interface
     */
    public HullPatchCell(ISafeEngineAccess engine, Vec2 pos) {
        super(engine, pos);
    }

    /**
     * Copy the contents of HullPatchCell
     * @return The copied HullPatchCell as a Cell
     */
    @Override
    public Cell copy() {
        return new HullPatchCell(engine, new Vec2(pos));
    }

    /**
     * @return The CellType of the current cell class
     */
    @Override
    public CellType getCellType() {
        return CellType.HULL_PATCH_CELL;
    }

    /**
     * Called by GameEngine when this cell is entered by another cell
     * @param type CellType that is entering this cell
     */
    @Override
    public void onEntered(CellType type) {
        if(type==CellType.SHIP)
            engine.getShip().restoreHP(HULL_PATCH_RESTORATION_AMOUNT, this);
    }

    /**
     * @return The CLI character representation of the cell type
     */
    @Override
    public String getCharacter() { return "H"; }

    /**
     * @param type The cell consuming this cell, so this cell
     *             can only be consumed by certain cells
     * @return Whether the cell can be consumed
     */
    @Override
    public boolean isConsumable(CellType type) {
        return type == CellType.SHIP;
    }
}
