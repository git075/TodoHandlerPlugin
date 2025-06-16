package dev.anurag.todohandlerplugin;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import dev.anurag.todohandlerplugin.highlighter.TodoHighlighter;
import dev.anurag.todohandlerplugin.util.TodoPanel;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TodoToolWindowFactory implements ToolWindowFactory {

    private final TodoHighlighter highlighter = new TodoHighlighter();

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        List<TodoScanner.TodoItem> todos = editor != null ? TodoScanner.scan(editor.getDocument()) : new ArrayList<>();

        TodoPanel todoPanel = new TodoPanel(project, editor, todos);

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(todoPanel.getPanel(), "", false);
        toolWindow.getContentManager().addContent(content);

        // 1. Listen for file switching
        project.getMessageBus()
                .connect()
                .subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
                    @Override
                    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
                        Editor newEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();
                        if (newEditor != null) {
                            List<TodoScanner.TodoItem> newTodos = TodoScanner.scan(newEditor.getDocument());
                            todoPanel.updateTodoList(newTodos, newEditor);
                            attachDocumentListener(newEditor, todoPanel);
                        }
                    }
                });

        // 2. Listen for new file creation
        VirtualFileManager.getInstance().addVirtualFileListener(new VirtualFileListener() {
            @Override
            public void fileCreated(@NotNull VirtualFileEvent event) {
                ApplicationManager.getApplication().invokeLater(() -> {
                    Editor newEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();
                    if (newEditor != null) {
                        List<TodoScanner.TodoItem> newTodos = TodoScanner.scan(newEditor.getDocument());
                        todoPanel.updateTodoList(newTodos, newEditor);
                        attachDocumentListener(newEditor, todoPanel);
                    }
                });
            }
        });

        // 3. Attach DocumentListener on the initial editor
        if (editor != null) {
            attachDocumentListener(editor, todoPanel);
        }
    }

    // Attach listener for text changes
    private void attachDocumentListener(Editor editor, TodoPanel todoPanel) {
        editor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                ApplicationManager.getApplication().invokeLater(() -> {
                    List<TodoScanner.TodoItem> updatedTodos = TodoScanner.scan(editor.getDocument());
                    todoPanel.updateTodoList(updatedTodos, editor);
                });
            }
        });
    }
}
