# Calibration IRIS-Elo — campagne 1

Première calibration empirique des **12 bots de référence**.

## Paramètres

```text
Date            : 2026-09-27
Commit benchmark: e2f4da26cfb0e2ad8d55cca821b44f58c9472cf4
Workflow run    : 36320063335
Artifact        : iris-elo-reference-1
Artifact digest : sha256:d01bf718fcbc670d3fdfa731e4726dc65af4d260d0ab3c6e87ca4f6631411ea3

Bots            : 12
Paires          : 66
Parties / paire : 4
Parties totales : 264
Parties / bot   : 44

Elo initial     : 1000
K               : 24
Seed            : 42
Max plies       : 400
Isolation JVM   : non
```

Chaque paire a joué exactement deux parties avec chaque couleur.

Le planning a été mélangé de manière reproductible à partir de la seed.

---

## Classement final

| Rang | Bot | IRIS-Elo | V | N | D | Score |
|---:|---|---:|---:|---:|---:|---:|
| 1 | Lookahead Bot | **1128,5** | 20 | 24 | 0 | 72,7 % |
| 2 | Tactical Bot | **1059,5** | 13 | 28 | 3 | 61,4 % |
| 3 | Minimax Bot | **1050,9** | 11 | 33 | 0 | 62,5 % |
| 4 | Guardian | **1046,7** | 10 | 31 | 3 | 58,0 % |
| 5 | Positional Bot | **1046,4** | 11 | 29 | 4 | 58,0 % |
| 6 | Cautious Bot | **1030,3** | 8 | 34 | 2 | 56,8 % |
| 7 | Chameleon | **1009,5** | 5 | 36 | 3 | 52,3 % |
| 8 | Greedy Bot | **996,3** | 2 | 38 | 4 | 47,7 % |
| 9 | Berserker | **964,6** | 1 | 34 | 9 | 40,9 % |
| 10 | Pressure Bot | **964,2** | 2 | 34 | 8 | 43,2 % |
| 11 | The Architect | **871,1** | 0 | 23 | 21 | 26,1 % |
| 12 | Random Bot | **832,0** | 1 | 16 | 27 | 20,5 % |

Le pourcentage de score correspond à :

```text
(victoires + 0,5 × nulles) / parties
```

---

## Statistiques globales

```text
Victoires blanches : 43
Victoires noires   : 41
Nulles             : 180
dont techniques    : 5
Forfaits           : 0
```

Soit :

```text
victoires blanches : 16,3 %
victoires noires   : 15,5 %
nulles             : 68,2 %
```

L'équilibrage des couleurs fonctionne correctement : 43 victoires blanches contre 41 noires.

La très forte proportion de nulles mérite en revanche d'être surveillée sur les campagnes suivantes.

---

## Faits remarquables

### LookaheadBot

```text
20 victoires
24 nulles
0 défaite
```

LookaheadBot domine très nettement cette première campagne.

Il a notamment obtenu :

```text
4 / 4 contre Random
4 / 4 contre Architect
3 / 4 contre Tactical
3 / 4 contre Positional
3 / 4 contre Guardian
2 / 4 contre Minimax
```

Sa recherche limitée à la meilleure réponse adverse semble fournir un excellent compromis entre tactique, position et coût de calcul.

### MinimaxBot

MinimaxBot termine sans défaite :

```text
11 victoires
33 nulles
0 défaite
```

Il obtient même un score brut légèrement supérieur à TacticalBot :

```text
Minimax  : 62,5 %
Tactical : 61,4 %
```

mais son Elo final est légèrement inférieur dans cette campagne séquentielle.

Cela rappelle qu'un Elo mis à jour après chaque partie dépend légèrement de l'ordre de la campagne, surtout avec seulement 44 parties par bot.

### TacticalBot

TacticalBot réalise une performance très supérieure à notre classement provisoire manuel.

Il constitue clairement un adversaire de référence plus difficile que prévu.

### Architect et Pressure

Ces bots sont nettement moins performants que leur sophistication conceptuelle ne le laissait supposer.

C'est pédagogiquement intéressant : posséder des plans ou mettre la pression ne suffit pas si ces heuristiques ne convertissent pas l'avantage en victoire.

---

## Nulles techniques

Seulement 5 parties sur 264 ont atteint la limite de 400 demi-coups.

Les bots concernés sont principalement :

```text
Random    : 4 apparitions
Architect : 3 apparitions
Greedy    : 3 apparitions
```

Le taux élevé de nulles n'est donc **pas principalement causé par la limite technique**.

---

## Convergence observée

Le classement a déjà commencé à se structurer à mi-campagne.

Après 132 parties :

```text
1. Lookahead
2. Tactical
3. Minimax
4. Cautious
5. Guardian
6. Chameleon
7. Positional
...
12. Random
```

À 264 parties :

```text
1. Lookahead
2. Tactical
3. Minimax
4. Guardian
5. Positional
6. Cautious
7. Chameleon
...
12. Random
```

Le haut et le bas du classement sont donc déjà relativement stables, tandis que le groupe central reste serré.

---

## Comparaison avec l'échelle provisoire

La calibration empirique remet fortement en cause plusieurs valeurs attribuées à la main.

Notamment :

- Lookahead dépasse Minimax ;
- Tactical progresse fortement ;
- Guardian est plus efficace que prévu ;
- Pressure sous-performe ;
- Architect sous-performe très nettement ;
- Random reste bien le bot le plus faible.

Il serait prématuré de remplacer les valeurs statiques après une seule campagne.

---

## Décision

Une seconde campagne est lancée avec :

```text
8 parties par paire
528 parties
88 parties par bot
seed = 20260927
Elo initial = 1000
K = 24
```

Cette seconde campagne permettra de vérifier si :

- Lookahead reste premier ;
- Tactical reste au niveau de Minimax ;
- Guardian / Positional / Cautious conservent leur groupe ;
- Pressure et Architect restent réellement faibles ;
- les écarts Elo se stabilisent avec deux fois plus de parties.

Les valeurs statiques de `ReferenceEloCatalog` ne seront recalibrées qu'après comparaison des deux campagnes.
