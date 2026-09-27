package fr.astroware.chess.tournament.rating;

import java.io.PrintStream;
import java.util.Locale;
import java.util.Objects;

/**
 * Rapport console du benchmark Elo.
 */
public final class EloBenchmarkReporter {

    private final PrintStream out;

    public EloBenchmarkReporter() {
        this(System.out);
    }

    public EloBenchmarkReporter(
        PrintStream out
    ) {
        this.out =
            Objects.requireNonNull(
                out,
                "out must not be null"
            );
    }

    public void printProgress(
        EloMatchRecord record,
        int totalGames
    ) {
        if (record.gameNumber() == 1
            || record.gameNumber()
                == totalGames
            || record.gameNumber() % 25
                == 0) {

            out.printf(
                Locale.ROOT,
                "Elo benchmark : %d / %d parties%n",
                record.gameNumber(),
                totalGames
            );
        }
    }

    public void print(
        EloBenchmarkResult result
    ) {
        out.println();
        out.println(
            "=========================================================================="
        );
        out.println(
            "                    BENCHMARK IRIS-ELO DYNAMIQUE"
        );
        out.println(
            "=========================================================================="
        );

        out.printf(
            Locale.ROOT,
            "Parties : %d | Elo initial : %.0f | K : %.1f | parties/pair : %d%n%n",
            result.gamesPlayed(),
            result.settings()
                .initialRating(),
            result.settings()
                .kFactor(),
            result.settings()
                .gamesPerPair()
        );

        out.printf(
            "%-4s %-14s %-22s %8s %6s %5s %5s %5s %5s%n",
            "#",
            "Clé",
            "Bot",
            "IRIS-Elo",
            "Part.",
            "V",
            "N",
            "D",
            "F"
        );

        out.println(
            "--------------------------------------------------------------------------"
        );

        int rank = 1;

        for (EloStanding standing
            : result.standings()) {

            out.printf(
                Locale.ROOT,
                "%-4d %-14s %-22s %8.1f %6d %5d %5d %5d %5d%n",
                rank++,
                standing.key(),
                standing.bot().botName(),
                standing.rating(),
                standing.games(),
                standing.wins(),
                standing.draws(),
                standing.losses(),
                standing.forfeits()
            );
        }

        out.println();
        out.println(
            "Échelle interne Chess Framework — aucune équivalence FIDE."
        );
        out.println(
            "Les Elo ont été mis à jour après chaque partie."
        );
        out.println(
            "=========================================================================="
        );
    }
}
