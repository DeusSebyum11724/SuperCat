package com.supercat.engine;

/**
 * Les differents etats possibles d'une partie de SuperCat. Ils correspondent
 * aux etats decrits dans le diagramme d'activite du jeu.
 */
public enum GameState {

    /** La partie est en cours : le joueur controle le chat. */
    PLAYING,

    /** La partie est en pause. */
    PAUSED,

    /** Le niveau courant est termine, en attente du niveau suivant. */
    LEVEL_COMPLETE,

    /** Game Over : collision avec un chien ou temps ecoule (regle RM9). */
    GAME_OVER,

    /** Tous les niveaux ont ete termines : victoire finale. */
    GAME_WON
}
