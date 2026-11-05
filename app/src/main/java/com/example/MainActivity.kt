package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.PdfExportModal
import com.example.ui.screens.TableEditorScreen
import com.example.ui.screens.TableSetupScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.TableViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          GridCraftApp()
        }
      }
    }
  }
}

@Composable
fun GridCraftApp(viewModel: TableViewModel = viewModel()) {
  val uiState by viewModel.uiState.collectAsState()

  when (uiState.currentScreen) {
    AppScreen.SETUP -> {
      TableSetupScreen(
        recentTables = uiState.recentTables,
        onCreateTable = { rows, cols, title, preset ->
          viewModel.createTable(rows, cols, title, preset)
        },
        onOpenRecentTable = { table ->
          viewModel.openRecentTable(table)
        },
        onDeleteRecentTable = { id ->
          viewModel.deleteRecentTable(id)
        }
      )
    }

    AppScreen.EDITOR, AppScreen.PDF_PREVIEW -> {
      TableEditorScreen(
        tableGrid = uiState.tableGrid,
        selectedCell = uiState.selectedCell,
        zoomScale = uiState.zoomScale,
        panOffsetX = uiState.panOffsetX,
        panOffsetY = uiState.panOffsetY,
        onBack = {
          viewModel.navigateTo(AppScreen.SETUP)
        },
        onSelectCell = { r, c ->
          viewModel.selectCell(r, c)
        },
        onUpdateCellText = { text ->
          viewModel.updateCellText(text)
        },
        onUpdateCellColor = { bg, textCol ->
          viewModel.updateCellColor(bg, textCol)
        },
        onUpdateRowColor = { bg, textCol ->
          viewModel.updateRowColor(bg, textCol)
        },
        onUpdateColColor = { bg, textCol ->
          viewModel.updateColumnColor(bg, textCol)
        },
        onToggleBold = {
          viewModel.toggleSelectedCellBold()
        },
        onToggleItalic = {
          viewModel.toggleSelectedCellItalic()
        },
        onSetAlignment = { align ->
          viewModel.setSelectedCellAlignment(align)
        },
        onClearCell = {
          viewModel.clearSelectedCell()
        },
        onNavigateNext = {
          viewModel.navigateNextCell()
        },
        onNavigatePrev = {
          viewModel.navigatePrevCell()
        },
        onAddRow = {
          viewModel.addRow()
        },
        onRemoveRow = {
          viewModel.removeRow()
        },
        onAddCol = {
          viewModel.addColumn()
        },
        onRemoveCol = {
          viewModel.removeColumn()
        },
        onZoomIn = {
          viewModel.zoomIn()
        },
        onZoomOut = {
          viewModel.zoomOut()
        },
        onSetZoom = { scale ->
          viewModel.setZoom(scale)
        },
        onResetZoom = {
          viewModel.resetZoom()
        },
        onPanBy = { dx, dy ->
          viewModel.panBy(dx, dy)
        },
        onUpdateTitle = { title ->
          viewModel.updateTableTitle(title)
        },
        onOpenPdfExport = {
          viewModel.setPdfOptionsDialogOpen(true)
        }
      )

      if (uiState.pdfOptionsDialogOpen) {
        PdfExportModal(
          tableGrid = uiState.tableGrid,
          initialOptions = uiState.pdfOptions,
          onDismiss = { viewModel.setPdfOptionsDialogOpen(false) }
        )
      }
    }
  }
}
