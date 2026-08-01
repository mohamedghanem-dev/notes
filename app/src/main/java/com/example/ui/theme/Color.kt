package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Primary Indigo / Slate Palette
val PrimaryIndigo = Color(0xFF4F46E5)
val OnPrimaryIndigo = Color(0xFFFFFFFF)
val PrimaryContainerIndigo = Color(0xFFEEF2FF)
val OnPrimaryContainerIndigo = Color(0xFF312E81)

val SecondarySlate = Color(0xFF475569)
val OnSecondarySlate = Color(0xFFFFFFFF)
val SecondaryContainerSlate = Color(0xFFF1F5F9)
val OnSecondaryContainerSlate = Color(0xFF0F172A)

val DarkBackground = Color(0xFF0F172A)
val DarkSurface = Color(0xFF1E293B)
val DarkSurfaceVariant = Color(0xFF334155)

val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF1F5F9)

// Note Background Color Presets (Clean pastel & dark tones)
object NoteColors {
    val DefaultLight = Color(0xFFFFFFFF)
    val DefaultDark = Color(0xFF1E293B)

    val Presets = listOf(
        NoteColorOption("#FFFFFF", Color(0xFFFFFFFF), Color(0xFF1E293B), "Default"),
        NoteColorOption("#FEF3C7", Color(0xFFFEF3C7), Color(0xFF78350F), "Warm Amber"),
        NoteColorOption("#D1FAE5", Color(0xFFD1FAE5), Color(0xFF065F46), "Soft Mint"),
        NoteColorOption("#E0F2FE", Color(0xFFE0F2FE), Color(0xFF075985), "Sky Blue"),
        NoteColorOption("#EDE9FE", Color(0xFFEDE9FE), Color(0xFF5B21B6), "Lavender"),
        NoteColorOption("#FCE7F3", Color(0xFFFCE7F3), Color(0xFF9D174D), "Soft Rose"),
        NoteColorOption("#FFEDD5", Color(0xFFFFEDD5), Color(0xFF9A3412), "Peach"),
        NoteColorOption("#334155", Color(0xFF334155), Color(0xFFF8FAFC), "Charcoal")
    )
}

data class NoteColorOption(
    val hex: String,
    val color: Color,
    val textColor: Color,
    val name: String
)
