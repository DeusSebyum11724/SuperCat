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

L'interface s'inspire de l'esthétique épurée des jeux **Monument Valley**
(douceur, dégradés pastel) et **Mini Metro** (page d'accueil présentant les
niveaux comme une ligne de stations).

Le jeu propose une **campagne de 12 niveaux** de difficulté croissante et un
**mode sans fin** qui génère des salles à l'infini pour les joueurs aguerris.

---

## ✨ Fonctionnalités

### Comptes et utilisateurs
- **Deux rôles** avec leur propre système de connexion : **joueur** et **administrateur**.
- Création de compte avec **vérification par e-mail** (code à 6 chiffres).
- Connexion / déconnexion.
- **Récupération du mot de passe** (vérification pseudo + e-mail).
- Création et modification du profil (e-mail, mot de passe).
- Mots de passe **hachés avec BCrypt** — jamais stockés en clair (règle RM1).

### Page d'accueil
- **Scène animée** réunissant les trois chats de l'aventure : Nora la rousse,
  Suie le noir aux yeux d'or et Givre le blanc et gris.
- Trois portes d'entrée : le **mode Histoire**, **la Campagne** et le
  **mode sans fin**.

### La Campagne (sélection des niveaux)
- Tous les niveaux affichés comme une **ligne de progression** (style Mini Metro).
- **Difficulté** et **meilleur score** affichés pour chaque niveau.
- Les niveaux terminés peuvent être **rejoués pour améliorer son score**.
- Déverrouillage progressif : un niveau s'ouvre quand le précédent est terminé.
- Statistiques globales : niveaux terminés, score total, record du mode sans fin.

### Le jeu
- **Trois modes** : le **mode Histoire** (l'aventure de Nora, une petite chatte
  rousse qui traverse un pays enneigé pour rallumer le Grand Foyer du château —
  9 chapitres présentés sur une **carte de voyage** avec chaumières, château et
  étapes, en compagnie de Suie et Givre), une **campagne** de 12 niveaux, et un
  **mode sans fin** généré à l'infini.
- Affichage **plein écran** avec mise à l'échelle proportionnelle.
- **Génération procédurale** : chaque niveau est un labyrinthe généré
  automatiquement (toujours résoluble), identique à chaque fois.
- **Musique d'ambiance** synthétisée + bouton activer/désactiver.
- **Écran-titre animé** à l'ouverture, et animations partout : démarche du
  chat, patrouille des chiens, apparition échelonnée des niveaux, fondus.
- **HUD schématique** : barre de temps, points pour les poissons, étiquette
  de difficulté du niveau.
- **Détection de collisions**, calcul du score, fin de partie et redémarrage.
- Chaque niveau (campagne et mode sans fin) est **vérifié résoluble** par un
  test automatisé (parcours en largeur du labyrinthe).

### Pour l'administrateur
- Consulter, **rechercher**, **supprimer** des comptes joueurs.
- **Réinitialiser** les scores d'un joueur.
- Consulter les **statistiques** de la plateforme.

---

## 🅰️ Projet de Type A — fonctionnalités avancées

Le projet implémente largement les critères de Type A (2 suffisent) :

| # | Fonctionnalité avancée | Mise en œuvre |
|---|------------------------|---------------|
| 1 | **Plusieurs niveaux** | 12 niveaux de campagne, difficulté progressive. |
| 2 | **Génération procédurale + mode sans fin** | Niveaux générés à l'infini (`LevelLoader`). |
| 3 | **Ennemis à mouvement autonome** | Les chiens patrouillent seuls et font demi-tour aux murs. |
| 4 | **Objets bonus / spéciaux** | Étoile (+250 points) et horloge (+10 secondes). |
| 5 | **Animations et transitions avancées** | Sprites animés, retours visuels, transitions en fondu. |

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
- Un compte **Gmail** avec un *mot de passe d'application* (e-mails de vérification).

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

> ℹ️ Sur MongoDB Atlas, autorisez votre adresse IP dans **Network Access**.

---

## 🚀 Compilation et lancement

