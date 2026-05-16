# 🐱 SuperCat

> Un jeu de plateforme 2D animé, développé en **JavaFX**, mettant en scène un chat
> agile dans un monde labyrinthique parsemé d'embûches.

**Université POLITEHNICA de Bucarest — Faculté d'Ingénierie en Langues Étrangères (FILS)**

Projet réalisé par :
- **BARBIER Ileana Geneviève**
- **BRISAN Andrei-Sebastian**

Groupe **1231FA**

---

## 🎮 Présentation

**SuperCat** est une application *desktop* développée en Java + JavaFX. Le joueur
incarne un chat qui doit parcourir des labyrinthes en 2D : il attrape des
**poissons d'or** (à la place des traditionnelles pièces), évite des **chiens**
(les ennemis) et doit atteindre la **sortie** avant la fin du chronomètre.

Le projet couvre l'ensemble du cycle : authentification multi-rôles, persistance
en base de données, moteur de jeu temps réel, détection de collisions, gestion du
score et espace d'administration.

---

## ✨ Fonctionnalités

### Comptes et utilisateurs
- **Deux rôles** avec leur propre système de connexion : **joueur** et **administrateur**.
- Création de compte, connexion / déconnexion.
- **Récupération du mot de passe** (vérification pseudo + e-mail).
- Création et modification du profil (e-mail, mot de passe).
- Mots de passe **hachés avec BCrypt** — jamais stockés en clair (règle RM1).

### Pour le joueur
- Démarrer une nouvelle partie et contrôler le chat au clavier.
- Interagir avec des éléments animés (poissons, bonus, chiens, sortie).
- Collecter des points, éviter les obstacles, atteindre l'objectif.
- Visualiser en temps réel le score et l'état de la partie (HUD).
- Sauvegarder automatiquement son meilleur score.
- Consulter le **classement mondial** (leaderboard).

### Pour l'administrateur
- Consulter et **rechercher** tous les comptes.
- **Supprimer** un compte joueur (les comptes admin sont protégés).
- **Réinitialiser** les scores d'un joueur.
- Consulter les **statistiques** de la plateforme.

### Le jeu
- **Animations** : démarche du chat, patrouille des chiens, flottement des
  poissons, pulsation des bonus, transitions en fondu entre les écrans.
- **3 niveaux** (labyrinthes) de difficulté croissante.
- **Détection de collisions** précise (chat / murs / chiens / poissons / sortie).
- **Calcul du score** : 100 points par poisson + bonus de temps + objets bonus.
- Mécanisme de **fin de partie et de redémarrage** (Game Over / Victoire / Rejouer).

---

## 🅰️ Projet de Type A — fonctionnalités avancées

Le projet implémente **4 fonctionnalités avancées** (2 suffisent pour le Type A) :

| # | Fonctionnalité avancée | Mise en œuvre |
|---|------------------------|---------------|
| 1 | **Plusieurs niveaux** | 3 labyrinthes distincts (`LevelLoader`), difficulté progressive. |
| 2 | **Ennemis à mouvement autonome** | Les chiens patrouillent seuls et font demi-tour aux murs (`Dog`). |
| 3 | **Objets bonus / spéciaux** | Étoile (+250 points) et horloge (+10 secondes) (`Bonus`). |
| 4 | **Animations et transitions avancées** | Sprites animés, retours visuels « +100 », transitions en fondu. |

---

## 🛠️ Technologies

| Élément | Choix |
|---------|-------|
| Langage | Java 21+ |
| Interface / rendu | JavaFX 25 (`Canvas`, `AnimationTimer`) |
| Base de données | SQLite (via `sqlite-jdbc`) |
| Sécurité | BCrypt (`jbcrypt`) pour le hachage des mots de passe |
| Build | Apache Maven |
| Tests | JUnit 5 |

---

## 📋 Prérequis

- **JDK 21 ou supérieur** (le projet a été développé et testé avec le JDK 25).
- **Apache Maven 3.9+**.

Aucune installation manuelle de JavaFX n'est nécessaire : Maven télécharge
automatiquement toutes les dépendances.

---

## 🚀 Compilation et lancement

```bash
# Compiler le projet
mvn compile

# Lancer le jeu
mvn javafx:run

# Exécuter les tests unitaires
mvn test
```

La base de données SQLite (`supercat.db`) est créée automatiquement au premier
lancement, dans le répertoire du projet.

### Compte administrateur de démonstration

Un compte administrateur est créé automatiquement au premier démarrage :

| Pseudo | Mot de passe |
|--------|--------------|
| `admin` | `admin123` |

