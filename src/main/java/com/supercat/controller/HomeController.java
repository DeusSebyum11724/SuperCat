package com.supercat.controller;

import com.supercat.SceneManager;
import com.supercat.database.DatabaseManager;
import com.supercat.engine.LevelLoader;
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
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.Map;

/**
 * Page d'accueil du joueur : selection des niveaux.
 *
 * Inspiree de "Mini Metro", elle presente les niveaux comme une ligne de
 * stations reliees : chaque niveau affiche sa difficulte et le meilleur
 * score du joueur. Les niveaux termines peuvent etre rejoues pour ameliorer
 * son score. Le mode sans fin se debloque apres 3 niveaux termines.
 */
public class HomeController {

    private final SceneManager sceneManager;
    private final DatabaseManager db = DatabaseManager.getInstance();

    public HomeController(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    public Parent getView() {
        User user = sceneManager.getCurrentUser();
        String username = (user != null) ? user.getUsername() : "joueur";

        Map<Integer, Integer> bests = db.getLevelBests(username);
        int endlessBest = db.getEndlessBest(username);
        int completed = bests.size();
        int totalScore = 0;
        for (int value : bests.values()) {
            totalScore += value;
        }
        boolean endlessUnlocked = completed >= 3;
        int campaignCount = LevelLoader.getCampaignCount();

        StackPane root = UIFactory.screen();
        VBox card = UIFactory.card();
        card.setMaxWidth(660);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.setPadding(new Insets(26, 32, 24, 32));

        // --- en-tete ---
        VBox header = new VBox(2, UIFactory.title("SuperCat"),
                UIFactory.subtitle("Bonjour " + username + " — choisis ton aventure"));
        header.setAlignment(Pos.CENTER);

        // --- bandeau de statistiques ---
        HBox stats = new HBox(10,
                statChip(completed + " / " + campaignCount, "NIVEAUX TERMINES"),
                statChip(totalScore + " pts", "SCORE TOTAL"),
                statChip(endlessBest + " salles", "RECORD SANS FIN"));
        stats.setAlignment(Pos.CENTER);

        // --- ligne des niveaux (style "Mini Metro") ---
        VBox route = new VBox(0);
        for (int i = 0; i < campaignCount; i++) {
            boolean done = bests.containsKey(i);
            boolean unlocked = (i == 0) || bests.containsKey(i - 1);
            int best = done ? bests.get(i) : 0;
            route.getChildren().add(levelStation(i, unlocked, done, best));
        }
        route.getChildren().add(endlessStation(endlessUnlocked, endlessBest));

        ScrollPane scroll = new ScrollPane(route);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(322);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        // --- pied de page ---
        Button profile = UIFactory.secondaryButton("Mon profil");
        profile.setOnAction(e -> sceneManager.showProfile());
        Button leaderboard = UIFactory.secondaryButton("Classement");
        leaderboard.setOnAction(e -> sceneManager.showLeaderboard());
        Button logout = UIFactory.secondaryButton("Deconnexion");
        logout.setOnAction(e -> sceneManager.logout());
        HBox footer = new HBox(10, profile, leaderboard, logout);
        footer.setAlignment(Pos.CENTER);

        card.getChildren().setAll(header, stats, scroll, footer);
        root.getChildren().add(card);
        return root;
    }

    // ----- Bandeau de statistiques -----
    private VBox statChip(String value, String caption) {
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: "
                + Theme.ACCENT + ";");
        Label captionLabel = new Label(caption);
        captionLabel.setStyle("-fx-font-size: 9.5px; -fx-font-weight: bold; -fx-text-fill: "
                + Theme.TEXT_MUTED + ";");
        VBox chip = new VBox(1, valueLabel, captionLabel);
        chip.setAlignment(Pos.CENTER);
        chip.setMinWidth(170);
        chip.setStyle("-fx-background-color: #F2ECE1; -fx-background-radius: 14; "
                + "-fx-padding: 10 14 10 14;");
        return chip;
    }

    // ----- Station d'un niveau de campagne -----
    private HBox levelStation(int index, boolean unlocked, boolean done, int best) {
        StackPane node = circleNode(String.valueOf(index + 1), unlocked, done);

        Label name = new Label((index + 1) + ".  " + LevelLoader.getLevelName(index));
        name.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + Theme.TEXT_DARK + ";");
        String difficulty = LevelLoader.getDifficultyLabel(index);
        Label diffTag = UIFactory.tag(difficulty, difficultyColor(difficulty), "white");
        HBox titleRow = new HBox(8, name, diffTag);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        String statusText = done ? ("Meilleur score : " + best + " pts  -  clic pour rejouer")
                : unlocked ? "Nouveau niveau - clic pour jouer"
                : "Verrouille - termine le niveau precedent";
        Label status = new Label(statusText);
        status.setStyle("-fx-font-size: 12px; -fx-text-fill: " + Theme.TEXT_MUTED + ";");

