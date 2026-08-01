package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "",
    val content: String = "",
    val folderId: Long = 0, // 0 = All/Uncategorized
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val isLocked: Boolean = false,
    val isArchived: Boolean = false,
    val isTrashed: Boolean = false,
    val colorHex: String = "#FFFFFF",
    val tags: String = "", // Comma-separated tags
    val drawingData: String? = null, // Optional drawing vector JSON or path
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
