package com.example.data.repository

import com.example.data.dao.FolderDao
import com.example.data.dao.NoteDao
import com.example.data.model.FolderEntity
import com.example.data.model.NoteEntity
import kotlinx.coroutines.flow.Flow

class NotesRepository(
    private val noteDao: NoteDao,
    private val folderDao: FolderDao
) {
    val allActiveNotes: Flow<List<NoteEntity>> = noteDao.getAllActiveNotes()
    val favoriteNotes: Flow<List<NoteEntity>> = noteDao.getFavoriteNotes()
    val archivedNotes: Flow<List<NoteEntity>> = noteDao.getArchivedNotes()
    val trashedNotes: Flow<List<NoteEntity>> = noteDao.getTrashedNotes()
    val lockedNotes: Flow<List<NoteEntity>> = noteDao.getLockedNotes()
    val allFolders: Flow<List<FolderEntity>> = folderDao.getAllFolders()
    val activeNotesCount: Flow<Int> = noteDao.getActiveNotesCount()

    fun getNotesByFolder(folderId: Long): Flow<List<NoteEntity>> = noteDao.getNotesByFolder(folderId)

    fun searchNotes(query: String): Flow<List<NoteEntity>> = noteDao.searchNotes(query)

    fun getNoteById(id: Long): Flow<NoteEntity?> = noteDao.getNoteById(id)

    suspend fun getNoteByIdSync(id: Long): NoteEntity? = noteDao.getNoteByIdSync(id)

    suspend fun insertNote(note: NoteEntity): Long = noteDao.insertNote(note)

    suspend fun updateNote(note: NoteEntity) = noteDao.updateNote(note)

    suspend fun deleteNotePermanently(id: Long) = noteDao.deleteNotePermanently(id)

    suspend fun clearTrash() = noteDao.clearTrash()

    suspend fun insertFolder(folder: FolderEntity): Long = folderDao.insertFolder(folder)

    suspend fun updateFolder(folder: FolderEntity) = folderDao.updateFolder(folder)

    suspend fun deleteFolder(id: Long) = folderDao.deleteFolder(id)
}
