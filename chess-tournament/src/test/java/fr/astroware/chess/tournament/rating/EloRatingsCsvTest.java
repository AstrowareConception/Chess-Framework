package fr.astroware.chess.tournament.rating;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EloRatingsCsvTest {

    @Test
    void readsRatingsFromBenchmarkFinalCsv() {
        String csv = """
            rank,key,bot,author,elo,games,wins,draws,losses,forfeits
            1,"lookahead","Lookahead Bot","AstroWare Conception",1652.250,88,36,52,0,0
            2,"random","Random Bot","AstroWare Conception",1340.125,88,0,43,45,0
            """;

        var ratings =
            EloRatingsCsv.parse(csv);

        assertEquals(
            1652.250,
            ratings.get("lookahead"),
            1.0e-9
        );

        assertEquals(
            1340.125,
            ratings.get("random"),
            1.0e-9
        );
    }

    @Test
    void supportsQuotedCommasInOtherColumns() {
        String csv = """
            rank,key,bot,author,elo,games,wins,draws,losses,forfeits
            1,"student-alpha","Alpha, The Bot","Doe, Alice",1512.5,10,5,4,1,0
            """;

        var ratings =
            EloRatingsCsv.parse(csv);

        assertEquals(
            1512.5,
            ratings.get("student-alpha"),
            1.0e-9
        );
    }

    @Test
    void rejectsMissingRequiredColumns() {
        assertThrows(
            IllegalArgumentException.class,
            () ->
                EloRatingsCsv.parse(
                    "key,bot\nrandom,Random Bot\n"
                )
        );
    }

    @Test
    void rejectsDuplicateKeys() {
        assertThrows(
            IllegalArgumentException.class,
            () ->
                EloRatingsCsv.parse(
                    """
                    key,elo
                    random,1400
                    random,1500
                    """
                )
        );
    }
}
