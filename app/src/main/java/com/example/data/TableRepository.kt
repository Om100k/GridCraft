package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.TableCell
import com.example.model.TableGrid
import com.example.model.TextAlignType
import org.json.JSONArray
import org.json.JSONObject

class TableRepository(context: Context) {
  private val prefs: SharedPreferences = context.getSharedPreferences("gridcraft_tables", Context.MODE_PRIVATE)

  fun saveTable(grid: TableGrid) {
    val list = getRecentTables().toMutableList()
    list.removeAll { it.id == grid.id }
    list.add(0, grid)
    val trimmed = list.take(15) // keep up to 15 recent tables
    saveList(trimmed)
  }

  fun getRecentTables(): List<TableGrid> {
    val rawJson = prefs.getString("saved_tables_json", null) ?: return emptyList()
    return try {
      val jsonArray = JSONArray(rawJson)
      val result = mutableListOf<TableGrid>()
      for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        result.add(deserializeTable(obj))
      }
      result
    } catch (e: Exception) {
      emptyList()
    }
  }

  fun deleteTable(tableId: String) {
    val list = getRecentTables().filterNot { it.id == tableId }
    saveList(list)
  }

  private fun saveList(list: List<TableGrid>) {
    val array = JSONArray()
    for (grid in list) {
      array.put(serializeTable(grid))
    }
    prefs.edit().putString("saved_tables_json", array.toString()).apply()
  }

  private fun serializeTable(grid: TableGrid): JSONObject {
    val obj = JSONObject()
    obj.put("id", grid.id)
    obj.put("title", grid.title)
    obj.put("rowCount", grid.rowCount)
    obj.put("colCount", grid.colCount)
    obj.put("lastModified", grid.lastModified)

    val cellsArray = JSONArray()
    for ((_, cell) in grid.cells) {
      val cellObj = JSONObject()
      cellObj.put("row", cell.row)
      cellObj.put("col", cell.col)
      cellObj.put("text", cell.text)
      cellObj.put("bgColor", cell.bgColor)
      cellObj.put("textColor", cell.textColor)
      cellObj.put("isBold", cell.isBold)
      cellObj.put("isItalic", cell.isItalic)
      cellObj.put("textAlign", cell.textAlign.name)
      cellsArray.put(cellObj)
    }
    obj.put("cells", cellsArray)
    return obj
  }

  private fun deserializeTable(obj: JSONObject): TableGrid {
    val id = obj.optString("id")
    val title = obj.optString("title", "Untitled Table")
    val rowCount = obj.optInt("rowCount", 5)
    val colCount = obj.optInt("colCount", 4)
    val lastModified = obj.optLong("lastModified", System.currentTimeMillis())

    val cellsMap = mutableMapOf<String, TableCell>()
    val cellsArray = obj.optJSONArray("cells")
    if (cellsArray != null) {
      for (i in 0 until cellsArray.length()) {
        val cellObj = cellsArray.getJSONObject(i)
        val row = cellObj.optInt("row")
        val col = cellObj.optInt("col")
        val text = cellObj.optString("text", "")
        val bgColor = cellObj.optLong("bgColor", 0xFFFFFFFF)
        val textColor = cellObj.optLong("textColor", 0xFF0F172A)
        val isBold = cellObj.optBoolean("isBold", false)
        val isItalic = cellObj.optBoolean("isItalic", false)
        val alignStr = cellObj.optString("textAlign", "CENTER")
        val align = try { TextAlignType.valueOf(alignStr) } catch (e: Exception) { TextAlignType.CENTER }

        val cell = TableCell(
          row = row,
          col = col,
          text = text,
          bgColor = bgColor,
          textColor = textColor,
          isBold = isBold,
          isItalic = isItalic,
          textAlign = align
        )
        cellsMap["$row,$col"] = cell
      }
    }

    return TableGrid(
      id = id,
      title = title,
      rowCount = rowCount,
      colCount = colCount,
      cells = cellsMap,
      lastModified = lastModified
    )
  }
}
