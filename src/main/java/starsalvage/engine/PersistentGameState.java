package starsalvage.engine;

import starsalvage.engine.cells.reactivecells.activecells.movingcells.Ship;

import java.io.*;
import java.util.List;
import java.util.logging.Logger;

/**
 * Contains the game data to be loaded and saved to file
 * and the functionality to save and load game state
 */
public class PersistentGameState implements Serializable {

    /**
     * Name of the save file
     */
    public transient static String saveFileName = "save.dat";
    /**
     * Used to log to console
     */
    private transient static final Logger LOGGER = Logger.getLogger(PersistentGameState.class.getName());

    // Data mirrored from GameEngine to be saved and loaded ----

    public Sector sector;
    public List<Sector> sectorHistory;
    public int sectorIdx;
    public int difficulty;
    public int seed;

    // Data mirrored from GameEngine to be saved and loaded ----

    /**
     * Mirrors parameters to PersistentGameState data
     * @param sector Sector
     * @param sectorHistory Sector history
     * @param sectorIdx Sector index
     * @param difficulty Game difficulty
     * @param seed Game seed
     */
    private PersistentGameState(Sector sector, List<Sector> sectorHistory,
                                int sectorIdx, int difficulty, int seed) {
        this.sector = sector;
        this.sectorHistory = sectorHistory;
        this.sectorIdx = sectorIdx;
        this.difficulty = difficulty;
        this.seed = seed;
    }

    /**
     * Saves game state to file
     * @return OK if game state was saved,
     * GAME_SAVE_FAILED if game state failed to save
     */
    public static Result<?> save(Sector sector, List<Sector> sectorHistory, int sectorIdx, int difficulty, int seed) {
        PersistentGameState state = new PersistentGameState(sector, sectorHistory, sectorIdx, difficulty, seed);

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(saveFileName))) {
            out.writeObject(state);
        } catch (Exception e) {
            return Result.err(Result.ErrType.GAME_SAVE_FAILED, e.toString(), LOGGER);
        }
        return Result.ok(null);
    }

    /**
     * Loads game state from file
     * @return OK if game state was loaded,
     * GAME_LOAD_FAILED if game state failed to load
     */
    public static Result<PersistentGameState> load() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(saveFileName))) {
            PersistentGameState gameState = (PersistentGameState) in.readObject();
            return Result.ok(gameState);
        } catch (Exception e) {
        return Result.err(Result.ErrType.GAME_LOAD_FAILED, e.getMessage(), LOGGER);
        }
    }
}
