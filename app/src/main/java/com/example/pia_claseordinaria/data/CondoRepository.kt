package com.example.pia_claseordinaria.data

import com.example.pia_claseordinaria.models.Amenidad
import com.example.pia_claseordinaria.models.Factura
import com.example.pia_claseordinaria.models.EstadoFactura
import com.example.pia_claseordinaria.models.ServiceAppointment
import com.example.pia_claseordinaria.models.Departamento
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class CondoRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getAmenidades(): List<Amenidad> {
        return try {
            db.collection("amenidades").get().await().toObjects(Amenidad::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getFacturas(userId: String): List<Factura> {
        if (userId.isEmpty()) return emptyList()
        return try {
            db.collection("facturas")
                .whereEqualTo("userId", userId)
                .get()
                .await()
                .documents.mapNotNull { doc ->
                    doc.toObject(Factura::class.java)?.copy(id = doc.id)
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAllFacturas(): List<Factura> {
        return try {
            db.collection("facturas")
                .get()
                .await()
                .documents.mapNotNull { doc ->
                    doc.toObject(Factura::class.java)?.copy(id = doc.id)
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun filterFacturas(userId: String?, startTimestamp: Long, endTimestamp: Long): List<Factura> {
        return try {
            var query: Query = db.collection("facturas")
                .whereGreaterThanOrEqualTo("fechaCreacion", startTimestamp)
                .whereLessThanOrEqualTo("fechaCreacion", endTimestamp)

            if (!userId.isNullOrEmpty()) {
                query = query.whereEqualTo("userId", userId)
            }

            query.get().await().documents.mapNotNull { doc ->
                doc.toObject(Factura::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun reservarAmenidad(facturaId: String, amenidad: Amenidad, userId: String, fecha: Long, horas: Int, costoTotal: Double): Boolean {
        return try {
            val reservation = mapOf(
                "amenidadId" to amenidad.id,
                "amenidadNombre" to amenidad.nombre,
                "userId" to userId,
                "fecha" to fecha,
                "horas" to horas,
                "costoTotal" to costoTotal,
                "timestamp" to System.currentTimeMillis()
            )
            db.collection("reservaciones").add(reservation).await()
            
            if (costoTotal > 0) {
                val nuevaFactura = Factura(
                    id = facturaId,
                    concepto = "Reserva de ${amenidad.nombre}",
                    monto = costoTotal,
                    fechaVencimiento = System.currentTimeMillis() + (86400000 * 3),
                    fechaCreacion = System.currentTimeMillis(),
                    estado = EstadoFactura.POR_PAGAR,
                    userId = userId,
                    amenidadNombre = amenidad.nombre
                )
                db.collection("facturas").document(facturaId).set(nuevaFactura).await()
            }
            
            db.collection("amenidades").document(amenidad.id)
                .update("horariosOcupados", com.google.firebase.firestore.FieldValue.arrayUnion(fecha))
                .await()

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun actualizarEstadoFactura(facturaId: String, nuevoEstado: EstadoFactura): Boolean {
        return try {
            db.collection("facturas").document(facturaId)
                .update("estado", nuevoEstado)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // --- Gestión de Usuarios para Facturación ---
    suspend fun findUserByEmail(email: String): String? {
        return try {
            val result = db.collection("usuarios")
                .whereEqualTo("email", email)
                .get()
                .await()
            if (!result.isEmpty) result.documents[0].id else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAllUserIds(): List<String> {
        return try {
            db.collection("usuarios")
                .get()
                .await()
                .documents.map { it.id }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAllUsers(): List<Pair<String, String>> {
        return try {
            db.collection("usuarios")
                .get()
                .await()
                .documents.map { doc ->
                    val name = doc.getString("fullName") ?: "Sin nombre"
                    val email = doc.getString("email") ?: ""
                    Pair(doc.id, "$name ($email)")
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun crearFactura(factura: Factura): Boolean {
        return try {
            if (factura.id.isEmpty()) {
                db.collection("facturas").add(factura).await()
            } else {
                db.collection("facturas").document(factura.id).set(factura).await()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    // --- Citas de Servicio ---
    suspend fun createServiceAppointment(appointment: ServiceAppointment): Boolean {
        android.util.Log.d("CondoRepository", "createServiceAppointment: Iniciando creación...")
        return try {
            val docRef = db.collection("citas_servicio").document()
            val finalAppointment = appointment.copy(id = docRef.id)
            docRef.set(finalAppointment).await()
            android.util.Log.d("CondoRepository", "createServiceAppointment: Cita creada con éxito con ID: ${docRef.id}")
            true
        } catch (e: Exception) {
            android.util.Log.e("CondoRepository", "createServiceAppointment: Error creando cita: ${e.message}", e)
            false
        }
    }

    suspend fun updateServiceAppointment(appointment: ServiceAppointment): Boolean {
        android.util.Log.d("CondoRepository", "updateServiceAppointment: Intentando actualizar cita ID: '${appointment.id}', Status: '${appointment.status}'")
        return try {
            if (appointment.id.isNotEmpty()) {
                db.collection("citas_servicio").document(appointment.id).set(appointment).await()
                android.util.Log.d("CondoRepository", "updateServiceAppointment: Cita ID '${appointment.id}' actualizada exitosamente")
                true
            } else {
                android.util.Log.e("CondoRepository", "updateServiceAppointment: Falló la actualización porque el ID está vacío")
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("CondoRepository", "updateServiceAppointment: Excepción actualizando cita ID '${appointment.id}': ${e.message}", e)
            false
        }
    }

    suspend fun getServiceAppointments(date: String): List<ServiceAppointment> {
        return try {
            db.collection("citas_servicio")
                .whereEqualTo("date", date)
                .get()
                .await()
                .documents.mapNotNull { doc ->
                    doc.toObject(ServiceAppointment::class.java)?.copy(id = doc.id)
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAllServiceAppointments(): List<ServiceAppointment> {
        return try {
            db.collection("citas_servicio")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
                .documents.mapNotNull { doc ->
                    doc.toObject(ServiceAppointment::class.java)?.copy(id = doc.id)
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- CRUD Departamentos ---
    suspend fun createDepartamento(depto: Departamento): Boolean {
        return try {
            val data = mapOf(
                "numero" to depto.numero,
                "torreSeccion" to depto.torreSeccion,
                "duenoId" to depto.duenoId,
                "estatus" to depto.estatus
            )
            db.collection("departamentos").add(data).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getDepartamentos(): List<Departamento> {
        return try {
            db.collection("departamentos").get().await().documents.mapNotNull { doc ->
                doc.toObject(Departamento::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateDepartamento(depto: Departamento): Boolean {
        return try {
            if (depto.id.isNotEmpty()) {
                val data = mapOf(
                    "numero" to depto.numero,
                    "torreSeccion" to depto.torreSeccion,
                    "duenoId" to depto.duenoId,
                    "estatus" to depto.estatus
                )
                db.collection("departamentos").document(depto.id).set(data).await()
                true
            } else false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteDepartamento(deptoId: String): Boolean {
        return try {
            db.collection("departamentos").document(deptoId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
