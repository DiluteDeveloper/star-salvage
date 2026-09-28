package starsalvage.engine.cells.activecells.movingcells;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import starsalvage.engine.GameEngine;
import starsalvage.engine.TestConstants;
import starsalvage.engine.Vec2;
import starsalvage.engine.cells.AsteroidCell;
import starsalvage.engine.cells.reactivecells.activecells.SentryTurretCell;
import starsalvage.engine.cells.reactivecells.activecells.movingcells.ScoutDroneCell;
import starsalvage.engine.cells.reactivecells.activecells.movingcells.SpaceMineCell;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SpaceMineCellTest {
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
    void testDrift() {
        SpaceMineCell cell = new SpaceMineCell(engine, new Vec2(5, 5));

        // Space mine attempts drifts LEFT in TEST_SEED to (4, 5)
        // and reflects backwards to (6, 5) if it detects a blocked cell or sector border
        cell.onGameTickEvent(new GameEngine.GameTickEvent());

        assertEquals(new Vec2(4, 5), cell.pos);
    }
    @Test
    void testReflectWhenAsteroidHit() {
        SpaceMineCell cell = new SpaceMineCell(engine, new Vec2(5, 5));
        engine.getSector().cells.put(new Vec2(4, 5), new AsteroidCell(new Vec2(4,5)));

        // Space mine attempts drifts LEFT in TEST_SEED to (4, 5)
        // and reflects backwards to (6, 5) if it detects a blocked cell or sector border
        cell.onGameTickEvent(new GameEngine.GameTickEvent());

        assertEquals(new Vec2(6, 5), cell.pos);
    }
    @Test
    void testReflectWhenScoutDroneHit() {
        SpaceMineCell cell = new SpaceMineCell(engine, new Vec2(5, 5));
        engine.getSector().overlayCells.add(new ScoutDroneCell(engine, new Vec2(4,5)));

        // Space mine attempts drifts LEFT in TEST_SEED to (4, 5)
        // and reflects backwards to (6, 5) if it detects a blocked cell or sector border
        cell.onGameTickEvent(new GameEngine.GameTickEvent());

        assertEquals(new Vec2(6, 5), cell.pos);
    }
    @Test
    void testReflectWhenSpaceMineHit() {
        SpaceMineCell cell = new SpaceMineCell(engine, new Vec2(5, 5));
        engine.getSector().overlayCells.add(new SpaceMineCell(engine, new Vec2(4,5)));

        // Space mine attempts drifts LEFT in TEST_SEED to (4, 5)
        // and reflects backwards to (6, 5) if it detects a blocked cell or sector border
        cell.onGameTickEvent(new GameEngine.GameTickEvent());

        assertEquals(new Vec2(6, 5), cell.pos);
    }
    @Test
    void testReflectWhenSentryTurretHit() {
        SpaceMineCell cell = new SpaceMineCell(engine, new Vec2(5, 5));
        engine.getSector().cells.put(new Vec2(4, 5),
                new SentryTurretCell(engine, new Vec2(4,5)));

        // Space mine attempts drifts LEFT in TEST_SEED to (4, 5)
        // and reflects backwards to (6, 5) if it detects a blocked cell or sector border
        cell.onGameTickEvent(new GameEngine.GameTickEvent());

        assertEquals(new Vec2(6, 5), cell.pos);
    }
    @Test
    void testReflectWhenSectorBorderHit() {
        SpaceMineCell cell = new SpaceMineCell(engine, new Vec2(0, 9));

        // Space mine attempts drifts LEFT in TEST_SEED to (4, 5)
        // and reflects backwards to (6, 5) if it detects a blocked cell or sector border
        cell.onGameTickEvent(new GameEngine.GameTickEvent());

        assertEquals(new Vec2(1, 9), cell.pos);
    }
}
