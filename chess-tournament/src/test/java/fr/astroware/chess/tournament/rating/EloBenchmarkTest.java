package fr.astroware.chess.tournament.rating;

import fr.astroware.chess.bot.api.BotMetadata;
import fr.astroware.chess.bot.api.ChessBot;
import fr.astroware.chess.bot.evaluation.EvaluatedMove;
import fr.astroware.chess.bot.rule.Rule;
import fr.astroware.chess.bot.situation.Situations;
import fr.astroware.chess.core.model.Move;
import fr.astroware.chess.tournament.roundrobin.TournamentParticipant;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EloBenchmarkTest {

    @Test
    void standardReferenceScaleWouldPlayHundredsOfGames() {
        EloBenchmarkSettings settings =
            EloBenchmarkSettings.standard(42L);

        assertEquals(
            264,
            settings.expectedGameCount(12)
        );
    }

    @Test
    void equalRatingsHaveFiftyPercentExpectedScore() {
        assertEquals(
            0.5,
            EloBenchmark.expectedScore(
                1_000.0,
                1_000.0
            ),
            1.0e-12
        );
    }

    @Test
    void higherRatingHasHigherExpectedScore() {
        double expected =
            EloBenchmark.expectedScore(
                1_200.0,
                1_000.0
            );

        assertTrue(expected > 0.75);
        assertTrue(expected < 0.77);
    }

    @Test
    void benchmarkAlternatesColorsUpdatesEveryGameAndPreservesRatingSum() {
        EloBenchmarkSettings settings =
            new EloBenchmarkSettings(
                1_000.0,
                32.0,
                4,
                10,
                12345L
            );

        EloBenchmarkResult result =
            new EloBenchmark().run(
                participants(),
                settings
            );

        assertEquals(4, result.gamesPlayed());

        assertEquals(
            2,
            result.history().stream()
                .filter(record ->
                    record.whiteKey()
                        .equals("alpha")
                )
                .count()
        );

        assertEquals(
            2,
            result.history().stream()
                .filter(record ->
                    record.whiteKey()
                        .equals("beta")
                )
                .count()
        );

        result.history().forEach(record -> {
            // Le Fool's Mate impose une victoire noire.
            assertEquals(
                0.0,
                record.whiteScore()
            );

            assertNotEquals(
                record.whiteRatingBefore(),
                record.whiteRatingAfter()
            );

            assertEquals(
                record.whiteRatingBefore()
                    + record.blackRatingBefore(),
                record.whiteRatingAfter()
                    + record.blackRatingAfter(),
                1.0e-9
            );
        });

        double finalSum =
            result.standings().stream()
                .mapToDouble(
                    EloStanding::rating
                )
                .sum();

        assertEquals(
            2_000.0,
            finalSum,
            1.0e-9
        );

        result.standings().forEach(standing -> {
            assertEquals(4, standing.games());
            assertEquals(2, standing.wins());
            assertEquals(2, standing.losses());
            assertEquals(0, standing.draws());
        });
    }

    @Test
    void sameSeedProducesSameScheduleAndRatingHistory() {
        EloBenchmarkSettings settings =
            new EloBenchmarkSettings(
                1_000.0,
                24.0,
                4,
                10,
                987654321L
            );

        EloBenchmark benchmark =
            new EloBenchmark();

        EloBenchmarkResult first =
            benchmark.run(
                participants(),
                settings
            );

        EloBenchmarkResult second =
            benchmark.run(
                participants(),
                settings
            );

        assertEquals(
            historySignature(first),
            historySignature(second)
        );

        assertEquals(
            standingSignature(first),
            standingSignature(second)
        );
    }

    @Test
    void refusesOddGamesPerPairBecauseColorsWouldBeUnbalanced() {
        assertThrows(
            IllegalArgumentException.class,
            () ->
                new EloBenchmarkSettings(
                    1_000.0,
                    24.0,
                    3,
                    100,
                    42L
                )
        );
    }

    private static List<TournamentParticipant>
        participants() {

        return List.of(
            new TournamentParticipant(
                "alpha",
                AlphaBot::new
            ),
            new TournamentParticipant(
                "beta",
                BetaBot::new
            )
        );
    }

    private static List<String> historySignature(
        EloBenchmarkResult result
    ) {
        return result.history().stream()
            .map(record ->
                record.whiteKey()
                    + "|"
                    + record.blackKey()
                    + "|"
                    + record.result()
                    + "|"
                    + record.whiteRatingAfter()
                    + "|"
                    + record.blackRatingAfter()
            )
            .toList();
    }

    private static List<String> standingSignature(
        EloBenchmarkResult result
    ) {
        return result.standings().stream()
            .map(standing ->
                standing.key()
                    + "|"
                    + standing.rating()
                    + "|"
                    + standing.wins()
                    + "|"
                    + standing.draws()
                    + "|"
                    + standing.losses()
            )
            .toList();
    }

    public static final class AlphaBot
        extends FoolMateScriptBot {

        @Override
        public BotMetadata metadata() {
            return new BotMetadata(
                "Alpha Script",
                "Test Alpha",
                "Bot déterministe de test Elo."
            );
        }
    }

    public static final class BetaBot
        extends FoolMateScriptBot {

        @Override
        public BotMetadata metadata() {
            return new BotMetadata(
                "Beta Script",
                "Test Beta",
                "Bot déterministe de test Elo."
            );
        }
    }

    public abstract static class FoolMateScriptBot
        extends ChessBot {

        @Override
        protected List<Rule<?>> rules() {
            return List.of(
                rule(
                    "Fool's Mate script",
                    Situations.always(),
                    (context, detections) -> {
                        Move move =
                            scriptedMove(
                                context.myColor(),
                                context.moveHistory()
                                    .size()
                            );

                        if (move != null
                            && context.legalMoves()
                                .contains(move)) {

                            return List.of(
                                EvaluatedMove.of(
                                    move,
                                    10.0,
                                    "Séquence déterministe de test"
                                )
                            );
                        }

                        return List.of();
                    }
                )
            );
        }

        private static Move scriptedMove(
            fr.astroware.chess.core.model.Color color,
            int historySize
        ) {
            if (color
                == fr.astroware.chess.core.model.Color.WHITE) {

                return switch (historySize) {
                    case 0 ->
                        Move.fromUci("f2f3");
                    case 2 ->
                        Move.fromUci("g2g4");
                    default -> null;
                };
            }

            return switch (historySize) {
                case 1 ->
                    Move.fromUci("e7e5");
                case 3 ->
                    Move.fromUci("d8h4");
                default -> null;
            };
        }
    }
}
