package com.example.pia_claseordinaria.models

data class Departamento(
    val id: String = "",
    val numero: String = "",
    val torreSeccion: String = "",
    val duenoId: String = "",
    val estatus: String = "vacio" // habitado, vacio
)
