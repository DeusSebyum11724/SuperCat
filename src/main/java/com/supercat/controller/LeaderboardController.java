package com.supercat.controller;

import com.supercat.SceneManager;
import com.supercat.database.DatabaseManager;
import com.supercat.model.ScoreEntry;
import com.supercat.model.User;
import com.supercat.ui.Theme;
import com.supercat.ui.UIFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Controleur de l'ecran du classement mondial (cas d'utilisation UC5,
 * regle metier RM8). Affiche les meilleurs scores enregistres en base.
 */
public class LeaderboardController {

    private final SceneManager sceneManager;
    private final DatabaseManager db = DatabaseManager.getInstance();

    public LeaderboardController(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    public Parent getView() {
        StackPane root = UIFactory.screen();
        VBox card = UIFactory.card();
        card.setMaxWidth(540);
        card.setMaxHeight(Region.USE_PREF_SIZE);

        VBox header = new VBox(3,
                UIFactory.heading("Classement mondial"),
                UIFactory.subtitle("Les meilleurs chasseurs de poissons d'or"));
        header.setAlignment(Pos.CENTER);

        List<ScoreEntry> entries = db.getLeaderboard();
        User current = sceneManager.getCurrentUser();
        String currentName = (current != null) ? current.getUsername() : "";

        VBox list = new VBox(7);
        list.setFillWidth(true);
        if (entries.isEmpty()) {
            Label empty = UIFactory.body(
                    "Aucun score enregistre pour le moment.\nSois le premier a marquer des points !");
            empty.setWrapText(true);
            empty.setStyle(empty.getStyle() + " -fx-text-alignment: center;");
            list.setAlignment(Pos.CENTER);
            list.getChildren().add(empty);
        } else {
            for (ScoreEntry entry : entries) {
                list.getChildren().add(buildRow(entry, entry.username().equals(currentName)));
            }
        }

        ScrollPane scroll = new ScrollPane(list);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(350);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        Button back = UIFactory.secondaryButton("Retour au menu");
        back.setMaxWidth(Double.MAX_VALUE);
        back.setOnAction(e -> sceneManager.showMenu());

        VBox content = new VBox(14, header, scroll, back);
        content.setFillWidth(true);
        card.getChildren().add(content);
        root.getChildren().add(card);
        return root;
    }

    /** Construit une ligne du classement (rang, pseudo, score). */
    private HBox buildRow(ScoreEntry entry, boolean isCurrentUser) {
        Label rank = new Label(String.valueOf(entry.rank()));
        rank.setMinSize(34, 34);
        rank.setAlignment(Pos.CENTER);
        String badge = switch (entry.rank()) {
            case 1 -> "#F1C40F";   // or
            case 2 -> "#AEB6BF";   // argent
            case 3 -> "#CD7F32";   // bronze
            default -> "#CBD3D8";
        };
        rank.setStyle("-fx-background-radius: 17; -fx-background-color: " + badge + "; "
                + "-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");

        Label name = new Label(entry.username() + (isCurrentUser ? "   (toi)" : ""));
        name.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + Theme.TEXT_DARK + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label score = new Label(entry.score() + " pts");
        score.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + Theme.ACCENT + ";");

        HBox row = new HBox(12, rank, name, spacer, score);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 14, 8, 8));
        row.setStyle("-fx-background-color: " + (isCurrentUser ? "#FFE4C4" : "#F4F6F7")
                + "; -fx-background-radius: 10;");
        return row;
    }
}
