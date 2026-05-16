package com.supercat;

import javafx.application.Application;

/**
 * Point d'entree de l'application SuperCat.
 *
 * Cette classe de lancement est volontairement distincte de la classe
 * Application (App). Lancer JavaFX depuis une classe qui n'herite pas de
 * Application evite l'erreur "JavaFX runtime components are missing".
 */
public final class Main {

    private Main() {
        // pas d'instanciation
    }

    public static void main(String[] args) {
        Application.launch(App.class, args);
    }
}
