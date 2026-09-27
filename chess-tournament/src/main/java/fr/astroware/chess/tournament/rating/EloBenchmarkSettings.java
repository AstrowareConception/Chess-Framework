package fr.astroware.chess.tournament.rating;

/**
 * Configuration d'un benchmark Elo dynamique.
 *
 * @param initialRating Elo initial identique pour tous les participants
 * @param kFactor facteur K de mise à jour Elo
 * @param gamesPerPair nombre de parties par paire ; doit être pair afin que
 *                     chaque bot joue autant avec les Blancs qu'avec les Noirs
 * @param maxPlies limite technique de demi-coups par partie
 * @param baseSeed seed rendant le benchmark reproductible
 */
public record EloBenchmarkSettings(
    double initialRating,
    double kFactor,
    int gamesPerPair,
    int maxPlies,
    long baseSeed
) {

    public EloBenchmarkSettings {
        if (!Double.isFinite(initialRating)
            || initialRating <= 0.0) {
            throw new IllegalArgumentException(
                "initialRating must be positive"
            );
        }

        if (!Double.isFinite(kFactor)
            || kFactor <= 0.0) {
            throw new IllegalArgumentException(
                "kFactor must be positive"
            );
        }

        if (gamesPerPair <= 0
            || gamesPerPair % 2 != 0) {
            throw new IllegalArgumentException(
                "gamesPerPair must be a positive even number"
            );
        }

        if (maxPlies <= 0) {
            throw new IllegalArgumentException(
                "maxPlies must be > 0"
            );
        }
    }

    /**
     * 12 bots de référence -> 66 paires -> 264 parties.
     */
    public static EloBenchmarkSettings standard(
        long seed
    ) {
        return new EloBenchmarkSettings(
            1_500.0,
            24.0,
            4,
            400,
            seed
        );
    }

    public int expectedGameCount(
        int participantCount
    ) {
        if (participantCount < 2) {
            return 0;
        }

        int pairs =
            participantCount
                * (participantCount - 1)
                / 2;

        return pairs * gamesPerPair;
    }
}
