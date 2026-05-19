package com.example.pia_claseordinaria;

public class User {
    public String fullName, email, phone, address, status, role, condominio;

    public User() {}

    public User(String fullName, String email, String phone, String address, String condominio) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.condominio = condominio;
        this.status = "pending"; // Requiere aprobación de la administración
        this.role = "USER";
    }
}
