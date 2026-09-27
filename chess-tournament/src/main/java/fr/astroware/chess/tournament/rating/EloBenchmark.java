package fr.astroware.chess.tournament.rating;

import fr.astroware.chess.core.game.GameStatus;
import fr.astroware.chess.tournament.match.MatchConfiguration;
import fr.astroware.chess.tournament.match.MatchResult;
import fr.astroware.chess.tournament.match.MatchRunner;
import fr.astroware.chess.tournament.match.MatchTermination;
import fr.astroware.chess.tournament.roundrobin.TournamentParticipant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Benchmark Elo dynamique entre plusieurs bots.
 *
 * <p>Toutes les confrontations sont équilibrées en couleurs. Le planning
 * complet est ensuite mélangé de manière déterministe à partir de la seed.
 * Les Elo sont mis à jour immédiatement après chaque partie.</p>
 */
public final class EloBenchmark {

    private final MatchRunner matchRunner;

    public EloBenchmark() {
        this(new MatchRunner());
    }

    public EloBenchmark(
        MatchRunner matchRunner
    ) {
        this.matchRunner =
            Objects.requireNonNull(
                matchRunner,
                "matchRunner must not be null"
            );
    }

    public EloBenchmarkResult run(
        List<TournamentParticipant> participants,
        EloBenchmarkSettings settings
    ) {
        return run(
            participants,
            settings,
            ignored -> {
            }
        );
    }

    public EloBenchmarkResult run(
        List<TournamentParticipant> participants,
        EloBenchmarkSettings settings,
        Consumer<EloMatchRecord> progress
    ) {
        Objects.requireNonNull(
            participants,
            "participants must not be null"
        );
        Objects.requireNonNull(
            settings,
            "settings must not be null"
        );
        Objects.requireNonNull(
            progress,
            "progress must not be null"
        );

        List<TournamentParticipant> roster =
            List.copyOf(participants);

        if (roster.size() < 2) {
            throw new IllegalArgumentException(
                "An Elo benchmark requires at least two participants"
            );
        }

        ensureUniqueKeys(roster);

        Map<String, RatingState> states =
            initialStates(
                roster,
                settings.initialRating()
            );

        List<ScheduledGame> schedule =
            createSchedule(
                roster,
                settings
            );

        List<EloMatchRecord> history =
            new ArrayList<>(
                schedule.size()
            );

        int gameNumber = 0;

        for (ScheduledGame scheduled
            : schedule) {

            gameNumber++;

            RatingState white =
                states.get(
                    scheduled.white().key()
                );

            RatingState black =
                states.get(
                    scheduled.black().key()
                );

            double whiteBefore =
                white.rating;

            double blackBefore =
                black.rating;

            double expectedWhite =
                expectedScore(
                    whiteBefore,
                    blackBefore
                );

            MatchResult match =
                matchRunner.play(
                    scheduled.white()
                        .playerFactory(),
                    scheduled.black()
                        .playerFactory(),
                    new MatchConfiguration(
                        settings.maxPlies(),
                        deriveGameSeed(
                            settings.baseSeed(),
                            gameNumber
                        ),
                        java.util.Optional.empty()
                    )
                );

            double whiteScore =
                scoreForWhite(match);

            double delta =
                settings.kFactor()
                    * (
                        whiteScore
                            - expectedWhite
                    );

            white.rating += delta;
            black.rating -= delta;

            white.record(
                whiteScore,
                match.termination()
                    == MatchTermination.FORFEIT
                    && match.incident()
                        .map(incident ->
                            incident.offenderColor()
                                == fr.astroware.chess.core.model.Color.WHITE
                        )
                        .orElse(false)
            );

            black.record(
                1.0 - whiteScore,
                match.termination()
                    == MatchTermination.FORFEIT
                    && match.incident()
                        .map(incident ->
                            incident.offenderColor()
                                == fr.astroware.chess.core.model.Color.BLACK
                        )
                        .orElse(false)
            );

            EloMatchRecord record =
                new EloMatchRecord(
                    gameNumber,
                    scheduled.white().key(),
                    scheduled.black().key(),
                    match.pgnResult(),
                    whiteScore,
                    expectedWhite,
                    whiteBefore,
                    blackBefore,
                    white.rating,
                    black.rating,
                    match.termination()
                        == MatchTermination.MOVE_LIMIT,
                    match.termination()
                        == MatchTermination.FORFEIT
                );

            history.add(record);
            progress.accept(record);
        }

        List<EloStanding> standings =
            states.values()
                .stream()
                .map(RatingState::standing)
                .sorted(
                    Comparator
                        .comparingDouble(
                            EloStanding::rating
                        )
                        .reversed()
                        .thenComparing(
                            EloStanding::key
                        )
                )
                .toList();

        return new EloBenchmarkResult(
            settings,
            standings,
            history
        );
    }

