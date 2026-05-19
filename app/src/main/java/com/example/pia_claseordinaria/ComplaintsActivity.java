package com.example.pia_claseordinaria;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class ComplaintsActivity extends AppCompatActivity {

    private EditText editTextQueja;
    private Button buttonSendQueja;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaints);

        db = FirebaseFirestore.getInstance();
        editTextQueja = findViewById(R.id.editTextQueja);
        buttonSendQueja = findViewById(R.id.buttonSendQueja);
        ImageButton buttonBack = findViewById(R.id.buttonBack);

        if (buttonBack != null) buttonBack.setOnClickListener(v -> finish());

        buttonSendQueja.setOnClickListener(v -> {
            String queja = editTextQueja.getText().toString().trim();
            if (!queja.isEmpty()) {
                sendQuejaToFirestore(queja);
            } else {
                Toast.makeText(this, "Por favor escribe tu comentario", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendQuejaToFirestore(String mensaje) {
        String uid = FirebaseAuth.getInstance().getUid();
        String email = FirebaseAuth.getInstance().getCurrentUser() != null ? 
                       FirebaseAuth.getInstance().getCurrentUser().getEmail() : "anonimo";

        Map<String, Object> data = new HashMap<>();
        data.put("mensaje", mensaje);
        data.put("usuario", email);
        data.put("uid", uid);
        data.put("fecha", System.currentTimeMillis());

        db.collection("quejas").add(data)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "¡Comentario enviado con éxito!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Mostrar error detallado para diagnóstico
                    Toast.makeText(this, "Error de Firebase: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
