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

public class AdminComplaintsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ComplaintAdapter adapter;
    private List<Map<String, Object>> complaintList;
    private FirebaseFirestore db;
    private ImageButton buttonBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_complaints);

        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerViewComplaints);
        buttonBack = findViewById(R.id.buttonBack);

        complaintList = new ArrayList<>();
        adapter = new ComplaintAdapter(complaintList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        buttonBack.setOnClickListener(v -> finish());

        loadComplaints();
    }

    private void loadComplaints() {
        db.collection("quejas")
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    complaintList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        complaintList.add(document.getData());
                    }
                    adapter.notifyDataSetChanged();
                    if (complaintList.isEmpty()) {
                        Toast.makeText(this, "No hay quejas registradas", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar quejas: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
