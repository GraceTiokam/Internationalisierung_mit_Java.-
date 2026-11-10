package aufgabenmanagement;

import static aufgabenmanagement.LanguageManager.t;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Scanner;

/**
 * Einstiegspunkt der Anwendung: erfasst die Daten eines Beraters und zeigt eine Aufgabenübersicht.
 * Alle Texte kommen über LanguageManager.t(...) aus messages_xx.properties.
 */
public class AufgabenManagement {

    private static final Scanner SCANNER = new Scanner(System.in);

    // Enum pour le genre
    public enum Gender {
        MALE, FEMALE
    }

    public static void main(String[] args) {
        LanguageManager.init(args);

        System.out.println(t("app.title"));
        System.out.println(t("proverb.intro", new Date()) + " " + t("proverb." + (1 + (int) (Math.random() * 4))));

        // Utilisation de NumberFormat pour le montant de la licence
        double licenseFee = Math.random() * 10000;
        System.out.println(t("license.warning", licenseFee));

        // Sprache vor der Eingabe wählen (Enter = aktuelle Sprache behalten)
        askForLanguageChange("language.prompt.start");

        // Eingabe
        String name = readText("input.name");
        Gender gender = readGender();
        String area = readText("input.area");
        int taskCount = readTaskCount();
        double paymentPerTask = readNonNegativeDouble("input.payment");
        double desiredDiscount = readNonNegativeDouble("input.discount");

        // Ausgabe
        runOverviewLoop(name, gender, area, taskCount, paymentPerTask, desiredDiscount);

        System.out.println(t("app.goodbye"));
        SCANNER.close();
    }

    /**
     * Exécute la boucle d'affichage de l'aperçu et de changement de langue.
     */
    private static void runOverviewLoop(String name, Gender gender, String area,
                                        int taskCount, double paymentPerTask, double desiredDiscount) {
        do {
            printOverview(name, gender, area, taskCount, paymentPerTask, desiredDiscount);
        } while (askForLanguageChange("language.prompt.end"));
    }


    private static void printOverview(String name, Gender gender, String area,
                                      int taskCount, double paymentPerTask, double desiredDiscount) {
        Date now = new Date();
        String title = (gender == Gender.FEMALE) ? t("title.female") : t("title.male");

        System.out.println();
        System.out.println(t("overview.header"));
        System.out.println(t("overview.date", now));
        System.out.println(t("overview.time", now));
        System.out.println(t("overview.area", title, name, area));
        System.out.println(t("overview.tasks", title + " " + name, taskCount));
        System.out.println(t("overview.payment", paymentPerTask)); // Pass the double directly, MessageFormat handles currency
        System.out.println(t("overview.discount", desiredDiscount));
        System.out.println();
    }

    /**
     * Fragt nach einem Sprachcode (de/fr/en).
     * Liefert true, wenn die Sprache gewechselt wurde, false bei leerer Eingabe (Enter).
     */
    private static boolean askForLanguageChange(String promptKey) {
        while (true) {
            System.out.print(t(promptKey) + " ");
            String input = readLine();
            if (input.isEmpty()) {
                return false;
            }
            if (LanguageManager.switchLanguage(input)) {
                System.out.println(t("language.changed"));
                return true;
            }
            System.out.println(t("error.language.invalid", input));
        }
    }

    // ---------- Eingabe-Hilfsmethoden ----------

    private static String readText(String promptKey) {
        while (true) {
            System.out.print(t(promptKey) + " ");
            String input = readLine();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println(t("error.input.empty"));
        }
    }

    /** Liest le genre. true = weiblich, false = männlich. Die Kürzel (m/w, h/f, m/f) stehen in den Sprachdateien. */
    private static Gender readGender() {
        while (true) {
            System.out.print(t("input.gender") + " ");
            String input = readLine();
            if (input.equalsIgnoreCase(t("gender.female.letter"))) {
                return Gender.FEMALE;
            }
            if (input.equalsIgnoreCase(t("gender.male.letter"))) {
                return Gender.MALE;
            }
            System.out.println(t("error.gender.invalid"));
        }
    }

    private static int readTaskCount() {
        while (true) {
            System.out.print(t("input.count") + " ");
            try {
                int value = Integer.parseInt(readLine());
                if (value >= 0) {
                    return value;
                }
            } catch (NumberFormatException e) {
                // Fehlermeldung siehe unten
            }
            System.out.println(t("error.number.invalid"));
        }
    }

    private static double readNonNegativeDouble(String promptKey) {
        while (true) {
            System.out.print(t(promptKey) + " ");
            try {
                // Komma und Punkt als Dezimaltrennzeichen erlauben (12,5 oder 12.5)
                // MessageFormat.format wird die Ausgabe entsprechend der Locale formatieren
                double value = Double.parseDouble(readLine().replace(',', '.'));
                if (value >= 0) {
                    return value;
                }
            } catch (NumberFormatException e) {
                // Fehlermeldung siehe unten
            }
            System.out.println(t("error.number.invalid"));
        }
    }

    /** Liest eine Zeile; endet die Eingabe (z. B. Strg+D), wird das Programm sauber beendet. */
    private static String readLine() {
        if (!SCANNER.hasNextLine()) {
            System.out.println();
            System.out.println(t("app.goodbye"));
            System.exit(0);
        }
        return SCANNER.nextLine().trim();
    }
}

