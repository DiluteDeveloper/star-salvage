package starsalvage.engine;

import starsalvage.engine.cells.Cell;
import starsalvage.engine.cells.reactivecells.ReactiveCell;
import starsalvage.engine.cells.reactivecells.activecells.movingcells.Ship;
import starsalvage.engine.cells.reactivecells.activecells.movingcells.MovingCell;
import starsalvage.engine.eventsystem.*;
import starsalvage.engine.interfaces.ISafeEngineAccess;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Bodie Bishop
 * Hosts all primary game logic
 */
public class GameEngine implements ISafeEngineAccess {

    /**
     * Represents all the possible game states
     */
    public enum GameState {
        IN_PROGRESS,
        NOT_STARTED,
        SHIP_DESTROYED,
        WIN,
        OUT_OF_STEPS
    } protected GameState gameState = GameEngine.GameState.NOT_STARTED;

    /**
     * An event called when a history snapshot is taken
     */
    public record TakeHistorySnapshotEvent() {};
    /**
     * An event called when a cell collision occurs
     * @param movingCell The cell that was moving/caused the collision
     * @param collidedCell The cell that was collided with
     */
    public record CellCollisionEvent(MovingCell movingCell, Cell collidedCell) {};
    /**
     * An event called when the player performs any ticked action
     */
    public record GameTickEvent() {};
    /**
     * An event called before GameTickEvent when the player performs any ticked action
     */
    public record GamePreTickEvent() {};
    /**
     * An event called when a sector begins
     */
    public record BeginSectorEvent() {};
    /**
     * An event called the game ends
     * @param result The game state when the game ends
     *
     */
    public record EndGameEvent(GameState result) {};
    /**
     * An event called when a rewind occurs
     */
    public record RewindEvent() {};
    /**
     * An event called after RewindEvent when a rewind occurs
     */
    public record PostRewindEvent() {};

    /**
     * Used to log to the console
     */
    protected final Logger LOGGER = Logger.getLogger(GameEngine.class.getName());

    private final HighscoreController highscoreController;
    protected Sector sector;

    /**
     * Copies of the sector each tick so it can be restored on rewind
     */
    protected List<Sector> sectorHistory = new ArrayList<Sector>();
    protected Ship ship;

    /**
     * Size of the game grid/board
     */
    public static final Vec2 BOARD_SIZE = new Vec2(10);
    /**
     * Number of game sectors
     */
    public static final int NUM_SECTORS = 2;
    /**
     * Number of times player can rewind
     */
    public static final int MAX_RECORDING_HISTORY_LENGTH = 5;
    /**
     * The maximum game difficulty
     */
    public static final int MAX_DIFFICULTY = 10;
    /**
     * The minimum game difficulty
     */
    public static final int MIN_DIFFICULTY = 0;

    public final EventBus bus = new EventBus();

    /**
     * Index of current sector: 0 before game starts, then increments by 1
     * when each sector is loaded
     */
    private int sectorIdx = 0;

    /**
     * Current game difficulty, incremented by 2 for each subsequent
     * sector until hitting MAX_DIFFICULTY
     */
    private int difficulty = 3;
    /**
     * Random seed
     */
    private int seed;

    /**
     * Initialises game engine variables and events
     * @param seed Random seed
     */
    public GameEngine(int seed) {

        this.seed = seed;

        ship = new Ship(this, new Vec2(0, BOARD_SIZE.y - 1));
        ship.subscribe();

        this.highscoreController = new HighscoreController(this);
        Result<?> result = highscoreController.load();
        if(!result.isSuccess)
            result.printErr();

        bus.subscribe(MovingCell.MoveEvent.class, this::onMoveEvent);
        bus.subscribe(CellCollisionEvent.class, this::onCellCollisionEvent);
        bus.subscribe(EndGameEvent.class, this::onEndGameEvent);
        bus.subscribe(TakeHistorySnapshotEvent.class, this::onTakeHistorySnapshotEvent);
    }
    public Ship getShip() { return ship; }

    /**
     * see {@link Sector#getCell(Vec2)}
     */
    public Result<Cell> getCell(Vec2 pos) {
        return sector.getCell(pos);
    }
    /**
     * see {@link Sector#getOverlayCell(Vec2)}
     */
    public Result<Cell> getOverlayCell(Vec2 pos) {
        return sector.getOverlayCell(pos);
    }

