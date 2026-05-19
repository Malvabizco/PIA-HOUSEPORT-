package com.example.pia_claseordinaria;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AnnouncementsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AnnouncementAdapter adapter;
    private List<Map<String, Object>> announcementList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcements);

        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerViewAnnouncements);
        ImageButton buttonBack = findViewById(R.id.buttonBack);

        announcementList = new ArrayList<>();
        adapter = new AnnouncementAdapter(announcementList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        buttonBack.setOnClickListener(v -> finish());

        loadAnnouncements();
    }

    private void loadAnnouncements() {
        db.collection("avisos")
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    announcementList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        announcementList.add(document.getData());
                    }
                    adapter.notifyDataSetChanged();
                    if (announcementList.isEmpty()) {
                        Toast.makeText(this, "No hay comunicados oficiales", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar avisos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
