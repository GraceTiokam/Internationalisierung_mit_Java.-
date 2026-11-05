package de.aufgabenmanagement.i18n;

/**
 * Where the startup language came from, in descending priority.
 */
public enum LanguageSource {
    COMMAND_LINE,
    CONFIG_FILE,
    JVM_DEFAULT,
    FALLBACK;

    /** Resource key describing this source to the user. */
    public String getMessageKey() {
        return "language.source." + name().toLowerCase(java.util.Locale.ROOT);
    }
}
