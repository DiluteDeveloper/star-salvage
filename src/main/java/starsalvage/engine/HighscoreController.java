package starsalvage.engine;

import starsalvage.engine.interfaces.ISafeEngineAccess;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Keeps track of top highscores and saves and loads from file
 */
public class HighscoreController {

    /**
     * Name of the save file
     */
    public final static String saveFileName = "highscores.dat";
    /**
     * Used to log to console
     */
    private static final Logger LOGGER = Logger.getLogger(HighscoreController.class.getName());
    /**
     * Interface to the game engine
     */
    private final ISafeEngineAccess engine;

    /**
     * An event called when a new top highscore is set
     */
    public record NewHighscoreEvent(List<HighScore> highScores, int newHighscore, int newHighscoreIdx) {};
    /**
     * An event called when highscores are loaded from file
     */
    public record LoadHighscoresEvent(List<HighScore> highScores) {};

    /**
     * The max number of highscores to save and store
     */
    private static final int MAX_HIGHSCORES = 5;

    /**
     * Sets engine interface and subscribes to EndGameEvent
     * @param engine The game engine interface
     */
    public HighscoreController(ISafeEngineAccess engine) {
        this.engine = engine;
        engine.subscribeToEvent(GameEngine.EndGameEvent.class, this::onEndGameEvent);
    }

    /**
     * Highscore value and the date it was created
     */
    public static class HighScore implements Serializable {
        /**
         * Highscore value
         */
        public int highScore;
        /**
         * Date created
         */
        public LocalDate date;

        /**
         * Sets date to now and sets highScore
         * @param highScore The highscore achieved
         */
        public HighScore(int highScore) {
            this.highScore = highScore;
            this.date = LocalDate.now();
        }
    }

    /**
     * List of current top highscores
     */
    public List<HighScore> highScores = new ArrayList<>();

    /**
     * Called on EndGameEvent and adds ship score to highscores if
     * the score value is greater than any highscore, and publishes
     * NewHighscoreEvent if so. Saves highscores to file.
     */
    public void onEndGameEvent(GameEngine.EndGameEvent e) {
        int shipScore = engine.getShip().getScore();

        if(shipScore!=-1) {
            for(int i = 0; i < highScores.size(); i++) {
                if(highScores.get(i).highScore <= shipScore) {
                    highScores.add(i, new HighScore(shipScore));
                    engine.publishEvent(new NewHighscoreEvent(highScores, shipScore, i));
                    break;
                }

                if(i == highScores.size() - 1) {
                    highScores.add(new HighScore(shipScore));
                    engine.publishEvent(new NewHighscoreEvent(highScores, shipScore, i));
                    break;
                }
            }
            if(highScores.isEmpty()) {
                highScores.add(new HighScore(shipScore));
                engine.publishEvent(new NewHighscoreEvent(highScores, shipScore, 0));
            }
            if(highScores.size() > MAX_HIGHSCORES)
                highScores.remove(highScores.size() - 1);
        }
        Result<?> result = save();
        if(!result.isSuccess)
            result.printErr();
    }

    /**
     * Saves highscores to file
     * @return OK if highscores were saved,
     * GAME_SAVE_FAILED if highscores failed to save
     */
    private Result<?> save() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(saveFileName))) {
            out.writeObject(highScores);
        } catch (Exception e) {
            return Result.err(Result.ErrType.GAME_SAVE_FAILED, e.toString(), LOGGER);
        }
        return Result.ok(null);
    }

    /**
     * Loads highscores from file and publishes LoadHighscoresEvent
     * @return OK if highscores were loaded,
     * GAME_LOAD_FAILED if highscores failed to load
     */
    public Result<?> load() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(saveFileName))) {
            highScores = (List<HighScore>) in.readObject();
            engine.publishEvent(new LoadHighscoresEvent(highScores));
            return Result.ok(null);
        } catch (Exception e) {
            return Result.err(Result.ErrType.GAME_LOAD_FAILED, e.getMessage(), LOGGER);
        }
    }

    public List<HighScore> getHighscores() { return highScores; }
}
