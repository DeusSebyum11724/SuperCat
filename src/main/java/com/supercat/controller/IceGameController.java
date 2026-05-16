package com.supercat.controller;

import com.supercat.SceneManager;
import com.supercat.engine.IcePuzzle;
import com.supercat.ui.CatArt;
import com.supercat.ui.Theme;
import javafx.animation.AnimationTimer;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

import java.util.List;

/**
 * Mini-jeu du Lac Gele : la glisse sur la glace.
 *
 * Nora glisse en ligne droite jusqu'a heurter un rocher ou le bord du lac.
 * Le joueur choisit une direction (fleches ou ZQSD/WASD) ; il faut passer
 * sur tous les poissons d'or puis venir s'immobiliser sur la sortie. Le
 * casse-tete est fourni par {@link IcePuzzle}, toujours resoluble.
 */
public class IceGameController extends StoryMiniGame {

    private static final double TILE = 44;
    private static final double GRID_W = IcePuzzle.COLS * TILE;
    private static final double GRID_H = IcePuzzle.ROWS * TILE;
    private static final double OX = (GAME_W - GRID_W) / 2;
    private static final double OY = (GAME_H - GRID_H) / 2;

    private IcePuzzle puzzle;
    private Canvas canvas;
    private GraphicsContext g;
    private AnimationTimer timer;

    private int catRow;
    private int catCol;
    private List<int[]> fish;
    private boolean[] collected;
    private int fishLeft;

    private boolean sliding;
    private int slideFromR;
    private int slideFromC;
    private int slideToR;
    private int slideToC;
    private int slideDirR;
    private int slideDirC;
    private int slideLen;
    private double slideProgress;
    private double slideDuration;

    private double timeLeft;
    private double animTime;
    private long lastNano;

    public IceGameController(SceneManager sceneManager, int chapter) {
        super(sceneManager, chapter);
    }

