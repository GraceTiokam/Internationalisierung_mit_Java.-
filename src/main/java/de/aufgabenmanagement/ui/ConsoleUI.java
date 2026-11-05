package de.aufgabenmanagement.ui;

import de.aufgabenmanagement.i18n.Language;
import de.aufgabenmanagement.i18n.LanguageManager;
import de.aufgabenmanagement.i18n.LanguageSelection;
import de.aufgabenmanagement.model.Task;
import de.aufgabenmanagement.service.InvalidTaskException;
import de.aufgabenmanagement.service.TaskManager;
import de.aufgabenmanagement.service.TaskValidationResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Console interaction: menus, prompts and output. All user-facing text is
 * obtained from the {@link LanguageManager}, so a language switch takes
 * effect on the very next line printed.
 */
public class ConsoleUI {

    private static final String SEPARATOR = "========================================";
    private static final String TASK_INDENT = "  ";
    private static final String DESCRIPTION_INDENT = "        ";
    private static final int LANGUAGE_MENU_BACK = 0;

    private final TaskManager taskManager;
    private final LanguageManager i18n;
    private final BufferedReader in;
    private final PrintStream out;

    public ConsoleUI(TaskManager taskManager, LanguageManager i18n, BufferedReader in, PrintStream out) {
        this.taskManager = Objects.requireNonNull(taskManager, "taskManager");
        this.i18n = Objects.requireNonNull(i18n, "i18n");
        this.in = Objects.requireNonNull(in, "in");
        this.out = Objects.requireNonNull(out, "out");
    }

    /** Prints warnings about ignored language values and the active language. */
    public void showStartupInfo(LanguageSelection selection) {
        selection.rejectedArgument().ifPresent(value ->
                printMessage("warning.invalidArgument", value, Language.supportedCodes()));
        selection.rejectedConfigValue().ifPresent(value ->
                printMessage("warning.invalidConfig", value, Language.supportedCodes()));
        printMessage("app.welcome");
        printMessage("app.language.active",
                i18n.getLanguageName(i18n.getCurrentLanguage()),
                i18n.get(selection.source().getMessageKey()));
    }

    /** Runs the main loop until the user exits or the input ends. */
    public void run() {
        try {
            boolean running = true;
            while (running) {
                printMainMenu();
                running = readMenuOption().map(this::handle).orElse(true);
            }
        } catch (EndOfInputException e) {
            out.println();
        }
        printMessage("app.exit");
    }

    /** @return {@code false} if the application should terminate */
    private boolean handle(MenuOption option) {
        switch (option) {
            case ADD_TASK -> addTask();
            case LIST_TASKS -> listTasks();
            case DELETE_TASK -> deleteTask();
            case TOGGLE_TASK -> toggleTask();
            case CHANGE_LANGUAGE -> changeLanguage();
            case EXIT -> {
                return false;
            }
        }
        return true;
    }

    // ---------------------------------------------------------------- menus

    private void printMainMenu() {
        out.println();
        out.println(SEPARATOR);
        out.println(i18n.get("app.title") + " – " + i18n.get("menu.title"));
        out.println(SEPARATOR);
        for (MenuOption option : MenuOption.values()) {
            printOption(option.getNumber(), i18n.get(option.getMessageKey()));
        }
    }

    private Optional<MenuOption> readMenuOption() {
        String input = prompt("menu.prompt");
        Optional<MenuOption> option = parseInt(input).flatMap(MenuOption::fromNumber);
        if (option.isEmpty()) {
            printMessage("error.invalidOption", input, MenuOption.minNumber(), MenuOption.maxNumber());
        }
        return option;
    }

    private void printOption(int number, String text) {
        out.println("  " + number + ") " + text);
    }

    // -------------------------------------------------------------- actions

    private void addTask() {
        printMessage("task.add.header");
        String title = prompt("task.add.titlePrompt");
        TaskValidationResult titleCheck = taskManager.validate(title, null);
        if (titleCheck != TaskValidationResult.VALID) {
            printValidationError(titleCheck);
            return;
        }
        String description = prompt("task.add.descriptionPrompt");
        try {
            Task task = taskManager.addTask(title, description);
            printMessage("task.add.success", task.getId(), task.getTitle());
        } catch (InvalidTaskException e) {
            printValidationError(e.getReason());
        }
    }

