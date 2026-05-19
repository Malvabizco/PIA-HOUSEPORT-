package com.example.pia_claseordinaria.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pia_claseordinaria.models.ServiceAppointment
import com.example.pia_claseordinaria.ui.viewmodels.CondoViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuardAppointmentsScreen(viewModel: CondoViewModel, onBack: () -> Unit) {
    var selectedDate by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    val appointments by viewModel.serviceAppointments.collectAsState()
    val assignedDates by viewModel.assignedDates.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var selectedAppointment by remember { mutableStateOf<ServiceAppointment?>(null) }
    var showCalendar by remember { mutableStateOf(false) }

    LaunchedEffect(selectedDate) {
        viewModel.loadServiceAppointments(selectedDate)
        viewModel.loadAssignedDates()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Control de Servicios", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { showCalendar = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Calendario")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            DateSelector(selectedDate) { selectedDate = it }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (appointments.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay servicios programados para esta fecha.", color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    items(appointments) { appointment ->
                        AppointmentItem(appointment) {
                            selectedAppointment = appointment
                        }
                    }
                }
            }
        }

        if (selectedAppointment != null) {
            AlertDialog(
                onDismissRequest = { selectedAppointment = null },
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Business, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verificación de Entrada", fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DetailRow("Empresa/Proveedor", selectedAppointment!!.companyName)
                        DetailRow("Servicio", selectedAppointment!!.category)
                        DetailRow("Descripción", selectedAppointment!!.userDescription)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        DetailRow("Dirigirse a", selectedAppointment!!.destinationAddress.uppercase(), isHighlight = true)
                        DetailRow("Hora Aprox.", selectedAppointment!!.time)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        DetailRow("Placas Vehículo", selectedAppointment!!.vehiclePlates, isHighlight = true)
                        DetailRow("Cant. Personas", selectedAppointment!!.visitorCount.toString())
                        DetailRow("Nombres", selectedAppointment!!.visitorNames.joinToString(", "))
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { selectedAppointment = null },
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("AUTORIZAR LECTURA") }
                },
                dismissButton = {
                    TextButton(onClick = { selectedAppointment = null }) { Text("CERRAR") }
                }
            )
        }

        if (showCalendar) {
            InteractiveCalendarDialog(
                assignedDates = assignedDates,
                currentSelectedDate = selectedDate,
                onDateSelected = { selectedDate = it },
                onDismissRequest = { showCalendar = false }
            )
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, isHighlight: Boolean = false) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(
            text = if (value.isEmpty()) "No especificado" else value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal,
            color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun DateSelector(selectedDate: String, onDateSelected: (String) -> Unit) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val today = Calendar.getInstance()
        val dates = listOf(
            Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 0) },
            Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) },
            Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 2) }
        )

        dates.forEachIndexed { index, cal ->
            val dateStr = sdf.format(cal.time)
            val isSelected = dateStr == selectedDate
            
            FilterChip(
                modifier = Modifier.weight(1f),
                selected = isSelected,
                onClick = { onDateSelected(dateStr) },
                label = { 
                    Text(
                        when(index) {
                            0 -> "Hoy"
                            1 -> "Mañana"
                            else -> SimpleDateFormat("dd/MM", Locale.getDefault()).format(cal.time)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    ) 
                }
            )
        }
    }
}

@Composable
fun AppointmentItem(appointment: ServiceAppointment, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { onClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp), 
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.LocationOn, 
                    contentDescription = null, 
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(appointment.companyName.ifEmpty { "Proveedor Externo" }, fontWeight = FontWeight.Bold)
                Text("${appointment.category} - ${appointment.destinationAddress}", style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(appointment.time, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text("Placas: ${appointment.vehiclePlates}", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun InteractiveCalendarDialog(
    assignedDates: Set<String>,
    currentSelectedDate: String,
    onDateSelected: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val monthSdf = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
    
    var currentMonthCal by remember { 
        mutableStateOf(Calendar.getInstance().apply { 
            val date = try { sdf.parse(currentSelectedDate) } catch (e: Exception) { null } ?: Date()
            time = date
            set(Calendar.DAY_OF_MONTH, 1) 
        }) 
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onDismissRequest) { Text("CERRAR") }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { 
                        currentMonthCal = (currentMonthCal.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Mes anterior")
                    }
                    Text(monthSdf.format(currentMonthCal.time).replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { 
                        currentMonthCal = (currentMonthCal.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Mes siguiente")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    listOf("D", "L", "M", "M", "J", "V", "S").forEach {
                        Text(it, fontWeight = FontWeight.Bold, color = Color.Gray)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                val daysInMonth = currentMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                val firstDayOfWeek = currentMonthCal.get(Calendar.DAY_OF_WEEK)
                
                var dayCounter = 1
                Column {
                    for (row in 0..5) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            for (col in 1..7) {
                                if (row == 0 && col < firstDayOfWeek || dayCounter > daysInMonth) {
                                    Box(modifier = Modifier.size(40.dp))
                                } else {
                                    val currentDay = dayCounter
                                    val calForDay = (currentMonthCal.clone() as Calendar).apply {
                                        set(Calendar.DAY_OF_MONTH, currentDay)
                                    }
                                    val dateStr = sdf.format(calForDay.time)
                                    val hasVisit = assignedDates.contains(dateStr)
                                    val isSelected = dateStr == currentSelectedDate
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                onDateSelected(dateStr)
                                                onDismissRequest()
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = currentDay.toString(),
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                                fontWeight = if (isSelected || hasVisit) FontWeight.Bold else FontWeight.Normal
                                            )
                                            if (hasVisit) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(50))
                                                )
                                            }
                                        }
                                    }
                                    dayCounter++
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}
