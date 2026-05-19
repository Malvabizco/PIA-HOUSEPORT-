package com.example.pia_claseordinaria;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class PendingActivationActivity extends AppCompatActivity {

    private Button buttonBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_activation);

        buttonBackToLogin = findViewById(R.id.buttonBackToLogin);

        buttonBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(PendingActivationActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
