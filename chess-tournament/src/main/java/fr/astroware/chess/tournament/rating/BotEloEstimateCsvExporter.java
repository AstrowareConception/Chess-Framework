package fr.astroware.chess.tournament.rating;

import java.util.Locale;
import java.util.Objects;

/**
 * Export CSV d'une estimation individuelle.
 */
public final class BotEloEstimateCsvExporter {

    public String export(
        BotEloEstimate estimate
    ) {
        Objects.requireNonNull(
            estimate,
            "estimate must not be null"
        );

        StringBuilder csv =
            new StringBuilder();

        csv.append(
            "bot_key,bot,author,estimated_elo,ci95_low,ci95_high,games,points,reference,reference_elo,wins,draws,losses,forfeits,score_rate"
        ).append(System.lineSeparator());

        for (BotEloReferenceResult result
            : estimate.references()) {

            csv.append(
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
                    quoted(
                        result.referenceKey()
                    )
                )
                .append(',')
                .append(
                    format(
                        result.referenceRating()
                    )
                )
                .append(',')
                .append(
                    result.wins()
                )
                .append(',')
                .append(
                    result.draws()
                )
                .append(',')
                .append(
                    result.losses()
                )
                .append(',')
                .append(
                    result.forfeits()
                )
                .append(',')
                .append(
                    format(
                        result.scoreRate()
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
