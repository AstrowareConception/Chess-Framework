package fr.astroware.chess.tournament.rating;

import fr.astroware.chess.bot.api.BotMetadata;

import java.util.Objects;

/**
 * Classement final d'un benchmark Elo.
 */
public record EloStanding(
    String key,
    BotMetadata bot,
    double rating,
    int games,
    int wins,
    int draws,
    int losses,
    int forfeits
) {

    public EloStanding {
        key = Objects.requireNonNull(
            key,
            "key must not be null"
        ).trim();

        Objects.requireNonNull(
            bot,
            "bot must not be null"
        );

        if (key.isEmpty()) {
            throw new IllegalArgumentException(
                "key must not be blank"
            );
        }

        if (!Double.isFinite(rating)
            || rating <= 0.0) {
            throw new IllegalArgumentException(
                "rating must be positive"
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
    }
}
