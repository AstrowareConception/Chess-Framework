package fr.astroware.chess.tournament.rating;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Échelle IRIS-Elo calibrée des bots de référence.
 *
 * <p>Les écarts proviennent de la seconde campagne de calibration
 * empirique : 528 parties, 8 parties par paire, K=24, seed 20260927.
 * La campagne brute a été jouée autour de 1000 puis toute l'échelle a
 * été translatée de +500 points. Une translation Elo ne modifie ni les
 * écarts, ni les probabilités attendues. Elles restent relatives au pool
 * Chess Framework et n'ont aucune équivalence FIDE.</p>
 */
public final class ReferenceEloCatalog {

    private static final Map<String, ReferenceElo>
        RATINGS = createRatings();

    private ReferenceEloCatalog() {
    }

    public static Map<String, ReferenceElo> all() {
        return RATINGS;
    }

    public static Optional<ReferenceElo> find(
        String botKey
    ) {
        if (botKey == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(
            RATINGS.get(
                botKey.trim()
                    .toLowerCase(
                        java.util.Locale.ROOT
                    )
            )
        );
    }

    private static Map<String, ReferenceElo>
        createRatings() {

        Map<String, ReferenceElo> ratings =
            new LinkedHashMap<>();

        put(
            ratings,
            "random",
            1_340,
            "Découverte",
            "Joue légalement sans raisonnement stratégique."
        );

        put(
            ratings,
            "greedy",
            1_455,
            "Débutant",
            "Comprend le gain matériel immédiat mais ignore les conséquences."
        );

        put(
            ratings,
            "berserker",
            1_451,
            "Agressif",
            "Cherche l'initiative et accepte beaucoup de risque."
        );

        put(
            ratings,
            "cautious",
            1_524,
            "Prudent",
            "Évite mieux les prises risquées et protège son roi."
        );

        put(
            ratings,
            "guardian",
            1_557,
            "Défensif",
            "Utilise la projection pour sauver les pièces menacées."
        );

        put(
            ratings,
            "architect",
            1_386,
            "Planificateur",
            "Combine ouvertures, développement, centre et roque."
        );

        put(
            ratings,
            "tactical",
            1_538,
            "Tactique",
            "Reconnaît plusieurs motifs tactiques et les priorise."
        );

        put(
            ratings,
            "pressure",
            1_462,
            "Pression",
            "Cherche clouages, surcharge, enfilades et contraintes."
        );

        put(
            ratings,
            "chameleon",
            1_497,
            "Adaptatif",
            "Change de profil et de priorités selon la phase de jeu."
        );

        put(
            ratings,
            "positional",
            1_508,
            "Positionnel",
            "Compare globalement matériel, mobilité, centre, pions et roi."
        );

        put(
            ratings,
            "lookahead",
            1_652,
            "Anticipation",
            "Évalue une meilleure réponse adverse avant de décider."
        );

        put(
            ratings,
            "minimax",
            1_629,
            "Recherche",
            "Utilise une recherche Minimax bornée avec alpha-bêta."
        );

        return Map.copyOf(ratings);
    }

    private static void put(
        Map<String, ReferenceElo> ratings,
        String key,
        int rating,
        String level,
        String explanation
    ) {
        ratings.put(
            key,
            new ReferenceElo(
                rating,
                level,
                explanation
            )
        );
    }
}
