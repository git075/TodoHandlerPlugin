package dev.anurag.todohandlerplugin.util;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import dev.anurag.todohandlerplugin.TodoScanner;
import dev.anurag.todohandlerplugin.service.AIService;
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

        this.allTodos = todos;
        saveTodosToState(todos);

        searchField = new JTextField();
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterTodos(model); }
            public void removeUpdate(DocumentEvent e) { filterTodos(model); }
            public void changedUpdate(DocumentEvent e) { filterTodos(model); }
        });

        for (TodoScanner.TodoItem todo : todos) {
            model.addElement(todo.toString());
        }

        todoList = new JList<>(model);
        JScrollPane scrollPane = new JScrollPane(todoList);

        // AI Suggestion Button
        JButton aiButton = new JButton("Get AI Suggestion");
        aiButton.addActionListener(e -> {
            int index = todoList.getSelectedIndex();
            if (index < 0 || index >= allTodos.size()) {
                JOptionPane.showMessageDialog(panel, "Please select a TODO item.");
                return;
            }

            String selectedTodo = allTodos.get(index).text;
            String fullCode = editor.getDocument().getText();

            AIService.fetchSuggestion(selectedTodo, fullCode, suggestion -> {
                JTextArea suggestionArea = new JTextArea(suggestion);
                suggestionArea.setWrapStyleWord(true);
                suggestionArea.setLineWrap(true);
                suggestionArea.setEditable(false);

                JScrollPane suggestionScroll = new JScrollPane(suggestionArea);
                suggestionScroll.setPreferredSize(new Dimension(500, 300));

                JOptionPane.showMessageDialog(panel, suggestionScroll, "AI Suggestion", JOptionPane.INFORMATION_MESSAGE);
            });
        });

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(new JLabel(" Filter: "), BorderLayout.WEST);
        topPanel.add(searchField, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(aiButton);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

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

    public void updateTodoList(List<TodoScanner.TodoItem> newTodos, Editor newEditor) {
        this.allTodos = newTodos;
        saveTodosToState(newTodos);

        DefaultListModel<String> model = (DefaultListModel<String>) todoList.getModel();
        model.clear();
        for (TodoScanner.TodoItem todo : newTodos) {
            model.addElement(todo.toString());
        }

        todoList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int index = todoList.getSelectedIndex();
                if (index >= 0 && index < allTodos.size()) {
                    int line = allTodos.get(index).line;
                    int offset = newEditor.getDocument().getLineStartOffset(line);
                    newEditor.getCaretModel().moveToOffset(offset);
                    newEditor.getScrollingModel().scrollToCaret(com.intellij.openapi.editor.ScrollType.CENTER);
                }
            }
        });
    }
}
