package fr.astroware.chess.tournament.rating;

import fr.astroware.chess.bot.api.BotMetadata;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BotEloEstimateCsvExporterTest {

    @Test
    void exportsEstimateAndReferenceBreakdown() {
        BotEloEstimate estimate =
            new BotEloEstimate(
                "student-deep-rabbit",
                new BotMetadata(
                    "Deep Rabbit",
                    "Alice Dupont",
                    "Bot de test"
                ),
                1_743.0,
                42.0,
                1_661.0,
                1_825.0,
                8,
                5.5,
                List.of(
                    new BotEloReferenceResult(
                        "tactical",
                        1_538.0,
                        4,
                        3,
                        1,
                        0,
                        0,
                        3.5
                    ),
                    new BotEloReferenceResult(
                        "lookahead",
                        1_652.0,
                        4,
                        1,
                        0,
                        3,
                        0,
                        1.0
                    )
                )
            );

        String csv =
            new BotEloEstimateCsvExporter()
                .export(estimate);

        assertTrue(
            csv.contains(
                "estimated_elo"
            )
        );

        assertTrue(
            csv.contains(
                "student-deep-rabbit"
            )
        );

        assertTrue(
            csv.contains("tactical")
        );

        assertTrue(
            csv.contains("lookahead")
        );
    }
}
