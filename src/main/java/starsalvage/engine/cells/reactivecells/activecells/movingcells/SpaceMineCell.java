package starsalvage.engine.cells.reactivecells.activecells.movingcells;

import starsalvage.engine.cells.Cell;
import starsalvage.engine.GameEngine;
import starsalvage.engine.Vec2;
import starsalvage.engine.interfaces.ISafeEngineAccess;

import java.util.Random;

/**
 * Drifts in a random direction and turns around when hitting a blocked cell,
 * Hurts the ship on contact
 */
public class SpaceMineCell extends MovingCell {

    /**
     * Amount to reduce ship HP on contact
     */
    public final static int SPACE_MINE_HP_REDUCTION = 4;
    /**
     * Amount to increase ship score on contact
     */
    public final static int SPACE_MINE_SCORE_VALUE = 1;

    /**
     * Current direction of the space mine
     */
    private Direction curDirection;

    /**
     * Sets the cell position and engine interface reference
     * @param pos The position of the cell
     * @param engine The engine interface
     */
    public SpaceMineCell(ISafeEngineAccess engine, Vec2 pos) {
        super(engine, pos);
        Direction[] dirs = Direction.values();
        Random rand = new Random(engine.getSeed());
        curDirection = dirs[rand.nextInt(dirs.length)];
    }
    /**
     * Copy the contents of SpaceMineCell
     * @return The copied SpaceMineCell as a Cell
     */
    @Override
    public Cell copy() {
        SpaceMineCell c = new SpaceMineCell(engine, new Vec2(pos));
        c.curDirection = curDirection;

        return c;
    }

    /**
     * Called on GameTickEvent and performs functionality to move
     * space mine in direction or reverse direction
     */
    public void onGameTickEvent(GameEngine.GameTickEvent event) {

        // I want the space mine to keep checking if it is stuck
       var result = move(curDirection);
       if(result.isSuccess)
           return;

       Direction newDirection = Direction.fromMoveOffset(new Vec2(-curDirection.moveOffset.x, -curDirection.moveOffset.y));

       if(newDirection==null) {
            LOGGER.warning("SpaceMineCell computed an invalid move direction!");
            return;
       }
       move(newDirection);
       curDirection = newDirection;
    }

    /**
     * @return The CellType of the current cell class
     */
    @Override
    public CellType getCellType() {
        return CellType.SPACE_MINE_CELL;
    }

    /**
     * Called by GameEngine when this cell is collided with
     * or entered by another cell
     * @param type CellType that is colliding with or entering this cell
     */
    @Override
    public void onEngaged(CellType type) {
        if(type==CellType.SHIP) {
            engine.getShip().reduceHP(SPACE_MINE_HP_REDUCTION, this);
            engine.getShip().addScore(SPACE_MINE_SCORE_VALUE, this);
        }
    }

    /**
     * @return The CLI character representation of the cell type
     */
    @Override
    public String getCharacter() { return "M"; }

    /**
     * @param type The cell consuming this cell, so this cell
     *             can only be consumed by certain cells
     * @return Whether the cell can be consumed
     */
    @Override
    public boolean isConsumable(CellType type) {
        return type == CellType.SHIP;
    }

    /**
     * @param type The cell asking to move onto blocked cell,
     *             so only certain cells can be disallowed or allowed to move onto a blocked cell
     * @return Whether the cell cannot be entered or moved onto
     */
    @Override
    public boolean isBlocked(CellType type) {
        return type != CellType.SHIP;
    }

}
