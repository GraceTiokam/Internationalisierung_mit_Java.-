package de.aufgabenmanagement.i18n;

import java.util.Locale;
import java.util.Optional;

/**
 * Determines the startup language. Pure logic without I/O, so the priority
 * rules are easy to unit test:
 * <ol>
 *     <li>valid command-line argument</li>
 *     <li>valid {@code language} entry in the configuration file</li>
 *     <li>JVM default locale, if supported</li>
 *     <li>English as fallback</li>
 * </ol>
 */
public final class LanguageResolver {

    private LanguageResolver() {
    }

    /**
     * @param args            program arguments; only the first one is considered
     * @param configuredValue raw value from the configuration file, if any
     * @param jvmLocale       the JVM default locale
     */
    public static LanguageSelection resolve(String[] args, Optional<String> configuredValue, Locale jvmLocale) {
        Optional<String> argument = (args == null || args.length == 0)
                ? Optional.empty()
                : Optional.ofNullable(args[0]);
        Optional<String> config = configuredValue == null ? Optional.empty() : configuredValue;

        Optional<Language> fromArgument = argument.flatMap(Language::fromCode);
        Optional<Language> fromConfig = config.flatMap(Language::fromCode);

        Optional<String> rejectedArgument = argument.filter(value -> fromArgument.isEmpty());
        Optional<String> rejectedConfig = config.filter(value -> fromConfig.isEmpty());

        if (fromArgument.isPresent()) {
            return new LanguageSelection(fromArgument.get(), LanguageSource.COMMAND_LINE,
                    rejectedArgument, rejectedConfig);
        }
        if (fromConfig.isPresent()) {
            return new LanguageSelection(fromConfig.get(), LanguageSource.CONFIG_FILE,
                    rejectedArgument, rejectedConfig);
        }
        return Language.fromLocale(jvmLocale)
                .map(language -> new LanguageSelection(language, LanguageSource.JVM_DEFAULT,
                        rejectedArgument, rejectedConfig))
                .orElseGet(() -> new LanguageSelection(Language.FALLBACK, LanguageSource.FALLBACK,
                        rejectedArgument, rejectedConfig));
    }
}
