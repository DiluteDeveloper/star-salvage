package starsalvage.engine;

import starsalvage.engine.cells.*;
import starsalvage.engine.cells.reactivecells.activecells.ActiveCell;
import starsalvage.engine.cells.reactivecells.activecells.movingcells.ScoutDroneCell;
import starsalvage.engine.cells.reactivecells.activecells.SentryTurretCell;
import starsalvage.engine.cells.reactivecells.activecells.movingcells.Ship;
import starsalvage.engine.cells.reactivecells.activecells.movingcells.SpaceMineCell;
import starsalvage.engine.cells.reactivecells.*;
import starsalvage.engine.interfaces.ISafeEngineAccess;

import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;

import static starsalvage.engine.Result.ok;

/**
 * Stores the cells and overlayCells that host
 * and create different game behaviours
 */
public class Sector implements Serializable {

    /**
     * Cells that cannot move onto another cell
     * position that already has a cell
     * public for UNIT TESTING ONLY but cannot limit
     * visibility to tests
     */
    public HashMap<Vec2, Cell> cells = new HashMap<>();
    /**
     * Cells that can move onto another cell
     * position that already has a cell,
     * can have multiple on one position
     * public for UNIT TESTING ONLY but cannot limit
     * visibility to tests
     */
    public List<Cell> overlayCells = new ArrayList<>();

    /**
     * Should be constructed using copy() or generateSector() only
     */
    private Sector() {}

    /**
     * Used to log to console
     */
    private final static Logger LOGGER = Logger.getLogger(starsalvage.engine.Sector.class.getName());

    /**
     * Number of asteroids in sector
     */
    private static final int NUM_ASTEROIDS = 8;
    /**
     * Number of drill charges in sector
     */
    private static final int NUM_DRILL_CHARGES = 2;
    /**
     * Number of hull patches in sector
     */
    private static final int NUM_HULL_PATCHES = 2;
    /**
     * Number of gravity wells in sector
     */
    private static final int NUM_GRAVITY_WELLS = 5;
    /**
     * Number of scrap cargos in sector
     */
    private static final int NUM_SCRAP_CARGO = 5;
    /**
     * Number of scout drones in sector
     */
    private static final int NUM_SCOUT_DRONES = 2;
    /**
     * Number of space mines in sector
     */
    private static final int NUM_SPACE_MINES = 2;

    /**
     * Creates a copy of the sector, any primitives are
     * automatically copied but non-primitives such as Cell, Vec2, Sector
     * share a reference to the same underlying class, hence the need for copy().
     * @return The sector copy
     */
    public Sector copy() {
       Sector sector = new Sector();
       for(Map.Entry<Vec2, Cell> staticCell : cells.entrySet()) {
            sector.cells.put(new Vec2(staticCell.getKey()), staticCell.getValue().copy());
        }
        for(Cell overlayCell : overlayCells) {
            sector.overlayCells.add(overlayCell.copy());
        }
        return sector;
    }

    /**
     * Unsubscribes and removes a cell
     * @param cell The cell to remove
     */
    public void removeCell(Cell cell) {
        if(cell instanceof ActiveCell a)
            a.unsubscribe();
        cells.remove(cell.pos);
    }
    /**
     * Unsubscribes and removes an overlay cell
     * @param cell The overlay cell to remove
     */
    public void removeOverlayCell(Cell cell) {
        if(cell instanceof ActiveCell a)
            a.unsubscribe();
        overlayCells.remove(cell);
    }

    /**
     * Gets the cell at pos
     * @param pos Position to get cell at
     * @return Cell at position; Ok if cell is found at position,
     * OUT_OF_BOUNDS if position is out of game grid bounds,
     * NO_CELL_AT_POSITION if no cell at position
     */
    public Result<Cell> getCell(Vec2 pos) {
        if(pos.x < 0 || pos.y < 0 || pos.x >= GameEngine.BOARD_SIZE.x || pos.y >= GameEngine.BOARD_SIZE.y)
            return Result.err(Result.ErrType.OUT_OF_BOUNDS, "Tried to access out of bounds!", LOGGER);

        Cell cell = cells.getOrDefault(pos, null);
        if(cell == null)
            return Result.err(Result.ErrType.NO_CELL_AT_POSITION, "There is no cell at that position!", LOGGER);

        return Result.ok(cell);
    }
    /**
     * Gets the first overlay cell at pos
     * @param pos Position to get overlay cell at
     * @return Overlay cell at position; Ok if overlay cell is found at position,
     * OUT_OF_BOUNDS if position is out of game grid bounds,
     * NO_CELL_AT_POSITION if no overlay cell at position
     */
    public Result<Cell> getOverlayCell(Vec2 pos) {
        if(pos.x < 0 || pos.y < 0 || pos.x >= GameEngine.BOARD_SIZE.x || pos.y >= GameEngine.BOARD_SIZE.y)
            return Result.err(Result.ErrType.OUT_OF_BOUNDS, "Tried to access out of bounds!", LOGGER);

        for(Cell overlayCell : overlayCells)
            if(overlayCell.pos.equals(pos))
                return ok(overlayCell);

        return Result.err(Result.ErrType.NO_CELL_AT_POSITION, "There is no cell at that position!", LOGGER);
    }
    /**
     * Gets all overlay cells at pos, for rare cases where multiple
     * overlay cells are at one position
     * @param pos Position to get overlay cells at
     * @return Ok & Overlay cells at position if any,
     * OUT_OF_BOUNDS if position is out of game grid bounds
     */
    public Result<List<Cell>> getOverlayCells(Vec2 pos) {
        if(pos.x < 0 || pos.y < 0 || pos.x >= GameEngine.BOARD_SIZE.x || pos.y >= GameEngine.BOARD_SIZE.y)
            return Result.err(Result.ErrType.OUT_OF_BOUNDS, "Tried to access out of bounds!", LOGGER);

        List<Cell> overlayCells = new ArrayList<Cell>();
        for(Cell overlayCell : this.overlayCells)
            if(overlayCell.pos.equals(pos))
                overlayCells.add(overlayCell);
        return Result.ok(overlayCells);
    }

