# Benchmark mon bot — estimation IRIS-Elo individuelle

La commande `elo-estimate` permet à un étudiant d'estimer la force de son bot **sans recalibrer tout le pool Elo**.

Le principe est simple :

```text
bot étudiant
   ↓
parties équilibrées contre des références IRIS-Elo connues
   ↓
scores observés
   ↓
estimation statistique
   ↓
IRIS-Elo + intervalle de confiance
```

---

## 1. Commande minimale

Après :

```bash
mvn verify
```

lancez :

```bash
bash scripts/chess.sh elo-estimate student-deep-rabbit
```

Sous Windows :

```powershell
powershell -ExecutionPolicy Bypass -File scripts/chess.ps1 elo-estimate student-deep-rabbit
```

Par défaut, le bot joue contre **tous les bots de référence calibrés**.

Avec 12 références et 4 parties par référence :

```text
12 × 4 = 48 parties
```

Chaque confrontation est équilibrée :

```text
2 parties avec le bot étudiant blanc
2 parties avec le bot étudiant noir
```

---

## 2. Exemple de résultat

Le rapport ressemble à :

```text
Deep Rabbit — Alice Dupont

IRIS-Elo estimé : 1743
Intervalle 95 %  : 1658 — 1828
Parties          : 48
Score            : 31,5 / 48
```

Puis le détail par référence :

```text
Référence       IRIS-Elo   Part.   V   N   D   Score
Random              1340       4   4   0   0   100 %
Greedy              1455       4   4   0   0   100 %
Tactical            1538       4   3   1   0    87 %
Guardian            1557       4   2   1   1    62 %
Minimax             1629       4   1   1   2    37 %
Lookahead           1652       4   1   0   3    25 %
...
```

---

## 3. Pourquoi cette méthode est différente d'un benchmark Elo global

`elo-benchmark` fait évoluer les ratings de **tous les participants** après chaque partie.

`elo-estimate` fait autre chose :

- les Elo des références restent fixes ;
- seul le niveau du bot évalué est estimé ;
- les résultats contre plusieurs ancres sont combinés ;
- le résultat inclut une incertitude.

C'est donc l'outil recommandé pendant le développement d'un bot étudiant.

---

## 4. Références utilisées

Par défaut, toutes les références calibrées sont utilisées.

Échelle actuelle :

```text
Random       1340
Architect    1386
Berserker    1451
Greedy       1455
Pressure     1462
Chameleon    1497
Positional   1508
Cautious     1524
Tactical     1538
Guardian     1557
Minimax      1629
Lookahead    1652
```

Ces valeurs proviennent de la calibration empirique de 528 parties.

---

## 5. Choisir un panel plus petit

Pour aller plus vite :

```bash
bash scripts/chess.sh elo-estimate student-deep-rabbit \
  --refs=random,greedy,tactical,guardian,minimax,lookahead \
  --games=4
```

Ce panel couvre déjà une large plage de difficulté.

Un bon panel doit idéalement contenir :

- au moins une référence plus faible que le bot ;
- plusieurs références proches ;
- au moins une référence plus forte.

---

## 6. Augmenter la précision

Le paramètre :

```text
--games=N
```

doit être pair.

Exemples :

```text
4 parties / référence  = rapide
8 parties / référence  = plus précis
16 parties / référence = estimation plus stable
```

Avec les 12 références :

```text
4  × 12 = 48 parties
8  × 12 = 96 parties
16 × 12 = 192 parties
```

Plus il y a de parties, plus l'intervalle de confiance se resserre.

---

## 7. Intervalle de confiance

Le résultat n'est pas seulement :

```text
1743
```

mais par exemple :

```text
1743
IC 95 % : 1658 — 1828
```

Cela signifie que l'estimation possède encore une incertitude.

Un bot évalué sur 8 parties peut avoir une marge très large.

Le même bot évalué sur 96 ou 192 parties aura normalement un intervalle plus étroit.

Il est donc préférable de comparer :

```text
Elo estimé + incertitude
```

et pas uniquement le nombre central.

---

## 8. Pourquoi un bot peut dépasser 2000

Il n'existe aucun plafond à 1652.

1652 est simplement l'Elo actuel du meilleur bot de référence.

Si un nouveau bot domine largement Lookahead, Minimax et les autres références, son estimation peut continuer à monter.

Par exemple, un bot qui marque environ **95 % sur 100 parties contre une référence à 1650** est estimé à plus de :

```text
2100 IRIS-Elo
```

Le framework possède un test automatique vérifiant précisément ce comportement.

---

## 9. Cas d'un score parfait

Mathématiquement, un score de :

```text
100 %
```

contre toutes les références tendrait vers un Elo infini.

Pour éviter de produire un nombre absurde après un petit échantillon, l'estimateur utilise un **prior très faible**.

Défaut :

```text
centre = 1500
sigma  = 800
```

Options avancées :

```text
--prior-elo=1500
--prior-sigma=800
```

Avec suffisamment de parties, l'influence de ce prior devient très faible.

Son rôle principal est d'empêcher une estimation infinie avec :

```text
4 victoires / 4
```

ou :

```text
0 victoire / 4
```

---

## 10. Export CSV

```bash
bash scripts/chess.sh elo-estimate student-deep-rabbit \
  --games=8 \
  --csv=deep-rabbit-elo.csv
```

Le CSV contient notamment :

```text
bot_key
bot
author
estimated_elo
ci95_low
ci95_high
games
points
reference
reference_elo
wins
draws
losses
forfeits
score_rate
```

