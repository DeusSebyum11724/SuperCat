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

Le projet couvre l'ensemble du cycle : authentification multi-rôles avec
**vérification par e-mail**, persistance dans une base **MongoDB**, moteur de
jeu temps réel, détection de collisions, gestion du score et espace
d'administration.

---

## ✨ Fonctionnalités

### Comptes et utilisateurs
- **Deux rôles** avec leur propre système de connexion : **joueur** et **administrateur**.
- Création de compte avec **vérification par e-mail** (code à 6 chiffres).
- Connexion / déconnexion.
- **Récupération du mot de passe** (vérification pseudo + e-mail).
- Création et modification du profil (e-mail, mot de passe).
- Mots de passe **hachés avec BCrypt** — jamais stockés en clair (règle RM1).

### Pour le joueur
- Démarrer une nouvelle partie et contrôler le chat au clavier.
- Interagir avec des éléments animés (poissons, bonus, chiens, sortie).
- Collecter des points, éviter les obstacles, atteindre l'objectif.
- Visualiser en temps réel le score et l'état de la partie (HUD).
- Sauvegarder automatiquement son meilleur score (à chaque niveau réussi).
- Consulter le **classement mondial** (leaderboard).

### Pour l'administrateur
- Consulter et **rechercher** tous les comptes.
- **Supprimer** un compte joueur (les comptes admin sont protégés).
- **Réinitialiser** les scores d'un joueur.
- Consulter les **statistiques** de la plateforme.

### Le jeu
- **Musique d'ambiance** (synthétisée par le programme) avec bouton activer/désactiver.
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
| Base de données | **MongoDB** (pilote `mongodb-driver-sync`) |
| E-mails | **Jakarta Mail** (SMTP Gmail) |
| Musique | `javax.sound.sampled` (synthèse audio, aucun fichier externe) |
| Sécurité | BCrypt (`jbcrypt`) pour le hachage des mots de passe |
| Build | Apache Maven |
| Tests | JUnit 5 |

---

## 📋 Prérequis

- **JDK 21 ou supérieur** (développé et testé avec le JDK 25).
- **Apache Maven 3.9+**.
- Un accès à une base **MongoDB** (par exemple MongoDB Atlas).
- Un compte **Gmail** avec un *mot de passe d'application* (pour l'envoi des
  e-mails de vérification).

Maven télécharge automatiquement toutes les bibliothèques nécessaires.

---

## ⚙️ Configuration (obligatoire)

L'application a besoin des identifiants MongoDB et Gmail. Ceux-ci ne sont
**jamais** stockés dans le code source ni publiés sur GitHub.

1. Copiez le modèle :
   ```bash
   cp config.properties.example config.properties
   ```
2. Renseignez vos identifiants dans `config.properties` :
   ```properties
   mongodb.uri=mongodb+srv://UTILISATEUR:MOTDEPASSE@cluster.mongodb.net/supercat
   gmail.user=votre.adresse@gmail.com
   gmail.password=mot de passe d application
   ```

> Le fichier `config.properties` est **exclu de Git** (`.gitignore`).
> Vous pouvez aussi définir les variables d'environnement
> `MONGODB_URI_CATALOG`, `GMAIL_USER` et `GMAIL_APP_PASSWORD`.

> ℹ️ Sur MongoDB Atlas, pensez à autoriser votre adresse IP dans
> **Network Access** pour que l'application puisse se connecter.

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

### Compte administrateur de démonstration

Un compte administrateur est créé automatiquement au premier démarrage :

| Pseudo | Mot de passe |
|--------|--------------|
| `admin` | `admin123` |

---

## 🕹️ Comment jouer

1. **Crée un compte** : un code de vérification est envoyé à ton adresse e-mail.
2. Saisis ce **code** pour activer ton compte, puis connecte-toi.
3. Clique sur **JOUER**.
4. Déplace le chat avec les **flèches du clavier** (ou **ZQSD** / **WASD**).
5. **Attrape tous les poissons d'or** : la sortie se déverrouille alors.
6. **Évite les chiens** : tout contact provoque un *Game Over*.
7. Atteins la **sortie** (verte) avant la fin du **chronomètre**.
8. **P** ou **Échap** = pause ; bouton **Son ON/OFF** = musique.

