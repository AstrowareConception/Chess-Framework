package fr.astroware.chess.tournament.cli;

import fr.astroware.chess.bot.api.BotMetadata;
import fr.astroware.chess.bot.api.ChessBot;
import fr.astroware.chess.tournament.console.ConsoleMatchListener;
import fr.astroware.chess.tournament.console.ConsoleTournamentReporter;
import fr.astroware.chess.tournament.execution.BotPlayer;
import fr.astroware.chess.tournament.execution.BotPlayerFactory;
import fr.astroware.chess.tournament.execution.BotPlayers;
import fr.astroware.chess.tournament.execution.IsolatedBotPlayerFactory;
import fr.astroware.chess.tournament.execution.IsolatedBotSettings;
import fr.astroware.chess.tournament.match.BotFactory;
import fr.astroware.chess.tournament.match.MatchConfiguration;
import fr.astroware.chess.tournament.match.MatchResult;
import fr.astroware.chess.tournament.match.MatchRunner;
import fr.astroware.chess.tournament.pgn.PgnExporter;
import fr.astroware.chess.tournament.rating.BotEloEstimate;
import fr.astroware.chess.tournament.rating.BotEloEstimateCsvExporter;
import fr.astroware.chess.tournament.rating.BotEloEstimateBatchCsvExporter;
import fr.astroware.chess.tournament.rating.BotEloEstimateBatchReporter;
import fr.astroware.chess.tournament.rating.BotEloEstimateReporter;
import fr.astroware.chess.tournament.rating.BotEloEstimateSettings;
import fr.astroware.chess.tournament.rating.BotEloEstimator;
import fr.astroware.chess.tournament.rating.EloReferenceOpponent;
import fr.astroware.chess.tournament.rating.EloBenchmark;
import fr.astroware.chess.tournament.rating.EloBenchmarkCsvExporter;
import fr.astroware.chess.tournament.rating.EloBenchmarkReporter;
import fr.astroware.chess.tournament.rating.EloBenchmarkResult;
import fr.astroware.chess.tournament.rating.EloBenchmarkSettings;
import fr.astroware.chess.tournament.rating.EloRatingsCsv;
import fr.astroware.chess.tournament.rating.ReferenceEloCatalog;
import fr.astroware.chess.tournament.roundrobin.RoundRobinConfiguration;
import fr.astroware.chess.tournament.roundrobin.RoundRobinPgnExporter;
import fr.astroware.chess.tournament.roundrobin.RoundRobinResult;
import fr.astroware.chess.tournament.roundrobin.RoundRobinTournament;
import fr.astroware.chess.tournament.roundrobin.StandingsCsvExporter;
import fr.astroware.chess.tournament.roundrobin.TournamentParticipant;
import fr.astroware.chess.tournament.submission.StudentSubmissionValidator;
import fr.astroware.chess.tournament.ui.SwingMatchViewer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * CLI du framework de tournoi.
 */
public final class ChessFrameworkCli {

    private static final long DEFAULT_SEED = 42L;
    private static final int DEFAULT_MAX_PLIES = 400;
    private static final int DEFAULT_GAMES_PER_PAIR = 2;
    private static final int DEFAULT_TIMEOUT_MS = 2_000;
    private static final int DEFAULT_STARTUP_TIMEOUT_MS = 5_000;
    private static final int DEFAULT_HEAP_MB = 256;

