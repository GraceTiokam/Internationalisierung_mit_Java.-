package de.aufgabenmanagement.i18n;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Languages supported by the application.
 */
public enum Language {
    GERMAN("de"),
    FRENCH("fr"),
    ENGLISH("en");

    /** Used when no other source yields a supported language. */
    public static final Language FALLBACK = ENGLISH;

    private final String code;

    Language(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public Locale toLocale() {
        return Locale.forLanguageTag(code);
    }

    /** Resource key of this language's display name, e.g. {@code language.name.de}. */
    public String getNameKey() {
        return "language.name." + code;
    }

    /**
     * Parses a language code case-insensitively, ignoring surrounding whitespace.
     *
     * @return the language, or empty for {@code null}, blank or unsupported codes
     */
    public static Optional<Language> fromCode(String code) {
        if (code == null) {
            return Optional.empty();
        }
        String normalized = code.strip().toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(language -> language.code.equals(normalized))
                .findFirst();
    }

    /** Maps a locale (e.g. {@code de_AT}) to a supported language by its language part. */
    public static Optional<Language> fromLocale(Locale locale) {
        return locale == null ? Optional.empty() : fromCode(locale.getLanguage());
    }

    /** Comma-separated list of supported codes, e.g. {@code de, fr, en}. */
    public static String supportedCodes() {
        return Arrays.stream(values()).map(Language::getCode).collect(Collectors.joining(", "));
    }
}
