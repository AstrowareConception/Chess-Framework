package fr.astroware.chess.tournament.rating;

import fr.astroware.chess.core.game.GameStatus;
import fr.astroware.chess.core.model.Color;
import fr.astroware.chess.tournament.match.MatchConfiguration;
import fr.astroware.chess.tournament.match.MatchResult;
import fr.astroware.chess.tournament.match.MatchRunner;
import fr.astroware.chess.tournament.match.MatchTermination;
import fr.astroware.chess.tournament.roundrobin.TournamentParticipant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * Estime l'IRIS-Elo d'un bot contre des références dont les ratings restent
 * fixes.
 *
 * <p>L'estimation est un maximum a posteriori très faiblement régularisé.
 * Le prior évite uniquement les estimations infinies en cas de score parfait
 * ou nul. L'information apportée par les parties domine rapidement ce prior.</p>
 */
public final class BotEloEstimator {

    private static final double ELO_SCALE =
        Math.log(10.0) / 400.0;

    private final MatchRunner matchRunner;

    public BotEloEstimator() {
        this(new MatchRunner());
    }

    public BotEloEstimator(
        MatchRunner matchRunner
    ) {
        this.matchRunner =
            Objects.requireNonNull(
                matchRunner,
                "matchRunner must not be null"
            );
    }

    public BotEloEstimate estimate(
        TournamentParticipant target,
        List<EloReferenceOpponent> references,
        BotEloEstimateSettings settings
    ) {
        Objects.requireNonNull(
            target,
            "target must not be null"
        );
        Objects.requireNonNull(
            references,
            "references must not be null"
        );
        Objects.requireNonNull(
            settings,
            "settings must not be null"
        );

        List<EloReferenceOpponent> anchors =
            List.copyOf(references);

        if (anchors.isEmpty()) {
            throw new IllegalArgumentException(
                "At least one Elo reference is required"
            );
        }

        ensureUniqueReferenceKeys(
            anchors
        );

        List<ScheduledGame> schedule =
            createSchedule(
                target,
                anchors,
                settings
            );

        Map<String, MutableReferenceResult>
            results =
                new LinkedHashMap<>();

        for (EloReferenceOpponent anchor
            : anchors) {

            results.put(
                anchor.participant().key(),
                new MutableReferenceResult(
                    anchor
                )
            );
        }

        int gameNumber = 0;

        for (ScheduledGame game
            : schedule) {

            gameNumber++;

            MatchResult match =
                matchRunner.play(
                    game.white()
                        .playerFactory(),
                    game.black()
                        .playerFactory(),
                    new MatchConfiguration(
                        settings.maxPlies(),
                        deriveSeed(
                            settings.baseSeed(),
                            gameNumber
                        ),
                        java.util.Optional.empty()
                    )
                );

            double targetScore =
                scoreForTarget(
                    match,
                    game.targetIsWhite()
                );

            MutableReferenceResult result =
                results.get(
                    game.reference()
                        .participant()
                        .key()
                );

            result.record(
                targetScore,
                targetForfeited(
                    match,
                    game.targetIsWhite()
                )
            );
        }

        List<BotEloReferenceResult>
            referenceResults =
                results.values()
                    .stream()
                    .map(
                        MutableReferenceResult::snapshot
                    )
                    .toList();

        double rating =
            estimateRating(
                referenceResults,
                settings
            );

        double standardError =
            standardError(
                rating,
                referenceResults,
                settings
            );

        double halfWidth =
            1.96 * standardError;

        int games =
            referenceResults.stream()
                .mapToInt(
                    BotEloReferenceResult::games
                )
                .sum();

        double points =
            referenceResults.stream()
                .mapToDouble(
                    BotEloReferenceResult::points
                )
                .sum();

        return new BotEloEstimate(
            target.key(),
            target.metadata(),
            rating,
            standardError,
            Math.max(
                100.0,
                rating - halfWidth
            ),
            rating + halfWidth,
            games,
            points,
            referenceResults
        );
    }

    private static double estimateRating(
        List<BotEloReferenceResult> results,
        BotEloEstimateSettings settings
    ) {
        double low = 100.0;
        double high = 5_000.0;

        for (int iteration = 0;
            iteration < 100;
            iteration++) {

            double middle =
                (low + high) / 2.0;

            double derivative =
                derivative(
                    middle,
                    results,
                    settings
                );

            if (derivative > 0.0) {
                low = middle;
            } else {
                high = middle;
            }
        }

        return (low + high) / 2.0;
    }