    @Override
    protected Node createContent() {
        puzzle = IcePuzzle.generate(chapter);
        canvas = new Canvas(GAME_W, GAME_H);
        g = canvas.getGraphicsContext2D();
        resetState();
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                frame(now);
            }
        };
        timer.start();
        return canvas;
    }

    @Override
    protected void stopLoop() {
        if (timer != null) {
            timer.stop();
        }
    }

    @Override
    protected void restartGame() {
        puzzle = IcePuzzle.generate(chapter);
        resetState();
    }

    private void resetState() {
        catRow = puzzle.getCatRow();
        catCol = puzzle.getCatCol();
        fish = puzzle.getFish();
        collected = new boolean[fish.size()];
        fishLeft = fish.size();
        sliding = false;
        slideProgress = 0;
        timeLeft = puzzle.getTimeLimit();
        animTime = 0;
        lastNano = 0;
        updateGoal();
        updateTimer(puzzle.getTimeLimit(), puzzle.getTimeLimit());
        if (g != null) {
            render();
        }
    }

    // =====================================================================
    //  Boucle de jeu
    // =====================================================================
    private void frame(long now) {
        if (!isActive()) {
            lastNano = 0;
            render();
            return;
        }
        if (lastNano == 0) {
            lastNano = now;
            render();
            return;
        }
        double dt = Math.min(0.05, (now - lastNano) / 1_000_000_000.0);
        lastNano = now;
        animTime += dt;

        timeLeft -= dt;
        if (timeLeft <= 0) {
            timeLeft = 0;
            updateTimer(0, puzzle.getTimeLimit());
            render();
            lose("Le temps du lac est ecoule.");
            return;
        }
        if (sliding) {
            advanceSlide(dt);
        }
        updateTimer((int) Math.ceil(timeLeft), puzzle.getTimeLimit());
        render();
    }

    private void advanceSlide(double dt) {
        slideProgress = Math.min(1.0, slideProgress + dt / slideDuration);
        int reached = (int) Math.round(slideProgress * slideLen);
        for (int k = 0; k <= reached; k++) {
            collectFishAt(slideFromR + slideDirR * k, slideFromC + slideDirC * k);
        }
        if (slideProgress >= 1.0) {
            sliding = false;
            catRow = slideToR;
            catCol = slideToC;
            if (catRow == puzzle.getExitRow() && catCol == puzzle.getExitCol()
                    && fishLeft == 0) {
                win();
            }
        }
    }

    private void collectFishAt(int r, int c) {
        for (int i = 0; i < fish.size(); i++) {
            if (!collected[i] && fish.get(i)[0] == r && fish.get(i)[1] == c) {
                collected[i] = true;
                fishLeft--;
                updateGoal();
            }
        }
    }

    private void updateGoal() {
        int total = fish.size();
        setGoal("Poissons " + (total - fishLeft) + " / " + total);
    }

    @Override
    protected void onKeyPressed(KeyCode code) {
        if (sliding) {
            return;
        }
        switch (code) {
            case UP, W, Z -> startSlide(-1, 0);
            case DOWN, S -> startSlide(1, 0);
            case LEFT, A, Q -> startSlide(0, -1);
            case RIGHT, D -> startSlide(0, 1);
            default -> { }
        }
    }

    private void startSlide(int dr, int dc) {
        int[] end = puzzle.slideEnd(catRow, catCol, dr, dc);
        if (end[0] == catRow && end[1] == catCol) {
            return;   // bloque dans cette direction
        }
        slideFromR = catRow;
        slideFromC = catCol;
        slideToR = end[0];
        slideToC = end[1];
        slideDirR = dr;
        slideDirC = dc;
        slideLen = Math.abs(slideToR - slideFromR) + Math.abs(slideToC - slideFromC);
        slideProgress = 0;
        slideDuration = Math.max(0.14, slideLen * 0.055);
        sliding = true;
    }

    // =====================================================================
    //  Rendu
    // =====================================================================
    private void render() {
        g.setFill(Color.web("#ECE5D7"));
        g.fillRect(0, 0, GAME_W, GAME_H);

        // surface du lac
        g.setFill(Color.web("#C9DCE4"));
        g.fillRoundRect(OX - 10, OY - 10, GRID_W + 20, GRID_H + 20, 20, 20);

        for (int r = 0; r < IcePuzzle.ROWS; r++) {
            for (int c = 0; c < IcePuzzle.COLS; c++) {
                double x = OX + c * TILE;
                double y = OY + r * TILE;
                if (puzzle.isWall(r, c)) {
                    drawRock(x, y);
                } else {
                    g.setFill((r + c) % 2 == 0
                            ? Color.web("#DDE9ED") : Color.web("#D3E1E8"));
                    g.fillRect(x, y, TILE, TILE);
                    g.setStroke(Color.web("#FFFFFF", 0.35));
                    g.setLineWidth(1);
                    g.strokeLine(x + 3, y + 3, x + TILE - 3, y + 3);
                }
            }
        }

        drawExit();
        for (int i = 0; i < fish.size(); i++) {
            if (!collected[i]) {
                drawFish(cellCenterX(fish.get(i)[1]), cellCenterY(fish.get(i)[0]), i);
            }
        }

        double fr = catRow;
        double fc = catCol;
        if (sliding) {
            fr = slideFromR + (slideToR - slideFromR) * slideProgress;
            fc = slideFromC + (slideToC - slideFromC) * slideProgress;
        }
        double cx = OX + fc * TILE + TILE / 2;
        double feetY = OY + fr * TILE + TILE * 0.86;
        CatArt.draw(g, CatArt.NORA, cx, feetY, TILE * 0.94, animTime);
    }

    private void drawRock(double x, double y) {
        g.setFill(Color.web("#B0AABA"));
        g.fillRoundRect(x + 1, y + 1, TILE - 2, TILE - 2, 12, 12);
        g.setFill(Color.web("#9C95A8"));
        g.fillRoundRect(x + 1, y + TILE * 0.5, TILE - 2, TILE * 0.5 - 1, 12, 12);
        // calotte de neige
        g.setFill(Color.web("#FBFAF6"));
        g.fillRoundRect(x + 3, y + 2, TILE - 6, TILE * 0.40, 10, 10);
    }

    private void drawExit() {
        double ex = cellCenterX(puzzle.getExitCol());
        double ey = cellCenterY(puzzle.getExitRow());
        boolean open = fishLeft == 0;
        String tint = open ? "#7BA793" : "#C97A6D";

        g.setFill(Color.web(tint, 0.30));
        g.fillOval(ex - TILE * 0.52, ey - TILE * 0.52, TILE * 1.04, TILE * 1.04);

        // arche / porte
        g.setFill(Color.web(tint));
        g.fillRoundRect(ex - TILE * 0.28, ey - TILE * 0.32,
                TILE * 0.56, TILE * 0.66, TILE * 0.56, TILE * 0.56);
        g.setFill(Color.web("#F4EFE4"));
        g.fillRoundRect(ex - TILE * 0.17, ey - TILE * 0.16,
                TILE * 0.34, TILE * 0.50, TILE * 0.34, TILE * 0.34);
        if (open) {
            g.setFill(Color.web("#E7B45C"));
            double f = TILE * 0.13;
            g.fillOval(ex - f, ey - f + TILE * 0.04, f * 2, f * 2.2);
        }
    }

    private void drawFish(double cx, double cy, int seed) {
        double bob = Math.sin(animTime * 2.4 + seed) * 2.4;
        cy += bob;
        g.setFill(Color.web("#FFD23F", 0.25));
        g.fillOval(cx - 14, cy - 14, 28, 28);
        g.setFill(Theme.FISH_DARK);
        g.fillPolygon(new double[]{cx - 5, cx - 13, cx - 13},
                new double[]{cy, cy - 6, cy + 6}, 3);
        g.setFill(Theme.FISH_BODY);
        g.fillOval(cx - 9, cy - 6, 19, 13);
        g.setFill(Color.WHITE);
        g.fillOval(cx + 2, cy - 4, 5, 5);
        g.setFill(Color.web("#2B2B2B"));
        g.fillOval(cx + 3.4, cy - 2.6, 2.4, 2.4);
    }

    private double cellCenterX(int col) {
        return OX + col * TILE + TILE / 2;
    }

    private double cellCenterY(int row) {
        return OY + row * TILE + TILE / 2;
    }
}
