package starsalvage.engine.cells.activecells;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import starsalvage.engine.GameEngine;
import starsalvage.engine.Vec2;

import starsalvage.engine.TestConstants;
import starsalvage.engine.cells.AsteroidCell;
import starsalvage.engine.cells.reactivecells.activecells.SentryTurretCell;
import starsalvage.engine.cells.reactivecells.activecells.movingcells.Ship;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SentryTurretCellTest {
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
    void testStartInCharging() {
        SentryTurretCell cell = new SentryTurretCell(engine, new Vec2(0, 0));
        assertEquals(SentryTurretCell.State.CHARGING, cell.state);
    }
    @Test
    void testEnterFiringAfterFirstTick() {
        SentryTurretCell cell = new SentryTurretCell(engine, new Vec2(0, 0));
        cell.onGameTickEvent(new GameEngine.GameTickEvent());
        assertEquals(SentryTurretCell.State.FIRING, cell.state);
    }
    @Test
    void testEnterIdleAfterSecondTick() {
        SentryTurretCell cell = new SentryTurretCell(engine, new Vec2(0, 0));
        cell.onGameTickEvent(new GameEngine.GameTickEvent());
        cell.onGameTickEvent(new GameEngine.GameTickEvent());
        assertEquals(SentryTurretCell.State.IDLE, cell.state);
    }
    @Test
    void testEnterChargingAfterThirdTick() {
        SentryTurretCell cell = new SentryTurretCell(engine, new Vec2(0, 0));
        cell.onGameTickEvent(new GameEngine.GameTickEvent());
        cell.onGameTickEvent(new GameEngine.GameTickEvent());
        cell.onGameTickEvent(new GameEngine.GameTickEvent());
        assertEquals(SentryTurretCell.State.CHARGING, cell.state);
    }
    @Test
    void testHitShipInLineOfFire() {
        SentryTurretCell cell = new SentryTurretCell(engine, new Vec2(2, 0));
        cell.state = SentryTurretCell.State.FIRING;
        cell.onGameTickEvent(new GameEngine.GameTickEvent());

        assertEquals(Ship.MAX_HP - SentryTurretCell.SENTRY_TURRET_HP_REDUCTION, engine.getShip().getHP());
    }
    @Test
    void testMissShipInLineOfFireWhenBlockedByAsteroid() {
        SentryTurretCell cell = new SentryTurretCell(engine, new Vec2(2, 0));
        engine.getSector().cells.put(new Vec2(1, 0), new AsteroidCell(new Vec2(1, 0)));

        cell.state = SentryTurretCell.State.FIRING;
        cell.onGameTickEvent(new GameEngine.GameTickEvent());

        assertEquals(Ship.MAX_HP, engine.getShip().getHP());
    }
    @Test
    void testMissShipOutOfLineOfFire() {
        SentryTurretCell cell = new SentryTurretCell(engine, new Vec2(2, 1));
        cell.state = SentryTurretCell.State.FIRING;
        cell.onGameTickEvent(new GameEngine.GameTickEvent());

        assertEquals(Ship.MAX_HP, engine.getShip().getHP());
    }
}
