package com.example.pia_claseordinaria.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pia_claseordinaria.data.CondoRepository
import com.example.pia_claseordinaria.models.Amenidad
import com.example.pia_claseordinaria.models.Factura
import com.example.pia_claseordinaria.models.EstadoFactura
import com.example.pia_claseordinaria.models.ServiceAppointment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class CondoViewModel(private val repository: CondoRepository = CondoRepository()) : ViewModel() {

    companion object {
        private val mockAmenidades = listOf(
            Amenidad("1", "Piscina Olímpica", "Piscina climatizada.", "PISCINA", "", true, 50.0),
            Amenidad("2", "Gimnasio Pro", "Equipamiento completo.", "GIMNASIO", "", true, 0.0),
            Amenidad("3", "Sala de Juegos", "Billar y consolas.", "SALA_JUEGOS", "", true, 20.0),
            Amenidad("4", "Salón de Eventos", "Espacio para fiestas.", "EVENTOS", "", true, 150.0)
        )
        private val listadoFacturasMemoria = mutableMapOf<String, Factura>()
    }

    private val _amenidades = MutableStateFlow<List<Amenidad>>(emptyList())
    val amenidades: StateFlow<List<Amenidad>> = _amenidades

    private val _facturas = MutableStateFlow<List<Factura>>(emptyList())
    val facturas: StateFlow<List<Factura>> = _facturas

    private val _serviceAppointments = MutableStateFlow<List<ServiceAppointment>>(emptyList())
    val serviceAppointments: StateFlow<List<ServiceAppointment>> = _serviceAppointments

    private val _assignedDates = MutableStateFlow<Set<String>>(emptySet())
    val assignedDates: StateFlow<Set<String>> = _assignedDates

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadAmenidades() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = repository.getAmenidades()
                _amenidades.value = if (result.isEmpty()) mockAmenidades else result
            } catch (e: Exception) {
                e.printStackTrace()
                _amenidades.value = mockAmenidades
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun isDateOccupied(amenidad: Amenidad, timestamp: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp }
        return amenidad.horariosOcupados.any { occupied ->
            val cal2 = Calendar.getInstance().apply { timeInMillis = occupied }
            cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
        }
    }

    fun loadFacturas(userId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val firebaseFacturas = if (userId.isNotEmpty()) repository.getFacturas(userId) else emptyList()
                val facturasUnicas = mutableMapOf<String, Factura>()
                
                firebaseFacturas.forEach { facturasUnicas[it.id] = it }
                listadoFacturasMemoria.values.filter { it.userId == userId || userId == "user_test_123" }
                    .forEach { facturasUnicas[it.id] = it }
                
                val resultadoFinal = facturasUnicas.values.sortedByDescending { it.fechaCreacion }
                _facturas.value = if (resultadoFinal.isEmpty() && userId == "user_test_123") {
                    listOf(Factura("f1", "Mantenimiento Mensual", 1200.0, System.currentTimeMillis(), System.currentTimeMillis(), EstadoFactura.POR_PAGAR, userId, ""))
                } else resultadoFinal
            } catch (e: Exception) {
                e.printStackTrace()
                _facturas.value = listadoFacturasMemoria.values.toList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAllFacturas() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = repository.getAllFacturas()
                
                val facturasConRecargos = result.map { factura ->
                    if (factura.estado == EstadoFactura.POR_PAGAR && 
                        System.currentTimeMillis() > factura.fechaVencimiento && 
                        factura.fechaVencimiento > 0) {
                        
                        val nuevoRecargo = factura.monto * 0.10
                        if (factura.recargoAplicado != nuevoRecargo) {
                            factura.copy(recargoAplicado = nuevoRecargo)
                        } else factura
                    } else factura
                }
                
                _facturas.value = facturasConRecargos
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun filterFacturas(userId: String?, startTimestamp: Long, endTimestamp: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = repository.filterFacturas(userId, startTimestamp, endTimestamp)
                _facturas.value = result
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun reservarAmenidad(amenidad: Amenidad, userId: String, fecha: Long, horas: Int, costoTotal: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            val idUnico = "res_${System.currentTimeMillis()}"
            
            try {
                repository.reservarAmenidad(idUnico, amenidad, userId, fecha, horas, costoTotal)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            if (costoTotal > 0) {
                val nuevaFactura = Factura(
                    id = idUnico,
                    concepto = "Reserva de ${amenidad.nombre}",
                    monto = costoTotal,
                    fechaVencimiento = System.currentTimeMillis() + 259200000,
                    fechaCreacion = System.currentTimeMillis(),
                    estado = EstadoFactura.POR_PAGAR,
                    userId = userId,
                    amenidadNombre = amenidad.nombre
                )
                listadoFacturasMemoria[idUnico] = nuevaFactura
            }
            
            loadFacturas(userId)
            loadAmenidades()
            _isLoading.value = false
        }
    }

    fun pagarFactura(facturaId: String, userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.actualizarEstadoFactura(facturaId, EstadoFactura.PAGADO)
                val factura = listadoFacturasMemoria[facturaId] ?: _facturas.value.find { it.id == facturaId }
                if (factura != null) {
                    listadoFacturasMemoria[facturaId] = factura.copy(estado = EstadoFactura.PAGADO)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            loadFacturas(userId)
            _isLoading.value = false
        }
    }

    fun sendFacturaToUser(email: String, concepto: String, monto: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            val userId = repository.findUserByEmail(email)
            if (userId != null) {
                val factura = Factura(
                    concepto = concepto,
                    monto = monto,
                    fechaVencimiento = System.currentTimeMillis() + 604800000,
                    estado = EstadoFactura.POR_PAGAR,
                    userId = userId
                )
                repository.crearFactura(factura)
            }
            _isLoading.value = false
        }
    }

    fun sendMaintenanceToAll(monto: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            val userIds = repository.getAllUserIds()
            userIds.forEach { uid ->
                val factura = Factura(
                    concepto = "Cuota Mantenimiento Mensual",
                    monto = monto,
                    fechaVencimiento = System.currentTimeMillis() + 604800000,
                    estado = EstadoFactura.POR_PAGAR,
                    userId = uid
                )
                repository.crearFactura(factura)
            }
            _isLoading.value = false
        }
    }

    // --- Citas de Servicio (Flujo Residente -> Admin -> Guardia) ---
    
    fun requestService(appointment: ServiceAppointment) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.createServiceAppointment(appointment)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAllServiceRequests() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val appointments = repository.getAllServiceAppointments()
                _serviceAppointments.value = appointments
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun approveAndChargeService(appointment: ServiceAppointment, price: Double, adminNotes: String) {
        viewModelScope.launch {
            _isLoading.value = true
            android.util.Log.d("CondoViewModel", "approveAndChargeService: Iniciando proceso para Cita ID: '${appointment.id}'")
            try {
                // 1. Actualizar la cita con el precio, notas y status ASSIGNED
                val updatedAppo = appointment.copy(
                    price = price,
                    adminNotes = adminNotes,
                    status = "ASSIGNED"
                )
                val updateSuccess = repository.updateServiceAppointment(updatedAppo)
                android.util.Log.d("CondoViewModel", "approveAndChargeService: updateServiceAppointment retorno = $updateSuccess")

                // 2. Generar la factura para el usuario
                val factura = Factura(
                    id = "svc_${System.currentTimeMillis()}",
                    concepto = "Servicio de ${appointment.category}",
                    monto = price,
                    fechaVencimiento = System.currentTimeMillis() + 604800000, // 1 semana para pagar
                    fechaCreacion = System.currentTimeMillis(),
                    estado = EstadoFactura.POR_PAGAR,
                    userId = appointment.userId,
                    amenidadNombre = "Servicio Técnico"
                )
                val facturaSuccess = repository.crearFactura(factura)
                android.util.Log.d("CondoViewModel", "approveAndChargeService: crearFactura retorno = $facturaSuccess")
                
                // 3. Recargar secuencialmente dentro del mismo coroutine para evitar condiciones de carrera
                android.util.Log.d("CondoViewModel", "approveAndChargeService: Recargando citas de servicio secuencialmente...")
                val appointments = repository.getAllServiceAppointments()
                _serviceAppointments.value = appointments
                android.util.Log.d("CondoViewModel", "approveAndChargeService: Citas recargadas. Cantidad = ${appointments.size}")
            } catch (e: Exception) {
                android.util.Log.e("CondoViewModel", "approveAndChargeService: Error en la corrutina: ${e.message}", e)
                e.printStackTrace()
            } finally {
                _isLoading.value = false
                android.util.Log.d("CondoViewModel", "approveAndChargeService: Finalizado proceso de aprobación")
            }
        }
    }

    fun loadServiceAppointments(date: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val appointments = repository.getServiceAppointments(date)
            // Filtramos solo las que están ASSIGNED para el guardia
            _serviceAppointments.value = appointments.filter { it.status == "ASSIGNED" }
            _isLoading.value = false
        }
    }

    fun loadAssignedDates() {
        viewModelScope.launch {
            try {
                val appointments = repository.getAllServiceAppointments()
                _assignedDates.value = appointments.filter { it.status == "ASSIGNED" }.map { it.date }.toSet()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
