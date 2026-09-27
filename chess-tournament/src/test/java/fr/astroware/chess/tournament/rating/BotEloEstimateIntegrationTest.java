package fr.astroware.chess.tournament.rating;

import fr.astroware.chess.tournament.roundrobin.TournamentParticipant;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BotEloEstimateIntegrationTest {

    @Test
    void balancedDeterministicMatchesProduceFiniteEstimate() {
        TournamentParticipant target =
            new TournamentParticipant(
                "alpha",
                EloBenchmarkTest.AlphaBot::new
            );

        TournamentParticipant anchor =
            new TournamentParticipant(
                "beta",
                EloBenchmarkTest.BetaBot::new
            );

        BotEloEstimate estimate =
            new BotEloEstimator().estimate(
                target,
                List.of(
                    new EloReferenceOpponent(
                        anchor,
                        1_600.0
                    )
                ),
                new BotEloEstimateSettings(
                    4,
                    10,
                    123L,
                    1_500.0,
                    800.0
                )
            );

        assertEquals(4, estimate.games());
        assertEquals(2.0, estimate.points());

        assertTrue(
            estimate.estimatedRating()
                > 1_560.0
        );

        assertTrue(
            estimate.estimatedRating()
                < 1_620.0
        );

        assertTrue(
            estimate.confidence95Low()
                < estimate.estimatedRating()
        );

        assertTrue(
            estimate.confidence95High()
                > estimate.estimatedRating()
        );
    }
}
