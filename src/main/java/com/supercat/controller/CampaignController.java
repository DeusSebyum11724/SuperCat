package com.supercat.controller;

import com.supercat.SceneManager;
import com.supercat.database.DatabaseManager;
import com.supercat.engine.LevelLoader;
import com.supercat.model.User;
import com.supercat.ui.Theme;
import com.supercat.ui.UIFactory;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
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
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;

import java.util.Map;

/**
 * Ecran de la campagne.
 *
 * Les douze niveaux forment une ligne horizontale de stations (esthetique
 * "Mini Metro") parcourue sans barre de defilement, a l'aide de deux fleches.
 * Un niveau s'ouvre lorsque le precedent est termine ; un niveau deja termine
 * peut etre rejoue pour ameliorer son score.
 */
public class CampaignController {

    private static final double CARD_WIDTH = 112;

    private final SceneManager sceneManager;
    private final DatabaseManager db = DatabaseManager.getInstance();
    private ScrollPane routeScroll;

    public CampaignController(SceneManager sceneManager) {
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
        int campaignCount = LevelLoader.getCampaignCount();

        int current = campaignCount - 1;
        for (int i = 0; i < campaignCount; i++) {
            boolean done = bests.containsKey(i);
            boolean unlocked = (i == 0) || bests.containsKey(i - 1);
            if (unlocked && !done) {
                current = i;
                break;
            }
        }

        StackPane root = UIFactory.screen();
        VBox card = UIFactory.card();
        card.setMaxWidth(740);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.setSpacing(18);
        card.setPadding(new Insets(26, 30, 24, 30));

        // --- en-tete ---
        Label heading = UIFactory.heading("La Campagne");
        Label sub = new Label("Douze labyrinthes, de plus en plus retors");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: " + Theme.TEXT_MUTED + ";");
        VBox header = new VBox(2, heading, sub);
        header.setAlignment(Pos.CENTER);

        // --- statistiques ---
        HBox stats = new HBox(10,
                statChip(completed + " / " + campaignCount, "NIVEAUX"),
                statChip(String.valueOf(totalScore), "SCORE TOTAL"),
                statChip(String.valueOf(endlessBest), "SANS FIN"));
        stats.setAlignment(Pos.CENTER);

        // --- ligne horizontale des niveaux ---
        Label routeLabel = new Label("PROGRESSION");
        routeLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: 600; -fx-text-fill: "
                + Theme.TEXT_MUTED + ";");
        HBox routeRow = buildRoute(bests, current, campaignCount);
        VBox routeSection = new VBox(6, routeLabel, routeRow);
        routeSection.setAlignment(Pos.CENTER_LEFT);

        // --- pied de page ---
        Button home = UIFactory.secondaryButton("Retour a l'accueil");
        home.setOnAction(e -> sceneManager.showHome());
        HBox footer = new HBox(home);
        footer.setAlignment(Pos.CENTER);

        card.getChildren().setAll(header, stats, routeSection, footer);
        root.getChildren().add(card);

        UIFactory.fadeInUp(header, 60);
        UIFactory.fadeInUp(stats, 160);
        UIFactory.fadeInUp(routeSection, 260);
        UIFactory.fadeInUp(footer, 380);

