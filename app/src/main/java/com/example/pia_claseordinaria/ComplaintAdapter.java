package com.example.pia_claseordinaria;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ComplaintAdapter extends RecyclerView.Adapter<ComplaintAdapter.ViewHolder> {

    private List<Map<String, Object>> complaints;

    public ComplaintAdapter(List<Map<String, Object>> complaints) {
        this.complaints = complaints;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.complaint_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> complaint = complaints.get(position);
        holder.textViewUser.setText(String.valueOf(complaint.get("usuario")));
        holder.textViewMessage.setText(String.valueOf(complaint.get("mensaje")));
        
        Object timestamp = complaint.get("fecha");
        if (timestamp != null) {
            try {
                long timeMillis = (long) timestamp;
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                holder.textViewDate.setText(sdf.format(new Date(timeMillis)));
            } catch (Exception e) {
                holder.textViewDate.setText("");
            }
        } else {
            holder.textViewDate.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return complaints.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewUser, textViewMessage, textViewDate;
        public ViewHolder(View itemView) {
            super(itemView);
            textViewUser = itemView.findViewById(R.id.textViewUser);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            textViewDate = itemView.findViewById(R.id.textViewDate);
        }
    }
}