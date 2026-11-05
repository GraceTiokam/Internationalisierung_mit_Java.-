package de.aufgabenmanagement.ui;

import de.aufgabenmanagement.config.ConfigManager;
import de.aufgabenmanagement.i18n.Language;
import de.aufgabenmanagement.i18n.LanguageManager;
import de.aufgabenmanagement.service.TaskManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drives the console UI with scripted input to verify complete user flows.
 */
class ConsoleUITest {

    @TempDir
    Path tempDir;

    private TaskManager taskManager;
    private ConfigManager config;

    private String runSession(Language language, String... lines) {
        taskManager = new TaskManager();
        config = new ConfigManager(tempDir.resolve("config.properties"));
        LanguageManager i18n = new LanguageManager(config, language);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        BufferedReader in = new BufferedReader(new StringReader(String.join("\n", lines) + "\n"));

        new ConsoleUI(taskManager, i18n, in, out).run();
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void addListToggleAndDeleteTask() {
        String output = runSession(Language.ENGLISH,
                "1", "Write report", "Quarterly numbers",
                "4", "1",
                "2",
                "3", "1",
                "2",
                "0");

        assertTrue(output.contains("Task #1 “Write report” was added successfully."));
        assertTrue(output.contains("Task #1 is now marked as completed."));
        assertTrue(output.contains("#1 [completed] Write report"));
        assertTrue(output.contains("Description: Quarterly numbers"));
        assertTrue(output.contains("Task #1 was deleted."));
        assertTrue(output.contains("There are no tasks yet."));
        assertTrue(output.contains("Goodbye!"));
    }

    @Test
    void invalidInputIsReportedWithoutCrashing() {
        String output = runSession(Language.GERMAN,
                "abc", "9",
                "1", "   ",
                "3", "77",
                "3", "keine-zahl",
                "0");

        assertTrue(output.contains("Ungültige Auswahl „abc“"));
        assertTrue(output.contains("Ungültige Auswahl „9“"));
        assertTrue(output.contains("Der Titel darf nicht leer sein."));
        assertTrue(output.contains("Es gibt keine Aufgabe mit der ID 77."));
        assertTrue(output.contains("„keine-zahl“ ist keine gültige ID."));
        assertEquals(0, taskManager.getTaskCount());
    }

    @Test
    void languageSwitchUpdatesMenuAtRuntimeAndPersistsChoice() {
        String output = runSession(Language.ENGLISH,
                "5", "2",   // switch to French
                "0");

        int switchPosition = output.indexOf("La langue a été changée en français.");
        assertTrue(switchPosition > 0);
        String afterSwitch = output.substring(switchPosition);
        assertTrue(afterSwitch.contains("Menu principal"));
        assertTrue(afterSwitch.contains("Ajouter une tâche"));
        assertTrue(afterSwitch.contains("Au revoir !"));
        assertEquals(Optional.of("fr"), config.readLanguageCode());
    }

    @Test
    void endOfInputExitsGracefully() {
        String output = runSession(Language.FRENCH, "1");

        assertTrue(output.contains("Au revoir !"));
    }
}
