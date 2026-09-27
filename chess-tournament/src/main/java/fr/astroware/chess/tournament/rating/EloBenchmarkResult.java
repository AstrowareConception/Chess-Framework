package fr.astroware.chess.tournament.rating;

import java.util.List;
import java.util.Objects;

/**
 * Résultat complet du benchmark Elo.
 */
public record EloBenchmarkResult(
    EloBenchmarkSettings settings,
    List<EloStanding> standings,
    List<EloMatchRecord> history
) {

    public EloBenchmarkResult {
        Objects.requireNonNull(
            settings,
            "settings must not be null"
        );

        standings = List.copyOf(
            Objects.requireNonNull(
                standings,
                "standings must not be null"
            )
        );

        history = List.copyOf(
            Objects.requireNonNull(
                history,
                "history must not be null"
            )
        );
    }

    public int gamesPlayed() {
        return history.size();
    }
}
