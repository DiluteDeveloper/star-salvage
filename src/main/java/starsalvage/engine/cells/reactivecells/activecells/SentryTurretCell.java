package starsalvage.engine.cells.reactivecells.activecells;

import starsalvage.engine.*;
import starsalvage.engine.cells.AsteroidCell;
import starsalvage.engine.cells.Cell;
import starsalvage.engine.cells.reactivecells.activecells.movingcells.MovingCell;
import starsalvage.engine.cells.reactivecells.activecells.movingcells.Ship;
import starsalvage.engine.interfaces.ISafeEngineAccess;

/**
 * Has 3 states that switch every tick and shoots in all directions in the FIRING state, 2 cells
 * in each direction. Asteroids in the closer cell in any given direction
 * block the shot for the further cell in that direction.
 */
public class SentryTurretCell extends ActiveCell {

    /**
     * Amount of score received for a ship moving onto a sentry turret
     * and destroying it
     */
    public final static int SENTRY_TURRET_SCORE_VALUE = 2;
    /**
     * Amount of HP lost when hit by a sentry turret or when
     * sentry turret is moved onto and destroyed when not in IDLE state
     */
    public final static int SENTRY_TURRET_HP_REDUCTION = 2;

    /**
     * An event called when a turret fires
     * @param pos Position of the turret
     * @param hitShip If turret hit the ship
     */
    public record TurretFireEvent(Vec2 pos, boolean hitShip) {};
    /**
     * An event called when a turret charges
     * @param pos Position of the turret
     */
    public record TurretChargeEvent(Vec2 pos) {};
    /**
     * An event called when a turret is in idle
     * @param pos Position of the turret
     */
    public record TurretIdleEvent(Vec2 pos) {};

    /**
     * States of the turret, changing each tick
     */
    public enum State {
        IDLE,
        CHARGING,
        FIRING;

        /**
         * Gets the previous state in order of
         * IDLE, CHARGING, FIRING.
         * @return Previous state
         */
        public State getPreviousState() {
            return switch(this) {
                case IDLE -> FIRING;
                case CHARGING -> IDLE;
                case FIRING -> CHARGING;
            };
        }
    }

    /**
     * Starts in charging state because starting in idle
     * means that the first tick is an idle as well as before
     * the first tick
     */
    public State state = State.CHARGING;

    /**
     * Sets the cell position and engine interface reference
     * @param pos The position of the cell
     * @param engine The engine interface
     */
    public SentryTurretCell(ISafeEngineAccess engine, Vec2 pos) {
        super(engine, pos);
    }

    /**
     * Copy the contents of SentryTurretCell
     * @return The copied SentryTurretCell as a Cell
     */
    @Override
    public Cell copy() {
        // Engine doesn't copy but should work for the use case
        SentryTurretCell c = new SentryTurretCell(engine, new Vec2(pos));
        c.state = state;
        return c;
    }

    /**
     * @return The CellType of the current cell class
     */
    @Override
    public CellType getCellType() {
        return CellType.SENTRY_TURRET_CELL;
    }

    /**
     * Called by GameEngine when this cell is entered by another cell
     * @param type CellType that is entering this cell
     */
    @Override
    public void onEntered(CellType type) {

        if(type==CellType.SHIP) {
            engine.getShip().addScore(SENTRY_TURRET_SCORE_VALUE, this);
            if(state != State.IDLE) {
                engine.getShip().reduceHP(SENTRY_TURRET_HP_REDUCTION, this);
            }
        }
    }

    /**
     * Called on GameTickEvent
     * Processes state change logic
     */
    public void onGameTickEvent(GameEngine.GameTickEvent e) {
        switch(state) {
            case IDLE:
                engine.publishEvent(new TurretIdleEvent(pos));
                state = State.CHARGING;
                break;
            case CHARGING:
                engine.publishEvent(new TurretChargeEvent(pos));
                state = State.FIRING;
                break;
            case FIRING:
                beginFireRoutine();
                state = State.IDLE;
        }
    }

    /**
     * Processes firing logic
     */
    private void beginFireRoutine() {
        MovingCell.Direction[] directions = MovingCell.Direction.values();
        boolean hitShip = false;
        for(MovingCell.Direction dir : directions) {
            var tryShip = engine.getOverlayCell(Vec2.add(pos, dir.moveOffset));

            if(tryShip.isSuccess)
                if(tryShip.getValueOrNull() instanceof Ship s) {
                    s.reduceHP(SENTRY_TURRET_HP_REDUCTION, this);
                    hitShip = true;
                    break;
                }
            else
                if(tryShip.getErrType() == Result.ErrType.OUT_OF_BOUNDS)
                    continue;

            var tryBlocked = engine.getCell(Vec2.add(pos, dir.moveOffset));
            if(tryBlocked.isSuccess) {
                if(tryBlocked.getValueOrNull().getCellType() == CellType.ASTEROID_CELL)
                    continue;
            }

            tryShip = engine.getOverlayCell(Vec2.add(pos, Vec2.mulScalar(dir.moveOffset, 2)));

            if(tryShip.isSuccess)
                if(tryShip.getValueOrNull() instanceof Ship s) {
                    s.reduceHP(SENTRY_TURRET_HP_REDUCTION, this);
                    hitShip = true;
                    break;
                }
                else
                if(tryShip.getErrType() == Result.ErrType.OUT_OF_BOUNDS)
                    continue;
        }
        engine.publishEvent(new TurretFireEvent(pos, hitShip));
    }

    /**
     * @param type The cell asking to move onto blocked cell,
     *             so only certain cells can be disallowed or allowed to move onto a blocked cell
     * @return Whether the cell cannot be entered or moved onto
     */
    @Override
    public boolean isBlocked(CellType type) {
        return type != Cell.CellType.SHIP;
    }

    /**
     * @return The CLI character representation of the cell type
     */
    @Override
    public String getCharacter() { return "T"; }

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
