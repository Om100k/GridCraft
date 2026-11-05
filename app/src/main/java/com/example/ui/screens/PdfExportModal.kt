package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.StayCurrentLandscape
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.model.TableGrid
import com.example.pdf.PageOrientation
import com.example.pdf.PageSizePreset
import com.example.pdf.PdfExportOptions
import com.example.pdf.PdfExporter
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfExportModal(
  tableGrid: TableGrid,
  initialOptions: PdfExportOptions,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val scrollState = rememberScrollState()

  var orientation by remember { mutableStateOf(initialOptions.orientation) }
  var pageSize by remember { mutableStateOf(initialOptions.pageSize) }
  var includeTitle by remember { mutableStateOf(initialOptions.includeTitle) }
  var includeTimestamp by remember { mutableStateOf(initialOptions.includeTimestamp) }
  var includePageNumbers by remember { mutableStateOf(initialOptions.includePageNumbers) }

  var savedSuccessFileMessage by remember { mutableStateOf<String?>(null) }
  var savedFileUri by remember { mutableStateOf<Uri?>(null) }
  var isSaving by remember { mutableStateOf(false) }

  val currentOptions = PdfExportOptions(
    orientation = orientation,
    pageSize = pageSize,
    includeTitle = includeTitle,
    includeTimestamp = includeTimestamp,
    includePageNumbers = includePageNumbers
  )

  // System file creator contract for saving directly to storage
  val savePdfLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.CreateDocument("application/pdf")
  ) { uri: Uri? ->
    if (uri != null) {
      try {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
          PdfExporter.exportToStream(tableGrid, outputStream, currentOptions)
        }
        savedFileUri = uri
        savedSuccessFileMessage = "Saved PDF to selected location!"
        Toast.makeText(context, "PDF saved successfully!", Toast.LENGTH_LONG).show()
      } catch (e: Exception) {
        Toast.makeText(context, "Failed to save PDF: ${e.message}", Toast.LENGTH_LONG).show()
      }
    }
  }

  fun performSaveToDownloads() {
    isSaving = true
    try {
      val result = PdfExporter.savePdfToPublicDownloads(context, tableGrid, currentOptions)
      if (result != null) {
        savedFileUri = result.first
        savedSuccessFileMessage = "Saved to Downloads: ${result.second}"
        Toast.makeText(context, "Saved to Downloads: ${result.second}", Toast.LENGTH_LONG).show()
      } else {
        // Fallback to cache file + share if direct media store couldn't write
        val file = PdfExporter.exportToCacheFile(context, tableGrid, currentOptions)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        savedFileUri = uri
        savedSuccessFileMessage = "Saved PDF file (${file.name})"
        Toast.makeText(context, "PDF generated successfully!", Toast.LENGTH_SHORT).show()
      }
    } catch (e: Exception) {
      Toast.makeText(context, "Save error: ${e.message}", Toast.LENGTH_LONG).show()
    } finally {
      isSaving = false
    }
  }

  fun sharePdfFile() {
    try {
      val file = PdfExporter.exportToCacheFile(context, tableGrid, currentOptions)
      val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
      val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "${tableGrid.title} - PDF Table")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
      context.startActivity(Intent.createChooser(shareIntent, "Share Table PDF"))
    } catch (e: Exception) {
      Toast.makeText(context, "Error sharing PDF: ${e.message}", Toast.LENGTH_SHORT).show()
    }
  }

  fun viewPdfFile() {
    try {
      val uri = savedFileUri ?: run {
        val file = PdfExporter.exportToCacheFile(context, tableGrid, currentOptions)
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
      }
      val viewIntent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
      val chooser = Intent.createChooser(viewIntent, "Open PDF with…")
      context.startActivity(chooser)
    } catch (e: Exception) {
      Toast.makeText(context, "No PDF viewer found on device. PDF is saved to Downloads.", Toast.LENGTH_LONG).show()
    }
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(scrollState)
        .padding(horizontal = 20.dp, vertical = 6.dp)
        .navigationBarsPadding()
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Filled.PictureAsPdf,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(24.dp)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "Export Table as PDF",
              fontWeight = FontWeight.Bold,
              style = MaterialTheme.typography.titleLarge
            )
            Text(
              text = "${tableGrid.title} • ${tableGrid.rowCount} Rows × ${tableGrid.colCount} Cols",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        IconButton(onClick = onDismiss) {
          Icon(Icons.Filled.Close, contentDescription = "Close")
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // SUCCESS BANNER (If already saved)
      AnimatedVisibility(
        visible = savedSuccessFileMessage != null,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Filled.CheckCircle,
              contentDescription = null,
              tint = Color(0xFF15803D),
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "PDF Saved Successfully!",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF14532D)
              )
              Text(
                text = savedSuccessFileMessage.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF166534)
              )
            }
            FilledTonalButton(
              onClick = { viewPdfFile() },
              colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF15803D), contentColor = Color.White),
              shape = RoundedCornerShape(8.dp)
            ) {
              Text("Open")
            }
          }
        }
      }

      // PRIMARY ACTION BUTTONS CARD (Right at the top, impossible to miss)
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Save & Export Options",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(10.dp))

          // 1. PRIMARY SAVE TO DOWNLOADS BUTTON
          Button(
            onClick = { performSaveToDownloads() },
            enabled = !isSaving,
            modifier = Modifier
              .fillMaxWidth()
              .height(54.dp)
              .testTag("save_pdf_downloads_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary
            )
          ) {
            if (isSaving) {
              CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Saving PDF…", fontWeight = FontWeight.Bold)
            } else {
              Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(22.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Save PDF to Device", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Secondary Action buttons: Save As, Share, View
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // Save As (Pick Folder / SAF)
            FilledTonalButton(
              onClick = {
                val sanitized = tableGrid.title.replace(Regex("[^a-zA-Z0-9_-]"), "_")
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                savePdfLauncher.launch("${sanitized}_$timestamp.pdf")
              },
              modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .testTag("save_as_pdf_button"),
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(Icons.Filled.FolderOpen, contentDescription = null, modifier = Modifier.size(17.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Save As…", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            // Share PDF
            FilledTonalButton(
              onClick = { sharePdfFile() },
              modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .testTag("share_pdf_button"),
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(17.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Share", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            // Open / View
            OutlinedButton(
              onClick = { viewPdfFile() },
              modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .testTag("view_pdf_button"),
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(17.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("View", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // PDF Page Orientation Options
      Text(
        text = "Page Orientation",
        fontWeight = FontWeight.SemiBold,
        style = MaterialTheme.typography.bodyMedium
      )
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Portrait
        OrientationCard(
          title = "Portrait",
          subtitle = "Standard vertical",
          icon = Icons.Filled.StayCurrentPortrait,
          isSelected = orientation == PageOrientation.PORTRAIT,
          onClick = { orientation = PageOrientation.PORTRAIT },
          modifier = Modifier.weight(1f).testTag("orientation_portrait")
        )

        // Landscape
        OrientationCard(
          title = "Landscape",
          subtitle = "Best for wide tables",
          icon = Icons.Filled.StayCurrentLandscape,
          isSelected = orientation == PageOrientation.LANDSCAPE,
          onClick = { orientation = PageOrientation.LANDSCAPE },
          modifier = Modifier.weight(1f).testTag("orientation_landscape")
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Page Size Chips
      Text(
        text = "Page Size",
        fontWeight = FontWeight.SemiBold,
        style = MaterialTheme.typography.bodyMedium
      )
      Spacer(modifier = Modifier.height(8.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        FilterChip(
          selected = pageSize == PageSizePreset.A4,
          onClick = { pageSize = PageSizePreset.A4 },
          label = { Text("A4 Standard (210 × 297 mm)") },
          leadingIcon = if (pageSize == PageSizePreset.A4) {
            { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
          } else null,
          modifier = Modifier.testTag("pagesize_a4")
        )

        FilterChip(
          selected = pageSize == PageSizePreset.LETTER,
          onClick = { pageSize = PageSizePreset.LETTER },
          label = { Text("US Letter (8.5 × 11 in)") },
          leadingIcon = if (pageSize == PageSizePreset.LETTER) {
            { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
          } else null,
          modifier = Modifier.testTag("pagesize_letter")
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Document Options Switches
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          // Include Title Switch
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(text = "Include Table Title", style = MaterialTheme.typography.bodyMedium)
            Switch(checked = includeTitle, onCheckedChange = { includeTitle = it })
          }

          // Include Date/Timestamp Switch
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(text = "Include Date & Time Stamp", style = MaterialTheme.typography.bodyMedium)
            Switch(checked = includeTimestamp, onCheckedChange = { includeTimestamp = it })
          }

          // Include Page Numbers Switch
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(text = "Include Page Numbers", style = MaterialTheme.typography.bodyMedium)
            Switch(checked = includePageNumbers, onCheckedChange = { includePageNumbers = it })
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
fun OrientationCard(
  title: String,
  subtitle: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
    border = androidx.compose.foundation.BorderStroke(
      width = if (isSelected) 2.dp else 1.dp,
      color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    ),
    modifier = modifier.clickable { onClick() }
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(24.dp)
      )
      Spacer(modifier = Modifier.width(10.dp))
      Column {
        Text(
          text = title,
          fontWeight = FontWeight.SemiBold,
          style = MaterialTheme.typography.bodyMedium,
          color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.labelSmall,
          color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}
