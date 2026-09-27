package fr.astroware.chess.tournament.cli;

import fr.astroware.chess.bots.students.ValidatorFixtureBot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BotCatalogTest {

    @Test
    void studentCatalogContainsOnlyAutomaticallyDiscoveredStudentBots() {
        assertTrue(
            BotCatalog.studentBots()
                .containsKey(
                    "student-validator-fixture"
                )
        );

        assertFalse(
            BotCatalog.studentBots()
                .containsKey("random")
        );

        assertTrue(
            BotCatalog.findClass(
                "student-validator-fixture"
            )
            .orElseThrow()
            .equals(
                ValidatorFixtureBot.class
            )
        );
    }

    @Test
    void referenceCatalogDoesNotContainStudentBots() {
        assertTrue(
            BotCatalog.referenceBots()
                .containsKey("random")
        );

        assertFalse(
            BotCatalog.referenceBots()
                .keySet()
                .stream()
                .anyMatch(key ->
                    key.startsWith("student-")
                )
        );
    }
}
