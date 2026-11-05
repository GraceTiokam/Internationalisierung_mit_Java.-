package de.aufgabenmanagement.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/**
 * Reads and writes the application configuration file ({@code config.properties}).
 * The file lives outside the JAR (by default in the working directory) so it
 * stays editable and persists between runs. All errors are handled gracefully:
 * an unreadable or corrupted file simply yields no value.
 */
public class ConfigManager {

    public static final String DEFAULT_FILE_NAME = "config.properties";
    public static final String LANGUAGE_KEY = "language";

    private static final String FILE_COMMENT = "Aufgabenmanagement configuration";

    private final Path configFile;

    public ConfigManager(Path configFile) {
        this.configFile = Objects.requireNonNull(configFile, "configFile");
    }

    /** Configuration file in the current working directory. */
    public static ConfigManager inWorkingDirectory() {
        return new ConfigManager(Path.of(DEFAULT_FILE_NAME));
    }

    public Path getConfigFile() {
        return configFile;
    }

    /**
     * @return the raw (unvalidated) language value, or empty if the file is
     *         missing, unreadable, corrupted or has no non-blank entry
     */
    public Optional<String> readLanguageCode() {
        return Optional.ofNullable(load().getProperty(LANGUAGE_KEY))
                .map(String::strip)
                .filter(value -> !value.isEmpty());
    }

    /**
     * Stores the language code, creating the file if necessary and keeping any
     * other entries already present.
     *
     * @return {@code true} if the file was written successfully
     */
    public boolean saveLanguageCode(String languageCode) {
        Objects.requireNonNull(languageCode, "languageCode");
        Properties properties = load();
        properties.setProperty(LANGUAGE_KEY, languageCode);
        try {
            Path parent = configFile.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (Writer writer = Files.newBufferedWriter(configFile, StandardCharsets.UTF_8)) {
                properties.store(writer, FILE_COMMENT);
            }
            return true;
        } catch (IOException | SecurityException e) {
            return false;
        }
    }

    /** Loads the file; returns empty properties on any problem. */
    private Properties load() {
        Properties properties = new Properties();
        if (!Files.isRegularFile(configFile)) {
            return properties;
        }
        try (Reader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IOException | IllegalArgumentException | SecurityException e) {
            // Unreadable, invalid UTF-8 or malformed \\u escapes: treat as empty.
            return new Properties();
        }
        return properties;
    }
}
