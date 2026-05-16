package com.supercat.controller;

import com.supercat.SceneManager;
import com.supercat.database.DatabaseManager;
import com.supercat.engine.GameEngine;
import com.supercat.engine.GameListener;
import com.supercat.engine.GameState;
import com.supercat.model.User;
import com.supercat.ui.Theme;
import com.supercat.ui.UIFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.HashSet;
import java.util.Set;

/**
 * Controleur de l'ecran de jeu (cas d'utilisation UC2 / UC3).
 *
 * Il cree le moteur de jeu, affiche la zone de jeu (canvas), le bandeau
 * d'informations (HUD : score, temps, poissons) et reagit aux evenements du
 * moteur via l'interface GameListener (fin de niveau, Game Over, victoire).
 *
 * Conforme au diagramme de classes : possede le moteur, l'ensemble des
 * touches actives, et expose handleKeyPressed/handleKeyReleased et
 * saveFinalScore().
 */
public class GameController implements GameListener {

    private final SceneManager sceneManager;
    private final Set<KeyCode> activeKeys = new HashSet<>();

    private GameEngine engine;
    private BorderPane root;
    private StackPane overlay;

    private final Label levelValue = new Label();
    private final Label scoreValue = new Label();
    private final Label fishValue = new Label();
    private final Label timeValue = new Label();

    public GameController(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
        initialize();
    }

    public Parent getView() {
        return root;
    }

    // =====================================================================
    //  Construction de l'ecran
    // =====================================================================
    private void initialize() {
        Canvas canvas = new Canvas(Theme.CANVAS_WIDTH, Theme.CANVAS_HEIGHT);
        engine = new GameEngine(canvas.getGraphicsContext2D(), activeKeys, this);

        overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.55);");
        overlay.setVisible(false);

        StackPane gameArea = new StackPane(canvas, overlay);
        gameArea.setMaxSize(Theme.CANVAS_WIDTH, Theme.CANVAS_HEIGHT);
        gameArea.setMinSize(Theme.CANVAS_WIDTH, Theme.CANVAS_HEIGHT);

        root = new BorderPane();
        root.setStyle(Theme.BG_GRADIENT);
        root.setTop(buildHud());
        root.setCenter(gameArea);
        BorderPane.setAlignment(gameArea, Pos.CENTER);

        // ecouteurs clavier installes au niveau de la scene
        Scene scene = sceneManager.getScene();
        scene.setOnKeyPressed(this::handleKeyPressed);
        scene.setOnKeyReleased(this::handleKeyReleased);

