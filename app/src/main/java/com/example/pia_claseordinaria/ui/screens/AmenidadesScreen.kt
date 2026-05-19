package com.example.pia_claseordinaria.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.pia_claseordinaria.models.Amenidad
import com.example.pia_claseordinaria.ui.components.AmenidadCard
import com.example.pia_claseordinaria.ui.viewmodels.CondoViewModel
import com.google.firebase.auth.FirebaseAuth
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmenidadesScreen(viewModel: CondoViewModel, onBack: () -> Unit) {
    val amenidades by viewModel.amenidades.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "user_test_123"

    var selectedAmenidad by remember { mutableStateOf<Amenidad?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadAmenidades()
    }

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text("Reservar Amenidades") },
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
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(amenidades) { amenidad ->
                    AmenidadCard(amenidad = amenidad) {
                        selectedAmenidad = amenidad
                        showDialog = true
                    }
                }
            }
        }

        if (showDialog && selectedAmenidad != null) {
            ReservationDialog(
                amenidad = selectedAmenidad!!,
                onDismiss = { showDialog = false },
                onConfirm = { fecha, horas ->
                    val costoTotal = selectedAmenidad!!.costoPorHora * horas
                    // CORRECCIÓN: Pasar el objeto Amenidad completo, no solo el ID
                    viewModel.reservarAmenidad(selectedAmenidad!!, userId, fecha, horas, costoTotal)
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun ReservationDialog(
    amenidad: Amenidad,
    onDismiss: () -> Unit,
    onConfirm: (Long, Int) -> Unit
) {
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var hours by remember { mutableIntStateOf(1) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reservar ${amenidad.nombre}") },
        text = {
            Column {
                Text("Costo por hora: $${amenidad.costoPorHora}")
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(onClick = {
                    val datePicker = DatePickerDialog(
                        context,
                        { _, y, m, d ->
                            val cal = Calendar.getInstance()
                            cal.set(y, m, d)
                            selectedDate = cal
                        },
                        selectedDate.get(Calendar.YEAR),
                        selectedDate.get(Calendar.MONTH),
                        selectedDate.get(Calendar.DAY_OF_MONTH)
                    )
                    datePicker.show()
                }) {
                    val dateText = "${selectedDate.get(Calendar.DAY_OF_MONTH)}/${selectedDate.get(Calendar.MONTH) + 1}/${selectedDate.get(Calendar.YEAR)}"
                    Text("Fecha: $dateText")
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Duración (Horas): $hours")
                Slider(
                    value = hours.toFloat(),
                    onValueChange = { hours = it.toInt() },
                    valueRange = 1f..5f,
                    steps = 3
                )
                
                val total = amenidad.costoPorHora * hours
                Text("Total a pagar: $$total", style = MaterialTheme.typography.titleMedium)
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedDate.timeInMillis, hours) }) {
                Text("Confirmar Reserva")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
