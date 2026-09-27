package fr.astroware.chess.tournament.rating;

import java.io.PrintStream;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Tableau synthétique de plusieurs estimations IRIS-Elo étudiantes.
 */
public final class BotEloEstimateBatchReporter {

    private final PrintStream out;

    public BotEloEstimateBatchReporter() {
        this(System.out);
    }

    public BotEloEstimateBatchReporter(
        PrintStream out
    ) {
        this.out =
            Objects.requireNonNull(
                out,
                "out must not be null"
            );
    }

    public void print(
        List<BotEloEstimate> estimates
    ) {
        List<BotEloEstimate> ranking =
            estimates.stream()
                .sorted(
                    Comparator
                        .comparingDouble(
                            BotEloEstimate
                                ::estimatedRating
                        )
                        .reversed()
                        .thenComparing(
                            BotEloEstimate
                                ::botKey
                        )
                )
                .toList();

        out.println();
        out.println(
            "================================================================================"
        );
        out.println(
            "                    ESTIMATIONS IRIS-ELO ÉTUDIANTES"
        );
        out.println(
            "================================================================================"
        );

        out.printf(
            "%-4s %-24s %-24s %8s %17s %7s%n",
            "#",
            "Bot",
            "Auteur",
            "Elo",
            "IC 95 %",
            "Part."
        );

        out.println(
            "--------------------------------------------------------------------------------"
        );

        int rank = 1;

        for (BotEloEstimate estimate
            : ranking) {

            out.printf(
                Locale.ROOT,
                "%-4d %-24s %-24s %8.0f %7.0f — %-7.0f %7d%n",
                rank++,
                estimate.bot()
                    .botName(),
                estimate.bot()
                    .authorName(),
                estimate.estimatedRating(),
                estimate.confidence95Low(),
                estimate.confidence95High(),
                estimate.games()
            );
        }

        out.println();
        out.println(
            "Estimations relatives aux références calibrées Chess Framework."
        );
        out.println(
            "Ce tableau ne remplace pas le classement officiel du tournoi."
        );
        out.println(
            "================================================================================"
        );
    }
}
