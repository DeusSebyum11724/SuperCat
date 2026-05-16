package com.supercat;

import com.supercat.controller.AdminController;
import com.supercat.controller.GameController;
import com.supercat.controller.LeaderboardController;
import com.supercat.controller.LoginController;
import com.supercat.controller.MenuController;
import com.supercat.controller.ProfileController;
import com.supercat.model.User;
import javafx.animation.FadeTransition;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Gestionnaire de navigation entre les ecrans. Il conserve la fenetre, la
 * scene et l'utilisateur connecte (session courante), et assure la
 * transition en fondu d'un ecran a l'autre.
 */
public class SceneManager {

    private final Stage stage;
    private final Scene scene;
    private User currentUser;

    public SceneManager(Stage stage, Scene scene) {
        this.stage = stage;
        this.scene = scene;
    }

    // ----- Navigation entre ecrans -----

    public void showLogin() {
        clearKeyHandlers();
        setRoot(new LoginController(this).getView());
    }

    public void showMenu() {
        clearKeyHandlers();
        setRoot(new MenuController(this).getView());
    }

    public void showGame() {
        // le GameController installe lui-meme les ecouteurs clavier
        setRoot(new GameController(this).getView());
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

    /** Deconnecte l'utilisateur et revient a l'ecran de connexion. */
    public void logout() {
        currentUser = null;
        showLogin();
    }

    // ----- Mecanique interne -----

    private void setRoot(Parent root) {
        scene.setRoot(root);
        // transition en fondu pour une apparition douce de chaque ecran
        FadeTransition fade = new FadeTransition(Duration.millis(280), root);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
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
