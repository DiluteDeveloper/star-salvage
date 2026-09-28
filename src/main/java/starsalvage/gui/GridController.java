package starsalvage.gui;

import javafx.beans.value.ChangeListener;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import starsalvage.engine.*;
import javafx.scene.layout.GridPane;
import starsalvage.engine.cells.Cell;
import starsalvage.engine.cells.reactivecells.activecells.movingcells.MovingCell;
import starsalvage.engine.cells.reactivecells.activecells.SentryTurretCell;
import starsalvage.engine.cells.reactivecells.activecells.movingcells.Ship;

import java.util.logging.Logger;

/**
 * Controls the game grid on the GUI
 */
public class GridController {

    /**
     * The grid pane containing the game grid
     */
    private final GridPane gameGrid;
    private final GameEngine engine;

    /**
     * The pixel width of a cell
     */
    private double cellWidth = 0;
    /**
     * The pixel height of a cell
     */
    private double cellHeight = 0;

    /**
     * Used to log to console
     */
    private static final Logger LOGGER = Logger.getLogger(GridController.class.getName());

    /**
     * GUI representation of the game grid cells
     */
    private StackPane[][] cells = new StackPane[GameEngine.BOARD_SIZE.x][GameEngine.BOARD_SIZE.y];

    /**
     * Hosts image data for different cell types and sentry turret states
     */
    private CellData cellData = new CellData();

    /**
     * Initialises cell grids and style and subscribes to events
     * @param engine Engine reference
     * @param gridPane The game grid
     */
    public GridController(GameEngine engine, GridPane gridPane) {
        this.gameGrid = gridPane;
        this.engine = engine;

        engine.subscribeToEvent(GameEngine.BeginSectorEvent.class, this::onBeginSectorEvent);
        engine.subscribeToEvent(MovingCell.MoveEvent.class, this::onMoveEvent);
        engine.subscribeToEvent(SentryTurretCell.TurretIdleEvent.class, this::onTurretIdleEvent);
        engine.subscribeToEvent(SentryTurretCell.TurretFireEvent.class, this::onTurretFireEvent);
        engine.subscribeToEvent(SentryTurretCell.TurretChargeEvent.class, this::onTurretChargeEvent);
        engine.subscribeToEvent(Ship.ConsumeDrillChargeEvent.class, this::onConsumeDrillChargeEvent);

        for(int x = 0; x < GameEngine.BOARD_SIZE.x; x++) {
            for (int y = 0; y < GameEngine.BOARD_SIZE.y; y++) {
                cells[x][y] = new StackPane();
                cells[x][y].setStyle("-fx-border-color: #444444; -fx-border-width: 0 1 1 0;");
                gameGrid.add(cells[x][y], x, y);
            }
        }

        ChangeListener<Number> sizeListener = (obs, oldVal, newVal) -> {
            cellWidth = gameGrid.getWidth() / GameEngine.BOARD_SIZE.x;
            cellHeight = gameGrid.getHeight() / GameEngine.BOARD_SIZE.y;
            for (int x = 0; x < GameEngine.BOARD_SIZE.x; x++) {
                for (int y = 0; y < GameEngine.BOARD_SIZE.y; y++) {
                    cells[x][y].setPrefSize(cellWidth, cellHeight);
                }
            }
        };

        gameGrid.widthProperty().addListener(sizeListener);
        gameGrid.heightProperty().addListener(sizeListener);
    }

    /**
     * Called on BeginSectorEvent and reloads the game grid
     */
    public void onBeginSectorEvent(GameEngine.BeginSectorEvent event) {
        reloadGrid(true);
    }

