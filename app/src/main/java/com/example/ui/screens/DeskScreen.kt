package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.example.ui.components.IconifyIcons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.LocalFileEntity
import com.example.data.local.NoteEntity
import com.example.data.local.VoiceMemoEntity
import com.example.ui.components.AppMotion
import com.example.ui.components.ExpandableSearchBarPill
import com.example.ui.components.FileTypeBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.InlineSearchResultsCard
import com.example.ui.components.pressBounceScale
import com.example.ui.components.staggeredEntrance
import com.example.ui.theme.OnPrimary
import com.example.ui.theme.Primary
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.PrimaryFixed
import com.example.ui.viewmodel.WorkspaceViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DeskScreen(
    files: List<LocalFileEntity>,
    notes: List<NoteEntity>,
    memos: List<VoiceMemoEntity>,
    onSearchFileClick: (LocalFileEntity) -> Unit,
    onSelectNote: (NoteEntity) -> Unit,
    onSeeAllFiles: () -> Unit,
    onOpenDocument: (LocalFileEntity) -> Unit,
    onOpenQuickNote: () -> Unit,
    onOpenTodoList: () -> Unit,
    onOpenShoppingList: () -> Unit,
    onOpenVoiceMemos: () -> Unit,
    onOpenPhotoNotes: () -> Unit = {},
    onOpenMindMap: () -> Unit = {},
    onImportUri: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    BackHandler(enabled = searchExpanded) {
        searchExpanded = false
        searchQuery = ""
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            onImportUri(uri)
        }
    }

    val calendar = remember { Calendar.getInstance() }
    val greeting = remember(calendar.get(Calendar.HOUR_OF_DAY)) {
        when (calendar.get(Calendar.HOUR_OF_DAY)) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }
    val dateText = remember {
        SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
    }

    val recentThree = files.take(3)
    val lastNote = notes.firstOrNull()

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
            // Greeting + Expandable Search Row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredEntrance(0),
                contentAlignment = Alignment.CenterEnd
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = !searchExpanded,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = greeting,
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = dateText,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.size(56.dp))
                    }
                }

                ExpandableSearchBarPill(
                    expanded = searchExpanded,
                    query = searchQuery,
                    placeholder = "Search files, notes, or memos…",
                    onExpand = { searchExpanded = true },
                    onQueryChange = { searchQuery = it },
                    onClose = {
                        searchExpanded = false
                        searchQuery = ""
                    },
                    buttonTestTag = "desk_search_button",
                    inputTestTag = "desk_search_input",
                    closeTestTag = "desk_search_close",
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }

            AnimatedVisibility(
                visible = searchExpanded,
                enter = AppMotion.PopCardEnter,
                exit = AppMotion.PopCardExit
            ) {
                InlineSearchResultsCard(
                    query = searchQuery,
                    files = files,
                    notes = notes,
                    memos = memos,
                    onSelectFile = { file ->
                        searchExpanded = false
                        searchQuery = ""
                        onSearchFileClick(file)
                    },
                    onSelectNote = { note ->
                        searchExpanded = false
                        searchQuery = ""
                        onSelectNote(note)
                    },
                    onSelectMemo = {
                        searchExpanded = false
                        searchQuery = ""
                        onOpenVoiceMemos()
                    }
                )
            }

            // Last Left Note Card
            var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
            LaunchedEffect(lastNote?.id, lastNote?.updatedAt) {
                nowMs = System.currentTimeMillis()
                while (true) {
                    kotlinx.coroutines.delay(30_000L)
                    nowMs = System.currentTimeMillis()
                }
            }

            val lastNotePreview = remember(lastNote?.id, lastNote?.content) {
                lastNote?.content
                    ?.lineSequence()
                    ?.map { it.trim().removePrefix("##").removePrefix("#").removePrefix("- ").trim() }
                    ?.filter { it.isNotBlank() }
                    ?.joinToString(" ")
                    ?.takeIf { it.isNotBlank() }
                    ?: "Tap Continue to pick up right where you left off."
            }

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredEntrance(1)
                    .testTag("desk_continue_note_card"),
                cornerRadius = 24.dp,
                alpha = 0.66f,
                onClick = {
                    if (lastNote != null) onSelectNote(lastNote) else onOpenQuickNote()
                }
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = lastNote?.title?.ifBlank { "Untitled Note" } ?: "Start a new note",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (lastNote != null) {
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = "${lastNote.wordCount} ${if (lastNote.wordCount == 1) "word" else "words"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = PrimaryContainer
                            )
                        }
                    }

                    Text(
                        text = if (lastNote != null) lastNotePreview else "Capture thoughts, outlines, or meeting notes anytime.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (lastNote != null) {
                                "Edited ${WorkspaceViewModel.formatTimeAgo(lastNote.updatedAt, nowMs)}"
                            } else {
                                "Ready when you are"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val continueInteraction = remember { MutableInteractionSource() }
                        Button(
                            onClick = {
                                if (lastNote != null) onSelectNote(lastNote) else onOpenQuickNote()
                            },
                            interactionSource = continueInteraction,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryContainer,
                                contentColor = OnPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .pressBounceScale(continueInteraction, pressedScale = 0.94f)
                                .testTag("continue_last_note_button")
                        ) {
                            Icon(
                                imageVector = IconifyIcons.EditNote,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (lastNote != null) "Continue" else "Start writing",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            // Recent Files Section
            Column(
                modifier = Modifier.staggeredEntrance(2),
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
                        text = "Recent files",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(
                        onClick = onSeeAllFiles,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("desk_see_all_files")
                    ) {
                        Text(
                            text = "See all",
                            style = MaterialTheme.typography.labelLarge,
                            color = PrimaryContainer
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    recentThree.forEach { file ->
                        GlassCard(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("desk_recent_file_${file.id}"),
                            cornerRadius = 20.dp,
                            alpha = 0.68f,
                            onClick = { onOpenDocument(file) }
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                FileTypeBadge(
                                    category = file.category,
                                    isLocked = file.isLocked,
                                    size = 40.dp,
                                    cornerRadius = 12.dp
                                )
                                Column {
                                    Text(
                                        text = file.name,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = WorkspaceViewModel.formatFileSize(file.sizeBytes),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                    repeat((3 - recentThree.size).coerceAtLeast(0)) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }

            // Quick Actions 2x2 Grid
            Column(
                modifier = Modifier.staggeredEntrance(3),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Quick actions",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionTile(
                        title = "Quick note",
                        icon = IconifyIcons.StickyNote,
                        onClick = onOpenQuickNote,
                        testTag = "quick_action_note",
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionTile(
                        title = "To-do list",
                        icon = IconifyIcons.Checklist,
                        onClick = onOpenTodoList,
                        testTag = "quick_action_todo",
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionTile(
                        title = "Shopping list",
                        icon = IconifyIcons.ShoppingBag,
                        onClick = onOpenShoppingList,
                        testTag = "quick_action_shopping",
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionTile(
                        title = "Voice memo",
                        icon = IconifyIcons.Mic,
                        onClick = onOpenVoiceMemos,
                        testTag = "quick_action_voice",
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionTile(
                        title = "Photo notes",
                        icon = IconifyIcons.Camera,
                        onClick = onOpenPhotoNotes,
                        testTag = "quick_action_photo_notes",
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionTile(
                        title = "Mind map",
                        icon = IconifyIcons.MindMap,
                        onClick = onOpenMindMap,
                        testTag = "quick_action_mind_map",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Open File Floating-style CTA
            val openFileInteraction = remember { MutableInteractionSource() }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .staggeredEntrance(4),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        openDocumentLauncher.launch(
                            arrayOf(
                                "application/pdf",
                                "text/plain",
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                "application/vnd.openxmlformats-officedocument.presentationml.presentation"
                            )
                        )
                    },
                    interactionSource = openFileInteraction,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryContainer,
                        contentColor = OnPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                    contentPadding = PaddingValues(horizontal = 22.dp, vertical = 14.dp),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .pressBounceScale(openFileInteraction, pressedScale = 0.94f)
                        .testTag("desk_open_file_button")
                ) {
                    Icon(
                        imageVector = IconifyIcons.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Open file",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionTile(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
            .height(100.dp)
            .testTag(testTag),
        cornerRadius = 24.dp,
        alpha = 0.68f,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PrimaryFixed,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
