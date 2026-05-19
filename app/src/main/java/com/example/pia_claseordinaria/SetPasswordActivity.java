package com.example.pia_claseordinaria;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SetPasswordActivity extends AppCompatActivity {

    private EditText editTextPassword, editTextConfirmPassword;
    private Button buttonRegister;
    private TextView textViewUserInfo, textViewEditInfo;
    private ImageButton backButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String fullName, email, phone, address, condominio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_password);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewUserInfo = findViewById(R.id.textViewUserInfo);
        textViewEditInfo = findViewById(R.id.textViewEditInfo);
        backButton = findViewById(R.id.backButton);

        Intent intent = getIntent();
        fullName = intent.getStringExtra("fullName");
        email = intent.getStringExtra("email");
        phone = intent.getStringExtra("phone");
        address = intent.getStringExtra("address");
        condominio = intent.getStringExtra("condominio");

        textViewUserInfo.setText("Correo: " + email + "\nTeléfono: " + phone);

        backButton.setOnClickListener(v -> finish());
        textViewEditInfo.setOnClickListener(v -> finish());
        buttonRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        if (password.isEmpty()) {
            editTextPassword.setError("La contraseña es obligatoria");
            editTextPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            editTextPassword.setError("Mínimo 6 caracteres");
            editTextPassword.requestFocus();
            return;
        }
        if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Las contraseñas no coinciden");
            editTextConfirmPassword.requestFocus();
            return;
        }

        // Crear una instancia secundaria de FirebaseApp para no cerrar la sesión del Admin en la instancia principal
        com.google.firebase.FirebaseOptions options = com.google.firebase.FirebaseApp.getInstance().getOptions();
        com.google.firebase.FirebaseApp tempApp;
        try {
            tempApp = com.google.firebase.FirebaseApp.getInstance("tempRegisterApp");
        } catch (IllegalStateException e) {
            tempApp = com.google.firebase.FirebaseApp.initializeApp(this, options, "tempRegisterApp");
        }
        FirebaseAuth tempAuth = FirebaseAuth.getInstance(tempApp);

        tempAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, authTask -> {
                    if (authTask.isSuccessful()) {
                        FirebaseUser firebaseUser = authTask.getResult().getUser();
                        if (firebaseUser != null) {
                            saveUserData(firebaseUser);
                        } else {
                            showErrorDialog("Ocurrió un error inesperado al obtener los datos de usuario.");
                        }
                    } else {
                        String errorMessage = authTask.getException() != null ? authTask.getException().getMessage() : "Error desconocido.";
                        showErrorDialog("No se pudo crear la cuenta. Es posible que el correo ya esté en uso.\n\nCausa: " + errorMessage);
                    }
                });
    }

    private void saveUserData(FirebaseUser firebaseUser) {
        User user = new User(fullName, email, phone, address, condominio);

        db.collection("usuarios").document(firebaseUser.getUid()).set(user)
                .addOnCompleteListener(dbTask -> {
                    if (dbTask.isSuccessful()) {
                        sendVerificationEmailAndNavigate(firebaseUser);
                    } else {
                        firebaseUser.delete().addOnCompleteListener(deleteTask -> {
                            String errorMessage = dbTask.getException() != null ? dbTask.getException().getMessage() : "Revisa las reglas de seguridad de tu base de datos.";
                            showErrorDialog("No se pudieron guardar tus datos en Firestore. Se canceló el registro.\n\nCausa: " + errorMessage);
                        });
                    }
                });
    }

    private void sendVerificationEmailAndNavigate(FirebaseUser firebaseUser) {
        firebaseUser.sendEmailVerification().addOnCompleteListener(emailTask -> {
            if (!emailTask.isSuccessful()) {
                Toast.makeText(this, "No se pudo enviar el correo de verificación.", Toast.LENGTH_LONG).show();
            }

            // Eliminar la app temporal para liberar recursos
            try {
                com.google.firebase.FirebaseApp.getInstance("tempRegisterApp").delete();
            } catch (Exception e) {
                e.printStackTrace();
            }

            Toast.makeText(SetPasswordActivity.this, "Usuario registrado con éxito en estado pendiente", Toast.LENGTH_LONG).show();

            // Regresar al dashboard del Admin sin cerrar su sesión
            Intent intent = new Intent(SetPasswordActivity.this, AdminDashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error en el Registro")
                .setMessage(message)
                .setPositiveButton("Entendido", null)
                .setCancelable(false)
                .show();
    }
}
