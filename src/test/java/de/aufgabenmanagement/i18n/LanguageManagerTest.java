package de.aufgabenmanagement.i18n;

import de.aufgabenmanagement.config.ConfigManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LanguageManagerTest {

    @TempDir
    Path tempDir;

    @Test
    void allBundlesContainExactlyTheSameKeys() {
        Set<String> reference = LanguageManager.loadBundle(Language.ENGLISH).keySet();
        for (Language language : Language.values()) {
            assertEquals(reference, LanguageManager.loadBundle(language).keySet(),
                    "Key mismatch in bundle for " + language);
        }
    }

    @ParameterizedTest
    @EnumSource(Language.class)
    void bundleIsLoadedForItsOwnLocaleEvenIfJvmLocaleDiffers(Language language) {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.JAPANESE);
            ResourceBundle bundle = LanguageManager.loadBundle(language);
            assertEquals(language.toLocale(), bundle.getLocale());
        } finally {
            Locale.setDefault(previous);
        }
    }

    @ParameterizedTest
    @EnumSource(Language.class)
    void everyMessageIsAValidPatternWithoutRawApostrophe(Language language) {
        ResourceBundle bundle = LanguageManager.loadBundle(language);
        for (String key : bundle.keySet()) {
            String pattern = bundle.getString(key);
            assertFalse(pattern.contains("'"), "MessageFormat would swallow ASCII apostrophe in " + key);
            assertDoesNotThrow(() -> new MessageFormat(pattern, language.toLocale()), "Invalid pattern: " + key);
        }
    }

    @Test
    void specialCharactersAreReadAsUtf8() {
        LanguageManager manager = new LanguageManager(
                new ConfigManager(tempDir.resolve("c.properties")), Language.GERMAN);
        assertEquals("Aufgabe löschen", manager.get("menu.delete"));

        manager.changeLanguage(Language.FRENCH);
        assertEquals("Afficher toutes les tâches", manager.get("menu.list"));
    }

    @Test
    void changeLanguageSwitchesTextsImmediatelyAndPersistsChoice() {
        ConfigManager config = new ConfigManager(tempDir.resolve("config.properties"));
        LanguageManager manager = new LanguageManager(config, Language.ENGLISH);
        assertEquals("Exit", manager.get("menu.exit"));

        assertTrue(manager.changeLanguage(Language.GERMAN));

        assertEquals(Language.GERMAN, manager.getCurrentLanguage());
        assertEquals("Beenden", manager.get("menu.exit"));
        assertEquals(Optional.of("de"), config.readLanguageCode());
    }

    @Test
    void placeholdersAreFilledAndNumbersAreNotGrouped() {
        LanguageManager manager = new LanguageManager(
                new ConfigManager(tempDir.resolve("c.properties")), Language.GERMAN);

        assertEquals("Aufgabe #1234 wurde gelöscht.", manager.get("task.delete.success", 1234));
    }

    @Test
    void missingKeyDoesNotCrash() {
        LanguageManager manager = new LanguageManager(
                new ConfigManager(tempDir.resolve("c.properties")), Language.ENGLISH);

        assertEquals("!does.not.exist!", manager.get("does.not.exist"));
    }
}
