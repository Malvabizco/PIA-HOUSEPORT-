package com.example.pia_claseordinaria.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pia_claseordinaria.models.ServiceAppointment
import com.example.pia_claseordinaria.ui.viewmodels.CondoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminServiceScreen(viewModel: CondoViewModel, onBack: () -> Unit) {
    val requests by viewModel.serviceAppointments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var selectedRequest by remember { mutableStateOf<ServiceAppointment?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadAllServiceRequests()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Gestión de Servicios", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (requests.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                    Text("No hay solicitudes de servicio", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Primero PENDING, luego el resto
                val sorted = requests.sortedWith(compareBy { if (it.status == "PENDING") 0 else 1 })
                items(sorted) { request ->
                    ServiceRequestCard(request) {
                        selectedRequest = request
                    }
                }
            }
        }

        if (selectedRequest != null) {
            ProcessServiceDialog(
                request = selectedRequest!!,
                onDismiss = { selectedRequest = null },
                onConfirm = { price, notes, company, visitorCount, visitorNames, plates ->
                    viewModel.approveAndChargeService(
                        selectedRequest!!.copy(
                            companyName = company,
                            visitorCount = visitorCount,
                            visitorNames = visitorNames,
                            vehiclePlates = plates
                        ),
                        price,
                        notes
                    )
                    selectedRequest = null
                    // La recarga se maneja de forma segura y secuencial dentro del ViewModel
                }
            )
        }
    }
}

@Composable
fun ServiceRequestCard(request: ServiceAppointment, onClick: () -> Unit) {
    val isPending = request.status == "PENDING"
    val statusColor = when (request.status) {
        "PENDING"  -> Color(0xFFFF8F00)
        "ASSIGNED" -> Color(0xFF2E7D32)
        else       -> Color.Gray
    }
    val bgColor = when (request.status) {
        "PENDING"  -> Color(0xFFFFF8E1)
        "ASSIGNED" -> Color(0xFFE8F5E9)
        else       -> Color(0xFFF5F5F5)
    }

    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = bgColor,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (isPending) Icons.Default.HourglassTop else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(request.category, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Depto: ${request.destinationAddress}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                if (request.userDescription.isNotEmpty()) {
                    Text(request.userDescription, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                }
                Text("Fecha solicitada: ${request.date}  ${request.time}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                color = bgColor,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isPending) "PENDIENTE" else "ASIGNADO",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcessServiceDialog(
    request: ServiceAppointment,
    onDismiss: () -> Unit,
    onConfirm: (Double, String, String, Int, List<String>, String) -> Unit
) {
    var price by remember { mutableStateOf(if (request.price > 0) request.price.toString() else "") }
    var adminNotes by remember { mutableStateOf(request.adminNotes) }
    var companyName by remember { mutableStateOf(request.companyName) }
    var visitorCountStr by remember { mutableStateOf(if (request.visitorCount > 0) request.visitorCount.toString() else "1") }
    var visitorNames = remember { mutableStateListOf<String>().also { list ->
        list.addAll(request.visitorNames)
    }}
    var plates by remember { mutableStateOf(request.vehiclePlates) }

    val visitorCount = visitorCountStr.toIntOrNull() ?: 0

    LaunchedEffect(visitorCount) {
        if (visitorCount > visitorNames.size) {
            repeat(visitorCount - visitorNames.size) { visitorNames.add("") }
        } else if (visitorCount < visitorNames.size && visitorCount >= 0) {
            while (visitorNames.size > visitorCount) { visitorNames.removeAt(visitorNames.size - 1) }
        }
    }

    val isAlreadyAssigned = request.status == "ASSIGNED"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    if (isAlreadyAssigned) "Editar Servicio Asignado" else "Procesar Solicitud",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    request.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                item {
                    // Solicitud del residente (solo lectura)
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Solicitud del residente:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(request.userDescription.ifEmpty { "Sin descripción" }, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                item {
                    OutlinedTextField(
                        value = companyName,
                        onValueChange = { companyName = it },
                        label = { Text("Empresa / Proveedor contratado") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) price = it },
                        label = { Text("Costo del servicio") },
                        modifier = Modifier.fillMaxWidth(),
                        prefix = { Text("\$") }
                    )
                }
                item {
                    OutlinedTextField(
                        value = adminNotes,
                        onValueChange = { adminNotes = it },
                        label = { Text("Notas para el residente") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = visitorCountStr,
                        onValueChange = { if (it.all { c -> c.isDigit() }) visitorCountStr = it },
                        label = { Text("Número de técnicos / personas") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                items(visitorCount) { i ->
                    OutlinedTextField(
                        value = if (i < visitorNames.size) visitorNames[i] else "",
                        onValueChange = { if (i < visitorNames.size) visitorNames[i] = it },
                        label = { Text("Nombre del Técnico ${i + 1}") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = plates,
                        onValueChange = { plates = it.uppercase() },
                        label = { Text("Placas del vehículo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        price.toDoubleOrNull() ?: 0.0,
                        adminNotes,
                        companyName,
                        visitorCount,
                        visitorNames.toList(),
                        plates
                    )
                },
                enabled = companyName.isNotEmpty()
            ) {
                Text(if (isAlreadyAssigned) "ACTUALIZAR" else "APROBAR Y COBRAR")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCELAR") }
        }
    )
}
