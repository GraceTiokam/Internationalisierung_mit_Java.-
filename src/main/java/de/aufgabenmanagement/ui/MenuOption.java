package de.aufgabenmanagement.ui;

import java.util.Arrays;
import java.util.Optional;

/**
 * Entries of the main menu with their selection number and resource key.
 */
enum MenuOption {
    ADD_TASK(1, "menu.add"),
    LIST_TASKS(2, "menu.list"),
    DELETE_TASK(3, "menu.delete"),
    TOGGLE_TASK(4, "menu.toggle"),
    CHANGE_LANGUAGE(5, "menu.language"),
    EXIT(0, "menu.exit");

    private final int number;
    private final String messageKey;

    MenuOption(int number, String messageKey) {
        this.number = number;
        this.messageKey = messageKey;
    }

    int getNumber() {
        return number;
    }

    String getMessageKey() {
        return messageKey;
    }

    static Optional<MenuOption> fromNumber(int number) {
        return Arrays.stream(values()).filter(option -> option.number == number).findFirst();
    }

    static int minNumber() {
        return Arrays.stream(values()).mapToInt(MenuOption::getNumber).min().orElse(0);
    }

    static int maxNumber() {
        return Arrays.stream(values()).mapToInt(MenuOption::getNumber).max().orElse(0);
    }
}