        if (campaignCount > 1) {
            routeScroll.setHvalue((double) current / (campaignCount - 1));
        }
        return root;
    }

    // ----- Ligne horizontale des niveaux -----
    private HBox buildRoute(Map<Integer, Integer> bests, int current, int campaignCount) {
        HBox route = new HBox(0);
        route.setAlignment(Pos.TOP_CENTER);
        for (int i = 0; i < campaignCount; i++) {
            boolean done = bests.containsKey(i);
            boolean unlocked = (i == 0) || bests.containsKey(i - 1);
            int best = done ? bests.get(i) : 0;
            route.getChildren().add(levelCard(i, unlocked, done, best, i == current));
        }

        routeScroll = new ScrollPane(route);
        routeScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        routeScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        routeScroll.setFitToHeight(true);
        routeScroll.setPrefViewportWidth(560);
        routeScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        routeScroll.setOnScroll(e -> routeScroll.setHvalue(
                routeScroll.getHvalue() - e.getDeltaY() / 1400.0));
        HBox.setHgrow(routeScroll, Priority.ALWAYS);

        Button left = arrowButton(true);
        left.setOnAction(e -> animateScroll(routeScroll.getHvalue() - 0.32));
        Button right = arrowButton(false);
        right.setOnAction(e -> animateScroll(routeScroll.getHvalue() + 0.32));

        HBox row = new HBox(6, left, routeScroll, right);
        row.setAlignment(Pos.CENTER);
        return row;
    }

    private void animateScroll(double target) {
        double clamped = Math.max(0, Math.min(1, target));
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(260),
                new KeyValue(routeScroll.hvalueProperty(), clamped, Interpolator.EASE_BOTH)));
        timeline.play();
    }

    /** Carte verticale d'un niveau de campagne (station de la ligne). */
    private VBox levelCard(int index, boolean unlocked, boolean done, int best, boolean current) {
        Circle circle = new Circle(15);
        if (done) {
            circle.setFill(Color.web(Theme.ACCENT));
        } else if (unlocked) {
            circle.setFill(Color.web("#FCFAF5"));
            circle.setStroke(Color.web(Theme.ACCENT));
            circle.setStrokeWidth(2.4);
        } else {
            circle.setFill(Color.web(Theme.LOCKED));
        }
        Label number = new Label(String.valueOf(index + 1));
        number.setStyle("-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: "
                + (unlocked && !done ? Theme.ACCENT : "white") + ";");
        StackPane node = new StackPane(circle, number);
        if (current) {
            UIFactory.breathe(node, 1.14, 1100);
        }

        Region line = new Region();
        line.setMinHeight(4);
        line.setMaxHeight(4);
        line.setMaxWidth(Double.MAX_VALUE);
        line.setStyle("-fx-background-color: #DCD2C4;");
        StackPane nodeArea = new StackPane(line, node);
        nodeArea.setMinHeight(34);

        Label name = new Label(LevelLoader.getLevelName(index));
        name.setWrapText(true);
        name.setMaxWidth(98);
        name.setMinHeight(28);
        name.setStyle("-fx-font-size: 10.5px; -fx-font-weight: 600; -fx-text-alignment: center; "
                + "-fx-text-fill: " + (unlocked ? Theme.TEXT_DARK : Theme.TEXT_MUTED) + ";");

        Label score = new Label(done ? best + " pts" : (unlocked ? "a jouer" : "verrouille"));
        score.setStyle("-fx-font-size: 10px; -fx-text-fill: "
                + (done ? Theme.ACCENT : Theme.TEXT_MUTED) + ";");

        VBox cardBox = new VBox(4, nodeArea, name, score);
        cardBox.setAlignment(Pos.TOP_CENTER);
        cardBox.setMinWidth(CARD_WIDTH);
        cardBox.setPrefWidth(CARD_WIDTH);
        cardBox.setMaxWidth(CARD_WIDTH);
        cardBox.setPadding(new Insets(6, 2, 8, 2));

        if (unlocked) {
            String idle = "-fx-background-color: transparent; -fx-cursor: hand;";
            String hover = "-fx-background-color: rgba(219,139,107,0.12); "
                    + "-fx-background-radius: 12; -fx-cursor: hand;";
            cardBox.setStyle(idle);
            cardBox.setOnMouseEntered(e -> cardBox.setStyle(hover));
            cardBox.setOnMouseExited(e -> cardBox.setStyle(idle));
            cardBox.setOnMouseClicked(e -> sceneManager.showCampaignLevel(index));
        } else {
            cardBox.setOpacity(0.5);
        }
        return cardBox;
    }

    // ----- Petits composants -----
    private VBox statChip(String value, String caption) {
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: "
                + Theme.ACCENT + ";");
        Label captionLabel = new Label(caption);
        captionLabel.setStyle("-fx-font-size: 9.5px; -fx-font-weight: 600; -fx-text-fill: "
                + Theme.TEXT_MUTED + ";");
        VBox chip = new VBox(2, valueLabel, captionLabel);
        chip.setAlignment(Pos.CENTER);
        chip.setMinWidth(150);
        chip.setStyle("-fx-background-color: #F2ECE1; -fx-background-radius: 14; "
                + "-fx-padding: 10 14 10 14;");
        return chip;
    }

    private Button arrowButton(boolean left) {
        Button button = new Button();
        Canvas canvas = new Canvas(20, 20);
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setStroke(Color.web(Theme.TEXT_DARK));
        g.setLineWidth(2.2);
        g.setLineCap(StrokeLineCap.ROUND);
        if (left) {
            g.strokeLine(13, 4, 7, 10);
            g.strokeLine(7, 10, 13, 16);
        } else {
            g.strokeLine(7, 4, 13, 10);
            g.strokeLine(13, 10, 7, 16);
        }
        button.setGraphic(canvas);
        button.setMinSize(40, 40);
        button.setMaxSize(40, 40);
        String idle = "-fx-background-color: #F2ECE1; -fx-background-radius: 20; -fx-cursor: hand;";
        String hover = "-fx-background-color: #E6DECF; -fx-background-radius: 20; -fx-cursor: hand;";
        button.setStyle(idle);
        button.setOnMouseEntered(e -> button.setStyle(hover));
        button.setOnMouseExited(e -> button.setStyle(idle));
        return button;
    }
}
