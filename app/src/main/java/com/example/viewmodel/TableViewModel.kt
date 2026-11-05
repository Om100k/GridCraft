package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.TableRepository
import com.example.model.TableCell
import com.example.model.TableGrid
import com.example.model.TablePreset
import com.example.model.TablePresetProvider
import com.example.model.TextAlignType
import com.example.pdf.PageOrientation
import com.example.pdf.PageSizePreset
import com.example.pdf.PdfExportOptions
import com.example.pdf.PdfExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

enum class AppScreen {
  SETUP,
  EDITOR,
  PDF_PREVIEW
}

data class TableUiState(
  val currentScreen: AppScreen = AppScreen.SETUP,
  val tableGrid: TableGrid = TableGrid(title = "My Table", rowCount = 5, colCount = 4),
  val selectedCell: Pair<Int, Int>? = Pair(0, 0),
  val zoomScale: Float = 1.0f,
  val panOffsetX: Float = 0f,
  val panOffsetY: Float = 0f,
  val recentTables: List<TableGrid> = emptyList(),
  val pdfOptions: PdfExportOptions = PdfExportOptions(),
  val exportedFile: File? = null,
  val isExporting: Boolean = false,
  val exportSuccessMessage: String? = null,
  val cellEditorOpen: Boolean = false,
  val colorPaletteOpen: Boolean = false,
  val pdfOptionsDialogOpen: Boolean = false,
  val rowColEditorOpen: Boolean = false
)

class TableViewModel(application: Application) : AndroidViewModel(application) {
  private val repository = TableRepository(application)

  private val _uiState = MutableStateFlow(TableUiState())
  val uiState: StateFlow<TableUiState> = _uiState.asStateFlow()

  init {
    loadRecentTables()
  }

  fun loadRecentTables() {
    viewModelScope.launch {
      val tables = repository.getRecentTables()
      _uiState.update { it.copy(recentTables = tables) }
    }
  }

  fun createTable(rows: Int, cols: Int, title: String, preset: TablePreset? = null) {
    val initialCells = mutableMapOf<String, TableCell>()
    val cleanRows = rows.coerceIn(1, 40)
    val cleanCols = cols.coerceIn(1, 20)
    val finalTitle = title.trim().ifEmpty { "My Table" }

    if (preset != null && preset.initialData.isNotEmpty()) {
      for ((coords, text) in preset.initialData) {
        val (r, c) = coords
        if (r < cleanRows && c < cleanCols) {
          val isHeader = r == 0
          initialCells["$r,$c"] = TableCell(
            row = r,
            col = c,
            text = text,
            bgColor = if (isHeader) preset.headerColor else 0xFFFFFFFF,
            textColor = if (isHeader) preset.headerTextColor else 0xFF0F172A,
            isBold = isHeader,
            textAlign = TextAlignType.CENTER
          )
        }
      }
    } else {
      // Default header row styling for modern look
      for (c in 0 until cleanCols) {
        initialCells["0,$c"] = TableCell(
          row = 0,
          col = c,
          text = if (c < 26) ('A' + c).toString() else "Col ${c + 1}",
          bgColor = 0xFF1E3A8A,
          textColor = 0xFFFFFFFF,
          isBold = true,
          textAlign = TextAlignType.CENTER
        )
      }
    }

    val newGrid = TableGrid(
      title = finalTitle,
      rowCount = cleanRows,
      colCount = cleanCols,
      cells = initialCells
    )

    repository.saveTable(newGrid)
    loadRecentTables()

    _uiState.update {
      it.copy(
        currentScreen = AppScreen.EDITOR,
        tableGrid = newGrid,
        selectedCell = Pair(0, 0),
        zoomScale = 1.0f,
        panOffsetX = 0f,
        panOffsetY = 0f
      )
    }
  }

  fun openRecentTable(grid: TableGrid) {
    _uiState.update {
      it.copy(
        currentScreen = AppScreen.EDITOR,
        tableGrid = grid,
        selectedCell = Pair(0, 0),
        zoomScale = 1.0f,
        panOffsetX = 0f,
        panOffsetY = 0f
      )
    }
  }

