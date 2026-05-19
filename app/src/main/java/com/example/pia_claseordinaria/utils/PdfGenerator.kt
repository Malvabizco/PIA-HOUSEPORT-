package com.example.pia_claseordinaria.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.pia_claseordinaria.models.Factura
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfGenerator {

    fun generateAndShareFinanzasPdf(context: Context, facturas: List<Factura>) {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint().apply {
            textSize = 20f
            isFakeBoldText = true
        }
        val headerPaint = Paint().apply {
            textSize = 14f
            isFakeBoldText = true
        }
        val textPaint = Paint().apply {
            textSize = 12f
        }

        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        var y = 40f
        canvas.drawText("Reporte de Finanzas - Condominio", 40f, y, titlePaint)
        y += 30f
        
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        canvas.drawText("Fecha de reporte: ${sdf.format(Date())}", 40f, y, textPaint)
        y += 40f

        // Headers
        canvas.drawText("Concepto", 40f, y, headerPaint)
        canvas.drawText("Monto", 250f, y, headerPaint)
        canvas.drawText("Fecha", 350f, y, headerPaint)
        canvas.drawText("Estado", 480f, y, headerPaint)
        
        y += 10f
        canvas.drawLine(40f, y, 550f, y, paint)
        y += 20f

        facturas.forEach { factura ->
            if (y > 800) { // Simple page break check (not fully multi-page)
                pdfDocument.finishPage(page)
                // In a real app, you'd start a new page here
                return@forEach 
            }
            
            canvas.drawText(factura.concepto.take(25), 40f, y, textPaint)
            canvas.drawText("$${String.format("%.2f", factura.monto)}", 250f, y, textPaint)
            canvas.drawText(sdf.format(Date(factura.fechaCreacion)), 350f, y, textPaint)
            canvas.drawText(factura.estado.name, 480f, y, textPaint)
            y += 20f
        }

        pdfDocument.finishPage(page)

        val directory = File(context.cacheDir, "docs")
        if (!directory.exists()) directory.mkdirs()
        
        val file = File(directory, "Reporte_Finanzas_${System.currentTimeMillis()}.pdf")
        
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            sharePdf(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            pdfDocument.close()
        }
    }

    private fun sharePdf(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Reporte de Finanzas")
            putExtra(Intent.EXTRA_TEXT, "Adjunto envío el reporte de finanzas generado desde la app.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Compartir Reporte"))
    }
}
