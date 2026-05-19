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

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.ViewHolder> {

    private List<Map<String, Object>> announcements;

    public AnnouncementAdapter(List<Map<String, Object>> announcements) {
        this.announcements = announcements;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.announcement_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> announcement = announcements.get(position);
        holder.textViewTitle.setText(String.valueOf(announcement.get("titulo")));
        holder.textViewMessage.setText(String.valueOf(announcement.get("mensaje")));
        
        Object timestamp = announcement.get("fecha");
        if (timestamp instanceof Long) {
            String date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date((Long) timestamp));
            holder.textViewDate.setText(date);
        }
    }

    @Override
    public int getItemCount() {
        return announcements.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle, textViewDate, textViewMessage;
        public ViewHolder(View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewAnnouncementTitle);
            textViewDate = itemView.findViewById(R.id.textViewAnnouncementDate);
            textViewMessage = itemView.findViewById(R.id.textViewAnnouncementMessage);
        }
    }
}
