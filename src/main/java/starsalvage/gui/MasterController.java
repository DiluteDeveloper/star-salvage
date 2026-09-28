package starsalvage.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import starsalvage.engine.GameEngine;
import starsalvage.engine.HighscoreController;
import starsalvage.engine.Result;
import starsalvage.engine.cells.reactivecells.activecells.movingcells.Ship;

import java.util.logging.Logger;

/**
 * Receives and stores all FXML types and button events
 * and coordinates between different GUI modules
 */
public class MasterController {

    /**
     * Used to log to console
     */
    private static final Logger LOGGER = Logger.getLogger(MasterController.class.getName());

    @FXML
    private Button helpButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button loadButton;
    @FXML
    private Button undoButton;
    @FXML
    private Button waitButton;
    @FXML
    private Button drillButton;
    @FXML
    private Button leftButton;
    @FXML
    private Button rightButton;
    @FXML
    private Button upButton;
    @FXML
    private Button downButton;
    @FXML
    private Button difficultyDownButton;
    @FXML
    private Button difficultyUpButton;
    @FXML
    private Button startButton;

    private MoveAndDrillController moveAndDrillController;

    @FXML
    private Label gameStatusLabel;
    private StatusController statusController;

    @FXML
    private Label highscoresLabel;
    private HighscoresController highscoresController;

    @FXML
    private GridPane gameGrid;
    private GridController gridController;

    @FXML
    private Label gameEndLabel;
    private GameEndController gameEndController;

    @FXML
    private StackPane startOverlay;
    @FXML
    private Label difficultyLabel;
    private GameStartController gameStartController;

    @FXML
    private VBox helpPanel;
    @FXML
    private VBox highscoresPanel;
    @FXML
    private VBox gameStatusPanel;
    @FXML
    private VBox rightPanel;

    public static final String DEFAULT_BUTTON_STYLE = "-fx-background-radius: 8; -fx-text-fill: white; -fx-background-color: #4a4a4a";
    public static final String DEFAULT_BUTTON_STYLE_NO_COLOUR = "-fx-background-radius: 8; -fx-text-fill: white; ";

    private GameEngine engine;

    /**
    * Called automatically by ApplicationHandler and initialises
     * game engine, GUI modules, subscribes to events and adds press effects to buttons
      */
    @FXML
    public void initialize() {
        engine = new GameEngine((int)System.currentTimeMillis());
        //engine = new GameEngine(100);

        gridController = new GridController(engine, gameGrid);
        moveAndDrillController = new MoveAndDrillController(engine, drillButton,
                leftButton, rightButton, upButton, downButton);
        statusController = new StatusController(engine, gameStatusLabel);
        highscoresController = new HighscoresController(engine, highscoresLabel);
        gameEndController = new GameEndController(engine, gameEndLabel);
        gameStartController = new GameStartController(engine, startOverlay, difficultyLabel);
        rightPanel.getChildren().remove(helpPanel);

        engine.subscribeToEvent(HighscoreController.NewHighscoreEvent.class, this::onNewHighscoreEvent);

        addPressEffect(drillButton);
        addPressEffect(leftButton);
        addPressEffect(upButton);
        addPressEffect(downButton);
        addPressEffect(rightButton);
        addPressEffect(helpButton);
        addPressEffect(saveButton);
        addPressEffect(loadButton);
        addPressEffect(waitButton);
        addPressEffect(undoButton);
        addPressEffect(difficultyUpButton);
        addPressEffect(difficultyDownButton);
        addPressEffect(startButton);

        if(engine.getHighscores().isEmpty())
           rightPanel.getChildren().remove(highscoresPanel);
    }

    /**
     * Called when difficulty up button pressed
     */
    @FXML
    private void onDifficultyUp() {
        gameStartController.onDifficultyUpButtonPressed();
    }
    /**
     * Called when difficulty down button pressed
     */
    @FXML
    private void onDifficultyDown() {
        gameStartController.onDifficultyDownButtonPressed();
    }
    /**
     * Called when start button pressed
     */
    @FXML
    private void onStart() {
        gameStartController.onStartButtonPressed();
        statusController.updateStatus();

    }
    /**
     * Called when up button pressed
     */
    @FXML
    private void onUp() {
        moveAndDrillController.onDirectionButtonPressed(Ship.Direction.UP,statusController);
    }

