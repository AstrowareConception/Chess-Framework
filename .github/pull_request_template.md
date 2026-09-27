## Type de contribution

- [ ] Bot étudiant IRIS Nice
- [ ] Fonctionnalité du framework
- [ ] Correction
- [ ] Documentation

## Identité du participant — pour une PR de bot

**Prénom NOM :**

**Classe / promotion :** _(information uniquement ; toutes les classes IRIS Nice peuvent participer)_

**Nom du bot :**

- [ ] Je confirme être étudiant à **IRIS Nice**.
- [ ] Je confirme qu'il s'agit de mon **unique bot** inscrit au tournoi.
- [ ] Le champ `authorName` de mon bot contient mon prénom et mon nom réels.

> Une PR mergée par l'organisateur constitue l'inscription du bot au tournoi.
> La date limite de soumission sera communiquée ultérieurement.

## Stratégie

Décrivez en quelques lignes la personnalité et les priorités de votre bot.

## Règles principales

Indiquez les principales situations reconnues et les actions associées.

## Extensions personnelles

Listez les situations, actions, évaluateurs ou autres composants que vous avez ajoutés.

## Tests et contrat tournoi

- [ ] `mvn verify` passe localement.
- [ ] `validate-students` valide mon bot.
- [ ] J'ai au moins un test dans le package `fr.astroware.chess.bots.students`.
- [ ] Mes nouvelles situations importantes ont des tests positifs et négatifs.
- [ ] Ma PR contient exactement un bot étudiant.
- [ ] Ma PR ne modifie que le package `students` et ses tests.
- [ ] Mon bot possède un constructeur public sans argument.
- [ ] Mon bot ne modifie pas le moteur, le SDK, le tournoi ou les `pom.xml`.
- [ ] Mon bot n'utilise ni réseau, ni disque, ni processus, ni réflexion.
- [ ] J'ai testé mon bot avec `--isolated`.
- [ ] J'ai testé mon bot contre plusieurs bots de référence.

## Limites connues

Décrivez les comportements dont vous savez qu'ils peuvent être améliorés.

## Règlement

J'ai lu :

```text
docs/IRIS_NICE_TOURNAMENT_RULES.md
```

- [ ] J'accepte le règlement du tournoi IRIS Nice.
