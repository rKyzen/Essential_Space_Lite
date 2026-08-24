package com.init.space.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.init.space.theme.*

@Composable
fun InitCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    backgroundColor: Color = DarkSurface,
    borderColor: Color = DarkBorderSubtle,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    } else {
        modifier
    }

    Surface(
        modifier = clickableModifier,
        shape = shape,
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(content = content)
    }
}

@Composable
fun InitHeroCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    InitCard(
        modifier = modifier,
        onClick = onClick,
        backgroundColor = DarkSurfaceVariant,
        borderColor = DarkBorder,
        shape = RoundedCornerShape(14.dp),
        content = content
    )
}

@Composable
fun InitPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Surface(
        modifier = modifier
            .defaultMinSize(minHeight = 44.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(enabled = enabled, onClick = onClick),
        color = if (enabled) SignalAccent else DarkSurfaceElevated,
        shape = RoundedCornerShape(10.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = MichromaFontFamily,
                    letterSpacing = 1.0.sp
                ),
                color = if (enabled) DarkBackground else DarkTextTertiary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun InitOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Surface(
        modifier = modifier
            .defaultMinSize(minHeight = 42.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(enabled = enabled, onClick = onClick),
        color = DarkSurface,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text.lowercase(),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Medium
                ),
                color = if (enabled) DarkTextPrimary else DarkTextTertiary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun InitDestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    // Muted amber outline for destructive actions — zero red
    Surface(
        modifier = modifier
            .defaultMinSize(minHeight = 42.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(enabled = enabled, onClick = onClick),
        color = DarkSurface,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, SignalMutedAmber.copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text.lowercase(),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Medium
                ),
                color = if (enabled) SignalMutedAmber else DarkTextTertiary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun InitFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = if (selected) DarkSurfaceElevated else DarkSurface,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (selected) DarkTextPrimary else DarkBorderSubtle)
    ) {
        Text(
            text = text.lowercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
            ),
            color = if (selected) DarkTextPrimary else DarkTextSecondary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        )
    }
}

@Composable
fun InitSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = DarkSurface,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    color = DarkTextPrimary
                ),
                cursorBrush = SolidColor(DarkTextPrimary),
                singleLine = true,
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            text = placeholder.lowercase(),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                color = DarkTextTertiary
                            )
                        )
                    }
                    innerTextField()
                }
            )

            if (query.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "clear",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        color = DarkTextSecondary
                    ),
                    modifier = Modifier.clickable { onQueryChange("") }
                )
            }
        }
    }
}

@Composable
fun InitCounterPill(
    label: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = DarkSurface,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, DarkBorderSubtle)
    ) {
        Text(
            text = "$label: $count",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = JetBrainsMonoFontFamily
            ),
            color = DarkTextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
fun InitConfirmDialog(
    title: String,
    message: String,
    confirmActionText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceElevated,
        shape = RoundedCornerShape(14.dp),
        title = {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = MichromaFontFamily,
                    letterSpacing = 1.0.sp
                ),
                color = if (isDestructive) SignalMutedAmber else DarkTextPrimary
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = JetBrainsMonoFontFamily
                ),
                color = DarkTextSecondary
            )
        },
        confirmButton = {
            if (isDestructive) {
                InitDestructiveButton(
                    text = confirmActionText,
                    onClick = {
                        onConfirm()
                        onDismiss()
                    }
                )
            } else {
                InitPrimaryButton(
                    text = confirmActionText,
                    onClick = {
                        onConfirm()
                        onDismiss()
                    }
                )
            }
        },
        dismissButton = {
            InitOutlineButton(
                text = "cancel",
                onClick = onDismiss
            )
        }
    )
}
