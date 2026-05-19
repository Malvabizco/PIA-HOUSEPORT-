package com.example.pia_claseordinaria;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class AdminPostAnnouncementActivity extends AppCompatActivity {

    private EditText editTextTitle, editTextMessage;
    private Button buttonPublish;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_post_announcement);


        db = FirebaseFirestore.getInstance();
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonPublish = findViewById(R.id.buttonPublish);
        ImageButton buttonBack = findViewById(R.id.buttonBack);

        buttonBack.setOnClickListener(v -> finish());

        buttonPublish.setOnClickListener(v -> {
            String title = editTextTitle.getText().toString().trim();
            String message = editTextMessage.getText().toString().trim();

            if (!title.isEmpty() && !message.isEmpty()) {
                publishAnnouncement(title, message);
            } else {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void publishAnnouncement(String title, String message) {
        Map<String, Object> announcement = new HashMap<>();
        announcement.put("titulo", title);
        announcement.put("mensaje", message);
        announcement.put("fecha", System.currentTimeMillis());

        db.collection("avisos").add(announcement)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Comunicado publicado con éxito", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al publicar", Toast.LENGTH_SHORT).show());
    }
}
