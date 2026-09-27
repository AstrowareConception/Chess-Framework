package fr.astroware.chess.tournament.rating;

import java.util.Objects;

/**
 * Mise à jour Elo produite par une partie du benchmark.
 */
public record EloMatchRecord(
    int gameNumber,
    String whiteKey,
    String blackKey,
    String result,
    double whiteScore,
    double whiteExpectedScore,
    double whiteRatingBefore,
    double blackRatingBefore,
    double whiteRatingAfter,
    double blackRatingAfter,
    boolean technicalDraw,
    boolean forfeit
) {

    public EloMatchRecord {
        if (gameNumber <= 0) {
            throw new IllegalArgumentException(
                "gameNumber must be > 0"
            );
        }

        whiteKey = requireText(
            whiteKey,
            "whiteKey"
        );
        blackKey = requireText(
            blackKey,
            "blackKey"
        );
        result = requireText(
            result,
            "result"
        );

        requireScore(
            whiteScore,
            "whiteScore"
        );
        requireScore(
            whiteExpectedScore,
            "whiteExpectedScore"
        );

        requireRating(
            whiteRatingBefore,
            "whiteRatingBefore"
        );
        requireRating(
            blackRatingBefore,
            "blackRatingBefore"
        );
        requireRating(
            whiteRatingAfter,
            "whiteRatingAfter"
        );
        requireRating(
            blackRatingAfter,
            "blackRatingAfter"
        );
    }

    public double blackScore() {
        return 1.0 - whiteScore;
    }

    public double blackExpectedScore() {
        return 1.0 - whiteExpectedScore;
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

    private static void requireScore(
        double value,
        String field
    ) {
        if (!Double.isFinite(value)
            || value < 0.0
            || value > 1.0) {
            throw new IllegalArgumentException(
                field + " must be between 0 and 1"
            );
        }
    }

    private static void requireRating(
        double value,
        String field
    ) {
        if (!Double.isFinite(value)
            || value <= 0.0) {
            throw new IllegalArgumentException(
                field + " must be positive"
            );
        }
    }
}
