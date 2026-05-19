package com.example.pia_claseordinaria;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AccessAdapter extends RecyclerView.Adapter<AccessAdapter.AccessViewHolder> {

    private List<AccessRecord> accessList;

    public AccessAdapter(List<AccessRecord> accessList) {
        this.accessList = accessList;
    }

    @NonNull
    @Override
    public AccessViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.access_item, parent, false);
        return new AccessViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccessViewHolder holder, int position) {
        AccessRecord record = accessList.get(position);
        holder.textViewDateTime.setText(record.date + " - " + record.time);
        holder.textViewKey.setText("Clave: " + record.key);
        holder.textViewStatus.setText(record.status);

        // Cambiar colores según el estado
        if ("AUTORIZADO".equals(record.status)) {
            holder.textViewStatus.setTextColor(Color.parseColor("#2E7D32")); // Verde
            holder.textViewStatus.setBackgroundColor(Color.parseColor("#E8F5E9"));
            holder.iconStatus.setImageResource(android.R.drawable.presence_online);
        } else if ("CADUCADO".equals(record.status)) {
            holder.textViewStatus.setTextColor(Color.parseColor("#C62828")); // Rojo
            holder.textViewStatus.setBackgroundColor(Color.parseColor("#FFEBEE"));
            holder.iconStatus.setImageResource(android.R.drawable.presence_busy);
        } else {
            holder.textViewStatus.setTextColor(Color.parseColor("#1976D2")); // Azul (GENERADO)
            holder.textViewStatus.setBackgroundColor(Color.parseColor("#E3F2FD"));
            holder.iconStatus.setImageResource(android.R.drawable.ic_menu_recent_history);
        }
    }

    @Override
    public int getItemCount() {
        return accessList.size();
    }

    public static class AccessViewHolder extends RecyclerView.ViewHolder {
        TextView textViewDateTime, textViewKey, textViewStatus;
        ImageView iconStatus;

        public AccessViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDateTime = itemView.findViewById(R.id.historyDateTime);
            textViewKey = itemView.findViewById(R.id.historyKey);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            iconStatus = itemView.findViewById(R.id.iconStatus);
        }
    }
}
