package starsalvage.gui;

import javafx.scene.control.Button;
import starsalvage.engine.GameEngine;
import starsalvage.engine.Result;
import starsalvage.engine.Vec2;
import starsalvage.engine.cells.reactivecells.activecells.movingcells.MovingCell;
import starsalvage.engine.cells.reactivecells.activecells.movingcells.Ship;

import java.util.logging.Logger;

/**
 * Controls movement and drilling logic in GUI,
 * switches movement buttons to drill direction buttons when in drill mode
 */
public class MoveAndDrillController {

    /**
     * Drill button style when no drill charges are available
     */
    private final static String DRILL_BUTTON_NONE_LEFT_STYLE =
            MasterController.DEFAULT_BUTTON_STYLE_NO_COLOUR + "-fx-background-color: #b11414";
    /**
     * Drill button style when drill charges are available
     */
    private final static String DRILL_BUTTON_ACCESSIBLE_STYLE =
            "-fx-background-radius: 8; -fx-text-fill: white; -fx-background-color: #05b505";
    /**
     * Drill button style when in drill mode
     */
    private final static String DRILL_BUTTON_DRILL_MODE_STYLE =
            MasterController.DEFAULT_BUTTON_STYLE_NO_COLOUR + "-fx-background-color: #0083aa";

    /**
     * Used to log to console
     */
    private static final Logger LOGGER = Logger.getLogger(MoveAndDrillController.class.getName());
    private final GameEngine engine;

    private final Button drillButton;
    private final Button leftButton;
    private final Button rightButton;
    private final Button upButton;
    private final Button downButton;

    /**
     * DrillMode represents whether the movement buttons will determine
     * the drill charge direction or move the player
     */
    private boolean inDrillMode = false;

    /**
     * Initialises member variables and subscribes to events
     */
    public MoveAndDrillController(GameEngine engine, Button drillButton,
                           Button leftButton, Button rightButton,
                           Button upButton, Button downButton) {
        this.engine = engine;
        this.drillButton = drillButton;
        drillButton.setStyle(DRILL_BUTTON_NONE_LEFT_STYLE);
        this.leftButton = leftButton;
        this.rightButton = rightButton;
        this.upButton = upButton;
        this.downButton = downButton;

        engine.subscribeToEvent(Ship.ConsumeDrillChargeEvent.class, this::onConsumeDrillChargeEvent);
        engine.subscribeToEvent(Ship.PickupDrillChargeEvent.class, this::onPickupDrillChargeEvent);
        engine.subscribeToEvent(GameEngine.PostRewindEvent.class, this::onPostRewindEvent);
    }

    /**
     * Called when any directional button is pressed
     * @param direction The associated direction of the button pressed
     * @param statusController StatusController object that facilitates status information display
     */
    public void onDirectionButtonPressed(MovingCell.Direction direction, StatusController statusController) {
        if(inDrillMode) {
            Result<?> result = engine.getShip().consumeDrillCharge(direction);
            if(result.isSuccess) {
                Vec2 targetPos = engine.getShip().pos;
                statusController.addActionStatus("You deployed a drill charge and " +
                        "destroyed an asteroid at (" + targetPos.x + ", " + targetPos.y + ").", 10);
            } else
                statusController.addActionStatus("You deployed a drill charge but there was no asteroid to destroy.", 10);
        }
        else {
            Result<Vec2> result = engine.getShip().move(direction);
            if(result.isSuccess)
                statusController.addActionStatus("Ship moved " + direction.name() + ".", 0);
            else
                statusController.setTempStatus(result.getMessage());
        }
    };

    /**
     * Refreshes directional buttons and drill buttons to reflect
     * current drill mode and drill charge state
     */
    public void updateDrillModeButtonStyle() {
        if(!inDrillMode) {
            if(engine.getShip().getNumDrillCharges()==0)
                drillButton.setStyle(DRILL_BUTTON_NONE_LEFT_STYLE);
            else
                drillButton.setStyle(DRILL_BUTTON_ACCESSIBLE_STYLE);

            leftButton.setStyle(MasterController.DEFAULT_BUTTON_STYLE);
            rightButton.setStyle(MasterController.DEFAULT_BUTTON_STYLE);
            upButton.setStyle(MasterController.DEFAULT_BUTTON_STYLE);
            downButton.setStyle(MasterController.DEFAULT_BUTTON_STYLE);
            return;
        }
        drillButton.setStyle(DRILL_BUTTON_DRILL_MODE_STYLE);
        leftButton.setStyle(DRILL_BUTTON_DRILL_MODE_STYLE);
        rightButton.setStyle(DRILL_BUTTON_DRILL_MODE_STYLE);
        upButton.setStyle(DRILL_BUTTON_DRILL_MODE_STYLE);
        downButton.setStyle(DRILL_BUTTON_DRILL_MODE_STYLE);
    }

    /**
     * Called when drill button is pressed and cycles between drill mode on
     * and drill mode off
     * @param statusController StatusController that facilitates status information display
     */
    public void onDrillButtonPressed(StatusController statusController) {
        if(engine.getGameState() != GameEngine.GameState.IN_PROGRESS) {
            statusController.setTempStatus("Cannot drill when game is not in progress!");
            return;
        } else if (engine.getShip().getNumDrillCharges()==0) {
            statusController.setTempStatus("No drill charges available!");
            return;
        }

        if(inDrillMode)
            statusController.setTempStatus("Exited drill mode.");
        else
            statusController.setTempStatus("Entered drill mode.");

        inDrillMode = !inDrillMode;
        updateDrillModeButtonStyle();
    }

    /**
     * Called on PickupDrillChargeEvent and updates drill button style
     */
    public void onPickupDrillChargeEvent(Ship.PickupDrillChargeEvent event) {
        if(event.numDrillCharges()<=1)
            drillButton.setStyle(DRILL_BUTTON_ACCESSIBLE_STYLE);
    }
    /**
     * Called on PostRewindEvent and updates drill button and directional button
     * style to reflect rewound game state
     */
    public void onPostRewindEvent(GameEngine.PostRewindEvent event) {
        updateDrillModeButtonStyle();
    }

    /**
     * Called on ConsumeDrillChargeEvent and switches drill mode off
     * and updates drill mode and directional button style
     */
    public void onConsumeDrillChargeEvent(Ship.ConsumeDrillChargeEvent event) {
        inDrillMode = !inDrillMode;
        updateDrillModeButtonStyle();
    }

}
