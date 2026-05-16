package com.supercat.engine;

/**
 * Etats possibles d'un niveau de SuperCat. Chaque niveau se joue
 * independamment : il se termine par une reussite (LEVEL_COMPLETE) ou un
 * echec (GAME_OVER).
 */
public enum GameState {

    /** Le niveau est en cours : le joueur controle le chat. */
    PLAYING,

    /** Le niveau est en pause. */
    PAUSED,

    /** Le niveau est reussi : le chat a atteint la sortie. */
    LEVEL_COMPLETE,

    /** Game Over : collision avec un chien ou temps ecoule (regle RM9). */
    GAME_OVER
}
