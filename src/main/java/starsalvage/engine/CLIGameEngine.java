package starsalvage.engine;

import starsalvage.engine.cells.Cell;
import starsalvage.engine.cells.EmptyCell;
import starsalvage.engine.cells.reactivecells.activecells.movingcells.Ship;
import starsalvage.engine.cells.reactivecells.activecells.movingcells.MovingCell;
import starsalvage.engine.cells.reactivecells.activecells.SentryTurretCell;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

import static starsalvage.engine.Result.ErrType.NO_HISTORY;

/**
 * Runs the Game Engine with a command line interface
 */
public class CLIGameEngine extends GameEngine {

    /**
     * Command line interface entry point
     */
    public static void main(String[] args)
    {
        Logger logger = Logger.getLogger("StarSalvage");
        try {
            CLIGameEngine engine = new CLIGameEngine((int)System.currentTimeMillis());
        } catch (Throwable e) {
            logger.severe(e.getMessage());
        }
    }

    /**
     * Number of characters to offset each line of the display cache
     */
    private final static int ADDITIONAL_DISPLAY_CACHE_OFFSET = 30;
    /**
     * Character width format for the additional display cache
     */
    private final static String ADDITIONAL_DISPLAY_CACHE_OFFSET_FMT = "%-" + ADDITIONAL_DISPLAY_CACHE_OFFSET + "s";
    /**
     * Number of divider characters between map prints
     */
    private final static int CLI_MAP_DIVIDER_LENGTH = 40;

    /**
     * List of strings for controls and player stats to be printed
     */
    private final List<String> displayCache = new ArrayList<>();

    /**
     * Messages to be put on the display panel
     * @param message The message
     * @param priority Lower=higher priority; the order the message shows on the display panel
     */
    public record PriorityMessage(String message, float priority) {};
    /**
     * History for the additional display cache to be recalled on rewind
     */
    private final List<List<PriorityMessage>> additionalDisplayCacheHistory = new ArrayList<>();
    /**
     * Additional messages to be put on the display cache such as turret states, hp loss, score increase
     */
    private List<PriorityMessage> additionalDisplayCache = new ArrayList<>();

    /**
     * Reads input from the command line
     */
    private final Scanner scanner = new Scanner(System.in);

    /**
     * Gets player stats as a list of strings
     * @return Player stats
     */
    public List<String> getPlayerStatsAsString() {
        List<String> stats = new ArrayList<>();
        stats.add("| Remaining Steps: " + (Ship.MAX_STEPS - ship.getStepCount()));
        if(ship.getNumDrillCharges()!=0)
            stats.add("| Drill Charges: " + ship.getNumDrillCharges());
        stats.add("| Sector: " + getSectorIdx());
        if(ship.getScore()!=0)
            stats.add("| Score: " + ship.getScore());
        stats.add("| HP: " + ship.getHP());
        return stats;
    }

    /**
     * Prints the sector to the console,
     * as well as the display cache and additional display cache and dividers;
     * Also clears the display cache and additional display cache
     */
    private void printMap() {
        additionalDisplayCache.sort(Comparator.comparing(PriorityMessage::priority));
        System.out.printf("%-4s\n", "_".repeat(CLI_MAP_DIVIDER_LENGTH));
        for(int y = 0; y < GameEngine.BOARD_SIZE.y; y++) {

            for(int x = 0; x < GameEngine.BOARD_SIZE.x; x++) {
                Vec2 pos = new Vec2(x,y);
                Result<Cell> cell = sector.getCell(pos);
                Result<Cell> overlayCell = sector.getOverlayCell(pos);

                // If moving cell exists
                if(overlayCell.isSuccess)
                    System.out.printf("%-4s", overlayCell.getValueOrNull().getCharacter());
                else if(cell.isSuccess)
                    System.out.printf("%-4s", cell.getValueOrNull().getCharacter());
                else
                    System.out.printf("%-4s", EmptyCell.getCharacterStatic());
            }
            if(y < displayCache.size()) {
                System.out.printf(ADDITIONAL_DISPLAY_CACHE_OFFSET_FMT, "  " + displayCache.get(y));
            } else
                System.out.printf(ADDITIONAL_DISPLAY_CACHE_OFFSET_FMT, " ".repeat(ADDITIONAL_DISPLAY_CACHE_OFFSET));

            int cols = (int) Math.ceil((double) additionalDisplayCache.size() / GameEngine.BOARD_SIZE.y);
            for(int col = 0; col < cols; col++) {
                int index = col * GameEngine.BOARD_SIZE.y + y;
                if(index < additionalDisplayCache.size()) {
                    System.out.printf(ADDITIONAL_DISPLAY_CACHE_OFFSET_FMT, "| " + additionalDisplayCache.get(index).message());
                }
            }
            System.out.println();
        }
        additionalDisplayCache.clear();
        displayCache.clear();
        System.out.printf("%-4s\n", "_".repeat(CLI_MAP_DIVIDER_LENGTH));
    }
    /**
     * Begins the CLI interface and initialises underlying GameEngine
     * @param seed The random seed
     */
    public CLIGameEngine(int seed) {
        // setup difficulty selection here
        super(seed);

        while(true) {
            System.out.println("Please enter difficulty: (" +
                    GameEngine.MIN_DIFFICULTY + "-" + GameEngine.MAX_DIFFICULTY + ")");
            try {
                int input = scanner.nextInt();
                if(input < GameEngine.MIN_DIFFICULTY || input > GameEngine.MAX_DIFFICULTY) {
                    System.out.println("Invalid difficulty!");
                    continue;
                }
                setDifficulty(input);
                scanner.nextLine();
                break;
            } catch (Exception e){
                System.out.println("Invalid difficulty!");
                scanner.nextLine();
            }
        }
        subscribeAll();

        tryBeginNextSector();
        beginGameInterface();
    }

