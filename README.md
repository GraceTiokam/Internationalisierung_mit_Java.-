AufgabenManagement – mehrsprachige Konsolenanwendung (I18N)
Die ursprüngliche Anwendung AufgabenManagement.java wurde mit ResourceBundle internationalisiert. Unterstützte Sprachen: Deutsch (de), Französisch (fr), Englisch (en).

Projektstruktur
AufgabenManagement/
├── pom.xml                                   Maven-Konfiguration (ausführbare JAR)
├── README.md
├── aufgabenmanagement.jar                    fertig gebaute JAR (Kopie aus target/)
└── src/main/
    ├── java/aufgabenmanagement/
    │   └──  AufgabenManagement.java           Einstiegspunkt + Programmlogik (Eingabe, Übersicht)
    │  
    └── resources/
        ├── messages_de.properties            deutsche Texte (Unicode-kodiert)
        ├── messages_fr.properties            französische Texte (Unicode-kodiert)
        └── messages_en.properties            englische Texte (Unicode-kodiert)

config.properties wird beim ersten Sprachwechsel im aktuellen Arbeitsverzeichnis angelegt:

language=fr

Bauen
Voraussetzung: Java 17+ und Maven.

mvn clean package

Ergebnis: target/aufgabenmanagement.jar (die Sprachdateien sind in der JAR enthalten).

Starten
java -jar target/aufgabenmanagement.jar        # Sprache automatisch ermitteln
java -jar target/aufgabenmanagement.jar fr     # Französisch erzwingen
java -jar target/aufgabenmanagement.jar en     # Englisch erzwingen

Reihenfolge der Sprachwahl beim Start
Kommandozeilenargument (de, fr, en)
zuletzt gewählte Sprache aus config.properties
Sprache der JVM (Locale.getDefault())
sonst Deutsch
Ungültige Werte (z. B. java -jar ... xy oder language=zz in der Datei) werden mit einer übersetzten Meldung ignoriert; eine fehlende oder kaputte config.properties wird einfach übersprungen. Die erste Ausgabezeile zeigt, woher die Sprache kommt.

Sprache zur Laufzeit wechseln
Vor der Eingabe: Sprachcode eingeben (de/fr/en) oder Enter zum Beibehalten.
Nach der Übersicht: Sprachcode eingeben – die Übersicht wird sofort in der neuen Sprache erneut angezeigt. Enter beendet das Programm.
Jeder Wechsel wird in config.properties gespeichert und beim nächsten Start verwendet (sofern kein Kommandozeilenargument angegeben ist).

Wie funktioniert die I18N?
LanguageManager.t("key") liefert den Text aus dem aktuellen ResourceBundle.
LanguageManager.t("key", args...) formatiert Platzhalter mit MessageFormat sprachabhängig:
Datum: {0,date,full} / {0,date,medium}
Zeit: {0,time,short}
Währung & Zahlen: {0,number,currency} / {0,number,0.##}
Geschlechtsspezifische Anrede & Satzstruktur (overview.area): Nutzt choice basierend auf dem Geschlechtscode (m/w bzw. h/f), z. B. {2,choice,m#Herr {0}...|w#Frau {0}...}.
Einzahl/Mehrzahl (overview.tasks): Nutzt choice, z. B. {1,choice,0#...|1#...|1<...}.
Für das Laden wird ResourceBundle.Control.getNoFallbackControl(...) verwendet, damit Java nicht heimlich auf die Systemsprache ausweicht.
Unicode-Escapes (\uXXXX): Zur sicheren systemübergreifenden Kompatibilität und Vermeidung von Codierungsproblemen werden Sonderzeichen, Umlaute (ä, ö, ü, ß), französische Akzente (é, è, ê, à, ç) und Anführungszeichen («, », „, “) in allen .properties-Dateien als Unicode-Sequenzen gespeichert.
In den Mustern wird das typografische Apostroph (’ bzw. \u2019) anstelle des einfachen Hochkommas (') verwendet, da ein einfaches Hochkomma in MessageFormat eine Sonderfunktion (Escaping) besitzt.
Hinweis zu Umlauten und Sonderzeichen in der Konsole
Die Sprachdateien sind mit Unicode-Escapes (\uXXXX) formatiert und werden von Java korrekt geladen. Wenn Sonderzeichen in der Konsole fehlerhaft angezeigt werden, ist die Konsole nicht auf UTF-8 eingestellt. Abhilfe (Windows: vorher chcp 65001 ausführen):

java -Dstdout.encoding=UTF-8 -jar target/aufgabenmanagement.jar
