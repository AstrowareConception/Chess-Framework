package fr.astroware.chess.tournament.rating;

import fr.astroware.chess.tournament.roundrobin.TournamentParticipant;

import java.util.Objects;

/**
 * Adversaire de référence dont l'IRIS-Elo est considéré comme fixé pendant
 * l'estimation d'un autre bot.
 */
public record EloReferenceOpponent(
    TournamentParticipant participant,
    double rating
) {

    public EloReferenceOpponent {
        Objects.requireNonNull(
            participant,
            "participant must not be null"
        );

        if (!Double.isFinite(rating)
            || rating <= 0.0) {
            throw new IllegalArgumentException(
                "rating must be positive"
            );
        }
    }
}
