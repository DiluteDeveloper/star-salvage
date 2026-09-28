package starsalvage.engine.cells.reactivecells;

import starsalvage.engine.cells.Cell;
import starsalvage.engine.Vec2;
import starsalvage.engine.interfaces.ISafeEngineAccess;

/**
 * Increments player score by 2 and is consumed when entered
 */
public class ScrapCargoCell extends ReactiveCell {

    /**
     * How much to increase player score when ship enters ScrapCargoCell position
     */
    public static final int PICKUP_SCORE_VALUE = 2;

    /**
     * Sets the cell position and engine interface reference
     * @param pos The position of the cell
     * @param engine The engine interface
     */
    public ScrapCargoCell(ISafeEngineAccess engine, Vec2 pos) {
        super(engine, pos);
    }

    /**
     * Copy the contents of ScrapCargoCell
     * @return The copied ScrapCargoCell as a Cell
     */
    @Override
    public Cell copy() {
        return new ScrapCargoCell(engine, new Vec2(pos));
    }

    /**
     * @return The CellType of the current cell class
     */
    @Override
    public CellType getCellType() {
        return Cell.CellType.SCRAP_CARGO_CELL;
    }

    /**
     * Called by GameEngine when this cell is entered by another cell
     * @param type CellType that is entering this cell
     */
    @Override
    public void onEntered(CellType type) {
        if(type==CellType.SHIP)
            engine.getShip().addScore(PICKUP_SCORE_VALUE, this);
    }

    /**
     * @return The CLI character representation of the cell type
     */
    @Override
    public String getCharacter() { return "C"; }

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
