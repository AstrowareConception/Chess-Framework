package fr.astroware.chess.tournament.rating;

/**
 * Configuration d'une estimation IRIS-Elo individuelle.
 *
 * @param gamesPerReference nombre pair de parties jouées contre chaque ancre
 * @param maxPlies limite technique de demi-coups
 * @param baseSeed seed reproductible
 * @param priorMean centre du prior faible utilisé uniquement pour éviter une
 *                  estimation infinie après un score parfait
 * @param priorSigma écart-type du prior ; une valeur élevée rend son influence
 *                   très faible dès que suffisamment de parties sont jouées
 */
public record BotEloEstimateSettings(
    int gamesPerReference,
    int maxPlies,
    long baseSeed,
    double priorMean,
    double priorSigma
) {

    public BotEloEstimateSettings {
        if (gamesPerReference <= 0
            || gamesPerReference % 2 != 0) {
            throw new IllegalArgumentException(
                "gamesPerReference must be a positive even number"
            );
        }

        if (maxPlies <= 0) {
            throw new IllegalArgumentException(
                "maxPlies must be > 0"
            );
        }

        if (!Double.isFinite(priorMean)
            || priorMean <= 0.0) {
            throw new IllegalArgumentException(
                "priorMean must be positive"
            );
        }

        if (!Double.isFinite(priorSigma)
            || priorSigma <= 0.0) {
            throw new IllegalArgumentException(
                "priorSigma must be positive"
            );
        }
    }

    public static BotEloEstimateSettings standard(
        long seed
    ) {
        return new BotEloEstimateSettings(
            4,
            400,
            seed,
            1_500.0,
            800.0
        );
    }

    public int expectedGameCount(
        int referenceCount
    ) {
        return referenceCount
            * gamesPerReference;
    }
}
