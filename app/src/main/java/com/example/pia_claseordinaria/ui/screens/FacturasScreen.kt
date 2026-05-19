package com.example.pia_claseordinaria.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.pia_claseordinaria.models.Factura
import com.example.pia_claseordinaria.models.EstadoFactura
import com.example.pia_claseordinaria.ui.viewmodels.CondoViewModel
import com.example.pia_claseordinaria.ui.components.FacturaCard
import com.example.pia_claseordinaria.utils.QRGenerator
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacturasScreen(viewModel: CondoViewModel, userId: String, onBack: () -> Unit) {
    val facturas by viewModel.facturas.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current
    
    var selectedFactura by remember { mutableStateOf<Factura?>(null) }
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        viewModel.loadFacturas(userId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Pagos y Facturas", fontWeight = FontWeight.Bold) },
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
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val pendiente = facturas.filter { it.estado == EstadoFactura.POR_PAGAR }.sumOf { it.monto }
                    Text("Total a Pagar", style = MaterialTheme.typography.bodyMedium)
                    Text("$${String.format("%.2f", pendiente)}", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
                    items(facturas) { factura ->
                        FacturaCard(factura = factura) {
                            selectedFactura = factura
                            showSheet = true
                        }
                    }
                }
            }
        }

        if (showSheet && selectedFactura != null) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState
            ) {
                FacturaDetailContent(
                    factura = selectedFactura!!,
                    onPayClick = {
                        viewModel.pagarFactura(selectedFactura!!.id, userId)
                        showSheet = false
                    },
                    onShareQR = { shareFacturaQR(context, selectedFactura!!) }
                )
            }
        }
    }
}

@Composable
fun FacturaDetailContent(factura: Factura, onPayClick: () -> Unit, onShareQR: () -> Unit) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val qrBitmap = remember(factura.id) { QRGenerator.generateQRCode("FACTURA:${factura.id}|MONTO:${factura.monto}|AMENIDAD:${factura.amenidadNombre}") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Detalle de Factura", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            IconButton(onClick = onShareQR) {
                Icon(Icons.Default.Share, contentDescription = "Compartir Comprobante")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        qrBitmap?.let {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Código QR de Pago",
                    modifier = Modifier.size(150.dp)
                )
            }
            Text(
                text = "Escanea este QR para validar el pago",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        DetailRow("Concepto", factura.concepto)
        DetailRow("Monto", "$${String.format("%.2f", factura.monto)}")
        DetailRow("Vencimiento", dateFormat.format(Date(factura.fechaVencimiento)))
        DetailRow("Estado", factura.estado.name)
        if (factura.amenidadNombre.isNotEmpty()) {
            DetailRow("Amenidad", factura.amenidadNombre)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (factura.estado == EstadoFactura.POR_PAGAR) {
            Button(
                onClick = onPayClick,
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Payments, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("PAGAR AHORA", modifier = Modifier.padding(8.dp))
                }
            }
        } else {
            Button(
                onClick = {},
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("YA ESTÁ PAGADO")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

private fun shareFacturaQR(context: Context, factura: Factura) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val details = listOf(
        "Concepto: ${factura.concepto}",
        "Monto: $${String.format("%.2f", factura.monto)}",
        "Vence: ${dateFormat.format(Date(factura.fechaVencimiento))}",
        "Estado: ${factura.estado}",
        "Amenidad: ${if(factura.amenidadNombre.isEmpty()) "General" else factura.amenidadNombre}"
    )
    
    val ticketBitmap = QRGenerator.createTicketBitmap(
        "COMPROBANTE DE PAGO",
        details,
        "FACTURA:${factura.id}|MONTO:${factura.monto}|STATUS:${factura.estado}"
    ) ?: return

    try {
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, "comprobante_pago.png")
        val stream = FileOutputStream(file)
        ticketBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()

        val contentUri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir Ticket de Pago"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray)
        Text(text = value, fontWeight = FontWeight.Medium)
    }
}