    /**
     * Subscribe to all events required by CLIGameEngine
     */
    private void subscribeAll() {
        bus.subscribe(Ship.LoseHPEvent.class, this::onShipLoseHPEvent);
        bus.subscribe(Ship.RestoreHPEvent.class, this::onShipRestoreHPEvent);
        bus.subscribe(Ship.IncreaseScoreEvent.class, this::onIncreaseScoreEvent);
        bus.subscribe(GameEngine.RewindEvent.class, this::onRewindEvent);
        bus.subscribe(GameTickEvent.class, this::onGameTickEvent);
        bus.subscribe(SentryTurretCell.TurretChargeEvent.class, this::onTurretChargeEvent);
        bus.subscribe(SentryTurretCell.TurretFireEvent.class, this::onTurretFireEvent);
    }

    /**
     * Begins the main menu game CLI interface
     */
    private void beginGameInterface() {

        while(true) {

            if(gameState != GameState.IN_PROGRESS) {
                while(true) {
                    displayCache.add("-> View results: \"v\"");
                    displayCache.addAll(getPlayerStatsAsString());
                    printMap();
                    String input = scanner.nextLine();
                    if(input.equalsIgnoreCase("v"))
                        break;
                    else
                        System.out.println("Not a valid action, please try again.");
                }
                beginResultsInterface();
                return;
            }
            int drillChargeCount = ship.getNumDrillCharges();
            displayCache.add("-> Move: \"m\"");
            displayCache.add("-> Wait: \"w\"");
            displayCache.add("-> Rewind: \"z\"");
            if(drillChargeCount > 0) {
                displayCache.add("-> Use a drill charge: \"b\"");
            }
            displayCache.addAll(getPlayerStatsAsString());
            printMap();

            String input = scanner.nextLine();
            switch(input.toLowerCase()) {
                case "b":
                    if(drillChargeCount>0) {
                        beginDrillChargeInterface();
                    } else
                        additionalDisplayCache.add(new PriorityMessage("You have no drill charges!", 1));
                    break;
                case "m":
                    beginMoveInterface();
                    break;
                case "w":
                    ship.waitForTick();
                    additionalDisplayCache.add(new PriorityMessage("You waited a turn.", 1));
                    continue;
                case "z":
                    var r = rewind();
                    if(!r.isSuccess)
                        additionalDisplayCache.add(new PriorityMessage(r.getMessage(), -50));
                    else
                        additionalDisplayCache.add(new PriorityMessage("You rewound time by one move.", -20));
                    continue;
                default:
                    additionalDisplayCache.add(new PriorityMessage("Not a valid action, please try again.", 1));
                    continue;
            }
        }
    }
    /**
     * Begins the drill charge CLI interface
     */
    private void beginDrillChargeInterface() {

        while(true) {

            if(gameState != GameState.IN_PROGRESS)
                return;

            displayCache.add("-> Main Menu: \"m\"");
            displayCache.add("-> Fire Left: \"l\"");
            displayCache.add("-> Fire Right: \"r\"");
            displayCache.add("-> Fire Up: \"u\"");
            displayCache.add("-> Fire Down: \"d\"");
            displayCache.addAll(getPlayerStatsAsString());

            printMap();
            String input = scanner.nextLine();

            Result<?> result = Result.err(Result.ErrType.INVALID_DIRECTION,
                    "Invalid direction, please try again.", LOGGER);
            switch (input.toLowerCase()) {
                case "m" -> {
                    return;
                }
                case "l" -> result = ship.consumeDrillCharge(MovingCell.Direction.LEFT);
                case "r" -> result = ship.consumeDrillCharge(MovingCell.Direction.RIGHT);
                case "u" -> result = ship.consumeDrillCharge(MovingCell.Direction.UP);
                case "d" -> result = ship.consumeDrillCharge(MovingCell.Direction.DOWN);
            };
            if(result.isSuccess) {
                additionalDisplayCache.add(new PriorityMessage("You destroyed an asteroid!", 1));
                return;
            }

            additionalDisplayCache.add(new PriorityMessage(result.getMessage(), 1));
            if(result.getErrType()!= Result.ErrType.NO_DRILL_CHARGES_AVAILABLE) {
                additionalDisplayCache.add(new PriorityMessage("The drill charge was wasted.", 1));
                return;
            }
        }
    }
    /**
     * Begins the movement CLI interface
     */
    private void beginMoveInterface() {

        while(true) {
            if(gameState != GameState.IN_PROGRESS)
                return;

            displayCache.add("-> Main Menu: \"m\"");
            displayCache.add("-> Left: \"l\"");
            displayCache.add("-> Right: \"r\"");
            displayCache.add("-> Up: \"u\"");
            displayCache.add("-> Down: \"d\"");
            displayCache.addAll(getPlayerStatsAsString());

            printMap();
            String input = scanner.nextLine();

            Result<Vec2> result = Result.err(Result.ErrType.INVALID_DIRECTION,
                    "Invalid direction, please try again.", LOGGER);
            switch (input.toLowerCase()) {
                case "m" -> {
                    return;
                }
                case "l" -> result = ship.move(MovingCell.Direction.LEFT);
                case "r" -> result = ship.move(MovingCell.Direction.RIGHT);
                case "u" -> result = ship.move(MovingCell.Direction.UP);
                case "d" -> result = ship.move(MovingCell.Direction.DOWN);
            };
            if(result.isSuccess)
                continue;

            additionalDisplayCache.add(new PriorityMessage(result.getMessage(), 1));
            additionalDisplayCache.add(new PriorityMessage("Please try again.", 1));
        }
    }
    /**
     * Triggers the result screen CLI interface
     */
    private void beginResultsInterface() {

        switch(gameState) {
            case WIN:
                System.out.println("You won with " + ship.getScore() + " score! Thanks for playing!");
                return;
            case SHIP_DESTROYED:
                System.out.println("Your ship was destroyed! Thanks for playing!");
                return;
            case OUT_OF_STEPS:
                System.out.println("You ran out of steps! Thanks for playing!");
        }
    }

