package starsalvage.engine.cells.reactivecells.activecells.movingcells;

import starsalvage.engine.*;
import starsalvage.engine.cells.Cell;
import starsalvage.engine.interfaces.ISafeEngineAccess;

import java.util.*;

/**
 * Has three different states and custom activities in each state,
 * including chasing the ship, patrolling, and being alerted of
 * the ship's presence
 */
public class ScoutDroneCell extends MovingCell {

    /**
     * The amount to reduce the ship's HP on contact
     */
    public final static int SCOUT_DRONE_HP_REDUCTION = 2;
    /**
     * The amount to increase the ship's score on contact
     */
    public final static int SCOUT_DRONE_SCORE_VALUE = 2;
    /**
     * The sum of the cell distance to the ship required to trigger
     * ALERT state
     */
    public final static int SCOUT_DRONE_SCAN_RANGE = 3;

    /**
     * The state of the ScoutDroneCell to trigger state-specific
     * functionality
     */
    public enum State {
        PATROL,
        ALERT,
        CHASE
    }

    /**
     * Public only for testing
     */
    public State state = State.PATROL;

    /**
     * Seed is stored for determinism on save and load as well
     * as on rewind
     */
    private int seed = 0;

    /**
     * Sets the cell position and engine interface reference as well as the seed
     * from engine.getSeed()
     * @param pos The position of the cell
     * @param engine The engine interface
     */
    public ScoutDroneCell(ISafeEngineAccess engine, Vec2 pos) {
        super(engine, pos);
        this.seed = engine.getSeed();
    }
    /**
     * Copy the contents of ScoutDroneCell
     * @return The copied ScoutDroneCell as a Cell
     */
    @Override
    public Cell copy() {
        // Engine doesn't copy but should work for the use case
        ScoutDroneCell c = new ScoutDroneCell(engine, new Vec2(pos));
        c.state = state;
        return c;
    }

    /**
     * @return The CellType of the current cell class
     */
    @Override
    public CellType getCellType() {
        return CellType.SCOUT_DRONE_CELL;
    }

    /**
     * Called by GameEngine when this cell is collided with
     * or entered by another cell
     * @param type CellType that is colliding with or entering this cell
     */
    @Override
    public void onEngaged(CellType type) {
        // Shouldn't make a difference but good for future
        // compatibility
        if(type==CellType.SHIP) {
            engine.getShip().reduceHP(SCOUT_DRONE_HP_REDUCTION, this);
            engine.getShip().addScore(SCOUT_DRONE_SCORE_VALUE, this);
        }
    }

    /**
     * Called on GameTickEvent and controls current state routine
     */
    public void onGameTickEvent(GameEngine.GameTickEvent e) {
        switch(state) {
            case PATROL -> beginPatrolRoutine(e);
            case ALERT -> beginAlertRoutine(e);
            case CHASE -> beginChaseRoutine(e);
        }
        //System.out.println(pos.x + ", " + pos.y + " : " + state.name());
    }
    /**
     * Begins the routine for PATROL state, involving random moves around the board
     * until the ship is within scan range
     */
    private void beginPatrolRoutine(GameEngine.GameTickEvent event) {

        List<Direction> validDirections = new ArrayList<>();
        for(Direction dir : Direction.values()) {
            if(isValidMoveDirection(dir).isSuccess)
                validDirections.add(dir);
        }
        if(validDirections.isEmpty())
            return;
        Random rand = new Random(seed);
        // Deterministic between load and rewind,
        // but still different for different scout drones.
        seed += pos.x * pos.y;
        move(validDirections.get(rand.nextInt(validDirections.size())));
        // 368121068
        if(isInRange(engine.getShip().pos))
            state = State.ALERT;
    }
    /**
     * Begins the routine for ALERT state, involving checking if the ship
     * is still within scan range, and changing to CHASE if so,
     * back to PATROL if out of range
     */
    private void beginAlertRoutine(GameEngine.GameTickEvent event) {
        if(isInRange(engine.getShip().pos))
            state = State.CHASE;
        else
            state = State.PATROL;
    }
    /**
     * Begins the routine for CHASE state, involving taking the shortest
     * route to move towards the player
     */
    private void beginChaseRoutine(GameEngine.GameTickEvent event) {
        int xDist = Math.abs(engine.getShip().pos.x - pos.x);
        int yDist = Math.abs(engine.getShip().pos.y - pos.y);
        Direction moveDirection;
        if(xDist <= yDist)
            moveDirection = Direction.fromMoveOffset(new Vec2(0, Integer.signum(engine.getShip().pos.y - pos.y)));
        else
            moveDirection = Direction.fromMoveOffset(new Vec2(Integer.signum(engine.getShip().pos.x - pos.x), 0));

        if(moveDirection==null) {
            LOGGER.warning("Scout Drone computed an invalid move direction!");
            return;
        }

        move(moveDirection);

        if(!isInRange(engine.getShip().pos))
            state = State.PATROL;
    }

    /**
     * @param shipPos The position of the ship
     * @return If the ship is in scan range
     */
    private boolean isInRange(Vec2 shipPos) {
        return (Math.abs(pos.x - shipPos.x) + Math.abs(pos.y - shipPos.y)) <=
                SCOUT_DRONE_SCAN_RANGE;
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

    /**
     * @return The CLI character representation of the cell type
     */
    @Override
    public String getCharacter() { return "D"; }

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
