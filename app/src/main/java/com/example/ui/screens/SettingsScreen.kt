package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.example.ui.components.IconifyIcons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.AppPreferences
import com.example.ui.components.AppMotion
import com.example.ui.components.GlassCard
import com.example.ui.components.staggeredEntrance
import com.example.ui.theme.GradientBlue
import com.example.ui.theme.GradientBlush
import com.example.ui.theme.GradientLavender
import com.example.ui.theme.OnPrimary
import com.example.ui.theme.OnPrimaryFixed
import com.example.ui.theme.OnSecondaryFixed
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.PrimaryFixed
import com.example.ui.theme.SecondaryFixed

@Composable
fun SettingsScreen(
    preferences: AppPreferences,
    isVoiceSpeaking: Boolean,
    onSelectVoiceProfile: (String) -> Unit,
    onToggleVoicePreview: () -> Unit,
    onSelectLanguage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var openSourceExpanded by remember { mutableStateOf(false) }
    var showVoiceDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (openSourceExpanded) 180f else 0f,
        animationSpec = AppMotion.SpatialSpring,
        label = "open_source_chevron"
    )
    val voicePulseTransition = rememberInfiniteTransition(label = "voice_pulse")
    val voiceSpeakingScale by voicePulseTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(550),
            repeatMode = RepeatMode.Reverse
        ),
        label = "voice_speaking_scale"
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Appearance / Theme Card (Light frosted-glass gradient theme)
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredEntrance(0),
                cornerRadius = 24.dp,
                alpha = 0.68f
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = IconifyIcons.Palette,
                            contentDescription = null,
                            tint = PrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Theme",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.78f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = PrimaryContainer,
                                shadowElevation = 2.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = IconifyIcons.Sun,
                                        contentDescription = null,
                                        tint = OnPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Light · Frosted Glass",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = OnPrimary
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(width = 64.dp, height = 30.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(GradientBlue, GradientLavender, GradientBlush)
                                        )
                                    )
                            )
                        }
                    }
                }
            }

            // Read-Aloud & Language Card
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredEntrance(1),
                cornerRadius = 24.dp,
                alpha = 0.68f
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Read-aloud voice row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { showVoiceDialog = true }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .testTag("settings_voice_row"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = PrimaryFixed,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = IconifyIcons.VolumeLoud,
                                        contentDescription = null,
                                        tint = OnPrimaryFixed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Read-aloud voice",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = preferences.voiceProfile,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        IconButton(
                            onClick = onToggleVoicePreview,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("settings_voice_preview_button")
                        ) {
                            AnimatedContent(
                                targetState = isVoiceSpeaking,
                                transitionSpec = {
                                    (scaleIn(initialScale = 0.7f) + fadeIn(tween(180)))
                                        .togetherWith(scaleOut(targetScale = 0.7f) + fadeOut(tween(140)))
                                },
                                label = "voice_icon_transition"
                            ) { speaking ->
                                Icon(
                                    imageVector = if (speaking) IconifyIcons.Waveform else IconifyIcons.PlayCircle,
                                    contentDescription = stringResource(R.string.cd_preview_voice),
                                    tint = PrimaryContainer,
                                    modifier = Modifier
                                        .size(26.dp)
                                        .graphicsLayer {
                                            val s = if (speaking) voiceSpeakingScale else 1f
                                            scaleX = s
                                            scaleY = s
                                        }
                                )
                            }
                        }
                    }

                    // Language row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { showLanguageDialog = true }
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                            .testTag("settings_language_row"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SecondaryFixed,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = IconifyIcons.Translate,
                                        contentDescription = null,
                                        tint = OnSecondaryFixed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Language",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = preferences.language,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Icon(
                            imageVector = IconifyIcons.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // About & Open Source Card
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredEntrance(2),
                cornerRadius = 24.dp,
                alpha = 0.68f
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { openSourceExpanded = !openSourceExpanded }
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                            .testTag("settings_opensource_button"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = IconifyIcons.Code,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Built with open source",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "View third-party licences",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = IconifyIcons.ChevronDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.rotate(chevronRotation)
                        )
                    }

                    AnimatedVisibility(
                        visible = openSourceExpanded,
                        enter = AppMotion.SoftExpandEnter,
                        exit = AppMotion.SoftCollapseExit
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp)
                                .padding(bottom = 12.dp, top = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LicenceItem("AndroidX & Jetpack Compose", "Apache License 2.0")
                            LicenceItem("Android Room & SQLite", "Apache License 2.0")
                            LicenceItem("Iconify Solar Icons", "Apache License 2.0")
                            LicenceItem("Inter Typeface", "SIL Open Font License 1.1")
                        }
                    }
                }
            }
        }
    }

    if (showVoiceDialog) {
        val options = listOf(
            "Calm — English (UK)",
            "Warm — English (US)",
            "Crisp — English (AU)"
        )
        AlertDialog(
            onDismissRequest = { showVoiceDialog = false },
            title = { Text("Read-aloud voice") },
            text = {
                Column {
                    options.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onSelectVoiceProfile(option)
                                    showVoiceDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = preferences.voiceProfile == option,
                                onClick = {
                                    onSelectVoiceProfile(option)
                                    showVoiceDialog = false
                                }
                            )
                            Text(option, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showVoiceDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    if (showLanguageDialog) {
        val languages = listOf("English", "English (UK)", "English (US)")
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Language") },
            text = {
                Column {
                    languages.forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onSelectLanguage(lang)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = preferences.language == lang,
                                onClick = {
                                    onSelectLanguage(lang)
                                    showLanguageDialog = false
                                }
                            )
                            Text(lang, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
private fun LicenceItem(name: String, subtitle: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.78f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = name,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
