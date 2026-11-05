package de.aufgabenmanagement.service;

/**
 * Outcome of validating task input. Deliberately free of user-facing text:
 * the presentation layer maps each value to a translated message.
 */
public enum TaskValidationResult {
    VALID,
    TITLE_EMPTY,
    TITLE_TOO_LONG,
    DESCRIPTION_TOO_LONG
}
