package dev.anurag.todohandlerplugin;



import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.editor.Document;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import dev.anurag.todohandlerplugin.highlighter.TodoHighlighter;
import dev.anurag.todohandlerplugin.util.TodoPanel;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TodoToolWindowFactory implements ToolWindowFactory {
    private TodoHighlighter highlighter = new TodoHighlighter();

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        List<TodoScanner.TodoItem> todos = editor != null ? TodoScanner.scan(editor.getDocument()) : new ArrayList<>();

        TodoPanel todoPanel = new TodoPanel(project, editor, todos);

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(todoPanel.getPanel(), "", false);
        toolWindow.getContentManager().addContent(content);

        //  Listen for file change
        project.getMessageBus()
                .connect()
                .subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
                    @Override
                    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
                        Editor newEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();
                        if (newEditor != null) {
                            List<TodoScanner.TodoItem> newTodos = TodoScanner.scan(newEditor.getDocument());
                            todoPanel.updateTodoList(newTodos, newEditor); // You'll create this method
                        }
                    }
                });
    }

}
