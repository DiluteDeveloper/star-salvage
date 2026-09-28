package starsalvage.engine.cells.reactivecells.activecells.movingcells;

import starsalvage.engine.*;
import starsalvage.engine.cells.Cell;
import starsalvage.engine.eventsystem.EventListener;
import starsalvage.engine.interfaces.ISafeEngineAccess;

import static starsalvage.engine.Result.ErrType.NO_DRILL_CHARGES_AVAILABLE;

/**
 * The ship is the player character that navigates the sectors
 * deploying drills, waiting, rewinding, taking damage, earning score and moving
 */
public class Ship extends MovingCell {

    /**
     * An event triggered upon losing HP
     * @param ship Ship that lost HP
     * @param lostHP amount of HP lost
     * @param publisher Cell that caused the HP loss
     */
    public record LoseHPEvent(Ship ship, int lostHP, Cell publisher) {};
    /**
     * An event triggered upon restoring HP
     * @param ship Ship that restored HP
     * @param hpRestored amount of HP restored
     * @param publisher Cell that caused the HP restoration
     */
    public record RestoreHPEvent(Ship ship, int hpRestored, Cell publisher) {};
    /**
     * An event triggered upon picking up a drill charge
     * @param numDrillCharges Number of drill charges available
     */
    public record PickupDrillChargeEvent(int numDrillCharges) {};
    /**
     * An event triggered upon consuming a drill charge
     * @param numDrillCharges Number of drill charges available
     * @param cellDestroyed Whether a cell was destroyed by the drill charge
     * @param targetPos The position that was targeted by the drill
     */
    public record ConsumeDrillChargeEvent(int numDrillCharges, boolean cellDestroyed, Vec2 targetPos) {};
    /**
     * An event triggered upon increasing score
     * @param scoreIncrease The amount of score increased
     * @param newScore The new score amount
     * @param publisher Cell that caused the score increase
     */
    public record IncreaseScoreEvent(int scoreIncrease, int newScore, Cell publisher) {};

    /**
     * Max number of steps until game over
     */
    public final static int MAX_STEPS = 100;
    /**
     * Max ship HP
     */
    public final static int MAX_HP = 10;

    /**
     * Current number of steps taken by ship; each step represents a game tick
     */
    private int stepCount = 0;
    /**
     * Current Ship HP
     */
    private int HP = MAX_HP;
    /**
     * Current number of drill charges
     */
    private int numDrillCharges = 0;
    /**
     * Current score used for highscores and to
     * encourage the player to take certain actions
     */
    private int score = 0;

    /**
     * Sets the cell position and engine interface reference
     * @param pos The position of the cell
     * @param engine The engine interface
     */
    public Ship(ISafeEngineAccess engine, Vec2 pos) {
        super(engine, pos);
    }
    /**
     * Subscribe cell to requested events
     */
    @Override
    public void subscribe() {
        super.subscribe();
        endGameEventListener = this::onEndGameEvent;
        engine.subscribeToEvent(GameEngine.EndGameEvent.class, endGameEventListener);
    }
    /**
     * Unsubscribe cell from subscribed events
     */
    @Override
    public void unsubscribe() {
        super.unsubscribe();
        engine.unsubscribeFromEvent(GameEngine.EndGameEvent.class, endGameEventListener);
    }

    /**
     * An instance of an event listener, required to be able to unsubscribe the cell
     */
    protected transient EventListener<GameEngine.EndGameEvent> endGameEventListener;

    /**
     * Copy the contents of Ship
     * @return The copied Ship as a Cell
     */
    @Override
    public Cell copy() {
        // Engine doesn't copy but should work for the use case
        Ship c = new Ship(engine, new Vec2(pos));
        c.stepCount = stepCount;
        c.HP = HP;
        c.numDrillCharges = numDrillCharges;
        c.score = score;
        return c;
    }

    /**
     * Increments drill charge count and triggers PickupDrillChargeEvent
     */
    public void pickupDrillCharge() {
        numDrillCharges++;
        engine.publishEvent(new PickupDrillChargeEvent(numDrillCharges));
    }
    /**
     * Triggers drill charge consumption used to destroy asteroids near the
     * ship and calls TakeHistorySnapshotEvent, GamePreTickEvent,
     * GameTickEvent and ConsumeDrillChargeEvent
     * @return Ok if drill charge was used and destroyed an asteroid,
     * GAME_NOT_STARTED if game is not in progress,
     * NO_DRILL_CHARGES_AVAILABLE if drill charge count is 0
     * NON_DESTRUCTIBLE_CELL if targeted cell is non-destructible
     */
    public Result<?> consumeDrillCharge(Direction direction) {
        if(engine.getGameState() != GameEngine.GameState.IN_PROGRESS)
            return Result.err(Result.ErrType.GAME_NOT_STARTED, "Cannot drill when game is not in progress!", LOGGER);
        if(numDrillCharges==0)
            return Result.err(NO_DRILL_CHARGES_AVAILABLE, "No drill charges available!", LOGGER);
        Vec2 targetPos = Vec2.add(pos, direction.moveOffset);

        engine.publishEvent(new GameEngine.TakeHistorySnapshotEvent());

        numDrillCharges--;

        Result<?> destroy = engine.tryDestroyCell(targetPos, getCellType());

        engine.publishEvent(new GameEngine.GamePreTickEvent());
        engine.publishEvent(new GameEngine.GameTickEvent());
        engine.publishEvent(new ConsumeDrillChargeEvent(numDrillCharges, destroy.isSuccess, targetPos));

        return destroy;
    }
    public int getStepCount() { return stepCount; }
    public int getNumDrillCharges() { return numDrillCharges; }
    public int getHP() { return HP;}

