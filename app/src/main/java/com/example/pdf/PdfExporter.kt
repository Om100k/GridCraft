package com.example.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.model.TableCell
import com.example.model.TableGrid
import com.example.model.TextAlignType
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class PageOrientation {
  PORTRAIT,
  LANDSCAPE
}

enum class PageSizePreset {
  A4,
  LETTER
}

data class PdfExportOptions(
  val orientation: PageOrientation = PageOrientation.PORTRAIT,
  val pageSize: PageSizePreset = PageSizePreset.A4,
  val includeTitle: Boolean = true,
  val includeTimestamp: Boolean = true,
  val includePageNumbers: Boolean = true,
  val autoFitWidth: Boolean = true
)

object PdfExporter {

  fun exportToCacheFile(
    context: Context,
    grid: TableGrid,
    options: PdfExportOptions = PdfExportOptions()
  ): File {
    val sanitizedTitle = grid.title.replace(Regex("[^a-zA-Z0-9_-]"), "_")
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val fileName = "Table_${sanitizedTitle}_$timestamp.pdf"
    val file = File(context.cacheDir, fileName)
    FileOutputStream(file).use { outputStream ->
      exportToStream(grid, outputStream, options)
    }
    return file
  }

  fun exportToStream(
    grid: TableGrid,
    outputStream: OutputStream,
    options: PdfExportOptions = PdfExportOptions()
  ) {
    val (rawWidth, rawHeight) = when (options.pageSize) {
      PageSizePreset.A4 -> Pair(595, 842)
      PageSizePreset.LETTER -> Pair(612, 792)
    }

    val (pageWidth, pageHeight) = if (options.orientation == PageOrientation.PORTRAIT) {
      Pair(rawWidth, rawHeight)
    } else {
      Pair(rawHeight, rawWidth)
    }

    val margin = 36f // 0.5 inch margins
    val contentWidth = pageWidth - 2 * margin
    val colCount = maxOf(1, grid.colCount)
    val colWidth = contentWidth / colCount
    val rowHeight = 28f

    val titleHeight = if (options.includeTitle) 32f else 0f
    val subtitleHeight = if (options.includeTimestamp) 20f else 0f
    val headerGap = 16f
    val footerHeight = 24f

    val topContentStart = margin + titleHeight + subtitleHeight + (if (titleHeight > 0 || subtitleHeight > 0) headerGap else 0f)
    val bottomContentEnd = pageHeight - margin - footerHeight
    val availableTableHeight = bottomContentEnd - topContentStart

    val rowsPerPage = maxOf(1, (availableTableHeight / rowHeight).toInt())
    val totalRows = grid.rowCount
    val totalPages = maxOf(1, ((totalRows + rowsPerPage - 1) / rowsPerPage))

    val document = PdfDocument()

    // Paints
    val bgPaint = Paint().apply { style = Paint.Style.FILL }
    val borderPaint = Paint().apply {
      style = Paint.Style.STROKE
      strokeWidth = 1f
      color = AndroidColor.parseColor("#CBD5E1")
    }
    val titlePaint = Paint().apply {
      isAntiAlias = true
      textSize = 18f
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      color = AndroidColor.parseColor("#0F172A")
    }
    val metaPaint = Paint().apply {
      isAntiAlias = true
      textSize = 9f
      color = AndroidColor.parseColor("#64748B")
    }
    val textPaint = Paint().apply {
      isAntiAlias = true
      textSize = 11f
    }

    var currentRow = 0
    val dateStr = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date())

    for (pageIndex in 0 until totalPages) {
      val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex + 1).create()
      val page = document.startPage(pageInfo)
      val canvas = page.canvas

      // Draw Header on first page (or compact on other pages)
      var currentY = margin
      if (options.includeTitle) {
        currentY += 20f
        canvas.drawText(grid.title, margin, currentY, titlePaint)
      }
      if (options.includeTimestamp) {
        currentY += 16f
        val meta = "$dateStr   |   ${grid.rowCount} Rows × ${grid.colCount} Columns"
        canvas.drawText(meta, margin, currentY, metaPaint)
      }

      val tableStartY = if (titleHeight > 0 || subtitleHeight > 0) currentY + headerGap else margin

      // Determine rows for this page
      val endRowForPage = minOf(totalRows, currentRow + rowsPerPage)
      var cellY = tableStartY

      for (r in currentRow until endRowForPage) {
        for (c in 0 until colCount) {
          val cell = grid.getCell(r, c)
          val cellLeft = margin + c * colWidth
          val cellTop = cellY
          val cellRight = cellLeft + colWidth
          val cellBottom = cellTop + rowHeight
          val rect = RectF(cellLeft, cellTop, cellRight, cellBottom)

          // 1. Draw Cell Background
          val bgColorInt = cell.bgColor.toInt()
          bgPaint.color = bgColorInt
          canvas.drawRect(rect, bgPaint)

          // 2. Draw Cell Border
          canvas.drawRect(rect, borderPaint)

          // 3. Draw Cell Text
          val textColorInt = cell.textColor.toInt()
          textPaint.color = textColorInt
          val typefaceStyle = when {
            cell.isBold && cell.isItalic -> Typeface.BOLD_ITALIC
            cell.isBold -> Typeface.BOLD
            cell.isItalic -> Typeface.ITALIC
            else -> Typeface.NORMAL
          }
          textPaint.typeface = Typeface.create(Typeface.DEFAULT, typefaceStyle)

          // Fit text within cell (support multi-line if text has line breaks)
          val lines = if (cell.text.contains("\n")) cell.text.split("\n") else listOf(cell.text)
          val lineHeight = textPaint.textSize + 2f
          val totalTextHeight = lines.size * lineHeight
          var currentLineY = cellTop + (rowHeight / 2f) - (totalTextHeight / 2f) + textPaint.textSize * 0.85f

          for (line in lines) {
            val textToDraw = ellipsizeText(line, colWidth - 8f, textPaint)
            val textWidth = textPaint.measureText(textToDraw)
            val textX = when (cell.textAlign) {
              TextAlignType.LEFT -> cellLeft + 6f
              TextAlignType.RIGHT -> cellRight - 6f - textWidth
              TextAlignType.CENTER -> cellLeft + (colWidth - textWidth) / 2f
            }
            canvas.drawText(textToDraw, textX, currentLineY, textPaint)
            currentLineY += lineHeight
          }
        }
        cellY += rowHeight
      }

      // Draw Footer
      if (options.includePageNumbers) {
        val footerText = "GridCraft • Page ${pageIndex + 1} of $totalPages"
        val footerWidth = metaPaint.measureText(footerText)
        val footerX = (pageWidth - footerWidth) / 2f
        val footerY = pageHeight - margin / 2f
        canvas.drawText(footerText, footerX, footerY, metaPaint)
      }

      document.finishPage(page)
      currentRow = endRowForPage
      if (currentRow >= totalRows) break
    }

    document.writeTo(outputStream)
    document.close()
  }

  private fun ellipsizeText(text: String, maxWidth: Float, paint: Paint): String {
    if (paint.measureText(text) <= maxWidth) return text
    var truncated = text
    while (truncated.isNotEmpty() && paint.measureText("$truncated…") > maxWidth) {
      truncated = truncated.dropLast(1)
    }
    return if (truncated.isEmpty()) "" else "$truncated…"
  }
}
