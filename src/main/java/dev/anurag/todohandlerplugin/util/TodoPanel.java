package dev.anurag.todohandlerplugin.util;


import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import dev.anurag.todohandlerplugin.TodoScanner;
import dev.anurag.todohandlerplugin.service.TodoStateService;


import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TodoPanel {

    private JPanel panel;
    private JList<String> todoList;
    private JTextField searchField;

    private List<TodoScanner.TodoItem> allTodos;

    public TodoPanel(Project project, Editor editor, List<TodoScanner.TodoItem> todos) {
        panel = new JPanel(new BorderLayout());
        DefaultListModel<String> model = new DefaultListModel<>();

        // Store and persist todos
        this.allTodos = todos;
        saveTodosToState(todos);

        // Top filter bar
        searchField = new JTextField();
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterTodos(model); }
            public void removeUpdate(DocumentEvent e) { filterTodos(model); }
            public void changedUpdate(DocumentEvent e) { filterTodos(model); }
        });

        // Initial load
        for (TodoScanner.TodoItem todo : todos) {
            model.addElement(todo.toString());
        }

        todoList = new JList<>(model);
        panel.add(new JScrollPane(todoList), BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(new JLabel(" Filter: "), BorderLayout.WEST);
        topPanel.add(searchField, BorderLayout.CENTER);
        panel.add(topPanel, BorderLayout.NORTH);

        // List selection logic
        todoList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int index = todoList.getSelectedIndex();
                if (index >= 0 && index < allTodos.size()) {
                    int line = allTodos.get(index).line;
                    int offset = editor.getDocument().getLineStartOffset(line);
                    editor.getCaretModel().moveToOffset(offset);
                    editor.getScrollingModel().scrollToCaret(com.intellij.openapi.editor.ScrollType.CENTER);
                }
            }
        });
    }

    public JPanel getPanel() {
        return panel;
    }


    private void filterTodos(DefaultListModel<String> model) {
        String keyword = searchField.getText().trim().toLowerCase();
        model.clear();

        for (TodoScanner.TodoItem todo : allTodos) {
            if (todo.text.toLowerCase().contains(keyword)) {
                model.addElement(todo.toString());
            }
        }
    }

    private void saveTodosToState(List<TodoScanner.TodoItem> todos) {
        List<String> todoTexts = new ArrayList<>();
        for (TodoScanner.TodoItem todo : todos) {
            todoTexts.add(todo.toString());
        }
        TodoStateService.getInstance().setTodos(todoTexts);
    }
}