    /**
     * Restores HP and calls RestoreHPEvent
     * @param amount The amount to restore
     * @param publisher The cell causing the HP increase
     */
    public void restoreHP(int amount, Cell publisher) {
        int oldHP = HP;
        HP+=amount;
        HP = Math.min(HP, MAX_HP);
        engine.publishEvent(new RestoreHPEvent(this, HP - oldHP, publisher));
    }
    /**
     * Triggers next tick but doesn't take any action;
     * calls TakeHistorySnapshotEvent, GamePreTickEvent and GameTickEvent
     * @return Ok if successful,
     * GAME_NOT_STARTED if game is not in progress
     */
    public Result<?> waitForTick() {
        if(engine.getGameState() != GameEngine.GameState.IN_PROGRESS)
            return Result.err(Result.ErrType.GAME_NOT_STARTED, "Cannot wait when game is not in progress!", LOGGER);
        engine.publishEvent(new GameEngine.TakeHistorySnapshotEvent());
        engine.publishEvent(new GameEngine.GamePreTickEvent());
        engine.publishEvent(new GameEngine.GameTickEvent());
        return Result.ok(null);
    }

    /**
     * @return The CellType of the current cell class
     */
    @Override
    public CellType getCellType() {
        return CellType.SHIP;
    }

    /**
     * Reduces Ship HP and calls EndGameEvent if HP drops to 0
     * @param amount Amount to reduce by
     * @param publisher The cell that caused the HP reduction
     */
    public void reduceHP(int amount, Cell publisher) {
        HP-=amount;
        engine.publishEvent(new LoseHPEvent(this, amount, publisher));
        if(HP <= 0) {
            score = -1;
            engine.publishEvent(new GameEngine.EndGameEvent(GameEngine.GameState.SHIP_DESTROYED));
        }
    }
    /**
     * @return The CLI character representation of the cell type
     */
    @Override
    public String getCharacter() { return "S"; }

    /**
     * Override of {@link MovingCell#move(Direction)}
     * that also triggers TakeHistorySnapshotEvent and GamePreTickEvent
     * @param dir Direction to move to
     * @return Ok & Position of move,
     * MOVE_ONTO_BLOCKED_CELL if cell is blocked,
     * OUT_OF_BOUNDS if move candidate is out of bounds,
     * GAME_NOT_STARTED if game has not started,
     * INVALID_MOVE if move is not one cell in magnitude
     */
    @Override
    public Result<Vec2> move(Direction dir) {

        Vec2 oldPos = pos;
        Result<Vec2> result = super.isValidMoveDirection(dir);
        if(!result.isSuccess)
            return result;

        var cellAtPos = engine.getCell(result.getValueOrNull());

        boolean hitWormhole = false;
        if(cellAtPos.isSuccess)
            hitWormhole = cellAtPos.getValueOrNull().getCellType()==CellType.WORMHOLE_CELL;

        engine.publishEvent(new GameEngine.TakeHistorySnapshotEvent());

        pos = result.getValueOrNull();

        engine.publishEvent(new GameEngine.GamePreTickEvent());
        engine.publishEvent(new MoveEvent(oldPos, result.getValueOrNull(), dir, this));

        // To stop ticking happening on new sector
        if(!hitWormhole) {
            engine.publishEvent(new GameEngine.GameTickEvent());
        }
        else { // trigger step count increment
            onGameTickEvent(new GameEngine.GameTickEvent());
        }

        return result;
    }
    public int getScore() { return score; }

    /**
     * Increases score and triggers IncreaseScoreEvent
     * @param score The amount to increase score by
     * @param publisher The cell that caused the score increase
     * @return New score
     */
    public int addScore(int score, Cell publisher) {
        this.score += score;
        engine.publishEvent(new IncreaseScoreEvent(score, this.score, publisher));
        return score;
    }

    /**
     * Called on GameTickEvent and increments stepCount,
     * triggers EndGameEvent if MAX_STEPS is reached
     */
    public void onGameTickEvent(GameEngine.GameTickEvent e) {
        stepCount++;
        if(stepCount==MAX_STEPS) {
            score = -1;
            engine.publishEvent(new GameEngine.EndGameEvent(GameEngine.GameState.OUT_OF_STEPS));
        }
    }
    /**
     * Called on EndGameEvent and sets stepCount
     * to -1 if the end game result was OUT_OF_STEPS
     * or SHIP_DESTROYED
     */
    public void onEndGameEvent(GameEngine.EndGameEvent event) {
        if(event.result()== GameEngine.GameState.OUT_OF_STEPS || event.result()== GameEngine.GameState.SHIP_DESTROYED)
            score = -1;
    }
}
