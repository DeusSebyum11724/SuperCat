package com.supercat;

import com.supercat.controller.AdminController;
import com.supercat.controller.GameController;
import com.supercat.controller.HomeController;
import com.supercat.controller.LeaderboardController;
import com.supercat.controller.LoginController;
import com.supercat.controller.ProfileController;
import com.supercat.controller.SettingsController;
import com.supercat.controller.SplashController;
import com.supercat.controller.StudioSplashController;
import com.supercat.engine.LevelLoader;
import com.supercat.model.User;
import com.supercat.service.Settings;
import com.supercat.ui.Theme;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Gestionnaire de navigation entre les ecrans.
 *
 * Tous les ecrans sont concus a une resolution fixe (860 x 660) puis mis a
 * l'echelle de maniere uniforme pour remplir la fenetre, y compris en plein
 * ecran (technique du "letterbox" : l'image conserve ses proportions, des
 * bandes sombres comblent l'espace restant). C'est l'approche habituelle des
 * jeux 2D pour s'adapter a toutes les resolutions.
 */
public class SceneManager {

    private final Stage stage;
    private final Scene scene;
    private final Group screenGroup;
    private final Scale viewScale = new Scale();

    private Parent currentScreen;
    private User currentUser;

    public SceneManager(Stage stage, Scene scene) {
        this.stage = stage;
        this.scene = scene;
        this.screenGroup = new Group();

        StackPane frame = new StackPane(screenGroup);
        frame.setStyle("-fx-background-color: #241F30;");   // cadre sombre (letterbox)
        scene.setRoot(frame);

        DoubleBinding scaleFactor = Bindings.createDoubleBinding(() -> {
            double w = scene.getWidth();
            double h = scene.getHeight();
            if (w <= 0 || h <= 0) {
                return 1.0;
            }
            return Math.min(w / Theme.SCENE_WIDTH, h / Theme.SCENE_HEIGHT);
        }, scene.widthProperty(), scene.heightProperty());

        viewScale.setPivotX(0);
        viewScale.setPivotY(0);
        viewScale.xProperty().bind(scaleFactor);
        viewScale.yProperty().bind(scaleFactor);
    }

    // ----- Navigation entre ecrans -----

    /** Ecran-titre du studio (affiche en tout premier). */
    public void showStudioSplash() {
        clearKeyHandlers();
        setRoot(new StudioSplashController(this).getView());
    }

    /** Ecran-titre du jeu. */
    public void showSplash() {
        clearKeyHandlers();
        setRoot(new SplashController(this).getView());
    }

    public void showLogin() {
        clearKeyHandlers();
        setRoot(new LoginController(this).getView());
    }

    public void showHome() {
        clearKeyHandlers();
        setRoot(new HomeController(this).getView());
    }

    public void showCampaignLevel(int levelIndex) {
        setRoot(new GameController(this, levelIndex, false).getView());
    }

    public void showEndless() {
        setRoot(new GameController(this, LevelLoader.getCampaignCount(), true).getView());
    }

    public void showAdmin() {
        clearKeyHandlers();
        setRoot(new AdminController(this).getView());
    }

    public void showProfile() {
        clearKeyHandlers();
        setRoot(new ProfileController(this).getView());
    }

    public void showLeaderboard() {
        clearKeyHandlers();
        setRoot(new LeaderboardController(this).getView());
    }

    public void showSettings() {
        clearKeyHandlers();
        setRoot(new SettingsController(this).getView());
    }

    public void logout() {
        currentUser = null;
        showLogin();
    }

    // ----- Mecanique interne -----

    private void setRoot(Parent screen) {
        if (currentScreen != null) {
            currentScreen.getTransforms().remove(viewScale);
        }
        currentScreen = screen;
        screen.getTransforms().add(viewScale);
        screenGroup.getChildren().setAll(screen);

        if (Settings.isReducedMotion()) {
            screen.setOpacity(1);
            return;
        }
        // transition douce : fondu accompagne d'une legere mise a l'echelle
        screen.setOpacity(0);
        screen.setScaleX(0.985);
        screen.setScaleY(0.985);
        FadeTransition fade = new FadeTransition(Duration.millis(480), screen);
        fade.setFromValue(0);
        fade.setToValue(1);
        ScaleTransition scale = new ScaleTransition(Duration.millis(480), screen);
        scale.setFromX(0.985);
        scale.setFromY(0.985);
        scale.setToX(1.0);
        scale.setToY(1.0);
        ParallelTransition transition = new ParallelTransition(fade, scale);
        transition.setInterpolator(Interpolator.EASE_OUT);
        transition.play();
    }

    private void clearKeyHandlers() {
        scene.setOnKeyPressed(null);
        scene.setOnKeyReleased(null);
    }

    // ----- Accesseurs -----

    public Stage getStage() { return stage; }
    public Scene getScene() { return scene; }
    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User currentUser) { this.currentUser = currentUser; }
}
