package fr.astroware.chess.tournament.rating;

import fr.astroware.chess.tournament.cli.BotCatalog;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReferenceEloCatalogTest {

    @Test
    void everyReferenceBotHasAnIrisElo() {
        assertEquals(
            BotCatalog.referenceBots().keySet(),
            ReferenceEloCatalog.all().keySet()
        );
    }

    @Test
    void referenceRatingsAreUniqueAndOrderedAcrossDifficultyRange() {
        var ratings =
            ReferenceEloCatalog.all()
                .values()
                .stream()
                .map(ReferenceElo::rating)
                .toList();

        assertEquals(
            ratings.size(),
            new HashSet<>(ratings).size()
        );

        assertTrue(
            ratings.stream()
                .min(Integer::compareTo)
                .orElseThrow()
                >= 1_300
        );

        assertTrue(
            ratings.stream()
                .max(Integer::compareTo)
                .orElseThrow()
                <= 1_700
        );

        assertEquals(
            1_340,
            ReferenceEloCatalog.find("random")
                .orElseThrow()
                .rating()
        );

        assertEquals(
            1_652,
            ReferenceEloCatalog.find("lookahead")
                .orElseThrow()
                .rating()
        );

        assertTrue(
            ReferenceEloCatalog.find("lookahead")
                .orElseThrow()
                .rating()
                > ReferenceEloCatalog.find("random")
                    .orElseThrow()
                    .rating()
        );
    }
}
