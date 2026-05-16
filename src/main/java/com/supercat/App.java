package com.supercat;

import com.supercat.database.DatabaseManager;
import com.supercat.ui.Theme;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Classe Application JavaFX de SuperCat. Elle initialise la base de donnees,
 * cree la fenetre principale et delegue la navigation entre les ecrans au
 * SceneManager.
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        // initialisation de la base de donnees (creation des tables au besoin)
        DatabaseManager.getInstance();

        Scene scene = new Scene(new StackPane(), Theme.SCENE_WIDTH, Theme.SCENE_HEIGHT);
        SceneManager sceneManager = new SceneManager(stage, scene);

        stage.setScene(scene);
        stage.setTitle("SuperCat - Le labyrinthe du chat");
        stage.setResizable(false);
        stage.centerOnScreen();

        sceneManager.showLogin();
        stage.show();
    }

    @Override
    public void stop() {
        // fermeture propre de la connexion a la base de donnees
        DatabaseManager.getInstance().close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
