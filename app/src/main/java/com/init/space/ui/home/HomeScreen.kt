package com.init.space.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.init.space.R
import com.init.space.data.entity.CaptureEntry
import com.init.space.theme.*
import com.init.space.ui.components.*
import com.init.space.utils.FileUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenEntry: (Long) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allEntries by viewModel.allEntries.collectAsState()
    val filteredEntries by viewModel.filteredEntries.collectAsState()
    val recentEntry by viewModel.recentEntry.collectAsState()
    val query by viewModel.query.collectAsState()
    val currentFilter by viewModel.filterMode.collectAsState()

    val totalCount by viewModel.totalCount.collectAsState()
    val reminderCount by viewModel.reminderCount.collectAsState()
    val starredCount by viewModel.starredCount.collectAsState()
    val voiceCount by viewModel.voiceCount.collectAsState()

    val audioPlayer = remember { AudioPlayerController(context) }
    DisposableEffect(Unit) {
        onDispose { audioPlayer.release() }
    }

    var entryPendingDelete by remember { mutableStateOf<CaptureEntry?>(null) }

    if (entryPendingDelete != null) {
        InitConfirmDialog(
            title = stringResource(R.string.detail_delete_confirm_title),
            message = stringResource(R.string.detail_delete_confirm_message),
            confirmActionText = stringResource(R.string.detail_delete_confirm_action),
            isDestructive = true,
            onConfirm = {
                entryPendingDelete?.let { viewModel.deleteEntry(it) }
                entryPendingDelete = null
            },
            onDismiss = { entryPendingDelete = null }
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Row: App Title & Settings
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "_init_ /space",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontFamily = MichromaFontFamily,
                                letterSpacing = 2.0.sp
                            ),
                            color = DarkTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.home_subtitle),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = JetBrainsMonoFontFamily
                            ),
                            color = DarkTextSecondary
                        )
                    }

                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings_title),
                            tint = DarkTextSecondary
                        )
                    }
                }
            }

            // Live Counters Row (Scalable & horizontally scrollable so text never clips)
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        InitCounterPill(
                            label = stringResource(R.string.section_all),
                            count = totalCount
                        )
                    }
                    item {
                        InitCounterPill(
                            label = stringResource(R.string.section_starred),
                            count = starredCount
                        )
                    }
                    item {
                        InitCounterPill(
                            label = stringResource(R.string.section_reminders),
                            count = reminderCount
                        )
                    }
                    item {
                        InitCounterPill(
                            label = stringResource(R.string.section_voice),
                            count = voiceCount
                        )
                    }
                }
            }

            // Search Bar
            item {
                InitSearchBar(
                    query = query,
                    onQueryChange = { viewModel.setQuery(it) },
                    placeholder = stringResource(R.string.search_hint)
                )
            }

            // Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        InitFilterChip(
                            text = stringResource(R.string.filter_all),
                            selected = currentFilter == HomeViewModel.FilterMode.ALL,
                            onClick = { viewModel.setFilter(HomeViewModel.FilterMode.ALL) }
                        )
                    }
                    item {
                        InitFilterChip(
                            text = stringResource(R.string.filter_notes),
                            selected = currentFilter == HomeViewModel.FilterMode.NOTES,
                            onClick = { viewModel.setFilter(HomeViewModel.FilterMode.NOTES) }
                        )
                    }
                    item {
                        InitFilterChip(
                            text = stringResource(R.string.filter_voice),
                            selected = currentFilter == HomeViewModel.FilterMode.VOICE,
                            onClick = { viewModel.setFilter(HomeViewModel.FilterMode.VOICE) }
                        )
                    }
                    item {
                        InitFilterChip(
                            text = stringResource(R.string.filter_starred),
                            selected = currentFilter == HomeViewModel.FilterMode.STARRED,
                            onClick = { viewModel.setFilter(HomeViewModel.FilterMode.STARRED) }
                        )
                    }
                    item {
                        InitFilterChip(
                            text = stringResource(R.string.filter_reminders),
                            selected = currentFilter == HomeViewModel.FilterMode.REMINDERS,
                            onClick = { viewModel.setFilter(HomeViewModel.FilterMode.REMINDERS) }
                        )
                    }
                }
            }

            // "recent" Section (Hero Focus Card) — when not in active search
            if (query.isBlank() && currentFilter == HomeViewModel.FilterMode.ALL && recentEntry != null) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.section_recent).uppercase(),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = MichromaFontFamily,
                                letterSpacing = 1.4.sp
                            ),
                            color = DarkTextPrimary
                        )

                        val entry = recentEntry!!
                        InitHeroCard(
                            onClick = { onOpenEntry(entry.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = formatCardDate(entry.timestamp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = JetBrainsMonoFontFamily
                                            ),
                                            color = DarkTextTertiary
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        val displayText = entry.textNote?.takeIf { it.isNotBlank() }
                                            ?: entry.aiSummary?.takeIf { it.isNotBlank() }
                                            ?: if (!entry.voiceNotePath.isNullOrBlank()) "voice memo capture" else "saved screenshot capture"

                                        Text(
                                            text = displayText,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontWeight = FontWeight.Normal
                                            ),
                                            color = DarkTextPrimary,
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    if (File(entry.thumbnailPath).exists()) {
                                        Spacer(modifier = Modifier.width(14.dp))
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(File(entry.thumbnailPath))
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(width = 64.dp, height = 64.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .border(1.dp, DarkBorderSubtle, RoundedCornerShape(8.dp))
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f, fill = false),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (!entry.voiceNotePath.isNullOrBlank()) {
                                            val isPlaying = audioPlayer.currentPlayingPath == entry.voiceNotePath && audioPlayer.isPlaying
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = DarkSurfaceElevated,
                                                border = BorderStroke(1.dp, DarkBorder),
                                                modifier = Modifier.clickable {
                                                    audioPlayer.togglePlay(entry.voiceNotePath)
                                                }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = if (isPlaying) Icons.Outlined.Stop else Icons.Default.PlayArrow,
                                                        contentDescription = null,
                                                        tint = DarkTextPrimary,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = FileUtils.formatDuration(entry.voiceNoteDurationMs),
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontFamily = JetBrainsMonoFontFamily
                                                        ),
                                                        color = DarkTextPrimary
                                                    )
                                                }
                                            }
                                        }

                                        if (entry.reminderAt != null) {
                                            Text(
                                                text = "reminder: " + formatShortReminder(entry.reminderAt),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontFamily = JetBrainsMonoFontFamily
                                                ),
                                                color = DarkTextSecondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = stringResource(R.string.open_capture).lowercase() + " →",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = SignalAccent
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Timeline Header
            item {
                Text(
                    text = stringResource(R.string.section_all).uppercase(),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = MichromaFontFamily,
                        letterSpacing = 1.4.sp
                    ),
                    color = DarkTextPrimary
                )
            }

            // Timeline Entries List
            if (filteredEntries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = stringResource(
                                    if (query.isNotBlank()) R.string.empty_search_title else R.string.empty_state_title
                                ),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = MichromaFontFamily,
                                    letterSpacing = 1.0.sp
                                ),
                                color = DarkTextSecondary
                            )
                            Text(
                                text = stringResource(
                                    if (query.isNotBlank()) R.string.empty_search_subtitle else R.string.empty_state_subtitle
                                ),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = JetBrainsMonoFontFamily
                                ),
                                color = DarkTextTertiary
                            )
                        }
                    }
                }
            } else {
                items(filteredEntries, key = { it.id }) { entry ->
                    TimelineEntryCard(
                        entry = entry,
                        audioPlayer = audioPlayer,
                        onClick = { onOpenEntry(entry.id) },
                        onLongClick = { entryPendingDelete = entry },
                        onToggleFavorite = { viewModel.toggleFavorite(entry) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TimelineEntryCard(
    entry: CaptureEntry,
    audioPlayer: AudioPlayerController,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hasVoice = !entry.voiceNotePath.isNullOrBlank()
    val isPlaying = hasVoice && audioPlayer.currentPlayingPath == entry.voiceNotePath && audioPlayer.isPlaying

    InitCard(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Thumbnail preview
            if (File(entry.thumbnailPath).exists()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(File(entry.thumbnailPath))
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(width = 60.dp, height = 76.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, DarkBorderSubtle, RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(14.dp))
            }

            // Note, tags, voice, metadata
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatCardDate(entry.timestamp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMonoFontFamily
                        ),
                        color = DarkTextTertiary
                    )

                    Icon(
                        imageVector = if (entry.isFavorite) Icons.Default.Star else Icons.Outlined.StarOutline,
                        contentDescription = "star",
                        tint = if (entry.isFavorite) DarkTextPrimary else DarkTextTertiary,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onToggleFavorite() }
                    )
                }

                val textContent = entry.textNote?.takeIf { it.isNotBlank() }
                    ?: entry.aiSummary?.takeIf { it.isNotBlank() }
                    ?: if (hasVoice) "voice note" else "captured image"

                Text(
                    text = textContent,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = JetBrainsMonoFontFamily
                    ),
                    color = DarkTextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (hasVoice) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = DarkSurfaceElevated,
                            border = BorderStroke(1.dp, DarkBorderSubtle),
                            modifier = Modifier.clickable {
                                audioPlayer.togglePlay(entry.voiceNotePath)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Outlined.Stop else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = DarkTextPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = FileUtils.formatDuration(entry.voiceNoteDurationMs),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = JetBrainsMonoFontFamily
                                    ),
                                    color = DarkTextPrimary
                                )
                            }
                        }
                    }

                    if (entry.reminderAt != null) {
                        Text(
                            text = "remind: " + formatShortReminder(entry.reminderAt),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMonoFontFamily
                            ),
                            color = DarkTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (!entry.aiSummary.isNullOrBlank()) {
                        Text(
                            text = "[ai]",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMonoFontFamily
                            ),
                            color = DarkTextTertiary
                        )
                    }
                }
            }
        }
    }
}

private fun formatCardDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd  HH:mm", Locale.US)
    return formatter.format(Date(timestamp))
}

private fun formatShortReminder(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM d, HH:mm", Locale.US)
    return formatter.format(Date(timestamp))
}
