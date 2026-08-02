package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.data.model.NoteEntity
import com.example.ui.components.NavigationDrawerContent
import com.example.ui.screens.LockScreen
import com.example.ui.screens.NoteEditorScreen
import com.example.ui.screens.NotesListScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.NotesAppTheme
import com.example.ui.viewmodel.NotesViewModel
import com.example.util.ProvideAppLanguage
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: NotesViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            NotesAppTheme(appThemeSetting = uiState.appTheme, dynamicColor = false) {
                ProvideAppLanguage(languageCode = uiState.language) {
                    val navController = rememberNavController()
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val coroutineScope = rememberCoroutineScope()

                    var noteToUnlock by remember { mutableStateOf<NoteEntity?>(null) }
                    var showCreateFolderDialog by remember { mutableStateOf(false) }

                    // Global App Lock verification on launch if enabled
                    if (uiState.isAppLockEnabled && uiState.hasPasswordSet && !uiState.isAppUnlocked) {
                        LockScreen(
                            titleText = "Notes Protected",
                            onVerifyPassword = { input -> viewModel.verifyPassword(input) },
                            onSuccess = { viewModel.unlockAppInSession() }
                        )
                    } else {
                        ModalNavigationDrawer(
                            drawerState = drawerState,
                            gesturesEnabled = true,
                            drawerContent = {
                                NavigationDrawerContent(
                                    activeFilter = uiState.filter,
                                    selectedFolderId = uiState.selectedFolderId,
                                    folders = uiState.folders,
                                    activeNotesCount = uiState.activeNotesCount,
                                    onFilterSelect = { filter, folderId ->
                                        viewModel.setFilter(filter, folderId)
                                    },
                                    onCreateFolderClick = {
                                        showCreateFolderDialog = true
                                        coroutineScope.launch { drawerState.close() }
                                    },
                                    onSettingsClick = {
                                        navController.navigate("settings")
                                    },
                                    onCloseDrawer = {
                                        coroutineScope.launch { drawerState.close() }
                                    }
                                )
                            }
                        ) {
                            NavHost(
                                navController = navController,
                                startDestination = "notes_list"
                            ) {
                                // Notes List Screen
                                composable("notes_list") {
                                    NotesListScreen(
                                        uiState = uiState,
                                        onOpenDrawer = {
                                            coroutineScope.launch { drawerState.open() }
                                        },
                                        onNoteClick = { noteId ->
                                            val targetNote = uiState.notes.find { it.id == noteId }
                                            if (targetNote != null && targetNote.isLocked && uiState.isAppLockEnabled && !uiState.unlockedNoteIds.contains(noteId)) {
                                                noteToUnlock = targetNote
                                            } else {
                                                navController.navigate("note_editor/$noteId")
                                            }
                                        },
                                        onCreateNoteClick = {
                                            navController.navigate("note_editor/0")
                                        },
                                        onFilterSelect = { filter, folderId ->
                                            viewModel.setFilter(filter, folderId)
                                        },
                                        onCreateFolder = { name, color ->
                                            viewModel.createFolder(name, color)
                                        },
                                        showCreateFolderDialog = showCreateFolderDialog,
                                        onCreateFolderDialogChange = { showCreateFolderDialog = it },
                                        onSettingsClick = {
                                            navController.navigate("settings")
                                        },
                                        onTogglePin = { note -> viewModel.togglePin(note) },
                                        onToggleFavorite = { note -> viewModel.toggleFavorite(note) },
                                        onToggleLock = { note -> viewModel.toggleLock(note) },
                                        onToggleArchive = { note -> viewModel.toggleArchive(note) },
                                        onMoveToTrash = { note -> viewModel.moveToTrash(note) },
                                        onRestoreFromTrash = { note -> viewModel.restoreFromTrash(note) },
                                        onDeletePermanently = { id -> viewModel.deleteNotePermanently(id) },
                                        onClearTrash = { viewModel.clearTrash() },
                                        onToggleGridView = { viewModel.toggleGridView() },
                                        onSearchQueryChange = { query -> viewModel.setSearchQuery(query) }
                                    )
                                }

                                // Note Editor Screen
                                composable(
                                    route = "note_editor/{noteId}",
                                    arguments = listOf(navArgument("noteId") { type = NavType.LongType })
                                ) { backStackEntry ->
                                    val noteId = backStackEntry.arguments?.getLong("noteId") ?: 0L
                                    val existingNote = if (noteId != 0L) {
                                        uiState.notes.find { it.id == noteId }
                                    } else null

                                    NoteEditorScreen(
                                        note = existingNote,
                                        initialFolderId = uiState.selectedFolderId,
                                        folders = uiState.folders,
                                        onBackClick = { navController.popBackStack() },
                                        onSaveClick = { noteToSave -> viewModel.saveNote(noteToSave) },
                                        onDeleteClick = { id -> viewModel.moveToTrash(existingNote ?: return@NoteEditorScreen) }
                                    )
                                }

                                // Settings Screen
                                composable("settings") {
                                    SettingsScreen(
                                        uiState = uiState,
                                        onBackClick = { navController.popBackStack() },
                                        onLanguageChange = { lang -> viewModel.setLanguage(lang) },
                                        onThemeChange = { theme -> viewModel.setAppTheme(theme) },
                                        onSetPassword = { pass -> viewModel.setPassword(pass) },
                                        onRemovePassword = { viewModel.removePassword() },
                                        onClearTrash = { viewModel.clearTrash() }
                                    )
                                }
                            }
                        }

                        // Unlock note dialog if clicking a locked note
                        noteToUnlock?.let { lockedNote ->
                            LockScreen(
                                titleText = "Protected Note",
                                onVerifyPassword = { input -> viewModel.verifyPassword(input) },
                                onSuccess = {
                                    viewModel.unlockNoteInSession(lockedNote.id)
                                    val id = lockedNote.id
                                    noteToUnlock = null
                                    navController.navigate("note_editor/$id")
                                },
                                onCancel = { noteToUnlock = null }
                            )
                        }
                    }
                }
            }
        }
    }
}
