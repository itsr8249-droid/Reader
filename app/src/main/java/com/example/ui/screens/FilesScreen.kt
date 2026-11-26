package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import com.example.ui.components.IconifyIcons
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.LocalFileEntity
import com.example.ui.components.FileTypeBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.staggeredEntrance
import com.example.ui.viewmodel.WorkspaceViewModel

@Composable
fun FilesScreen(
    files: List<LocalFileEntity>,
    isGridView: Boolean,
    onFileClick: (LocalFileEntity) -> Unit,
    modifier: Modifier = Modifier
) {
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // File Cards (List or 2-Col Grid) with smooth mode morphing
            AnimatedContent(
                targetState = Triple(files.isEmpty(), isGridView, files.map { it.id }),
                transitionSpec = {
                    (scaleIn(initialScale = 0.96f, animationSpec = tween(220)) + fadeIn(tween(220)))
                        .togetherWith(scaleOut(targetScale = 0.96f, animationSpec = tween(160)) + fadeOut(tween(160)))
                },
                label = "files_layout_switch"
            ) { (isEmpty, grid, _) ->
                if (isEmpty) {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .staggeredEntrance(0),
                        alpha = 0.65f
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "No files yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Open a document from Desk or create one in Tools.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else if (grid) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        files.chunked(2).forEachIndexed { rowIdx, pair ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .staggeredEntrance(rowIdx),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                pair.forEach { file ->
                                    GlassCard(
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("file_card_${file.id}"),
                                        cornerRadius = 24.dp,
                                        alpha = 0.80f,
                                        onClick = { onFileClick(file) }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            FileTypeBadge(
                                                category = file.category,
                                                isLocked = file.isLocked,
                                                size = 48.dp,
                                                cornerRadius = 12.dp
                                            )
                                            Column {
                                                Text(
                                                    text = file.name,
                                                    style = MaterialTheme.typography.titleLarge,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${WorkspaceViewModel.formatFileSize(file.sizeBytes)} · ${WorkspaceViewModel.formatRelativeDate(file.lastModifiedAt)}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                                if (pair.size == 1) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        files.forEachIndexed { idx, file ->
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .staggeredEntrance(idx)
                                    .testTag("file_card_${file.id}"),
                                cornerRadius = 24.dp,
                                alpha = 0.80f,
                                onClick = { onFileClick(file) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    FileTypeBadge(
                                        category = file.category,
                                        isLocked = file.isLocked,
                                        size = 48.dp,
                                        cornerRadius = 12.dp
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = file.name,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${WorkspaceViewModel.formatFileSize(file.sizeBytes)} · ${WorkspaceViewModel.formatRelativeDate(file.lastModifiedAt)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Icon(
                                        imageVector = IconifyIcons.ChevronRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
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
