package fr.astroware.chess.tournament.submission;

import fr.astroware.chess.bot.api.BotMetadata;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Règles d'identité propres au tournoi étudiant IRIS Nice.
 */
public final class StudentTournamentPolicy {

    private StudentTournamentPolicy() {
    }

    /**
     * Vérifie l'identité réelle et l'unicité du participant.
     *
     * <p>Le tournoi autorise exactement un bot par participant. La classe ou
     * la promotion n'intervient pas dans cette règle.</p>
     */
    public static void requireUniqueParticipant(
        BotMetadata metadata,
        Set<String> seenAuthors
    ) {
        Objects.requireNonNull(
            metadata,
            "metadata must not be null"
        );
        Objects.requireNonNull(
            seenAuthors,
            "seenAuthors must not be null"
        );

        String author =
            metadata.authorName().trim();

        if (!author.contains(" ")) {
            throw new IllegalStateException(
                "authorName doit contenir le nom complet du participant "
                    + "(prénom et nom)"
            );
        }

        String normalized =
            author.toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim();

        if (!seenAuthors.add(normalized)) {
            throw new IllegalStateException(
                "l'auteur '"
                    + author
                    + "' possède déjà un autre bot. "
                    + "Le tournoi IRIS Nice autorise un seul bot par participant."
            );
        }
    }
}
