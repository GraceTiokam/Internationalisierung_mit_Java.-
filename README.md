# Aufgabenmanagement

Mehrsprachige Java-Konsolenanwendung zur Verwaltung von Aufgaben – auf Deutsch, Französisch und Englisch.
Keine Laufzeitabhängigkeiten; nur JUnit 5 für Tests.

## Funktionen

- **Aufgabe hinzufügen** – Titel (Pflicht, max. 100 Zeichen) und optionale Beschreibung (max. 500 Zeichen)
- **Alle Aufgaben anzeigen** – mit ID, Status (erledigt / offen) und Beschreibung
- **Aufgabe löschen** – per ID; nicht vorhandene IDs werden gemeldet
- **Erledigt-Status umschalten** – per ID
- **Sprache ändern** – zur Laufzeit, ohne Neustart; die Wahl wird gespeichert
- **Beenden** – auch bei Ende der Eingabe (Strg+D) wird sauber beendet

Jede Aufgabe hat eine eindeutige, fortlaufende ID (IDs gelöschter Aufgaben werden nicht wiederverwendet).
Die Aufgaben werden nur im Speicher gehalten. Ungültige Menüeingaben, leere Titel, ungültige IDs sowie
fehlende oder beschädigte Konfigurationsdateien führen nie zum Absturz, sondern zu übersetzten Meldungen.

## Architektur

```
src/main/java/de/aufgabenmanagement/
├── Main.java                    Einstiegspunkt, verbindet die Komponenten
├── model/Task.java              Datenmodell einer Aufgabe
├── service/
│   ├── TaskManager.java         Geschäftslogik (Anlegen, Auflisten, Löschen, Status)
│   ├── TaskValidationResult.java  Validierungsergebnis (ohne Texte)
│   └── InvalidTaskException.java
├── i18n/
│   ├── Language.java            Unterstützte Sprachen (de, fr, en)
│   ├── LanguageResolver.java    Startsprache nach Priorität bestimmen (reine Logik)
│   ├── LanguageSelection.java   Ergebnis inkl. Quelle und ignorierter Werte
│   ├── LanguageSource.java      Kommandozeile / Konfiguration / JVM / Fallback
│   └── LanguageManager.java     ResourceBundle verwalten, Sprache wechseln & speichern
├── config/ConfigManager.java    config.properties lesen und schreiben
└── ui/
    ├── ConsoleUI.java           Menüs, Eingaben, Ausgaben
    └── MenuOption.java          Einträge des Hauptmenüs
src/main/resources/
└── messages_de.properties, messages_fr.properties, messages_en.properties
```

- Die **Geschäftslogik** (`TaskManager`) kennt weder Konsole noch Übersetzungen; Fehler werden als
  Enum (`TaskValidationResult`) gemeldet und erst in der `ConsoleUI` in Text übersetzt.
- **Alle sichtbaren Texte** stammen aus den UTF-8-kodierten `messages_*.properties`.
  Platzhalter folgen dem `MessageFormat`-Stil (`{0}`). Apostrophe werden als `’` geschrieben,
  da `MessageFormat` das ASCII-Zeichen `'` als Escape-Zeichen interpretiert.

## Kompilieren und Starten

Voraussetzungen: JDK 17 oder neuer, Maven 3.6+.

```bash
mvn clean package
```

Dabei werden alle Tests ausgeführt und die ausführbare Datei `target/application.jar` erzeugt
(inklusive aller Übersetzungsdateien). Start:

```bash
java -jar target/application.jar
```

Nur Tests ausführen: `mvn test`.

Hinweis für Windows: Für korrekte Umlaute/Akzente die Konsole ggf. mit `chcp 65001` auf UTF-8 umstellen.

## Startsprache festlegen

Die Sprache wird beim Start in folgender Reihenfolge bestimmt:

1. **Kommandozeilenargument** (`de`, `fr` oder `en`, Groß-/Kleinschreibung egal):
   ```bash
   java -jar target/application.jar fr
   ```
   Ein gültiges Argument hat Vorrang vor Konfigurationsdatei und JVM-Sprache.
   Ein ungültiges Argument (z. B. `xx`) wird mit einer Warnung ignoriert.
2. **Konfigurationsdatei** `config.properties` mit dem Eintrag `language=fr`.
3. **JVM-Standardsprache** (`Locale.getDefault()`), falls unterstützt – z. B.
   `java -Duser.language=de -jar target/application.jar`.
4. Andernfalls **Englisch** als Fallback.

Beim Start zeigt die Anwendung an, welche Sprache aktiv ist und aus welcher Quelle sie stammt.

## Speicherung der Sprache

- `config.properties` liegt **außerhalb des JARs im aktuellen Arbeitsverzeichnis** und kann von Hand bearbeitet werden.
- Fehlt die Datei oder enthält sie keinen gültigen Sprachwert, wird sie beim Start automatisch
  mit der ermittelten Sprache angelegt bzw. repariert.
- Ein bestehender gültiger Eintrag wird durch ein Kommandozeilenargument **nicht** überschrieben –
  das Argument gilt nur für den jeweiligen Start.
- Bei jedem Sprachwechsel über das Menü wird die neue Sprache sofort gespeichert und beim nächsten
  Start (ohne Argument) wiederhergestellt.
- Beschädigte oder nicht lesbare Dateien werden ignoriert; kann nicht geschrieben werden,
  erscheint ein Hinweis, die Anwendung läuft aber weiter.
