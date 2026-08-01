package com.example.ui.screens

import android.text.format.DateFormat
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FolderEntity
import com.example.data.model.NoteEntity
import com.example.ui.components.NavigationDrawerContent
import com.example.ui.components.parseColor
import com.example.ui.viewmodel.NotesFilter
import com.example.ui.viewmodel.NotesUiState
import com.example.util.LocalAppStrings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    uiState: NotesUiState,
    onOpenDrawer: () -> Unit,
    onNoteClick: (Long) -> Unit,
    onCreateNoteClick: () -> Unit,
    onFilterSelect: (NotesFilter, Long?) -> Unit,
    onCreateFolder: (String, String) -> Unit,
    onSettingsClick: () -> Unit,
    onTogglePin: (NoteEntity) -> Unit,
    onToggleFavorite: (NoteEntity) -> Unit,
    onToggleLock: (NoteEntity) -> Unit,
    onToggleArchive: (NoteEntity) -> Unit = {},
    onMoveToTrash: (NoteEntity) -> Unit,
    onRestoreFromTrash: (NoteEntity) -> Unit,
    onDeletePermanently: (Long) -> Unit,
    onClearTrash: () -> Unit,
    onToggleGridView: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    var isSearchActive by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }

    val pageTitle = when (uiState.filter) {
        NotesFilter.ALL -> strings.allNotes
        NotesFilter.STARRED -> strings.starred
        NotesFilter.LOCKED -> strings.lockedNotes
        NotesFilter.ARCHIVE -> strings.archive
        NotesFilter.TRASH -> strings.trash
        NotesFilter.FOLDER -> {
            uiState.folders.find { it.id == uiState.selectedFolderId }?.name ?: strings.folders
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = { Text(strings.searchPlaceholder, fontSize = 14.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 8.dp)
                                .testTag("search_text_field"),
                            shape = RoundedCornerShape(20.dp),
                            trailingIcon = {
                                if (uiState.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { onSearchQueryChange("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                    }
                                }
                            }
                        )
                    } else {
                        Text(
                            text = pageTitle,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.testTag("open_drawer_button")
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = "Open Drawer")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            isSearchActive = !isSearchActive
                            if (!isSearchActive) onSearchQueryChange("")
                        },
                        modifier = Modifier.testTag("toggle_search_button")
                    ) {
                        Icon(
                            imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }

                    IconButton(
                        onClick = onToggleGridView,
                        modifier = Modifier.testTag("toggle_view_mode_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isGridView) Icons.Default.List else Icons.Default.GridView,
                            contentDescription = "Toggle View Mode"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.filter != NotesFilter.TRASH) {
                FloatingActionButton(
                    onClick = onCreateNoteClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("create_note_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Create Note",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Trash Banner
            if (uiState.filter == NotesFilter.TRASH && uiState.notes.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.trash,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        TextButton(
                            onClick = onClearTrash,
                            modifier = Modifier.testTag("empty_trash_button")
                        ) {
                            Text(
                                strings.clearTrash,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (uiState.notes.isEmpty()) {
                // Empty State Layout
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (uiState.filter) {
                                    NotesFilter.STARRED -> Icons.Default.Star
                                    NotesFilter.LOCKED -> Icons.Default.Lock
                                    NotesFilter.TRASH -> Icons.Default.Delete
                                    else -> Icons.Default.StickyNote2
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = strings.noNotesFound,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = strings.createFirstNote,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                if (uiState.isGridView) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.notes, key = { it.id }) { note ->
                            SwipeableNoteItem(
                                note = note,
                                onMoveToTrash = { onMoveToTrash(note) },
                                onRestoreFromTrash = { onRestoreFromTrash(note) },
                                onDeletePermanently = { onDeletePermanently(note.id) },
                                onToggleArchive = { onToggleArchive(note) }
                            ) {
                                NoteCardItem(
                                    note = note,
                                    folder = uiState.folders.find { it.id == note.folderId },
                                    isAppLockEnabled = uiState.isAppLockEnabled,
                                    isUnlockedInSession = uiState.unlockedNoteIds.contains(note.id),
                                    onClick = { onNoteClick(note.id) },
                                    onTogglePin = { onTogglePin(note) },
                                    onToggleFavorite = { onToggleFavorite(note) },
                                    onToggleLock = { onToggleLock(note) },
                                    onToggleArchive = { onToggleArchive(note) },
                                    onMoveToTrash = { onMoveToTrash(note) },
                                    onRestoreFromTrash = { onRestoreFromTrash(note) },
                                    onDeletePermanently = { onDeletePermanently(note.id) }
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.notes, key = { it.id }) { note ->
                            SwipeableNoteItem(
                                note = note,
                                onMoveToTrash = { onMoveToTrash(note) },
                                onRestoreFromTrash = { onRestoreFromTrash(note) },
                                onDeletePermanently = { onDeletePermanently(note.id) },
                                onToggleArchive = { onToggleArchive(note) }
                            ) {
                                NoteCardItem(
                                    note = note,
                                    folder = uiState.folders.find { it.id == note.folderId },
                                    isAppLockEnabled = uiState.isAppLockEnabled,
                                    isUnlockedInSession = uiState.unlockedNoteIds.contains(note.id),
                                    onClick = { onNoteClick(note.id) },
                                    onTogglePin = { onTogglePin(note) },
                                    onToggleFavorite = { onToggleFavorite(note) },
                                    onToggleLock = { onToggleLock(note) },
                                    onToggleArchive = { onToggleArchive(note) },
                                    onMoveToTrash = { onMoveToTrash(note) },
                                    onRestoreFromTrash = { onRestoreFromTrash(note) },
                                    onDeletePermanently = { onDeletePermanently(note.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Create Folder Dialog
    if (showCreateFolderDialog) {
        AlertDialog(
            onDismissRequest = { showCreateFolderDialog = false },
            title = { Text(strings.createFolder) },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    label = { Text(strings.folderName) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_folder_name_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            onCreateFolder(newFolderName, "#4F46E5")
                            newFolderName = ""
                            showCreateFolderDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_create_folder")
                ) {
                    Text(strings.save)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCreateFolderDialog = false },
                    modifier = Modifier.testTag("cancel_create_folder")
                ) {
                    Text(strings.cancel)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableNoteItem(
    note: NoteEntity,
    onMoveToTrash: () -> Unit,
    onRestoreFromTrash: () -> Unit,
    onDeletePermanently: () -> Unit,
    onToggleArchive: () -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    if (note.isTrashed) {
                        onRestoreFromTrash()
                    } else {
                        onToggleArchive()
                    }
                    false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    if (note.isTrashed) {
                        onDeletePermanently()
                    } else {
                        onMoveToTrash()
                    }
                    false
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val isStartToEnd = direction == SwipeToDismissBoxValue.StartToEnd
            val isEndToStart = direction == SwipeToDismissBoxValue.EndToStart

            val bgColor = when {
                isStartToEnd -> if (note.isTrashed) Color(0xFF10B981) else Color(0xFF3B82F6)
                isEndToStart -> Color(0xFFEF4444)
                else -> Color.Transparent
            }

            val icon = when {
                isStartToEnd -> if (note.isTrashed) Icons.Default.RestoreFromTrash else if (note.isArchived) Icons.Default.Unarchive else Icons.Default.Archive
                isEndToStart -> if (note.isTrashed) Icons.Default.DeleteForever else Icons.Default.Delete
                else -> null
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(bgColor)
                    .padding(horizontal = 20.dp),
                contentAlignment = if (isStartToEnd) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        },
        content = {
            content()
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCardItem(
    note: NoteEntity,
    folder: FolderEntity?,
    isAppLockEnabled: Boolean,
    isUnlockedInSession: Boolean,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleLock: () -> Unit,
    onToggleArchive: () -> Unit = {},
    onMoveToTrash: () -> Unit,
    onRestoreFromTrash: () -> Unit,
    onDeletePermanently: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    var showMenu by remember { mutableStateOf(false) }

    val bgColor = try {
        Color(android.graphics.Color.parseColor(note.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.surface
    }

    // Determine contrast content color
    val contentColor = if (note.colorHex.equals("#334155", ignoreCase = true)) Color.White else Color(0xFF1E293B)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (note.isPinned) 2.dp else 1.dp,
                color = if (note.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu = true }
            )
            .testTag("note_item_${note.id}"),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row (Badges & Icons)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (folder != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = parseColor(folder.colorHex).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = folder.name,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = parseColor(folder.colorHex),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (note.isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    if (note.isFavorite) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Starred",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    if (note.isLocked) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color(0xFFE11D48),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More",
                                tint = contentColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            if (!note.isTrashed) {
                                DropdownMenuItem(
                                    text = { Text(if (note.isPinned) strings.unpinNote else strings.pinNote) },
                                    leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null) },
                                    onClick = {
                                        onTogglePin()
                                        showMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(strings.starred) },
                                    leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) },
                                    onClick = {
                                        onToggleFavorite()
                                        showMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (note.isLocked) strings.unlockNote else strings.lockNote) },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                    onClick = {
                                        onToggleLock()
                                        showMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (note.isArchived) strings.unarchiveNote else strings.archiveNote) },
                                    leadingIcon = { Icon(if (note.isArchived) Icons.Default.Unarchive else Icons.Default.Archive, contentDescription = null) },
                                    onClick = {
                                        onToggleArchive()
                                        showMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(strings.delete) },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                                    onClick = {
                                        onMoveToTrash()
                                        showMenu = false
                                    }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text(strings.restore) },
                                    leadingIcon = { Icon(Icons.Default.RestoreFromTrash, contentDescription = null) },
                                    onClick = {
                                        onRestoreFromTrash()
                                        showMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(strings.deletePermanently) },
                                    leadingIcon = { Icon(Icons.Default.DeleteForever, contentDescription = null) },
                                    onClick = {
                                        onDeletePermanently()
                                        showMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            if (note.title.isNotBlank()) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = contentColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Content Snippet or Protected placeholder
            if (note.isLocked && isAppLockEnabled && !isUnlockedInSession) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Protected",
                        tint = Color(0xFFE11D48),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.noteLockedPrompt,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFE11D48)
                    )
                }
            } else {
                if (note.content.isNotBlank()) {
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.85f),
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer (Tags & Date)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (note.tags.isNotBlank()) {
                    Text(
                        text = "#" + note.tags.split(",").firstOrNull()?.trim(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                val dateStr = DateFormat.format("MMM dd", note.updatedAt).toString()
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.6f)
                )
            }
        }
    }
}
