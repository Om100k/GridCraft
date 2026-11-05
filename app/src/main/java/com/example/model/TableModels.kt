package com.example.model

import java.util.UUID

enum class TextAlignType {
  LEFT,
  CENTER,
  RIGHT
}

data class TableCell(
  val row: Int,
  val col: Int,
  val text: String = "",
  val bgColor: Long = 0xFFFFFFFF,
  val textColor: Long = 0xFF0F172A,
  val isBold: Boolean = false,
  val isItalic: Boolean = false,
  val textAlign: TextAlignType = TextAlignType.CENTER,
  val fontSizeSp: Float = 14f
)

data class TableGrid(
  val id: String = UUID.randomUUID().toString(),
  val title: String = "Untitled Table",
  val rowCount: Int = 5,
  val colCount: Int = 4,
  val cells: Map<String, TableCell> = emptyMap(),
  val lastModified: Long = System.currentTimeMillis()
) {
  fun cellKey(row: Int, col: Int): String = "$row,$col"

  fun getCell(row: Int, col: Int): TableCell {
    return cells[cellKey(row, col)] ?: TableCell(
      row = row,
      col = col,
      bgColor = if (row == 0) 0xFFF1F5F9 else 0xFFFFFFFF,
      textColor = 0xFF0F172A,
      isBold = row == 0
    )
  }

  fun updateCell(row: Int, col: Int, transform: (TableCell) -> TableCell): TableGrid {
    val current = getCell(row, col)
    val updated = transform(current)
    val newCells = cells.toMutableMap()
    newCells[cellKey(row, col)] = updated
    return copy(cells = newCells, lastModified = System.currentTimeMillis())
  }

  fun updateRowColor(row: Int, color: Long, textColor: Long? = null): TableGrid {
    val newCells = cells.toMutableMap()
    for (c in 0 until colCount) {
      val cell = getCell(row, c)
      newCells[cellKey(row, c)] = cell.copy(
        bgColor = color,
        textColor = textColor ?: cell.textColor
      )
    }
    return copy(cells = newCells, lastModified = System.currentTimeMillis())
  }

  fun updateColColor(col: Int, color: Long, textColor: Long? = null): TableGrid {
    val newCells = cells.toMutableMap()
    for (r in 0 until rowCount) {
      val cell = getCell(r, col)
      newCells[cellKey(r, col)] = cell.copy(
        bgColor = color,
        textColor = textColor ?: cell.textColor
      )
    }
    return copy(cells = newCells, lastModified = System.currentTimeMillis())
  }

  fun addRow(): TableGrid {
    return copy(rowCount = rowCount + 1, lastModified = System.currentTimeMillis())
  }

  fun removeRow(): TableGrid {
    if (rowCount <= 1) return this
    val newCells = cells.filterKeys { key ->
      val r = key.substringBefore(",").toIntOrNull() ?: 0
      r < rowCount - 1
    }
    return copy(rowCount = rowCount - 1, cells = newCells, lastModified = System.currentTimeMillis())
  }

  fun addColumn(): TableGrid {
    return copy(colCount = colCount + 1, lastModified = System.currentTimeMillis())
  }

  fun removeColumn(): TableGrid {
    if (colCount <= 1) return this
    val newCells = cells.filterKeys { key ->
      val c = key.substringAfter(",").toIntOrNull() ?: 0
      c < colCount - 1
    }
    return copy(colCount = colCount - 1, cells = newCells, lastModified = System.currentTimeMillis())
  }
}

data class TablePreset(
  val name: String,
  val description: String,
  val rows: Int,
  val cols: Int,
  val headerColor: Long,
  val headerTextColor: Long = 0xFFFFFFFF,
  val initialData: Map<Pair<Int, Int>, String> = emptyMap()
)

object TablePresetProvider {
  val presets = listOf(
    TablePreset(
      name = "Blank Grid",
      description = "Clean blank slate with custom dimensions",
      rows = 5,
      cols = 4,
      headerColor = 0xFF1E3A8A
    ),
    TablePreset(
      name = "Weekly Planner",
      description = "Monday through Friday schedule matrix",
      rows = 6,
      cols = 6,
      headerColor = 0xFF1E293B,
      initialData = mapOf(
        Pair(0, 0) to "Time",
        Pair(0, 1) to "Mon",
        Pair(0, 2) to "Tue",
        Pair(0, 3) to "Wed",
        Pair(0, 4) to "Thu",
        Pair(0, 5) to "Fri",
        Pair(1, 0) to "09:00 AM",
        Pair(2, 0) to "11:00 AM",
        Pair(3, 0) to "01:00 PM",
        Pair(4, 0) to "03:00 PM",
        Pair(5, 0) to "05:00 PM",
      )
    ),
    TablePreset(
      name = "Budget & Expenses",
      description = "Track items, categories, and totals",
      rows = 6,
      cols = 4,
      headerColor = 0xFF065F46,
      initialData = mapOf(
        Pair(0, 0) to "Item",
        Pair(0, 1) to "Category",
        Pair(0, 2) to "Cost ($)",
        Pair(0, 3) to "Status",
        Pair(1, 0) to "Groceries",
        Pair(1, 1) to "Food",
        Pair(1, 2) to "85.50",
        Pair(1, 3) to "Paid",
        Pair(2, 0) to "Electricity",
        Pair(2, 1) to "Utilities",
        Pair(2, 2) to "62.00",
        Pair(2, 3) to "Pending",
      )
    ),
    TablePreset(
      name = "Task Tracker",
      description = "Priority, Assignee, and Status columns",
      rows = 5,
      cols = 4,
      headerColor = 0xFF581C87,
      initialData = mapOf(
        Pair(0, 0) to "Task",
        Pair(0, 1) to "Priority",
        Pair(0, 2) to "Owner",
        Pair(0, 3) to "Status",
        Pair(1, 0) to "Design Review",
        Pair(1, 1) to "High",
        Pair(1, 2) to "Alex",
        Pair(1, 3) to "Done",
        Pair(2, 0) to "App Testing",
        Pair(2, 1) to "Medium",
        Pair(2, 2) to "Sam",
        Pair(2, 3) to "In Progress",
      )
    ),
    TablePreset(
      name = "Inventory Sheet",
      description = "SKU, Description, Qty, and Price",
      rows = 6,
      cols = 4,
      headerColor = 0xFF7F1D1D,
      initialData = mapOf(
        Pair(0, 0) to "SKU",
        Pair(0, 1) to "Product",
        Pair(0, 2) to "In Stock",
        Pair(0, 3) to "Unit Price",
        Pair(1, 0) to "PRD-001",
        Pair(1, 1) to "Notebook",
        Pair(1, 2) to "120",
        Pair(1, 3) to "$4.99",
      )
    )
  )
}
