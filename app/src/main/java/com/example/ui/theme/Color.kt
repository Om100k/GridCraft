package com.example.ui.theme

import androidx.compose.ui.graphics.Color

val RoyalBluePrimary = Color(0xFF1E40AF)
val RoyalBlueSecondary = Color(0xFF0284C7)
val RoyalBlueTertiary = Color(0xFFD97706)

val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF1F5F9)
val LightOnSurface = Color(0xFF0F172A)
val LightOnSurfaceVariant = Color(0xFF475569)
val LightOutline = Color(0xFFCBD5E1)

val DarkPrimary = Color(0xFF60A5FA)
val DarkSecondary = Color(0xFF38BDF8)
val DarkTertiary = Color(0xFFFBBF24)
val DarkBackground = Color(0xFF0B0F17)
val DarkSurface = Color(0xFF151D2A)
val DarkSurfaceVariant = Color(0xFF1E293B)
val DarkOnSurface = Color(0xFFF1F5F9)
val DarkOnSurfaceVariant = Color(0xFF94A3B8)
val DarkOutline = Color(0xFF334155)

// Table cell color preset options
object CellColorPalette {
  val defaultColors = listOf(
    CellColorOption("Default White", 0xFFFFFFFF, 0xFF0F172A),
    CellColorOption("Soft Slate", 0xFFF1F5F9, 0xFF0F172A),
    CellColorOption("Light Blue", 0xFFDBEAFE, 0xFF1E3A8A),
    CellColorOption("Sky Blue", 0xFFBAE6FD, 0xFF0369A1),
    CellColorOption("Mint Green", 0xFFD1FAE5, 0xFF065F46),
    CellColorOption("Lime Green", 0xFFDCFCE7, 0xFF14532D),
    CellColorOption("Soft Amber", 0xFFFEF3C7, 0xFF92400E),
    CellColorOption("Warm Peach", 0xFFFFEDD5, 0xFF9A3412),
    CellColorOption("Blush Rose", 0xFFFFE4E6, 0xFF9F1239),
    CellColorOption("Soft Lavender", 0xFFEDE9FE, 0xFF5B21B6),
    CellColorOption("Soft Teal", 0xFFCCFBF1, 0xFF115E59),
    CellColorOption("Header Dark Slate", 0xFF1E293B, 0xFFFFFFFF),
    CellColorOption("Header Royal Navy", 0xFF1E3A8A, 0xFFFFFFFF),
    CellColorOption("Header Deep Emerald", 0xFF064E3B, 0xFFFFFFFF),
    CellColorOption("Header Deep Crimson", 0xFF7F1D1D, 0xFFFFFFFF),
    CellColorOption("Header Purple", 0xFF581C87, 0xFFFFFFFF),
  )
}

data class CellColorOption(
  val name: String,
  val bgColor: Long,
  val suggestedTextColor: Long
)
