# Règlement — Tournoi Chess Framework — IRIS Nice

Ce document définit le cadre du tournoi organisé à **IRIS Nice** avec Chess Framework.

La date de clôture des soumissions et la date du tournoi seront communiquées séparément par l'organisateur.

---

## 1. Participants autorisés

Le tournoi est réservé aux **étudiants de l'école IRIS Nice**.

Toutes les classes peuvent participer : le niveau, l'année ou la filière ne constituent pas un critère d'exclusion.

La participation est **individuelle**.

Chaque participant peut inscrire **un seul bot**.

Le champ :

```java
metadata().authorName()
```

doit contenir le **prénom et le nom réels de l'étudiant**.

Le validateur refuse automatiquement deux bots portant le même auteur.

L'éligibilité IRIS Nice est contrôlée par l'organisateur lors de la revue de la Pull Request.

---

## 2. Une Pull Request = une inscription

L'inscription au tournoi se fait par **Pull Request GitHub** vers `main`.

Le workflow est :

```text
étudiant IRIS Nice
    ↓
branche personnelle
    ↓
un bot + ses tests
    ↓
Pull Request
    ↓
CI automatique
    ↓
revue de l'organisateur
    ↓
merge
    ↓
bot officiellement inscrit
```

Une PR non mergée avant la date limite ne fait pas partie du tournoi.

La date de clôture sera annoncée ultérieurement.

---

## 3. Un seul bot par participant

Une soumission étudiante doit contenir exactement **une classe étendant `ChessBot`**.

Un même auteur ne peut avoir qu'un seul bot mergé dans le package étudiant.

Il est interdit de contourner cette règle par :

- plusieurs comptes GitHub ;
- plusieurs variantes du nom de l'auteur ;
- une seconde PR avec un autre bot.

En cas de doute, l'organisateur tranche à partir de l'identité réelle de l'étudiant IRIS Nice.

---

## 4. Emplacement obligatoire

Le bot doit être créé dans :

```text
chess-bots/src/main/java/fr/astroware/chess/bots/students/
```

Les tests doivent être placés dans :

```text
chess-bots/src/test/java/fr/astroware/chess/bots/students/
```

Le package Java doit être :

```java
package fr.astroware.chess.bots.students;
```

Le générateur fourni est recommandé :

Linux/macOS/Git Bash :

```bash
bash scripts/new-student-bot.sh DeepRabbitBot "Alice Dupont" "Deep Rabbit"
```

Windows PowerShell :

```powershell
powershell -ExecutionPolicy Bypass -File scripts/new-student-bot.ps1 DeepRabbitBot "Alice Dupont" "Deep Rabbit"
```

---

## 5. Ce qu'une PR de bot peut modifier

Une Pull Request de tournoi peut modifier uniquement :

```text
chess-bots/src/main/java/fr/astroware/chess/bots/students/
chess-bots/src/test/java/fr/astroware/chess/bots/students/
```

Elle ne peut pas modifier :

- le moteur d'échecs ;
- le SDK ;
- le tournoi ;
- les bots de référence ;
- les POM Maven ;
- la CI ;
- la documentation du framework.

Cette règle garantit que tous les concurrents jouent avec le même environnement.

---

## 6. Tests obligatoires

Chaque bot doit posséder au moins un test JUnit.

Avant d'ouvrir la Pull Request :

```bash
mvn verify
bash scripts/chess.sh validate-students
```

Sous Windows :

```powershell
mvn verify
powershell -ExecutionPolicy Bypass -File scripts/chess.ps1 validate-students
```

La CI rejoue automatiquement ces validations.

---

## 7. Identité du bot

Chaque bot fournit :

```java
new BotMetadata(
    "Nom du bot",
    "Prénom Nom",
    "Description de la stratégie"
);
```

Le nom du bot doit être unique.

L'auteur doit être le participant réel.

Les pseudonymes sont autorisés pour le **nom du bot**, pas pour l'identité de l'auteur.

---

## 8. Règles de sécurité et d'équité

Un bot doit décider uniquement à partir des informations fournies par le framework.

Sont interdits dans une soumission :

- accès réseau ;
- accès direct au système de fichiers ;
- lancement de processus ;
- réflexion Java ;
- chargement dynamique de classes ;
- arrêt volontaire de la JVM ;
- modification du framework ;
- ajout de dépendances Maven.

Les contrôles automatiques de PR détectent les usages évidents de ces API.

Pendant le tournoi, chaque bot étudiant est exécuté dans une **JVM enfant isolée** avec timeout et plafond mémoire.

L'isolation est forcée automatiquement dès que le CLI détecte un participant `student-*` ou le mode `--students`. Le flag `--isolated` peut toujours être écrit explicitement dans la commande officielle, mais son oubli ne désactive pas cette protection.

---

## 9. Forfaits techniques

Un bot perd la partie par forfait s'il provoque notamment :

- une exception non gérée ;
- un timeout ;
- l'arrêt de son processus ;
- une erreur de protocole ;
- un coup illégal.

Le tournoi continue après le forfait.

Les incidents sont conservés dans les résultats et exports.

---

## 10. Entraînement avant le tournoi

Le framework fournit un ensemble d'adversaires de référence très contrastés :

