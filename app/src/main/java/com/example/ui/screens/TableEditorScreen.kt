package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TableCell
import com.example.model.TableGrid
import com.example.model.TextAlignType
import com.example.ui.theme.CellColorOption
import com.example.ui.theme.CellColorPalette
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableEditorScreen(
  tableGrid: TableGrid,
  selectedCell: Pair<Int, Int>?,
  zoomScale: Float,
  panOffsetX: Float,
  panOffsetY: Float,
  onBack: () -> Unit,
  onSelectCell: (row: Int, col: Int) -> Unit,
  onUpdateCellText: (String) -> Unit,
  onUpdateCellColor: (bgColor: Long, textColor: Long?) -> Unit,
  onUpdateRowColor: (bgColor: Long, textColor: Long?) -> Unit,
  onUpdateColColor: (bgColor: Long, textColor: Long?) -> Unit,
  onToggleBold: () -> Unit,
  onToggleItalic: () -> Unit,
  onSetAlignment: (TextAlignType) -> Unit,
  onClearCell: () -> Unit,
  onNavigateNext: () -> Unit,
  onNavigatePrev: () -> Unit,
  onAddRow: () -> Unit,
  onRemoveRow: () -> Unit,
  onAddCol: () -> Unit,
  onRemoveCol: () -> Unit,
  onZoomIn: () -> Unit,
  onZoomOut: () -> Unit,
  onSetZoom: (Float) -> Unit,
  onResetZoom: () -> Unit,
  onPanBy: (Float, Float) -> Unit,
  onUpdateTitle: (String) -> Unit,
  onOpenPdfExport: () -> Unit
) {
  BackHandler {
    onBack()
  }

  var showCellEditorDialog by remember { mutableStateOf(false) }
  var showColorPaletteSheet by remember { mutableStateOf(false) }
  var showManageGridSheet by remember { mutableStateOf(false) }
  var showTitleDialog by remember { mutableStateOf(false) }
  var overflowMenuOpen by remember { mutableStateOf(false) }

  // Default to Fit Width so all columns fit cleanly on screen
  var fitToScreenWidth by remember { mutableStateOf(true) }

  val horizontalScrollState = rememberScrollState()
  val verticalScrollState = rememberScrollState()

  val currentCell = if (selectedCell != null) {
    tableGrid.getCell(selectedCell.first, selectedCell.second)
  } else null

  val colHeaderLabel = { c: Int ->
    if (c < 26) ('A' + c).toString() else "C${c + 1}"
  }
  val lastColLetter = colHeaderLabel(maxOf(0, tableGrid.colCount - 1))
  val cellCoordLabel = if (selectedCell != null) {
    "${colHeaderLabel(selectedCell.second)}${selectedCell.first + 1}"
  } else ""

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column(
            modifier = Modifier
              .clickable { showTitleDialog = true }
              .padding(vertical = 4.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = tableGrid.title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Spacer(modifier = Modifier.width(6.dp))
              Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = "Edit title",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
              )
            }
            Text(
              text = "${tableGrid.rowCount} Rows × ${tableGrid.colCount} Cols (Col A–$lastColLetter)",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.primary
            )
          }
        },
        navigationIcon = {
          IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("editor_back_button")
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Setup")
          }
        },
        actions = {
          // Export PDF Button
          Button(
            onClick = onOpenPdfExport,
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .padding(end = 4.dp)
              .testTag("export_pdf_button")
          ) {
            Icon(
              imageVector = Icons.Filled.PictureAsPdf,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "PDF", fontWeight = FontWeight.Bold)
          }

          IconButton(onClick = { overflowMenuOpen = true }) {
            Icon(Icons.Filled.MoreVert, contentDescription = "Options")
          }

          DropdownMenu(
            expanded = overflowMenuOpen,
            onDismissRequest = { overflowMenuOpen = false }
          ) {
            DropdownMenuItem(
              text = { Text("Grid Dimensions") },
              leadingIcon = { Icon(Icons.Filled.Settings, contentDescription = null) },
              onClick = {
                overflowMenuOpen = false
                showManageGridSheet = true
              }
            )
            DropdownMenuItem(
              text = { Text(if (fitToScreenWidth) "Wide Columns View" else "Fit All Columns") },
              leadingIcon = { Icon(Icons.Filled.FitScreen, contentDescription = null) },
              onClick = {
                overflowMenuOpen = false
                fitToScreenWidth = !fitToScreenWidth
              }
            )
            DropdownMenuItem(
              text = { Text("Reset Zoom (100%)") },
              leadingIcon = { Icon(Icons.Filled.ZoomOutMap, contentDescription = null) },
              onClick = {
                overflowMenuOpen = false
                onResetZoom()
              }
            )
            DropdownMenuItem(
              text = { Text("Export as PDF") },
              leadingIcon = { Icon(Icons.Filled.PictureAsPdf, contentDescription = null) },
              onClick = {
                overflowMenuOpen = false
                onOpenPdfExport()
              }
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    bottomBar = {
      // Bottom Cell Toolbar (Visible when cell is selected)
      AnimatedVisibility(
        visible = selectedCell != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
      ) {
        SelectedCellToolbar(
          cell = currentCell,
          cellCoordinate = cellCoordLabel,
          onEditText = { showCellEditorDialog = true },
          onOpenColorPicker = { showColorPaletteSheet = true },
          onToggleBold = onToggleBold,
          onToggleItalic = onToggleItalic,
          onSetAlignment = onSetAlignment,
          onClearCell = onClearCell,
          onNavigatePrev = onNavigatePrev,
          onNavigateNext = onNavigateNext,
          modifier = Modifier.navigationBarsPadding()
        )
      }
    }
  ) { innerPadding ->
    BoxWithConstraints(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .background(MaterialTheme.colorScheme.background)
    ) {
      val screenWidth = maxWidth
      val headerColWidth = 32.dp
      val horizontalMargin = 20.dp
      val availableWidth = (screenWidth - headerColWidth - horizontalMargin).coerceAtLeast(100.dp)

      // Calculate base cell width so all columns fit on screen
      val autoFitColWidth = (availableWidth / maxOf(1, tableGrid.colCount)).coerceIn(48.dp, 160.dp)
      val baseColWidth: Dp = if (fitToScreenWidth) autoFitColWidth else 105.dp

      // Scale dimensions by zoomScale so layout bounds, click targets and scrollbars match 100%
      val effectiveColWidth = (baseColWidth.value * zoomScale).dp
      val effectiveRowHeight = (44f * zoomScale).dp
      val effectiveHeaderWidth = (32f * zoomScale.coerceAtLeast(0.7f)).dp
      val effectiveHeaderHeight = (30f * zoomScale.coerceAtLeast(0.7f)).dp

      val effectiveFontSize: TextUnit = when {
        effectiveColWidth < 55.dp -> 9.sp
        effectiveColWidth < 75.dp -> 11.sp
        effectiveColWidth < 100.dp -> 13.sp
        else -> 14.sp
      }

      val totalTableWidth = effectiveHeaderWidth + (effectiveColWidth * tableGrid.colCount)
      val isHorizontallyOverflowing = totalTableWidth > screenWidth

      val transformableState = rememberTransformableState { zoomChange, _, _ ->
        if (zoomChange != 1.0f) {
          onSetZoom((zoomScale * zoomChange).coerceIn(0.4f, 3.0f))
        }
      }

      Column(modifier = Modifier.fillMaxSize()) {
        // Sub-bar showing column info and View Mode toggles
        Surface(
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(end = 8.dp)
              ) {
                Text(
                  text = "All ${tableGrid.colCount} Columns Loaded (A–$lastColLetter)",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
              }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
              FilterChip(
                selected = fitToScreenWidth,
                onClick = { fitToScreenWidth = !fitToScreenWidth },
                label = { Text(if (fitToScreenWidth) "Fit Width" else "Wide Cols", fontSize = 11.sp) },
                leadingIcon = {
                  Icon(
                    imageVector = if (fitToScreenWidth) Icons.Filled.FitScreen else Icons.Filled.ViewColumn,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                  )
                },
                modifier = Modifier.height(30.dp),
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                  selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
              )
            }
          }
        }

        // Horizontal overflow helper banner if table extends beyond screen
        if (isHorizontallyOverflowing) {
          Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp),
              horizontalArrangement = Arrangement.Center,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "← Swipe horizontally to view all ${tableGrid.colCount} columns (A through $lastColLetter) →",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
              )
            }
          }
        }

        // Main Zoomable & Dual-Axis Scrollable Grid Canvas
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
        ) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .transformable(state = transformableState)
              .horizontalScroll(horizontalScrollState)
              .verticalScroll(verticalScrollState)
              .padding(10.dp)
          ) {
            TableGridView(
              tableGrid = tableGrid,
              selectedCell = selectedCell,
              cellWidth = effectiveColWidth,
              cellHeight = effectiveRowHeight,
              headerWidth = effectiveHeaderWidth,
              headerHeight = effectiveHeaderHeight,
              fontSize = effectiveFontSize,
              onCellClick = { r, c ->
                if (selectedCell == Pair(r, c)) {
                  showCellEditorDialog = true
                } else {
                  onSelectCell(r, c)
                }
              }
            )
          }

          // Floating Zoom Controls (Bottom Right corner)
          ZoomFloatingControls(
            zoomScale = zoomScale,
            fitToScreen = fitToScreenWidth,
            onZoomIn = onZoomIn,
            onZoomOut = onZoomOut,
            onResetZoom = onResetZoom,
            onToggleFit = { fitToScreenWidth = !fitToScreenWidth },
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .padding(end = 16.dp, bottom = if (selectedCell != null) 92.dp else 24.dp)
          )
        }
      }
    }
  }

  // Cell Text & Style Editor Dialog
  if (showCellEditorDialog && currentCell != null && selectedCell != null) {
    CellTextEditorDialog(
      cell = currentCell,
      coordinateLabel = cellCoordLabel,
      onDismiss = { showCellEditorDialog = false },
      onSaveText = { text ->
        onUpdateCellText(text)
        showCellEditorDialog = false
      },
      onToggleBold = onToggleBold,
      onToggleItalic = onToggleItalic,
      onSetAlignment = onSetAlignment,
      onOpenColors = {
        showCellEditorDialog = false
        showColorPaletteSheet = true
      },
      onNextCell = {
        onNavigateNext()
      }
    )
  }

  // Color Palette Sheet
  if (showColorPaletteSheet && selectedCell != null) {
    ColorPickerBottomSheet(
      coordinateLabel = cellCoordLabel,
      onDismiss = { showColorPaletteSheet = false },
      onApplyToCell = { color, textColor ->
        onUpdateCellColor(color, textColor)
        showColorPaletteSheet = false
      },
      onApplyToRow = { color, textColor ->
        onUpdateRowColor(color, textColor)
        showColorPaletteSheet = false
      },
      onApplyToColumn = { color, textColor ->
        onUpdateColColor(color, textColor)
        showColorPaletteSheet = false
      }
    )
  }

  // Grid Dimensions & Settings Sheet
  if (showManageGridSheet) {
    ManageGridBottomSheet(
      tableGrid = tableGrid,
      onDismiss = { showManageGridSheet = false },
      onAddRow = onAddRow,
      onRemoveRow = onRemoveRow,
      onAddCol = onAddCol,
      onRemoveCol = onRemoveCol,
      onResetZoom = onResetZoom
    )
  }

  // Title Edit Dialog
  if (showTitleDialog) {
    var tempTitle by remember { mutableStateOf(tableGrid.title) }
    AlertDialog(
      onDismissRequest = { showTitleDialog = false },
      title = { Text("Rename Table") },
      text = {
        OutlinedTextField(
          value = tempTitle,
          onValueChange = { tempTitle = it },
          label = { Text("Table Title") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("rename_title_input")
        )
      },
      confirmButton = {
        Button(
          onClick = {
            onUpdateTitle(tempTitle)
            showTitleDialog = false
          },
          modifier = Modifier.testTag("confirm_rename_button")
        ) {
          Text("Save")
        }
      },
      dismissButton = {
        TextButton(onClick = { showTitleDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
fun TableGridView(
  tableGrid: TableGrid,
  selectedCell: Pair<Int, Int>?,
  cellWidth: Dp,
  cellHeight: Dp,
  headerWidth: Dp,
  headerHeight: Dp,
  fontSize: TextUnit,
  onCellClick: (row: Int, col: Int) -> Unit
) {
  Column(
    modifier = Modifier
      .wrapContentSize()
      .shadow(4.dp, RoundedCornerShape(8.dp))
      .clip(RoundedCornerShape(8.dp))
      .background(Color.White)
      .testTag("table_grid_view")
  ) {
    // Top Column Headers (Corner spacer + A, B, C...)
    Row {
      // Corner Header (# symbol)
      Box(
        modifier = Modifier
          .size(headerWidth, headerHeight)
          .background(MaterialTheme.colorScheme.surfaceVariant)
          .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "#",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      // ALL Selected Column Labels (A, B, C, D, E...)
      for (c in 0 until tableGrid.colCount) {
        val colLetter = if (c < 26) ('A' + c).toString() else "C${c + 1}"
        val isColSelected = selectedCell?.second == c
        Box(
          modifier = Modifier
            .size(cellWidth, headerHeight)
            .background(
              if (isColSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            )
            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            .testTag("col_header_$c"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = colLetter,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (isColSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    // Table Rows
    for (r in 0 until tableGrid.rowCount) {
      Row {
        // Row Number Header (1, 2, 3...)
        val isRowSelected = selectedCell?.first == r
        Box(
          modifier = Modifier
            .size(headerWidth, cellHeight)
            .background(
              if (isRowSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            )
            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "${r + 1}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (isRowSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        // All Data Cells in this row across all colCount columns
        for (c in 0 until tableGrid.colCount) {
          val cell = tableGrid.getCell(r, c)
          val isSelected = selectedCell == Pair(r, c)

          val cellBg = Color(cell.bgColor)
          val cellTextCol = Color(cell.textColor)

          Box(
            modifier = Modifier
              .size(cellWidth, cellHeight)
              .background(cellBg)
              .border(
                width = if (isSelected) 2.5.dp else 0.5.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFCBD5E1)
              )
              .clickable { onCellClick(r, c) }
              .padding(horizontal = 3.dp, vertical = 2.dp)
              .testTag("cell_${r}_$c"),
            contentAlignment = when (cell.textAlign) {
              TextAlignType.LEFT -> Alignment.CenterStart
              TextAlignType.RIGHT -> Alignment.CenterEnd
              TextAlignType.CENTER -> Alignment.Center
            }
          ) {
            Text(
              text = cell.text,
              color = cellTextCol,
              fontWeight = if (cell.isBold) FontWeight.Bold else FontWeight.Normal,
              fontStyle = if (cell.isItalic) FontStyle.Italic else FontStyle.Normal,
              fontSize = fontSize,
              textAlign = when (cell.textAlign) {
                TextAlignType.LEFT -> TextAlign.Start
                TextAlignType.RIGHT -> TextAlign.End
                TextAlignType.CENTER -> TextAlign.Center
              },
              maxLines = 2,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }
    }
  }
}

@Composable
fun SelectedCellToolbar(
  cell: TableCell?,
  cellCoordinate: String,
  onEditText: () -> Unit,
  onOpenColorPicker: () -> Unit,
  onToggleBold: () -> Unit,
  onToggleItalic: () -> Unit,
  onSetAlignment: (TextAlignType) -> Unit,
  onClearCell: () -> Unit,
  onNavigatePrev: () -> Unit,
  onNavigateNext: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    tonalElevation = 6.dp,
    shadowElevation = 8.dp,
    color = MaterialTheme.colorScheme.surface
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // Cell coordinate badge & text preview
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .weight(1f)
            .clickable { onEditText() }
        ) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.padding(end = 8.dp)
          ) {
            Text(
              text = cellCoordinate,
              fontWeight = FontWeight.Bold,
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }

          Text(
            text = if (cell?.text.isNullOrBlank()) "Tap to type text…" else cell?.text.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            color = if (cell?.text.isNullOrBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        // Navigation between cells
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onNavigatePrev,
            modifier = Modifier.size(36.dp).testTag("prev_cell_button")
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Cell", modifier = Modifier.size(20.dp))
          }
          IconButton(
            onClick = onNavigateNext,
            modifier = Modifier.size(36.dp).testTag("next_cell_button")
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Cell", modifier = Modifier.size(20.dp))
          }
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      // Formatting Action Buttons Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        // Edit Text button
        FilledTonalButton(
          onClick = onEditText,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.height(36.dp).testTag("edit_text_button")
        ) {
          Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Edit Text", fontSize = 12.sp)
        }

        // Color Picker button
        FilledTonalButton(
          onClick = onOpenColorPicker,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.height(36.dp).testTag("color_picker_button")
        ) {
          Icon(Icons.Filled.ColorLens, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Color", fontSize = 12.sp)
        }

        // Bold toggle
        IconButton(
          onClick = onToggleBold,
          colors = IconButtonDefaults.iconButtonColors(
            containerColor = if (cell?.isBold == true) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
          ),
          modifier = Modifier.size(36.dp).testTag("bold_toggle_button")
        ) {
          Icon(Icons.Filled.FormatBold, contentDescription = "Bold", modifier = Modifier.size(18.dp))
        }

        // Italic toggle
        IconButton(
          onClick = onToggleItalic,
          colors = IconButtonDefaults.iconButtonColors(
            containerColor = if (cell?.isItalic == true) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
          ),
          modifier = Modifier.size(36.dp).testTag("italic_toggle_button")
        ) {
          Icon(Icons.Filled.FormatItalic, contentDescription = "Italic", modifier = Modifier.size(18.dp))
        }

        // Alignment cycle
        IconButton(
          onClick = {
            val nextAlign = when (cell?.textAlign) {
              TextAlignType.LEFT -> TextAlignType.CENTER
              TextAlignType.CENTER -> TextAlignType.RIGHT
              TextAlignType.RIGHT -> TextAlignType.LEFT
              else -> TextAlignType.CENTER
            }
            onSetAlignment(nextAlign)
          },
          modifier = Modifier.size(36.dp).testTag("alignment_button")
        ) {
          val icon = when (cell?.textAlign) {
            TextAlignType.LEFT -> Icons.Filled.FormatAlignLeft
            TextAlignType.RIGHT -> Icons.Filled.FormatAlignRight
            else -> Icons.Filled.FormatAlignCenter
          }
          Icon(icon, contentDescription = "Align", modifier = Modifier.size(18.dp))
        }

        // Clear cell
        IconButton(
          onClick = onClearCell,
          modifier = Modifier.size(36.dp).testTag("clear_cell_button")
        ) {
          Icon(Icons.Filled.Delete, contentDescription = "Clear Cell", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
        }
      }
    }
  }
}

@Composable
fun ZoomFloatingControls(
  zoomScale: Float,
  fitToScreen: Boolean,
  onZoomIn: () -> Unit,
  onZoomOut: () -> Unit,
  onResetZoom: () -> Unit,
  onToggleFit: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(24.dp),
    shadowElevation = 6.dp,
    tonalElevation = 4.dp,
    color = MaterialTheme.colorScheme.surface,
    modifier = modifier.testTag("zoom_controls")
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
      IconButton(
        onClick = onZoomOut,
        modifier = Modifier.size(34.dp).testTag("zoom_out_button")
      ) {
        Icon(Icons.Filled.ZoomOut, contentDescription = "Zoom Out", modifier = Modifier.size(18.dp))
      }

      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
          .clickable { onResetZoom() }
          .padding(horizontal = 3.dp)
          .testTag("zoom_reset_button")
      ) {
        val zoomPercent = (zoomScale * 100).roundToInt()
        Text(
          text = "$zoomPercent%",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
        )
      }

      IconButton(
        onClick = onZoomIn,
        modifier = Modifier.size(34.dp).testTag("zoom_in_button")
      ) {
        Icon(Icons.Filled.ZoomIn, contentDescription = "Zoom In", modifier = Modifier.size(18.dp))
      }

      Spacer(modifier = Modifier.width(2.dp))

      IconButton(
        onClick = onToggleFit,
        modifier = Modifier.size(34.dp)
      ) {
        Icon(
          imageVector = if (fitToScreen) Icons.Filled.FitScreen else Icons.Filled.ViewColumn,
          contentDescription = "Toggle column fit",
          tint = if (fitToScreen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(18.dp)
        )
      }
    }
  }
}

@Composable
fun CellTextEditorDialog(
  cell: TableCell,
  coordinateLabel: String,
  onDismiss: () -> Unit,
  onSaveText: (String) -> Unit,
  onToggleBold: () -> Unit,
  onToggleItalic: () -> Unit,
  onSetAlignment: (TextAlignType) -> Unit,
  onOpenColors: () -> Unit,
  onNextCell: () -> Unit
) {
  var text by remember { mutableStateOf(cell.text) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Edit Cell ($coordinateLabel)", fontWeight = FontWeight.Bold)
        IconButton(onClick = onOpenColors) {
          Icon(Icons.Filled.ColorLens, contentDescription = "Change Color", tint = MaterialTheme.colorScheme.primary)
        }
      }
    },
    text = {
      Column {
        OutlinedTextField(
          value = text,
          onValueChange = { text = it },
          label = { Text("Cell Content") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("cell_content_input"),
          maxLines = 4,
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
          keyboardActions = KeyboardActions(onDone = { onSaveText(text) })
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Format Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            FilledTonalIconButton(
              onClick = onToggleBold,
              colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = if (cell.isBold) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
              ),
              modifier = Modifier.size(36.dp)
            ) {
              Icon(Icons.Filled.FormatBold, contentDescription = "Bold", modifier = Modifier.size(18.dp))
            }
            FilledTonalIconButton(
              onClick = onToggleItalic,
              colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = if (cell.isItalic) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
              ),
              modifier = Modifier.size(36.dp)
            ) {
              Icon(Icons.Filled.FormatItalic, contentDescription = "Italic", modifier = Modifier.size(18.dp))
            }
          }

          Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(
              onClick = { onSetAlignment(TextAlignType.LEFT) },
              modifier = Modifier.size(36.dp)
            ) {
              Icon(
                Icons.Filled.FormatAlignLeft,
                contentDescription = "Align Left",
                tint = if (cell.textAlign == TextAlignType.LEFT) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
              )
            }
            IconButton(
              onClick = { onSetAlignment(TextAlignType.CENTER) },
              modifier = Modifier.size(36.dp)
            ) {
              Icon(
                Icons.Filled.FormatAlignCenter,
                contentDescription = "Align Center",
                tint = if (cell.textAlign == TextAlignType.CENTER) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
              )
            }
            IconButton(
              onClick = { onSetAlignment(TextAlignType.RIGHT) },
              modifier = Modifier.size(36.dp)
            ) {
              Icon(
                Icons.Filled.FormatAlignRight,
                contentDescription = "Align Right",
                tint = if (cell.textAlign == TextAlignType.RIGHT) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onSaveText(text) },
        modifier = Modifier.testTag("save_cell_button")
      ) {
        Text("Done")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ColorPickerBottomSheet(
  coordinateLabel: String,
  onDismiss: () -> Unit,
  onApplyToCell: (bgColor: Long, textColor: Long?) -> Unit,
  onApplyToRow: (bgColor: Long, textColor: Long?) -> Unit,
  onApplyToColumn: (bgColor: Long, textColor: Long?) -> Unit
) {
  val sheetState = rememberModalBottomSheetState()
  var selectedColorOption by remember { mutableStateOf<CellColorOption>(CellColorPalette.defaultColors[0]) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 8.dp)
        .navigationBarsPadding()
    ) {
      Text(
        text = "Choose Box Color ($coordinateLabel)",
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleLarge
      )
      Text(
        text = "Select a color and choose where to apply it",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Color swatches grid
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        CellColorPalette.defaultColors.forEach { option ->
          val isSelected = selectedColorOption == option
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(CircleShape)
              .background(Color(option.bgColor))
              .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFCBD5E1),
                shape = CircleShape
              )
              .clickable { selectedColorOption = option }
              .testTag("color_${option.name.lowercase().replace(" ", "_")}"),
            contentAlignment = Alignment.Center
          ) {
            if (isSelected) {
              Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Selected",
                tint = Color(option.suggestedTextColor),
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Preview banner with selected color
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(selectedColorOption.bgColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Text(
            text = "Sample Box Text (${selectedColorOption.name})",
            color = Color(selectedColorOption.suggestedTextColor),
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Apply buttons
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
          onClick = {
            onApplyToCell(selectedColorOption.bgColor, selectedColorOption.suggestedTextColor)
          },
          modifier = Modifier.fillMaxWidth().testTag("apply_color_cell_button"),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("Apply to Selected Cell ($coordinateLabel)")
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          FilledTonalButton(
            onClick = {
              onApplyToRow(selectedColorOption.bgColor, selectedColorOption.suggestedTextColor)
            },
            modifier = Modifier.weight(1f).testTag("apply_color_row_button"),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text("Fill Entire Row")
          }

          FilledTonalButton(
            onClick = {
              onApplyToColumn(selectedColorOption.bgColor, selectedColorOption.suggestedTextColor)
            },
            modifier = Modifier.weight(1f).testTag("apply_color_col_button"),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text("Fill Entire Column")
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageGridBottomSheet(
  tableGrid: TableGrid,
  onDismiss: () -> Unit,
  onAddRow: () -> Unit,
  onRemoveRow: () -> Unit,
  onAddCol: () -> Unit,
  onRemoveCol: () -> Unit,
  onResetZoom: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState()

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 8.dp)
        .navigationBarsPadding()
    ) {
      Text(
        text = "Grid Management",
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleLarge
      )
      Text(
        text = "Current: ${tableGrid.rowCount} Rows × ${tableGrid.colCount} Columns",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(20.dp))

      // Row controls
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(text = "Rows (${tableGrid.rowCount})", fontWeight = FontWeight.SemiBold)
          Text(text = "Add or remove row at the end", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          FilledTonalIconButton(
            onClick = onRemoveRow,
            enabled = tableGrid.rowCount > 1,
            modifier = Modifier.testTag("remove_row_button")
          ) {
            Icon(Icons.Filled.Remove, contentDescription = "Remove row")
          }
          FilledTonalIconButton(
            onClick = onAddRow,
            enabled = tableGrid.rowCount < 40,
            modifier = Modifier.testTag("add_row_button")
          ) {
            Icon(Icons.Filled.Add, contentDescription = "Add row")
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Column controls
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(text = "Columns (${tableGrid.colCount})", fontWeight = FontWeight.SemiBold)
          Text(text = "Add or remove column at the end", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          FilledTonalIconButton(
            onClick = onRemoveCol,
            enabled = tableGrid.colCount > 1,
            modifier = Modifier.testTag("remove_col_button")
          ) {
            Icon(Icons.Filled.Remove, contentDescription = "Remove column")
          }
          FilledTonalIconButton(
            onClick = onAddCol,
            enabled = tableGrid.colCount < 20,
            modifier = Modifier.testTag("add_col_button")
          ) {
            Icon(Icons.Filled.Add, contentDescription = "Add column")
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      OutlinedButton(
        onClick = {
          onResetZoom()
          onDismiss()
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
      ) {
        Icon(Icons.Filled.ZoomOutMap, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Reset Canvas View (100%)")
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}