    private static double derivative(
        double rating,
        List<BotEloReferenceResult> results,
        BotEloEstimateSettings settings
    ) {
        double scorePart = 0.0;

        for (BotEloReferenceResult result
            : results) {

            double expected =
                EloBenchmark.expectedScore(
                    rating,
                    result.referenceRating()
                );

            scorePart +=
                result.points()
                    - result.games()
                        * expected;
        }

        double priorPart =
            (
                rating
                    - settings.priorMean()
            )
                / (
                    settings.priorSigma()
                        * settings.priorSigma()
                );

        return ELO_SCALE
            * scorePart
            - priorPart;
    }

    private static double standardError(
        double rating,
        List<BotEloReferenceResult> results,
        BotEloEstimateSettings settings
    ) {
        double information =
            1.0
                / (
                    settings.priorSigma()
                        * settings.priorSigma()
                );

        for (BotEloReferenceResult result
            : results) {

            double expected =
                EloBenchmark.expectedScore(
                    rating,
                    result.referenceRating()
                );

            information +=
                ELO_SCALE
                    * ELO_SCALE
                    * result.games()
                    * expected
                    * (1.0 - expected);
        }

        return 1.0
            / Math.sqrt(information);
    }

    private static List<ScheduledGame>
        createSchedule(
            TournamentParticipant target,
            List<EloReferenceOpponent> references,
            BotEloEstimateSettings settings
        ) {

        List<ScheduledGame> games =
            new ArrayList<>(
                settings.expectedGameCount(
                    references.size()
                )
            );

        for (EloReferenceOpponent reference
            : references) {

            for (int game = 0;
                game
                    < settings.gamesPerReference();
                game++) {

                boolean targetIsWhite =
                    game % 2 == 0;

                games.add(
                    new ScheduledGame(
                        targetIsWhite
                            ? target
                            : reference.participant(),
                        targetIsWhite
                            ? reference.participant()
                            : target,
                        reference,
                        targetIsWhite
                    )
                );
            }
        }

        Collections.shuffle(
            games,
            new Random(
                settings.baseSeed()
                    ^ 0xB07E10E571A7EL
            )
        );

        return List.copyOf(games);
    }

    private static void ensureUniqueReferenceKeys(
        List<EloReferenceOpponent> references
    ) {
        long distinct =
            references.stream()
                .map(reference ->
                    reference.participant()
                        .key()
                )
                .distinct()
                .count();

        if (distinct
            != references.size()) {
            throw new IllegalArgumentException(
                "Reference bot keys must be unique"
            );
        }
    }

    private static double scoreForTarget(
        MatchResult match,
        boolean targetIsWhite
    ) {
        if (match.termination()
            == MatchTermination.MOVE_LIMIT) {
            return 0.5;
        }

        GameStatus status =
            match.gameResult()
                .orElseThrow()
                .status();

        return switch (status) {
            case DRAW -> 0.5;
            case WHITE_WINS ->
                targetIsWhite
                    ? 1.0
                    : 0.0;
            case BLACK_WINS ->
                targetIsWhite
                    ? 0.0
                    : 1.0;
            case ONGOING ->
                throw new IllegalStateException(
                    "Finished estimate game cannot be ongoing"
                );
        };
    }

    private static boolean targetForfeited(
        MatchResult match,
        boolean targetIsWhite
    ) {
        if (match.termination()
            != MatchTermination.FORFEIT) {
            return false;
        }

        Color targetColor =
            targetIsWhite
                ? Color.WHITE
                : Color.BLACK;

        return match.incident()
            .map(incident ->
                incident.offenderColor()
                    == targetColor
            )
            .orElse(false);
    }

    private static long deriveSeed(
        long baseSeed,
        int gameNumber
    ) {
        long value =
            baseSeed
                ^ (
                    0x9E3779B97F4A7C15L
                        * gameNumber
                );

        value ^= value >>> 30;
        value *= 0xBF58476D1CE4E5B9L;
        value ^= value >>> 27;
        value *= 0x94D049BB133111EBL;
        value ^= value >>> 31;

        return value;
    }

    private record ScheduledGame(
        TournamentParticipant white,
        TournamentParticipant black,
        EloReferenceOpponent reference,
        boolean targetIsWhite
    ) {
    }

    private static final class
        MutableReferenceResult {

        private final EloReferenceOpponent
            reference;

        private int games;
        private int wins;
        private int draws;
        private int losses;
        private int forfeits;
        private double points;

        private MutableReferenceResult(
            EloReferenceOpponent reference
        ) {
            this.reference = reference;
        }

        private void record(
            double score,
            boolean forfeit
        ) {
            games++;
            points += score;

            if (score == 1.0) {
                wins++;
            } else if (score == 0.5) {
                draws++;
            } else {
                losses++;
            }

            if (forfeit) {
                forfeits++;
            }
        }

        private BotEloReferenceResult snapshot() {
            return new BotEloReferenceResult(
                reference.participant().key(),
                reference.rating(),
                games,
                wins,
                draws,
                losses,
                forfeits,
                points
            );
        }
    }
}