  fun deleteRecentTable(id: String) {
    repository.deleteTable(id)
    loadRecentTables()
  }

  fun navigateTo(screen: AppScreen) {
    _uiState.update { it.copy(currentScreen = screen) }
  }

  fun updateTableTitle(newTitle: String) {
    _uiState.update {
      val updated = it.tableGrid.copy(title = newTitle.ifBlank { "My Table" })
      repository.saveTable(updated)
      it.copy(tableGrid = updated)
    }
    loadRecentTables()
  }

  fun selectCell(row: Int, col: Int) {
    _uiState.update { it.copy(selectedCell = Pair(row, col)) }
  }

  fun setCellEditorOpen(open: Boolean) {
    _uiState.update { it.copy(cellEditorOpen = open) }
  }

  fun setColorPaletteOpen(open: Boolean) {
    _uiState.update { it.copy(colorPaletteOpen = open) }
  }

  fun setPdfOptionsDialogOpen(open: Boolean) {
    _uiState.update { it.copy(pdfOptionsDialogOpen = open) }
  }

  fun setRowColEditorOpen(open: Boolean) {
    _uiState.update { it.copy(rowColEditorOpen = open) }
  }

  fun updateCellText(text: String) {
    val selected = _uiState.value.selectedCell ?: return
    _uiState.update { state ->
      val updated = state.tableGrid.updateCell(selected.first, selected.second) {
        it.copy(text = text)
      }
      repository.saveTable(updated)
      state.copy(tableGrid = updated)
    }
  }

  fun updateCellColor(bgColor: Long, textColor: Long? = null) {
    val selected = _uiState.value.selectedCell ?: return
    _uiState.update { state ->
      val updated = state.tableGrid.updateCell(selected.first, selected.second) {
        it.copy(
          bgColor = bgColor,
          textColor = textColor ?: it.textColor
        )
      }
      repository.saveTable(updated)
      state.copy(tableGrid = updated)
    }
  }

  fun updateRowColor(color: Long, textColor: Long? = null) {
    val selected = _uiState.value.selectedCell ?: return
    _uiState.update { state ->
      val updated = state.tableGrid.updateRowColor(selected.first, color, textColor)
      repository.saveTable(updated)
      state.copy(tableGrid = updated)
    }
  }

  fun updateColumnColor(color: Long, textColor: Long? = null) {
    val selected = _uiState.value.selectedCell ?: return
    _uiState.update { state ->
      val updated = state.tableGrid.updateColColor(selected.second, color, textColor)
      repository.saveTable(updated)
      state.copy(tableGrid = updated)
    }
  }

  fun toggleSelectedCellBold() {
    val selected = _uiState.value.selectedCell ?: return
    _uiState.update { state ->
      val updated = state.tableGrid.updateCell(selected.first, selected.second) {
        it.copy(isBold = !it.isBold)
      }
      repository.saveTable(updated)
      state.copy(tableGrid = updated)
    }
  }

  fun toggleSelectedCellItalic() {
    val selected = _uiState.value.selectedCell ?: return
    _uiState.update { state ->
      val updated = state.tableGrid.updateCell(selected.first, selected.second) {
        it.copy(isItalic = !it.isItalic)
      }
      repository.saveTable(updated)
      state.copy(tableGrid = updated)
    }
  }

  fun setSelectedCellAlignment(align: TextAlignType) {
    val selected = _uiState.value.selectedCell ?: return
    _uiState.update { state ->
      val updated = state.tableGrid.updateCell(selected.first, selected.second) {
        it.copy(textAlign = align)
      }
      repository.saveTable(updated)
      state.copy(tableGrid = updated)
    }
  }

