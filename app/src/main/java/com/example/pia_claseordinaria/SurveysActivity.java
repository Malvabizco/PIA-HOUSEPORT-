package com.example.pia_claseordinaria;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SurveysActivity extends AppCompatActivity {

    private RecyclerView recyclerViewSurveys;
    private Button btnMarkAsRead;
    // Aquí podrías usar un adaptador para mostrar las encuestas reales de Firebase
    // private SurveyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surveys);

        recyclerViewSurveys = findViewById(R.id.recyclerViewSurveys);
        btnMarkAsRead = findViewById(R.id.btnMarkAsRead);

        findViewById(R.id.buttonBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        recyclerViewSurveys.setLayoutManager(new LinearLayoutManager(this));
        
        // Configuración del botón "Marcar como leído"
        btnMarkAsRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                marcarComoLeido();
            }
        });

        // Aquí cargarías las encuestas de Firebase Firestore
        cargarEncuestas();
    }

    private void cargarEncuestas() {
        // Implementación futura para cargar desde Firestore
        // Toast.makeText(this, "Cargando encuestas...", Toast.LENGTH_SHORT).show();
    }

    private void marcarComoLeido() {
        // Lógica para actualizar el estado en Firebase
        Toast.makeText(this, "Encuestas marcadas como leídas", Toast.LENGTH_SHORT).show();
        // Podrías limpiar la lista o actualizar el UI aquí
    }
}