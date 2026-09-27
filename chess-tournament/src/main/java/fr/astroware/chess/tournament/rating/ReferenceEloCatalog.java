package fr.astroware.chess.tournament.rating;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Échelle de difficulté initiale des bots de référence.
 *
 * <p>Les valeurs sont volontairement espacées pour fournir une progression
 * pédagogique lisible. Elles sont dites provisoires : elles pourront être
 * recalibrées après accumulation de résultats de tournois réels.</p>
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
            400,
            "Découverte",
            "Joue légalement sans raisonnement stratégique."
        );

        put(
            ratings,
            "greedy",
            600,
            "Débutant",
            "Comprend le gain matériel immédiat mais ignore les conséquences."
        );

        put(
            ratings,
            "berserker",
            700,
            "Agressif",
            "Cherche l'initiative et accepte beaucoup de risque."
        );

        put(
            ratings,
            "cautious",
            750,
            "Prudent",
            "Évite mieux les prises risquées et protège son roi."
        );

        put(
            ratings,
            "guardian",
            825,
            "Défensif",
            "Utilise la projection pour sauver les pièces menacées."
        );

        put(
            ratings,
            "architect",
            900,
            "Planificateur",
            "Combine ouvertures, développement, centre et roque."
        );

        put(
            ratings,
            "tactical",
            1_000,
            "Tactique",
            "Reconnaît plusieurs motifs tactiques et les priorise."
        );

        put(
            ratings,
            "pressure",
            1_075,
            "Pression",
            "Cherche clouages, surcharge, enfilades et contraintes."
        );

        put(
            ratings,
            "chameleon",
            1_125,
            "Adaptatif",
            "Change de profil et de priorités selon la phase de jeu."
        );

        put(
            ratings,
            "positional",
            1_200,
            "Positionnel",
            "Compare globalement matériel, mobilité, centre, pions et roi."
        );

        put(
            ratings,
            "lookahead",
            1_300,
            "Anticipation",
            "Évalue une meilleure réponse adverse avant de décider."
        );

        put(
            ratings,
            "minimax",
            1_400,
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
