package starsalvage.engine.cells.reactivecells;

import starsalvage.engine.*;
import starsalvage.engine.cells.Cell;
import starsalvage.engine.interfaces.ISafeEngineAccess;

/**
 * Reduces the player HP when entered and is not consumed
 */
public class GravityWellCell extends ReactiveCell {

    /**
     * Amount to reduce player HP when entered by
     */
    public static final int GRAVITY_WELL_HP_REDUCTION = 2;

    /**
     * Sets the cell position and engine interface reference
     * @param pos The position of the cell
     * @param engine The engine interface
     */
    public GravityWellCell(ISafeEngineAccess engine, Vec2 pos) {
        super(engine, pos);
    }

    /**
     * Copy the contents of GravityWellCell
     * @return The copied GravityWellCell as a Cell
     */
    @Override
    public Cell copy() {
        return new GravityWellCell(engine, new Vec2(pos));
    }

    /**
     * @return The CellType of the current cell class
     */
    @Override
    public CellType getCellType() {
        return CellType.GRAVITY_WELL_CELL;
    }

    /**
     * Called by GameEngine when this cell is entered by another cell
     * @param type CellType that is entering this cell
     */
    @Override
    public void onEntered(CellType type) {
        if(type==CellType.SHIP)
            engine.getShip().reduceHP(GRAVITY_WELL_HP_REDUCTION, this);
    }

    /**
     * @return The CLI character representation of the cell type
     */
    @Override
    public String getCharacter() { return "X"; }
}
