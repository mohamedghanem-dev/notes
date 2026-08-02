package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FolderEntity
import com.example.data.model.NoteEntity
import com.example.ui.components.ColorPickerModal
import com.example.ui.components.parseColor
import com.example.util.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    note: NoteEntity?,
    initialFolderId: Long?,
    folders: List<FolderEntity>,
    onBackClick: () -> Unit,
    onSaveClick: (NoteEntity) -> Unit,
    onDeleteClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current

    var title by remember(note) { mutableStateOf(note?.title ?: "") }
    var content by remember(note) { mutableStateOf(note?.content ?: "") }
    var folderId by remember(note, initialFolderId) { mutableLongStateOf(note?.folderId ?: initialFolderId ?: 0L) }
    var isPinned by remember(note) { mutableStateOf(note?.isPinned ?: false) }
    var isFavorite by remember(note) { mutableStateOf(note?.isFavorite ?: false) }
    var isLocked by remember(note) { mutableStateOf(note?.isLocked ?: false) }
    var colorHex by remember(note) { mutableStateOf(note?.colorHex ?: "#FFFFFF") }
    var tags by remember(note) { mutableStateOf(note?.tags ?: "") }

    var showColorPicker by remember { mutableStateOf(false) }
    var showTagDialog by remember { mutableStateOf(false) }
    var showFolderMenu by remember { mutableStateOf(false) }
    var newTagInput by remember { mutableStateOf("") }
    var showMoreMenu by remember { mutableStateOf(false) }

    // History for Undo/Redo
    val undoStack = remember { mutableStateListOf<Pair<String, String>>() }
    val redoStack = remember { mutableStateListOf<Pair<String, String>>() }

    fun updateContent(newContent: String) {
        undoStack.add(Pair(title, content))
        content = newContent
        redoStack.clear()
    }

    // Builds the note from current editor state and saves it if there's anything to save.
    // Used by: the back arrow, the system/gesture back action, and the small save button —
    // so a note is never lost no matter how the user leaves the screen.
    fun performSave(showToast: Boolean = false) {
        val currentNote = (note ?: NoteEntity()).copy(
            title = title,
            content = content,
            folderId = folderId,
            isPinned = isPinned,
            isFavorite = isFavorite,
            isLocked = isLocked,
            colorHex = colorHex,
            tags = tags
        )
        if (title.isNotBlank() || content.isNotBlank()) {
            onSaveClick(currentNote)
            if (showToast) Toast.makeText(context, strings.save, Toast.LENGTH_SHORT).show()
        }
    }

    // Catches the system back button AND the edge swipe-back gesture — without this,
    // swiping out of the note quickly used to discard unsaved changes silently.
    BackHandler {
        performSave()
        onBackClick()
    }

    val selectedFolder = folders.find { it.id == folderId }

    val editorBgColor = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.background
    }

    val contentColor = if (colorHex.equals("#334155", ignoreCase = true)) Color.White else MaterialTheme.colorScheme.onBackground

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = editorBgColor,
        topBar = {
            TopAppBar(
                title = {
                    Box {
                        Surface(
                            onClick = { showFolderMenu = true },
                            shape = RoundedCornerShape(20.dp),
                            color = if (selectedFolder != null) parseColor(selectedFolder.colorHex).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.testTag("folder_selector_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = if (selectedFolder != null) parseColor(selectedFolder.colorHex) else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = selectedFolder?.name ?: strings.folders,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (selectedFolder != null) parseColor(selectedFolder.colorHex) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showFolderMenu,
                            onDismissRequest = { showFolderMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(strings.allNotes) },
                                onClick = {
                                    folderId = 0L
                                    showFolderMenu = false
                                }
                            )
                            folders.forEach { folder ->
                                DropdownMenuItem(
                                    text = { Text(folder.name) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Folder,
                                            contentDescription = null,
                                            tint = parseColor(folder.colorHex)
                                        )
                                    },
                                    onClick = {
                                        folderId = folder.id
                                        showFolderMenu = false
                                    }
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            performSave()
                            onBackClick()
                        },
                        modifier = Modifier.testTag("editor_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = contentColor
                        )
                    }
                },
                actions = {
                    // Small explicit save button — saves in place without leaving the note.
                    IconButton(
                        onClick = { performSave(showToast = true) },
                        modifier = Modifier.testTag("editor_save_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = strings.save,
                            tint = contentColor
                        )
                    }

                    IconButton(onClick = { isPinned = !isPinned }) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = strings.pinNote,
                            tint = if (isPinned) MaterialTheme.colorScheme.primary else contentColor.copy(alpha = 0.6f)
                        )
                    }

                    IconButton(onClick = { isFavorite = !isFavorite }) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = strings.starred,
                            tint = if (isFavorite) Color(0xFFF59E0B) else contentColor.copy(alpha = 0.6f)
                        )
                    }

                    IconButton(onClick = { isLocked = !isLocked }) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = strings.lockNote,
                            tint = if (isLocked) Color(0xFFE11D48) else contentColor.copy(alpha = 0.6f)
                        )
                    }

                    Box {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More",
                                tint = contentColor
                            )
                        }

                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(strings.shareNote) },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                                onClick = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "$title\n\n$content")
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, strings.shareNote))
                                    showMoreMenu = false
                                }
                            )

                            if (note != null) {
                                DropdownMenuItem(
                                    text = { Text(strings.delete) },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                                    onClick = {
                                        onDeleteClick(note.id)
                                        showMoreMenu = false
                                        onBackClick()
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            // Bottom Editor Toolbar (Matching screenshot 1 layout)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left group: Palette, Tag
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { showColorPicker = true },
                            modifier = Modifier.testTag("color_picker_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = strings.colorPalette,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(
                            onClick = { showTagDialog = true },
                            modifier = Modifier.testTag("add_tag_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tag,
                                contentDescription = strings.addTag,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Formatting group: Undo, Redo, Checkbox list
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                if (undoStack.isNotEmpty()) {
                                    val last = undoStack.removeAt(undoStack.lastIndex)
                                    redoStack.add(Pair(title, content))
                                    title = last.first
                                    content = last.second
                                }
                            },
                            enabled = undoStack.isNotEmpty(),
                            modifier = Modifier.testTag("undo_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Undo,
                                contentDescription = "Undo",
                                tint = if (undoStack.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        }

                        IconButton(
                            onClick = {
                                if (redoStack.isNotEmpty()) {
                                    val next = redoStack.removeAt(redoStack.lastIndex)
                                    undoStack.add(Pair(title, content))
                                    title = next.first
                                    content = next.second
                                }
                            },
                            enabled = redoStack.isNotEmpty(),
                            modifier = Modifier.testTag("redo_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Redo,
                                contentDescription = "Redo",
                                tint = if (redoStack.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        }

                        IconButton(
                            onClick = {
                                updateContent(content + "\n• ")
                            },
                            modifier = Modifier.testTag("bullet_list_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatListBulleted,
                                contentDescription = "Bullet List"
                            )
                        }

                        IconButton(
                            onClick = {
                                updateContent(content + "\n[ ] ")
                            },
                            modifier = Modifier.testTag("check_list_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Checklist,
                                contentDescription = "Checklist"
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Tag Chips Bar if tags present
            if (tags.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tags.split(",").filter { it.isNotBlank() }.forEach { tag ->
                        AssistChip(
                            onClick = {},
                            label = { Text("#$tag", fontSize = 12.sp) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove tag",
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable {
                                            tags = tags
                                                .split(",")
                                                .filter { it.trim() != tag.trim() }
                                                .joinToString(",")
                                        }
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Title Field
            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = {
                    Text(
                        text = strings.noteTitle,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = contentColor.copy(alpha = 0.4f)
                        )
                    )
                },
                textStyle = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = contentColor,
                    unfocusedTextColor = contentColor,
                    cursorColor = contentColor
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("note_title_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Body Content Field
            TextField(
                value = content,
                onValueChange = { updateContent(it) },
                placeholder = {
                    Text(
                        text = strings.noteContent,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = contentColor.copy(alpha = 0.4f)
                        )
                    )
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = contentColor,
                    lineHeight = 24.sp
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = contentColor,
                    unfocusedTextColor = contentColor,
                    cursorColor = contentColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("note_content_input")
            )
        }
    }

    // Modals
    if (showColorPicker) {
        ColorPickerModal(
            selectedHex = colorHex,
            onColorSelect = { hex -> colorHex = hex },
            onDismiss = { showColorPicker = false }
        )
    }

    if (showTagDialog) {
        AlertDialog(
            onDismissRequest = { showTagDialog = false },
            title = { Text(strings.addTag) },
            text = {
                OutlinedTextField(
                    value = newTagInput,
                    onValueChange = { newTagInput = it },
                    label = { Text(strings.tags) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("tag_input_field")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTagInput.isNotBlank()) {
                            tags = if (tags.isBlank()) newTagInput.trim() else "$tags,${newTagInput.trim()}"
                            newTagInput = ""
                            showTagDialog = false
                        }
                    },
                    modifier = Modifier.testTag("save_tag_button")
                ) {
                    Text(strings.save)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showTagDialog = false },
                    modifier = Modifier.testTag("cancel_tag_button")
                ) {
                    Text(strings.cancel)
                }
            }
        )
    }
}
