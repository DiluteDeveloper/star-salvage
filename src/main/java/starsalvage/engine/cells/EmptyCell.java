package starsalvage.engine.cells;

import starsalvage.engine.Vec2;

/**
 * Represents a cell with no behaviour or attributes
 */
public class EmptyCell extends Cell {

    /**
     * Sets the cell position
     * @param pos The position of the cell
     */
    public EmptyCell(Vec2 pos) {
        super(pos);
    }

    /**
     * Copy the contents of EmptyCell
     * @return The copied EmptyCell as a Cell
     */
    @Override
    public Cell copy() {
        return new EmptyCell(new Vec2(pos));
    }
    /**
     * @return The CellType of the current cell class
     */
    @Override
    public CellType getCellType() { return CellType.EMPTY_CELL; }
    /**
     * @return The CLI character representation of the cell type
     */
    @Override
    public String getCharacter() { return " "; }

    /**
     * Static version of getCharacter()
     * @return The CLI character representation of the cell type
     */
    public static String getCharacterStatic() { return " "; }

}
