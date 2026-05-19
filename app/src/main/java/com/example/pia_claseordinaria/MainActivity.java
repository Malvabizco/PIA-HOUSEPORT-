package com.example.pia_claseordinaria;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    private TextView textViewRegister;
    private MaterialButtonToggleGroup roleToggleButtonGroup;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegister = findViewById(R.id.textViewRegister);
        roleToggleButtonGroup = findViewById(R.id.roleToggleButtonGroup);
        progressBar = findViewById(R.id.progressBar);

        buttonLogin.setOnClickListener(this);
        textViewRegister.setOnClickListener(this);
        textViewRegister.setVisibility(View.GONE);

        // Persistencia de sesión habilitada para acceso instantáneo
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            String cachedRole = getCachedRole(currentUser.getUid());
            String cachedStatus = getCachedStatus(currentUser.getUid());
            if (cachedRole != null && "active".equals(cachedStatus)) {
                // Redirección instantánea basada en caché (SWR)
                redirectToDashboardByRoleName(cachedRole);
                // Revalidar en segundo plano
                revalidateUserSessionInBackground(currentUser.getUid(), cachedRole);
            } else {
                checkUserRoleAndRedirect(currentUser.getUid(), null);
            }
        }
    }

    // --- MÉTODOS DE CACHÉ LOCAL (SWR) ---
    private static final String PREFS_NAME = "HousePortPrefs";
    private static final String KEY_USER_ROLE = "user_role_";
    private static final String KEY_USER_STATUS = "user_status_";

    private void saveUserCache(String uid, String role, String status) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString(KEY_USER_ROLE + uid, role)
                .putString(KEY_USER_STATUS + uid, status)
                .apply();
    }

    private String getCachedRole(String uid) {
        return getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(KEY_USER_ROLE + uid, null);
    }

    private String getCachedStatus(String uid) {
        return getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(KEY_USER_STATUS + uid, "active");
    }

    private void clearUserCache(String uid) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .remove(KEY_USER_ROLE + uid)
                .remove(KEY_USER_STATUS + uid)
                .apply();
    }

    private void revalidateUserSessionInBackground(String uid, String currentCachedRole) {
        db.collection("usuarios").document(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot doc = task.getResult();
                if (doc.exists()) {
                    String roleInDb = doc.getString("role");
                    String status = doc.getString("status");

                    saveUserCache(uid, roleInDb, status);

                    // Si ya no está activo o su rol cambió, cerrar sesión y forzar redirección correctiva
                    if (!"active".equals(status) || !currentCachedRole.equals(roleInDb)) {
                        mAuth.signOut();
                        clearUserCache(uid);
                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                }
            }
        });
    }

    private void checkUserRoleAndRedirect(String uid, String selectedRole) {
        progressBar.setVisibility(View.VISIBLE);
        buttonLogin.setEnabled(false);

        db.collection("usuarios").document(uid).get().addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            buttonLogin.setEnabled(true);

            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot doc = task.getResult();
                String roleInDb = doc.exists() ? doc.getString("role") : "USER";
                String status = doc.exists() ? doc.getString("status") : "active";

                // Guardar en caché local
                saveUserCache(uid, roleInDb, status);

                if (!"active".equals(status)) {
                    startActivity(new Intent(MainActivity.this, PendingActivationActivity.class));
                    finish();
                    return;
                }

                // VALIDACIÓN DE SEGURIDAD DEL ROL:
                // Si el usuario seleccionó iniciar con un rol diferente a su rol real de la DB, bloquear el login
                if (selectedRole != null && !roleInDb.equals(selectedRole)) {
                    mAuth.signOut();
                    clearUserCache(uid);
                    Toast.makeText(MainActivity.this, "No tienes permisos para ingresar como " + selectedRole, Toast.LENGTH_LONG).show();
                    return;
                }

                // Prioridad al rol seleccionado en login, si no, al de la DB
                String finalRole = (selectedRole != null) ? selectedRole : roleInDb;

                Intent intent;
                if ("ADMIN".equals(finalRole)) {
                    intent = new Intent(MainActivity.this, AdminDashboardActivity.class);
                } else if ("GUARD".equals(finalRole)) {
                    intent = new Intent(MainActivity.this, GuardDashboardActivity.class);
                } else {
                    intent = new Intent(MainActivity.this, ProfileActivity.class);
                }
                startActivity(intent);
                finish();
            } else {
                // Fallback para errores de red o perfiles manuales
                if (selectedRole != null) {
                    redirectToDashboardByRoleName(selectedRole);
                }
            }
        });
    }

    private void redirectToDashboardByRoleName(String role) {
        Intent intent;
        if ("ADMIN".equals(role)) {
            intent = new Intent(this, AdminDashboardActivity.class);
        } else if ("GUARD".equals(role)) {
            intent = new Intent(this, GuardDashboardActivity.class);
        } else {
            intent = new Intent(this, ProfileActivity.class);
        }
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.buttonLogin) {
            loginUser();
        } else if (v.getId() == R.id.textViewRegister) {
            startActivity(new Intent(this, RegisterActivity.class));
        }
    }

    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        int checkedId = roleToggleButtonGroup.getCheckedButtonId();
        final String selectedRole;
        if (checkedId == R.id.btnRoleAdmin) selectedRole = "ADMIN";
        else if (checkedId == R.id.btnRoleGuard) selectedRole = "GUARD";
        else selectedRole = "USER";

        progressBar.setVisibility(View.VISIBLE);
        buttonLogin.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    if (user.isEmailVerified()) {
                        String cachedRole = getCachedRole(user.getUid());
                        String cachedStatus = getCachedStatus(user.getUid());

                        // Redirección optimista instantánea solo si la caché coincide con el rol seleccionado
                        if (cachedRole != null && "active".equals(cachedStatus) && cachedRole.equals(selectedRole)) {
                            // Redirección optimista instantánea!
                            redirectToDashboardByRoleName(cachedRole);
                            // Revalidar en segundo plano
                            revalidateUserSessionInBackground(user.getUid(), cachedRole);
                        } else {
                            checkUserRoleAndRedirect(user.getUid(), selectedRole);
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        buttonLogin.setEnabled(true);
                        startActivity(new Intent(this, VerifyEmailActivity.class));
                    }
                }
            } else {
                progressBar.setVisibility(View.GONE);
                buttonLogin.setEnabled(true);
                Toast.makeText(MainActivity.this, "Credenciales incorrectas", Toast.LENGTH_LONG).show();
            }
        });
    }
}
