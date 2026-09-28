package starsalvage.gui;

import javafx.scene.control.Label;
import starsalvage.engine.GameEngine;
import starsalvage.engine.HighscoreController;

import java.util.List;

/**
 * Controls highscore panel on GUI
 */
public class HighscoresController {

    /**
     * Label containing top highscores
     */
    private Label highscoresLabel;
    private GameEngine engine;

    /**
     * Initialises member variables, updates highscore panel after highscores loaded
     * and subscribes to events
     * @param engine Engine reference
     * @param highscoresLabel Highscore label
     */
    public HighscoresController(GameEngine engine, Label highscoresLabel) {
        this.engine = engine;
        this.highscoresLabel = highscoresLabel;

        engine.subscribeToEvent(HighscoreController.LoadHighscoresEvent.class, this::onLoadHighscoresEvent);
        engine.subscribeToEvent(HighscoreController.NewHighscoreEvent.class, this::onNewHighscoreEvent);

        updateHighscores(engine.getHighscores());
    }

    /**
     * Called on LoadHighscoresEvent and updates highscores
     */
    private void onLoadHighscoresEvent(HighscoreController.LoadHighscoresEvent event) {
        updateHighscores(event.highScores());
    }

    /**
     * Called on NewHighscoreEvent and updates highscores
     */
    private void onNewHighscoreEvent(HighscoreController.NewHighscoreEvent event) {
        updateHighscores(event.highScores());
    }

    /**
     * Updates top highscores with current game engine highscore data
     * @param highScores List of current top highscores
     */
    private void updateHighscores(List<HighscoreController.HighScore> highScores) {
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < highScores.size(); i++) {
                sb.append("#" + (i + 1) + " " + highScores.get(i).highScore + " " + highScores.get(i).date + "\n");
            }

            highscoresLabel.setText(sb.toString());
        }
}