    /**
     * Called on CellCollisionEvent and updates additionalDisplayCache
     * to show relevant info for cell collision
     */
    @Override
    public void onCellCollisionEvent(CellCollisionEvent event) {
        super.onCellCollisionEvent(event);

        Cell other = null;
        if(event.movingCell().getCellType()== Cell.CellType.SHIP)
            other=event.collidedCell();
        else if(event.collidedCell().getCellType()== Cell.CellType.SHIP)
            other=event.movingCell();
        else
            return;

        Cell.CellType type = other.getCellType();
        switch(type) {
            case DRILL_CHARGE_CELL -> additionalDisplayCache.add(new PriorityMessage("You picked up a drill charge!", type.id));
            case HULL_PATCH_CELL -> additionalDisplayCache.add(new PriorityMessage("You picked up a hull patch!", type.id));
            case SCRAP_CARGO_CELL -> additionalDisplayCache.add(new PriorityMessage("You picked up some scrap cargo!", type.id));
            case WORMHOLE_CELL -> additionalDisplayCache.add(new PriorityMessage("You hit a wormhole and reached sector two!", type.id));
            case SENTRY_TURRET_CELL -> {
                if(other instanceof SentryTurretCell turret)
                    if(turret.state== SentryTurretCell.State.IDLE)
                        additionalDisplayCache.add(new PriorityMessage("You destroyed a turret!", type.id));
                    else
                        additionalDisplayCache.add(new PriorityMessage("You hit a turret!", type.id));
            }
            case SCOUT_DRONE_CELL -> additionalDisplayCache.add(new PriorityMessage("You hit a scout drone!", type.id));
            case SPACE_MINE_CELL -> additionalDisplayCache.add(new PriorityMessage("You hit a space mine!", type.id));
            case GRAVITY_WELL_CELL -> additionalDisplayCache.add(new PriorityMessage("You hit a gravity well!", type.id));
        }
    }
    /**
     * Called on Ship.LoseHPEvent and updates additionalDisplayCache
     * to show HP lost
     */
    public void onShipLoseHPEvent(Ship.LoseHPEvent event) {
        additionalDisplayCache.add(new PriorityMessage(" - You lost " + event.lostHP() + " HP", event.publisher().getCellType().id + 0.5f));
    }
    /**
     * Called on Ship.RestoreHPEvent and updates additionalDisplayCache
     * to show HP restored
     */
    public void onShipRestoreHPEvent(Ship.RestoreHPEvent event) {
        additionalDisplayCache.add(new PriorityMessage(" - You restored " + event.hpRestored() + " HP", event.publisher().getCellType().id + 0.2f));
    }
    /**
     * Called on Ship.IncreaseScoreEvent and updates additionalDisplayCache
     * to show score increased
     */
    public void onIncreaseScoreEvent(Ship.IncreaseScoreEvent event) {
        additionalDisplayCache.add(new PriorityMessage(" - Your score increased by " + event.scoreIncrease(), event.publisher().getCellType().id + 0.1f));
    }
    /**
     * Called on MovingCell.MoveEvent and updates additionalDisplayCache
     * to show direction the ship moved
     */
    @Override
    public void onMoveEvent(MovingCell.MoveEvent event) {
        super.onMoveEvent(event);
        if(event.movingCell().getCellType() == Cell.CellType.SHIP)
            additionalDisplayCache.add(new PriorityMessage("You moved " + event.direction().name().toLowerCase() + ".", -1));
    }