---

## 🕹️ Comment jouer

1. **Crée un compte** ou connecte-toi.
2. Clique sur **JOUER**.
3. Déplace le chat avec les **flèches du clavier** (ou **ZQSD** / **WASD**).
4. **Attrape tous les poissons d'or** : la sortie se déverrouille alors.
5. **Évite les chiens** : tout contact provoque un *Game Over*.
6. Atteins la **sortie** (verte) avant la fin du **chronomètre**.
7. Appuie sur **P** ou **Échap** pour mettre le jeu en **pause**.

### Score
- **+100** points par poisson d'or collecté.
- **+250** points pour une étoile bonus.
- **+10 secondes** pour une horloge bonus.
- **Bonus de temps** en fin de niveau : `temps restant × 5`.

> Conformément à la règle RM9, un *Game Over* (chien ou temps écoulé)
> n'enregistre **pas** le score.

---

## 🗂️ Structure du projet

```
SuperCat/
├── pom.xml                     # Configuration Maven
├── README.md
└── src/
    ├── main/java/com/supercat/
    │   ├── Main.java            # Point d'entrée
    │   ├── App.java             # Application JavaFX
    │   ├── SceneManager.java    # Navigation entre écrans
    │   ├── model/               # Objets du jeu + modèle de données
    │   │   ├── GameObject.java  (classe abstraite)
    │   │   ├── Cat.java  Dog.java  Fish.java  Bonus.java
    │   │   ├── Wall.java  Exit.java
    │   │   └── User.java  ScoreEntry.java
    │   ├── engine/              # Moteur de jeu
    │   │   ├── GameEngine.java  CollisionManager.java
    │   │   ├── Level.java  LevelLoader.java
    │   │   ├── GameState.java  GameListener.java  FloatingText.java
    │   ├── database/
    │   │   └── DatabaseManager.java   # Singleton SQLite
    │   ├── controller/          # Contrôleurs des écrans
    │   │   ├── LoginController.java   MenuController.java
    │   │   ├── GameController.java    AdminController.java
    │   │   ├── ProfileController.java LeaderboardController.java
    │   └── ui/
    │       ├── Theme.java        # Couleurs et constantes
    │       └── UIFactory.java    # Composants graphiques stylisés
    └── test/java/com/supercat/   # Tests unitaires JUnit 5
```

L'architecture suit le découpage en couches du dossier de conception :
**présentation** (`ui`, `controller`) → **moteur** (`engine`) → **modèle**
(`model`) → **persistance** (`database`).

---

## 🗄️ Base de données

Base **SQLite**, deux tables :

**USERS**
| Champ | Type | Contraintes |
|-------|------|-------------|
| id | INTEGER | PK, auto-incrément |
| username | TEXT | unique, non nul |
| password | TEXT | non nul (haché BCrypt) |
| email | TEXT | utilisé pour la récupération de mot de passe |
| role | TEXT | `joueur` ou `admin` |

**SCORES**
| Champ | Type | Contraintes |
|-------|------|-------------|
| id | INTEGER | PK, auto-incrément |
| user_id | INTEGER | clé étrangère → `users(id)`, suppression en cascade |
| value | INTEGER | non nul |
| date | TEXT | date/heure de la partie |

---

## ✅ Tests unitaires

23 tests répartis en 5 classes (`mvn test`) :

| Classe de test | Vérifie |
|----------------|---------|
| `CollisionManagerTest` | La détection de collisions. |
| `GameObjectTest` | Déplacement du chat, patrouille des chiens, poissons, bonus. |
| `UserTest` | Le modèle `User` (rôles, score, profil). |
| `PasswordSecurityTest` | Le hachage BCrypt des mots de passe (RM1). |
| `LevelLoaderTest` | Le chargement des 3 niveaux et la difficulté progressive. |

---

## 🔁 Gestion du code avec Git

Le projet est hébergé sur GitHub : **https://github.com/DeusSebyum11724/SuperCat**

```bash
# Cloner le dépôt
git clone https://github.com/DeusSebyum11724/SuperCat.git

# Récupérer les dernières modifications depuis GitHub
git pull origin main

# Envoyer ses modifications vers GitHub
git add .
git commit -m "Description des modifications"
git push origin main
```

---

## 👥 Auteurs

Projet académique réalisé par **BARBIER Ileana Geneviève** et
**BRISAN Andrei-Sebastian** — Groupe 1231FA, Université POLITEHNICA de Bucarest,
Faculté d'Ingénierie en Langues Étrangères.
