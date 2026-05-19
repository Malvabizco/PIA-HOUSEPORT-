package com.example.pia_claseordinaria;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    private List<ChatMessage> messageList;
    private String currentUserId;

    public ChatAdapter(List<ChatMessage> messageList) {
        this.messageList = messageList;
        this.currentUserId = FirebaseAuth.getInstance().getUid();
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messageList.get(position);
        if (message.senderId.equals(currentUserId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item_sent, parent, false);
            return new SentMessageHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item_received, parent, false);
            return new ReceivedMessageHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);
        if (holder.getItemViewType() == VIEW_TYPE_SENT) {
            ((SentMessageHolder) holder).bind(message);
        } else {
            ((ReceivedMessageHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView textViewMessageBody, textViewMessageTime;
        SentMessageHolder(View itemView) {
            super(itemView);
            textViewMessageBody = itemView.findViewById(R.id.textViewMessageBody);
            textViewMessageTime = itemView.findViewById(R.id.textViewMessageTime);
        }
        void bind(ChatMessage message) {
            textViewMessageBody.setText(message.message);
            textViewMessageTime.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(message.timestamp)));
        }
    }

    static class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView textViewMessageBody, textViewMessageTime, textViewSenderName;
        ReceivedMessageHolder(View itemView) {
            super(itemView);
            textViewMessageBody = itemView.findViewById(R.id.textViewMessageBody);
            textViewMessageTime = itemView.findViewById(R.id.textViewMessageTime);
            textViewSenderName = itemView.findViewById(R.id.textViewSenderName);
        }
        void bind(ChatMessage message) {
            textViewMessageBody.setText(message.message);
            textViewSenderName.setText(message.senderName + " (" + message.role + ")");
            textViewMessageTime.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(message.timestamp)));
        }
    }
}