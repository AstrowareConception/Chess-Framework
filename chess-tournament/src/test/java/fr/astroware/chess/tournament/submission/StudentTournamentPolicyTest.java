package fr.astroware.chess.tournament.submission;

import fr.astroware.chess.bot.api.BotMetadata;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertThrows;

class StudentTournamentPolicyTest {

    @Test
    void rejectsSecondBotFromSameParticipant() {
        var seenAuthors =
            new HashSet<String>();

        StudentTournamentPolicy.requireUniqueParticipant(
            new BotMetadata(
                "First Bot",
                "Alice Dupont",
                "Première soumission"
            ),
            seenAuthors
        );

        assertThrows(
            IllegalStateException.class,
            () ->
                StudentTournamentPolicy.requireUniqueParticipant(
                    new BotMetadata(
                        "Second Bot",
                        "  alice   DUPONT ",
                        "Tentative de seconde soumission"
                    ),
                    seenAuthors
                )
        );
    }

    @Test
    void requiresFullParticipantName() {
        assertThrows(
            IllegalStateException.class,
            () ->
                StudentTournamentPolicy.requireUniqueParticipant(
                    new BotMetadata(
                        "Solo",
                        "Alice",
                        "Identité incomplète"
                    ),
                    new HashSet<>()
                )
        );
    }
}
