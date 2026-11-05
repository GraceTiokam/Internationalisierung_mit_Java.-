package de.aufgabenmanagement.service;

/**
 * Thrown when a task cannot be created because its input is invalid.
 */
public class InvalidTaskException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final TaskValidationResult reason;

    public InvalidTaskException(TaskValidationResult reason) {
        super("Invalid task input: " + reason);
        this.reason = reason;
    }

    public TaskValidationResult getReason() {
        return reason;
    }
}
