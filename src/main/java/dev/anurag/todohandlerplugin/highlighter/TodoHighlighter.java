package dev.anurag.todohandlerplugin.highlighter;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import dev.anurag.todohandlerplugin.TodoScanner;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TodoHighlighter {
    private final List<RangeHighlighter> highlighters = new ArrayList<>();

    public void highlightTodos(Editor editor, List<TodoScanner.TodoItem> todos) {
        clearHighlights(editor);
        MarkupModel markupModel = editor.getMarkupModel();

        for (TodoScanner.TodoItem todo : todos) {
            int start = editor.getDocument().getLineStartOffset(todo.line);
            int end = editor.getDocument().getLineEndOffset(todo.line);

            RangeHighlighter highlighter = markupModel.addRangeHighlighter(
                    start,
                    end,
                    0,
                    new TextAttributes(Color.RED, null, null, null, Font.BOLD),
                    HighlighterTargetArea.EXACT_RANGE
            );
            highlighters.add(highlighter);
        }
    }

    public void clearHighlights(Editor editor) {
        MarkupModel markupModel = editor.getMarkupModel();
        for (RangeHighlighter highlighter : highlighters) {
            markupModel.removeHighlighter(highlighter);
        }
        highlighters.clear();
    }
}