    /**
     * Called when down button pressed
     */
    @FXML
    private void onDown() {
        moveAndDrillController.onDirectionButtonPressed(Ship.Direction.DOWN,statusController);
    }
    /**
     * Called when left button pressed
     */
    @FXML
    private void onLeft() {
        moveAndDrillController.onDirectionButtonPressed(Ship.Direction.LEFT,statusController);
    }
    /**
     * Called when right button pressed
     */
    @FXML
    private void onRight() {
        moveAndDrillController.onDirectionButtonPressed(Ship.Direction.RIGHT,statusController);
    }

    /**
     * Called when wait button pressed
     */
    @FXML
    private void onWait() {
        var result = engine.getShip().waitForTick();
        if(!result.isSuccess)
            statusController.setTempStatus(result.getMessage());
        else
            statusController.addActionStatus("Waited one turn.", 0);
    }

    /**
     * Called when drill button pressed
     */
    @FXML
    private void onDrill() {
        moveAndDrillController.onDrillButtonPressed(statusController);
    }

    /**
     * Called when undo button pressed
     */
    @FXML
    private void onUndo() {
        var result = engine.rewind();
        if(!result.isSuccess) {
            statusController.setTempStatus(result.getMessage());
            return;
        } else
            statusController.addActionStatus("Undid previous action.", 0);

        gridController.reloadGrid(true);
        statusController.updateStatus();
    }
    /**
     * Called when save button pressed
     */
    @FXML
    private void onSave() {
        Result<?> result = engine.saveGameState();
        if(!result.isSuccess)
            statusController.setTempStatus(result.getMessage());
        else
            statusController.setTempStatus("Game saved!");
    }
    /**
     * Called when load button pressed
     */
    @FXML
    private void onLoad() {
        Result<?> result = engine.loadGameState();
        if(!result.isSuccess) {
            statusController.setTempStatus(result.getMessage());
            return;
        } else {
            statusController.setTempStatus("Game loaded!");
        }

        gameStartController.disableOverlay();
        moveAndDrillController.updateDrillModeButtonStyle();
        gridController.reloadGrid(true);
        statusController.updateStatus();
    }

    /**
     * Whether the help menu is activated
     */
    boolean helpActive = false;
    /**
     * Called when help button pressed and triggers
     * visibility of help menu
     */
    @FXML
    private void onHelp() {
        if(engine.getGameState() != GameEngine.GameState.IN_PROGRESS) {
            statusController.setTempStatus("Cannot open help menu when game is not in progress!");
            return;
        }
        if(helpActive) {
            rightPanel.getChildren().remove(helpPanel);
            if(!engine.getHighscores().isEmpty()) {
                rightPanel.getChildren().add(0, highscoresPanel);
                rightPanel.getChildren().add(1, gameStatusPanel);
            } else {
                rightPanel.getChildren().add(0, gameStatusPanel);
            }
        } else {
            rightPanel.getChildren().remove(gameStatusPanel);
            rightPanel.getChildren().remove(highscoresPanel);
            rightPanel.getChildren().add(0, helpPanel);
        }
        helpActive = !helpActive;
    }

    /**
     * Adds press effect to buttons
     * @param button Button to add press effect to
     */
    public static void addPressEffect(Button button) {
        button.setOnMousePressed(e -> button.setEffect(new DropShadow(2, Color.GRAY)));
        button.setOnMouseReleased(e -> button.setEffect(null));
    }

    /**
     * Called on NewHighscoreEvent and shows the highscore panel if this is the
     * first highscore and the help menu isn't shown
     */
    public void onNewHighscoreEvent(HighscoreController.NewHighscoreEvent event) {
        if(!rightPanel.getChildren().contains(highscoresPanel) && !helpActive)
            rightPanel.getChildren().add(0, highscoresPanel);
    }
}
