package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.NotesUiState
import com.example.util.LocalAppStrings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: NotesUiState,
    onBackClick: () -> Unit,
    onLanguageChange: (String) -> Unit,
    onThemeChange: (String) -> Unit,
    onSetPassword: suspend (String) -> Boolean,
    onRemovePassword: () -> Unit,
    onClearTrash: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val coroutineScope = rememberCoroutineScope()

    var showPasswordDialog by remember { mutableStateOf(false) }
    var passwordInput by remember { mutableStateOf("") }
    var passwordConfirmInput by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = strings.settings,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Section 1: Security & Privacy (Matching User Screenshot 3 style)
            SettingsCategoryHeader(title = strings.securityAndPrivacy)

            SettingsCard {
                SettingsItemRow(
                    icon = Icons.Default.Security,
                    title = strings.enableAppLock,
                    subtitle = if (uiState.hasPasswordSet) strings.passwordSetSuccess else strings.setPassword,
                    testTag = "settings_password_row",
                    trailingContent = {
                        Switch(
                            checked = uiState.isAppLockEnabled && uiState.hasPasswordSet,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    showPasswordDialog = true
                                } else {
                                    onRemovePassword()
                                }
                            },
                            modifier = Modifier.testTag("app_lock_switch")
                        )
                    },
                    onClick = {
                        showPasswordDialog = true
                    }
                )

                if (uiState.hasPasswordSet) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    SettingsItemRow(
                        icon = Icons.Default.LockReset,
                        title = strings.changePassword,
                        subtitle = strings.securityHelpNote,
                        testTag = "settings_change_password_row",
                        onClick = {
                            showPasswordDialog = true
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 2: Preferences / Language (Default English, Arabic option)
            SettingsCategoryHeader(title = strings.language)

            SettingsCard {
                SettingsItemRow(
                    icon = Icons.Default.Language,
                    title = strings.language,
                    subtitle = if (uiState.language == "ar") strings.arabic else strings.english,
                    testTag = "settings_language_row",
                    onClick = {
                        showLanguageDialog = true
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                SettingsItemRow(
                    icon = Icons.Default.ColorLens,
                    title = strings.theme,
                    subtitle = when (uiState.appTheme) {
                        "light" -> strings.lightTheme
                        "dark" -> strings.darkTheme
                        else -> strings.systemTheme
                    },
                    testTag = "settings_theme_row",
                    onClick = {
                        showThemeDialog = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 3: Storage & Data
            SettingsCategoryHeader(title = strings.storageAndData)

            SettingsCard {
                SettingsItemRow(
                    icon = Icons.Default.Storage,
                    title = strings.totalNotes,
                    subtitle = "${uiState.activeNotesCount} ${strings.allNotes}",
                    testTag = "settings_storage_row",
                    onClick = {}
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                SettingsItemRow(
                    icon = Icons.Default.DeleteSweep,
                    title = strings.clearTrash,
                    subtitle = strings.emptyTrashWarning,
                    testTag = "settings_clear_trash_row",
                    onClick = onClearTrash
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 4: App Info
            SettingsCategoryHeader(title = strings.appInfo)

            SettingsCard {
                SettingsItemRow(
                    icon = Icons.Default.Info,
                    title = strings.appName,
                    subtitle = strings.version,
                    testTag = "settings_info_row",
                    onClick = {}
                )
            }
        }
    }

    // Password Setup Dialog
    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showPasswordDialog = false
                passwordInput = ""
                passwordConfirmInput = ""
                passwordError = null
            },
            title = { Text(strings.setPassword) },
            text = {
                Column {
                    Text(
                        text = strings.securityHelpNote,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = {
                            passwordInput = it
                            passwordError = null
                        },
                        label = { Text(strings.enterPassword) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("set_password_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = passwordConfirmInput,
                        onValueChange = {
                            passwordConfirmInput = it
                            passwordError = null
                        },
                        label = { Text(strings.confirmPassword) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("confirm_password_input")
                    )

                    if (passwordError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = passwordError!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (passwordInput.isBlank()) {
                            passwordError = strings.wrongPassword
                            return@Button
                        }
                        if (passwordInput != passwordConfirmInput) {
                            passwordError = strings.confirmPassword
                            return@Button
                        }
                        coroutineScope.launch {
                            val success = onSetPassword(passwordInput)
                            if (success) {
                                showPasswordDialog = false
                                passwordInput = ""
                                passwordConfirmInput = ""
                            }
                        }
                    },
                    modifier = Modifier.testTag("save_password_button")
                ) {
                    Text(strings.save)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPasswordDialog = false
                        passwordInput = ""
                        passwordConfirmInput = ""
                        passwordError = null
                    },
                    modifier = Modifier.testTag("cancel_password_button")
                ) {
                    Text(strings.cancel)
                }
            }
        )
    }

    // Language Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(strings.selectLanguage) },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                onLanguageChange("en")
                                showLanguageDialog = false
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(strings.english, fontWeight = if (uiState.language == "en") FontWeight.Bold else FontWeight.Normal)
                        if (uiState.language == "en") {
                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                onLanguageChange("ar")
                                showLanguageDialog = false
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(strings.arabic, fontWeight = if (uiState.language == "ar") FontWeight.Bold else FontWeight.Normal)
                        if (uiState.language == "ar") {
                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    // Theme Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(strings.theme) },
            text = {
                Column {
                    listOf("system" to strings.systemTheme, "light" to strings.lightTheme, "dark" to strings.darkTheme).forEach { (key, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onThemeChange(key)
                                    showThemeDialog = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(label, fontWeight = if (uiState.appTheme == key) FontWeight.Bold else FontWeight.Normal)
                            if (uiState.appTheme == key) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}

@Composable
fun SettingsCategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(4.dp), content = content)
    }
}

@Composable
fun SettingsItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    testTag: String = "",
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (trailingContent != null) {
            trailingContent()
        } else {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}
