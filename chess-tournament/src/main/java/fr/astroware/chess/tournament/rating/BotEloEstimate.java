package fr.astroware.chess.tournament.rating;

import fr.astroware.chess.bot.api.BotMetadata;

import java.util.List;
import java.util.Objects;

/**
 * Estimation IRIS-Elo d'un bot face à un ensemble d'ancres fixes.
 */
public record BotEloEstimate(
    String botKey,
    BotMetadata bot,
    double estimatedRating,
    double standardError,
    double confidence95Low,
    double confidence95High,
    int games,
    double points,
    List<BotEloReferenceResult> references
) {

    public BotEloEstimate {
        botKey =
            Objects.requireNonNull(
                botKey,
                "botKey must not be null"
            ).trim();

        Objects.requireNonNull(
            bot,
            "bot must not be null"
        );

        references = List.copyOf(
            Objects.requireNonNull(
                references,
                "references must not be null"
            )
        );

        if (botKey.isEmpty()) {
            throw new IllegalArgumentException(
                "botKey must not be blank"
            );
        }

        for (double value : new double[] {
            estimatedRating,
            standardError,
            confidence95Low,
            confidence95High,
            points
        }) {
            if (!Double.isFinite(value)) {
                throw new IllegalArgumentException(
                    "estimate values must be finite"
                );
            }
        }

        if (estimatedRating <= 0.0
            || standardError <= 0.0
            || confidence95Low <= 0.0
            || confidence95High
                < confidence95Low) {
            throw new IllegalArgumentException(
                "invalid estimate bounds"
            );
        }

        if (games <= 0
            || points < 0.0
            || points > games) {
            throw new IllegalArgumentException(
                "invalid game totals"
            );
        }
    }

    public double scoreRate() {
        return points / games;
    }

    public double confidence95HalfWidth() {
        return (
            confidence95High
                - confidence95Low
        ) / 2.0;
    }
}
