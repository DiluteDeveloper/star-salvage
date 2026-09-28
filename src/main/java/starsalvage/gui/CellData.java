package starsalvage.gui;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import starsalvage.engine.cells.Cell;
import starsalvage.engine.Result;
import starsalvage.engine.cells.reactivecells.activecells.SentryTurretCell;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Hosts Cell image data and functionality related to Cell image data
 */
public class CellData {

    /**
     * A mapping of cell types to their image paths
     */
    protected static final Map<Cell.CellType, String> imagePaths = new HashMap<>(Map.ofEntries(
            Map.entry(Cell.CellType.SHIP, "images/ship.png"),
            Map.entry(Cell.CellType.GRAVITY_WELL_CELL, "images/gravitywell.png"),
            Map.entry(Cell.CellType.ASTEROID_CELL, "images/asteroid.png"),
            Map.entry(Cell.CellType.ENTRY_CELL, "images/entrycell.png"),
            Map.entry(Cell.CellType.SPACE_MINE_CELL, "images/spacemine.png"),
            Map.entry(Cell.CellType.WORMHOLE_CELL, "images/wormhole.png"),
            Map.entry(Cell.CellType.SCRAP_CARGO_CELL, "images/scrapcargo.png"),
            Map.entry(Cell.CellType.DRILL_CHARGE_CELL, "images/drillcharge.png"),
            Map.entry(Cell.CellType.HULL_PATCH_CELL, "images/hullpatch.png"),
            Map.entry(Cell.CellType.SENTRY_TURRET_CELL, "images/turretidle.png"),
            Map.entry(Cell.CellType.SCOUT_DRONE_CELL, "images/scoutdrone.png")
    ));
    /**
     * A mapping of sentry turret states to their image paths
     */
    protected static final Map<SentryTurretCell.State, String> turretImagePaths = new HashMap<>(Map.of(
            SentryTurretCell.State.IDLE, "images/turretidle.png",
            SentryTurretCell.State.CHARGING, "images/turretcharging.png",
            SentryTurretCell.State.FIRING, "images/turretfiring.png"
    ));

    /**
     * Used to log to console
     */
    private static final Logger LOGGER = Logger.getLogger(CellData.class.getName());

    /**
     * Mapping of cell types to loaded images
     */
    private Map<Cell.CellType, Image> images = new HashMap<>();
    /**
     * Mapping of sentry turret states to loaded images
     */
    private Map<SentryTurretCell.State, Image> turretImages = new HashMap<>();

    /**
     * Loads images from file into memory
     */
    CellData() {
        for (var entry : imagePaths.entrySet()) {
            var stream = getClass().getResourceAsStream(entry.getValue());
            if (stream == null) {
                LOGGER.warning("Image not found: " + entry.getValue());
                continue;
            }
            images.put(entry.getKey(), new Image(stream));
        }
        for (var entry : turretImagePaths.entrySet()) {
            var stream = getClass().getResourceAsStream(entry.getValue());
            if (stream == null) {
                LOGGER.warning("Image not found: " + entry.getValue());
                continue;
            }
            turretImages.put(entry.getKey(), new Image(stream));
        }
    }

    /**
     * Get an image view mapped to the cell and/or sentry turret
     * @param cell The cell mapped to an image
     * @param decrementState Whether to decrement the sentry turret cell state
     *                       when getting the image associated with the sentry turret state;
     *                       Used when loading game state or rewinding as the tick when FIRING occurs
     *                       and the image is set to FIRING is also the same tick when the
     *                       turret states are set to IDLE and creates a mismatch in some scenarios
     * @return Ok & ImageView of image mapped to cell or turret state,
     * NO_IMAGE if image was not able to be retrieved
     */
    public Result<ImageView> getCellImageView(Cell cell, boolean decrementState) {
        if(!images.containsKey(cell.getCellType()))
            return Result.err(Result.ErrType.NO_IMAGE, "There is no image associated with this cell type!", LOGGER);

        if(cell instanceof SentryTurretCell t)
            return getTurretImageView(t.state, decrementState);
        ImageView view = new ImageView(images.get(cell.getCellType()));
        view.setManaged(false);
        view.setSmooth(false);
        return Result.ok(view);
    }

    /**
     * Get the image view for a turret state
     * @param state The turret state
     * @param decrementState Whether to decrement the sentry turret cell state
     *                       when getting the image associated with the sentry turret state;
     *                       Used when loading game state or rewinding as the tick when FIRING occurs
     *                       and the image is set to FIRING is also the same tick when the
     *                       turret states are set to IDLE and creates a mismatch in some scenarios
     * @return Ok & ImageView of image mapped to turret state,
     * NO_IMAGE if image was not able to be retrieved
     */
    private Result<ImageView> getTurretImageView(SentryTurretCell.State state, boolean decrementState) {
        if(!turretImages.containsKey(state))
            return Result.err(Result.ErrType.NO_IMAGE, "There is no image associated with this turret state!", LOGGER);

        SentryTurretCell.State fixedState = decrementState ? state.getPreviousState() : state;
        ImageView view = new ImageView(turretImages.get(fixedState));
        view.setManaged(false);
        view.setSmooth(false);
        return Result.ok(view);
    }
}
