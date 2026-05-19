package com.example.pia_claseordinaria.models

enum class EstadoFactura {
    PAGADO,
    POR_PAGAR
}

data class Factura(
    val id: String = "",
    val concepto: String = "",
    val monto: Double = 0.0,
    val fechaVencimiento: Long = 0,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val estado: EstadoFactura = EstadoFactura.POR_PAGAR,
    val userId: String = "",
    val amenidadNombre: String = "",
    val recargoAplicado: Double = 0.0 // Nuevo campo para registrar el recargo
)