        VBox info = new VBox(3, titleRow, status);
        HBox.setHgrow(info, Priority.ALWAYS);

        HBox station = new HBox(12, node, info);
        station.setAlignment(Pos.CENTER_LEFT);
        station.setPadding(new Insets(0, 14, 0, 4));
        attachInteraction(station, unlocked, () -> sceneManager.showCampaignLevel(index));
        return station;
    }

    // ----- Station du mode sans fin -----
    private HBox endlessStation(boolean unlocked, int best) {
        StackPane node = squareNode(unlocked);

        Label name = new Label("Mode sans fin");
        name.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + Theme.TEXT_DARK + ";");
        Label tag = UIFactory.tag("INFINI", Theme.GOLD, "white");
        HBox titleRow = new HBox(8, name, tag);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label status = new Label(unlocked
                ? ("Record : " + best + " salle(s) franchie(s)  -  difficulte infinie")
                : "Verrouille - termine 3 niveaux pour le debloquer");
        status.setStyle("-fx-font-size: 12px; -fx-text-fill: " + Theme.TEXT_MUTED + ";");

        VBox info = new VBox(3, titleRow, status);
        HBox.setHgrow(info, Priority.ALWAYS);

        HBox station = new HBox(12, node, info);
        station.setAlignment(Pos.CENTER_LEFT);
        station.setPadding(new Insets(0, 14, 0, 4));
        attachInteraction(station, unlocked, sceneManager::showEndless);
        return station;
    }

    // ----- Noeuds geometriques de la "ligne" -----
    private StackPane circleNode(String text, boolean unlocked, boolean done) {
        Circle circle = new Circle(17);
        if (done) {
            circle.setFill(Color.web(Theme.ACCENT));
        } else if (unlocked) {
            circle.setFill(Color.web("#FBF7F0"));
            circle.setStroke(Color.web(Theme.ACCENT));
            circle.setStrokeWidth(2.5);
        } else {
            circle.setFill(Color.web(Theme.LOCKED));
        }
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: "
                + (unlocked && !done ? Theme.ACCENT : "white") + ";");
        return assembleNode(circle, label);
    }

    private StackPane squareNode(boolean unlocked) {
        Region square = new Region();
        square.setMinSize(32, 32);
        square.setMaxSize(32, 32);
        square.setStyle("-fx-background-radius: 9; -fx-background-color: "
                + (unlocked ? Theme.GOLD : Theme.LOCKED) + ";");
        return assembleNode(square, null);
    }

    /** Assemble un noeud (forme + libelle) sur le segment de "ligne". */
    private StackPane assembleNode(javafx.scene.Node shape, Label label) {
        Region line = new Region();
        line.setMinWidth(4);
        line.setMaxWidth(4);
        line.setMinHeight(66);
        line.setMaxHeight(Double.MAX_VALUE);
        line.setStyle("-fx-background-color: #D9CFC2;");

        StackPane node = new StackPane();
        node.setMinWidth(52);
        node.setPrefWidth(52);
        node.getChildren().add(line);
        node.getChildren().add(shape);
        if (label != null) {
            node.getChildren().add(label);
        }
        return node;
    }

    /** Ajoute survol et clic a une station si le niveau est deverrouille. */
    private void attachInteraction(HBox station, boolean unlocked, Runnable onClick) {
        if (unlocked) {
            String idle = "-fx-background-color: transparent;";
            String hover = "-fx-background-color: rgba(224,138,111,0.13);";
            station.setStyle(idle);
            station.setOnMouseEntered(e -> station.setStyle(hover));
            station.setOnMouseExited(e -> station.setStyle(idle));
            station.setOnMouseClicked(e -> onClick.run());
            station.setStyle(idle);
        } else {
            station.setOpacity(0.5);
        }
    }

    private String difficultyColor(String difficulty) {
        return switch (difficulty) {
            case "Facile" -> Theme.SUCCESS;
            case "Moyen" -> Theme.SECONDARY;
            case "Difficile" -> Theme.GOLD;
            case "Expert" -> Theme.ACCENT;
            default -> Theme.DANGER;
        };
    }
}
