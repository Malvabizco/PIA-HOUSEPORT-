package com.example.pia_claseordinaria;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class PendingUsersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<User> userList;
    private List<String> userIdList;
    private FirebaseFirestore db;
    private ImageButton buttonBack;
    private MaterialButtonToggleGroup toggleGroupFilters;
    private boolean currentFilterPending = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_users);

        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerViewPendingUsers);
        buttonBack = findViewById(R.id.buttonBack);
        toggleGroupFilters = findViewById(R.id.toggleGroupFilters);

        userList = new ArrayList<>();
        userIdList = new ArrayList<>();
        adapter = new UserAdapter(userList, userIdList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        buttonBack.setOnClickListener(v -> finish());

        // Setup filter selection changes
        toggleGroupFilters.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnFilterAll) {
                    currentFilterPending = false;
                    loadUsers(false);
                } else if (checkedId == R.id.btnFilterPending) {
                    currentFilterPending = true;
                    loadUsers(true);
                }
            }
        });

        // Load initially with pending users as default
        loadUsers(true);
    }

    private void loadUsers(boolean pendingOnly) {
        Query query = db.collection("usuarios");
        if (pendingOnly) {
            query = query.whereEqualTo("status", "pending");
        }

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userList.clear();
                    userIdList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        User user = document.toObject(User.class);
                        if (user != null) {
                            userList.add(user);
                            userIdList.add(document.getId());
                        }
                    }
                    adapter.notifyDataSetChanged();
                    if (userList.isEmpty()) {
                        String message = pendingOnly ? "No hay usuarios pendientes" : "No hay usuarios registrados";
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar usuarios: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload when returning to activity to ensure freshness
        loadUsers(currentFilterPending);
    }
}
