package dev.anurag.todohandlerplugin.service;

import com.intellij.openapi.components.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@State(
        name = "TodoStateService",
        storages = @Storage("TodoHandlerPlugin.xml")
)
public class TodoStateService implements PersistentStateComponent<TodoStateService.State> {

    public static class State {
        public List<String> todos = new ArrayList<>();
    }

    private State state = new State();

    public static TodoStateService getInstance() {
        return ServiceManager.getService(TodoStateService.class);
    }

    @Override
    public @Nullable State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }

    public List<String> getTodos() {
        return state.todos;
    }

    public void setTodos(List<String> todos) {
        state.todos = new ArrayList<>(todos);
    }
}
