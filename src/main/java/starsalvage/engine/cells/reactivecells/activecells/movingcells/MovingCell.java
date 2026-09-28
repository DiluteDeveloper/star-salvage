package starsalvage.engine.cells.reactivecells.activecells.movingcells;

import starsalvage.engine.*;
import starsalvage.engine.cells.reactivecells.activecells.ActiveCell;
import starsalvage.engine.interfaces.ISafeEngineAccess;

/**
 * Abstract cell class used for cells with the ability to move
 */
public abstract class MovingCell extends ActiveCell {

    /**
     * Contains all the directions a cell can move
     */
    public enum Direction {
        LEFT(new Vec2(-1, 0)),
        RIGHT(new Vec2(1, 0)),
        UP(new Vec2(0, -1)),
        DOWN(new Vec2(0, 1));

        /**
         * The x and y offset of the move direction
         */
        public final Vec2 moveOffset;

        Direction(Vec2 moveOffset) {
            this.moveOffset = moveOffset;
        }

        /**
         * @param moveOffset The moveOffset of the direction
         * @return The direction corresponding to the moveOffset
         */
        public static Direction fromMoveOffset(Vec2 moveOffset) {
            for (Direction d : values()) {
                if (d.moveOffset.x == moveOffset.x && d.moveOffset.y == moveOffset.y) return d;
            }
            return null;
        }
    }

    /**
     * An event called when any cell moves
     * @param oldPos The old position of the moving cell
     * @param newPos The new position of the moving cell
     * @param direction The direction the cell moved
     * @param movingCell The moving cell
     */
    public record MoveEvent (Vec2 oldPos, Vec2 newPos, Direction direction, MovingCell movingCell) {};

    /**
     * Sets the cell position and engine interface reference
     * @param pos The position of the cell
     * @param engine The engine interface
     */
    public MovingCell(ISafeEngineAccess engine, Vec2 pos) {
        super(engine, pos);
    }

    /**
     * Check whether a direction is a valid move candidate
     * @param dir The direction to move
     * @return Ok & Position of move,
     * MOVE_ONTO_BLOCKED_CELL if cell is blocked,
     * OUT_OF_BOUNDS if move candidate is out of bounds,
     * GAME_NOT_STARTED if game has not started,
     * INVALID_MOVE if move is not one cell in magnitude
     */
    protected Result<Vec2> isValidMoveDirection(Direction dir) {

        if(engine.getGameState() != GameEngine.GameState.IN_PROGRESS)
            return Result.err(Result.ErrType.GAME_NOT_STARTED, "Cannot move when game is not in progress!", LOGGER);

        if(Math.abs(dir.moveOffset.x + dir.moveOffset.y)!=1)
            return Result.err(Result.ErrType.INVALID_MOVE, "Tried to move more than one space at a time!", LOGGER);
        Vec2 dirPos = Vec2.add(dir.moveOffset, pos);
        var cellAtPos = engine.getCell(dirPos);
        var overlayCellAtPos = engine.getOverlayCell(dirPos);


        if(cellAtPos.isSuccess)
            if(cellAtPos.getValueOrNull().isBlocked(getCellType()))
                return Result.err(Result.ErrType.MOVE_ONTO_BLOCKED_CELL, "Tried to move onto a blocked cell!", LOGGER);

        if(overlayCellAtPos.isSuccess)
            if(overlayCellAtPos.getValueOrNull().isBlocked(getCellType()))
                return Result.err(Result.ErrType.MOVE_ONTO_BLOCKED_CELL, "Tried to move onto a blocked cell!", LOGGER);

        if(dirPos.x < 0 || dirPos.x >= GameEngine.BOARD_SIZE.x ||
                dirPos.y < 0 || dirPos.y >= GameEngine.BOARD_SIZE.y)
            return Result.err(Result.ErrType.OUT_OF_BOUNDS, "Tried to move out of bounds!", LOGGER);

        return Result.ok(dirPos);
    }

    /**
     * Move the cell to a new position and trigger a MoveEvent
     * @param dir Direction to move to
     * @return Ok & Position of move,
     * MOVE_ONTO_BLOCKED_CELL if cell is blocked,
     * OUT_OF_BOUNDS if move candidate is out of bounds,
     * GAME_NOT_STARTED if game has not started,
     * INVALID_MOVE if move is not one cell in magnitude
     */
    public Result<Vec2> move(Direction dir) {
        Vec2 oldPos = pos;
        var result = isValidMoveDirection(dir);
        if(!result.isSuccess)
            return result;

        Vec2 newPos = result.getValueOrNull();
        pos = newPos;

        engine.publishEvent(new MoveEvent(oldPos, newPos, dir, this));

        return Result.ok(newPos);
    }

    /**
     * Called by GameEngine when this cell is entered by another cell
     * @param type CellType that is entering this cell
     */
    public void onEntered(CellType type) {}
    /**
     * Called by GameEngine when this cell is collided with by another cell
     * @param type CellType that is colliding with this cell
     */
    public void onCollided(CellType type) {}
    /**
     * Called by GameEngine when this cell is collided with
     * or entered by another cell
     * @param type CellType that is colliding with or entering this cell
     */
    public void onEngaged(CellType type) {}

}
