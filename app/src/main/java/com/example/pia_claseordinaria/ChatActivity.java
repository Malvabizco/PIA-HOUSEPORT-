package com.example.pia_claseordinaria;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerViewMessages;
    private EditText editTextMessage;
    private FloatingActionButton buttonSend;
    private ImageButton buttonBack;
    private ChatAdapter adapter;
    private List<ChatMessage> messageList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userName = "Usuario";
    private String userRole = "SEGURIDAD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        buttonBack = findViewById(R.id.buttonBack);

        messageList = new ArrayList<>();
        adapter = new ChatAdapter(messageList);
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Hace que los mensajes empiecen desde abajo
        recyclerViewMessages.setLayoutManager(layoutManager);
        recyclerViewMessages.setAdapter(adapter);

        // 2. Ajustar la lista cuando el teclado sube
        recyclerViewMessages.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom) {
                recyclerViewMessages.postDelayed(() -> {
                    if (messageList.size() > 0) {
                        recyclerViewMessages.smoothScrollToPosition(messageList.size() - 1);
                    }
                }, 100);
            }
        });

        buttonBack.setOnClickListener(v -> finish());
        buttonSend.setOnClickListener(v -> sendMessage());

        fetchUserData();
        listenForMessages();
    }

    private void fetchUserData() {
        String uid = mAuth.getUid();
        if (uid == null) return;

        db.collection("usuarios").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String fullName = documentSnapshot.getString("fullName");
                String role = documentSnapshot.getString("role");
                if (fullName != null) userName = fullName;
                if (role != null) userRole = role;
            }
        });
    }

    private void listenForMessages() {
        db.collection("chat_seguridad")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("ChatActivity", "Error al escuchar mensajes: " + error.getMessage());
                        return;
                    }
                    if (value != null) {
                        messageList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            ChatMessage msg = doc.toObject(ChatMessage.class);
                            if (msg != null) messageList.add(msg);
                        }
                        adapter.notifyDataSetChanged();
                        if (!messageList.isEmpty()) {
                            recyclerViewMessages.scrollToPosition(messageList.size() - 1);
                        }
                    }
                });
    }

    private void sendMessage() {
        String text = editTextMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        String uid = mAuth.getUid();
        if (uid == null) {
            Toast.makeText(this, "Error: No hay sesión activa", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear el objeto mensaje con los datos actuales
        ChatMessage msg = new ChatMessage(uid, userName, text, userRole);

        // Solución al error de envío: Asegurar que el objeto sea válido para Firestore
        db.collection("chat_seguridad").add(msg)
                .addOnSuccessListener(documentReference -> {
                    editTextMessage.setText("");
                })
                .addOnFailureListener(e -> {
                    Log.e("ChatActivity", "Error Firestore: " + e.getMessage());
                    Toast.makeText(this, "Error de conexión: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}