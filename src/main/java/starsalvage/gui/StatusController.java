package starsalvage.gui;

import javafx.scene.control.Label;
import starsalvage.engine.CLIGameEngine;
import starsalvage.engine.GameEngine;
import starsalvage.engine.cells.reactivecells.activecells.SentryTurretCell;
import starsalvage.engine.cells.reactivecells.activecells.movingcells.Ship;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Controls the status panel in the GUI hosting
 * player stats and relevant game grid information
 */
public class StatusController {

    Label gameStatusLabel;
    GameEngine engine;

    /**
     * Temporary status displayed to status panel such as an invalid action
     * or failed move
     */
    private String tempStatus = "";
    /**
     * List of actions that occurred on a tick as status strings
     */
    private List<CLIGameEngine.PriorityMessage> actionsStatus = new ArrayList<>();

    /**
     * Initialise member variables and subscribe to events
     */
    public StatusController(GameEngine engine, Label gameStatusLabel) {
        this.engine = engine;
        this.gameStatusLabel = gameStatusLabel;

        engine.subscribeToEvent(GameEngine.GamePreTickEvent.class, this::onGamePreTickEvent);
        engine.subscribeToEvent(GameEngine.PostRewindEvent.class, this::onPostRewindEvent);
        engine.subscribeToEvent(Ship.IncreaseScoreEvent.class, this::onIncreaseScoreEvent);
        engine.subscribeToEvent(Ship.LoseHPEvent.class, this::onLoseHPEvent);
        engine.subscribeToEvent(SentryTurretCell.TurretFireEvent.class, this::onTurretFireEvent);
    }

    /**
     * Set the temporary status and update status
     * @param status Temporary status
     */
    public void setTempStatus(String status) {
        tempStatus = status;
        updateStatus();
    }
    /**
     * Add an action status to be displayed during current tick
     * and updates status to reflect
     * @param status Action status
     * @param priority determines the placement of the action status;
     *                 lower priority = higher placement
     */
    public void addActionStatus(String status, int priority) {
        actionsStatus.add(new CLIGameEngine.PriorityMessage(status, priority));
        updateStatus();
    }

    /**
     * Add player stats, temporary status and action status to status panel
     */
    public void updateStatus() {

        StringBuilder sb = new StringBuilder();
        if(engine.getGameState() != GameEngine.GameState.NOT_STARTED) {
            sb.append("Remaining Steps: " + (Ship.MAX_STEPS - engine.getShip().getStepCount())+ "\n");
            if(engine.getShip().getNumDrillCharges()!=0)
                sb.append("Drill Charges: " + engine.getShip().getNumDrillCharges()+ "\n");
            sb.append("Sector: " + engine.getSectorIdx()+ "\n");
            if(engine.getShip().getScore()!=0)
                sb.append("Score: " + engine.getShip().getScore()+ "\n");
            sb.append("HP: " + engine.getShip().getHP() + "\n");
        }

        // Order action status' by priority
        actionsStatus.sort(Comparator.comparing(CLIGameEngine.PriorityMessage::priority));
        for(CLIGameEngine.PriorityMessage actionStatus : actionsStatus) {
            sb.append("| " + actionStatus.message() + "\n");
        }
        if(!tempStatus.equals(""))
            sb.append("> " + tempStatus + "\n");
        gameStatusLabel.setText(sb.toString());
    }

    /**
     * Called on GamePreTickEvent and clears all status and updates status
     */
    public void onGamePreTickEvent(GameEngine.GamePreTickEvent event) {
        tempStatus = "";
        actionsStatus.clear();
        updateStatus();
    }
    /**
     * Called on PostRewindEvent and clears all status and updates status
     */
    public void onPostRewindEvent(GameEngine.PostRewindEvent event) {
        tempStatus = "";
        actionsStatus.clear();
        updateStatus();
    }
    /**
     * Called on IncreaseScoreEvent and adds an action status to represent the action
     */
    public void onIncreaseScoreEvent(Ship.IncreaseScoreEvent event) {
        switch(event.publisher().getCellType()) {
            case SCRAP_CARGO_CELL -> addActionStatus("You collected a scrap cargo.", 5);
            case SCOUT_DRONE_CELL-> addActionStatus("You destroyed a scout drone.", 5);
        }
    }
    /**
     * Called on LoseHPEvent and adds an action status to represent gravity well drift
     */
    public void onLoseHPEvent(Ship.LoseHPEvent event) {
        switch(event.publisher().getCellType()) {
            case GRAVITY_WELL_CELL -> addActionStatus("You drifted into a gravity well.", 5);
        }
    }
    /**
     * Called on TurretFireEvent and adds an action status to represent all turrets states
     * and whether they hit the ship
     */
    public void onTurretFireEvent(SentryTurretCell.TurretFireEvent event) {
        if(event.hitShip())
            addActionStatus("A sentry turret fires and your ship loses 2 Hull.", 5);
        else
            addActionStatus("A sentry turret fires, but your ship is out of the line of fire.", 5);
    }
}
