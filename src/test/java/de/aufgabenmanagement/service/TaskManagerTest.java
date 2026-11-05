package de.aufgabenmanagement.service;

import de.aufgabenmanagement.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskManagerTest {

    private TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = new TaskManager();
    }

    @Test
    void addTaskAssignsUniqueIdsAndStoresTasksInOrder() {
        Task first = manager.addTask("Einkaufen", "Milch und Brot");
        Task second = manager.addTask("Lernen", null);

        assertNotEquals(first.getId(), second.getId());
        assertEquals(List.of(first, second), manager.getAllTasks());
        assertFalse(first.isCompleted());
    }

    @Test
    void addTaskTrimsInputAndTreatsBlankDescriptionAsAbsent() {
        Task task = manager.addTask("  Rapport écrire  ", "   ");

        assertEquals("Rapport écrire", task.getTitle());
        assertTrue(task.getDescription().isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    void addTaskRejectsEmptyTitle(String title) {
        InvalidTaskException e = assertThrows(InvalidTaskException.class, () -> manager.addTask(title, "x"));
        assertEquals(TaskValidationResult.TITLE_EMPTY, e.getReason());
        assertEquals(0, manager.getTaskCount());
    }

    @Test
    void addTaskRejectsTooLongTitleAndDescription() {
        String longTitle = "a".repeat(TaskManager.MAX_TITLE_LENGTH + 1);
        String longDescription = "b".repeat(TaskManager.MAX_DESCRIPTION_LENGTH + 1);

        assertEquals(TaskValidationResult.TITLE_TOO_LONG,
                assertThrows(InvalidTaskException.class, () -> manager.addTask(longTitle, null)).getReason());
        assertEquals(TaskValidationResult.DESCRIPTION_TOO_LONG,
                assertThrows(InvalidTaskException.class, () -> manager.addTask("ok", longDescription)).getReason());
        assertEquals(TaskValidationResult.VALID,
                manager.validate("a".repeat(TaskManager.MAX_TITLE_LENGTH), null));
    }

    @Test
    void deleteExistingTaskRemovesIt() {
        Task task = manager.addTask("Aufgabe", null);

        assertTrue(manager.deleteTask(task.getId()));
        assertTrue(manager.getAllTasks().isEmpty());
    }

    @Test
    void deleteNonExistingTaskReturnsFalse() {
        manager.addTask("Aufgabe", null);

        assertFalse(manager.deleteTask(42));
        assertFalse(manager.deleteTask(-1));
        assertEquals(1, manager.getTaskCount());
    }

    @Test
    void idsAreNotReusedAfterDeletion() {
        Task first = manager.addTask("A", null);
        manager.deleteTask(first.getId());

        Task second = manager.addTask("B", null);
        assertNotEquals(first.getId(), second.getId());
    }

    @Test
    void toggleCompletedFlipsStatus() {
        Task task = manager.addTask("A", null);

        assertTrue(manager.toggleCompleted(task.getId()).orElseThrow().isCompleted());
        assertFalse(manager.toggleCompleted(task.getId()).orElseThrow().isCompleted());
        assertTrue(manager.toggleCompleted(999).isEmpty());
    }

    @Test
    void returnedListIsUnmodifiable() {
        manager.addTask("A", null);
        List<Task> tasks = manager.getAllTasks();

        assertThrows(UnsupportedOperationException.class, () -> tasks.remove(0));
        assertEquals(1, manager.getTaskCount());
    }
}