```bash
mvn compile      # compiler le projet
mvn javafx:run   # lancer le jeu
mvn test         # exécuter les tests unitaires
```

### Compte administrateur de démonstration

| Pseudo | Mot de passe |
|--------|--------------|
| `admin` | `admin123` |

---

## 🕹️ Comment jouer

1. **Crée un compte** : un code de vérification est envoyé à ton adresse e-mail.
2. Saisis ce **code** pour activer ton compte, puis connecte-toi.
3. Sur la **page d'accueil**, choisis un niveau (les niveaux se débloquent au fur
   et à mesure). Le **mode sans fin** s'ouvre après 3 niveaux terminés.
4. Déplace le chat avec les **flèches** (ou **ZQSD** / **WASD**).
5. **Attrape tous les poissons d'or** : la sortie se déverrouille alors.
6. **Évite les chiens** et atteins la **sortie** avant la fin du chronomètre.
7. **P** ou **Échap** = pause ; bouton **Son ON/OFF** = musique.

### Score
- **+100** points par poisson d'or, **+250** pour une étoile bonus,
  **+10 secondes** pour une horloge bonus.
- **Bonus de temps** en fin de niveau : `temps restant × 5`.
- Le score est enregistré **par niveau** : rejoue un niveau pour l'améliorer.
- Le mode sans fin retient le **nombre de salles franchies**.

> Un *Game Over* (chien ou temps écoulé) n'enregistre pas le score (règle RM9).

---

## 🗂️ Structure du projet

```
SuperCat/
├── pom.xml                      # Configuration Maven
├── config.properties.example    # Modèle de configuration
└── src/
    ├── main/java/com/supercat/
    │   ├── Main.java  App.java  SceneManager.java
    │   ├── model/                # GameObject, Cat, Dog, Fish, Bonus,
    │   │                         #   Wall, Exit, User, ScoreEntry
    │   ├── engine/               # GameEngine, CollisionManager, Level,
    │   │                         #   LevelLoader (génération procédurale),
    │   │                         #   MusicPlayer, GameState, GameListener
    │   ├── database/             # DatabaseManager (Singleton MongoDB)
    │   ├── service/              # Config, EmailService (Jakarta Mail)
    │   ├── controller/           # Login, Home, Game, Admin,
    │   │                         #   Profile, Leaderboard
    │   └── ui/                   # Theme, UIFactory
    └── test/java/com/supercat/   # Tests unitaires JUnit 5
```

---

## 🗄️ Base de données

Base **MongoDB** (`supercat`), deux collections :

**users** — `username`, `password` (haché BCrypt), `email`, `role`,
`verified`, `verificationCode`.

**scores** — `username`, `level` (0 à 11 pour la campagne, `-1` pour le mode
sans fin), `value` (points, ou nombre de salles franchies en mode sans fin),
`date`.

---

## ✅ Tests unitaires

29 tests répartis en 6 classes (`mvn test`) :

| Classe de test | Vérifie |
|----------------|---------|
| `CollisionManagerTest` | La détection de collisions. |
| `GameObjectTest` | Déplacement du chat, patrouille des chiens, poissons, bonus. |
| `UserTest` | Le modèle `User` (rôles, score, profil, vérification). |
| `PasswordSecurityTest` | Le hachage BCrypt des mots de passe (RM1). |
| `LevelLoaderTest` | La génération procédurale (campagne + mode sans fin). |
| `LevelSolvabilityTest` | Que tous les niveaux générés sont réellement résolubles. |

---

## 🔁 Gestion du code avec Git

Le projet est hébergé sur GitHub : **https://github.com/DeusSebyum11724/SuperCat**

```bash
git clone https://github.com/DeusSebyum11724/SuperCat.git
git pull origin main
git add .
git commit -m "Description des modifications"
git push origin main
```

> Après le clonage, créez le fichier `config.properties` (voir **Configuration**).

---

## 👥 Auteurs

Projet académique réalisé par **BARBIER Ileana Geneviève** et
**BRISAN Andrei-Sebastian** — Groupe 1231FA, Université POLITEHNICA de Bucarest,
Faculté d'Ingénierie en Langues Étrangères.
