package com.example.pia_claseordinaria;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pia_claseordinaria.utils.QRGenerator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class AccessControlActivity extends AppCompatActivity {

    private ImageView imageViewQR;
    private TextView textViewQRStatus;
    private Button buttonGenerateQR;
    private ImageButton buttonShareQR, buttonBack;
    private RecyclerView recyclerViewHistory;
    private AccessAdapter adapter;
    private List<AccessRecord> accessHistoryList;
    private AccessRecord latestRecord;
    private String latestDocId;
    private String userAddress = "Cargando...";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access_control);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        imageViewQR = findViewById(R.id.imageViewQR);
        textViewQRStatus = findViewById(R.id.textViewQRStatus);
        buttonGenerateQR = findViewById(R.id.buttonGenerateQR);
        buttonShareQR = findViewById(R.id.buttonShareQR);
        buttonBack = findViewById(R.id.buttonBack);
        recyclerViewHistory = findViewById(R.id.recyclerViewAccessHistory);

        accessHistoryList = new ArrayList<>();
        adapter = new AccessAdapter(accessHistoryList);
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewHistory.setAdapter(adapter);

        buttonBack.setOnClickListener(v -> finish());
        buttonGenerateQR.setOnClickListener(v -> generateNewAccessQR());
        
        if (buttonShareQR != null) {
            buttonShareQR.setOnClickListener(v -> shareCurrentTicket());
        }

        loadUserInfo();
        loadAccessHistory();
    }

    private void loadUserInfo() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("usuarios").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        userAddress = documentSnapshot.getString("address");
                        if (userAddress == null || userAddress.isEmpty()) userAddress = "Dirección no especificada";
                    }
                });
        }
    }

    private void shareCurrentTicket() {
        if (latestRecord == null || latestDocId == null) {
            Toast.makeText(this, "Genera un acceso primero", Toast.LENGTH_SHORT).show();
            return;
        }

        // Datos para el Ticket
        List<String> details = Arrays.asList(
            "Residente: " + latestRecord.email,
            "Destino: " + (latestRecord.address != null ? latestRecord.address : userAddress),
            "Fecha: " + latestRecord.date,
            "Hora: " + latestRecord.time,
            "ID Acceso: " + latestRecord.key
        );

        String qrContent = "ACCESO_CONDO|UID:" + latestRecord.userId + "|DESTINO:" + (latestRecord.address != null ? latestRecord.address : userAddress) + "|KEY:" + latestRecord.key;
        
        // Generar Ticket (Imagen con texto y QR)
        Bitmap ticketBitmap = QRGenerator.INSTANCE.createTicketBitmap("PASE DE ACCESO", details, qrContent);

        if (ticketBitmap == null) return;

        try {
            File cachePath = new File(getCacheDir(), "images");
            cachePath.mkdirs();
            File file = new File(cachePath, "ticket_acceso.png");
            FileOutputStream stream = new FileOutputStream(file);
            ticketBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            Uri contentUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);

            // ACTUALIZAR ESTATUS A COMPARTIDO
            db.collection("registro_de_entrada").document(latestDocId).update("status", "COMPARTIDO");

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/png");
            intent.putExtra(Intent.EXTRA_STREAM, contentUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Enviar Pase de Acceso"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadAccessHistory() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        db.collection("registro_de_entrada")
                .whereEqualTo("userId", user.getUid())
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("AccessControlActivity", "Error de SnapshotListener: " + error.getMessage(), error);
                        Toast.makeText(this, "Error al cargar historial: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (value != null) {
                        processDocuments(value.getDocuments());
                    }
                });
    }

    private void processDocuments(List<DocumentSnapshot> docs) {
        accessHistoryList.clear();
        List<PairIdRecord> tempItems = new ArrayList<>();

        for (DocumentSnapshot doc : docs) {
            try {
                AccessRecord record = doc.toObject(AccessRecord.class);
                if (record != null) {
                    tempItems.add(new PairIdRecord(doc.getId(), record));
                }
            } catch (Exception e) {
                Log.e("AccessControlActivity", "Error al deserializar registro: " + e.getMessage(), e);
            }
        }
        
        Collections.sort(tempItems, (o1, o2) -> Long.compare(o2.timestamp, o1.timestamp));

        for (PairIdRecord item : tempItems) {
            accessHistoryList.add(item.record);
        }

        if (!accessHistoryList.isEmpty()) {
            latestRecord = accessHistoryList.get(0);
            latestDocId = tempItems.get(0).id;
            displayLatestQR(latestRecord);
        } else {
            imageViewQR.setImageResource(android.R.drawable.ic_menu_gallery);
            textViewQRStatus.setText("Genera un nuevo código para entrar");
        }
        adapter.notifyDataSetChanged();
    }

    private void generateNewAccessQR() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Inicia sesión nuevamente", Toast.LENGTH_SHORT).show();
            return;
        }

        buttonGenerateQR.setEnabled(false);
        Toast.makeText(this, "Generando nuevo código de acceso...", Toast.LENGTH_SHORT).show();

        String key = generateRandomKey(8);
        Date now = new Date();
        String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(now);
        String time = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(now);

        AccessRecord record = new AccessRecord(user.getUid(), user.getEmail(), key, date, time, userAddress);
        record.status = "GENERADO";
        record.timestamp = System.currentTimeMillis();

        db.collection("registro_de_entrada").add(record)
            .addOnSuccessListener(documentReference -> {
                buttonGenerateQR.setEnabled(true);
                Toast.makeText(AccessControlActivity.this, "¡Código generado con éxito!", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                buttonGenerateQR.setEnabled(true);
                Toast.makeText(AccessControlActivity.this, "Error al generar código: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("AccessControlActivity", "Error al crear registro de entrada: " + e.getMessage(), e);
            });
    }

    private void displayLatestQR(AccessRecord record) {
        String qrContent = "ACCESO_CONDO|UID:" + record.userId + "|DESTINO:" + (record.address != null ? record.address : "No especificado") + "|KEY:" + record.key;
        try {
            Bitmap qrBitmap = QRGenerator.INSTANCE.generateQRCode(qrContent);
            if (qrBitmap != null) {
                imageViewQR.setImageBitmap(qrBitmap);
                textViewQRStatus.setText("Estatus: " + record.status + " (" + record.time + ")");
                
                if ("AUTORIZADO".equals(record.status)) textViewQRStatus.setTextColor(Color.parseColor("#2E7D32"));
                else if ("COMPARTIDO".equals(record.status)) textViewQRStatus.setTextColor(Color.parseColor("#1976D2"));
                else textViewQRStatus.setTextColor(Color.GRAY);
            } else {
                Toast.makeText(this, "Error al generar la imagen del QR", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("AccessControlActivity", "Error en displayLatestQR: " + e.getMessage(), e);
            Toast.makeText(this, "Error en visualización: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String generateRandomKey(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) sb.append(chars.charAt(random.nextInt(chars.length())));
        return sb.toString();
    }

    private static class PairIdRecord {
        String id;
        AccessRecord record;
        long timestamp;
        PairIdRecord(String id, AccessRecord record) {
            this.id = id;
            this.record = record;
            this.timestamp = record.timestamp;
        }
    }
}
