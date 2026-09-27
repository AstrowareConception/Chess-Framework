# Benchmark IRIS-Elo dynamique

Le benchmark IRIS-Elo permet de **mesurer empiriquement** la force relative des bots en les faisant jouer plusieurs centaines de parties les uns contre les autres.

Il remplace progressivement l'échelle de difficulté estimée à la main par un classement issu des résultats réels.

> IRIS-Elo reste une échelle interne à Chess Framework. Il ne possède aucune équivalence directe avec un Elo FIDE ou le niveau d'un joueur humain.

---

## 1. Principe

Tous les participants commencent avec le même Elo :

```text
1000
```

Après chaque partie, leur Elo est immédiatement mis à jour.

Pour le joueur A :

```text
E_A = 1 / (1 + 10 ^ ((R_B - R_A) / 400))

R_A' = R_A + K × (S_A - E_A)
```

avec :

- `R_A` : Elo avant la partie ;
- `E_A` : score attendu ;
- `S_A` : résultat réel — 1, 0,5 ou 0 ;
- `K` : facteur de sensibilité.

Le joueur B reçoit la mise à jour opposée.

La somme totale des Elo reste donc constante.

---

## 2. Configuration standard

Le réglage de référence est :

```text
Elo initial     = 1000
K               = 24
parties / paire = 4
max plies       = 400
seed            = 42
```

Avec les 12 bots de référence :

```text
12 bots
→ 66 paires
→ 4 parties par paire
→ 264 parties
```

Chaque paire joue :

```text
2 parties avec A blanc / B noir
2 parties avec B blanc / A noir
```

Pour une calibration plus dense :

```text
8 parties / paire = 528 parties
12 parties / paire = 792 parties
```

Le nombre doit être **pair** afin d'équilibrer parfaitement les couleurs.

---

## 3. Ordre des parties

Les confrontations équilibrées sont d'abord toutes générées.

Elles sont ensuite mélangées avec la seed du benchmark.

Ainsi :

- tous les duels prévus sont joués ;
- les couleurs restent équilibrées ;
- l'ordre des mises à jour Elo n'est pas toujours le même couple après couple ;
- une même seed reproduit exactement le même planning.

Le benchmark est donc reproductible.

---

## 4. Lancer la calibration des bots de référence

Après :

```bash
mvn verify
```

lancez :

```bash
bash scripts/chess.sh elo-benchmark \
  --games=4 \
  --seed=42 \
  --csv=elo-final.csv \
  --history=elo-history.csv
```

Sans liste explicite, `elo-benchmark` utilise **tous les bots de référence**.

---

## 5. Sous Windows

```powershell
powershell -ExecutionPolicy Bypass -File scripts/chess.ps1 \
  elo-benchmark \
  --games=4 \
  --seed=42 \
  --csv=elo-final.csv \
  --history=elo-history.csv
```

---

## 6. Tester seulement quelques bots

Pour une calibration rapide :

```bash
bash scripts/chess.sh elo-benchmark \
  random greedy tactical positional \
  --games=8
```

Il faut au moins deux bots.

---

## 7. Benchmark des étudiants

Après merge des Pull Requests étudiantes :

```bash
bash scripts/chess.sh elo-benchmark \
  --students \
  --games=8 \
  --csv=elo-etudiants.csv \
  --history=elo-etudiants-history.csv
```

Cela permet d'obtenir un classement Elo complémentaire au classement du tournoi toutes rondes.

Le classement officiel du tournoi peut conserver son barème 1 / 0,5 / 0 ; l'Elo fournit une seconde lecture de la performance.

---

## 8. Benchmark mixte

Pour inclure références **et** étudiants :

```bash
bash scripts/chess.sh elo-benchmark \
  --all \
  --games=4
```

C'est particulièrement utile pour situer les bots étudiants sur l'échelle des adversaires de référence.

---

## 9. Isolation JVM

Le benchmark peut utiliser les mêmes conditions de sécurité que le tournoi :

```bash
bash scripts/chess.sh elo-benchmark \
  --students \
  --games=4 \
  --isolated \
  --timeout-ms=2000 \
  --heap-mb=256
```

L'isolation est **automatiquement forcée** dès qu'un bot étudiant est inclus.

