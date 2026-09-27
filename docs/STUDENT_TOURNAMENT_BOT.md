# Créer et soumettre son bot de tournoi

Ce guide décrit le workflow complet d'une **soumission étudiante** : création du bot, tests, validation locale, Pull Request et intégration automatique au tournoi.

Le règlement officiel du tournoi IRIS Nice est dans :

```text
docs/IRIS_NICE_TOURNAMENT_RULES.md
```

L'objectif est qu'une soumission valide ne nécessite **aucune modification manuelle du framework** après son merge.

Les exemples de lancement utilisent Bash. Sous Windows, remplacez `bash scripts/chess.sh` par `powershell -ExecutionPolicy Bypass -File scripts/chess.ps1`.

---

## 1. Forker le dépôt et créer sa branche

Si vous n'avez pas de droits d'écriture sur le dépôt du cours, cliquez sur **Fork** depuis [le dépôt principal](https://github.com/AstrowareConception/Chess-Framework), puis clonez **votre fork**. Remplacez `ALICE-GITHUB` par votre identifiant GitHub :

```bash
git clone https://github.com/ALICE-GITHUB/Chess-Framework.git
cd Chess-Framework
git switch -c bot/alice-deep-rabbit
```

Si vous travaillez déjà dans votre clone, placez-vous sur `main`, exécutez `git pull`, puis créez la branche. Les étudiants disposant de droits d'écriture peuvent cloner directement le dépôt principal. Une Pull Request de bot doit rester consacrée à **un seul bot**.

---

## 2. Emplacement imposé

Votre code de production doit être placé sous :

```text
chess-bots/src/main/java/fr/astroware/chess/bots/students/
```

Exemple :

```text
chess-bots/
└── src/
    ├── main/java/fr/astroware/chess/bots/students/
    │   ├── DeepRabbitBot.java
    │   └── RabbitEvaluator.java
    └── test/java/fr/astroware/chess/bots/students/
        └── DeepRabbitBotTest.java
```

Le package Java doit être exactement :

```java
package fr.astroware.chess.bots.students;
```

Vous pouvez ajouter des classes auxiliaires dans ce package, mais **une seule classe de la Pull Request doit étendre `ChessBot`**.

### Générateur recommandé

Le dépôt fournit un générateur qui crée **la classe et son test** au bon endroit.

Linux/macOS/Git Bash :

```bash
bash scripts/new-student-bot.sh DeepRabbitBot "Alice Dupont" "Deep Rabbit" "Bot positionnel et prudent."
```

Windows PowerShell :

```powershell
powershell -ExecutionPolicy Bypass -File scripts/new-student-bot.ps1 DeepRabbitBot "Alice Dupont" "Deep Rabbit" "Bot positionnel et prudent."
```

Vous pouvez ensuite modifier les règles générées sans toucher au framework.

---

## 3. Squelette minimal

```java
package fr.astroware.chess.bots.students;

import fr.astroware.chess.bot.action.Actions;
import fr.astroware.chess.bot.api.BotMetadata;
import fr.astroware.chess.bot.api.ChessBot;
import fr.astroware.chess.bot.rule.Rule;
import fr.astroware.chess.bot.situation.Situations;

import java.util.List;

public final class DeepRabbitBot extends ChessBot {

    public DeepRabbitBot() {
    }

    @Override
    public BotMetadata metadata() {
        return new BotMetadata(
            "Deep Rabbit",
            "Alice Dupont",
            "Bot solide qui protège son roi avant de chercher l'initiative."
        );
    }

    @Override
    protected List<Rule<?>> rules() {
        return List.of(
            rule(
                "Secours",
                Situations.always(),
                Actions.randomLegalMove()
            )
        );
    }
}
```

Le constructeur public sans argument est obligatoire : le tournoi doit pouvoir créer une nouvelle instance à chaque partie.

---

## 4. Métadonnées obligatoires

Les trois champs doivent être renseignés :

```java
new BotMetadata(
    "Nom du bot",
    "Prénom Nom de l'étudiant",
    "Description de la stratégie"
);
```

Le nom du bot doit être unique :

- parmi les bots de référence ;
- parmi toutes les autres soumissions étudiantes.

La CI vérifie cette unicité.

---

## 5. Construire sa stratégie

La structure recommandée reste :

```text
Situation
    ↓
Detection
    ↓
Action
    ↓
EvaluatedMove
    ↓
choix stratégique
```

Commencez par les briques existantes avant de créer les vôtres.

Exemple :

```java
@Override
protected List<Rule<?>> rules() {
    return List.of(
        rule(
            "Mater immédiatement",
            Situations.mateInOne(),
            Actions.playMateInOne()
        ),
        rule(
            "Sortir d'échec",
            Situations.inCheck(),
            Actions.bestCheckEscape()
        ),
        rule(
            "Exploiter une capture",
            Situations.captureAvailable(),
            Actions.bestMaterialCapture()
        ),
        rule(
            "Secours",
            Situations.always(),
            Actions.randomLegalMove()
        )
    );
}
```

Pour aller plus loin, consultez :

- `docs/ANALYSIS.md` ;
- `docs/STRATEGIES.md` ;
- `docs/EVALUATION.md` ;
- `docs/REFERENCE_BOTS.md`.

---

## 6. Ajouter au moins un test

Une soumission sans test est refusée automatiquement.

Exemple :

```java
package fr.astroware.chess.bots.students;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeepRabbitBotTest {

    @Test
    void exposesTournamentIdentity() {
        var metadata =
            new DeepRabbitBot().metadata();

        assertEquals(
            "Deep Rabbit",
            metadata.botName()
        );
    }
}
```

Une situation ou une action personnalisée significative doit également être couverte par des cas positifs et négatifs.

---

## 7. Périmètre d'une Pull Request de bot

Dès qu'une Pull Request contient un bot étudiant, elle ne peut modifier que :

```text
chess-bots/src/main/java/fr/astroware/chess/bots/students/
chess-bots/src/test/java/fr/astroware/chess/bots/students/
```

Il est donc interdit dans cette même PR de modifier :

- `chess-core` ;
- `chess-bot-sdk` ;
- `chess-tournament` ;
- les bots de référence ;
- un `pom.xml` ;
- les workflows GitHub.

Cette règle garantit que tous les concurrents utilisent exactement le même framework.

Si une évolution du framework est nécessaire, elle doit faire l'objet d'une contribution séparée **avant le gel du tournoi**.

---

## 8. API interdites dans une soumission

Un bot de tournoi n'a pas besoin d'accéder à la machine qui l'exécute.

La validation de PR refuse notamment les usages directs évidents de :

- `java.net.*` ;
- `java.nio.file.*` ;
- accès fichiers via les classes `java.io.File...` ;
- réflexion `java.lang.reflect.*` ;
- `ProcessBuilder` ;
- `Runtime.getRuntime(...)` ;
- `ClassLoader` ;
- `System.exit(...)`.

Comme la PR ne peut pas modifier un `pom.xml`, elle ne peut pas non plus ajouter sa propre dépendance Maven.

Ce contrôle statique complète l'isolation du tournoi ; il ne remplace pas la séparation en JVM.

---

## 9. Vérifier localement

À la racine du dépôt :

```bash
mvn verify
```

Cette commande doit être verte. Elle compile les quatre modules, lance tous les tests et produit également :

```text
chess-tournament/target/chess-tournament-0.1.0-SNAPSHOT-runner.jar
```

Validez ensuite réellement les bots étudiants compilés :

```bash
bash scripts/chess.sh validate-students
```

Le validateur contrôle réellement les classes compilées :

- classe `public` et non abstraite ;
- constructeur public sans argument ;
- métadonnées ;
- unicité du nom ;
- instanciation en JVM isolée ;
- smoke-test avec les Blancs ;
- smoke-test avec les Noirs ;
- absence de forfait, timeout ou coup illégal durant ces tests.

---

## 10. Le bot apparaît automatiquement dans le catalogue

Après compilation, les bots du package `students` sont découverts automatiquement.

La clé CLI est générée depuis le nom de classe :

```text
DeepRabbitBot
    ↓
student-deep-rabbit
```

Vous pouvez vérifier :

```bash
bash scripts/chess.sh list
```

Aucune modification de `BotCatalog` n'est demandée à l'étudiant.

---

## 11. Tester un duel dans les conditions du tournoi

Le mode recommandé est l'isolation JVM :

```bash
bash scripts/chess.sh console student-deep-rabbit tactical --isolated
```

Avec des limites explicites :

```bash
bash scripts/chess.sh console student-deep-rabbit minimax --isolated --timeout-ms=3000 --heap-mb=256
```

Chaque bot tourne alors dans sa propre JVM.

L'isolation est automatique pour un bot étudiant : le flag `--isolated` reste recommandé dans les exemples pour rendre la condition de tournoi visible, mais le framework force cette protection si un bot `student-*` est détecté.

Une décision qui dépasse le timeout entraîne un forfait au lieu de bloquer le tournoi.

---

## 11 bis. Choisir un adversaire avec l'IRIS-Elo

Les bots de référence sont classés sur une échelle pédagogique appelée **IRIS-Elo**.

```bash
bash scripts/chess.sh ratings
```

Sous Windows :

```powershell
powershell -ExecutionPolicy Bypass -File scripts/chess.ps1 ratings
```

Cette valeur ne correspond pas à un Elo FIDE. Elle sert uniquement à choisir une difficulté progressive.

Exemple de progression calibrée :

```text
Random      1340
Architect   1386
Greedy      1455
Tactical    1538
Guardian    1557
Minimax     1629
Lookahead   1652
```

Une bonne méthode de travail consiste à faire progresser son bot dans cette échelle plutôt que de ne tester que contre RandomBot.

---

## 11 ter. Estimer son propre IRIS-Elo

Après avoir validé votre bot :

```bash
bash scripts/chess.sh elo-estimate student-deep-rabbit
```

Le framework joue automatiquement contre les références calibrées et retourne :

- l'IRIS-Elo estimé ;
- un intervalle de confiance à 95 % ;
- le score obtenu contre chaque référence ;
- les forfaits éventuels.

Pour une estimation plus stable :

```bash
bash scripts/chess.sh elo-estimate student-deep-rabbit --games=8
```

Voir `docs/ELO_ESTIMATE.md`.

---

## 12. Faire un mini-tournoi local

```bash
bash scripts/chess.sh tournament student-deep-rabbit random tactical --games=2 --isolated
```

Vous obtenez :

- alternance des couleurs ;
- score 1 / 0,5 / 0 ;
- classement ;
- temps moyen de décision ;
- compteur de forfaits ;
- traces d'incidents éventuels.

Vous pouvez également exporter :

```bash
bash scripts/chess.sh tournament student-deep-rabbit random tactical --games=2 --isolated --pgn=parties.pgn --csv=classement.csv
```

---

## 13. Ouvrir la Pull Request

Avant le push final :

```bash
mvn verify
bash scripts/chess.sh validate-students
git status
git add chess-bots/src/main/java/fr/astroware/chess/bots/students/ chess-bots/src/test/java/fr/astroware/chess/bots/students/
git commit -m "feat: add DeepRabbitBot"
```

Vérifiez avec `git status` que votre PR ne contient que votre package `students` et ses tests. Si Git affiche d'autres modifications, ne les ajoutez pas à ce commit. Sur PowerShell, utilisez `powershell -ExecutionPolicy Bypass -File scripts/chess.ps1 validate-students`.

Puis :

```bash
git push -u origin bot/alice-deep-rabbit
```

Sur la page GitHub de votre fork, ouvrez une Pull Request avec **base repository** `AstrowareConception/Chess-Framework`, branche `main`, et **head repository** votre fork, branche `bot/alice-deep-rabbit`. Vérifiez les fichiers affichés dans « Files changed » et complétez le template :

- nom du bot ;
- prénom et nom réels de l'étudiant ;
- stratégie ;
- règles principales ;
- extensions personnelles ;
- tests ;
- limites connues.

---

## 14. Ce que la CI vérifie

Une Pull Request de bot passe deux familles de contrôles.

### Périmètre Git

```text
un seul ChessBot
+ au moins un test
+ package students
+ aucun fichier du framework modifié
+ aucune API interdite évidente
```

### Build et validation exécutable

```text
mvn verify
    ↓
compilation
    ↓
tests JUnit
    ↓
StudentSubmissionValidator
    ↓
JVM isolée comme Blanc
    ↓
JVM isolée comme Noir
```

Une soumission qui ne respecte pas le contrat n'atteint donc pas le tournoi final.

---

## 15. Après le merge

Le bot est automatiquement découvert par `BotCatalog`.

Il devient disponible dans :

```text
list
console
pgn
gui
tournament
tournament --students
tournament --all
```

Le jour du tournoi IRIS Nice, `tournament --students` sélectionne automatiquement uniquement les bots étudiants mergés et validés.

Il n'y a pas de registre manuel à maintenir après chaque merge.

---

## 16. Checklist avant rendu

- [ ] Ma classe est dans `fr.astroware.chess.bots.students`.
- [ ] Son fichier se termine par `Bot.java`.
- [ ] Elle étend `ChessBot`.
- [ ] Elle possède un constructeur public sans argument.
- [ ] `metadata()` contient nom, auteur et description.
- [ ] Le nom de mon bot est unique.
- [ ] J'ai au moins un test étudiant.
- [ ] Je n'ai modifié aucun fichier du framework.
- [ ] Je n'utilise ni réseau, ni disque, ni processus, ni réflexion.
- [ ] `mvn verify` est vert.
- [ ] `validate-students` valide mon bot.
- [ ] J'ai testé au moins un duel en `--isolated`.
- [ ] Ma Pull Request décrit clairement ma stratégie.

À ce stade, votre bot est prêt à rejoindre le tournoi.


---

## 17. En cas de problème

Consultez :

```text
docs/TROUBLESHOOTING.md
```

Vous y trouverez les procédures pour Java/Maven, PowerShell, un bot non détecté, les timeouts, les refus de CI et le débogage des positions FEN.
