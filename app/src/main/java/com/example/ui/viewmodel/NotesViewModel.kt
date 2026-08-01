package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.FolderEntity
import com.example.data.model.NoteEntity
import com.example.data.preferences.UserPreferencesManager
import com.example.data.repository.NotesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class NotesFilter {
    ALL,
    STARRED,
    LOCKED,
    ARCHIVE,
    TRASH,
    FOLDER
}

data class NotesUiState(
    val filter: NotesFilter = NotesFilter.ALL,
    val selectedFolderId: Long? = null,
    val searchQuery: String = "",
    val isGridView: Boolean = true,
    val notes: List<NoteEntity> = emptyList(),
    val folders: List<FolderEntity> = emptyList(),
    val language: String = "en",
    val isAppLockEnabled: Boolean = false,
    val hasPasswordSet: Boolean = false,
    val appTheme: String = "system",
    val activeNotesCount: Int = 0,
    val unlockedNoteIds: Set<Long> = emptySet(),
    val isAppUnlocked: Boolean = false
)

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NotesRepository
    val userPrefs: UserPreferencesManager

    private val _filter = MutableStateFlow(NotesFilter.ALL)
    private val _selectedFolderId = MutableStateFlow<Long?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _unlockedNoteIds = MutableStateFlow<Set<Long>>(emptySet())
    private val _isAppUnlocked = MutableStateFlow(false)

    init {
        val db = AppDatabase.getDatabase(application)
        repository = NotesRepository(db.noteDao(), db.folderDao())
        userPrefs = UserPreferencesManager(application)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val filteredNotesFlow: Flow<List<NoteEntity>> = combine(
        _filter,
        _selectedFolderId,
        _searchQuery
    ) { filter, folderId, query ->
        Triple(filter, folderId, query)
    }.flatMapLatest { (filter, folderId, query) ->
        if (query.isNotBlank()) {
            repository.searchNotes(query)
        } else {
            when (filter) {
                NotesFilter.ALL -> repository.allActiveNotes
                NotesFilter.STARRED -> repository.favoriteNotes
                NotesFilter.ARCHIVE -> repository.archivedNotes
                NotesFilter.TRASH -> repository.trashedNotes
                NotesFilter.LOCKED -> repository.lockedNotes
                NotesFilter.FOLDER -> folderId?.let { repository.getNotesByFolder(it) } ?: repository.allActiveNotes
            }
        }
    }

    val uiState: StateFlow<NotesUiState> = combine(
        filteredNotesFlow,
        repository.allFolders,
        userPrefs.languageFlow,
        userPrefs.isAppLockEnabledFlow,
        userPrefs.hasPasswordSetFlow,
        userPrefs.appThemeFlow,
        userPrefs.isGridViewFlow,
        repository.activeNotesCount,
        _filter,
        _selectedFolderId,
        _searchQuery,
        _unlockedNoteIds,
        _isAppUnlocked
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val notes = args[0] as List<NoteEntity>
        val folders = args[1] as List<FolderEntity>
        val language = args[2] as String
        val appLockEnabled = args[3] as Boolean
        val hasPassword = args[4] as Boolean
        val appTheme = args[5] as String
        val isGrid = args[6] as Boolean
        val count = args[7] as Int
        val filter = args[8] as NotesFilter
        val selectedFolderId = args[9] as Long?
        val searchQuery = args[10] as String
        val unlockedNoteIds = args[11] as Set<Long>
        val isAppUnlocked = args[12] as Boolean

        NotesUiState(
            filter = filter,
            selectedFolderId = selectedFolderId,
            searchQuery = searchQuery,
            isGridView = isGrid,
            notes = notes,
            folders = folders,
            language = language,
            isAppLockEnabled = appLockEnabled,
            hasPasswordSet = hasPassword,
            appTheme = appTheme,
            activeNotesCount = count,
            unlockedNoteIds = unlockedNoteIds,
            isAppUnlocked = isAppUnlocked
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NotesUiState()
    )

    fun setFilter(filter: NotesFilter, folderId: Long? = null) {
        _filter.value = filter
        _selectedFolderId.value = folderId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleGridView() {
        viewModelScope.launch {
            userPrefs.setGridView(!uiState.value.isGridView)
        }
    }

    fun saveNote(note: NoteEntity) {
        viewModelScope.launch {
            if (note.id == 0L) {
                repository.insertNote(note)
            } else {
                repository.updateNote(note.copy(updatedAt = System.currentTimeMillis()))
            }
        }
    }

    fun moveToTrash(note: NoteEntity) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isTrashed = true, isPinned = false))
        }
    }

    fun restoreFromTrash(note: NoteEntity) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isTrashed = false))
        }
    }

    fun deleteNotePermanently(noteId: Long) {
        viewModelScope.launch {
            repository.deleteNotePermanently(noteId)
        }
    }

    fun clearTrash() {
        viewModelScope.launch {
            repository.clearTrash()
        }
    }

    fun togglePin(note: NoteEntity) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isPinned = !note.isPinned))
        }
    }

    fun toggleFavorite(note: NoteEntity) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isFavorite = !note.isFavorite))
        }
    }

    fun toggleLock(note: NoteEntity) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isLocked = !note.isLocked))
        }
    }

    fun toggleArchive(note: NoteEntity) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isArchived = !note.isArchived))
        }
    }

    fun createFolder(name: String, colorHex: String = "#4F46E5", iconName: String = "folder") {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.insertFolder(FolderEntity(name = name, colorHex = colorHex, iconName = iconName))
        }
    }

    fun deleteFolder(folderId: Long) {
        viewModelScope.launch {
            repository.deleteFolder(folderId)
            if (_selectedFolderId.value == folderId) {
                setFilter(NotesFilter.ALL)
            }
        }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch {
            userPrefs.setLanguage(language)
        }
    }

    fun setAppTheme(theme: String) {
        viewModelScope.launch {
            userPrefs.setAppTheme(theme)
        }
    }

    suspend fun setPassword(password: String): Boolean {
        return userPrefs.setPassword(password)
    }

    fun removePassword() {
        viewModelScope.launch {
            userPrefs.removePassword()
        }
    }

    suspend fun verifyPassword(password: String): Boolean {
        return userPrefs.verifyPassword(password)
    }

    fun unlockNoteInSession(noteId: Long) {
        _unlockedNoteIds.value = _unlockedNoteIds.value + noteId
    }

    fun unlockAppInSession() {
        _isAppUnlocked.value = true
    }

    fun getNoteFlow(noteId: Long): Flow<NoteEntity?> {
        return repository.getNoteById(noteId)
    }
}