Chaque ligne correspond à une référence.

---

## 11. Isolation

Un bot étudiant est automatiquement exécuté dans une JVM isolée.

Vous pouvez donc simplement faire :

```bash
bash scripts/chess.sh elo-estimate student-deep-rabbit
```

sans ajouter `--isolated`.

Les limites peuvent être modifiées :

```bash
bash scripts/chess.sh elo-estimate student-deep-rabbit \
  --timeout-ms=3000 \
  --heap-mb=256
```

---

## 12. Workflow conseillé pendant le développement

Une boucle d'amélioration efficace :

```text
version 1 du bot
   ↓
elo-estimate --games=4
   ↓
analyse des adversaires difficiles
   ↓
modification des règles / actions / plans
   ↓
tests
   ↓
elo-estimate --games=4
   ↓
si progression intéressante
   ↓
elo-estimate --games=8 ou 16
```

Cela donne un indicateur objectif de progression.

---

## 13. Ne pas optimiser uniquement pour les références

Les références servent d'ancres, mais un étudiant ne doit pas coder :

```text
si adversaire == TacticalBot
    alors stratégie spéciale
```

Le tournoi interdit l'inspection privée ou les mécanismes de contournement.

Le but est d'améliorer une stratégie **générale**.

Le vrai test final reste le tournoi contre les autres bots étudiants.

---

## 14. Utilisation avant la Pull Request

Avant de soumettre :

```bash
mvn verify
bash scripts/chess.sh validate-students
bash scripts/chess.sh elo-estimate student-deep-rabbit --games=8
```

Cela ne garantit pas la victoire au tournoi, mais permet de vérifier que le bot :

- fonctionne ;
- respecte le timeout ;
- possède un niveau raisonnable ;
- est capable d'affronter plusieurs styles de jeu.

---

## 15. Estimation IRIS-Elo et classement du tournoi

Les deux mesures sont complémentaires.

### Estimation Elo

Répond à :

> « Quelle est approximativement la force de mon bot face à des références calibrées ? »

### Tournoi

Répond à :

> « Comment mon bot se classe face aux autres participants IRIS Nice dans cette compétition précise ? »

Un bot peut avoir un très bon Elo estimé et pourtant perdre une confrontation spécifique au tournoi.

C'est aussi ce qui rend la compétition intéressante.

---

## 16. Limite fondamentale

Un IRIS-Elo reste relatif aux ancres Chess Framework.

Même si un bot obtient :

```text
2050 IRIS-Elo
```

cela ne signifie pas :

```text
2050 FIDE
```

Pour relier un jour les deux échelles, il faudrait ajouter des ancres externes indépendamment calibrées — par exemple des moteurs UCI configurés à différents niveaux.

Cette évolution reste possible sans remettre en cause le système actuel.


---

## 17. PowerShell et `--refs`

Sous PowerShell, une valeur contenant des virgules doit être passée entre guillemets.

Utilisez :

```powershell
powershell -ExecutionPolicy Bypass -File scripts/chess.ps1 elo-estimate student-deep-rabbit "--refs=random,greedy,tactical,guardian,minimax,lookahead" --games=8
```

Sans guillemets, PowerShell peut interpréter la virgule comme un séparateur de tableau avant même que le CLI Java ne reçoive l'argument.


---

## 18. Estimer tous les bots étudiants

L'organisateur peut estimer automatiquement **tous les bots étudiants mergés** :

```bash
bash scripts/chess.sh elo-estimate-all --games=4
```

Avec export :

```bash
bash scripts/chess.sh elo-estimate-all \
  --games=8 \
  --csv=iris-student-ratings.csv
```

Chaque bot étudiant :

- est découvert automatiquement dans le catalogue ;
- est exécuté en JVM isolée ;
- joue contre le même panel de références ;
- reçoit une estimation indépendante ;
- conserve son intervalle de confiance.

Le rapport final contient :

```text
rang
bot
auteur
IRIS-Elo estimé
IC 95 %
nombre de parties
```

Exemple :

```text
1  Deep Rabbit     Alice Dupont     1743   1658 — 1828   96
2  Blue Knight     Bob Martin       1681   1590 — 1772   96
3  Solid Turtle    Chloé Bernard    1512   1425 — 1599   96
```

Cette table est utile :

- avant le tournoi pour situer les participants ;
- après les Pull Requests pour vérifier la diversité du niveau ;
- pour produire un classement d'entraînement ;
- pour comparer la force estimée au classement réel du tournoi.

Elle ne remplace pas le classement officiel du tournoi.


---

## 19. Workflow GitHub organisateur

Le dépôt fournit un workflow manuel :

```text
.github/workflows/student-elo-estimates.yml
```

Dans GitHub :

```text
Actions
→ Student Elo Estimates
→ Run workflow
```

Paramètres :

- nombre de parties par référence ;
- panel de références optionnel ;
- seed ;
- max plies ;
- timeout étudiant ;
- mémoire JVM étudiant.

Le workflow :

1. compile le framework ;
2. découvre automatiquement tous les bots étudiants mergés ;
3. les exécute en JVM isolée ;
4. estime leur IRIS-Elo ;
5. produit un CSV synthétique ;
6. publie le résultat comme artefact GitHub.

Fichiers :

```text
iris-student-ratings.csv
iris-student-ratings.log
```

C'est le mode recommandé pour produire une photographie du niveau estimé de toute la promotion avant le tournoi.
