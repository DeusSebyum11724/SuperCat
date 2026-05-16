package com.supercat.engine;

import com.supercat.model.Bonus;
import com.supercat.model.Cat;
import com.supercat.model.Dog;
import com.supercat.model.Exit;
import com.supercat.model.Fish;
import com.supercat.model.Wall;

import java.util.List;

/**
 * Represente un niveau de jeu (un labyrinthe) entierement instancie : les
 * murs, le chat a sa position de depart, les poissons d'or, les chiens, les
 * bonus et la sortie.
 *
 * Un objet Level est produit par le LevelLoader (generation procedurale).
 */
public class Level {

    private final int index;
    private final String name;
    private final String difficultyLabel;
    private final int timeLimit;          // secondes accordees au joueur
    private final Cat cat;
    private final Exit exit;
    private final List<Wall> walls;
    private final List<Fish> fish;
    private final List<Dog> dogs;
    private final List<Bonus> bonuses;

    public Level(int index, String name, String difficultyLabel, int timeLimit,
                 Cat cat, Exit exit, List<Wall> walls, List<Fish> fish,
                 List<Dog> dogs, List<Bonus> bonuses) {
        this.index = index;
        this.name = name;
        this.difficultyLabel = difficultyLabel;
        this.timeLimit = timeLimit;
        this.cat = cat;
        this.exit = exit;
        this.walls = walls;
        this.fish = fish;
        this.dogs = dogs;
        this.bonuses = bonuses;
    }

    public int getIndex() { return index; }
    public String getName() { return name; }
    public String getDifficultyLabel() { return difficultyLabel; }
    public int getTimeLimit() { return timeLimit; }
    public Cat getCat() { return cat; }
    public Exit getExit() { return exit; }
    public List<Wall> getWalls() { return walls; }
    public List<Fish> getFish() { return fish; }
    public List<Dog> getDogs() { return dogs; }
    public List<Bonus> getBonuses() { return bonuses; }
}
