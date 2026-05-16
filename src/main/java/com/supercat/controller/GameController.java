package com.supercat.controller;

import com.supercat.SceneManager;
import com.supercat.database.DatabaseManager;
import com.supercat.engine.GameEngine;
import com.supercat.engine.GameListener;
import com.supercat.engine.GameState;
import com.supercat.engine.LevelLoader;
import com.supercat.engine.MusicPlayer;
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
 * Gere deux modes :
 *  - campagne : un niveau choisi sur la page d'accueil, score enregistre
 *    par niveau (rejouable pour ameliorer son score) ;
 *  - mode sans fin : des salles generees a l'infini, difficulte croissante,
 *    le score retenu est le nombre de salles franchies.
 */
public class GameController implements GameListener {

    private final SceneManager sceneManager;
    private final Set<KeyCode> activeKeys = new HashSet<>();

    private final boolean endless;
    private final int startIndex;
    private int currentIndex;
    private int endlessCleared = 0;
    private boolean endlessSaved = false;

    private GameEngine engine;
    private MusicPlayer music;
    private BorderPane root;
    private StackPane overlay;

    private final Label levelValue = new Label();
    private final Label difficultyValue = new Label();
    private final Label scoreValue = new Label();
    private final Label fishValue = new Label();
    private final Label timeValue = new Label();

    public GameController(SceneManager sceneManager, int levelIndex, boolean endless) {
        this.sceneManager = sceneManager;
        this.endless = endless;
        this.startIndex = levelIndex;
        this.currentIndex = levelIndex;
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
        music = new MusicPlayer();

        overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(62,56,82,0.62);");
        overlay.setVisible(false);

        StackPane gameArea = new StackPane(canvas, overlay);
        gameArea.setMaxSize(Theme.CANVAS_WIDTH, Theme.CANVAS_HEIGHT);
        gameArea.setMinSize(Theme.CANVAS_WIDTH, Theme.CANVAS_HEIGHT);

        root = new BorderPane();
        root.setStyle(Theme.BG_GRADIENT);
        root.setTop(buildHud());
        root.setCenter(gameArea);
        BorderPane.setAlignment(gameArea, Pos.CENTER);

        Scene scene = sceneManager.getScene();
        scene.setOnKeyPressed(this::handleKeyPressed);
        scene.setOnKeyReleased(this::handleKeyReleased);

        engine.playLevel(currentIndex);
        engine.start();
        music.start();
    }

