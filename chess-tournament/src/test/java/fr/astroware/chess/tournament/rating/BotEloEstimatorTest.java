package fr.astroware.chess.tournament.rating;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BotEloEstimatorTest {

    @Test
    void fiftyPercentAgainst1600AnchorEstimatesNear1600() {
        BotEloEstimateSettings settings =
            new BotEloEstimateSettings(
                4,
                20,
                42L,
                1_500.0,
                800.0
            );

        List<BotEloReferenceResult> results =
            List.of(
                new BotEloReferenceResult(
                    "anchor",
                    1_600.0,
                    4,
                    2,
                    0,
                    2,
                    0,
                    2.0
                )
            );

        double rating =
            BotEloEstimator.estimateRating(
                results,
                settings
            );

        assertTrue(rating > 1_560.0);
        assertTrue(rating < 1_620.0);
    }

    @Test
    void dominantBotCanNaturallyExceedTwoThousand() {
        BotEloEstimateSettings settings =
            new BotEloEstimateSettings(
                100,
                20,
                42L,
                1_500.0,
                800.0
            );

        List<BotEloReferenceResult> results =
            List.of(
                new BotEloReferenceResult(
                    "lookahead-like-anchor",
                    1_650.0,
                    100,
                    95,
                    0,
                    5,
                    0,
                    95.0
                )
            );

        double rating =
            BotEloEstimator.estimateRating(
                results,
                settings
            );

        assertTrue(
            rating > 2_100.0,
            "95 % against a 1650 anchor should imply an IRIS-Elo above 2100"
        );
    }

    @Test
    void moreGamesReduceUncertainty() {
        BotEloEstimateSettings settings =
            new BotEloEstimateSettings(
                4,
                20,
                42L,
                1_500.0,
                800.0
            );

        List<BotEloReferenceResult> few =
            List.of(
                new BotEloReferenceResult(
                    "anchor",
                    1_600.0,
                    4,
                    2,
                    0,
                    2,
                    0,
                    2.0
                )
            );

        List<BotEloReferenceResult> many =
            List.of(
                new BotEloReferenceResult(
                    "anchor",
                    1_600.0,
                    40,
                    20,
                    0,
                    20,
                    0,
                    20.0
                )
            );

        double fewRating =
            BotEloEstimator.estimateRating(
                few,
                settings
            );

        double manyRating =
            BotEloEstimator.estimateRating(
                many,
                settings
            );

        double fewError =
            BotEloEstimator.standardError(
                fewRating,
                few,
                settings
            );

        double manyError =
            BotEloEstimator.standardError(
                manyRating,
                many,
                settings
            );

        assertTrue(manyError < fewError);
    }
}
