package com.example.pia_claseordinaria.models

data class ServiceAppointment(
    val id: String = "",
    val userId: String = "",
    val category: String = "",
    val userDescription: String = "", // Lo que el usuario pide
    val adminNotes: String = "", // Notas del admin sobre el proveedor
    val companyName: String = "",
    val visitorNames: List<String> = emptyList(), // Lista dinámica de nombres
    val visitorCount: Int = 0,
    val vehiclePlates: String = "",
    val destinationAddress: String = "",
    val date: String = "",
    val time: String = "",
    val price: Double = 0.0,
    val status: String = "PENDING", // PENDING, ASSIGNED, COMPLETED, CANCELLED
    val timestamp: Long = System.currentTimeMillis()
)
