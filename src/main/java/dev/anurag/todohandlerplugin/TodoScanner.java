package dev.anurag.todohandlerplugin;

import com.intellij.openapi.editor.Document;
import java.util.ArrayList;
import java.util.List;

public class TodoScanner {
    public static class TodoItem {
        public final String text;
        public final int line;

        public TodoItem(String text, int line) {
            this.text = text;
            this.line = line;
        }

        @Override
        public String toString() {
            return "Line " + (line + 1) + ": " + text;
        }
    }

    public static List<TodoItem> scan(Document document) {
        List<TodoItem> todos = new ArrayList<>();
     // List<String> todos = TodoStorageService.getInstance().getTodos(file.getPath());

        String[] lines = document.getText().split("\n");

        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains("TODO")) {
                todos.add(new TodoItem(lines[i].trim(), i));
            }
        }
        return todos;
    }
}
