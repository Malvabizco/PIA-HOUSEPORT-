package com.example.pia_claseordinaria;

public class AccessRecord {
    public String userId, email, key, date, time, status, address;
    public long timestamp;

    public AccessRecord() {}

    public AccessRecord(String userId, String email, String key, String date, String time, String address) {
        this.userId = userId;
        this.email = email;
        this.key = key;
        this.date = date;
        this.time = time;
        this.address = address;
        this.status = "GENERADO";
        this.timestamp = System.currentTimeMillis();
    }
}
