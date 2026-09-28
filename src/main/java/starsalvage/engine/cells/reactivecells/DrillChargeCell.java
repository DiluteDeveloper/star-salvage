package starsalvage.engine.cells.reactivecells;

import starsalvage.engine.cells.Cell;
import starsalvage.engine.Vec2;
import starsalvage.engine.interfaces.ISafeEngineAccess;

/**
 * Increments the player's drill charge count when entered and is consumed
 */
public class DrillChargeCell extends ReactiveCell {

    /**
     * Sets the cell position and engine interface reference
     * @param pos The position of the cell
     * @param engine The engine interface
     */
    public DrillChargeCell(ISafeEngineAccess engine, Vec2 pos) {
        super(engine, pos);
    }

    /**
     * Copy the contents of DrillChargeCell
     * @return The copied DrillChargeCell as a Cell
     */
    @Override
    public Cell copy() {
        return new DrillChargeCell(engine, new Vec2(pos));
    }

    /**
     * @return The CellType of the current cell class
     */
    @Override
    public CellType getCellType() {
        return CellType.DRILL_CHARGE_CELL;
    }

    /**
     * Called by GameEngine when this cell is entered by another cell
     * @param type CellType that is entering this cell
     */
    @Override
    public void onEntered(CellType type) {

        if(type==CellType.SHIP)
            engine.getShip().pickupDrillCharge();
    }

    /**
     * @return The CLI character representation of the cell type
     */
    @Override
    public String getCharacter() { return "B"; }

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
