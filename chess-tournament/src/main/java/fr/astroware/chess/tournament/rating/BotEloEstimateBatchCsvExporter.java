package fr.astroware.chess.tournament.rating;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Export CSV synthétique de plusieurs estimations IRIS-Elo.
 */
public final class BotEloEstimateBatchCsvExporter {

    public String export(
        List<BotEloEstimate> estimates
    ) {
        Objects.requireNonNull(
            estimates,
            "estimates must not be null"
        );

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

        StringBuilder csv =
            new StringBuilder();

        csv.append(
            "rank,bot_key,bot,author,estimated_elo,ci95_low,ci95_high,games,points,score_rate"
        ).append(System.lineSeparator());

        int rank = 1;

        for (BotEloEstimate estimate
            : ranking) {

            csv.append(rank++)
                .append(',')
                .append(
                    quoted(
                        estimate.botKey()
                    )
                )
                .append(',')
                .append(
                    quoted(
                        estimate.bot()
                            .botName()
                    )
                )
                .append(',')
                .append(
                    quoted(
                        estimate.bot()
                            .authorName()
                    )
                )
                .append(',')
                .append(
                    format(
                        estimate.estimatedRating()
                    )
                )
                .append(',')
                .append(
                    format(
                        estimate.confidence95Low()
                    )
                )
                .append(',')
                .append(
                    format(
                        estimate.confidence95High()
                    )
                )
                .append(',')
                .append(
                    estimate.games()
                )
                .append(',')
                .append(
                    format(
                        estimate.points()
                    )
                )
                .append(',')
                .append(
                    format(
                        estimate.scoreRate()
                    )
                )
                .append(
                    System.lineSeparator()
                );
        }

        return csv.toString();
    }

    private static String format(
        double value
    ) {
        return String.format(
            Locale.ROOT,
            "%.3f",
            value
        );
    }

    private static String quoted(
        String value
    ) {
        String quote =
            Character.toString('"');

        return quote
            + value.replace(
                quote,
                quote + quote
            )
            + quote;
    }
}
