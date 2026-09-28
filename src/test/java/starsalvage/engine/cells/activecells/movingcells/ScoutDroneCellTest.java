package starsalvage.engine.cells.activecells.movingcells;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import starsalvage.engine.GameEngine;
import starsalvage.engine.Vec2;

import static org.junit.jupiter.api.Assertions.*;

import starsalvage.engine.TestConstants;
import starsalvage.engine.cells.reactivecells.activecells.movingcells.ScoutDroneCell;

class ScoutDroneCellTest {

    private GameEngine engine;

    @BeforeEach
    void setUp() {
        engine = new GameEngine(TestConstants.TEST_SEED);
        engine.tryBeginNextSector();
        engine.getShip().pos = new Vec2(0, 0);
        engine.getSector().cells.clear();
        engine.getSector().overlayCells.clear();
        engine.getSector().overlayCells.add(engine.getShip());
    }
    @Test
    void testStartInPatrol() {
        ScoutDroneCell cell = new ScoutDroneCell(engine, new Vec2(0, 0));
        assertEquals(ScoutDroneCell.State.PATROL, cell.state);
    }
    @Test
    void testContinuePatrolStateWhenOutOfRange() {
        ScoutDroneCell cell = new ScoutDroneCell(engine, new Vec2(4, 1));

        // Scout drone moves LEFT in TEST_SEED to (3, 1), then checks if in range
        cell.onGameTickEvent(new GameEngine.GameTickEvent());

        assertEquals(ScoutDroneCell.State.PATROL, cell.state);
    }
    @Test
    void testEscalatePatrolToAlertStateWhenInRange() {
        ScoutDroneCell cell = new ScoutDroneCell(engine, new Vec2(3, 1));

        // Scout drone moves LEFT in TEST_SEED to (2, 1), then checks if in range
        cell.onGameTickEvent(new GameEngine.GameTickEvent());

        assertEquals(ScoutDroneCell.State.ALERT, cell.state);
    }
    @Test
    void testDecayAlertToPatrolStateWhenOutOfRange() {
        ScoutDroneCell cell = new ScoutDroneCell(engine, new Vec2(3, 1));
        cell.state = ScoutDroneCell.State.ALERT;

        // Scout drone does not move, then checks if in range
        cell.onGameTickEvent(new GameEngine.GameTickEvent());

        assertEquals(ScoutDroneCell.State.PATROL, cell.state);
    }
    @Test
    void testEscalateAlertToChaseStateWhenInRange() {
        ScoutDroneCell cell = new ScoutDroneCell(engine, new Vec2(2, 1));
        cell.state = ScoutDroneCell.State.ALERT;

        // Scout drone does not move, then checks if in range
        cell.onGameTickEvent(new GameEngine.GameTickEvent());

        assertEquals(ScoutDroneCell.State.CHASE, cell.state);
    }

    @Test
    void testDecayChaseToPatrolStateWhenOutOfRange() {
        ScoutDroneCell cell = new ScoutDroneCell(engine, new Vec2(4, 1));
        cell.state = ScoutDroneCell.State.CHASE;

        // Scout drone moves LEFT in TEST_SEED to (3, 1), then checks if in range
        cell.onGameTickEvent(new GameEngine.GameTickEvent());

        assertEquals(ScoutDroneCell.State.PATROL, cell.state);
    }
    @Test
    void testContinueChaseStateWhenInRange() {
        ScoutDroneCell cell = new ScoutDroneCell(engine, new Vec2(3, 1));
        cell.state = ScoutDroneCell.State.CHASE;

        // Scout drone moves LEFT in TEST_SEED to (2, 1), then checks if in range
        cell.onGameTickEvent(new GameEngine.GameTickEvent());

        assertEquals(ScoutDroneCell.State.CHASE, cell.state);
    }
}