        engine.startNewGame();
        engine.start();
    }

    /** Construit le bandeau d'informations affiche au-dessus du jeu. */
    private HBox buildHud() {
        HBox hud = new HBox(20);
        hud.setAlignment(Pos.CENTER_LEFT);
        hud.setPadding(new Insets(12, 18, 12, 18));
        hud.setStyle("-fx-background-color: #243140;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button pauseBtn = UIFactory.secondaryButton("Pause");
        pauseBtn.setOnAction(e -> togglePause());
        Button quitBtn = UIFactory.dangerButton("Quitter");
        quitBtn.setOnAction(e -> exitToMenu());

        hud.getChildren().addAll(
                statBlock("NIVEAU", levelValue, 180),
                statBlock("SCORE", scoreValue, 80),
                statBlock("POISSONS", fishValue, 85),
                statBlock("TEMPS", timeValue, 70),
                spacer, pauseBtn, quitBtn);
        return hud;
    }

    private VBox statBlock(String caption, Label valueLabel, double minWidth) {
        Label cap = new Label(caption);
        cap.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #9FB0BF;");
        valueLabel.setStyle(valueStyle("white"));
        VBox box = new VBox(1, cap, valueLabel);
        box.setMinWidth(minWidth);
        return box;
    }

    private String valueStyle(String color) {
        return "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + color + ";";
    }

    // =====================================================================
    //  Gestion du clavier
    // =====================================================================
    public void handleKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();
        if (code == KeyCode.P || code == KeyCode.ESCAPE) {
            togglePause();
            return;
        }
        activeKeys.add(code);
    }

    public void handleKeyReleased(KeyEvent event) {
        activeKeys.remove(event.getCode());
    }

    // =====================================================================
    //  Evenements du moteur de jeu (interface GameListener)
    // =====================================================================
    @Override
    public void onTick() {
        levelValue.setText(engine.getLevelName()
                + "  (" + engine.getLevelNumber() + "/" + engine.getTotalLevels() + ")");
        scoreValue.setText(String.valueOf(engine.getScore()));

        int collected = engine.getFishCollected();
        int total = engine.getFishTotal();
        fishValue.setText(collected + " / " + total);
        fishValue.setStyle(valueStyle(collected >= total ? "#2ECC71" : "white"));

        int t = engine.getTimeLeft();
        timeValue.setText(String.format("%d:%02d", t / 60, t % 60));
        timeValue.setStyle(valueStyle(t <= 10 ? "#E74C3C" : "white"));
    }

    @Override
    public void onLevelComplete() {
        Button next = UIFactory.successButton("Niveau suivant");
        next.setOnAction(e -> {
            hideOverlay();
            engine.nextLevel();
        });
        Button menu = UIFactory.secondaryButton("Menu principal");
        menu.setOnAction(e -> exitToMenu());
        showOverlay("Niveau termine !",
                "Bravo ! Tu as termine le niveau " + engine.getLevelNumber() + ".\n"
                        + "Score actuel : " + engine.getScore() + " points.", next, menu);
    }

    @Override
    public void onGameOver() {
        String reason = engine.getTimeLeft() <= 0
                ? "Le temps est ecoule !"
                : "Un chien t'a attrape !";
        Button retry = UIFactory.primaryButton("Recommencer");
        retry.setOnAction(e -> {
            hideOverlay();
            engine.restart();
        });
        Button menu = UIFactory.secondaryButton("Menu principal");
        menu.setOnAction(e -> exitToMenu());
        showOverlay("Game Over",
                reason + "\n\nScore de la partie : " + engine.getScore()
                        + " points (non enregistre).", retry, menu);
    }

    @Override
    public void onGameWon() {
        boolean newRecord = saveFinalScore();
        Button replay = UIFactory.primaryButton("Rejouer");
        replay.setOnAction(e -> {
            hideOverlay();
            engine.restart();
        });
        Button menu = UIFactory.secondaryButton("Menu principal");
        menu.setOnAction(e -> exitToMenu());
        String detail = "Felicitations ! Tu as termine les " + engine.getTotalLevels()
                + " niveaux du labyrinthe.\nScore final : " + engine.getScore() + " points.";
        if (newRecord) {
            detail += "\n\n*** NOUVEAU RECORD PERSONNEL ! ***";
        }
        showOverlay("VICTOIRE !", detail, replay, menu);
    }

    /**
     * Enregistre le score final en base de donnees (UC4). Retourne true s'il
     * s'agit d'un nouveau record personnel. N'est appele qu'en cas de
     * victoire : un Game Over n'enregistre jamais le score (regle RM9).
     */
    public boolean saveFinalScore() {
        User user = sceneManager.getCurrentUser();
        if (user == null) {
            return false;
        }
        DatabaseManager db = DatabaseManager.getInstance();
        boolean newRecord = db.updateHighScore(user.getId(), engine.getScore());
        user.setHighScore(db.getHighScore(user.getId()));
        return newRecord;
    }

    // =====================================================================
    //  Pause et navigation
    // =====================================================================
    private void togglePause() {
        GameState state = engine.getState();
        if (state == GameState.PLAYING) {
            engine.pause();
            Button resume = UIFactory.primaryButton("Reprendre");
            resume.setOnAction(e -> resumeGame());
            Button quit = UIFactory.secondaryButton("Quitter vers le menu");
            quit.setOnAction(e -> exitToMenu());
            showOverlay("Pause", "La partie est en pause.\n"
                    + "Appuie sur P pour reprendre.", resume, quit);
        } else if (state == GameState.PAUSED) {
            resumeGame();
        }
    }

    private void resumeGame() {
        hideOverlay();
        engine.resume();
    }

    private void exitToMenu() {
        engine.stop();
        sceneManager.showMenu();
    }

    // =====================================================================
    //  Panneau superpose (fin de niveau, Game Over, victoire, pause)
    // =====================================================================
    private void showOverlay(String headingText, String detailText, Button... actions) {
        VBox box = UIFactory.card();
        box.setMaxWidth(400);
        box.setMaxHeight(Region.USE_PREF_SIZE);

        Label detail = UIFactory.body(detailText);
        detail.setWrapText(true);
        detail.setMaxWidth(330);
        detail.setStyle(detail.getStyle() + " -fx-font-size: 15px; -fx-text-alignment: center;");

        VBox buttons = new VBox(10, actions);
        buttons.setFillWidth(true);
        buttons.setAlignment(Pos.CENTER);
        for (Button action : actions) {
            action.setMaxWidth(Double.MAX_VALUE);
        }

        box.getChildren().setAll(UIFactory.catFace(68), UIFactory.heading(headingText),
                detail, buttons);
        overlay.getChildren().setAll(box);
        overlay.setVisible(true);
    }

    private void hideOverlay() {
        overlay.setVisible(false);
        overlay.getChildren().clear();
    }
}
