package com.example.pia_claseordinaria.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pia_claseordinaria.models.EstadoFactura
import com.example.pia_claseordinaria.models.Factura
import com.example.pia_claseordinaria.ui.viewmodels.CondoViewModel
import com.example.pia_claseordinaria.utils.PdfGenerator
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStatsScreen(viewModel: CondoViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val facturas by viewModel.facturas.collectAsState()
    var showFilterDialog by remember { mutableStateOf(false) }
    
    var filterUserId by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf<EstadoFactura?>(null) }
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }

    val filteredFacturas = remember(facturas, filterStatus) {
        if (filterStatus == null) facturas else facturas.filter { it.estado == filterStatus }
    }

    val totalPagado = facturas.filter { it.estado == EstadoFactura.PAGADO }.sumOf { it.monto }
    val totalPendiente = facturas.filter { it.estado == EstadoFactura.POR_PAGAR }.sumOf { it.monto }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("HousePort Finanzas", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { PdfGenerator.generateAndShareFinanzasPdf(context, filteredFacturas) }) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.Tune, contentDescription = "Filtrar")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        if (showFilterDialog) {
            FilterDialog(
                currentUserId = filterUserId,
                currentStatus = filterStatus,
                currentStart = startDate,
                currentEnd = endDate,
                onDismiss = { showFilterDialog = false },
                onApply = { userId, status, start, end ->
                    filterUserId = userId
                    filterStatus = status
                    startDate = start
                    endDate = end
                    viewModel.filterFacturas(
                        userId = userId.ifEmpty { null },
                        startTimestamp = start ?: 0L,
                        endTimestamp = end ?: System.currentTimeMillis()
                    )
                    showFilterDialog = false
                }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                BalanceCard(totalPagado, totalPendiente)
            }

            item {
                Button(
                    onClick = { viewModel.sendMaintenanceToAll(1200.0) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Groups, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generar Cuota Mantenimiento a Todos ($1200)")
                }
            }

            item {
                ChartsCard(totalPagado, totalPendiente)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        if (filterStatus != null) "Transacciones: ${filterStatus}" else "Todas las Transacciones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (filterUserId.isNotEmpty() || startDate != null || filterStatus != null) {
                        TextButton(onClick = {
                            filterUserId = ""
                            filterStatus = null
                            startDate = null
                            endDate = null
                            viewModel.loadAllFacturas()
                        }) {
                            Text("Limpiar filtros", fontSize = 12.sp)
                        }
                    }
                }
            }

            items(filteredFacturas) { factura ->
                TransactionItem(factura)
            }
        }
    }
}

@Composable
fun BalanceCard(pagado: Double, pendiente: Double) {
    val total = pagado + pendiente
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.surfaceVariant)
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Text("Balance Total Registrado", style = MaterialTheme.typography.labelLarge)
                Text(
                    "$${String.format("%.2f", total)}",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    FinanceSummaryMiniCard(
                        label = "Ingresos (Pagado)",
                        amount = pagado,
                        color = Color(0xFF4CAF50),
                        icon = Icons.Default.ArrowUpward
                    )
                    FinanceSummaryMiniCard(
                        label = "Pendientes (Por Cobrar)",
                        amount = pendiente,
                        color = Color(0xFFF44336),
                        icon = Icons.Default.ArrowDownward
                    )
                }
            }
        }
    }
}

@Composable
fun ChartsCard(pagado: Double, pendiente: Double) {
    val maxMonto = (pagado + pendiente).coerceAtLeast(1.0).toFloat()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Distribución de Ingresos",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                BarComponent(
                    monto = pagado.toFloat(),
                    maxMonto = maxMonto,
                    label = "Pagado",
                    color = Color(0xFF4CAF50)
                )
                BarComponent(
                    monto = pendiente.toFloat(),
                    maxMonto = maxMonto,
                    label = "Pendiente",
                    color = Color(0xFFF44336)
                )
            }
        }
    }
}

