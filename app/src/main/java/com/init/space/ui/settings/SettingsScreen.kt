package com.init.space.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.init.space.R
import com.init.space.data.AppDatabase
import com.init.space.theme.*
import com.init.space.ui.components.InitCard
import com.init.space.ui.components.InitConfirmDialog
import com.init.space.ui.components.InitDestructiveButton
import com.init.space.utils.FileUtils
import com.init.space.utils.PrefsManager
import com.init.space.utils.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var aiEnabled by remember { mutableStateOf(PrefsManager.isAiSummaryEnabled(context)) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    val packageInfo = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (_: Exception) {
            null
        }
    }
    val versionName = packageInfo?.versionName ?: "3.0"
    val versionCode = packageInfo?.let {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            it.longVersionCode
        } else {
            @Suppress("DEPRECATION") it.versionCode.toLong()
        }
    } ?: 29L

    if (showDeleteAllDialog) {
        InitConfirmDialog(
            title = stringResource(R.string.settings_delete_all_confirm_title),
            message = stringResource(R.string.settings_delete_all_confirm_message),
            confirmActionText = stringResource(R.string.settings_delete_all_confirm_action),
            isDestructive = true,
            onConfirm = {
                deleteAllCaptures(context) {
                    Toast.makeText(context, context.getString(R.string.settings_delete_all_success), Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { showDeleteAllDialog = false }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 20.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "back",
                        tint = DarkTextPrimary,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onBack() }
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.settings_title).uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = MichromaFontFamily,
                            letterSpacing = 1.4.sp
                        ),
                        color = DarkTextPrimary
                    )
                }
            }

            // Version info
            item {
                InitCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "_init_ /space",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = MichromaFontFamily,
                                letterSpacing = 1.2.sp
                            ),
                            color = DarkTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.settings_version, versionName, versionCode),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = JetBrainsMonoFontFamily
                            ),
                            color = DarkTextSecondary
                        )
                    }
                }
            }

            // AI Summary Setting
            item {
                InitCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.settings_ai_summary),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = DarkTextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.settings_ai_summary_subtitle),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = JetBrainsMonoFontFamily
                                ),
                                color = DarkTextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Switch(
                            checked = aiEnabled,
                            onCheckedChange = {
                                aiEnabled = it
                                PrefsManager.setAiSummaryEnabled(context, it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DarkBackground,
                                checkedTrackColor = SignalAccent,
                                uncheckedThumbColor = DarkTextSecondary,
                                uncheckedTrackColor = DarkSurfaceVariant
                            )
                        )
                    }
                }
            }

            // Accessibility Shortcut
            item {
                InitCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.settings_accessibility),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Medium
                            ),
                            color = DarkTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.settings_accessibility_subtitle),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = JetBrainsMonoFontFamily
                            ),
                            color = DarkTextSecondary
                        )
                    }
                }
            }

            // Privacy Policy
            item {
                InitCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://esl-pap.netlify.app/")))
                        } catch (_: Exception) {
                            Toast.makeText(context, "unable to open privacy policy link", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.settings_privacy),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Medium
                            ),
                            color = DarkTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.settings_privacy_subtitle),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = JetBrainsMonoFontFamily
                            ),
                            color = DarkTextSecondary
                        )
                    }
                }
            }

            // About Card
            item {
                InitCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.settings_about_title).uppercase(),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = MichromaFontFamily,
                                letterSpacing = 1.2.sp
                            ),
                            color = DarkTextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.settings_about_body),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                lineHeight = 17.sp
                            ),
                            color = DarkTextSecondary
                        )
                    }
                }
            }

            // Wipe All Data (NO red, SignalMutedAmber)
            item {
                InitCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = SignalMutedAmber.copy(alpha = 0.3f),
                    onClick = { showDeleteAllDialog = true }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.settings_delete_all),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Medium
                            ),
                            color = SignalMutedAmber
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.settings_delete_all_subtitle),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = JetBrainsMonoFontFamily
                            ),
                            color = DarkTextSecondary
                        )
                    }
                }
            }
        }
    }
}

private fun deleteAllCaptures(context: Context, onComplete: () -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        val db = AppDatabase.getDatabase(context)
        val all = db.queryAllEntriesSnapshot()
        all.forEach { entry ->
            FileUtils.deleteFile(entry.screenshotPath)
            FileUtils.deleteFile(entry.thumbnailPath)
            FileUtils.deleteFile(entry.voiceNotePath)
            ReminderScheduler.cancel(context, entry.id)
        }
        db.deleteAll()
        withContext(Dispatchers.Main) {
            onComplete()
        }
    }
}
