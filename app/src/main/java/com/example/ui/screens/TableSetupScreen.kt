package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TableGrid
import com.example.model.TablePreset
import com.example.model.TablePresetProvider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TableSetupScreen(
  recentTables: List<TableGrid>,
  onCreateTable: (rows: Int, cols: Int, title: String, preset: TablePreset?) -> Unit,
  onOpenRecentTable: (TableGrid) -> Unit,
  onDeleteRecentTable: (String) -> Unit
) {
  var rows by remember { mutableIntStateOf(5) }
  var cols by remember { mutableIntStateOf(4) }
  var title by remember { mutableStateOf("") }
  var selectedPreset by remember { mutableStateOf<TablePreset?>(null) }
  val focusManager = LocalFocusManager.current

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    TopAppBar(
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Filled.TableChart,
            contentDescription = "GridCraft Logo",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "GridCraft",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge
          )
        }
      },
      colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surface
      )
    )

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
      item {
        Spacer(modifier = Modifier.height(4.dp))
        // Header Banner Card
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(20.dp)
          ) {
            Text(
              text = "Create New Grid Table",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Choose row & column count, customize each box with text and colors, zoom freely, and export clean PDFs.",
              style = MaterialTheme.typography.bodyMedium,
              color = Color.White.copy(alpha = 0.85f)
            )
          }
        }
      }

      // Title input
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
          ),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Table Title",
              fontWeight = FontWeight.SemiBold,
              style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
              value = title,
              onValueChange = { title = it },
              placeholder = { Text("e.g. Weekly Schedule, Budget Sheet, Product List") },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("table_title_input"),
              singleLine = true,
              trailingIcon = {
                if (title.isNotEmpty()) {
                  IconButton(onClick = { title = "" }) {
                    Icon(Icons.Filled.Clear, contentDescription = "Clear Title")
                  }
                }
              },
              keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
              keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
              shape = RoundedCornerShape(12.dp)
            )
          }
        }
      }

      // Rows and Columns Config Card
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
          ),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Grid Dimensions",
              fontWeight = FontWeight.SemiBold,
              style = MaterialTheme.typography.titleMedium
            )
            Text(
              text = "Set rows and columns for your table",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Rows selector
            DimensionCounter(
              label = "Rows",
              count = rows,
              minVal = 1,
              maxVal = 30,
              onCountChange = {
                rows = it
                selectedPreset = null
              },
              quickPresets = listOf(3, 5, 8, 10, 15),
              testTagPrefix = "rows"
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Columns selector
            DimensionCounter(
              label = "Columns",
              count = cols,
              minVal = 1,
              maxVal = 20,
              onCountChange = {
                cols = it
                selectedPreset = null
              },
              quickPresets = listOf(2, 3, 4, 5, 6, 7, 8, 10, 12),
              testTagPrefix = "cols"
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Live Grid Visualizer Preview
            Text(
              text = "Live Grid Preview ($rows × $cols)",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            MiniGridPreview(
              rows = rows,
              cols = cols,
              modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
            )
          }
        }
      }

      // Quick Starter Templates / Presets
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
          ),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Or Choose a Starter Template",
              fontWeight = FontWeight.SemiBold,
              style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              TablePresetProvider.presets.forEach { preset ->
                val isSelected = selectedPreset == preset
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                  border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                      selectedPreset = preset
                      rows = preset.rows
                      cols = preset.cols
                      if (title.isBlank()) title = preset.name
                    }
                    .testTag("preset_${preset.name.lowercase().replace(" ", "_")}")
                ) {
                  Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Box(
                      modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(preset.headerColor)),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(
                        imageVector = Icons.Filled.GridOn,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                      )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = preset.name,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                      )
                      Text(
                        text = "${preset.description} • ${preset.rows}×${preset.cols}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                    if (isSelected) {
                      Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }

      // Create Table Button
      item {
        Button(
          onClick = {
            val finalTitle = title.ifBlank { selectedPreset?.name ?: "My Table" }
            onCreateTable(rows, cols, finalTitle, selectedPreset)
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .testTag("create_table_button"),
          shape = RoundedCornerShape(16.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
          )
        ) {
          Icon(
            imageVector = Icons.Filled.TableChart,
            contentDescription = null,
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Create Table (${rows} × ${cols})",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      // Recent Tables Section
      if (recentTables.isNotEmpty()) {
        item {
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "Recent Tables",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
          )
        }

        items(recentTables, key = { it.id }) { table ->
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onOpenRecentTable(table) }
              .testTag("recent_table_${table.id}")
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(42.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Filled.TableChart,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = table.title,
                  fontWeight = FontWeight.SemiBold,
                  style = MaterialTheme.typography.bodyLarge
                )
                val dateStr = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date(table.lastModified))
                Text(
                  text = "${table.rowCount} Rows × ${table.colCount} Columns • $dateStr",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              IconButton(
                onClick = { onDeleteRecentTable(table.id) },
                modifier = Modifier.testTag("delete_table_${table.id}")
              ) {
                Icon(
                  imageVector = Icons.Filled.DeleteOutline,
                  contentDescription = "Delete Table",
                  tint = MaterialTheme.colorScheme.error
                )
              }
              Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DimensionCounter(
  label: String,
  count: Int,
  minVal: Int,
  maxVal: Int,
  onCountChange: (Int) -> Unit,
  quickPresets: List<Int>,
  testTagPrefix: String
) {
  var showDirectInputDialog by remember { mutableStateOf(false) }
  var directInputText by remember { mutableStateOf(count.toString()) }

  Column {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = label,
          fontWeight = FontWeight.SemiBold,
          style = MaterialTheme.typography.bodyLarge
        )
        Text(
          text = "Tap number to type exact count",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        FilledTonalIconButton(
          onClick = { if (count > minVal) onCountChange(count - 1) },
          enabled = count > minVal,
          modifier = Modifier
            .size(38.dp)
            .testTag("${testTagPrefix}_decrement")
        ) {
          Icon(Icons.Filled.Remove, contentDescription = "Decrease $label")
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier
            .clickable {
              directInputText = count.toString()
              showDirectInputDialog = true
            }
            .padding(horizontal = 4.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
          ) {
            Text(
              text = "$count",
              fontWeight = FontWeight.Bold,
              style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
              Icons.Filled.Edit,
              contentDescription = "Edit count",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(12.dp)
            )
          }
        }

        FilledTonalIconButton(
          onClick = { if (count < maxVal) onCountChange(count + 1) },
          enabled = count < maxVal,
          modifier = Modifier
            .size(38.dp)
            .testTag("${testTagPrefix}_increment")
        ) {
          Icon(Icons.Filled.Add, contentDescription = "Increase $label")
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Quick chips
    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      quickPresets.forEach { presetVal ->
        FilterChip(
          selected = count == presetVal,
          onClick = { onCountChange(presetVal) },
          label = { Text("$presetVal") },
          modifier = Modifier.height(32.dp),
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
          )
        )
      }
    }
  }

  if (showDirectInputDialog) {
    AlertDialog(
      onDismissRequest = { showDirectInputDialog = false },
      title = { Text("Set $label Count ($minVal - $maxVal)") },
      text = {
        OutlinedTextField(
          value = directInputText,
          onValueChange = { directInputText = it.filter { ch -> ch.isDigit() } },
          keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      },
      confirmButton = {
        Button(
          onClick = {
            val parsed = directInputText.toIntOrNull()
            if (parsed != null) {
              onCountChange(parsed.coerceIn(minVal, maxVal))
            }
            showDirectInputDialog = false
          }
        ) {
          Text("Set")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDirectInputDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
fun MiniGridPreview(
  rows: Int,
  cols: Int,
  modifier: Modifier = Modifier
) {
  val displayRows = rows.coerceAtMost(6)
  val displayCols = cols.coerceAtMost(10)
  val lastColChar = if (cols <= 26) ('A' + cols - 1).toString() else "Col $cols"

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
      .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
      .padding(8.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      verticalArrangement = Arrangement.spacedBy(2.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      // Column letters preview
      Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        for (c in 0 until displayCols) {
          val colLetter = if (c < 26) ('A' + c).toString() else "C"
          Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = colLetter,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }
      }

      for (r in 0 until displayRows) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(2.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          for (c in 0 until displayCols) {
            val isHeader = r == 0
            Box(
              modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                  if (isHeader) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                )
            )
          }
        }
      }

      Text(
        text = "✓ Table will load all $cols columns (A to $lastColChar) and $rows rows",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 2.dp)
      )
    }
  }
}

