# Chess Framework

Framework pédagogique **Java** : vous créez un joueur d'échecs en pratiquant l'héritage, les interfaces, les génériques, la composition et les tests. Le dépôt fournit déjà les règles du jeu et le tournoi ; votre travail porte sur **la stratégie de votre bot**.

## Vous êtes étudiant ? Commencez ici

**Votre objectif :** livrer une classe `VotreNomBot extends ChessBot`, ses éventuelles classes auxiliaires et au moins un test. Elle doit choisir des coups légaux, avoir une stratégie identifiable et être soumise par Pull Request. Vous n'avez ni moteur d'échecs ni interface graphique à développer.

### 1. Préparer votre poste

Installez **JDK 25**, **Maven 3.9.x** et **Git**, puis vérifiez dans un terminal :

```bash
java --version
mvn --version
git --version
```

`java` et le Java affiché par `mvn --version` doivent pointer vers le **JDK 25**. Dans IntelliJ IDEA ou un autre IDE, choisissez aussi ce JDK pour le projet Maven. En cas de difficulté : [dépannage](docs/TROUBLESHOOTING.md).

### 2. Faire un fork, cloner et créer une branche personnelle

Sur [GitHub](https://github.com/AstrowareConception/Chess-Framework), cliquez sur **Fork** pour créer une copie dans votre compte. Clonez **votre fork** (remplacez `ALICE-GITHUB` par votre identifiant GitHub), puis créez votre branche :

```bash
git clone https://github.com/ALICE-GITHUB/Chess-Framework.git
cd Chess-Framework
git switch -c bot/alice-deep-rabbit
```

Remplacez le nom de la branche et les noms d'exemple ci-dessous par les vôtres. **Créez la branche avant le bot** : votre Pull Request contiendra alors uniquement votre travail. Si l'enseignant vous a donné un accès direct en écriture, vous pouvez cloner le dépôt principal et sauter l'étape du fork.

### 3. Générer votre bot et son test

Linux, macOS ou Git Bash :

```bash
bash scripts/new-student-bot.sh DeepRabbitBot "Alice Dupont" "Deep Rabbit" "Bot prudent qui protège son roi."
```

Windows PowerShell, depuis la racine du dépôt :

```powershell
powershell -ExecutionPolicy Bypass -File scripts/new-student-bot.ps1 DeepRabbitBot "Alice Dupont" "Deep Rabbit" "Bot prudent qui protège son roi."
```

Le script crée `DeepRabbitBot.java` et `DeepRabbitBotTest.java` dans les packages `students` prévus pour la soumission. Ouvrez ces deux fichiers : le bot généré sait déjà jouer ; **votre exercice consiste à remplacer progressivement sa stratégie de départ par la vôtre**. La clé du bot dans la console sera `student-deep-rabbit`. Pour le tournoi IRIS Nice, remplacez « Alice Dupont » par **votre prénom et votre nom réels** : la participation est individuelle, avec un bot par étudiant.

### 4. Construire, valider et jouer

```bash
mvn verify
bash scripts/chess.sh validate-students
bash scripts/chess.sh list
bash scripts/chess.sh console student-deep-rabbit random --isolated
```

Sur Windows PowerShell, gardez `mvn verify` et remplacez `bash scripts/chess.sh` par `powershell -ExecutionPolicy Bypass -File scripts/chess.ps1` (par exemple `powershell -ExecutionPolicy Bypass -File scripts/chess.ps1 list`). `mvn verify` compile et lance les tests ; `validate-students` vérifie l'identité du bot et lui fait jouer des parties isolées avec les deux couleurs ; `list` confirme sa découverte ; `console` montre un duel. **Si vous modifiez le Java, relancez `mvn verify` avant le duel** pour mettre à jour le runner.

### 5. Améliorer la stratégie, puis rendre le travail

Suivez cet ordre de travail :

1. Lisez [le modèle objet illustré par des diagrammes UML](docs/OBJECT_MODEL.md) pour comprendre qui décide, qui détecte et qui valide un coup.
2. Comparez [`RandomBot`](chess-bots/src/main/java/fr/astroware/chess/bots/baseline/RandomBot.java), [`GreedyBot`](chess-bots/src/main/java/fr/astroware/chess/bots/baseline/GreedyBot.java) et [`SolidPlannerBot`](chess-bots/src/main/java/fr/astroware/chess/bots/examples/SolidPlannerBot.java). Repérez leurs méthodes redéfinies et l'ordre de leurs règles.
3. Modifiez **une règle à la fois** dans votre classe. Faites un duel contre `random`, puis `greedy` ; ajoutez un test qui prouve un comportement de votre stratégie, au-delà du seul nom du bot.
4. Passez ensuite au [parcours d'exercices POO](docs/OBJECT_MODEL.md#parcours-de-travail-conseille) et au [guide complet de création et de Pull Request](docs/STUDENT_TOURNAMENT_BOT.md). Le bot et ses tests doivent rester dans les deux répertoires `students` ; aucune modification du framework ne doit figurer dans cette Pull Request.

Avant de pousser : `mvn verify`, `validate-students`, puis `git status` pour contrôler les fichiers. Ajoutez seulement votre bot et ses tests, faites un commit et poussez votre branche :

```bash
git add chess-bots/src/main/java/fr/astroware/chess/bots/students/ chess-bots/src/test/java/fr/astroware/chess/bots/students/
git commit -m "feat: add DeepRabbitBot"
git push -u origin bot/alice-deep-rabbit
```

Sur GitHub, ouvrez une Pull Request de **votre branche du fork vers `AstrowareConception/Chess-Framework:main`** ; attendez les contrôles CI. La [checklist de rendu](docs/STUDENT_TOURNAMENT_BOT.md#16-checklist-avant-rendu) récapitule les conditions du tournoi. Un classement IRIS-Elo est disponible pour choisir des adversaires ; il n'est **pas** une mesure Elo FIDE.

### Où chercher une réponse ?

| Votre question | À lire |
| --- | --- |
| « Comment fonctionne le modèle et quel concept POO dois-je pratiquer ? » | [Modèle objet et exercices UML](docs/OBJECT_MODEL.md) |
| « Comment écrire et soumettre mon bot ? » | [Guide de soumission](docs/STUDENT_TOURNAMENT_BOT.md) |
| « Comment apprendre Java et l'API pas à pas ? » | [Bien démarrer](docs/GETTING_STARTED.md) |
| « Une commande échoue ou le bot n'apparaît pas ? » | [Dépannage](docs/TROUBLESHOOTING.md) |
| « Comment se déroule le tournoi IRIS Nice ? » | [Règlement](docs/IRIS_NICE_TOURNAMENT_RULES.md) |

## Vision

Le projet fournit un moteur de jeu fiable et une API orientée objet permettant à un étudiant de créer son propre joueur d'échecs en étendant `ChessBot`.

Le comportement d'un bot reste volontairement lisible :

```text
Situation reconnue
    ↓
plusieurs coups candidats
    ↓
évaluation
    ↓
choix selon la stratégie du bot
```

Un bot peut également suivre :

- une **personnalité stratégique** : défensive, équilibrée, offensive, aventureuse ;
- une **ouverture** connue tant que la partie reste dans son livre ;
- des **plans multi-coups** comme prendre le centre, développer ou préparer le roque ;
- des règles tactiques ordonnées ;
- un fallback de sécurité.

L'objectif pédagogique est de pratiquer la programmation orientée objet, l'héritage, la composition, le polymorphisme, les interfaces, l'encapsulation, les génériques, les tests, Git/GitHub et plusieurs design patterns dans un projet ludique dont l'aboutissement est un tournoi de bots.

## Principes

- Java 25 LTS.
- Maven multi-module.
- API simple pour les étudiants, architecture interne rigoureuse.
- Le framework gère les règles des échecs et la légalité des coups.
- Les positions sont exposées en lecture seule.
- Une situation peut produire plusieurs détections.
- Une action peut produire plusieurs coups candidats.
- Chaque candidat peut être noté de 0 à 10.
- Le profil du bot peut privilégier l'agressivité, la sécurité ou la prise de risque.
- Les plans stratégiques peuvent durer plusieurs coups sans imposer une séquence rigide.
- Les ouvertures sont suivies tant qu'une ligne reste compatible et légale.
- Les règles restent ordonnées : l'ordre exprime les priorités du bot.
- Chaque décision peut être tracée et expliquée.
- Les matchs sont reproductibles grâce à des graines aléatoires contrôlées.
- Les bots étudiants ont vocation à être intégrés par Pull Request.
- Le tournoi peut exécuter chaque bot dans une JVM enfant isolée avec timeout dur et plafond mémoire.

## Stack

- **Java 25 LTS**
- **Maven**
- **JUnit**
- **JaCoCo**
- **GitHub Actions**
- moteur de règles d'échecs masqué derrière une interface interne
- CLI de duel et de tournoi, plus viewer Swing
- exports PGN/CSV, rapports de tournoi et replay

## Exemple de bot stratégique

```java
public final class MyBot extends ChessBot {

    @Override
    public BotMetadata metadata() {
        return new BotMetadata(
            "Deep Rabbit",
            "Alice Dupont",
            "Bot solide qui privilégie la sécurité avant l'activité."
        );
    }

    @Override
    protected StrategyProfile strategyProfile() {
        return StrategyProfiles.solid();
    }

    @Override
    protected List<Rule<?>> rules() {
        return List.of(
            // Les règles tactiques critiques peuvent être placées ici.

            Openings.londonSystem().asRule(),
            Openings.scandinavianDefense().asRule(),

            Plans.castleKingside().asRule(),
            Plans.developMinorPieces().asRule(),
            Plans.takeCenter().asRule(),

            rule(
                "Secours",
                Situations.always(),
                Actions.randomLegalMove()
            )
        );
    }
}
```

L'ordre est volontaire : ici le bot préfère mettre son roi à l'abri avant de chercher davantage d'espace central.

Un autre étudiant peut conserver les mêmes briques mais changer :

- leur ordre ;
- le profil stratégique ;
- les poids d'évaluation ;
- les ouvertures ;
- les plans ;
- les situations ;
- les actions.

Il obtient alors un bot au comportement différent sans réécrire le moteur.

## Documentation

- [Modèle objet UML et parcours d'exercices POO](docs/OBJECT_MODEL.md)
- [Bien démarrer](docs/GETTING_STARTED.md)
- [Créer et soumettre son bot de tournoi](docs/STUDENT_TOURNAMENT_BOT.md)
- [Dépannage étudiant](docs/TROUBLESHOOTING.md)
- [Checklist V1 — prêt étudiant](docs/STUDENT_READY_CHECKLIST.md)
- [Procédure de gel du framework](docs/FRAMEWORK_FREEZE.md)
- [Benchmark IRIS-Elo dynamique](docs/ELO_BENCHMARK.md)
- [Benchmark mon bot — estimation IRIS-Elo](docs/ELO_ESTIMATE.md)
- [Spécifications fonctionnelles et pédagogiques](docs/SPECIFICATIONS.md)
- [Architecture cible](docs/ARCHITECTURE.md)
- [Évaluation des positions et des coups](docs/EVALUATION.md)
- [Analyse de position](docs/ANALYSIS.md)
- [Bots de référence](docs/REFERENCE_BOTS.md)
- [Console, PGN et viewer graphique](docs/INTERFACES.md)
- [Stratégies, postures et plans multi-coups](docs/STRATEGIES.md)
- [Ouvertures et répertoires](docs/OPENINGS.md)
- [Roadmap](docs/ROADMAP.md)
- [Contribution des bots](CONTRIBUTING.md)

## État actuel

Déjà disponibles :

- modèle objet de base : couleurs, pièces, cases, coups ;
- `Analysis`, `AttackMap`, matériel, attaquants, défenseurs et pièces pendues ;
- `PositionEvaluation` notée de 0 à 10 : matériel, mobilité, centre, pions et sécurité du roi ;
- structures de pions : isolés, doublés, passés et passés protégés ;
- colonnes ouvertes et semi-ouvertes ;
- évitement des mats en un adverses ;
- recherche adversariale profondeur 2 : meilleur coup → meilleure réponse adverse → score robuste ;
- situations `captureAvailable()` et `hangingEnemyPiece()` ;
- actions de capture matérialiste et prudente ;
- notation UCI simple des coups ;
- `ChessBot`, `Situation`, `Detection`, `Action`, `Rule` ;
- candidats évalués de 0 à 10 ;
- profils stratégiques ;
- tolérance au risque ;
- livres d'ouvertures ;
- Système de Londres ;
- Défense Scandinave ;
- plans « prendre le centre », « développer les pièces mineures » et « préparer le petit roque » ;
- `RandomBot` ;
- `GreedyBot` ;
- `CautiousBot` ;
- `BerserkerBot` ;
- `GuardianBot` ;
- `TacticalBot` ;
- `PressureBot` ;
- `ChameleonBot` ;
- `PositionalBot` ;
- `LookaheadBot` ;
- `MinimaxBot` ;
- `SolidPlannerBot` comme exemple pédagogique ;
- `MatchRunner` pour faire jouer réellement deux bots avec une seed reproductible ;
- tests unitaires et CI GitHub Actions ;
- validation automatique des Pull Requests étudiantes ;
- découverte automatique des bots du package `students` dans le catalogue.

La couche d'analyse dispose désormais de la **projection après un coup**. Mat en un et fourchette sont déjà détectés par simulation réelle. Le catalogue couvre désormais mat en un, sortie d'échec, fourchette, clouage, enfilade, double échec, attaque à la découverte et surcharge d'un défenseur. Le framework sait aussi adapter une stratégie à l'ouverture, au milieu de jeu et à la finale.

## Bots de référence

Le framework fournit plusieurs styles d'adversaires : aléatoire, matérialiste, prudent, offensif, défensif, tactique, positionnel, recherche à deux plis et Minimax.

La commande suivante donne la liste exacte, y compris les bots étudiants compilés :

```bash
bash scripts/chess.sh list
```

Voir [docs/REFERENCE_BOTS.md](docs/REFERENCE_BOTS.md) pour le rôle pédagogique de chaque bot.

## Tournoi final

Le moteur de tournoi est opérationnel en toutes rondes avec alternance des couleurs, seed reproductible, isolation JVM, timeout dur, plafond mémoire, forfaits structurés et exports PGN/CSV.

Il enregistre les résultats, les coups, les temps de décision, les traces de décision et le classement final.

Les bots étudiants sont soumis par Pull Request. Leur périmètre, leurs tests et leur capacité à jouer en JVM isolée sont contrôlés automatiquement par la CI.


## Lancer un duel

Après `mvn verify`, le projet produit un runner autonome :

```text
chess-tournament/target/chess-tournament-0.1.0-SNAPSHOT-runner.jar
```

Lister les bots :

```bash
bash scripts/chess.sh list
```

Suivre une partie en console :

```bash
bash scripts/chess.sh console tactical cautious
```

Exporter un PGN :

```bash
bash scripts/chess.sh pgn tactical guardian partie.pgn
```

Ouvrir le viewer graphique :

```bash
bash scripts/chess.sh gui architect tactical
```

Voir `docs/INTERFACES.md` pour le détail.


### Lancer un tournoi

```bash
bash scripts/chess.sh tournament positional lookahead minimax --games=2
```

Avec exports :

```bash
bash scripts/chess.sh tournament tactical positional minimax --pgn=parties.pgn --csv=classement.csv
```

Voir `docs/TOURNAMENT.md`.


### Exécution isolée recommandée pour le tournoi

```bash
bash scripts/chess.sh tournament positional lookahead minimax --games=2 --isolated --timeout-ms=3000 --heap-mb=256
```

En mode isolé, chaque bot tourne dans une JVM enfant. Une décision qui dépasse le timeout provoque un forfait sans bloquer le tournoi.


### Benchmark IRIS-Elo dynamique

Les bots peuvent être calibrés empiriquement par plusieurs centaines de parties :

```bash
bash scripts/chess.sh elo-benchmark --games=4 --csv=elo-final.csv --history=elo-history.csv
```

Avec 12 bots de référence et 4 parties par paire, le benchmark joue **264 parties** avec couleurs équilibrées et met à jour l'Elo après chaque match.

Voir [docs/ELO_BENCHMARK.md](docs/ELO_BENCHMARK.md).


### Estimer l'IRIS-Elo de son bot

Un bot étudiant peut être évalué directement contre les références calibrées :

```bash
bash scripts/chess.sh elo-estimate student-deep-rabbit
```

Par défaut, il joue 4 parties contre chaque référence, avec couleurs équilibrées. Le framework retourne un IRIS-Elo estimé et un intervalle de confiance à 95 %.

Voir [docs/ELO_ESTIMATE.md](docs/ELO_ESTIMATE.md).


### Estimer toute la promotion

Après merge des bots étudiants :

```bash
bash scripts/chess.sh elo-estimate-all --games=8 --csv=iris-student-ratings.csv
```

Ou directement depuis GitHub Actions avec le workflow **Student Elo Estimates**.
