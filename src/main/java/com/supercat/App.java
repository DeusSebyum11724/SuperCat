package com.supercat;

import com.supercat.database.DatabaseManager;
import com.supercat.ui.Theme;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Classe Application JavaFX de SuperCat. Elle initialise la base de donnees
 * MongoDB, cree la fenetre principale et delegue la navigation entre les
 * ecrans au SceneManager.
 */
public class App extends Application {

    private boolean databaseReady = false;

    @Override
    public void start(Stage stage) {
        // connexion a la base de donnees MongoDB
        try {
            DatabaseManager.getInstance();
            databaseReady = true;
        } catch (RuntimeException e) {
            showFatalError(e.getMessage());
            Platform.exit();
            return;
        }

        Scene scene = new Scene(new StackPane(), Theme.SCENE_WIDTH, Theme.SCENE_HEIGHT);
        SceneManager sceneManager = new SceneManager(stage, scene);

        stage.setScene(scene);
        stage.setTitle("SuperCat - Le labyrinthe du chat");
        stage.setResizable(false);
        stage.centerOnScreen();

        sceneManager.showSplash();
        stage.show();
    }

    @Override
    public void stop() {
        if (databaseReady) {
            DatabaseManager.getInstance().close();
        }
    }

    /** Affiche une boite de dialogue d'erreur fatale au demarrage. */
    private void showFatalError(String message) {
        System.err.println("[SuperCat] ERREUR FATALE : " + message);
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle("SuperCat");
        alert.setHeaderText("SuperCat ne peut pas demarrer");
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
