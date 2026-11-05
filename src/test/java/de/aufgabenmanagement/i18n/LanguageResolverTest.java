package de.aufgabenmanagement.i18n;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LanguageResolverTest {

    private static final String[] NO_ARGS = {};

    @Test
    void commandLineArgumentOverridesConfigAndJvm() {
        LanguageSelection selection = LanguageResolver.resolve(
                new String[]{"fr"}, Optional.of("de"), Locale.ENGLISH);

        assertEquals(Language.FRENCH, selection.language());
        assertEquals(LanguageSource.COMMAND_LINE, selection.source());
    }

    @Test
    void configOverridesJvmWhenNoArgument() {
        LanguageSelection selection = LanguageResolver.resolve(NO_ARGS, Optional.of("de"), Locale.FRENCH);

        assertEquals(Language.GERMAN, selection.language());
        assertEquals(LanguageSource.CONFIG_FILE, selection.source());
    }

    @Test
    void invalidArgumentFallsBackToConfigAndIsReported() {
        LanguageSelection selection = LanguageResolver.resolve(
                new String[]{"xx"}, Optional.of("fr"), Locale.GERMAN);

        assertEquals(Language.FRENCH, selection.language());
        assertEquals(LanguageSource.CONFIG_FILE, selection.source());
        assertEquals(Optional.of("xx"), selection.rejectedArgument());
    }

    @Test
    void supportedJvmLocaleIsUsedWithoutArgumentAndConfig() {
        LanguageSelection selection = LanguageResolver.resolve(NO_ARGS, Optional.empty(), Locale.GERMANY);

        assertEquals(Language.GERMAN, selection.language());
        assertEquals(LanguageSource.JVM_DEFAULT, selection.source());
    }

    @Test
    void invalidConfigFallsBackToJvmAndIsReported() {
        LanguageSelection selection = LanguageResolver.resolve(NO_ARGS, Optional.of("klingon"),
                Locale.CANADA_FRENCH);

        assertEquals(Language.FRENCH, selection.language());
        assertEquals(LanguageSource.JVM_DEFAULT, selection.source());
        assertEquals(Optional.of("klingon"), selection.rejectedConfigValue());
    }

    @Test
    void unsupportedJvmLocaleFallsBackToEnglish() {
        LanguageSelection selection = LanguageResolver.resolve(NO_ARGS, Optional.empty(), Locale.JAPANESE);

        assertEquals(Language.ENGLISH, selection.language());
        assertEquals(LanguageSource.FALLBACK, selection.source());
    }

    @Test
    void everythingInvalidFallsBackToEnglish() {
        LanguageSelection selection = LanguageResolver.resolve(
                new String[]{""}, Optional.of("??"), Locale.ROOT);

        assertEquals(Language.ENGLISH, selection.language());
        assertEquals(LanguageSource.FALLBACK, selection.source());
        assertTrue(selection.rejectedArgument().isPresent());
        assertTrue(selection.rejectedConfigValue().isPresent());
    }

    @Test
    void nullInputsAreHandledSafely() {
        LanguageSelection selection = LanguageResolver.resolve(null, null, null);

        assertEquals(Language.ENGLISH, selection.language());
        assertEquals(LanguageSource.FALLBACK, selection.source());
    }

    @ParameterizedTest
    @CsvSource({"de, GERMAN", "DE, GERMAN", "' fr ', FRENCH", "En, ENGLISH"})
    void argumentIsParsedCaseInsensitively(String argument, Language expected) {
        LanguageSelection selection = LanguageResolver.resolve(
                new String[]{argument}, Optional.empty(), Locale.JAPANESE);

        assertEquals(expected, selection.language());
        assertEquals(LanguageSource.COMMAND_LINE, selection.source());
    }
}
