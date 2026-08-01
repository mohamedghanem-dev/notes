package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FolderEntity
import com.example.ui.viewmodel.NotesFilter
import com.example.util.LocalAppStrings

@Composable
fun NavigationDrawerContent(
    activeFilter: NotesFilter,
    selectedFolderId: Long?,
    folders: List<FolderEntity>,
    activeNotesCount: Int,
    onFilterSelect: (NotesFilter, Long?) -> Unit,
    onCreateFolderClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current

    ModalDrawerSheet(
        modifier = modifier
            .width(300.dp)
            .fillMaxHeight(),
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp)
        ) {
            // Header Section (Inspired by Proton / Samsung Notes Drawer)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = strings.appName,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = strings.appName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$activeNotesCount ${strings.totalNotes}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onCloseDrawer,
                    modifier = Modifier.testTag("close_drawer_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Drawer",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // Drawer Items List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                item {
                    DrawerNavigationItem(
                        label = strings.allNotes,
                        icon = Icons.Default.Notes,
                        isSelected = activeFilter == NotesFilter.ALL,
                        count = activeNotesCount,
                        testTag = "drawer_all_notes",
                        onClick = {
                            onFilterSelect(NotesFilter.ALL, null)
                            onCloseDrawer()
                        }
                    )

                    DrawerNavigationItem(
                        label = strings.starred,
                        icon = Icons.Default.Star,
                        isSelected = activeFilter == NotesFilter.STARRED,
                        iconTint = Color(0xFFF59E0B),
                        testTag = "drawer_starred",
                        onClick = {
                            onFilterSelect(NotesFilter.STARRED, null)
                            onCloseDrawer()
                        }
                    )

                    DrawerNavigationItem(
                        label = strings.lockedNotes,
                        icon = Icons.Default.Lock,
                        isSelected = activeFilter == NotesFilter.LOCKED,
                        iconTint = Color(0xFFE11D48),
                        testTag = "drawer_locked",
                        onClick = {
                            onFilterSelect(NotesFilter.LOCKED, null)
                            onCloseDrawer()
                        }
                    )

                    DrawerNavigationItem(
                        label = strings.archive,
                        icon = Icons.Default.Archive,
                        isSelected = activeFilter == NotesFilter.ARCHIVE,
                        testTag = "drawer_archive",
                        onClick = {
                            onFilterSelect(NotesFilter.ARCHIVE, null)
                            onCloseDrawer()
                        }
                    )

                    DrawerNavigationItem(
                        label = strings.trash,
                        icon = Icons.Default.Delete,
                        isSelected = activeFilter == NotesFilter.TRASH,
                        testTag = "drawer_trash",
                        onClick = {
                            onFilterSelect(NotesFilter.TRASH, null)
                            onCloseDrawer()
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Folders Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = strings.folders,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        IconButton(
                            onClick = onCreateFolderClick,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("add_folder_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = strings.createFolder,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Custom Folders
                items(folders) { folder ->
                    val isSelected = activeFilter == NotesFilter.FOLDER && selectedFolderId == folder.id
                    DrawerNavigationItem(
                        label = folder.name,
                        icon = Icons.Default.Folder,
                        isSelected = isSelected,
                        iconTint = parseColor(folder.colorHex),
                        testTag = "folder_${folder.id}",
                        onClick = {
                            onFilterSelect(NotesFilter.FOLDER, folder.id)
                            onCloseDrawer()
                        }
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // Bottom Settings Item
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                color = Color.Transparent
            ) {
                DrawerNavigationItem(
                    label = strings.settings,
                    icon = Icons.Default.Settings,
                    isSelected = false,
                    testTag = "drawer_settings",
                    onClick = {
                        onSettingsClick()
                        onCloseDrawer()
                    }
                )
            }
        }
    }
}

@Composable
fun DrawerNavigationItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    count: Int? = null,
    testTag: String = ""
) {
    Surface(
        selected = isSelected,
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else iconTint,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                ),
                modifier = Modifier.weight(1f)
            )
            if (count != null && count > 0) {
                Surface(
                    shape = CircleShape,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

fun parseColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color(0xFF4F46E5)
    }
}
