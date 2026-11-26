package com.example.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.LocalFileEntity
import com.example.data.local.NoteEntity
import com.example.data.local.VoiceMemoEntity
import com.example.ui.theme.PrimaryContainer
import com.example.ui.viewmodel.WorkspaceViewModel

@Composable
fun InlineSearchResultsCard(
    query: String,
    files: List<LocalFileEntity>,
    notes: List<NoteEntity>,
    memos: List<VoiceMemoEntity>,
    onSelectFile: (LocalFileEntity) -> Unit,
    onSelectNote: (NoteEntity) -> Unit,
    onSelectMemo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val normalized = query.trim().lowercase()

    val matchedFiles = remember(files, normalized) {
        if (normalized.isEmpty()) files.take(3)
        else files.filter {
            it.name.lowercase().contains(normalized) ||
                it.snippetTitle.lowercase().contains(normalized) ||
                it.snippetBody.lowercase().contains(normalized)
        }
    }
    val matchedNotes = remember(notes, normalized) {
        if (normalized.isEmpty()) notes.take(2)
        else notes.filter {
            it.title.lowercase().contains(normalized) ||
                it.content.lowercase().contains(normalized)
        }
    }
    val matchedMemos = remember(memos, normalized) {
        if (normalized.isEmpty()) memos.take(2)
        else memos.filter { it.title.lowercase().contains(normalized) }
    }

    val hasResults = matchedFiles.isNotEmpty() || matchedNotes.isNotEmpty() || matchedMemos.isNotEmpty()

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = 0.80f,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            .testTag("inline_search_results_card"),
        cornerRadius = 24.dp,
        alpha = 0.92f
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!hasResults) {
                Text(
                    text = "No results for \"$query\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 12.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 340.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(matchedFiles, key = { "f_${it.id}" }) { file ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.85f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onSelectFile(file) }
                                .testTag("search_result_file_${file.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                FileTypeBadge(file.category, file.isLocked, size = 38.dp)
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = file.name,
                                        style = MaterialTheme.typography.labelLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${file.category.label} · ${WorkspaceViewModel.formatFileSize(file.sizeBytes)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    items(matchedNotes, key = { "n_${it.id}" }) { note ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.85f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceContainerHigh),
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onSelectNote(note) }
                                .testTag("search_result_note_${note.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = IconifyIcons.FileNote,
                                    contentDescription = "Note",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(38.dp)
                                )
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = note.title,
                                        style = MaterialTheme.typography.labelLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Note · ${note.wordCount} words",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    items(matchedMemos, key = { "m_${it.id}" }) { memo ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.85f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onSelectMemo() }
                                .testTag("search_result_memo_${memo.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(IconifyIcons.Mic, contentDescription = null, tint = PrimaryContainer)
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = memo.title,
                                        style = MaterialTheme.typography.labelLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Voice Memo · ${WorkspaceViewModel.formatDuration(memo.durationSeconds)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
