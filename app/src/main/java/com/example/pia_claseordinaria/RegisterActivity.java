package com.example.pia_claseordinaria;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextFullName, editTextEmail, editTextPhone, editTextAddress, editTextCondominio;
    private Button buttonNext;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editTextFullName = findViewById(R.id.editTextFullName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextAddress = findViewById(R.id.editTextAddress);
        editTextCondominio = findViewById(R.id.editTextCondominio);
        buttonNext = findViewById(R.id.buttonNext);
        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> finish());

        buttonNext.setOnClickListener(v -> {
            goToSetPassword();
        });
    }

    private void goToSetPassword() {
        String fullName = editTextFullName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();
        String condominio = editTextCondominio.getText().toString().trim();

        if (fullName.isEmpty()) {
            editTextFullName.setError("El nombre es obligatorio");
            editTextFullName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            editTextEmail.setError("El correo es obligatorio");
            editTextEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Proporcione un correo válido");
            editTextEmail.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            editTextPhone.setError("El teléfono es obligatorio");
            editTextPhone.requestFocus();
            return;
        }

        if (condominio.isEmpty()) {
            editTextCondominio.setError("El condominio es obligatorio");
            editTextCondominio.requestFocus();
            return;
        }

        if (address.isEmpty()) {
            editTextAddress.setError("La dirección es obligatoria");
            editTextAddress.requestFocus();
            return;
        }

        // Pasar a la siguiente pantalla con los datos
        Intent intent = new Intent(RegisterActivity.this, SetPasswordActivity.class);
        intent.putExtra("fullName", fullName);
        intent.putExtra("email", email);
        intent.putExtra("phone", phone);
        intent.putExtra("address", address);
        intent.putExtra("condominio", condominio);
        startActivity(intent);
    }
}
