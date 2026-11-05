package de.aufgabenmanagement;

import de.aufgabenmanagement.config.ConfigManager;
import de.aufgabenmanagement.i18n.Language;
import de.aufgabenmanagement.i18n.LanguageManager;
import de.aufgabenmanagement.i18n.LanguageResolver;
import de.aufgabenmanagement.i18n.LanguageSelection;
import de.aufgabenmanagement.service.TaskManager;
import de.aufgabenmanagement.ui.ConsoleUI;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;

/**
 * Application entry point: wires the components together and starts the console UI.
 * <p>
 * Usage: {@code java -jar application.jar [de|fr|en]}
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        PrintStream out = new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8);
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

        ConfigManager configManager = ConfigManager.inWorkingDirectory();
        Optional<String> configuredLanguage = configManager.readLanguageCode();
        LanguageSelection selection = LanguageResolver.resolve(args, configuredLanguage, Locale.getDefault());

        // Create the config file if missing, or repair it if its value is unusable.
        // An existing valid entry is never overwritten by a command-line argument.
        boolean configHasValidLanguage = configuredLanguage.flatMap(Language::fromCode).isPresent();
        if (!configHasValidLanguage) {
            configManager.saveLanguageCode(selection.language().getCode());
        }

        LanguageManager languageManager = new LanguageManager(configManager, selection.language());
        ConsoleUI ui = new ConsoleUI(new TaskManager(), languageManager, in, out);
        ui.showStartupInfo(selection);
        ui.run();
    }
}
