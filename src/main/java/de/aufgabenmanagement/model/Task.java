package de.aufgabenmanagement.model;

import java.util.Objects;
import java.util.Optional;

/**
 * A single task. ID, title and description are fixed after creation;
 * only the completion status can change.
 */
public final class Task {

    private final int id;
    private final String title;
    private final String description;
    private boolean completed;

    /**
     * @param id          unique, positive identifier
     * @param title       non-blank title
     * @param description optional description; {@code null} or blank means "none"
     */
    public Task(int id, String title, String description) {
        if (id <= 0) {
            throw new IllegalArgumentException("Task id must be positive: " + id);
        }
        Objects.requireNonNull(title, "title");
        if (title.isBlank()) {
            throw new IllegalArgumentException("Task title must not be blank");
        }
        this.id = id;
        this.title = title.strip();
        this.description = (description == null || description.isBlank()) ? null : description.strip();
        this.completed = false;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof Task task && task.id == id);
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return "Task{id=" + id + ", title='" + title + "', completed=" + completed + '}';
    }
}
