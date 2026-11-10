# AufgabenManagement – mehrsprachige Konsolenanwendung (I18N)

Die ursprüngliche Anwendung `AufgabenManagement.java` wurde mit `ResourceBundle`
internationalisiert. Unterstützte Sprachen: **Deutsch (de)**, **Französisch (fr)**, **Englisch (en)**.

## Projektstruktur

```
AufgabenManagement/
├── pom.xml                                   Maven-Konfiguration (ausführbare JAR)
├── README.md
├── aufgabenmanagement.jar                    fertig gebaute JAR (Kopie aus target/)
└── src/main/
    ├── java/aufgabenmanagement/
    │   ├── AufgabenManagement.java           Einstiegspunkt + Programmlogik (Eingabe, Übersicht)
    │   └── LanguageManager.java              Sprache ermitteln/wechseln, ResourceBundle, t(...)
    └── resources/
        ├── messages_de.properties            deutsche Texte
        ├── messages_fr.properties            französische Texte
        └── messages_en.properties            englische Texte
```

`config.properties` wird beim ersten Sprachwechsel **im aktuellen Arbeitsverzeichnis** angelegt:

```
language=fr
```

## Bauen

Voraussetzung: Java 17+ und Maven.

```bash
mvn clean package
```

Ergebnis: `target/aufgabenmanagement.jar` (die Sprachdateien sind in der JAR enthalten).

## Starten

```bash
java -jar target/aufgabenmanagement.jar        # Sprache automatisch ermitteln
java -jar target/aufgabenmanagement.jar fr     # Französisch erzwingen
java -jar target/aufgabenmanagement.jar en     # Englisch erzwingen
```

## Reihenfolge der Sprachwahl beim Start

1. Kommandozeilenargument (`de`, `fr`, `en`)
2. zuletzt gewählte Sprache aus `config.properties`
3. Sprache der JVM (`Locale.getDefault()`)
4. sonst Deutsch

Ungültige Werte (z. B. `java -jar ... xy` oder `language=zz` in der Datei) werden mit
einer übersetzten Meldung ignoriert; eine fehlende oder kaputte `config.properties` wird
einfach übersprungen. Die erste Ausgabezeile zeigt, woher die Sprache kommt.

## Sprache zur Laufzeit wechseln

- **Vor der Eingabe**: Sprachcode eingeben (`de`/`fr`/`en`) oder Enter zum Beibehalten.
- **Nach der Übersicht**: Sprachcode eingeben – die Übersicht wird sofort in der neuen
  Sprache erneut angezeigt. Enter beendet das Programm.

Jeder Wechsel wird in `config.properties` gespeichert und beim nächsten Start verwendet
(sofern kein Kommandozeilenargument angegeben ist).

## Wie funktioniert die I18N?

- `LanguageManager.t("key")` liefert den Text aus dem aktuellen `ResourceBundle`.
- `LanguageManager.t("key", args...)` formatiert Platzhalter mit `MessageFormat`
  sprachabhängig: Datum (`{0,date,full}`), Zahlen (`{0,number,#,##0.00}`) und
  Einzahl/Mehrzahl (`{1,choice,0#...|1#...|1<...}`).
- Für das Laden wird `ResourceBundle.Control.getNoFallbackControl(...)` verwendet, damit
  Java nicht heimlich auf die Systemsprache ausweicht.
- In den `.properties`-Dateien wird `’` statt `'` verwendet, weil ein einfaches
  Hochkomma in `MessageFormat` eine Sonderbedeutung hat.

## Hinweis zu Umlauten in der Konsole

Die Sprachdateien sind UTF-8. Werden Umlaute als `?` angezeigt, ist die Konsole
nicht auf UTF-8 eingestellt. Abhilfe (Windows: vorher `chcp 65001`):

```bash
java -Dstdout.encoding=UTF-8 -jar target/aufgabenmanagement.jar
```
