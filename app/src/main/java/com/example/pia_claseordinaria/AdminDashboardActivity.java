package com.example.pia_claseordinaria;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboardActivity extends AppCompatActivity {

    private MaterialCardView cardPendingUsers, cardRegisterUser, cardPostAnnouncement, cardViewComplaints, cardSecurityChat, cardScheduleService, cardAdminStats, cardManageDeptos;
    private ImageButton buttonLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);


        cardPendingUsers = findViewById(R.id.cardPendingUsers);
        cardRegisterUser = findViewById(R.id.cardRegisterUser);
        cardPostAnnouncement = findViewById(R.id.cardPostAnnouncement);
        cardViewComplaints = findViewById(R.id.cardViewComplaints);
        cardSecurityChat = findViewById(R.id.cardSecurityChat);
        cardScheduleService = findViewById(R.id.cardScheduleService);
        cardAdminStats = findViewById(R.id.cardAdminStats);
        cardManageDeptos = findViewById(R.id.cardManageDeptos);
        buttonLogout = findViewById(R.id.buttonLogout);

        cardPendingUsers.setOnClickListener(v -> startActivity(new Intent(this, PendingUsersActivity.class)));
        cardRegisterUser.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        cardPostAnnouncement.setOnClickListener(v -> startActivity(new Intent(this, AdminPostAnnouncementActivity.class)));
        cardViewComplaints.setOnClickListener(v -> startActivity(new Intent(this, AdminComplaintsActivity.class)));
        cardSecurityChat.setOnClickListener(v -> startActivity(new Intent(this, ChatActivity.class)));
        
        if (cardScheduleService != null) {
            // El admin va a la pantalla de GESTIÓN de servicios (donde contrata y asigna)
            cardScheduleService.setOnClickListener(v -> startActivity(new Intent(this, AdminServiceActivity.class)));
        }

        if (cardAdminStats != null) {
            cardAdminStats.setOnClickListener(v -> startActivity(new Intent(this, AdminStatsActivity.class)));
        }

        if (cardManageDeptos != null) {
            cardManageDeptos.setOnClickListener(v -> startActivity(new Intent(this, ManageDepartmentsActivity.class)));
        }

        buttonLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }
}
