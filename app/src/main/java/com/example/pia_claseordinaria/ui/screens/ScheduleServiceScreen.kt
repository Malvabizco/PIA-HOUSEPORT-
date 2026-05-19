package com.example.pia_claseordinaria.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.pia_claseordinaria.models.ServiceAppointment
import com.example.pia_claseordinaria.ui.viewmodels.CondoViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleServiceScreen(viewModel: CondoViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    var category by remember { mutableStateOf("ELECTRICISTA") }
    var description by remember { mutableStateOf("") }
    var destinationAddress by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var time by remember { mutableStateOf("12:00 PM") }

    LaunchedEffect(Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            FirebaseFirestore.getInstance().collection("usuarios").document(user.uid)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        destinationAddress = doc.getString("address") ?: ""
                    }
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Solicitar Servicio", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("¿Qué necesitas?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            var expanded by remember { mutableStateOf(false) }
            val categories = listOf("ELECTRICISTA", "PLOMERO", "JARDINERÍA", "LIMPIEZA", "FUMIGACIÓN", "OTRO")

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría del servicio") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    categories.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = { category = selectionOption; expanded = false }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Describe el problema o servicio") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            HorizontalDivider()
            Text("¿Cuándo lo necesitas?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = date,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fecha") },
                    modifier = Modifier.weight(1f),
                    trailingIcon = {
                        IconButton(onClick = {
                            val calendar = Calendar.getInstance()
                            DatePickerDialog(context, { _, y, m, d ->
                                val cal = Calendar.getInstance()
                                cal.set(y, m, d)
                                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                        }) { Icon(Icons.Default.CalendarToday, contentDescription = null) }
                    }
                )
                OutlinedTextField(
                    value = time,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Hora aprox.") },
                    modifier = Modifier.weight(1f),
                    trailingIcon = {
                        IconButton(onClick = {
                            val calendar = Calendar.getInstance()
                            TimePickerDialog(context, { _, h, min ->
                                val cal = Calendar.getInstance()
                                cal.set(Calendar.HOUR_OF_DAY, h)
                                cal.set(Calendar.MINUTE, min)
                                time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.time)
                            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
                        }) { Icon(Icons.Default.Schedule, contentDescription = null) }
                    }
                )
            }

            Button(
                onClick = {
                    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown"
                    val appointment = ServiceAppointment(
                        userId = currentUserId,
                        category = category,
                        userDescription = description,
                        companyName = "",
                        visitorNames = emptyList(),
                        visitorCount = 0,
                        vehiclePlates = "",
                        destinationAddress = destinationAddress,
                        date = date,
                        time = time,
                        status = "PENDING"
                    )
                    viewModel.requestService(appointment)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = description.isNotBlank()
            ) {
                Text("ENVIAR SOLICITUD", modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
            }

            Text(
                "El administrador revisará tu solicitud, asignará un proveedor y te informará el costo antes de que ingrese al condominio.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
