package dev.anurag.todohandlerplugin.service;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.io.IOException;

public class AIService {

    private static final String API_KEY = "";
    private static final String ENDPOINT = "https://api.openai.com/v1/chat/completions";
    private static final OkHttpClient client = new OkHttpClient();

    public static void fetchSuggestion(String todoText, String fullCode, SuggestionCallback callback) {
        String prompt = "I am working on the following code:\n" +
                fullCode + "\n\n" +
                "There is a TODO comment in the code:\n" +
                "\"" + todoText + "\"\n\n" +
                "Please suggest how to implement this TODO step-by-step based on the code context.";

        //  Safely build JSON using org.json
        JSONObject requestBodyJson = new JSONObject();
        requestBodyJson.put("model", "gpt-3.5-turbo");

        JSONArray messages = new JSONArray();

        JSONObject systemMsg = new JSONObject();
        systemMsg.put("role", "system");
        systemMsg.put("content", "You are a helpful coding assistant.");
        messages.put(systemMsg);

        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", prompt);
        messages.put(userMsg);

        requestBodyJson.put("messages", messages);
        requestBodyJson.put("max_tokens", 300);
        requestBodyJson.put("temperature", 0.7);

        RequestBody body = RequestBody.create(
                requestBodyJson.toString(),
                MediaType.get("application/json")
        );

        Request request = new Request.Builder()
                .url(ENDPOINT)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                SwingUtilities.invokeLater(() ->
                        callback.onSuggestionReceived(" Failed to fetch suggestion: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "null";
                    System.err.println("API Error: " + response.code() + " - " + response.message());
                    System.err.println("Response Body: " + errorBody);
                    SwingUtilities.invokeLater(() ->
                            callback.onSuggestionReceived(" OpenAI API error: " + response.code() + " - " + response.message()));
                    return;
                }

                String result = response.body().string();
                String suggestion = extractSuggestion(result);
                SwingUtilities.invokeLater(() ->
                        callback.onSuggestionReceived(suggestion));
            }
        });
    }

    private static String extractSuggestion(String responseJson) {
        String pattern = "\"content\":\"(.*?)\"";
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.DOTALL)
                .matcher(responseJson);
        if (matcher.find()) {
            return matcher.group(1).replace("\\n", "\n").replace("\\\"", "\"");
        }
        return " No suggestion found.";
    }

    public interface SuggestionCallback {
        void onSuggestionReceived(String suggestion);
    }
}
