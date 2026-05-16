package com.supercat.controller;

import com.supercat.SceneManager;
import com.supercat.database.DatabaseManager;
import com.supercat.engine.GameEngine;
import com.supercat.engine.GameListener;
import com.supercat.engine.GameState;
import com.supercat.engine.LevelLoader;
import com.supercat.engine.MusicPlayer;
import com.supercat.model.User;
import com.supercat.service.Settings;
import com.supercat.ui.Theme;
import com.supercat.ui.UIFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
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
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;

import java.util.HashSet;
import java.util.Set;

/**
 * Controleur de l'ecran de jeu (cas d'utilisation UC2 / UC3).
 *
 * Gere la campagne (un niveau choisi, score enregistre par niveau) et le
 * mode sans fin. Le bandeau d'informations est volontairement leger,
 * clair et discret : le labyrinthe reste l'element principal a l'ecran.
 */
public class GameController implements GameListener {

    private static final int MAX_PIPS = 18;
    private static final double TIMER_WIDTH = 112;

    private final SceneManager sceneManager;
    private final Set<KeyCode> activeKeys = new HashSet<>();

    private final boolean story;
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
    private final Label levelCaption = new Label();
    private final Label difficultyTag = new Label();
    private final Label scoreValue = new Label();
    private final Region[] fishPips = new Region[MAX_PIPS];
    private final Region timerFill = new Region();
    private final Label timeValue = new Label();
    private int lastCollected = -1;
    private int lastTotal = -1;

    public GameController(SceneManager sceneManager, int levelIndex) {
        this.sceneManager = sceneManager;
        this.startIndex = levelIndex;
        this.currentIndex = levelIndex;
        this.story = levelIndex >= LevelLoader.STORY_BASE;
        this.endless = !story && levelIndex >= LevelLoader.getCampaignCount();
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
        overlay.setStyle("-fx-background-color: rgba(48,44,54,0.46);");
        overlay.setVisible(false);

        StackPane gameArea = new StackPane(canvas, overlay);
        gameArea.setMaxSize(Theme.CANVAS_WIDTH, Theme.CANVAS_HEIGHT);
        gameArea.setMinSize(Theme.CANVAS_WIDTH, Theme.CANVAS_HEIGHT);

        root = new BorderPane();
        root.setStyle(Theme.BG_GRADIENT);
        root.setMinSize(Theme.SCENE_WIDTH, Theme.SCENE_HEIGHT);
        root.setPrefSize(Theme.SCENE_WIDTH, Theme.SCENE_HEIGHT);
        root.setMaxSize(Theme.SCENE_WIDTH, Theme.SCENE_HEIGHT);
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

    /** Bandeau d'informations leger affiche au-dessus du labyrinthe. */
    private HBox buildHud() {
        HBox hud = new HBox(14);
        hud.setAlignment(Pos.CENTER_LEFT);
        hud.setPadding(new Insets(9, 16, 9, 12));
        hud.setStyle("-fx-background-color: " + Theme.HUD_BG + "; "
                + "-fx-effect: dropshadow(gaussian, rgba(63,59,66,0.12), 9, 0, 0, 3);");

        Button pauseBtn = iconButton("pause", this::togglePause);
        Button soundBtn = iconButton(Settings.isMusicEnabled() ? "sound" : "muted", null);
        soundBtn.setOnAction(e -> {
            boolean enabled = !Settings.isMusicEnabled();
            Settings.setMusicEnabled(enabled);
            music.setMuted(!enabled);
            soundBtn.setGraphic(icon(enabled ? "sound" : "muted"));
        });
        Button quitBtn = iconButton("home", this::exitToHome);

        levelValue.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: "
                + Theme.TEXT_DARK + ";");
        levelCaption.setStyle("-fx-font-size: 8.5px; -fx-font-weight: 600; -fx-text-fill: "
                + Theme.TEXT_MUTED + ";");
        VBox levelBlock = new VBox(1, levelCaption, levelValue);
        levelBlock.setMinWidth(150);

        scoreValue.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: "
                + Theme.TEXT_DARK + ";");
        VBox difficultyBlock = hudBlock("DIFFICULTE", difficultyTag);
        VBox scoreBlock = hudBlock("SCORE", scoreValue);

