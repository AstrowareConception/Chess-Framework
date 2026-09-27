package fr.astroware.chess.tournament.rating;

import java.util.Locale;
import java.util.Objects;

/**
 * Exports CSV du classement final et de l'historique Elo.
 */
public final class EloBenchmarkCsvExporter {

    public String finalStandings(
        EloBenchmarkResult result
    ) {
        Objects.requireNonNull(
            result,
            "result must not be null"
        );

        StringBuilder csv =
            new StringBuilder();

        csv.append(
            "rank,key,bot,author,elo,games,wins,draws,losses,forfeits"
        ).append(System.lineSeparator());

        int rank = 1;

        for (EloStanding standing
            : result.standings()) {

            csv.append(rank++)
                .append(',')
                .append(csv(standing.key()))
                .append(',')
                .append(
                    csv(
                        standing.bot()
                            .botName()
                    )
                )
                .append(',')
                .append(
                    csv(
                        standing.bot()
                            .authorName()
                    )
                )
                .append(',')
                .append(
                    format(
                        standing.rating()
                    )
                )
                .append(',')
                .append(standing.games())
                .append(',')
                .append(standing.wins())
                .append(',')
                .append(standing.draws())
                .append(',')
                .append(standing.losses())
                .append(',')
                .append(standing.forfeits())
                .append(
                    System.lineSeparator()
                );
        }

        return csv.toString();
    }

    public String history(
        EloBenchmarkResult result
    ) {
        Objects.requireNonNull(
            result,
            "result must not be null"
        );

        StringBuilder csv =
            new StringBuilder();

        csv.append(
            "game,white,black,result,white_score,white_expected,white_elo_before,black_elo_before,white_elo_after,black_elo_after,technical_draw,forfeit"
        ).append(System.lineSeparator());

        for (EloMatchRecord record
            : result.history()) {

            csv.append(
                    record.gameNumber()
                )
                .append(',')
                .append(
                    csv(record.whiteKey())
                )
                .append(',')
                .append(
                    csv(record.blackKey())
                )
                .append(',')
                .append(
                    csv(record.result())
                )
                .append(',')
                .append(
                    format(
                        record.whiteScore()
                    )
                )
                .append(',')
                .append(
                    format(
                        record.whiteExpectedScore()
                    )
                )
                .append(',')
                .append(
                    format(
                        record.whiteRatingBefore()
                    )
                )
                .append(',')
                .append(
                    format(
                        record.blackRatingBefore()
                    )
                )
                .append(',')
                .append(
                    format(
                        record.whiteRatingAfter()
                    )
                )
                .append(',')
                .append(
                    format(
                        record.blackRatingAfter()
                    )
                )
                .append(',')
                .append(
                    record.technicalDraw()
                )
                .append(',')
                .append(
                    record.forfeit()
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

    private static String csv(
        String value
    ) {
        String quote =
            Character.toString('"');

        String escaped =
            value.replace(
                quote,
                quote + quote
            );

        return quote
            + escaped
            + quote;
    }
}
