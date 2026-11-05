package de.aufgabenmanagement.service;

import de.aufgabenmanagement.model.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Business logic for managing tasks (in memory).
 * Independent of console presentation and translations.
 */
public class TaskManager {

    public static final int MAX_TITLE_LENGTH = 100;
    public static final int MAX_DESCRIPTION_LENGTH = 500;

    /** Insertion-ordered so tasks are listed in creation order. */
    private final Map<Integer, Task> tasks = new LinkedHashMap<>();
    private int nextId = 1;

    /**
     * Validates task input without creating a task.
     */
    public TaskValidationResult validate(String title, String description) {
        if (title == null || title.isBlank()) {
            return TaskValidationResult.TITLE_EMPTY;
        }
        if (title.strip().length() > MAX_TITLE_LENGTH) {
            return TaskValidationResult.TITLE_TOO_LONG;
        }
        if (description != null && description.strip().length() > MAX_DESCRIPTION_LENGTH) {
            return TaskValidationResult.DESCRIPTION_TOO_LONG;
        }
        return TaskValidationResult.VALID;
    }

    /**
     * Creates and stores a new task with a unique ID.
     *
     * @throws InvalidTaskException if the input is invalid
     */
    public Task addTask(String title, String description) {
        TaskValidationResult result = validate(title, description);
        if (result != TaskValidationResult.VALID) {
            throw new InvalidTaskException(result);
        }
        Task task = new Task(nextId++, title, description);
        tasks.put(task.getId(), task);
        return task;
    }

    /** Returns an unmodifiable snapshot of all tasks in creation order. */
    public List<Task> getAllTasks() {
        return Collections.unmodifiableList(new ArrayList<>(tasks.values()));
    }

    public Optional<Task> findTask(int id) {
        return Optional.ofNullable(tasks.get(id));
    }

    /**
     * @return {@code true} if a task with this ID existed and was removed
     */
    public boolean deleteTask(int id) {
        return tasks.remove(id) != null;
    }

    /**
     * Flips the completion status of a task.
     *
     * @return the updated task, or empty if no task has this ID
     */
    public Optional<Task> toggleCompleted(int id) {
        Optional<Task> task = findTask(id);
        task.ifPresent(t -> t.setCompleted(!t.isCompleted()));
        return task;
    }

    public int getTaskCount() {
        return tasks.size();
    }
}
