package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.TableGrid
import com.example.model.TextAlignType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("GridCraft", appName)
  }

  @Test
  fun `verify table grid creation and cell manipulation`() {
    val grid = TableGrid(title = "Schedule", rowCount = 4, colCount = 3)
    assertEquals(4, grid.rowCount)
    assertEquals(3, grid.colCount)

    val updated = grid.updateCell(1, 1) { cell ->
      cell.copy(text = "Hello Grid", bgColor = 0xFFDBEAFE, isBold = true, textAlign = TextAlignType.CENTER)
    }

    val cell = updated.getCell(1, 1)
    assertEquals("Hello Grid", cell.text)
    assertEquals(0xFFDBEAFE, cell.bgColor)
    assertEquals(true, cell.isBold)
    assertEquals(TextAlignType.CENTER, cell.textAlign)
  }
}
