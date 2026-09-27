package fr.astroware.chess.tournament.rating;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EloBenchmarkCsvExporterTest {

    @Test
    void exportsFinalStandingsAndPerGameHistory() {
        EloBenchmarkResult result =
            new EloBenchmark().run(
                java.util.List.of(
                    new fr.astroware.chess.tournament.roundrobin.TournamentParticipant(
                        "alpha",
                        EloBenchmarkTest.AlphaBot::new
                    ),
                    new fr.astroware.chess.tournament.roundrobin.TournamentParticipant(
                        "beta",
                        EloBenchmarkTest.BetaBot::new
                    )
                ),
                new EloBenchmarkSettings(
                    1_000.0,
                    24.0,
                    2,
                    10,
                    42L
                )
            );

        EloBenchmarkCsvExporter exporter =
            new EloBenchmarkCsvExporter();

        String standings =
            exporter.finalStandings(result);

        String history =
            exporter.history(result);

        assertTrue(
            standings.startsWith(
                "rank,key,bot,author,elo"
            )
        );

        assertTrue(
            standings.contains(
                ""alpha""
            )
        );

        assertTrue(
            history.startsWith(
                "game,white,black,result"
            )
        );

        assertTrue(
            history.contains(
                "white_elo_before"
            )
        );

        assertTrue(
            history.contains(
                ""beta""
            )
        );
    }
}