    /**
     * Starts the next sector and publishes BeginSectorEvent
     */
    public void tryBeginNextSector() {
        gameState = GameEngine.GameState.IN_PROGRESS;
        if(sectorIdx >= NUM_SECTORS) {
            bus.publish(new EndGameEvent(GameEngine.GameState.WIN));
            return;
        }
        if(sectorIdx != 0) {
            difficulty = Math.min(difficulty + 2, MAX_DIFFICULTY);
            seed += 10000;
            sector.unsubscribe();
        }
        Result<Sector> newSector = Sector.generateSector(this);
        if(!newSector.isSuccess) {
            newSector.printErr();
            return;
        }
        sector = newSector.getValueOrNull();
        sectorIdx++;

        sectorHistory.clear();

        bus.publish(new BeginSectorEvent());
    }

    public Sector getSector() {
        return sector;
    }
    /**
     * Attempts to destroy a destructible cell
     * @param pos Cell position
     * @param destroyerType Type of cell attempting to destroy cell
     * @return Ok if cell was destroyed or doesn't exist,
     * NON_DESTRUCTIBLE_CELL if cell is non-destructible
     */
    public Result<?> tryDestroyCell(Vec2 pos, Cell.CellType destroyerType) {
        Result<Cell> cell = sector.getCell(pos);
        if(cell.isSuccess) {
            if(cell.getValueOrNull().isDestructible(destroyerType)) {
                sector.removeCell(cell.getValueOrNull());
                return Result.ok(null);
            }
            return Result.err(Result.ErrType.NON_DESTRUCTIBLE_CELL, "Cell is not destructible!", LOGGER);
        }
        return cell;
    }


    public int getSectorIdx() { return sectorIdx; }

    /**
     * see {@link EventBus#publish(Object)}
     */
    public <T> void publishEvent(T event) {
        bus.publish(event);
    }
    /**
     * see {@link EventBus#subscribe(Class, EventListener)}
     */
    public <T> void subscribeToEvent(Class<T> eventType, EventListener<T> listener) {
        bus.subscribe(eventType, listener);
    }
    /**
     * see {@link EventBus#unsubscribe(Class, EventListener)}
     */
    public <T> void unsubscribeFromEvent(Class<T> eventType, EventListener<T> listener) {
        bus.unsubscribe(eventType, listener);
    }

    /**
     * Called on CellCollisionEvent and triggers OnCollided, OnEngaged, and OnEntered for relevant cells
     */
    public void onCellCollisionEvent(CellCollisionEvent event) {
        event.movingCell().onCollided(event.collidedCell.getCellType());
        event.movingCell().onEngaged(event.collidedCell.getCellType());

        if(event.collidedCell() instanceof ReactiveCell r) {
            r.onEntered(event.movingCell.getCellType());
            if(event.collidedCell instanceof MovingCell m) {
                m.onEngaged(event.movingCell.getCellType());
            }
        }
    }

