package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import com.example.ui.components.IconifyIcons
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.local.VoiceMemoEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.pressBounceScale
import com.example.ui.components.staggeredEntrance
import com.example.ui.theme.OnPrimary
import com.example.ui.theme.OnPrimaryFixed
import com.example.ui.theme.OnSecondaryContainer
import com.example.ui.theme.OnTertiaryFixed
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.PrimaryFixed
import com.example.ui.theme.SecondaryContainer
import com.example.ui.theme.TertiaryFixed
import com.example.ui.viewmodel.WorkspaceViewModel

@Composable
fun VoiceMemosScreen(
    memos: List<VoiceMemoEntity>,
    isRecording: Boolean,
    elapsedSeconds: Int,
    waveformBars: List<Float>,
    playingMemoId: Long?,
    onToggleRecord: () -> Unit,
    onTogglePlay: (VoiceMemoEntity) -> Unit,
    onDeleteMemo: (VoiceMemoEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var permissionDeniedNote by remember { mutableStateOf(false) }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            permissionDeniedNote = false
            onToggleRecord()
        } else {
            permissionDeniedNote = true
        }
    }

    val pulseTransition = rememberInfiniteTransition(label = "record_halo")
    val haloScale by pulseTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(720),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_scale"
    )
    val outerHaloScale by pulseTransition.animateFloat(
        initialValue = 1.04f,
        targetValue = 1.44f,
        animationSpec = infiniteRepeatable(
            animation = tween(980),
            repeatMode = RepeatMode.Reverse
        ),
        label = "outer_halo_scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Studio Recording Card
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredEntrance(0),
                cornerRadius = 24.dp,
                alpha = 0.72f
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(if (isRecording) 11.dp else 9.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryContainer)
                            )
                            Text(
                                text = if (isRecording) "Recording..." else "Ready to capture",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.85f)
                        ) {
                            Text(
                                text = "WAV",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Waveform Canvas Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.65f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(96.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            waveformBars.forEach { level ->
                                val targetHeight by animateDpAsState(
                                    targetValue = (level.coerceIn(0.12f, 1.0f) * 58f).dp,
                                    animationSpec = spring(
                                        dampingRatio = 0.62f,
                                        stiffness = Spring.StiffnessMedium
                                    ),
                                    label = "wave_bar"
                                )
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(targetHeight)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(
                                            PrimaryContainer.copy(
                                                alpha = (0.35f + level * 0.65f).coerceIn(0.35f, 1f)
                                            )
                                        )
                                )
                            }
                        }
                    }

                    // Tabular Timer
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = WorkspaceViewModel.formatDuration(
                                if (isRecording) elapsedSeconds else memos.firstOrNull()?.durationSeconds ?: 0
                            ),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontFeatureSettings = "tnum"
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (permissionDeniedNote) {
                                "Microphone permission is needed to record WAV memos"
                            } else {
                                "Tap to record or stop"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (permissionDeniedNote) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Record Button with Dual Pulsing Halos
                    val recordInteraction = remember { MutableInteractionSource() }
                    Box(
                        modifier = Modifier.size(92.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = isRecording,
                            enter = scaleIn(initialScale = 0.6f) + fadeIn(tween(220)),
                            exit = scaleOut(targetScale = 0.6f) + fadeOut(tween(180))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Box(
                                    modifier = Modifier
                                        .size(82.dp)
                                        .scale(outerHaloScale)
                                        .clip(CircleShape)
                                        .background(PrimaryContainer.copy(alpha = 0.12f))
                                )
                                Box(
                                    modifier = Modifier
                                        .size(74.dp)
                                        .scale(haloScale)
                                        .clip(CircleShape)
                                        .background(PrimaryContainer.copy(alpha = 0.24f))
                                )
                            }
                        }
                        Surface(
                            modifier = Modifier
                                .size(64.dp)
                                .pressBounceScale(recordInteraction, pressedScale = 0.90f)
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = recordInteraction,
                                    indication = androidx.compose.material3.ripple()
                                ) {
                                    if (isRecording) {
                                        onToggleRecord()
                                    } else {
                                        val hasMic = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.RECORD_AUDIO
                                        ) == PackageManager.PERMISSION_GRANTED
                                        if (hasMic) {
                                            permissionDeniedNote = false
                                            onToggleRecord()
                                        } else {
                                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    }
                                }
                                .testTag("voice_record_button"),
                            shape = CircleShape,
                            color = PrimaryContainer,
                            shadowElevation = 6.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                AnimatedContent(
                                    targetState = isRecording,
                                    transitionSpec = {
                                        (scaleIn(initialScale = 0.65f) + fadeIn(tween(180)))
                                            .togetherWith(scaleOut(targetScale = 0.65f) + fadeOut(tween(140)))
                                    },
                                    label = "record_icon_morph"
                                ) { recording ->
                                    Icon(
                                        imageVector = if (recording) IconifyIcons.Stop else IconifyIcons.Mic,
                                        contentDescription = stringResource(R.string.cd_record_memo),
                                        tint = OnPrimary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Recordings Section
            Column(
                modifier = Modifier.staggeredEntrance(1),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recordings",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${memos.size} ${if (memos.size == 1) "memo" else "memos"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                memos.forEachIndexed { idx, memo ->
                    val isPlaying = playingMemoId == memo.id
                    val (badgeBg, badgeFg) = when (idx % 3) {
                        0 -> PrimaryFixed to OnPrimaryFixed
                        1 -> SecondaryContainer to OnSecondaryContainer
                        else -> TertiaryFixed to OnTertiaryFixed
                    }
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("voice_memo_card_${memo.id}"),
                        cornerRadius = 18.dp,
                        alpha = 0.72f
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = badgeBg,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = IconifyIcons.Waveform,
                                            contentDescription = null,
                                            tint = badgeFg,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = memo.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${WorkspaceViewModel.formatDuration(memo.durationSeconds)} · ${WorkspaceViewModel.formatRelativeDate(memo.createdAt)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(
                                    onClick = { onDeleteMemo(memo) },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(
                                        imageVector = IconifyIcons.Delete,
                                        contentDescription = "Delete ${memo.title}",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                val playBg by animateColorAsState(
                                    targetValue = if (isPlaying) PrimaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                                    animationSpec = tween(220),
                                    label = "play_btn_bg"
                                )
                                val playTint by animateColorAsState(
                                    targetValue = if (isPlaying) OnPrimary else PrimaryContainer,
                                    animationSpec = tween(220),
                                    label = "play_btn_tint"
                                )
                                val playInteraction = remember(memo.id) { MutableInteractionSource() }
                                Surface(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .pressBounceScale(playInteraction, pressedScale = 0.90f)
                                        .clip(CircleShape)
                                        .clickable(
                                            interactionSource = playInteraction,
                                            indication = androidx.compose.material3.ripple()
                                        ) { onTogglePlay(memo) }
                                        .testTag("voice_play_button_${memo.id}"),
                                    shape = CircleShape,
                                    color = playBg
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        AnimatedContent(
                                            targetState = isPlaying,
                                            transitionSpec = {
                                                (scaleIn(initialScale = 0.65f) + fadeIn(tween(180)))
                                                    .togetherWith(scaleOut(targetScale = 0.65f) + fadeOut(tween(140)))
                                            },
                                            label = "play_pause_morph"
                                        ) { playing ->
                                            Icon(
                                                imageVector = if (playing) IconifyIcons.Pause else IconifyIcons.Play,
                                                contentDescription = if (playing) "Pause ${memo.title}" else "Play ${memo.title}",
                                                tint = playTint,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
