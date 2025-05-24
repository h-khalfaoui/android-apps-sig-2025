package com.example.medcare.ui;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medcare.R;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import okhttp3.*;

public class ChatbotFragment2 extends Fragment {

    private static final String TAG = "ChatbotFragment";
    private static final String LOCAL_API_URL = "http://192.168.8.107:11436/api/generate";
    private static final String LOCAL_MODEL = "gemma:2b-instruct";

    private EditText userInput;
    private Button sendButton;
    private ProgressBar progressBar;
    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages = new ArrayList<>();

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    private final String[] defaultResponses = {
            "Je suis là pour vous aider. Que puis-je faire pour vous?",
            "Je comprends votre message. Avez-vous des questions spécifiques?",
            "Je suis à votre écoute. Comment puis-je vous assister aujourd'hui?",
            "Bien sûr, je suis là pour vous. Que puis-je faire pour vous?",
            "Je vous écoute. N'hésitez pas à me poser des questions spécifiques.",
            "Je ferai de mon mieux pour vous aider. Pourriez-vous préciser votre demande?"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chatbot2, container, false);

        userInput = view.findViewById(R.id.inputEditText);
        sendButton = view.findViewById(R.id.sendButton);
        progressBar = view.findViewById(R.id.progressBar);
        chatRecyclerView = view.findViewById(R.id.chatRecyclerView);

        // Configurer le RecyclerView
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatAdapter = new ChatAdapter(chatMessages);
        chatRecyclerView.setAdapter(chatAdapter);

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        // Ajouter le message de bienvenue
        addBotMessage("Bonjour ! Je Suis l'Assistant Médical MedWay à Votre Service. Comment Puis-je Vous Aider Aujourd'hui ?");

        sendButton.setOnClickListener(v -> {
            String input = userInput.getText().toString().trim();
            if (!input.isEmpty()) {
                addUserMessage(input);
                if (progressBar != null) {
                    progressBar.setVisibility(View.VISIBLE);
                }
                sendButton.setEnabled(false);
                sendMessageToChatbot(input);
                userInput.setText("");
            }
        });

        return view;
    }

    private void addUserMessage(String message) {
        chatMessages.add(new ChatMessage(message, true));
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        scrollToBottom();
    }

    private void addBotMessage(String message) {
        chatMessages.add(new ChatMessage(message, false));
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        scrollToBottom();
    }

    private void scrollToBottom() {
        chatRecyclerView.post(() -> chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1));
    }

    private void sendMessageToChatbot(String message) {
        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("model", LOCAL_MODEL);

            // Format simplifié spécifique à Gemma
            jsonRequest.put("prompt", "Tu es un assistant médical bilingue. " +
                    "Réponds dans la langue de l'utilisateur. Question: " + message);

            jsonRequest.put("stream", false);

            // Options optimisées
            JSONObject options = new JSONObject();
            options.put("num_ctx", 512);
            options.put("temperature", 0.7);
            options.put("num_thread", 2);
            jsonRequest.put("options", options);

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"),
                    jsonRequest.toString()
            );

            Request request = new Request.Builder()
                    .url(LOCAL_API_URL)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "API call failed", e);
                    requireActivity().runOnUiThread(() -> handleError());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    Log.d(TAG, "API Response: " + responseBody);

                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        String botReply = jsonResponse.optString("response", "");

                        if (botReply.isEmpty()) {
                            Log.e(TAG, "Received empty response");
                            requireActivity().runOnUiThread(() -> handleError());
                        } else {
                            requireActivity().runOnUiThread(() -> displayBotReply(botReply));
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error", e);
                        requireActivity().runOnUiThread(() -> handleError());
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error creating request", e);
            handleError();
        }
    }

    private void handleError() {
        if (getActivity() != null) {
            requireActivity().runOnUiThread(() -> {
                displayLocalResponse();
                resetUI();
            });
        }
    }

    private void displayBotReply(String reply) {
        if (getActivity() == null) return;

        String cleanedReply = reply.trim();
        if (!cleanedReply.isEmpty()) {
            addBotMessage(cleanedReply);
        } else {
            addBotMessage("[Réponse vide reçue]");
        }
        resetUI();
    }

    private void displayLocalResponse() {
        String defaultResponse = defaultResponses[new Random().nextInt(defaultResponses.length)];
        addBotMessage(defaultResponse);

        Toast.makeText(getContext(),
                "Désolé, le service est temporairement indisponible. Utilisation du mode hors ligne.",
                Toast.LENGTH_SHORT).show();
    }

    private void resetUI() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        sendButton.setEnabled(true);
    }

    // Classe pour représenter un message
    private static class ChatMessage {
        private final String message;
        private final boolean isUser;  // true pour utilisateur, false pour assistant

        public ChatMessage(String message, boolean isUser) {
            this.message = message;
            this.isUser = isUser;
        }

        public String getMessage() {
            return message;
        }

        public boolean isUser() {
            return isUser;
        }
    }

    // Adapter pour le RecyclerView
    private static class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
        private final List<ChatMessage> messages;

        public ChatAdapter(List<ChatMessage> messages) {
            this.messages = messages;
        }

        @NonNull
        @Override
        public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(viewType == 1 ? R.layout.item_user_message : R.layout.item_bot_message, parent, false);
            return new ChatViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
            ChatMessage message = messages.get(position);
            holder.messageText.setText(message.getMessage());
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        @Override
        public int getItemViewType(int position) {
            // 1 pour utilisateur, 0 pour assistant
            return messages.get(position).isUser() ? 1 : 0;
        }

        static class ChatViewHolder extends RecyclerView.ViewHolder {
            TextView messageText;

            public ChatViewHolder(@NonNull View itemView) {
                super(itemView);
                messageText = itemView.findViewById(R.id.messageText);
            }
        }
    }
}