    /**
     * Unsubscribes all cells and overlay cells
     * from event system
     */
    public void unsubscribe() {
        for(Cell c : cells.values()) {
            if(c instanceof ActiveCell a)
                a.unsubscribe();
        }
        for(Cell c : overlayCells) {
            if(c instanceof ActiveCell a)
                a.unsubscribe();
        }
    }

    /**
     * Updates the engine reference in all cells and overlay cells
     */
    public void updateEngineRef(ISafeEngineAccess engine) {
        for(Cell c : cells.values()) {
            if(c instanceof ReactiveCell r) {
                r.updateEngineRef(engine);
            }
        }
        for(Cell c : overlayCells) {
            if(c instanceof ReactiveCell r) {
                r.updateEngineRef(engine);
            }
        }
    }

    /**
     * Gets the ship, but the result of this should be cached
     * @return Ok & Ship if ship found,
     * NO_SHIP_IN_SECTOR if no ship found
     */
    public Result<Ship> getShip() {
        for(Cell c : overlayCells) {
            if(c instanceof Ship s)
                return Result.ok(s);
        }
        return Result.err(Result.ErrType.NO_SHIP_IN_SECTOR, "No ship in sector!", LOGGER);
    }

    /**
     * Generates new sector and all cells in
     * sector prescribed by internal spawning logic and
     * adds engine ship to sector
     * @param engine Interface to game engine
     * @return Ok & Sector,
     * INVALID_BOARD_SIZE if number of cells/entities to be spawned count exceeds game grid volume
     */
    public static Result<Sector> generateSector(ISafeEngineAccess engine) {

        Sector sector = new Sector();
        sector.overlayCells.add(engine.getShip());
        sector.cells.put(engine.getShip().pos, new EntryCell(engine.getShip().pos));

        if(GameEngine.BOARD_SIZE.x * GameEngine.BOARD_SIZE.y <
                1 + NUM_ASTEROIDS + NUM_DRILL_CHARGES + NUM_HULL_PATCHES + NUM_GRAVITY_WELLS +
                        NUM_SCRAP_CARGO + NUM_SCRAP_CARGO + NUM_SPACE_MINES + engine.getDifficulty()) {
            return Result.err(Result.ErrType.INVALID_BOARD_SIZE, "There is not enough available cells in the " +
                    "board to generate all entities!", LOGGER);
        }

        List<Vec2> availableCells = new ArrayList<>(GameEngine.BOARD_SIZE.x * GameEngine.BOARD_SIZE.y);

        for(int x = 0; x < GameEngine.BOARD_SIZE.x; x++) {
            for(int y = 0; y < GameEngine.BOARD_SIZE.y; y++) {
                availableCells.add(new Vec2(x, y));
            }
        }
        availableCells.remove(engine.getShip().pos);

        var rand = new Random(engine.getSeed());
        Collections.shuffle(availableCells, rand);

        Vec2 wormholePos = availableCells.get(0);
        sector.cells.put(wormholePos, new WormholeCell(engine, wormholePos));
        availableCells.remove(0);

        for(int i = 0; i < NUM_ASTEROIDS; i++) {
            Vec2 pos = availableCells.get(0);
            sector.cells.put(pos, new AsteroidCell(pos));
            availableCells.remove(0);
        }
        for(int i = 0; i < NUM_DRILL_CHARGES; i++) {
            Vec2 pos = availableCells.get(0);
            sector.cells.put(pos, new DrillChargeCell(engine, pos));
            availableCells.remove(0);
        }
        for(int i = 0; i < NUM_HULL_PATCHES; i++) {
            Vec2 pos = availableCells.get(0);
            sector.cells.put(pos, new HullPatchCell(engine, pos));
            availableCells.remove(0);
        }
        for(int i = 0; i < NUM_GRAVITY_WELLS; i++) {
            Vec2 pos = availableCells.get(0);
            sector.cells.put(pos, new GravityWellCell(engine, pos));
            availableCells.remove(0);
        }
        for(int i = 0; i < NUM_SCRAP_CARGO; i++) {
            Vec2 pos = availableCells.get(0);
            sector.cells.put(pos, new ScrapCargoCell(engine, pos));
            availableCells.remove(0);
        }
        // Scout drone needs to be first, then sentry turrets, then space mines;
        // Event system published event call order is deterministic based on
        // subscription order; scout drone subscribes to events first,
        // then sentry turrets, then space mines
        for(int i = 0; i < NUM_SCOUT_DRONES; i++) {
            Vec2 pos = availableCells.get(0);
            ScoutDroneCell cell = new ScoutDroneCell(engine, pos);
            cell.subscribe();
            sector.overlayCells.add(cell);
            availableCells.remove(0);
        }
        for(int i = 0; i < engine.getDifficulty(); i++) {
            Vec2 pos = availableCells.get(0);
            SentryTurretCell cell = new SentryTurretCell(engine, pos);
            cell.subscribe();
            sector.cells.put(pos, cell);
            availableCells.remove(0);
        }

        for(int i = 0; i < NUM_SPACE_MINES; i++) {
            Vec2 pos = availableCells.get(0);
            SpaceMineCell cell = new SpaceMineCell(engine, pos);
            cell.subscribe();
            sector.overlayCells.add(cell);
            availableCells.remove(0);
        }

        return Result.ok(sector);
    }
}