    private void listTasks() {
        List<Task> tasks = taskManager.getAllTasks();
        if (tasks.isEmpty()) {
            printMessage("task.list.empty");
            return;
        }
        printMessage("task.list.header", tasks.size());
        for (Task task : tasks) {
            String status = i18n.get(task.isCompleted() ? "task.status.completed" : "task.status.open");
            out.println(TASK_INDENT + i18n.get("task.list.entry", task.getId(), status, task.getTitle()));
            task.getDescription().ifPresent(description ->
                    out.println(DESCRIPTION_INDENT + i18n.get("task.list.description", description)));
        }
    }

    private void deleteTask() {
        readTaskId("task.delete.prompt").ifPresent(id -> {
            if (taskManager.deleteTask(id)) {
                printMessage("task.delete.success", id);
            } else {
                printMessage("error.taskNotFound", id);
            }
        });
    }

    private void toggleTask() {
        readTaskId("task.toggle.prompt").ifPresent(id -> taskManager.toggleCompleted(id).ifPresentOrElse(
                task -> printMessage(task.isCompleted() ? "task.toggle.completed" : "task.toggle.reopened",
                        task.getId()),
                () -> printMessage("error.taskNotFound", id)));
    }

    private void changeLanguage() {
        Language[] languages = Language.values();
        out.println();
        printMessage("language.menu.title");
        for (int i = 0; i < languages.length; i++) {
            printOption(i + 1, i18n.getLanguageName(languages[i]) + " (" + languages[i].getCode() + ")");
        }
        printOption(LANGUAGE_MENU_BACK, i18n.get("language.menu.back"));

        String input = prompt("language.menu.prompt");
        OptionalInt choice = parseInt(input).stream().mapToInt(Integer::intValue).findFirst();
        if (choice.isPresent() && choice.getAsInt() == LANGUAGE_MENU_BACK) {
            return;
        }
        if (choice.isEmpty() || choice.getAsInt() < 1 || choice.getAsInt() > languages.length) {
            printMessage("error.invalidOption", input, LANGUAGE_MENU_BACK, languages.length);
            return;
        }

        Language selected = languages[choice.getAsInt() - 1];
        if (selected == i18n.getCurrentLanguage()) {
            printMessage("language.unchanged", i18n.getLanguageName(selected));
            return;
        }
        boolean saved = i18n.changeLanguage(selected);
        // From here on every message appears in the new language.
        printMessage("language.changed", i18n.getLanguageName(selected));
        if (!saved) {
            printMessage("error.configSaveFailed");
        }
    }

    // -------------------------------------------------------------- helpers

    private Optional<Integer> readTaskId(String promptKey) {
        String input = prompt(promptKey);
        Optional<Integer> id = parseInt(input).filter(value -> value > 0);
        if (id.isEmpty()) {
            printMessage("error.invalidId", input);
        }
        return id;
    }

    private void printValidationError(TaskValidationResult result) {
        switch (result) {
            case TITLE_EMPTY -> printMessage("validation.title.empty");
            case TITLE_TOO_LONG -> printMessage("validation.title.tooLong", TaskManager.MAX_TITLE_LENGTH);
            case DESCRIPTION_TOO_LONG ->
                    printMessage("validation.description.tooLong", TaskManager.MAX_DESCRIPTION_LENGTH);
            case VALID -> { /* nothing to report */ }
        }
    }

    /** Prints a translated prompt and reads one stripped line. */
    private String prompt(String promptKey) {
        out.print(i18n.get(promptKey) + " ");
        out.flush();
        try {
            String line = in.readLine();
            if (line == null) {
                throw new EndOfInputException();
            }
            return line.strip();
        } catch (IOException e) {
            throw new EndOfInputException();
        }
    }

    private void printMessage(String key, Object... args) {
        out.println(i18n.get(key, args));
    }

    private static Optional<Integer> parseInt(String input) {
        try {
            return Optional.of(Integer.parseInt(input.strip()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /** Signals that standard input was closed (e.g. Ctrl+D / end of piped input). */
    private static final class EndOfInputException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
}