- RandomBot ;
- GreedyBot ;
- BerserkerBot ;
- CautiousBot ;
- GuardianBot ;
- SolidPlannerBot / Architect ;
- TacticalBot ;
- PressureBot ;
- ChameleonBot ;
- PositionalBot ;
- LookaheadBot ;
- MinimaxBot.

Liste exacte :

```bash
bash scripts/chess.sh list
```

Sous Windows :

```powershell
powershell -ExecutionPolicy Bypass -File scripts/chess.ps1 list
```

Un étudiant peut tester son bot contre n'importe lequel de ces adversaires.

Exemple :

```bash
bash scripts/chess.sh console student-deep-rabbit tactical --isolated
```

---

## 11. IRIS-Elo des bots de référence

Les bots de référence disposent d'un **IRIS-Elo provisoire**.

Cette note est une échelle pédagogique interne permettant de choisir un adversaire d'entraînement.

Elle **n'est pas un Elo FIDE** et ne représente pas le niveau équivalent d'un joueur humain.

Afficher l'échelle :

```bash
bash scripts/chess.sh ratings
```

Exemples de calibration actuelle :

```text
Random      1340
Architect   1386
Greedy      1455
Tactical    1538
Guardian    1557
Minimax     1629
Lookahead   1652
```

Ces valeurs proviennent d'une campagne de **528 parties** puis d'une translation globale de +500 points, mathématiquement neutre. Elles peuvent évoluer si le code des bots de référence change ou après une nouvelle calibration.

Les campagnes Elo peuvent être chaînées avec `--ratings-in` afin de conserver l'évolution du classement d'une campagne à la suivante.

---

## 11 bis. Benchmark Elo dynamique

Le framework peut également calculer un IRIS-Elo à partir de résultats réels.

Pour les bots de référence :

```bash
bash scripts/chess.sh elo-benchmark --games=4
```

Avec 12 bots, cela représente 264 parties.

Pour situer les bots étudiants par rapport aux références après clôture :

```bash
bash scripts/chess.sh elo-benchmark --all --games=4 --isolated
```

Le classement Elo est complémentaire au classement officiel du tournoi.

Voir `docs/ELO_BENCHMARK.md`.

---

## 12. Tournoi officiel

Une fois toutes les PR autorisées mergées, le logiciel découvre automatiquement les bots étudiants.

Le tournoi officiel se lance avec :

```bash
bash scripts/chess.sh tournament --students --isolated
```

Cette commande **n'inclut pas les bots de référence**.

Elle utilise uniquement les bots étudiants présents et validés dans le dépôt.

Les paramètres définitifs seront publiés avant le tournoi :

- nombre de parties par paire ;
- seed ;
- nombre maximal de demi-coups ;
- timeout par décision ;
- mémoire maximale ;
- date et heure du tournoi.

---

## 13. Format

Le format actuel est un **toutes rondes**.

Chaque paire de participants joue le nombre de parties configuré.

Les couleurs alternent automatiquement.

Barème :

```text
victoire = 1 point
nulle    = 0,5 point
défaite  = 0 point
```

Une nulle technique liée à la limite de demi-coups rapporte également 0,5 point à chaque bot mais reste comptabilisée séparément.

---

## 14. Départages

Le classement est déterministe.

Les critères actuels sont :

1. nombre de points ;
2. nombre de victoires ;
3. moins de nulles techniques ;
4. nom du bot.

Le temps de calcul est affiché comme statistique mais **n'est pas un critère de départage**.

---

## 15. Résultats et transparence

Le framework peut produire :

- classement console ;
- PGN multi-parties ;
- CSV du classement ;
- temps moyen de décision ;
- nombre de forfaits ;
- traces des décisions.

Exemple :

```bash
bash scripts/chess.sh tournament --students --games=4 --isolated --pgn=iris-nice.pgn --csv=iris-nice.csv
```

Les exports permettent de rejouer et analyser les parties après le tournoi.

---

## 16. Validation par l'organisateur

Une CI verte ne garantit pas automatiquement l'inscription.

La Pull Request doit être **revue et validée par l'organisateur**.

L'organisateur peut refuser une soumission qui :

- n'appartient pas à un étudiant IRIS Nice ;
- tente de contourner le règlement ;
- utilise une identité incorrecte ;
- exploite manifestement une faille du framework ;
- nuit au bon déroulement ou à l'équité du tournoi.

Le merge de la Pull Request constitue la validation technique et administrative de l'inscription.

---

## 17. Date limite

La date limite de soumission sera annoncée ultérieurement.

Après cette échéance :

- aucune nouvelle inscription n'est acceptée ;
- aucune modification de bot n'est acceptée, sauf décision exceptionnelle de l'organisateur ;
- le commit/tag du tournoi est figé.

La procédure de gel est décrite dans :

```text
docs/FRAMEWORK_FREEZE.md
```

---

## 18. Esprit du tournoi

L'objectif n'est pas uniquement de gagner.

Le tournoi doit aussi faire travailler :

- Java et POO ;
- architecture et composition ;
- algorithmes ;
- heuristiques ;
- tests ;
- Git ;
- Pull Requests ;
- CI ;
- lecture et respect d'un contrat logiciel ;
- analyse après partie.

Le bot doit rester compréhensible et défendable par son auteur.
