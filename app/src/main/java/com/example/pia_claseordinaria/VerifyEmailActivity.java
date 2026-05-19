package com.example.pia_claseordinaria;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class VerifyEmailActivity extends AppCompatActivity {

    private Button buttonResendEmail;
    private TextView textViewBackToLogin;

    private FirebaseAuth mAuth;
    private Handler handler;
    private Runnable checkEmailVerifiedRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email);

        mAuth = FirebaseAuth.getInstance();

        buttonResendEmail = findViewById(R.id.buttonResendEmail);
        textViewBackToLogin = findViewById(R.id.textViewBackToLogin);

        buttonResendEmail.setOnClickListener(v -> resendVerificationEmail());

        textViewBackToLogin.setOnClickListener(v -> {
            startActivity(new Intent(VerifyEmailActivity.this, MainActivity.class));
            finish();
        });

        // Iniciar la comprobación periódica del estado de verificación
        handler = new Handler();
        checkEmailVerifiedRunnable = new Runnable() {
            @Override
            public void run() {
                checkIfEmailIsVerified();
                handler.postDelayed(this, 3000); // Comprobar cada 3 segundos
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        handler.post(checkEmailVerifiedRunnable);
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(checkEmailVerifiedRunnable);
    }

    private void resendVerificationEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(VerifyEmailActivity.this, "Correo de verificación reenviado.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(VerifyEmailActivity.this, "Error al reenviar el correo.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void checkIfEmailIsVerified() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful() && user.isEmailVerified()) {
                    handler.removeCallbacks(checkEmailVerifiedRunnable); // Detener la comprobación
                    Toast.makeText(VerifyEmailActivity.this, "¡Correo verificado!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(VerifyEmailActivity.this, PendingActivationActivity.class));
                    finish();
                }
            });
        }
    }
}