Pour calibrer uniquement les bots de référence fournis par le framework, l'exécution dans la JVM principale reste plus rapide.

---

## 10. Facteur K

Le défaut est :

```text
K = 24
```

Un K plus élevé fait réagir plus rapidement l'Elo :

```text
K = 32
```

Un K plus faible donne une évolution plus lente :

```text
K = 16
```

Pour comparer plusieurs campagnes, conservez le même K.

Exemple :

```bash
bash scripts/chess.sh elo-benchmark --games=8 --k=24
```

---

## 11. Elo initial

Tous les participants commencent volontairement au **même Elo** afin de ne pas injecter notre opinion initiale dans le résultat.

Défaut :

```text
1000
```

Option :

```text
--initial-elo=1000
```

Le niveau absolu importe moins que les **écarts** et l'ordre final.

---

## 12. Export final

`--csv=...` produit :

```text
rank
key
bot
author
elo
games
wins
draws
losses
forfeits
```

Exemple :

```bash
--csv=elo-final.csv
```

---

## 13. Historique match par match

`--history=...` produit une ligne par partie :

```text
game
white
black
result
white_score
white_expected
white_elo_before
black_elo_before
white_elo_after
black_elo_after
technical_draw
forfeit
```

On peut donc tracer l'évolution de chaque bot au fil des parties et observer la convergence.

---

## 14. Nulles et forfaits

Une vraie nulle vaut :

```text
0,5 / 0,5
```

Une partie arrêtée par la limite technique de demi-coups est également notée :

```text
0,5 / 0,5
```

mais reste identifiée comme `technical_draw`.

Un forfait produit une victoire normale pour l'adversaire au niveau Elo :

```text
1 / 0
```

et reste marqué par `forfeit=true` dans l'historique.

---

## 15. GitHub Actions

Le dépôt fournit le workflow manuel :

```text
.github/workflows/elo-benchmark.yml
```

Dans GitHub :

```text
Actions
→ Elo Benchmark
→ Run workflow
```

Paramètres disponibles :

- population : références / étudiants / tous ;
- parties par paire ;
- Elo initial ;
- K ;
- seed ;
- max plies ;
- isolation JVM.

Les résultats sont publiés comme artefacts :

```text
elo-final.csv
elo-history.csv
elo-benchmark.log
```

---

## 16. Interpréter une campagne

Une seule campagne de 264 parties donne déjà une hiérarchie utile.

Pour stabiliser l'échelle de référence, la procédure recommandée est :

1. première campagne : 4 parties/pair — 264 parties ;
2. examiner les résultats et les taux de nulles techniques ;
3. deuxième campagne : 8 parties/pair — 528 parties ;
4. comparer l'ordre et les écarts ;
5. si nécessaire, lancer plusieurs seeds ;
6. retenir une échelle de référence empirique.

Une hiérarchie qui reste similaire avec plusieurs seeds et davantage de parties est beaucoup plus crédible qu'une note attribuée manuellement.

---

## 17. Pourquoi ne pas démarrer avec les Elo provisoires ?

Le fichier `ReferenceEloCatalog` contient actuellement une échelle pédagogique provisoire de 400 à 1400.

Elle sert à guider les étudiants **avant calibration**.

Le benchmark dynamique repart volontairement de 1000 pour tout le monde.

Ainsi, le classement empirique n'est pas biaisé par les valeurs provisoires.

Une fois plusieurs campagnes effectuées, les valeurs statiques pourront être remplacées par les valeurs calibrées.

---

## 18. Reproductibilité

Avec :

- mêmes bots ;
- même code ;
- même seed ;
- même K ;
- même nombre de parties ;
- même limite de demi-coups ;

le planning et les résultats pseudo-aléatoires sont reproductibles.

La CI vérifie également la reproductibilité de l'historique Elo sur des bots déterministes de test.


---

## 19. Campagnes archivées

Les calibrations de référence sont archivées dans :

```text
docs/calibrations/
```

Première campagne :

```text
IRIS_ELO_REFERENCE_2026-09-27_SEED42.md
```

Elle contient 264 parties et constitue le premier point de comparaison empirique de l'échelle des bots de référence.
