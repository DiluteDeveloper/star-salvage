package starsalvage.engine.cells.activecells.movingcells;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import starsalvage.engine.GameEngine;
import starsalvage.engine.TestConstants;
import starsalvage.engine.Vec2;
import starsalvage.engine.cells.AsteroidCell;
import starsalvage.engine.cells.reactivecells.DrillChargeCell;
import starsalvage.engine.cells.reactivecells.activecells.movingcells.MovingCell;
import starsalvage.engine.cells.reactivecells.activecells.SentryTurretCell;
import starsalvage.engine.cells.reactivecells.activecells.movingcells.ScoutDroneCell;
import starsalvage.engine.cells.reactivecells.activecells.movingcells.SpaceMineCell;

import static org.junit.jupiter.api.Assertions.*;

public class ShipTest {

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
    void testDrillChargeSectorPickup() {
        engine.getSector().cells.put(new Vec2(0, 1), new DrillChargeCell(engine, new Vec2(0, 1)));

        engine.getShip().move(MovingCell.Direction.DOWN);

        assertEquals(1, engine.getShip().getNumDrillCharges());
    }
    @Test
    void testDrillChargeRawPickup() {
        engine.getShip().pickupDrillCharge();
        assertEquals(1, engine.getShip().getNumDrillCharges());
    }
    @Test
    void testConsumeDrillChargeHitNothing() {
        engine.getShip().pickupDrillCharge();
        engine.getShip().consumeDrillCharge(MovingCell.Direction.DOWN);

        assertEquals(0, engine.getShip().getNumDrillCharges());
    }
    @Test
    void testConsumeDrillChargeDestroyAsteroid() {
        engine.getSector().cells.put(new Vec2(0, 1), new AsteroidCell(new Vec2(0, 1)));
        engine.getShip().pickupDrillCharge();
        engine.getShip().consumeDrillCharge(MovingCell.Direction.DOWN);

        assertEquals(0, engine.getShip().getNumDrillCharges());
        assertFalse(engine.getSector().cells.containsKey(new Vec2(0, 1)));
    }
    @Test
    void testRewindCorrectness() {
        engine.getSector().cells.put(new Vec2(1, 1),
                new DrillChargeCell(engine, new Vec2(1, 1)));

        AsteroidCell asteroidCell = new AsteroidCell(new Vec2(2, 1));
        engine.getSector().cells.put(new Vec2(2, 1),
                asteroidCell);

        ScoutDroneCell scoutDroneCell = new ScoutDroneCell(engine, new Vec2(3, 1));
        engine.getSector().overlayCells.add(scoutDroneCell);

        SpaceMineCell spaceMineCell = new SpaceMineCell(engine, new Vec2(4, 1));
        engine.getSector().overlayCells.add(spaceMineCell);

        SentryTurretCell sentryTurretCell = new SentryTurretCell(engine, new Vec2(5, 1));
        engine.getSector().cells.put(new Vec2(5, 1), sentryTurretCell);

        int ogNumDrillCharges = 0;
        Vec2 ogShipPos = new Vec2(0);
        Vec2 ogScoutDronePos = new Vec2(3, 1);
        ScoutDroneCell.State ogScoutDroneState = ScoutDroneCell.State.PATROL;
        Vec2 ogSpaceMinePos = new Vec2(4, 1);
        SentryTurretCell.State ogSentryTurretState = SentryTurretCell.State.CHARGING;

        // Check initial values are correct before actions and rewinds

        assertEquals(ogNumDrillCharges, engine.getShip().getNumDrillCharges());
        assertEquals(ogShipPos, engine.getShip().pos);

        assertTrue(engine.getSector().cells.containsKey(asteroidCell.pos));

        assertEquals(ogScoutDronePos, scoutDroneCell.pos);
        assertEquals(ogScoutDroneState, scoutDroneCell.state);

        assertEquals(ogSpaceMinePos, spaceMineCell.pos);
        assertEquals(ogSentryTurretState, sentryTurretCell.state);

        // Pickup drill charge
        engine.getShip().move(MovingCell.Direction.RIGHT);

        // Wait
        engine.getShip().waitForTick();

        // Use drill charge on asteroid
        engine.getShip().consumeDrillCharge(MovingCell.Direction.RIGHT);

        // Move down
        engine.getShip().consumeDrillCharge(MovingCell.Direction.DOWN);

        // Move left
        engine.getShip().consumeDrillCharge(MovingCell.Direction.LEFT);

        for(int i = 0; i < 5; i++) {
            engine.rewind();
        }

        // Check initial state is restored when rewinding

        assertEquals(ogNumDrillCharges, engine.getShip().getNumDrillCharges());
        assertEquals(ogShipPos, engine.getShip().pos);

        assertTrue(engine.getSector().cells.containsKey(asteroidCell.pos));

        assertEquals(ogScoutDronePos, scoutDroneCell.pos);
        assertEquals(ogScoutDroneState, scoutDroneCell.state);

        assertEquals(ogSpaceMinePos, spaceMineCell.pos);

        assertEquals(ogSentryTurretState, sentryTurretCell.state);
    }
}
