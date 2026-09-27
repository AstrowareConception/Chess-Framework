package fr.astroware.chess.tournament.rating;

import java.io.PrintStream;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;

/**
 * Rapport console d'une estimation IRIS-Elo individuelle.
 */
public final class BotEloEstimateReporter {

    private final PrintStream out;

    public BotEloEstimateReporter() {
        this(System.out);
    }

    public BotEloEstimateReporter(
        PrintStream out
    ) {
        this.out =
            Objects.requireNonNull(
                out,
                "out must not be null"
            );
    }

    public void print(
        BotEloEstimate estimate
    ) {
        Objects.requireNonNull(
            estimate,
            "estimate must not be null"
        );

        out.println();
        out.println(
            "=========================================================================="
        );
        out.println(
            "                         ESTIMATION IRIS-ELO"
        );
        out.println(
            "=========================================================================="
        );

        out.printf(
            Locale.ROOT,
            "%s — %s%n",
            estimate.bot().botName(),
            estimate.bot().authorName()
        );

        out.printf(
            Locale.ROOT,
            "IRIS-Elo estimé : %.0f%n",
            estimate.estimatedRating()
        );

        out.printf(
            Locale.ROOT,
            "Intervalle 95 %%  : %.0f — %.0f%n",
            estimate.confidence95Low(),
            estimate.confidence95High()
        );

        out.printf(
            Locale.ROOT,
            "Parties          : %d%n",
            estimate.games()
        );

        out.printf(
            Locale.ROOT,
            "Score            : %.1f / %d (%.1f %%%%)%n%n",
            estimate.points(),
            estimate.games(),
            estimate.scoreRate() * 100.0
        );

        out.printf(
            "%-14s %8s %6s %4s %4s %4s %5s %8s%n",
            "Référence",
            "IRIS-Elo",
            "Part.",
            "V",
            "N",
            "D",
            "F",
            "Score"
        );

        out.println(
            "--------------------------------------------------------------------------"
        );

        estimate.references()
            .stream()
            .sorted(
                Comparator.comparingDouble(
                    BotEloReferenceResult
                        ::referenceRating
                )
            )
            .forEach(result ->
                out.printf(
                    Locale.ROOT,
                    "%-14s %8.0f %6d %4d %4d %4d %5d %7.1f%%%n",
                    result.referenceKey(),
                    result.referenceRating(),
                    result.games(),
                    result.wins(),
                    result.draws(),
                    result.losses(),
                    result.forfeits(),
                    result.scoreRate()
                        * 100.0
                )
            );

        out.println();
        out.println(
            "Estimation relative aux bots de référence Chess Framework."
        );
        out.println(
            "Aucune équivalence FIDE directe."
        );
        out.println(
            "=========================================================================="
        );
    }
}