/**
 * Kapselt alles rund um I18N (Wrapper wie in der Übung):
 * - Startsprache ermitteln (Argument > config.properties > JVM-Sprache > Deutsch)
 * - ResourceBundle laden und Sprache zur Laufzeit wechseln
 * - Texte übersetzen mit t(...)
 * - zuletzt gewählte Sprache in config.properties speichern
 */
final class LanguageManager {

    private static final String BUNDLE_NAME = "messages";
    private static final String CONFIG_FILE = "config.properties";
    private static final String CONFIG_KEY = "language";
    private static final String FALLBACK_LANGUAGE = "de";

    // Ohne Fallback: ResourceBundle soll NICHT stillschweigend auf die JVM-Sprache ausweichen
    private static final ResourceBundle.Control NO_FALLBACK =
            ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES);

    private static String currentLanguage;
    private static Locale currentLocale;
    private static ResourceBundle bundle;


    /** Ermittelt die Startsprache in der vorgegebenen Reihenfolge und lädt das Bundle. */
    static void init(String[] args) {
        String fromArgument = args.length > 0 ? args[0] : null;
        String fromConfig = loadLanguageFromConfig();
        String fromJvm = Locale.getDefault().getLanguage();

        String sourceKey;
        if (isSupported(fromArgument)) {
            currentLanguage = normalize(fromArgument);
            sourceKey = "language.source.argument";
        } else if (isSupported(fromConfig)) {
            currentLanguage = normalize(fromConfig);
            sourceKey = "language.source.config";
        } else if (isSupported(fromJvm)) {
            currentLanguage = normalize(fromJvm);
            sourceKey = "language.source.jvm";
        } else {
            currentLanguage = FALLBACK_LANGUAGE;
            sourceKey = "language.source.fallback";
        }
        loadBundle(currentLanguage);

        // Warnungen erst jetzt ausgeben, damit sie schon übersetzt sind
        if (fromArgument != null && !isSupported(fromArgument)) {
            System.out.println(t("error.argument.invalid", fromArgument));
        }
        if (fromConfig != null && !isSupported(fromConfig)) {
            System.out.println(t("error.config.invalid", fromConfig, CONFIG_FILE));
        }
        System.out.println(t("language.current") + " " + t(sourceKey));
    }

    /** Wechselt die Sprache zur Laufzeit und speichert sie. Liefert false bei ungültigem Code. */
    static boolean switchLanguage(String languageCode) {
        if (!isSupported(languageCode)) {
            return false;
        }
        currentLanguage = normalize(languageCode);
        loadBundle(currentLanguage);
        saveLanguageToConfig(currentLanguage);
        return true;
    }

    /** Nur "de", "fr" und "en" sind erlaubt (Groß-/Kleinschreibung egal). */
    static boolean isSupported(String languageCode) {
        if (languageCode == null) {
            return false;
        }
        String code = normalize(languageCode);
        return code.equals("de") || code.equals("fr") || code.equals("en");
    }

    /** Text ohne Platzhalter. */
    static String t(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return "#" + key + " missing#";
        }
    }

    /** Text mit Platzhaltern {0}, {1}, ... (Zahlen und Datum werden passend zur Sprache formatiert). */
    static String t(String key, Object... args) {
        // MessageFormat est sensible à la locale courante pour le formatage des nombres et dates.
        // Pour les devises, on utilise le style 'currency' dans les fichiers .properties.
        return new MessageFormat(t(key), currentLocale).format(args);
    }

    // ---------- private Hilfsmethoden ----------

    private static String normalize(String languageCode) {
        return languageCode.trim().toLowerCase(Locale.ROOT);
    }

    private static void loadBundle(String languageCode) {
        currentLocale = toLocale(languageCode);
        bundle = ResourceBundle.getBundle(BUNDLE_NAME, currentLocale, NO_FALLBACK);
    }

    /** Land dazu, damit Datum und Zahlen korrekt formatiert werden. */
    private static Locale toLocale(String languageCode) {
        return switch (languageCode) {
            case "fr" -> Locale.FRANCE; // fr-FR
            case "en" -> Locale.UK;     // en-GB
            default -> Locale.GERMANY;  // de-DE
        };
    }

    /** Liest die gespeicherte Sprache; null, wenn keine Datei oder kein Eintrag vorhanden ist. */
    private static String loadLanguageFromConfig() {
        Properties config = new Properties();
        try (InputStream in = new FileInputStream(CONFIG_FILE)) {
            config.load(in);
            return config.getProperty(CONFIG_KEY);
        } catch (IOException | IllegalArgumentException e) {
            // Datei fehlt oder ist kaputt -> einfach ignorieren
            return null;
        }
    }

    private static void saveLanguageToConfig(String languageCode) {
        Properties config = new Properties();
        config.setProperty(CONFIG_KEY, languageCode);
        try (OutputStream out = new FileOutputStream(CONFIG_FILE)) {
            config.store(out, "Zuletzt gewaehlte Sprache");
        } catch (IOException e) {
            System.out.println(t("error.config.save", e.getMessage()));
        }
    }
}