    private HBox buildHud() {
        HBox hud = new HBox(14);
        hud.setAlignment(Pos.CENTER_LEFT);
        hud.setPadding(new Insets(11, 16, 11, 16));
        hud.setStyle("-fx-background-color: " + Theme.HUD_BG + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button musicBtn = hudButton("Son ON");
        musicBtn.setOnAction(e -> {
            music.setMuted(!music.isMuted());
            musicBtn.setText(music.isMuted() ? "Son OFF" : "Son ON");
        });
        Button pauseBtn = hudButton("Pause");
        pauseBtn.setOnAction(e -> togglePause());
        Button quitBtn = hudButton("Quitter");
        quitBtn.setOnAction(e -> exitToHome());

        hud.getChildren().addAll(
                statBlock("NIVEAU", levelValue, 150),
                statBlock("DIFFICULTE", difficultyValue, 92),
                statBlock("SCORE", scoreValue, 78),
                statBlock("POISSONS", fishValue, 86),
                statBlock("TEMPS", timeValue, 66),
                spacer, musicBtn, pauseBtn, quitBtn);
        return hud;
    }

    private VBox statBlock(String caption, Label valueLabel, double minWidth) {
        Label cap = new Label(caption);
        cap.setStyle("-fx-font-size: 9.5px; -fx-font-weight: bold; -fx-text-fill: #ABA2BC;");
        valueLabel.setStyle(valueStyle("white"));
        VBox box = new VBox(2, cap, valueLabel);
        box.setMinWidth(minWidth);
        return box;
    }

    private String valueStyle(String color) {
        return "-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + color + ";";
    }

    private Button hudButton(String text) {
        Button b = new Button(text);
        String base = "-fx-background-color: #564E6E; -fx-text-fill: white; -fx-cursor: hand; "
                + "-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-background-radius: 16; "
                + "-fx-padding: 7 14 7 14;";
        b.setStyle(base);
        b.setOnMouseEntered(e -> b.setStyle(base.replace("#564E6E", "#675E84")));
        b.setOnMouseExited(e -> b.setStyle(base));
        return b;
    }

    // =====================================================================
    //  Clavier
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
    //  Evenements du moteur (GameListener)
    // =====================================================================
    @Override
    public void onTick() {
        if (endless) {
            levelValue.setText(engine.getLevelName());
        } else {
            levelValue.setText(engine.getLevelName()
                    + "  (" + (currentIndex + 1) + "/" + LevelLoader.getCampaignCount() + ")");
        }
        difficultyValue.setText(engine.getLevelDifficulty());
        difficultyValue.setStyle(valueStyle(difficultyColor(engine.getLevelDifficulty())));
        scoreValue.setText(String.valueOf(engine.getScore()));

        int collected = engine.getFishCollected();
        int total = engine.getFishTotal();
        fishValue.setText(collected + " / " + total);
        fishValue.setStyle(valueStyle(collected >= total ? "#9BD0B4" : "white"));

        int t = engine.getTimeLeft();
        timeValue.setText(String.format("%d:%02d", t / 60, t % 60));
        timeValue.setStyle(valueStyle(t <= 10 ? "#E79A8E" : "white"));
    }

    @Override
    public void onLevelComplete() {
        if (endless) {
            endlessCleared++;
            Button cont = UIFactory.successButton("Continuer");
            cont.setOnAction(e -> advanceLevel());
            Button quit = UIFactory.secondaryButton("Quitter");
            quit.setOnAction(e -> exitToHome());
            showOverlay("Salle franchie !",
                    "Salles franchies dans cette partie : " + endlessCleared
                            + "\nLa difficulte continue d'augmenter...", cont, quit);
        } else {
            boolean record = saveCampaignScore();
            int best = currentBest();
            boolean hasNext = currentIndex + 1 < LevelLoader.getCampaignCount();

            Button replay = UIFactory.secondaryButton("Rejouer ce niveau");
            replay.setOnAction(e -> {
                hideOverlay();
                engine.restart();
            });
            Button home = UIFactory.secondaryButton("Page d'accueil");
            home.setOnAction(e -> exitToHome());

            String detail = "Score du niveau : " + engine.getScore() + " points\n"
                    + (record ? "Nouveau record pour ce niveau !"
                              : "Ton meilleur : " + best + " points");
            if (hasNext) {
                Button next = UIFactory.primaryButton("Niveau suivant");
                next.setOnAction(e -> advanceLevel());
                showOverlay("Niveau termine !", detail, next, replay, home);
            } else {
                showOverlay("Campagne terminee !",
                        detail + "\n\nTu as termine les "
                                + LevelLoader.getCampaignCount() + " niveaux. Bravo !",
                        replay, home);
            }
        }
    }

    @Override
    public void onGameOver() {
        String reason = engine.getTimeLeft() <= 0
                ? "Le temps est ecoule !"
                : "Un chien t'a attrape !";
        if (endless) {
            finishEndlessRun();
            Button retry = UIFactory.primaryButton("Recommencer");
            retry.setOnAction(e -> restartEndless());
            Button home = UIFactory.secondaryButton("Page d'accueil");
            home.setOnAction(e -> exitToHome());
            showOverlay("Game Over",
                    reason + "\n\nTu as franchi " + endlessCleared + " salle(s) sans fin.",
                    retry, home);
        } else {
            Button retry = UIFactory.primaryButton("Reessayer");
            retry.setOnAction(e -> {
                hideOverlay();
                engine.restart();
            });
            Button home = UIFactory.secondaryButton("Page d'accueil");
            home.setOnAction(e -> exitToHome());
            showOverlay("Game Over",
                    reason + "\n\nScore non enregistre (regle RM9).", retry, home);
        }
    }

    // =====================================================================
    //  Enregistrement des scores
    // =====================================================================
    private boolean saveCampaignScore() {
        User user = sceneManager.getCurrentUser();
        if (user == null) {
            return false;
        }
        DatabaseManager db = DatabaseManager.getInstance();
        boolean record = db.saveLevelScore(user.getUsername(), currentIndex, engine.getScore());
        user.setHighScore(db.getTotalScore(user.getUsername()));
        return record;
    }

    private int currentBest() {
        User user = sceneManager.getCurrentUser();
        if (user == null) {
            return 0;
        }
        return DatabaseManager.getInstance().getLevelBest(user.getUsername(), currentIndex);
    }

    /** Enregistre une fois le resultat du mode sans fin (salles franchies). */
    private void finishEndlessRun() {
        if (endlessSaved || !endless || endlessCleared <= 0) {
            return;
        }
        endlessSaved = true;
        User user = sceneManager.getCurrentUser();
        if (user != null) {
            DatabaseManager.getInstance().saveEndlessResult(user.getUsername(), endlessCleared);
        }
    }

    // =====================================================================
    //  Navigation
    // =====================================================================
    private void advanceLevel() {
        hideOverlay();
        currentIndex++;
        engine.playLevel(currentIndex);
    }

    private void restartEndless() {
        hideOverlay();
        currentIndex = startIndex;
        endlessCleared = 0;
        endlessSaved = false;
        engine.playLevel(currentIndex);
    }

    private void exitToHome() {
        finishEndlessRun();
        engine.stop();
        music.stop();
        sceneManager.showHome();
    }

    private void togglePause() {
        GameState state = engine.getState();
        if (state == GameState.PLAYING) {
            engine.pause();
            Button resume = UIFactory.primaryButton("Reprendre");
            resume.setOnAction(e -> resumeGame());
            Button quit = UIFactory.secondaryButton("Quitter");
            quit.setOnAction(e -> exitToHome());
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

    private String difficultyColor(String difficulty) {
        return switch (difficulty) {
            case "Facile" -> "#9BD0B4";
            case "Moyen" -> "#A6C3D6";
            case "Difficile" -> "#EAC98A";
            case "Expert" -> "#EFAE99";
            default -> "#E79A8E";
        };
    }

    // =====================================================================
    //  Panneau superpose
    // =====================================================================
    private void showOverlay(String headingText, String detailText, Button... actions) {
        VBox box = UIFactory.card();
        box.setMaxWidth(410);
        box.setMaxHeight(Region.USE_PREF_SIZE);

        Label detail = UIFactory.body(detailText);
        detail.setWrapText(true);
        detail.setMaxWidth(340);
        detail.setStyle(detail.getStyle() + " -fx-font-size: 15px; -fx-text-alignment: center;");

        VBox buttons = new VBox(10, actions);
        buttons.setFillWidth(true);
        buttons.setAlignment(Pos.CENTER);
        for (Button action : actions) {
            action.setMaxWidth(Double.MAX_VALUE);
        }

        box.getChildren().setAll(UIFactory.catFace(66), UIFactory.heading(headingText),
                detail, buttons);
        overlay.getChildren().setAll(box);
        overlay.setVisible(true);
    }

    private void hideOverlay() {
        overlay.setVisible(false);
        overlay.getChildren().clear();
    }
}
