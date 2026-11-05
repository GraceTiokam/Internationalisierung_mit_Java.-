package de.aufgabenmanagement.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigManagerTest {

    @TempDir
    Path tempDir;

    @Test
    void missingFileYieldsEmptyValue() {
        ConfigManager config = new ConfigManager(tempDir.resolve("config.properties"));

        assertTrue(config.readLanguageCode().isEmpty());
    }

    @Test
    void saveCreatesFileAndValueCanBeReadBack() {
        Path file = tempDir.resolve("config.properties");
        ConfigManager config = new ConfigManager(file);

        assertTrue(config.saveLanguageCode("fr"));
        assertTrue(Files.exists(file));
        assertEquals(Optional.of("fr"), config.readLanguageCode());
        assertEquals(Optional.of("fr"), new ConfigManager(file).readLanguageCode());
    }

    @Test
    void saveKeepsOtherEntries() throws IOException {
        Path file = tempDir.resolve("config.properties");
        Files.writeString(file, "language=de\ntheme=dark\n", StandardCharsets.UTF_8);

        new ConfigManager(file).saveLanguageCode("en");

        String content = Files.readString(file, StandardCharsets.UTF_8);
        assertTrue(content.contains("language=en"));
        assertTrue(content.contains("theme=dark"));
    }

    @Test
    void valueIsTrimmedAndBlankValueIsIgnored() throws IOException {
        Path file = tempDir.resolve("config.properties");
        Files.writeString(file, "language =  de  \n", StandardCharsets.UTF_8);
        assertEquals(Optional.of("de"), new ConfigManager(file).readLanguageCode());

        Files.writeString(file, "language=\n", StandardCharsets.UTF_8);
        assertTrue(new ConfigManager(file).readLanguageCode().isEmpty());
    }

    @Test
    void corruptedFileIsHandledGracefully() throws IOException {
        Path file = tempDir.resolve("config.properties");
        // Malformed unicode escape makes Properties.load throw IllegalArgumentException.
        Files.writeString(file, "language=\\uZZZZ\n", StandardCharsets.UTF_8);
        assertTrue(new ConfigManager(file).readLanguageCode().isEmpty());

        // Binary garbage must not crash either.
        Files.write(file, new byte[]{(byte) 0xFF, (byte) 0xFE, 0x00, (byte) 0xC3});
        ConfigManager config = new ConfigManager(file);
        assertTrue(config.readLanguageCode().isEmpty());

        // A corrupted file can be repaired by saving a new value.
        assertTrue(config.saveLanguageCode("de"));
        assertEquals(Optional.of("de"), config.readLanguageCode());
    }

    @Test
    void unwritableLocationReturnsFalseInsteadOfThrowing() {
        // The config path is an existing directory, so writing must fail.
        ConfigManager config = new ConfigManager(tempDir);

        assertFalse(config.saveLanguageCode("de"));
        assertTrue(config.readLanguageCode().isEmpty());
    }
}
