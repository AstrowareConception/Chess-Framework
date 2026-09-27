package fr.astroware.chess.tournament.rating;

import java.util.Objects;

/**
 * Niveau pédagogique d'un bot de référence.
 *
 * <p>Cette valeur est un IRIS-Elo interne au framework. Elle sert uniquement
 * à positionner les adversaires de test les uns par rapport aux autres et ne
 * doit pas être interprétée comme un classement FIDE ou une estimation du
 * niveau humain équivalent.</p>
 */
public record ReferenceElo(
    int rating,
    String level,
    String explanation
) {

    public ReferenceElo {
        if (rating < 100) {
            throw new IllegalArgumentException(
                "rating must be >= 100"
            );
        }

        level = requireText(level, "level");
        explanation = requireText(
            explanation,
            "explanation"
        );
    }

    private static String requireText(
        String value,
        String field
    ) {
        Objects.requireNonNull(
            value,
            field + " must not be null"
        );

        String normalized = value.trim();

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(
                field + " must not be blank"
            );
        }

        return normalized;
    }
}