    /**
     * Formule Elo classique sans bonus de couleur.
     */
    public static double expectedScore(
        double ownRating,
        double opponentRating
    ) {
        return 1.0
            / (
                1.0
                    + Math.pow(
                        10.0,
                        (
                            opponentRating
                                - ownRating
                        ) / 400.0
                    )
            );
    }

    private static List<ScheduledGame>
        createSchedule(
            List<TournamentParticipant> roster,
            EloBenchmarkSettings settings
        ) {

        List<ScheduledGame> games =
            new ArrayList<>(
                settings.expectedGameCount(
                    roster.size()
                )
            );

        for (
            int first = 0;
            first < roster.size();
            first++
        ) {
            for (
                int second = first + 1;
                second < roster.size();
                second++
            ) {
                TournamentParticipant a =
                    roster.get(first);

                TournamentParticipant b =
                    roster.get(second);

                for (
                    int game = 0;
                    game
                        < settings.gamesPerPair();
                    game++
                ) {
                    boolean aIsWhite =
                        game % 2 == 0;

                    games.add(
                        new ScheduledGame(
                            aIsWhite ? a : b,
                            aIsWhite ? b : a
                        )
                    );
                }
            }
        }

        Collections.shuffle(
            games,
            new Random(
                settings.baseSeed()
                    ^ 0xE10B3A4D5EEDL
            )
        );

        return List.copyOf(games);
    }

    private static Map<String, RatingState>
        initialStates(
            List<TournamentParticipant> roster,
            double initialRating
        ) {

        Map<String, RatingState> states =
            new LinkedHashMap<>();

        for (TournamentParticipant participant
            : roster) {

            states.put(
                participant.key(),
                new RatingState(
                    participant,
                    initialRating
                )
            );
        }

        return states;
    }

    private static void ensureUniqueKeys(
        List<TournamentParticipant> roster
    ) {
        Set<String> keys =
            new HashSet<>();

        for (TournamentParticipant participant
            : roster) {

            if (!keys.add(
                participant.key()
            )) {
                throw new IllegalArgumentException(
                    "Duplicate benchmark participant key: "
                        + participant.key()
                );
            }
        }
    }

    private static double scoreForWhite(
        MatchResult match
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
            case WHITE_WINS -> 1.0;
            case BLACK_WINS -> 0.0;
            case DRAW -> 0.5;
            case ONGOING ->
                throw new IllegalStateException(
                    "Finished benchmark game cannot be ongoing"
                );
        };
    }

    private static long deriveGameSeed(
        long baseSeed,
        int gameNumber
    ) {
        long mixed =
            baseSeed
                ^ (
                    0x9E3779B97F4A7C15L
                        * gameNumber
                );

        mixed ^= mixed >>> 30;
        mixed *= 0xBF58476D1CE4E5B9L;
        mixed ^= mixed >>> 27;
        mixed *= 0x94D049BB133111EBL;
        mixed ^= mixed >>> 31;

        return mixed;
    }

    private record ScheduledGame(
        TournamentParticipant white,
        TournamentParticipant black
    ) {
    }

    private static final class RatingState {

        private final TournamentParticipant
            participant;

        private double rating;
        private int games;
        private int wins;
        private int draws;
        private int losses;
        private int forfeits;

        private RatingState(
            TournamentParticipant participant,
            double initialRating
        ) {
            this.participant = participant;
            this.rating = initialRating;
        }

        private void record(
            double score,
            boolean forfeit
        ) {
            games++;

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

        private EloStanding standing() {
            return new EloStanding(
                participant.key(),
                participant.metadata(),
                rating,
                games,
                wins,
                draws,
                losses,
                forfeits
            );
        }
    }
}