@Composable
fun BarComponent(monto: Float, maxMonto: Float, label: String, color: Color) {
    val heightScale by animateFloatAsState(
        targetValue = if (maxMonto > 0) (monto / maxMonto) else 0f,
        animationSpec = tween(durationMillis = 1000), label = ""
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .height(100.dp * heightScale)
                .width(45.dp)
                .background(color, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun FinanceSummaryMiniCard(label: String, amount: Double, color: Color, icon: ImageVector) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = color.copy(alpha = 0.15f),
                modifier = Modifier.size(24.dp)
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.padding(4.dp).size(16.dp), tint = color)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            "$${String.format("%.2f", amount)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun TransactionItem(factura: Factura) {
    val isPagado = factura.estado == EstadoFactura.PAGADO
    val isVencido = !isPagado && System.currentTimeMillis() > factura.fechaVencimiento && factura.fechaVencimiento > 0
    val statusColor = when {
        isPagado -> Color(0xFF4CAF50)
        isVencido -> Color(0xFFBA1A1A) 
        else -> Color(0xFFF44336)
    }
    val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = statusColor.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = when {
                        isPagado -> Icons.Default.CheckCircle
                        isVencido -> Icons.Default.Warning
                        else -> Icons.Default.PendingActions
                    },
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.padding(12.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = factura.concepto,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = "ID: ${factura.userId.ifEmpty { "N/A" }} • ${sdf.format(Date(factura.fechaCreacion))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                val montoTotal = factura.monto + factura.recargoAplicado
                Text(
                    text = "$${String.format("%.2f", montoTotal)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = statusColor
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = statusColor.copy(alpha = 0.1f),
                ) {
                    Text(
                        text = when {
                            isPagado -> "PAGADO"
                            isVencido -> "MOROSO"
                            else -> "PENDIENTE"
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    currentUserId: String,
    currentStatus: EstadoFactura?,
    currentStart: Long?,
    currentEnd: Long?,
    onDismiss: () -> Unit,
    onApply: (String, EstadoFactura?, Long?, Long?) -> Unit
) {
    var userId by remember { mutableStateOf(currentUserId) }
    var status by remember { mutableStateOf(currentStatus) }
    var start by remember { mutableStateOf(currentStart) }
    var end by remember { mutableStateOf(currentEnd) }
    
    val datePickerStateStart = rememberDatePickerState(initialSelectedDateMillis = start)
    val datePickerStateEnd = rememberDatePickerState(initialSelectedDateMillis = end)
    
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { onApply(userId, status, start, end) },
                shape = RoundedCornerShape(12.dp)
            ) { Text("Aplicar Filtros") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        title = { Text("Filtrar Finanzas", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = userId,
                    onValueChange = { userId = it },
                    label = { Text("Filtrar por ID Usuario / Condomino") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Estado de Pago:", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = status == null,
                        onClick = { status = null },
                        label = { Text("Todos") }
                    )
                    FilterChip(
                        selected = status == EstadoFactura.PAGADO,
                        onClick = { status = EstadoFactura.PAGADO },
                        label = { Text("Pagados") }
                    )
                    FilterChip(
                        selected = status == EstadoFactura.POR_PAGAR,
                        onClick = { status = EstadoFactura.POR_PAGAR },
                        label = { Text("Pendientes") }
                    )
                }

                OutlinedTextField(
                    value = start?.let { sdf.format(Date(it)) } ?: "Desde (Fecha)",
                    onValueChange = {},
                    label = { Text("Fecha Inicio") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().clickable { showStartPicker = true },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = end?.let { sdf.format(Date(it)) } ?: "Hasta (Fecha)",
                    onValueChange = {},
                    label = { Text("Fecha Fin") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().clickable { showEndPicker = true },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    )

    if (showStartPicker) {
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    start = datePickerStateStart.selectedDateMillis
                    showStartPicker = false
                }) { Text("OK") }
            }
        ) { DatePicker(state = datePickerStateStart) }
    }

    if (showEndPicker) {
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    end = datePickerStateEnd.selectedDateMillis
                    showEndPicker = false
                }) { Text("OK") }
            }
        ) { DatePicker(state = datePickerStateEnd) }
    }
}