  fun clearSelectedCell() {
    val selected = _uiState.value.selectedCell ?: return
    _uiState.update { state ->
      val updated = state.tableGrid.updateCell(selected.first, selected.second) {
        it.copy(text = "", bgColor = 0xFFFFFFFF, textColor = 0xFF0F172A, isBold = false, isItalic = false)
      }
      repository.saveTable(updated)
      state.copy(tableGrid = updated)
    }
  }

  fun navigateNextCell() {
    val selected = _uiState.value.selectedCell ?: return
    val grid = _uiState.value.tableGrid
    val totalCols = grid.colCount
    val totalRows = grid.rowCount
    var r = selected.first
    var c = selected.second + 1
    if (c >= totalCols) {
      c = 0
      r = (r + 1).coerceAtMost(totalRows - 1)
    }
    _uiState.update { it.copy(selectedCell = Pair(r, c)) }
  }

  fun navigatePrevCell() {
    val selected = _uiState.value.selectedCell ?: return
    val grid = _uiState.value.tableGrid
    val totalCols = grid.colCount
    var r = selected.first
    var c = selected.second - 1
    if (c < 0) {
      c = totalCols - 1
      r = (r - 1).coerceAtLeast(0)
    }
    _uiState.update { it.copy(selectedCell = Pair(r, c)) }
  }

  fun addRow() {
    _uiState.update { state ->
      val updated = state.tableGrid.addRow()
      repository.saveTable(updated)
      state.copy(tableGrid = updated)
    }
  }

  fun removeRow() {
    _uiState.update { state ->
      val updated = state.tableGrid.removeRow()
      val sel = state.selectedCell
      val adjustedSel = if (sel != null && sel.first >= updated.rowCount) {
        Pair(updated.rowCount - 1, sel.second)
      } else sel
      repository.saveTable(updated)
      state.copy(tableGrid = updated, selectedCell = adjustedSel)
    }
  }

  fun addColumn() {
    _uiState.update { state ->
      val updated = state.tableGrid.addColumn()
      repository.saveTable(updated)
      state.copy(tableGrid = updated)
    }
  }

  fun removeColumn() {
    _uiState.update { state ->
      val updated = state.tableGrid.removeColumn()
      val sel = state.selectedCell
      val adjustedSel = if (sel != null && sel.second >= updated.colCount) {
        Pair(sel.first, updated.colCount - 1)
      } else sel
      repository.saveTable(updated)
      state.copy(tableGrid = updated, selectedCell = adjustedSel)
    }
  }

  // Zoom and Pan controls
  fun zoomIn() {
    _uiState.update {
      val newScale = (it.zoomScale + 0.25f).coerceIn(0.4f, 3.5f)
      it.copy(zoomScale = newScale)
    }
  }

  fun zoomOut() {
    _uiState.update {
      val newScale = (it.zoomScale - 0.25f).coerceIn(0.4f, 3.5f)
      it.copy(zoomScale = newScale)
    }
  }

  fun setZoom(scale: Float) {
    _uiState.update {
      it.copy(zoomScale = scale.coerceIn(0.4f, 3.5f))
    }
  }

  fun resetZoom() {
    _uiState.update {
      it.copy(zoomScale = 1.0f, panOffsetX = 0f, panOffsetY = 0f)
    }
  }

  fun panBy(dx: Float, dy: Float) {
    _uiState.update {
      it.copy(
        panOffsetX = it.panOffsetX + dx,
        panOffsetY = it.panOffsetY + dy
      )
    }
  }

  fun updatePdfOptions(options: PdfExportOptions) {
    _uiState.update { it.copy(pdfOptions = options) }
  }

  fun generatePdfFile(context: Context): File {
    val file = PdfExporter.exportToCacheFile(context, _uiState.value.tableGrid, _uiState.value.pdfOptions)
    _uiState.update { it.copy(exportedFile = file) }
    return file
  }

  fun clearExportMessage() {
    _uiState.update { it.copy(exportSuccessMessage = null) }
  }

  fun setExportSuccess(msg: String) {
    _uiState.update { it.copy(exportSuccessMessage = msg) }
  }
}
