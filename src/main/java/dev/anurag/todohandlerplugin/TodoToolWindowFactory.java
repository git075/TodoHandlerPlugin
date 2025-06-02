package dev.anurag.todohandlerplugin;



import com.intellij.openapi.application.ApplicationManager;
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

import java.util.List;

public class TodoToolWindowFactory implements ToolWindowFactory {
    private TodoHighlighter highlighter = new TodoHighlighter();

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor == null) return;

        Document document = editor.getDocument();
        List<TodoScanner.TodoItem> todos = TodoScanner.scan(document);

        highlighter.highlightTodos(editor, todos);

        TodoPanel todoPanel = new TodoPanel(project, editor, todos);
        Content content = ApplicationManager.getApplication().getService(ContentFactory.class).createContent(todoPanel.getPanel(), "", false);
       // ContentFactory content = ContentFactory
        toolWindow.getContentManager().addContent(content);
    }
}
