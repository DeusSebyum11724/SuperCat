package com.supercat.controller;

import com.supercat.SceneManager;
import com.supercat.model.User;
import com.supercat.ui.UIFactory;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Controleur du menu principal du joueur. Donne acces au jeu, au profil,
 * au classement et a la deconnexion.
 */
public class MenuController {

    private final SceneManager sceneManager;

    public MenuController(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    public Parent getView() {
        User user = sceneManager.getCurrentUser();
        String name = (user != null) ? user.getUsername() : "joueur";
        int highScore = (user != null) ? user.getHighScore() : 0;

        StackPane root = UIFactory.screen();
        VBox card = UIFactory.card();
        card.setMaxWidth(470);
        card.setMaxHeight(Region.USE_PREF_SIZE);

        VBox header = new VBox(3,
                UIFactory.heading("Bonjour, " + name + " !"),
                UIFactory.subtitle("Ton meilleur score : " + highScore + " points"));
        header.setAlignment(Pos.CENTER);

        // encadre des regles du jeu
        Label rules = UIFactory.body(
                "Deplace le chat avec les fleches du clavier (ou ZQSD).\n"
                + "Attrape tous les poissons d'or pour ouvrir la sortie,\n"
                + "evite les chiens et atteins la sortie avant la fin du temps !");
        rules.setWrapText(true);
        rules.setStyle(rules.getStyle() + " -fx-text-alignment: center;");
        VBox rulesBox = new VBox(rules);
        rulesBox.setAlignment(Pos.CENTER);
        rulesBox.setStyle("-fx-background-color: #FFF4E2; -fx-background-radius: 12; -fx-padding: 15;");

        Button play = UIFactory.primaryButton("JOUER");
        Button profile = UIFactory.secondaryButton("Mon profil");
        Button leaderboard = UIFactory.secondaryButton("Classement mondial");
        Button logout = UIFactory.secondaryButton("Se deconnecter");
        for (Button b : new Button[]{play, profile, leaderboard, logout}) {
            b.setMaxWidth(Double.MAX_VALUE);
        }
        play.setOnAction(e -> sceneManager.showGame());
        profile.setOnAction(e -> sceneManager.showProfile());
        leaderboard.setOnAction(e -> sceneManager.showLeaderboard());
        logout.setOnAction(e -> sceneManager.logout());

        VBox buttons = new VBox(10, play, profile, leaderboard, logout);
        buttons.setFillWidth(true);

        card.getChildren().addAll(UIFactory.catFace(88), UIFactory.title("SuperCat"),
                header, rulesBox, buttons);
        root.getChildren().add(card);
        return root;
    }
}
