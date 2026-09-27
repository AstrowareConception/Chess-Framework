# Calibration IRIS-Elo — campagne 2

Seconde calibration empirique des **12 bots de référence**.

Cette campagne double le nombre de parties par paire par rapport à la campagne 1 et utilise une seed indépendante.

## Paramètres

```text
Date            : 2026-09-27
Commit benchmark: da1fa6cbc49571d20229132ec37a4e0328391dc2
Workflow run    : 36320558956
Artifact        : iris-elo-reference-2
Artifact digest : sha256:ab06c8c823a666ba09f244896a1f172b7e3cd5a99e874abaae97560d90fc2242

Bots            : 12
Paires          : 66
Parties / paire : 8
Parties totales : 528
Parties / bot   : 88

Elo initial     : 1000
K               : 24
Seed            : 20260927
Max plies       : 400
Isolation JVM   : non
```

Chaque paire a joué exactement quatre parties avec chaque couleur.

---

## Classement final

| Rang | Bot | IRIS-Elo | V | N | D | Score |
|---:|---|---:|---:|---:|---:|---:|
| 1 | Lookahead Bot | **1152,2** | 36 | 52 | 0 | 70,5 % |
| 2 | Minimax Bot | **1129,2** | 28 | 60 | 0 | 65,9 % |
| 3 | Guardian | **1056,5** | 26 | 58 | 4 | 62,5 % |
| 4 | Tactical Bot | **1038,4** | 18 | 63 | 7 | 56,3 % |
| 5 | Cautious Bot | **1024,5** | 21 | 60 | 7 | 58,0 % |
| 6 | Positional Bot | **1007,8** | 16 | 60 | 12 | 52,3 % |
| 7 | Chameleon | **996,9** | 8 | 72 | 8 | 50,0 % |
| 8 | Pressure Bot | **961,9** | 3 | 73 | 12 | 44,9 % |
| 9 | Greedy Bot | **955,3** | 4 | 68 | 16 | 43,2 % |
| 10 | Berserker | **950,8** | 0 | 73 | 15 | 41,5 % |
| 11 | The Architect | **886,1** | 0 | 54 | 34 | 30,7 % |
| 12 | Random Bot | **840,4** | 0 | 43 | 45 | 24,4 % |

---

## Statistiques globales

```text
Victoires blanches : 77
Victoires noires   : 83
Nulles naturelles  : 362
Nulles techniques  : 6
Forfaits           : 0
```

Soit au total :

```text
160 parties décisives
368 nulles
```

Le taux de nulles est donc d'environ **69,7 %**.

Le biais couleur reste négligeable :

```text
77 victoires blanches
83 victoires noires
```

---

## Stabilité par rapport à la campagne 1

La corrélation de rang entre les deux campagnes est :

```text
Spearman : 0,951
Kendall  : 0,848
```

La hiérarchie générale est donc très stable.

### Positions identiques

```text
Lookahead  : 1er → 1er
Chameleon  : 7e → 7e
Architect  : 11e → 11e
Random     : 12e → 12e
```

### Évolutions limitées

```text
Minimax    : 3e → 2e
Guardian   : 4e → 3e
Tactical   : 2e → 4e
Cautious   : 6e → 5e
Positional : 5e → 6e
Pressure   : 10e → 8e
Greedy     : 8e → 9e
Berserker  : 9e → 10e
```

Aucun bot ne change radicalement de zone du classement.

---

## Faits remarquables

### LookaheadBot

Lookahead reste premier et termine une seconde fois **invaincu** :

```text
36 victoires
52 nulles
0 défaite
```

Son compromis « meilleur coup + meilleure réponse adverse » apparaît extrêmement efficace dans ce framework.

### MinimaxBot

Minimax termine également invaincu :

```text
28 victoires
60 nulles
0 défaite
```

Avec davantage de parties, sa robustesse ressort mieux et il passe devant Tactical.

### Guardian

Guardian confirme une performance supérieure à ce qu'indiquait notre première échelle manuelle.

Son profil défensif convertit suffisamment bien les erreurs adverses pour atteindre la 3e place.

### Groupe intermédiaire

```text
Tactical   1038
Cautious   1024
Positional 1008
Chameleon   997
```

Ces bots constituent un excellent groupe d'entraînement de difficulté intermédiaire.

### Bas de tableau

```text
Architect 886
Random    840
```

Ces deux positions sont très stables entre les deux campagnes.

---

## Nulles techniques

Seulement 6 parties sur 528 atteignent la limite de 400 demi-coups.

Les apparitions principales sont :

```text
Architect : 5
Random    : 4
Greedy    : 3
```

Le taux de nulles élevé ne vient donc pas de la limite technique.

---

## Décision de calibration

Cette seconde campagne est retenue comme **base de l'échelle IRIS-Elo de référence**.

Les valeurs statiques affichées par la commande `ratings` sont recalibrées à partir des Elo finaux arrondis à l'entier le plus proche :

```text
Lookahead  1152
Minimax    1129
Guardian   1057
Tactical   1038
Cautious   1024
Positional 1008
Chameleon   997
Pressure    962
Greedy      955
Berserker   951
Architect   886
Random      840
```

Ces valeurs restent relatives au pool de bots Chess Framework et n'ont aucune équivalence FIDE.

La campagne 1 est conservée comme contrôle de stabilité.

De futures campagnes pourront faire évoluer cette échelle si le code des bots de référence change.
