package com.init.space.ui.detail

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.init.space.R
import com.init.space.theme.*
import com.init.space.ui.components.*
import com.init.space.utils.FileUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DetailScreen(
    entryId: Long,
    viewModel: DetailViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val entry by viewModel.entry.collectAsState()
    val isGeneratingSummary by viewModel.isGeneratingSummary.collectAsState()
    val summaryStatus by viewModel.summaryStatus.collectAsState()

    var noteText by remember { mutableStateOf("") }
    var isScreenshotExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val audioPlayer = remember { AudioPlayerController(context) }
    DisposableEffect(Unit) {
        viewModel.loadEntry(entryId)
        onDispose { audioPlayer.release() }
    }

    LaunchedEffect(entry) {
        entry?.let {
            if (noteText.isEmpty() && it.textNote != null) {
                noteText = it.textNote
            }
        }
    }

    if (showDeleteDialog) {
        InitConfirmDialog(
            title = stringResource(R.string.detail_delete_confirm_title),
            message = stringResource(R.string.detail_delete_confirm_message),
            confirmActionText = stringResource(R.string.detail_delete_confirm_action),
            isDestructive = true,
            onConfirm = {
                viewModel.deleteCapture { onBack() }
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    val currentEntry = entry
    if (currentEntry == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "loading capture...",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = JetBrainsMonoFontFamily
                ),
                color = DarkTextSecondary
            )
        }
        return
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
            contentPadding = PaddingValues(top = 20.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Toolbar Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onBack() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "back",
                            tint = DarkTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "BACK",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = MichromaFontFamily,
                                letterSpacing = 1.2.sp
                            ),
                            color = DarkTextPrimary
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.toggleFavorite() }) {
                            Icon(
                                imageVector = if (currentEntry.isFavorite) Icons.Default.Star else Icons.Outlined.StarOutline,
                                contentDescription = "star",
                                tint = if (currentEntry.isFavorite) DarkTextPrimary else DarkTextSecondary
                            )
                        }
                    }
                }
            }

            // Screenshot Preview Card (Adaptive height)
            if (File(currentEntry.screenshotPath).exists()) {
                item {
                    InitCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { isScreenshotExpanded = !isScreenshotExpanded }
                    ) {
                        Box {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(File(currentEntry.screenshotPath))
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "screenshot",
                                contentScale = if (isScreenshotExpanded) ContentScale.FillWidth else ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 180.dp, max = if (isScreenshotExpanded) 460.dp else 220.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )

                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp),
                                shape = RoundedCornerShape(6.dp),
                                color = DarkBackground.copy(alpha = 0.85f),
                                border = BorderStroke(1.dp, DarkBorderSubtle)
                            ) {
                                Text(
                                    text = if (isScreenshotExpanded) "fit" else "expand",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = JetBrainsMonoFontFamily
                                    ),
                                    color = DarkTextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Metadata Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatDetailedDate(currentEntry.timestamp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMonoFontFamily
                        ),
                        color = DarkTextTertiary
                    )

                    if (!currentEntry.appName.isNullOrBlank()) {
                        Text(
                            text = "from: " + currentEntry.appName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMonoFontFamily
                            ),
                            color = DarkTextTertiary
                        )
                    }
                }
            }

            // Note Editor Section
            item {
                InitCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.section_notes).uppercase(),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = MichromaFontFamily,
                                letterSpacing = 1.2.sp
                            ),
                            color = DarkTextPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        BasicTextField(
                            value = noteText,
                            onValueChange = { noteText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 72.dp),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                color = DarkTextPrimary,
                                lineHeight = 19.sp
                            ),
                            cursorBrush = SolidColor(DarkTextPrimary),
                            decorationBox = { innerTextField ->
                                if (noteText.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.detail_note_hint),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            color = DarkTextTertiary
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.detail_note_count, noteText.length),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JetBrainsMonoFontFamily
                                ),
                                color = DarkTextTertiary
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (noteText.isNotEmpty()) {
                                    InitOutlineButton(
                                        text = stringResource(R.string.detail_copy_note),
                                        onClick = {
                                            copyToClipboard(context, "note", noteText)
                                            Toast.makeText(context, context.getString(R.string.detail_note_copied), Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }

                                InitPrimaryButton(
                                    text = stringResource(R.string.detail_save_note),
                                    onClick = {
                                        viewModel.saveTextNote(noteText)
                                        Toast.makeText(context, context.getString(R.string.detail_note_saved), Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Voice Memo Section
            if (!currentEntry.voiceNotePath.isNullOrBlank() && File(currentEntry.voiceNotePath).exists()) {
                item {
                    val isPlaying = audioPlayer.currentPlayingPath == currentEntry.voiceNotePath && audioPlayer.isPlaying

                    InitCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.section_voice).uppercase(),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = MichromaFontFamily,
                                    letterSpacing = 1.2.sp
                                ),
                                color = DarkTextPrimary
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = DarkSurfaceElevated,
                                        border = BorderStroke(1.dp, DarkBorder),
                                        modifier = Modifier.clickable {
                                            audioPlayer.togglePlay(currentEntry.voiceNotePath)
                                        }
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isPlaying) Icons.Outlined.Stop else Icons.Default.PlayArrow,
                                                contentDescription = null,
                                                tint = DarkTextPrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    Column {
                                        Text(
                                            text = if (isPlaying) "playing voice memo" else "voice memo",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontFamily = JetBrainsMonoFontFamily
                                            ),
                                            color = DarkTextPrimary
                                        )
                                        Text(
                                            text = FileUtils.formatDuration(currentEntry.voiceNoteDurationMs),
                                            style = MaterialTheme.typography.labelSmall.copy(
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
            }

            // Reminders Section
            item {
                InitCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.reminder_label).uppercase(),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = MichromaFontFamily,
                                letterSpacing = 1.2.sp
                            ),
                            color = DarkTextPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        val reminderText = currentEntry.reminderAt?.let {
                            formatDetailedDate(it)
                        } ?: stringResource(R.string.reminder_none)

                        Text(
                            text = reminderText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = JetBrainsMonoFontFamily
                            ),
                            color = if (currentEntry.reminderAt != null) DarkTextPrimary else DarkTextTertiary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            InitOutlineButton(
                                text = stringResource(R.string.reminder_set_button),
                                onClick = {
                                    pickReminderDateTime(context) { timestamp ->
                                        viewModel.setReminder(timestamp)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )

                            if (currentEntry.reminderAt != null) {
                                InitOutlineButton(
                                    text = stringResource(R.string.reminder_clear_button),
                                    onClick = { viewModel.clearReminder() },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        if (currentEntry.reminderAt != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                InitOutlineButton(
                                    text = stringResource(R.string.reminder_add_calendar),
                                    onClick = { exportToCalendar(context, currentEntry) },
                                    modifier = Modifier.weight(1f)
                                )
                                InitOutlineButton(
                                    text = stringResource(R.string.reminder_add_clock),
                                    onClick = { exportToClock(context, currentEntry) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // AI Summary Section
            item {
                InitCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.ai_summary_title).uppercase(),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = MichromaFontFamily,
                                    letterSpacing = 1.2.sp
                                ),
                                color = DarkTextPrimary
                            )

                            if (isGeneratingSummary) {
                                Text(
                                    text = "thinking...",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = JetBrainsMonoFontFamily
                                    ),
                                    color = DarkTextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val summaryContent = currentEntry.aiSummary?.takeIf { it.isNotBlank() }
                            ?: summaryStatus
                            ?: stringResource(R.string.ai_summary_empty)

                        Text(
                            text = summaryContent,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                lineHeight = 19.sp
                            ),
                            color = if (currentEntry.aiSummary != null) DarkTextPrimary else DarkTextTertiary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            InitOutlineButton(
                                text = stringResource(
                                    if (currentEntry.aiSummary != null) R.string.ai_summary_refresh else R.string.ai_summary_generate
                                ),
                                onClick = { viewModel.generateAiSummary() },
                                enabled = !isGeneratingSummary,
                                modifier = Modifier.weight(1f)
                            )

                            if (!currentEntry.aiSummary.isNullOrBlank()) {
                                InitOutlineButton(
                                    text = stringResource(R.string.ai_summary_copy),
                                    onClick = {
                                        copyToClipboard(context, "summary", currentEntry.aiSummary)
                                        Toast.makeText(context, context.getString(R.string.ai_summary_copied), Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Actions: Share & Delete (NO red)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    InitOutlineButton(
                        text = stringResource(R.string.detail_share),
                        onClick = { shareCapture(context, currentEntry) },
                        modifier = Modifier.weight(1f)
                    )

                    InitDestructiveButton(
                        text = stringResource(R.string.detail_delete),
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

private fun formatDetailedDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd  HH:mm:ss", Locale.US)
    return formatter.format(Date(timestamp))
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(ClipboardManager::class.java)
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
}

private fun pickReminderDateTime(context: Context, onPicked: (Long) -> Unit) {
    val now = Calendar.getInstance()
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    val chosen = Calendar.getInstance().apply {
                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, month)
                        set(Calendar.DAY_OF_MONTH, dayOfMonth)
                        set(Calendar.HOUR_OF_DAY, hourOfDay)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis

                    if (chosen <= System.currentTimeMillis()) {
                        Toast.makeText(context, "select a future time", Toast.LENGTH_SHORT).show()
                    } else {
                        onPicked(chosen)
                    }
                },
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                true
            ).show()
        },
        now.get(Calendar.YEAR),
        now.get(Calendar.MONTH),
        now.get(Calendar.DAY_OF_MONTH)
    ).show()
}

private fun exportToCalendar(context: Context, entry: com.init.space.data.entity.CaptureEntry) {
    val reminderAt = entry.reminderAt ?: return
    val title = entry.textNote?.takeIf { it.isNotBlank() } ?: "_init_ /space reminder"
    val intent = Intent(Intent.ACTION_INSERT).apply {
        data = CalendarContract.Events.CONTENT_URI
        putExtra(CalendarContract.Events.TITLE, title)
        putExtra(CalendarContract.Events.DESCRIPTION, "reminder from _init_ /space")
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, reminderAt)
        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, reminderAt + 30 * 60 * 1000L)
    }
    context.startActivity(intent)
}

private fun exportToClock(context: Context, entry: com.init.space.data.entity.CaptureEntry) {
    val reminderAt = entry.reminderAt ?: return
    val calendar = Calendar.getInstance().apply { timeInMillis = reminderAt }
    val label = entry.textNote?.takeIf { it.isNotBlank() } ?: "_init_ /space reminder"
    val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
        putExtra(AlarmClock.EXTRA_HOUR, calendar.get(Calendar.HOUR_OF_DAY))
        putExtra(AlarmClock.EXTRA_MINUTES, calendar.get(Calendar.MINUTE))
        putExtra(AlarmClock.EXTRA_MESSAGE, label)
        putExtra(AlarmClock.EXTRA_SKIP_UI, false)
    }
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, context.getString(R.string.reminder_clock_unavailable), Toast.LENGTH_SHORT).show()
    }
}

private fun shareCapture(context: Context, entry: com.init.space.data.entity.CaptureEntry) {
    val file = File(entry.screenshotPath)
    if (!file.exists()) {
        Toast.makeText(context, "screenshot file not found", Toast.LENGTH_SHORT).show()
        return
    }

    val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/jpeg"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, entry.textNote.orEmpty())
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "share capture"))
}
