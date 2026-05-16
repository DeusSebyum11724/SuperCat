package com.supercat;

import com.supercat.controller.AdminController;
import com.supercat.controller.CampaignController;
import com.supercat.controller.GameController;
import com.supercat.controller.HomeController;
import com.supercat.controller.IceGameController;
import com.supercat.controller.LeaderboardController;
import com.supercat.controller.LoginController;
import com.supercat.controller.MemoryGameController;
import com.supercat.controller.ProfileController;
import com.supercat.controller.SettingsController;
import com.supercat.controller.SplashController;
import com.supercat.controller.StoryController;
import com.supercat.controller.StudioSplashController;
import com.supercat.engine.LevelLoader;
import com.supercat.engine.Story;
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
 * l'echelle pour remplir la fenetre. Le cadre adopte le meme fond que
 * l'ecran affiche : l'image conserve ses proportions sans laisser
 * apparaitre de bandes visibles, meme en plein ecran.
 */
public class SceneManager {

    private static final String CALM_FRAME =
            "linear-gradient(to bottom, #F3EEE5 0%, #EBE4D6 100%)";
    private static final String COZY_FRAME =
            "linear-gradient(to bottom, #2A2440 0%, #3E3550 55%, #574752 100%)";

    private final Stage stage;
    private final Scene scene;
    private final StackPane frame;
    private final Group screenGroup;
    private final Scale viewScale = new Scale();

    private Parent currentScreen;
    private User currentUser;

    public SceneManager(Stage stage, Scene scene) {
        this.stage = stage;
        this.scene = scene;
        this.screenGroup = new Group();
        this.frame = new StackPane(screenGroup);
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

    public void showStudioSplash() {
        clearKeyHandlers();
        setRoot(new StudioSplashController(this).getView(), COZY_FRAME);
    }

    public void showSplash() {
        clearKeyHandlers();
        setRoot(new SplashController(this).getView(), CALM_FRAME);
    }

    public void showLogin() {
        clearKeyHandlers();
        setRoot(new LoginController(this).getView(), CALM_FRAME);
    }

    public void showHome() {
        clearKeyHandlers();
        setRoot(new HomeController(this).getView(), CALM_FRAME);
    }

    /** Affiche la ligne horizontale des niveaux de la campagne. */
    public void showCampaign() {
        clearKeyHandlers();
        setRoot(new CampaignController(this).getView(), CALM_FRAME);
    }

    public void showCampaignLevel(int levelIndex) {
        setRoot(new GameController(this, levelIndex).getView(), CALM_FRAME);
    }

    public void showEndless() {
        setRoot(new GameController(this, LevelLoader.getCampaignCount()).getView(), CALM_FRAME);
    }

    /** Affiche l'ecran narratif du chapitre courant du mode Histoire. */
    public void showStory() {
        clearKeyHandlers();
        setRoot(new StoryController(this).getView(), CALM_FRAME);
    }

    /** Lance le mini-jeu d'un chapitre du mode Histoire (selon son type). */
    public void showStoryLevel(int chapter) {
        clearKeyHandlers();
        Parent screen = switch (Story.game(chapter)) {
            case ICE -> new IceGameController(this, chapter).getView();
            case MEMORY -> new MemoryGameController(this, chapter).getView();
            case MAZE -> new GameController(this, LevelLoader.storyIndex(chapter)).getView();
        };
        setRoot(screen, CALM_FRAME);
    }

    public void showAdmin() {
        clearKeyHandlers();
        setRoot(new AdminController(this).getView(), CALM_FRAME);
    }

    public void showProfile() {
        clearKeyHandlers();
        setRoot(new ProfileController(this).getView(), CALM_FRAME);
    }

    public void showLeaderboard() {
        clearKeyHandlers();
        setRoot(new LeaderboardController(this).getView(), CALM_FRAME);
    }

    public void showSettings() {
        clearKeyHandlers();
        setRoot(new SettingsController(this).getView(), CALM_FRAME);
    }

    public void logout() {
        currentUser = null;
        showLogin();
    }

    // ----- Mecanique interne -----

    private void setRoot(Parent screen, String frameBackground) {
        frame.setStyle("-fx-background-color: " + frameBackground + ";");
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
