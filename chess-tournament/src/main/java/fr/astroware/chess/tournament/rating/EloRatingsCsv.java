package fr.astroware.chess.tournament.rating;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Lecture d'un classement Elo précédemment exporté.
 *
 * <p>Le fichier attendu est celui produit par
 * {@link EloBenchmarkCsvExporter#finalStandings(EloBenchmarkResult)}.
 * Seules les colonnes {@code key} et {@code elo} sont nécessaires.</p>
 */
public final class EloRatingsCsv {

    private EloRatingsCsv() {
    }

    public static Map<String, Double> read(
        Path path
    ) {
        Objects.requireNonNull(
            path,
            "path must not be null"
        );

        try {
            return parse(
                Files.readString(
                    path,
                    StandardCharsets.UTF_8
                )
            );
        } catch (IOException exception) {
            throw new IllegalStateException(
                "Impossible de lire les Elo initiaux : "
                    + path,
                exception
            );
        }
    }

    public static Map<String, Double> parse(
        String csv
    ) {
        Objects.requireNonNull(
            csv,
            "csv must not be null"
        );

        List<String> lines =
            csv.lines()
                .filter(line ->
                    !line.isBlank()
                )
                .toList();

        if (lines.isEmpty()) {
            throw new IllegalArgumentException(
                "Le fichier Elo est vide"
            );
        }

        List<String> header =
            parseCsvLine(
                lines.getFirst()
            );

        int keyIndex =
            header.indexOf("key");

        int eloIndex =
            header.indexOf("elo");

        if (keyIndex < 0
            || eloIndex < 0) {
            throw new IllegalArgumentException(
                "Le CSV Elo doit contenir les colonnes key et elo"
            );
        }

        Map<String, Double> ratings =
            new LinkedHashMap<>();

        for (
            int lineIndex = 1;
            lineIndex < lines.size();
            lineIndex++
        ) {
            List<String> fields =
                parseCsvLine(
                    lines.get(lineIndex)
                );

            int required =
                Math.max(
                    keyIndex,
                    eloIndex
                );

            if (fields.size()
                <= required) {
                throw new IllegalArgumentException(
                    "Ligne CSV Elo incomplète : "
                        + lines.get(lineIndex)
                );
            }

            String key =
                fields.get(keyIndex)
                    .trim();

            if (key.isEmpty()) {
                throw new IllegalArgumentException(
                    "Clé de bot vide dans le CSV Elo"
                );
            }

            double rating;

            try {
                rating =
                    Double.parseDouble(
                        fields.get(eloIndex)
                    );
            } catch (
                NumberFormatException exception
            ) {
                throw new IllegalArgumentException(
                    "Elo invalide pour "
                        + key,
                    exception
                );
            }

            if (!Double.isFinite(rating)
                || rating <= 0.0) {
                throw new IllegalArgumentException(
                    "Elo invalide pour "
                        + key
                        + " : "
                        + rating
                );
            }

            if (ratings.put(
                key,
                rating
            ) != null) {
                throw new IllegalArgumentException(
                    "Clé de bot dupliquée dans le CSV Elo : "
                        + key
                );
            }
        }

        return Map.copyOf(ratings);
    }

    private static List<String> parseCsvLine(
        String line
    ) {
        java.util.ArrayList<String> fields =
            new java.util.ArrayList<>();

        StringBuilder current =
            new StringBuilder();

        boolean quoted = false;

        for (
            int index = 0;
            index < line.length();
            index++
        ) {
            char ch =
                line.charAt(index);

            if (ch == '"') {
                if (quoted
                    && index + 1
                        < line.length()
                    && line.charAt(
                        index + 1
                    ) == '"') {

                    current.append('"');
                    index++;
                } else {
                    quoted = !quoted;
                }

                continue;
            }

            if (ch == ','
                && !quoted) {
                fields.add(
                    current.toString()
                );
                current.setLength(0);
                continue;
            }

            current.append(ch);
        }

        if (quoted) {
            throw new IllegalArgumentException(
                "Guillemets CSV non fermés : "
                    + line
            );
        }

        fields.add(
            current.toString()
        );

        return List.copyOf(fields);
    }
}
