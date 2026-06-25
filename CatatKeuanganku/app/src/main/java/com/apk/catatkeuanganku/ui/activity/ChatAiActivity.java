package com.apk.catatkeuanganku.ui.activity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.apk.catatkeuanganku.R;
import com.apk.catatkeuanganku.database.AppDatabase;
import com.apk.catatkeuanganku.database.ChatMessage;
import com.apk.catatkeuanganku.database.TransactionEntity;
import com.apk.catatkeuanganku.ui.adapter.ChatAdapter;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.GenerationConfig;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ChatAiActivity extends AppCompatActivity {

    private RecyclerView rvChat;
    private EditText etMessage;
    private ImageButton btnSend;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages = new ArrayList<>();
    private GenerativeModelFutures model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_ai);

        // Inisialisasi UI
        rvChat = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        chatAdapter = new ChatAdapter(chatMessages);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(chatAdapter);

        // --- KONFIGURASI GEMINI 2.0 FLASH ---
        String apiKey = "AIzaSyD6OtJtetUsWRfrh7I6_VTIRiX_APpZwIY";
        String modelName = "gemini-2.0-flash"; // Sesuai yang dicentang di Settings

        GenerationConfig.Builder configBuilder = new GenerationConfig.Builder();
        configBuilder.temperature = 0.7f;
        GenerationConfig config = configBuilder.build();

        GenerativeModel gm = new GenerativeModel(modelName, apiKey, config);
        model = GenerativeModelFutures.from(gm);
        // -------------------------------------

        btnSend.setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessage(message);
            }
        });

        // Greeting awal
        if (chatMessages.isEmpty()) {
            addMessage("Halo! Saya FinBot, konsultan keuangan pribadi Anda. Ada yang bisa saya bantu hari ini?", ChatMessage.ROLE_AI);
        }
    }

    private void sendMessage(String userText) {
        // Tampilkan pesan user di UI
        addMessage(userText, ChatMessage.ROLE_USER);
        etMessage.setText("");

        // Jalankan di background thread untuk ambil data DB dan panggil AI
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                // 1. Ambil data dari database
                List<TransactionEntity> transactions = AppDatabase.getInstance(this).transactionDao().getAllTransactions();
                long balance = AppDatabase.getInstance(this).transactionDao().getCurrentBalance();

                // 2. Rancang Prompt (Context)
                StringBuilder context = new StringBuilder();
                context.append("Kamu adalah FinBot, asisten keuangan cerdas. Gunakan data berikut untuk menjawab secara personal.\n");
                context.append("Saldo saat ini: Rp ").append(balance).append("\n");
                context.append("Riwayat transaksi terakhir:\n");

                // Batasi riwayat transaksi agar token tidak bengkak
                int limit = Math.min(transactions.size(), 10);
                for (int i = 0; i < limit; i++) {
                    TransactionEntity t = transactions.get(i);
                    context.append("- ").append(t.getType()).append(": ")
                            .append(t.getName()).append(" (Rp ").append(t.getAmount())
                            .append(") pada ").append(t.getDate()).append("\n");
                }

                context.append("\nUser bertanya: ").append(userText);

                // 3. Siapkan Content
                Content content = new Content.Builder()
                        .addText(context.toString())
                        .build();

                // 4. Panggil Gemini API
                ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

                Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                    @Override
                    public void onSuccess(GenerateContentResponse result) {
                        String aiResponse = result.getText();
                        runOnUiThread(() -> addMessage(aiResponse, ChatMessage.ROLE_AI));
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        runOnUiThread(() -> addMessage("Error: " + t.getMessage(), ChatMessage.ROLE_AI));
                    }
                }, executor);

            } catch (Exception e) {
                runOnUiThread(() -> addMessage("Gagal mengambil data database: " + e.getMessage(), ChatMessage.ROLE_AI));
            }
        });
    }

    private void addMessage(String text, String role) {
        chatMessages.add(new ChatMessage(text, role));
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        rvChat.scrollToPosition(chatMessages.size() - 1);
    }
}