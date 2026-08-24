package com.init.space.ui.disclosure

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.init.space.R
import com.init.space.theme.*
import com.init.space.ui.components.InitCard
import com.init.space.ui.components.InitOutlineButton
import com.init.space.ui.components.InitPrimaryButton
import com.init.space.utils.PrefsManager

@Composable
fun AccessibilityDisclosureScreen(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp),
            contentPadding = PaddingValues(top = 32.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "_init_ /space",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontFamily = MichromaFontFamily,
                            letterSpacing = 2.0.sp
                        ),
                        color = DarkTextPrimary
                    )
                    Text(
                        text = stringResource(R.string.disclosure_heading),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = JetBrainsMonoFontFamily
                        ),
                        color = DarkTextSecondary
                    )
                }
            }

            item {
                InitCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.disclosure_reason_title).uppercase(),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = MichromaFontFamily,
                                letterSpacing = 1.2.sp
                            ),
                            color = DarkTextPrimary
                        )

                        Text(
                            text = stringResource(R.string.disclosure_reason_body),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                lineHeight = 19.sp
                            ),
                            color = DarkTextSecondary
                        )
                    }
                }
            }

            item {
                InitCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.disclosure_permissions_title).uppercase(),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = MichromaFontFamily,
                                letterSpacing = 1.2.sp
                            ),
                            color = DarkTextPrimary
                        )

                        Text(
                            text = "• " + stringResource(R.string.disclosure_permission_media),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                lineHeight = 17.sp
                            ),
                            color = DarkTextSecondary
                        )

                        Text(
                            text = "• " + stringResource(R.string.disclosure_permission_mic),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                lineHeight = 17.sp
                            ),
                            color = DarkTextSecondary
                        )

                        Text(
                            text = "• " + stringResource(R.string.disclosure_permission_notifications),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                lineHeight = 17.sp
                            ),
                            color = DarkTextSecondary
                        )
                    }
                }
            }

            item {
                InitCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.disclosure_limit_title).uppercase(),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = MichromaFontFamily,
                                letterSpacing = 1.2.sp
                            ),
                            color = DarkTextPrimary
                        )

                        Text(
                            text = stringResource(R.string.disclosure_limit_body) + "\n\n" + stringResource(R.string.disclosure_storage_body),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                lineHeight = 17.sp
                            ),
                            color = DarkTextSecondary
                        )
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    InitPrimaryButton(
                        text = stringResource(R.string.disclosure_continue),
                        onClick = {
                            PrefsManager.setDisclosureAccepted(context, true)
                            PrefsManager.setOnboardingDone(context, true)
                            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    InitOutlineButton(
                        text = stringResource(R.string.disclosure_permissions_button),
                        onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    InitOutlineButton(
                        text = stringResource(R.string.disclosure_not_now),
                        onClick = {
                            PrefsManager.setDisclosureAccepted(context, true)
                            PrefsManager.setOnboardingDone(context, true)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