    /**
     * Called on MoveEvent and triggers subsequent CellCollisionEvent if applicable
     * and removes consumable cells
     */
    public void onMoveEvent(MovingCell.MoveEvent event) {
        var cellResult = sector.getCell(event.newPos());
        if(cellResult.isSuccess) {
            Cell cell = cellResult.getValueOrNull();

            bus.publish(new CellCollisionEvent(event.movingCell(), cellResult.getValueOrNull()));
            if(cell.isConsumable(event.movingCell().getCellType())) {
                sector.removeCell(cell);
            }
        }

        var overlayResult = sector.getOverlayCells(event.newPos());
        if(overlayResult.isSuccess) {
            List<Cell> cells = overlayResult.getValueOrNull();

            for(Cell c : cells) {
                if(c==event.movingCell())
                    continue;

                bus.publish(new CellCollisionEvent(event.movingCell(), c));
                if(c.isConsumable(event.movingCell().getCellType())) {
                    sector.removeOverlayCell(c);
                }
                if(event.movingCell().isConsumable(c.getCellType())) {
                    sector.removeOverlayCell(event.movingCell());
                }
            }


        }


    }
    /**
     * Called on EndGameEvent and updates gameState
     */
    public void onEndGameEvent(EndGameEvent event) {
        gameState = event.result();
    }
    /**
     * Called on TakeHistorySnapshotEvent and updates SectorHistory
     * to include the current sector
     */
    public void onTakeHistorySnapshotEvent(TakeHistorySnapshotEvent event) {
        if(sectorHistory.size()>= MAX_RECORDING_HISTORY_LENGTH)
            sectorHistory.remove(0);

        sectorHistory.add(sector.copy());
    }
    /**
     * Rewinds to the previous game tick/step and triggers RewindEvent and PostRewindEvent
     * @return Ok if rewind was successful,
     * GAME_NOT_STARTED if game is not in progress,
     * NO_HISTORY if no rewind history
     */
    public Result<?> rewind() {
        if(gameState != GameState.IN_PROGRESS)
            return Result.err(Result.ErrType.GAME_NOT_STARTED, "Cannot rewind when game is not in progress!", LOGGER);
        if(sectorHistory.isEmpty())
            return Result.err(Result.ErrType.NO_HISTORY, "Rewind history is empty!", LOGGER);

        sector.unsubscribe();
        sector = sectorHistory.get(sectorHistory.size() - 1);
        ship = sector.getShip().printErrOrGetValue();
        sector.updateEngineRef(this);

        sectorHistory.remove(sectorHistory.size() - 1);
        bus.publish(new RewindEvent());
        bus.publish(new PostRewindEvent());
        return Result.ok(null);
    }

    /**
     * Saves the game state to file
     * @return Ok if save was successful,
     * GAME_NOT_STARTED if game is not in progress,
     * GAME_SAVE_FAILED if save failed otherwise
     */
    public Result<?> saveGameState() {
        if(gameState != GameEngine.GameState.IN_PROGRESS)
            return Result.err(Result.ErrType.GAME_NOT_STARTED, "Cannot save when game is not in progress!", LOGGER);
        Result<?> result = starsalvage.engine.
                PersistentGameState.save(sector, sectorHistory, sectorIdx, difficulty, seed);
        if(!result.isSuccess) {
            result.printErr();
            return Result.err(result.getErrType(), "Failed to save game state!", LOGGER);
        }
        return result;
    }
    /**
     * Load the game state from file
     * @return Ok if load was successful,
     * GAME_IN_PROGRESS if game is in progress,
     * GAME_LOAD_FAILED if load failed otherwise
     */
    public Result<?> loadGameState() {
        if(gameState != GameEngine.GameState.NOT_STARTED) {
            return Result.err(Result.ErrType.GAME_IN_PROGRESS, "Cannot load when game is over or in progress!", LOGGER);
        }
        var result = starsalvage.engine.PersistentGameState.load();
        if(!result.isSuccess) {
            result.printErr();
            LOGGER.warning("Failed to load game state!");
            return Result.err(result.getErrType(), "Failed to load game state!", LOGGER);
        }

        this.gameState = GameEngine.GameState.IN_PROGRESS;
        PersistentGameState pState = result.getValueOrNull();
        sectorHistory = pState.sectorHistory;

        sector = pState.sector;
        sector.updateEngineRef(this);

        ship = sector.getShip().printErrOrGetValue();
        sectorIdx = pState.sectorIdx;
        difficulty = pState.difficulty;
        return Result.ok(null);
    }

    /**
     * Sets the game difficulty constrained between
     * MIN_DIFFICULTY and MAX_DIFFICULTY, logs warning and sets
     * difficulty to 3 if outside range, Applies when next sector begins
     * @param difficulty The difficulty to set to
     */
    public void setDifficulty(int difficulty) {
        if(difficulty<MIN_DIFFICULTY || difficulty>MAX_DIFFICULTY) {
            LOGGER.warning("Difficulty is not within accepted range, setting to default (3)");
            this.difficulty = 3;
        } else
            this.difficulty = difficulty;
    }

    public GameState getGameState() { return gameState; }
    public int getSeed() { return seed; }
    public int getDifficulty() { return difficulty; }
    public List<HighscoreController.HighScore> getHighscores() { return highscoreController.getHighscores(); }
}
