package starsalvage.engine.cells.reactivecells;

import starsalvage.engine.cells.Cell;
import starsalvage.engine.Vec2;
import starsalvage.engine.interfaces.ISafeEngineAccess;

/**
 * When entered by a Ship, begins the next sector
 */
public class WormholeCell extends ReactiveCell {

    /**
     * Sets the cell position and engine interface reference
     * @param pos The position of the cell
     * @param engine The engine interface
     */
    public WormholeCell(ISafeEngineAccess engine, Vec2 pos) {
        super(engine, pos);
    }

    /**
     * Copy the contents of WormholeCell
     * @return The copied WormholeCell as a Cell
     */
    @Override
    public Cell copy() {
        return new WormholeCell(engine, new Vec2(pos));
    }

    /**
     * @return The CellType of the current cell class
     */
    @Override
    public CellType getCellType() {
        return CellType.WORMHOLE_CELL;
    }

    @Override
    public void onEntered(CellType type) {
        if(type==CellType.SHIP)
            engine.tryBeginNextSector();
    }

    /**
     * @return The CLI character representation of the cell type
     */
    @Override
    public String getCharacter() { return "W"; }
}
