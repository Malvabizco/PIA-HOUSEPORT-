package com.example.pia_claseordinaria;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton buttonConfig;
    private TextView textViewHeader;
    private View cardReservations, cardAnnouncements, cardComplaints, cardAccessControl, cardEncuestas, cardPayments, cardServices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        buttonConfig = findViewById(R.id.buttonConfig);
        textViewHeader = findViewById(R.id.textViewHeader);
        cardReservations = findViewById(R.id.cardReservations);
        cardAnnouncements = findViewById(R.id.cardAnnouncements);
        cardComplaints = findViewById(R.id.cardComplaints);
        cardAccessControl = findViewById(R.id.cardAccessControl);
        cardEncuestas = findViewById(R.id.cardEncuestas);
        cardPayments = findViewById(R.id.cardPayments);
        cardServices = findViewById(R.id.cardServices);

        String role = getIntent().getStringExtra("ROLE");
        if (role == null) role = "USER";

        if ("ADMIN".equals(role)) {
            textViewHeader.setText("PANEL DE CONTROL");
        } else {
            loadUserAddress();
        }

        buttonConfig.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));

        cardReservations.setOnClickListener(v -> startActivity(new Intent(this, ReservationsActivity.class)));
        cardAnnouncements.setOnClickListener(v -> startActivity(new Intent(this, AnnouncementsActivity.class)));
        cardComplaints.setOnClickListener(v -> startActivity(new Intent(this, ComplaintsActivity.class)));
        cardEncuestas.setOnClickListener(v -> startActivity(new Intent(this, SurveysActivity.class)));
        cardPayments.setOnClickListener(v -> startActivity(new Intent(this, PaymentsActivity.class)));
        
        String finalRole = role;
        cardServices.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScheduleServiceActivity.class);
            intent.putExtra("ROLE", finalRole);
            startActivity(intent);
        });

        cardAccessControl.setOnClickListener(v -> {
            Intent intent = new Intent(this, AccessControlActivity.class);
            intent.putExtra("ROLE", finalRole);
            startActivity(intent);
        });

        setupSidebar();
    }

    private void loadUserAddress() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance().collection("usuarios").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String address = documentSnapshot.getString("address");
                        if (address != null && !address.isEmpty()) {
                            textViewHeader.setText(address.toUpperCase());
                        }
                    }
                });
        }
    }

    private void setupSidebar() {
        Button btnLogout = findViewById(R.id.nav_logout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
        }
    }
}
