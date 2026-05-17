package com.supercat;

import com.supercat.database.DatabaseManager;
import com.supercat.engine.MusicPlayer;
import com.supercat.engine.SoundEffects;
import com.supercat.service.Settings;
import com.supercat.ui.Theme;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Classe Application JavaFX de SuperCat. Elle initialise la base de donnees,
 * cree la fenetre principale (plein ecran par defaut) et delegue la
 * navigation entre les ecrans au SceneManager.
 */
public class App extends Application {

    private boolean databaseReady = false;

    @Override
    public void start(Stage stage) {
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

        // musique d'ambiance continue + preparation du miaou
        MusicPlayer.instance().start();
        SoundEffects.preload();

        stage.setScene(scene);
        stage.setTitle("SuperCat");
        stage.setResizable(false);
        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        stage.setFullScreen(Settings.isFullscreen());
        if (!Settings.isFullscreen()) {
            stage.setWidth(900);
            stage.setHeight(740);
            stage.centerOnScreen();
        }

        sceneManager.showStudioSplash();
        stage.show();
    }

    @Override
    public void stop() {
        MusicPlayer.instance().stop();
        if (databaseReady) {
            DatabaseManager.getInstance().close();
        }
    }

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