        HBox pipRow = new HBox(3);
        pipRow.setAlignment(Pos.CENTER_LEFT);
        for (int i = 0; i < MAX_PIPS; i++) {
            Region pip = new Region();
            pip.setMinSize(6, 6);
            pip.setPrefSize(6, 6);
            pip.setMaxSize(6, 6);
            fishPips[i] = pip;
            pipRow.getChildren().add(pip);
        }
        VBox fishBlock = hudBlock("POISSONS", pipRow);

        StackPane timerTrack = new StackPane(timerFill);
        timerTrack.setMinSize(TIMER_WIDTH, 7);
        timerTrack.setMaxSize(TIMER_WIDTH, 7);
        timerTrack.setStyle("-fx-background-color: #E4DCCC; -fx-background-radius: 4;");
        timerFill.setMinHeight(7);
        timerFill.setMaxHeight(7);
        StackPane.setAlignment(timerFill, Pos.CENTER_LEFT);
        timeValue.setStyle("-fx-font-size: 11px; -fx-font-weight: 600; -fx-text-fill: "
                + Theme.TEXT_MUTED + ";");
        HBox timerLine = new HBox(8, timerTrack, timeValue);
        timerLine.setAlignment(Pos.CENTER_LEFT);
        VBox timeBlock = hudBlock("TEMPS", timerLine);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        hud.getChildren().addAll(pauseBtn, soundBtn, levelBlock, spacer,
                difficultyBlock, scoreBlock, fishBlock, timeBlock, quitBtn);
        return hud;
    }

    private VBox hudBlock(String caption, javafx.scene.Node value) {
        Label cap = new Label(caption);
        cap.setStyle("-fx-font-size: 8.5px; -fx-font-weight: 600; -fx-text-fill: " + Theme.TEXT_MUTED + ";");
        VBox box = new VBox(3, cap, value);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    /** Petit bouton portant une icone dessinee au trait fin. */
    private Button iconButton(String iconType, Runnable action) {
        Button button = new Button();
        button.setGraphic(icon(iconType));
        button.setMinSize(40, 40);
        button.setMaxSize(40, 40);
        String idle = "-fx-background-color: transparent; -fx-background-radius: 12; -fx-cursor: hand;";
        String hover = "-fx-background-color: #EBE4D7; -fx-background-radius: 12; -fx-cursor: hand;";
        button.setStyle(idle);
        button.setOnMouseEntered(e -> button.setStyle(hover));
        button.setOnMouseExited(e -> button.setStyle(idle));
        if (action != null) {
            button.setOnAction(e -> action.run());
        }
        return button;
    }

    /** Dessine une icone geometrique au trait fin sur un petit canvas. */
    private Canvas icon(String type) {
        Canvas canvas = new Canvas(22, 22);
        GraphicsContext g = canvas.getGraphicsContext2D();
        Color ink = Color.web(Theme.TEXT_DARK);
        g.setStroke(ink);
        g.setFill(ink);
        g.setLineWidth(2);
        g.setLineCap(StrokeLineCap.ROUND);
        switch (type) {
            case "pause" -> {
                g.fillRoundRect(6.5, 4, 3.6, 14, 2, 2);
                g.fillRoundRect(12, 4, 3.6, 14, 2, 2);
            }
            case "sound" -> {
                g.fillPolygon(new double[]{3, 8, 8, 3}, new double[]{8.5, 8.5, 13.5, 13.5}, 4);
                g.fillPolygon(new double[]{8, 13, 13}, new double[]{11, 5, 17}, 3);
                g.strokeArc(11.5, 6.5, 8, 9, -55, 110, ArcType.OPEN);
            }
            case "muted" -> {
                g.fillPolygon(new double[]{3, 8, 8, 3}, new double[]{8.5, 8.5, 13.5, 13.5}, 4);
                g.fillPolygon(new double[]{8, 13, 13}, new double[]{11, 5, 17}, 3);
                g.strokeLine(14, 7, 20, 15);
            }
            case "home" -> {
                g.strokeLine(4, 11, 11, 4.5);
                g.strokeLine(11, 4.5, 18, 11);
                g.strokePolygon(new double[]{6, 16, 16, 6}, new double[]{11, 11, 18, 18}, 4);
            }
            default -> { }
        }
        return canvas;
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
        levelValue.setText(engine.getLevelName());
        if (story) {
            levelCaption.setText("CHAPITRE " + (currentIndex - LevelLoader.STORY_BASE + 1));
        } else if (endless) {
            levelCaption.setText("MODE SANS FIN");
        } else {
            levelCaption.setText("NIVEAU " + (currentIndex + 1));
        }

        String difficulty = engine.getLevelDifficulty();
        difficultyTag.setText(difficulty);
        difficultyTag.setStyle("-fx-background-color: " + difficultyColor(difficulty) + "; "
                + "-fx-text-fill: white; -fx-background-radius: 9; -fx-padding: 2 9 2 9; "
                + "-fx-font-size: 11px; -fx-font-weight: 600;");

        scoreValue.setText(String.valueOf(engine.getScore()));

        int collected = engine.getFishCollected();
        int total = engine.getFishTotal();
        if (collected != lastCollected || total != lastTotal) {
            for (int i = 0; i < MAX_PIPS; i++) {
                boolean shown = i < total;
                fishPips[i].setVisible(shown);
                fishPips[i].setManaged(shown);
                fishPips[i].setStyle("-fx-background-radius: 3; -fx-background-color: "
                        + (i < collected ? Theme.GOLD : "#E4DCCC") + ";");
            }
            lastCollected = collected;
            lastTotal = total;
        }

        int t = engine.getTimeLeft();
        double fraction = Math.min(1.0, t / (double) Math.max(1, engine.getTimeLimit()));
        double width = fraction * TIMER_WIDTH;
        timerFill.setMinWidth(width);
        timerFill.setPrefWidth(width);
        timerFill.setMaxWidth(width);
        timerFill.setStyle("-fx-background-radius: 4; -fx-background-color: "
                + (t <= 10 ? Theme.DANGER : Theme.ACCENT) + ";");
        timeValue.setText(String.format("%d:%02d", t / 60, t % 60));
    }

    @Override
    public void onLevelComplete() {
        if (story) {
            int chapter = currentIndex - LevelLoader.STORY_BASE;
            saveStoryProgress(chapter + 1);
            Button continueBtn = UIFactory.primaryButton("Continuer l'histoire");
            continueBtn.setOnAction(e -> exitToStory());
            Button replay = UIFactory.secondaryButton("Rejouer le chapitre");
            replay.setOnAction(e -> {
                hideOverlay();
                engine.restart();
            });
            Button home = UIFactory.secondaryButton("Accueil");
            home.setOnAction(e -> exitToHome());
            showOverlay("Chapitre termine",
                    "Nora a rassemble les lueurs de cette piece.", continueBtn, replay, home);
            return;
        }
        if (endless) {
            endlessCleared++;
            Button cont = UIFactory.successButton("Continuer");
            cont.setOnAction(e -> advanceLevel());
            Button quit = UIFactory.secondaryButton("Quitter");
            quit.setOnAction(e -> exitToHome());
            showOverlay("Salle franchie",
                    "Salles franchies : " + endlessCleared + "\nLa difficulte augmente.",
                    cont, quit);
        } else {
            boolean record = saveCampaignScore();
            int best = currentBest();
            boolean hasNext = currentIndex + 1 < LevelLoader.getCampaignCount();
            String detail = "Score  " + engine.getScore()
                    + (record ? "\nNouveau record" : "\nMeilleur  " + best);

            Button replay = UIFactory.secondaryButton("Rejouer");
            replay.setOnAction(e -> {
                hideOverlay();
                engine.restart();
            });
            Button home = UIFactory.secondaryButton("Accueil");
            home.setOnAction(e -> exitToHome());

            if (hasNext) {
                Button next = UIFactory.primaryButton("Niveau suivant");
                next.setOnAction(e -> advanceLevel());
                showOverlay("Termine", detail, next, replay, home);
            } else {
                showOverlay("Campagne terminee",
                        detail + "\n\nTu as franchi les "
                                + LevelLoader.getCampaignCount() + " niveaux.", replay, home);
            }
        }
    }

    @Override
    public void onGameOver() {
        String reason = engine.getTimeLeft() <= 0
                ? "Le temps est ecoule."
                : "Un chien t'a attrape.";
        if (story) {
            Button retry = UIFactory.primaryButton("Reessayer");
            retry.setOnAction(e -> {
                hideOverlay();
                engine.restart();
            });
            Button back = UIFactory.secondaryButton("Mode Histoire");
            back.setOnAction(e -> exitToStory());
            Button home = UIFactory.secondaryButton("Accueil");
            home.setOnAction(e -> exitToHome());
            showOverlay("Reessaie", reason, retry, back, home);
            return;
        }
        if (endless) {
            finishEndlessRun();
            Button retry = UIFactory.primaryButton("Recommencer");
            retry.setOnAction(e -> restartEndless());
            Button home = UIFactory.secondaryButton("Accueil");
            home.setOnAction(e -> exitToHome());
            showOverlay("Partie terminee",
                    reason + "\nSalles franchies : " + endlessCleared, retry, home);
        } else {
            Button retry = UIFactory.primaryButton("Reessayer");
            retry.setOnAction(e -> {
                hideOverlay();
                engine.restart();
            });
            Button home = UIFactory.secondaryButton("Accueil");
            home.setOnAction(e -> exitToHome());
            showOverlay("Reessaie", reason, retry, home);
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
        lastCollected = -1;
        engine.playLevel(currentIndex);
    }

    private void restartEndless() {
        hideOverlay();
        currentIndex = startIndex;
        endlessCleared = 0;
        endlessSaved = false;
        lastCollected = -1;
        engine.playLevel(currentIndex);
    }

    private void exitToHome() {
        finishEndlessRun();
        engine.stop();
        music.stop();
        sceneManager.showHome();
    }

    private void exitToStory() {
        engine.stop();
        music.stop();
        sceneManager.showStory();
    }

    private void saveStoryProgress(int chaptersCompleted) {
        User user = sceneManager.getCurrentUser();
        if (user != null) {
            DatabaseManager.getInstance().saveStoryProgress(user.getUsername(), chaptersCompleted);
        }
    }

    private void togglePause() {
        GameState state = engine.getState();
        if (state == GameState.PLAYING) {
            engine.pause();
            Button resume = UIFactory.primaryButton("Reprendre");
            resume.setOnAction(e -> resumeGame());
            Button quit = UIFactory.secondaryButton("Quitter");
            quit.setOnAction(e -> exitToHome());
            showOverlay("Pause", "Appuie sur P pour reprendre.", resume, quit);
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
            case "Facile" -> Theme.SUCCESS;
            case "Moyen" -> Theme.SECONDARY;
            case "Difficile" -> Theme.GOLD;
            case "Expert" -> Theme.ACCENT;
            default -> Theme.DANGER;
        };
    }

    // =====================================================================
    //  Panneau superpose
    // =====================================================================
    private void showOverlay(String headingText, String detailText, Button... actions) {
        VBox box = UIFactory.card();
        box.setMaxWidth(360);
        box.setMaxHeight(Region.USE_PREF_SIZE);
        box.setSpacing(16);

        Label detail = UIFactory.body(detailText);
        detail.setWrapText(true);
        detail.setMaxWidth(290);
        detail.setStyle(detail.getStyle() + " -fx-text-alignment: center; -fx-text-fill: "
                + Theme.TEXT_MUTED + ";");

        VBox buttons = new VBox(9, actions);
        buttons.setFillWidth(true);
        buttons.setAlignment(Pos.CENTER);
        for (Button action : actions) {
            action.setMaxWidth(Double.MAX_VALUE);
        }

        box.getChildren().setAll(UIFactory.heading(headingText), detail, buttons);
        overlay.getChildren().setAll(box);
        overlay.setVisible(true);
        UIFactory.fadeInUp(box, 0);
    }

    private void hideOverlay() {
        overlay.setVisible(false);
        overlay.getChildren().clear();
    }
}
