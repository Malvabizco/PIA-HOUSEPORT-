package com.example.pia_claseordinaria;

public class ChatMessage {
    public String senderId, senderName, message, role;
    public long timestamp;

    public ChatMessage() {}

    public ChatMessage(String senderId, String senderName, String message, String role) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.message = message;
        this.role = role;
        this.timestamp = System.currentTimeMillis();
    }
}