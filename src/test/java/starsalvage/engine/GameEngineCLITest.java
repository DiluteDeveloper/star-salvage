package starsalvage.engine;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameEngineCLITest {

    //public static void main(String[] args) {
     //   CLIGameEngine engine = new CLIGameEngine(TestConstants.TEST_SEED);
    //}
    @Test
    public void testFullGameWin() {
        List<String> inputs = List.of(
                "3", // Set difficulty
                "m", "r", "u", "u", "l", // Pickup drill charge, hit drone
               "m", "b", "d", "m", "d",// Destroy an asteroid with the drill charge and move into it - 6 steps by end
                "m", "w", // Wait and get hit by a drone - 7 steps
                "m", "r", "r", // Move right and pickup a hull patch - 9 steps
                "u", "u", "r", "u", "r", // Move towards wormhole - 14 steps
                "d", "m", "z", "z", "z", // Pickup drill charge then rewind thrice - 12 steps
                "m", "r", "m", "w", // Re pickup drill charge and get hit by turret - 14 steps
                "m", "r", "m", "z", "m", "r", // Destroy turret and rewind and destroy it again - 15 steps
                "r", "r", "r", "u", // Go to and enter wormhole - 19 steps
                "l", "u", "m", "z", "z", // Hit gravity well then rewind twice - 19 steps
                "m", "l", "l", "l", "l", "l", // Move towards wormhole and get hit by turret - 24 steps
                "d", "l", "l", // Move towards wormhole and hit a space mine - 27 steps
                "l", // Pickup a scrap cargo - 28 steps
                "r", "m", "w", // dodge turret fire - 30 steps
                "m", "d", "d", "d", "r", // move towards wormhole and pickup hull patch - 34 steps
                "r", "v" // Win while hitting drone and view results - 35 steps
        );

        String input = String.join("\n", inputs) + "\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        CLIGameEngine engine = new CLIGameEngine(TestConstants.TEST_SEED);

        assertEquals(35, engine.getShip().getStepCount());
        assertEquals(2, engine.getSectorIdx());
        assertEquals(1, engine.getShip().getNumDrillCharges());
        assertEquals(11, engine.getShip().getScore());
        assertEquals(4, engine.getShip().getHP());
        assertEquals(GameEngine.GameState.WIN, engine.getGameState());

    }
    @Test
    public void testFullGameLossHP() {
        List<String> inputs = List.of(
                "3", // Set difficulty
                "m", "r", "u", "u", "l", // Pickup drill charge, hit drone
                "m", "b", "d", "m", "d",// Destroy an asteroid with the drill charge and move into it - 6 steps by end
                "m", "w", // Wait and get hit by a drone - 7 steps
                "m", "r", "r", // Move right and pickup a hull patch - 9 steps
                "u", "u", "r", "u", "r", // Move towards wormhole - 14 steps
                "d", "m", "z", "z", "z", // Pickup drill charge then rewind thrice - 12 steps
                "m", "r", "m", "w", // Re pickup drill charge and get hit by turret - 14 steps
                "m", "r", "m", "z", "m", "r", // Destroy turret and rewind and destroy it again - 15 steps
                "r", "r", "r", "u", // Go to and enter wormhole - 19 steps
                "l", "u", "m", "z", "z", // Hit gravity well then rewind twice - 19 steps
                "m", "l", "l", "l", "l", "l", // Move towards wormhole and get hit by turret - 24 steps
                "d", "l", "l", // Move towards wormhole and hit a space mine - 27 steps
                "l", // Pickup a scrap cargo - 28 steps
                "r", "d", // hit by turret and lose - 30 steps
                "v"
        );

        String input = String.join("\n", inputs) + "\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        CLIGameEngine engine = new CLIGameEngine(TestConstants.TEST_SEED);

        assertEquals(30, engine.getShip().getStepCount());
        assertEquals(2, engine.getSectorIdx());
        assertEquals(1, engine.getShip().getNumDrillCharges());
        assertEquals(-1, engine.getShip().getScore());
        assertEquals(0, engine.getShip().getHP());
        assertEquals(GameEngine.GameState.SHIP_DESTROYED, engine.getGameState());

    }
    @Test
    public void testFullGameOutOfSteps() {
        List<String> inputs = List.of(
                "3", // Set difficulty
                "m", "r", "u", "u", "l", // Pickup drill charge, hit drone
                "m", "b", "d", "m", "d",// Destroy an asteroid with the drill charge and move into it - 6 steps by end
                "m", "w", // Wait and get hit by a drone - 7 steps
                "m", "r", "r", // Move right and pickup a hull patch - 9 steps
                "u", "u", "r", "u", "r", // Move towards wormhole - 14 steps
                "d", "m", "z", "z", "z", // Pickup drill charge then rewind thrice - 12 steps
                "m", "r", "m", "w", // Re pickup drill charge and get hit by turret - 14 steps
                "m", "r", "m", "z", "m", "r", // Destroy turret and rewind and destroy it again - 15 steps
                "r", "r", "r", "u", // Go to and enter wormhole - 19 steps
                "l", "u", "m", "z", "z", // Hit gravity well then rewind twice - 19 steps
                "m", "l", "l", "l", "l", "l", // Move towards wormhole and get hit by turret - 24 steps
                "d", "l", "l", // Move towards wormhole and hit a space mine - 27 steps
                "l", // Pickup a scrap cargo - 28 steps
                "r", "m", // get ready to wait - 29 steps
                "w", "w", "w", "w", "w", "w", "w", // 36 steps
                "w", "w", "w", "w", "w", "w", "w", // 43 steps
                "w", "w", "w", "w", "w", "w", "w", // 50 steps
                "w", "w", "w", "w", "w", "w", "w", // 57 steps
                "w", "w", "w", "w", "w", "w", "w", // 64 steps
                "w", "w", "w", "w", "w", "w", "w", // 71 steps
                "w", "w", "w", "w", "w", "w", "w", // 78 steps
                "w", "w", "w", "w", "w", "w", "w", // 85 steps
                "w", "w", "w", "w", "w", "w", "w", // 92 steps
                "w", "w", "w", "w", "w", "w", "w", // 99 steps
                "w", // 100 steps
                "v"
        );

        String input = String.join("\n", inputs) + "\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        CLIGameEngine engine = new CLIGameEngine(TestConstants.TEST_SEED);

        assertEquals(100, engine.getShip().getStepCount());
        assertEquals(2, engine.getSectorIdx());
        assertEquals(1, engine.getShip().getNumDrillCharges());
        assertEquals(-1, engine.getShip().getScore());
        assertEquals(2, engine.getShip().getHP());
        assertEquals(GameEngine.GameState.OUT_OF_STEPS, engine.getGameState());

    }
}
