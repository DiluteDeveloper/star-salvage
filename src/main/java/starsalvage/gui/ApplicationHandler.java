package starsalvage.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import starsalvage.engine.Vec2;

/**
 * GUI for the Maze Runner Game.
 *
 * NOTE: Do NOT run this class directly in IntelliJ - run 'RunGame' instead.
 */
public class ApplicationHandler extends Application {

    /**
     * The X and Y size of the game window
     */
    private static final int WINDOW_SIZE_X = 920, WINDOW_SIZE_Y = 720;
    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane root = new FXMLLoader(getClass().getResource("game_gui.fxml")).load();

        primaryStage.setScene(new Scene(root, WINDOW_SIZE_X, WINDOW_SIZE_Y));
        primaryStage.setTitle("MiniDungeon Game");
        primaryStage.show();
    }

    /** In IntelliJ, do NOT run this method.  Run 'RunGame.main()' instead. */
    public static void main(String[] args) {
        launch(args);
    }
}
