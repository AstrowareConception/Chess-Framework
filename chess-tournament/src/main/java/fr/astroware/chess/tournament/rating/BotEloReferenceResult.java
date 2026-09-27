package fr.astroware.chess.tournament.rating;

import java.util.Objects;

/**
 * Résultat du bot évalué contre une ancre IRIS-Elo.
 */
public record BotEloReferenceResult(
    String referenceKey,
    double referenceRating,
    int games,
    int wins,
    int draws,
    int losses,
    int forfeits,
    double points
) {

    public BotEloReferenceResult {
        referenceKey =
            Objects.requireNonNull(
                referenceKey,
                "referenceKey must not be null"
            ).trim();

        if (referenceKey.isEmpty()) {
            throw new IllegalArgumentException(
                "referenceKey must not be blank"
            );
        }

        if (!Double.isFinite(referenceRating)
            || referenceRating <= 0.0) {
            throw new IllegalArgumentException(
                "referenceRating must be positive"
            );
        }

        if (games < 0
            || wins < 0
            || draws < 0
            || losses < 0
            || forfeits < 0) {
            throw new IllegalArgumentException(
                "counters must be non-negative"
            );
        }

        if (!Double.isFinite(points)
            || points < 0.0
            || points > games) {
            throw new IllegalArgumentException(
                "points must be between 0 and games"
            );
        }
    }

    public double scoreRate() {
        return games == 0
            ? 0.0
            : points / games;
    }
}
