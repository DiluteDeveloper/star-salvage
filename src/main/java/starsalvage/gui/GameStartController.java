package starsalvage.gui;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import starsalvage.engine.GameEngine;

/**
 * Controls menu overtop of the game grid on game start
 */
public class GameStartController {

    /**
     * The current difficulty set on the game start overlay
     */
    private int difficulty = 3;
    private final GameEngine engine;

    /**
     * The game start overlay
     */
    private final StackPane startOverlay;
    /**
     * The difficulty value shown on the game start overlay
     */
    private final Label difficultyLabel;

    /**
     * Initialises member variables
     * @param engine Engine reference
     * @param startOverlay The game start overlay
     * @param difficultyLabel The difficulty label on the game start overlay
     */
    public GameStartController(GameEngine engine, StackPane startOverlay, Label difficultyLabel) {
        this.engine = engine;
        this.startOverlay = startOverlay;
        this.difficultyLabel = difficultyLabel;
    }

    /**
     * Disable the game start overlay
     */
    public void disableOverlay() {
        startOverlay.setVisible(false);
    }

    /**
     * Trigger the start of the next sector and disable the game start overlay
     */
    public void onStartButtonPressed() {
       engine.tryBeginNextSector();
       disableOverlay();
    }

    /**
     * Increment the difficulty by 1 and update the engine difficulty
     */
    public void onDifficultyUpButtonPressed() {
        difficulty = Math.min(difficulty + 1, GameEngine.MAX_DIFFICULTY);
        difficultyLabel.setText(Integer.toString(difficulty));
        engine.setDifficulty(difficulty);
    }
    /**
     * Decrement the difficulty by 1 and update the engine difficulty
     */
    public void onDifficultyDownButtonPressed() {
        difficulty = Math.max(difficulty - 1, GameEngine.MIN_DIFFICULTY);
        difficultyLabel.setText(Integer.toString(difficulty));
        engine.setDifficulty(difficulty);
    }
}
