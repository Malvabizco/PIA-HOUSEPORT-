package com.example.pia_claseordinaria;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Map;

public class ScannedAccessAdapter extends RecyclerView.Adapter<ScannedAccessAdapter.ViewHolder> {

    private List<Map<String, String>> scannedList;

    public ScannedAccessAdapter(List<Map<String, String>> scannedList) {
        this.scannedList = scannedList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.scan_result_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, String> data = scannedList.get(position);
        holder.textViewScanName.setText("Nombre: " + data.get("name"));
        holder.textViewScanEmail.setText("Correo: " + data.get("email"));
        holder.textViewScanAddress.setText("Dirección: " + data.get("address"));
        holder.textViewScanPhone.setText("Teléfono: " + data.get("phone"));
        holder.textViewScanTime.setText("Fecha y hora: " + data.get("time"));
    }

    @Override
    public int getItemCount() {
        return scannedList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewScanName, textViewScanEmail, textViewScanAddress, textViewScanPhone, textViewScanTime;
        public ViewHolder(View itemView) {
            super(itemView);
            textViewScanName = itemView.findViewById(R.id.textViewScanName);
            textViewScanEmail = itemView.findViewById(R.id.textViewScanEmail);
            textViewScanAddress = itemView.findViewById(R.id.textViewScanAddress);
            textViewScanPhone = itemView.findViewById(R.id.textViewScanPhone);
            textViewScanTime = itemView.findViewById(R.id.textViewScanTime);
        }
    }
}