    /**
     * Called on EndGameEvent and updates additionalDisplayCache
     * to show game results
     */
    @Override
    public void onEndGameEvent(EndGameEvent event) {
        super.onEndGameEvent(event);
        gameState = event.result();
        switch(gameState) {
            case WIN -> additionalDisplayCache.add(new PriorityMessage("Game over, you won!", 100));
            case SHIP_DESTROYED -> additionalDisplayCache.add(new PriorityMessage("Game over, your ship was destroyed!", 100));
            case OUT_OF_STEPS -> additionalDisplayCache.add(new PriorityMessage("Game over, you ran out of steps!", 100));
        }
    }
    /**
     * Called on RewindEvent and updates additionalDisplayCache
     * to the previous tick additionalDisplayCache
     */
    public void onRewindEvent(GameEngine.RewindEvent event) {
        if(additionalDisplayCacheHistory.size() >= 2)
            additionalDisplayCache = (additionalDisplayCacheHistory.get(additionalDisplayCacheHistory.size() - 2));
        additionalDisplayCacheHistory.remove(additionalDisplayCacheHistory.size() - 1);
    }
    /**
     * Used to only show turret actions in the command line once to not overload the CLI
     */
    private boolean turretActionDisplayedThisTick = false;
    /**
     * called on GameTickEvent and adds additional display cache history
     * and resets turretActionDisplayedThisTick
     */
    public void onGameTickEvent(GameEngine.GameTickEvent event) {
        additionalDisplayCacheHistory.add(new ArrayList<>(additionalDisplayCache));
        turretActionDisplayedThisTick = false;
    }
    /**
     * called on SentryTurretCell.TurretChargeEvent and adds turret charge message to additionalDisplayCache
     */
    public void onTurretChargeEvent(SentryTurretCell.TurretChargeEvent event) {
        if(turretActionDisplayedThisTick)
            return;

        additionalDisplayCache.add(new PriorityMessage("Turrets are charging!", 50));
        turretActionDisplayedThisTick = true;
    }
    /**
     * called on SentryTurretCell.TurretFireEvent and adds turret fire message to additionalDisplayCache
     */
    public void onTurretFireEvent(SentryTurretCell.TurretFireEvent event) {
        if(!turretActionDisplayedThisTick)
            additionalDisplayCache.add(new PriorityMessage("Turrets fired!", 10));
        if(event.hitShip())
                additionalDisplayCache.add(new PriorityMessage("You were hit by a turret!", 10.1f));
        turretActionDisplayedThisTick = true;
    }

}
