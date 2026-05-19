package com.example.pia_claseordinaria.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

object QRGenerator {
    fun generateQRCode(content: String): Bitmap? {
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Crea una imagen de "Ticket" que contiene el título, detalles y el código QR.
     */
    fun createTicketBitmap(title: String, details: List<String>, qrContent: String): Bitmap? {
        val qrBitmap = generateQRCode(qrContent) ?: return null
        
        val width = 600
        val padding = 40
        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 24f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 36f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        // Calcular altura dinámica
        val lineSpacing = 10
        val titleHeight = 50
        val detailsHeight = details.size * (textPaint.textSize.toInt() + lineSpacing)
        val qrSize = 512
        val totalHeight = padding + titleHeight + 20 + detailsHeight + 30 + qrSize + padding

        val resultBitmap = Bitmap.createBitmap(width, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        canvas.drawColor(Color.WHITE)

        var currentY = padding.toFloat() + titleHeight
        canvas.drawText(title.uppercase(), padding.toFloat(), currentY, titlePaint)
        
        currentY += 20
        details.forEach { detail ->
            currentY += textPaint.textSize + lineSpacing
            canvas.drawText(detail, padding.toFloat(), currentY, textPaint)
        }

        currentY += 40
        val qrLeft = (width - qrSize) / 2f
        canvas.drawBitmap(qrBitmap, qrLeft, currentY, null)

        return resultBitmap
    }
}
