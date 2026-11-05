package de.aufgabenmanagement.i18n;

import de.aufgabenmanagement.config.ConfigManager;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Holds the active language and its {@link ResourceBundle}; switches the
 * language at runtime and persists the choice via {@link ConfigManager}.
 * <p>
 * All messages are formatted with {@link MessageFormat}, so translations use
 * {@code {0}} placeholders. Apostrophes in translations are written as the
 * typographic character ’ to avoid MessageFormat's quote handling.
 */
public class LanguageManager {

    public static final String BUNDLE_BASE_NAME = "messages";

    /** Prevents silently falling back to the JVM locale's bundle. */
    private static final ResourceBundle.Control NO_FALLBACK =
            ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES);

    private final ConfigManager configManager;
    private Language currentLanguage;
    private ResourceBundle bundle;

    public LanguageManager(ConfigManager configManager, Language initialLanguage) {
        this.configManager = Objects.requireNonNull(configManager, "configManager");
        applyLanguage(Objects.requireNonNull(initialLanguage, "initialLanguage"));
    }

    public Language getCurrentLanguage() {
        return currentLanguage;
    }

    /**
     * Switches to the given language immediately and saves it to the configuration file.
     *
     * @return {@code true} if the choice was persisted successfully
     */
    public boolean changeLanguage(Language language) {
        applyLanguage(Objects.requireNonNull(language, "language"));
        return configManager.saveLanguageCode(language.getCode());
    }

    /**
     * Returns the translated, formatted message for the key. A missing key is
     * rendered as {@code !key!} instead of crashing the application.
     */
    public String get(String key, Object... args) {
        String pattern;
        try {
            pattern = bundle.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
        return new MessageFormat(pattern, currentLanguage.toLocale()).format(args);
    }

    /** Display name of a language in the currently active language. */
    public String getLanguageName(Language language) {
        return get(language.getNameKey());
    }

    /** Loads the resource bundle of a language (also used by tests). */
    static ResourceBundle loadBundle(Language language) {
        return ResourceBundle.getBundle(BUNDLE_BASE_NAME, language.toLocale(), NO_FALLBACK);
    }

    private void applyLanguage(Language language) {
        this.bundle = loadBundle(language);
        this.currentLanguage = language;
    }
}