    /**
     * Reset the GUI grid contents with current game engine cell data
     * @param decrementState Whether to decrement the sentry turret cell state
     *                       when getting the image associated with the sentry turret state;
     *                       Used when loading game state or rewinding as the tick when FIRING occurs
     *                       and the image is set to FIRING is also the same tick when the
     *                       turret states are set to IDLE and creates a mismatch in some scenarios
     */
    public void reloadGrid(boolean decrementState) {
        for(int x = 0; x < GameEngine.BOARD_SIZE.x; x++) {
            for (int y = 0; y < GameEngine.BOARD_SIZE.y; y++) {
                cells[x][y].getChildren().clear();

                Vec2 pos = new Vec2(x, y);
                recalculateCellContent(pos, decrementState);
            }
        }
    }
    /**
     * Recalculate the images and opacity of images in a cell with current cell data
     * @param pos The cell position to recalculate
     * @param decrementState Whether to decrement the sentry turret cell state
     *                       when getting the image associated with the sentry turret state;
     *                       Used when loading game state or rewinding as the tick when FIRING occurs
     *                       and the image is set to FIRING is also the same tick when the
     *                       turret states are set to IDLE and creates a mismatch in some scenarios
     */
    private void recalculateCellContent(Vec2 pos, boolean decrementState) {
        Result<Cell> cell= engine.getCell(pos);
        Result<Cell> overlayCell= engine.getOverlayCell(pos);
        this.cells[pos.x][pos.y].getChildren().clear();
        if(cell.isSuccess)
            addCellImage(cell.getValueOrNull(), overlayCell.isSuccess, pos, decrementState);
        if(overlayCell.isSuccess)
            addCellImage(overlayCell.getValueOrNull(), cell.isSuccess, pos, decrementState);
    }

    /**
     * Add the cell image to the GUI cell
     * @param cell The cell to identify with an image
     * @param pos The cell position the cell is located in
     * @param hasOverlay Whether there is an overlayCell on the same position,
     *                   used to lower opacity for better visibility with overlapping
     *                   images
     * @param decrementState Whether to decrement the sentry turret cell state
     *                       when getting the image associated with the sentry turret state;
     *                       Used when loading game state or rewinding as the tick when FIRING occurs
     *                       and the image is set to FIRING is also the same tick when the
     *                       turret states are set to IDLE and creates a mismatch in some scenarios
     */
    private void addCellImage(Cell cell, boolean hasOverlay, Vec2 pos, boolean decrementState) {
        if(cell.getCellType() == Cell.CellType.EMPTY_CELL) return;

        Result<ImageView> viewResult = cellData.getCellImageView(cell, decrementState);
        if(!viewResult.isSuccess) return;
        ImageView view = viewResult.getValueOrNull();
        view.fitWidthProperty().bind(cells[pos.x][pos.y].widthProperty());
        view.fitHeightProperty().bind(cells[pos.x][pos.y].heightProperty());
        if(hasOverlay) view.setOpacity(0.7);
        cells[pos.x][pos.y].getChildren().add(view);
    }

    /**
     * Called on MoveEvent and recalculates cell content for old cell pos
     * and new cell pos
     */
    public void onMoveEvent(MovingCell.MoveEvent event) {
        recalculateCellContent(event.oldPos(), false);
        recalculateCellContent(event.newPos(), false);
    }

    /**
     * Called on TurretIdleEvent and recalculates the cell content at turret position
     */
    public void onTurretIdleEvent(SentryTurretCell.TurretIdleEvent event) {
        recalculateCellContent(event.pos(), false);
    }
    /**
     * Called on TurretChargeEvent and recalculates the cell content at turret position
     */
    public void onTurretChargeEvent(SentryTurretCell.TurretChargeEvent event) {
        recalculateCellContent(event.pos(), false);
    }
    /**
     * Called on TurretFireEvent and recalculates the cell content at turret position
     */
    public void onTurretFireEvent(SentryTurretCell.TurretFireEvent event) {
        recalculateCellContent(event.pos(), false);
    }
    /**
     * Called on ConsumeDrillChargeEvent and recalculates the cell content at
     * the target position if a cell was destroyed
     */
    public void onConsumeDrillChargeEvent(Ship.ConsumeDrillChargeEvent event) {
        if(event.cellDestroyed())
            recalculateCellContent(event.targetPos(), false);
    }
}
