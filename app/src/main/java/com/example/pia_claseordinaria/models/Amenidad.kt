package com.example.pia_claseordinaria.models

data class Amenidad(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val tipo: String = "OTRO", // PISCINA, GIMNASIO, SALA_JUEGOS, EVENTOS, etc.
    val imagenUrl: String = "",
    val disponible: Boolean = true,
    val costoPorHora: Double = 0.0,
    val horariosOcupados: List<Long> = emptyList() // Timestamps de reservaciones existentes
)