### Score
- **+100** points par poisson d'or collecté.
- **+250** points pour une étoile bonus.
- **+10 secondes** pour une horloge bonus.
- **Bonus de temps** en fin de niveau : `temps restant × 5`.

> Le score est enregistré à la fin de **chaque niveau réussi**. Un *Game Over*
> (chien ou temps écoulé) n'enregistre **pas** le score (règle RM9).

---

## 🗂️ Structure du projet

```
SuperCat/
├── pom.xml                      # Configuration Maven
├── config.properties.example    # Modèle de configuration
├── README.md
└── src/
    ├── main/java/com/supercat/
    │   ├── Main.java             # Point d'entrée
    │   ├── App.java              # Application JavaFX
    │   ├── SceneManager.java     # Navigation entre écrans
    │   ├── model/                # Objets du jeu + modèle de données
    │   │   ├── GameObject.java   (classe abstraite)
    │   │   ├── Cat.java  Dog.java  Fish.java  Bonus.java
    │   │   ├── Wall.java  Exit.java
    │   │   └── User.java  ScoreEntry.java
    │   ├── engine/               # Moteur de jeu
    │   │   ├── GameEngine.java   CollisionManager.java
    │   │   ├── Level.java  LevelLoader.java  MusicPlayer.java
    │   │   ├── GameState.java  GameListener.java  FloatingText.java
    │   ├── database/
    │   │   └── DatabaseManager.java   # Singleton MongoDB
    │   ├── service/
    │   │   ├── Config.java       # Chargement des identifiants
    │   │   └── EmailService.java # Envoi des e-mails (Jakarta Mail)
    │   ├── controller/           # Contrôleurs des écrans
    │   │   ├── LoginController.java   MenuController.java
    │   │   ├── GameController.java    AdminController.java
    │   │   ├── ProfileController.java LeaderboardController.java
    │   └── ui/
    │       ├── Theme.java         # Couleurs et constantes
    │       └── UIFactory.java     # Composants graphiques stylisés
    └── test/java/com/supercat/    # Tests unitaires JUnit 5
```

L'architecture suit le découpage en couches du dossier de conception :
**présentation** (`ui`, `controller`) → **moteur** (`engine`) →
**services** (`service`) → **modèle** (`model`) → **persistance** (`database`).

---

## 🗄️ Base de données

Base **MongoDB**, base `supercat`, deux collections :

**users**
| Champ | Description |
|-------|-------------|
| `_id` | identifiant unique (ObjectId) |
| `username` | pseudo (unique) |
| `password` | mot de passe haché BCrypt |
| `email` | adresse e-mail |
| `role` | `joueur` ou `admin` |
| `verified` | `true` si le compte a été vérifié par e-mail |
| `verificationCode` | code de vérification (supprimé après activation) |

**scores**
| Champ | Description |
|-------|-------------|
| `_id` | identifiant unique (ObjectId) |
| `username` | propriétaire du score |
| `value` | valeur numérique du score |
| `date` | date et heure de la partie |

---

## ✅ Tests unitaires

24 tests répartis en 5 classes (`mvn test`) :

| Classe de test | Vérifie |
|----------------|---------|
| `CollisionManagerTest` | La détection de collisions. |
| `GameObjectTest` | Déplacement du chat, patrouille des chiens, poissons, bonus. |
| `UserTest` | Le modèle `User` (rôles, score, profil, vérification). |
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

> Après le clonage, créez le fichier `config.properties` (voir la section
> **Configuration**) : il n'est pas inclus dans le dépôt.

---

## 👥 Auteurs

Projet académique réalisé par **BARBIER Ileana Geneviève** et
**BRISAN Andrei-Sebastian** — Groupe 1231FA, Université POLITEHNICA de Bucarest,
Faculté d'Ingénierie en Langues Étrangères.
