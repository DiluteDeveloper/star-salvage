package starsalvage.gui;

import javafx.scene.control.Label;
import starsalvage.engine.GameEngine;
import starsalvage.engine.HighscoreController;

/**
 * Controls GUI logic for when the game ends
 */
public class GameEndController {

    private Label gameEndLabel;

    private GameEngine engine;

    /**
     * Setup event subscriptions and member variables
     * @param engine GameEngine to reference
     * @param gameEndLabel The label overtop of the game grid to be visible on game end
     */
    public GameEndController(GameEngine engine, Label gameEndLabel){
        this.gameEndLabel = gameEndLabel;
        this.engine = engine;

        engine.subscribeToEvent(GameEngine.EndGameEvent.class, this::onEndGameEvent);
        engine.subscribeToEvent(HighscoreController.NewHighscoreEvent.class, this::onNewHighscoreEvent);
    }

    /**
     * Called on EndGameEvent and sets the gameEndLabel text based on the game end result
     */
    public void onEndGameEvent(GameEngine.EndGameEvent event) {
        switch(event.result()) {
            // WIN is serviced by onNewHighscoreEvent
            case OUT_OF_STEPS:
                gameEndLabel.setText("You ran out of steps!");
                break;
            case SHIP_DESTROYED:
                gameEndLabel.setText("Your ship was destroyed!");
                break;
        }
    }

    /**
     * Called on NewHighscoreEvent and sets the gameEndLabel text based on the highscore achieved
     */
    public void onNewHighscoreEvent(HighscoreController.NewHighscoreEvent event) {
        gameEndLabel.setText("You won!\nNew #" + (event.newHighscoreIdx() + 1) + " high score!");
    }
}