    private ChessFrameworkCli() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 0
            || "help".equalsIgnoreCase(args[0])) {
            printUsage();
            return;
        }

        if ("list".equalsIgnoreCase(args[0])) {
            printBots();
            return;
        }

        if ("ratings".equalsIgnoreCase(args[0])) {
            printRatings();
            return;
        }

        String mode =
            args[0].toLowerCase(Locale.ROOT);

        if ("tournament".equals(mode)) {
            runTournament(args);
            return;
        }

        if ("elo-benchmark".equals(mode)) {
            runEloBenchmark(args);
            return;
        }

        if ("elo-estimate".equals(mode)) {
            runEloEstimate(args);
            return;
        }

        if ("elo-estimate-all".equals(mode)) {
            runAllStudentEloEstimates(args);
            return;
        }

        if ("validate-students".equals(mode)) {
            validateStudents();
            return;
        }

        if (args.length < 3) {
            printUsage();
            return;
        }

        runDuel(mode, args);
    }

    private static void validateStudents() {
        var report =
            new StudentSubmissionValidator()
                .validate();

        if (report.validatedBots().isEmpty()) {
            System.out.println(
                "Aucun bot étudiant compilé à valider."
            );
            return;
        }

        System.out.println(
            "Bots étudiants validés :"
        );

        report.validatedBots().forEach(bot ->
            System.out.printf(
                "  - %s — %s (%s)%n",
                bot.metadata().botName(),
                bot.metadata().authorName(),
                bot.className()
            )
        );
    }




    private static void runAllStudentEloEstimates(
        String[] args
    ) {
        Map<String, BotFactory> students =
            BotCatalog.studentBots();

        if (students.isEmpty()) {
            throw new IllegalArgumentException(
                "Aucun bot étudiant mergé à estimer."
            );
        }

        IsolatedBotSettings isolationSettings =
            readIsolationSettings(args);

        List<String> studentKeys =
            new ArrayList<>(
                students.keySet()
            );

        studentKeys.sort(
            String::compareTo
        );

        List<String> referenceKeys =
            readReferenceKeys(
                args,
                ""
            );

        List<EloReferenceOpponent> references =
            referenceKeys.stream()
                .map(key -> {
                    double rating =
                        ReferenceEloCatalog.find(key)
                            .orElseThrow()
                            .rating();

                    return new EloReferenceOpponent(
                        tournamentParticipant(
                            key,
                            false,
                            isolationSettings
                        ),
                        rating
                    );
                })
                .toList();

        int gamesPerReference =
            readGamesPerReference(args);

        int maxPlies =
            readMaxPlies(args);

        long baseSeed =
            readSeed(args);

        double priorElo =
            readDoubleOption(
                args,
                "--prior-elo=",
                1_500.0
            );

        double priorSigma =
            readDoubleOption(
                args,
                "--prior-sigma=",
                800.0
            );

        List<BotEloEstimate> estimates =
            new ArrayList<>();

        int index = 0;

        for (String studentKey
            : studentKeys) {

            index++;

            TournamentParticipant target =
                tournamentParticipant(
                    studentKey,
                    true,
                    isolationSettings
                );

            long seed =
                baseSeed
                    ^ (
                        0x9E3779B97F4A7C15L
                            * index
                    );

            BotEloEstimate estimate =
                new BotEloEstimator()
                    .estimate(
                        target,
                        references,
                        new BotEloEstimateSettings(
                            gamesPerReference,
                            maxPlies,
                            seed,
                            priorElo,
                            priorSigma
                        )
                    );

            estimates.add(estimate);

            System.out.printf(
                Locale.ROOT,
                "[%d/%d] %s : %.0f IRIS-Elo (IC95 %.0f — %.0f)%n",
                index,
                studentKeys.size(),
                estimate.bot()
                    .botName(),
                estimate.estimatedRating(),
                estimate.confidence95Low(),
                estimate.confidence95High()
            );
        }

        new BotEloEstimateBatchReporter()
            .print(estimates);

        for (String arg : args) {
            if (arg.startsWith("--csv=")) {
                Path path =
                    Path.of(
                        arg.substring(
                            "--csv=".length()
                        )
                    );

                writeUtf8(
                    path,
                    new BotEloEstimateBatchCsvExporter()
                        .export(estimates),
                    "estimations Elo étudiantes"
                );
            }
        }
    }

    private static void runEloEstimate(
        String[] args
    ) {
        if (args.length < 2) {
            throw new IllegalArgumentException(
                "Usage : elo-estimate <bot> [--refs=a,b,c] [--games=N]"
            );
        }

        String targetKey =
            args[1].toLowerCase(Locale.ROOT);

        boolean forceIsolation =
            hasFlag(args, "--isolated");

        IsolatedBotSettings isolationSettings =
            readIsolationSettings(args);

        boolean targetIsolation =
            forceIsolation
                || targetKey.startsWith("student-");

        TournamentParticipant target =
            tournamentParticipant(
                targetKey,
                targetIsolation,
                isolationSettings
            );

        List<String> referenceKeys =
            readReferenceKeys(
                args,
                targetKey
            );

        List<EloReferenceOpponent> references =
            referenceKeys.stream()
                .map(key -> {
                    double rating =
                        ReferenceEloCatalog.find(key)
                            .orElseThrow(() ->
                                new IllegalArgumentException(
                                    "Le bot '"
                                        + key
                                        + "' n'est pas une référence Elo calibrée."
                                )
                            )
                            .rating();

                    TournamentParticipant participant =
                        tournamentParticipant(
                            key,
                            forceIsolation,
                            isolationSettings
                        );

                    return new EloReferenceOpponent(
                        participant,
                        rating
                    );
                })
                .toList();

        BotEloEstimateSettings settings =
            new BotEloEstimateSettings(
                readGamesPerReference(args),
                readMaxPlies(args),
                readSeed(args),
                readDoubleOption(
                    args,
                    "--prior-elo=",
                    1_500.0
                ),
                readDoubleOption(
                    args,
                    "--prior-sigma=",
                    800.0
                )
            );

        if (targetIsolation
            || forceIsolation) {
            printIsolationSettings(
                isolationSettings
            );
        }

        int expectedGames =
            settings.expectedGameCount(
                references.size()
            );

        System.out.printf(
            Locale.ROOT,
            "Estimation Elo : %s contre %d référence(s), %d parties%n",
            targetKey,
            references.size(),
            expectedGames
        );

        BotEloEstimate estimate =
            new BotEloEstimator().estimate(
                target,
                references,
                settings
            );

        new BotEloEstimateReporter()
            .print(estimate);

        for (String arg : args) {
            if (arg.startsWith("--csv=")) {
                Path path =
                    Path.of(
                        arg.substring(
                            "--csv=".length()
                        )
                    );

                writeUtf8(
                    path,
                    new BotEloEstimateCsvExporter()
                        .export(estimate),
                    "estimation Elo"
                );
            }
        }
    }

    private static List<String> readReferenceKeys(
        String[] args,
        String targetKey
    ) {
        for (String arg : args) {
            if (arg.startsWith("--refs=")) {
                String raw =
                    arg.substring(
                        "--refs=".length()
                    );

                List<String> keys =
                    java.util.Arrays.stream(
                        raw.split(",")
                    )
                    .map(String::trim)
                    .filter(value ->
                        !value.isEmpty()
                    )
                    .map(value ->
                        value.toLowerCase(
                            Locale.ROOT
                        )
                    )
                    .filter(value ->
                        !value.equals(targetKey)
                    )
                    .distinct()
                    .toList();

                if (keys.isEmpty()) {
                    throw new IllegalArgumentException(
                        "--refs doit contenir au moins une référence différente du bot évalué"
                    );
                }

                return keys;
            }
        }

        List<String> keys =
            new ArrayList<>(
                ReferenceEloCatalog.all()
                    .keySet()
            );

        keys.remove(targetKey);

        keys.sort(
            java.util.Comparator.comparingInt(
                key ->
                    ReferenceEloCatalog
                        .find(key)
                        .orElseThrow()
                        .rating()
            )
        );

        if (keys.isEmpty()) {
            throw new IllegalArgumentException(
                "Aucune référence Elo disponible"
            );
        }

        return List.copyOf(keys);
    }

    private static int readGamesPerReference(
        String[] args
    ) {
        int games =
            readPositiveIntOption(
                args,
                "--games=",
                4
            );

        if (games % 2 != 0) {
            throw new IllegalArgumentException(
                "--games doit être pair pour équilibrer les couleurs"
            );
        }

        return games;
    }

    private static void runEloBenchmark(
        String[] args
    ) {
        boolean isolated =
            hasFlag(args, "--isolated");

        IsolatedBotSettings isolationSettings =
            readIsolationSettings(args);

        boolean all =
            hasFlag(args, "--all");

        boolean studentsOnly =
            hasFlag(args, "--students");

        if (all && studentsOnly) {
            throw new IllegalArgumentException(
                "Utilisez soit --all, soit --students, pas les deux."
            );
        }

        List<String> requestedBots =
            tournamentBotNames(args);

        final boolean effectiveIsolation =
            isolated
                || studentsOnly
                || requestedBots.stream()
                    .anyMatch(name ->
                        name.toLowerCase(Locale.ROOT)
                            .startsWith("student-")
                    )
                || (
                    all
                        && !BotCatalog.studentBots()
                            .isEmpty()
                );

        List<String> keys;

        if (studentsOnly) {
            keys = new ArrayList<>(
                BotCatalog.studentBots()
                    .keySet()
            );

            if (keys.size() < 2) {
                throw new IllegalArgumentException(
                    "Le benchmark étudiant nécessite au moins deux bots étudiants mergés."
                );
            }
        } else if (all) {
            keys = new ArrayList<>(
                BotCatalog.all().keySet()
            );
        } else if (!requestedBots.isEmpty()) {
            if (requestedBots.size() < 2) {
                throw new IllegalArgumentException(
                    "Un benchmark Elo explicite nécessite au moins deux bots."
                );
            }

            keys = new ArrayList<>(
                requestedBots
            );
        } else {
            keys = new ArrayList<>(
                BotCatalog.referenceBots()
                    .keySet()
            );
        }

        keys.sort(String::compareTo);

        List<TournamentParticipant> participants =
            keys.stream()
                .map(key ->
                    tournamentParticipant(
                        key,
                        effectiveIsolation,
                        isolationSettings
                    )
                )
                .toList();

        Map<String, Double> resumeRatings =
            readEloRatings(args);

        EloBenchmarkSettings settings =
            new EloBenchmarkSettings(
                readDoubleOption(
                    args,
                    "--initial-elo=",
                    1_500.0
                ),
                readDoubleOption(
                    args,
                    "--k=",
                    24.0
                ),
                readGamesPerPairForElo(args),
                readMaxPlies(args),
                readSeed(args)
            );

        int expectedGames =
            settings.expectedGameCount(
                participants.size()
            );

        System.out.printf(
            Locale.ROOT,
            "Benchmark Elo : %d bots, %d parties, %.0f Elo initial, K=%.1f%n",
            participants.size(),
            expectedGames,
            settings.initialRating(),
            settings.kFactor()
        );

        if (effectiveIsolation) {
            printIsolationSettings(
                isolationSettings
            );
        }

        EloBenchmarkReporter reporter =
            new EloBenchmarkReporter();

        EloBenchmarkResult result =
            new EloBenchmark().run(
                participants,
                settings,
                resumeRatings,
                record ->
                    reporter.printProgress(
                        record,
                        expectedGames
                    )
            );

        reporter.print(result);

        writeEloExports(
            args,
            result
        );
    }

    private static int readGamesPerPairForElo(
        String[] args
    ) {
        int value =
            readPositiveIntOption(
                args,
                "--games=",
                4
            );

        if (value % 2 != 0) {
            throw new IllegalArgumentException(
                "--games doit être pair pour équilibrer les couleurs dans le benchmark Elo"
            );
        }

        return value;
    }

    private static double readDoubleOption(
        String[] args,
        String prefix,
        double defaultValue
    ) {
        for (String arg : args) {
            if (arg.startsWith(prefix)) {
                double value =
                    Double.parseDouble(
                        arg.substring(
                            prefix.length()
                        )
                    );

                if (!Double.isFinite(value)
                    || value <= 0.0) {
                    throw new IllegalArgumentException(
                        prefix
                            + " doit être strictement positif"
                    );
                }

                return value;
            }
        }

        return defaultValue;
    }


    private static Map<String, Double> readEloRatings(
        String[] args
    ) {
        for (String arg : args) {
            if (arg.startsWith("--ratings-in=")) {
                Path path =
                    Path.of(
                        arg.substring(
                            "--ratings-in=".length()
                        )
                    );

                Map<String, Double> ratings =
                    EloRatingsCsv.read(path);

                System.out.println(
                    "Reprise Elo : "
                        + ratings.size()
                        + " rating(s) chargés depuis "
                        + path.toAbsolutePath()
                );

                return ratings;
            }
        }

        return Map.of();
    }

    private static void writeEloExports(
        String[] args,
        EloBenchmarkResult result
    ) {
        EloBenchmarkCsvExporter exporter =
            new EloBenchmarkCsvExporter();

        for (String arg : args) {
            if (arg.startsWith("--csv=")) {
                Path path = Path.of(
                    arg.substring(
                        "--csv=".length()
                    )
                );

                writeUtf8(
                    path,
                    exporter.finalStandings(
                        result
                    ),
                    "classement Elo"
                );
            }

            if (arg.startsWith("--history=")) {
                Path path = Path.of(
                    arg.substring(
                        "--history=".length()
                    )
                );

                writeUtf8(
                    path,
                    exporter.history(
                        result
                    ),
                    "historique Elo"
                );
            }
        }
    }

    private static void writeUtf8(
        Path path,
        String content,
        String label
    ) {
        try {
            Files.writeString(
                path,
                content,
                StandardCharsets.UTF_8
            );
        } catch (IOException exception) {
            throw new IllegalStateException(
                "Impossible d'écrire "
                    + label
                    + " : "
                    + path,
                exception
            );
        }

        System.out.println(
            label
                + " : "
                + path.toAbsolutePath()
        );
    }

    private static void runDuel(
        String mode,
        String[] args
    ) throws IOException {
        boolean isolated =
            hasFlag(args, "--isolated");

        IsolatedBotSettings settings =
            readIsolationSettings(args);

        BotPlayerFactory<? extends BotPlayer> white =
            playerFactory(
                args[1],
                isolated,
                settings
            );

        BotPlayerFactory<? extends BotPlayer> black =
            playerFactory(
                args[2],
                isolated,
                settings
            );

        MatchConfiguration configuration =
            new MatchConfiguration(
                readMaxPlies(args),
                readSeed(args),
                java.util.Optional.empty()
            );

        if (isolated) {
            printIsolationSettings(settings);
        }

        MatchRunner runner = new MatchRunner();

        switch (mode) {
            case "console" -> runner.play(
                white,
                black,
                configuration,
                new ConsoleMatchListener()
            );

            case "pgn" -> {
                MatchResult result =
                    runner.play(
                        white,
                        black,
                        configuration
                    );

                String pgn =
                    new PgnExporter().export(result);

                Path output =
                    readPgnOutput(args);

                if (output != null) {
                    Files.writeString(
                        output,
                        pgn,
                        StandardCharsets.UTF_8
                    );

                    System.out.println(
                        "PGN écrit dans : "
                            + output.toAbsolutePath()
                    );
                } else {
                    System.out.println(pgn);
                }

                printSummary(result);
            }

            case "gui" -> {
                MatchResult result =
                    runner.play(
                        white,
                        black,
                        configuration
                    );

                printSummary(result);
                SwingMatchViewer.show(result);
            }

            default -> {
                System.err.println(
                    "Mode inconnu : " + mode
                );
                printUsage();
            }
        }
    }

    private static void runTournament(
        String[] args
    ) {
        boolean isolated =
            hasFlag(args, "--isolated");

        IsolatedBotSettings isolationSettings =
            readIsolationSettings(args);

        List<String> requestedBots =
            tournamentBotNames(args);

        boolean all =
            hasFlag(args, "--all");

        boolean studentsOnly =
            hasFlag(args, "--students");

        final boolean effectiveIsolation =
            isolated
                || studentsOnly
                || requestedBots.stream()
                    .anyMatch(name ->
                        name.toLowerCase(Locale.ROOT)
                            .startsWith("student-")
                    )
                || (
                    all
                        && !BotCatalog.studentBots()
                            .isEmpty()
                );

        if (all && studentsOnly) {
            throw new IllegalArgumentException(
                "Utilisez soit --all, soit --students, pas les deux."
            );
        }

        List<String> botNames;

        if (studentsOnly) {
            botNames = new ArrayList<>(
                BotCatalog.studentBots().keySet()
            );
            botNames.sort(String::compareTo);

            if (botNames.size() < 2) {
                throw new IllegalArgumentException(
                    "Le tournoi étudiant nécessite au moins deux bots étudiants mergés."
                );
            }
        } else if (all) {
            botNames = new ArrayList<>(
                BotCatalog.all().keySet()
            );
            botNames.sort(String::compareTo);
        } else {
            if (requestedBots.size() < 2) {
                throw new IllegalArgumentException(
                    "Le tournoi nécessite au moins deux bots "
                        + "ou l'option --all"
                );
            }

            botNames = List.copyOf(
                requestedBots
            );
        }

        List<TournamentParticipant> participants =
            botNames.stream()
                .map(name ->
                    tournamentParticipant(
                        name,
                        effectiveIsolation,
                        isolationSettings
                    )
                )
                .toList();

        if (effectiveIsolation) {
            printIsolationSettings(
                isolationSettings
            );
        }

        RoundRobinResult result =
            new RoundRobinTournament().play(
                participants,
                new RoundRobinConfiguration(
                    readGamesPerPair(args),
                    readMaxPlies(args),
                    readSeed(args)
                )
            );

        new ConsoleTournamentReporter()
            .print(result);

        writeTournamentExports(
            args,
            result
        );
    }

    private static TournamentParticipant
        tournamentParticipant(
            String name,
            boolean isolated,
            IsolatedBotSettings settings
        ) {

        String key =
            name.toLowerCase(Locale.ROOT);

        BotFactory factory =
            requireBot(name);

        BotMetadata metadata =
            factory.create().metadata();

        if (!isolated) {
            return new TournamentParticipant(
                key,
                factory
            );
        }

        return TournamentParticipant.isolated(
            key,
            requireBotClass(name),
            metadata,
            settings
        );
    }

    private static BotPlayerFactory<? extends BotPlayer>
        playerFactory(
            String name,
            boolean isolated,
            IsolatedBotSettings settings
        ) {

        if (isolated) {
            return new IsolatedBotPlayerFactory(
                requireBotClass(name),
                settings
            );
        }

        return BotPlayers.inProcess(
            requireBot(name)
        );
    }

    private static void writeTournamentExports(
        String[] args,
        RoundRobinResult result
    ) {
        for (String arg : args) {
            if (arg.startsWith("--pgn=")) {
                Path path = Path.of(
                    arg.substring(
                        "--pgn=".length()
                    )
                );

                try {
                    Files.writeString(
                        path,
                        new RoundRobinPgnExporter()
                            .export(result),
                        StandardCharsets.UTF_8
                    );
                } catch (IOException exception) {
                    throw new IllegalStateException(
                        "Impossible d'écrire le PGN du tournoi : "
                            + path,
                        exception
                    );
                }

                System.out.println(
                    "PGN tournoi : "
                        + path.toAbsolutePath()
                );
            }

            if (arg.startsWith("--csv=")) {
                Path path = Path.of(
                    arg.substring(
                        "--csv=".length()
                    )
                );

                try {
                    Files.writeString(
                        path,
                        new StandingsCsvExporter()
                            .export(result),
                        StandardCharsets.UTF_8
                    );
                } catch (IOException exception) {
                    throw new IllegalStateException(
                        "Impossible d'écrire le CSV du classement : "
                            + path,
                        exception
                    );
                }

                System.out.println(
                    "CSV classement : "
                        + path.toAbsolutePath()
                );
            }
        }
    }

    private static BotFactory requireBot(
        String name
    ) {
        return BotCatalog.find(name)
            .orElseThrow(() ->
                unknownBot(name)
            );
    }

    private static Class<? extends ChessBot>
        requireBotClass(String name) {

        return BotCatalog.findClass(name)
            .orElseThrow(() ->
                unknownBot(name)
            );
    }

    private static IllegalArgumentException
        unknownBot(String name) {

        return new IllegalArgumentException(
            "Bot inconnu : "
                + name
                + ". Utilisez 'list' pour voir les bots."
        );
    }

    private static List<String> tournamentBotNames(
        String[] args
    ) {
        List<String> names =
            new ArrayList<>();

        for (int index = 1;
            index < args.length;
            index++) {

            String arg = args[index];

            if (!arg.startsWith("--")) {
                names.add(arg);
            }
        }

        return names;
    }

    private static boolean hasFlag(
        String[] args,
        String flag
    ) {
        return java.util.Arrays.stream(args)
            .anyMatch(
                value ->
                    flag.equalsIgnoreCase(value)
            );
    }

    private static IsolatedBotSettings
        readIsolationSettings(
            String[] args
        ) {

        return new IsolatedBotSettings(
            Duration.ofMillis(
                readPositiveIntOption(
                    args,
                    "--startup-timeout-ms=",
                    DEFAULT_STARTUP_TIMEOUT_MS
                )
            ),
            Duration.ofMillis(
                readPositiveIntOption(
                    args,
                    "--timeout-ms=",
                    DEFAULT_TIMEOUT_MS
                )
            ),
            readPositiveIntOption(
                args,
                "--heap-mb=",
                DEFAULT_HEAP_MB
            )
        );
    }

    private static long readSeed(
        String[] args
    ) {
        for (String arg : args) {
            if (arg.startsWith("--seed=")) {
                return Long.parseLong(
                    arg.substring(
                        "--seed=".length()
                    )
                );
            }
        }

        return DEFAULT_SEED;
    }

    private static int readGamesPerPair(
        String[] args
    ) {
        return readPositiveIntOption(
            args,
            "--games=",
            DEFAULT_GAMES_PER_PAIR
        );
    }

    private static int readMaxPlies(
        String[] args
    ) {
        return readPositiveIntOption(
            args,
            "--max-plies=",
            DEFAULT_MAX_PLIES
        );
    }

    private static int readPositiveIntOption(
        String[] args,
        String prefix,
        int defaultValue
    ) {
        for (String arg : args) {
            if (arg.startsWith(prefix)) {
                int value = Integer.parseInt(
                    arg.substring(
                        prefix.length()
                    )
                );

                if (value <= 0) {
                    throw new IllegalArgumentException(
                        prefix
                            + " doit être strictement positif"
                    );
                }

                return value;
            }
        }

        return defaultValue;
    }

    private static Path readPgnOutput(
        String[] args
    ) {
        for (
            int index = 3;
            index < args.length;
            index++
        ) {
            String arg = args[index];

            if (!arg.startsWith("--")) {
                return Path.of(arg);
            }
        }

        return null;
    }

    private static void printIsolationSettings(
        IsolatedBotSettings settings
    ) {
        System.out.printf(
            Locale.ROOT,
            "Isolation JVM : timeout=%d ms, démarrage=%d ms, heap=%d MiB%n",
            settings.decisionTimeoutMillis(),
            settings.startupTimeoutMillis(),
            settings.maxHeapMegabytes()
        );
    }

    private static void printSummary(
        MatchResult result
    ) {
        System.out.println();
        System.out.println(
            result.white().botName()
                + " vs "
                + result.black().botName()
        );
        System.out.println(
            "Résultat : "
                + result.pgnResult()
        );
        System.out.println(
            "Coups : "
                + result.fullMovesPlayed()
                + " ("
                + result.pliesPlayed()
                + " demi-coups)"
        );
        System.out.println(
            "Fin : "
                + result.termination()
        );

        result.incident().ifPresent(
            incident ->
                System.out.println(
                    "Incident : "
                        + incident.summary()
                )
        );
    }

    private static void printRatings() {
        System.out.println(
            "IRIS-Elo calibré des bots de référence"
        );
        System.out.println(
            "(calibration empirique interne, sans équivalence FIDE)"
        );
        System.out.println();

        ReferenceEloCatalog.all()
            .entrySet()
            .stream()
            .sorted(
                Map.Entry.comparingByValue(
                    java.util.Comparator.comparingInt(
                        rating ->
                            rating.rating()
                    )
                )
            )
            .forEach(entry ->
                System.out.printf(
                    Locale.ROOT,
                    "  %-12s %4d  %-13s — %s%n",
                    entry.getKey(),
                    entry.getValue().rating(),
                    entry.getValue().level(),
                    entry.getValue().explanation()
                )
            );
    }

    private static void printBots() {
        System.out.println(
            "Bots disponibles :"
        );

        for (
            Map.Entry<String, BotFactory> entry
            : BotCatalog.all().entrySet()
        ) {
            var metadata =
                entry.getValue()
                    .create()
                    .metadata();

            String rating =
                ReferenceEloCatalog.find(
                    entry.getKey()
                )
                .map(value ->
                    " — IRIS-Elo "
                        + value.rating()
                )
                .orElse("");

            System.out.printf(
                "  %-22s %-20s%s — %s%n",
                entry.getKey(),
                metadata.botName(),
                rating,
                metadata.description()
            );
        }
    }

    private static void printUsage() {
        System.out.println(
            """
            Chess Framework

            Usage :
              list
              ratings
              elo-benchmark [bot1 bot2 ...] [--students|--all] [options]
              elo-estimate <bot> [options]
              elo-estimate-all [options]
              validate-students
              console <blancs> <noirs> [options]
              pgn     <blancs> <noirs> [fichier.pgn] [options]
              gui     <blancs> <noirs> [options]
              tournament <bot1> <bot2> [...] [options]
              tournament --students [options]
              tournament --all [options]

            Options communes :
              --seed=N
              --max-plies=N

            Isolation JVM :
              --isolated
              --timeout-ms=N
              --startup-timeout-ms=N
              --heap-mb=N

            Options tournoi :
              --games=N
              --pgn=parties.pgn
              --csv=classement.csv

            Benchmark / estimation Elo :
              --games=N          nombre pair de parties par adversaire (défaut 4)
              --initial-elo=N    Elo initial d'un bot sans historique (défaut 1500)
              --ratings-in=file   reprend les Elo d'un précédent elo-final.csv
              --k=N              facteur K (défaut 24)
              --csv=elo.csv      classement final
              --history=hist.csv historique match par match

            Exemples :
              console tactical random
              console minimax random --isolated
              console minimax random --isolated --timeout-ms=3000 --heap-mb=256
              pgn tactical guardian partie.pgn --isolated
              gui architect tactical --isolated
              tournament random greedy tactical
              tournament positional lookahead minimax --games=2 --isolated
              tournament --students --games=4 --isolated
              tournament --all --games=2 --isolated --timeout-ms=3000
              elo-benchmark --games=4 --csv=elo.csv --history=elo-history.csv
              elo-benchmark --ratings-in=elo.csv --games=8 --csv=elo-next.csv
              elo-benchmark random greedy tactical --games=8
              elo-benchmark --students --games=8 --isolated
              elo-estimate student-deep-rabbit --games=4
              elo-estimate student-deep-rabbit --refs=random,tactical,guardian,minimax,lookahead --games=8 --csv=deep-rabbit-elo.csv
              elo-estimate-all --games=4 --csv=iris-student-ratings.csv
              tournament tactical positional --pgn=parties.pgn --csv=classement.csv

            Utilisez :
              list

            pour afficher les bots disponibles.
            """
        );
    }
}
