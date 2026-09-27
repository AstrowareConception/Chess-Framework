# Benchmark IRIS-Elo dynamique

Le benchmark IRIS-Elo permet de **mesurer empiriquement** la force relative des bots en les faisant jouer plusieurs centaines de parties les uns contre les autres.

Il remplace progressivement l'échelle de difficulté estimée à la main par un classement issu des résultats réels.

> IRIS-Elo reste une échelle interne à Chess Framework. Il ne possède aucune équivalence directe avec un Elo FIDE ou le niveau d'un joueur humain.

---

## 1. Principe

Par défaut, un bot sans historique commence à :

```text
1500
```

Ce choix ne modifie pas les écarts ni les probabilités Elo : ajouter la même constante à tous les ratings est une simple translation de l'échelle.

L'intérêt est uniquement de disposer d'une échelle plus lisible, laissant naturellement de la place à des bots très supérieurs au-dessus de 2000.

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
Elo initial     = 1500
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

## 4 bis. Continuer une campagne précédente

Le point le plus important pour faire **converger réellement** l'échelle est de ne pas repartir de zéro à chaque campagne.

Le classement final d'un benchmark peut être réutilisé directement comme état initial du suivant :

```bash
bash scripts/chess.sh elo-benchmark \
  --ratings-in=elo-final.csv \
  --games=8 \
  --seed=20261001 \
  --csv=elo-next.csv \
  --history=elo-next-history.csv
```

Le fichier `elo-final.csv` contient déjà les colonnes `key` et `elo` nécessaires.

Comportement :

- un bot présent dans le fichier reprend son Elo exact ;
- un nouveau bot absent du fichier démarre à `--initial-elo`, soit 1500 par défaut ;
- les mises à jour reprennent ensuite normalement après chaque partie.

Cela permet de faire :

```text
campagne 1
→ campagne 2 avec les Elo de la campagne 1
→ campagne 3 avec les Elo de la campagne 2
→ ...
```

C'est la méthode recommandée pour laisser les écarts continuer à se développer au lieu de réinitialiser le pool.

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

## 6 bis. Pourquoi augmenter K ne crée pas un « vrai 2000 »

Le facteur `K` contrôle la **vitesse** des variations, pas l'origine ni la signification de l'échelle.

Un K élevé :

- fait monter ou descendre plus vite ;
- converge plus rapidement au début ;
- augmente aussi la volatilité.

Un K plus faible :

- évolue plus lentement ;
- produit une échelle plus stable ;
- demande davantage de parties.

Augmenter artificiellement K pour obtenir des nombres plus grands serait donc une mauvaise calibration.

La bonne combinaison est :

1. une origine lisible — 1500 par défaut ;
2. suffisamment de parties ;
3. plusieurs seeds ;
4. conservation des Elo entre campagnes ;
5. même formule Elo et même K pour rendre les campagnes comparables.

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
--initial-elo=1500
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

## 18 bis. Ce que signifie un bot à 2000 IRIS-Elo

Avec l'origine actuelle à 1500, un bot peut tout à fait dépasser 2000 si ses résultats justifient un écart suffisamment grand avec le pool.

Par exemple, un écart Elo de 400 points correspond à un score attendu d'environ :

```text
91 %
```

Un bot autour de 2050 face à un adversaire à 1650 est donc censé marquer environ neuf points sur dix à long terme.

Cela donne des écarts numériquement significatifs **sans modifier la formule Elo**.

En revanche, 2000 IRIS-Elo ne signifie toujours pas automatiquement « 2000 FIDE ».

Pour créer un pont vers une échelle FIDE, il faudrait introduire des **ancres externes** : moteurs UCI ou adversaires dont le niveau a été calibré indépendamment. C'est une extension possible du framework.

---

## 19. Campagnes archivées

Les calibrations de référence sont archivées dans :

```text
docs/calibrations/
```

Campagnes :

```text
IRIS_ELO_REFERENCE_2026-09-27_SEED42.md
IRIS_ELO_REFERENCE_2026-09-27_SEED20260927.md
```

La première contient 264 parties. La seconde contient 528 parties et sert de base à l'échelle IRIS-Elo actuellement affichée par `ratings`.
