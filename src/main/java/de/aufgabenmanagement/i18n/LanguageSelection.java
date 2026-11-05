package de.aufgabenmanagement.i18n;

import java.util.Objects;
import java.util.Optional;

/**
 * Result of the startup language resolution.
 *
 * @param language            the language to use
 * @param source              where it came from
 * @param rejectedArgument    a command-line value that was present but unsupported
 * @param rejectedConfigValue a configuration value that was present but unsupported
 */
public record LanguageSelection(Language language,
                                LanguageSource source,
                                Optional<String> rejectedArgument,
                                Optional<String> rejectedConfigValue) {

    public LanguageSelection {
        Objects.requireNonNull(language, "language");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(rejectedArgument, "rejectedArgument");
        Objects.requireNonNull(rejectedConfigValue, "rejectedConfigValue");
    }
}
