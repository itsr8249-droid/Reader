package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import com.example.ui.components.IconifyIcons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.NoteEntity
import com.example.ui.components.pressBounceScale
import com.example.ui.components.staggeredEntrance
import com.example.ui.theme.GlassBorderWhite
import com.example.ui.theme.GlassShadowNavy
import com.example.ui.theme.Primary
import com.example.ui.theme.PrimaryContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NoteEditorScreen(
    note: NoteEntity?,
    onNoteChange: (String, String) -> Unit,
    isVoiceSpeaking: Boolean = false,
    onToggleReadAloud: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val activeTitle = note?.title?.ifBlank { "Untitled Note" } ?: "Weekly Reflections"
    var bodyField by remember(note?.id) {
        mutableStateOf(TextFieldValue(note?.content ?: ""))
    }
    val undoStack = remember(note?.id) { mutableStateListOf<String>() }

    LaunchedEffect(note?.id) {
        if (note != null) {
            bodyField = TextFieldValue(note.content, TextRange(note.content.length))
        }
    }

    val dateLabel = remember(note?.updatedAt) {
        val ts = note?.updatedAt ?: System.currentTimeMillis()
        SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(ts))
    }
    val words = remember(bodyField.text) {
        WorkspaceRepositoryCountWords(bodyField.text)
    }

    fun applyFormatting(prefix: String, suffix: String = "", lineLevel: Boolean = false) {
        val text = bodyField.text
        undoStack.add(text)
        if (undoStack.size > 30) undoStack.removeAt(0)

        val sel = bodyField.selection
        val start = sel.min
        val end = sel.max

        val newText: String
        val newCursor: Int
        if (lineLevel) {
            val lineStart = text.lastIndexOf('\n', (start - 1).coerceAtLeast(0)).let {
                if (it == -1) 0 else it + 1
            }
            val currentLine = text.substring(lineStart)
            if (currentLine.startsWith(prefix)) {
                newText = text.removeRange(lineStart, lineStart + prefix.length)
                newCursor = (start - prefix.length).coerceAtLeast(lineStart)
            } else {
                newText = text.substring(0, lineStart) + prefix + text.substring(lineStart)
                newCursor = start + prefix.length
            }
        } else if (start != end) {
            val selected = text.substring(start, end)
            val wrapped = "$prefix$selected$suffix"
            newText = text.substring(0, start) + wrapped + text.substring(end)
            newCursor = start + wrapped.length
        } else {
            val placeholder = if (prefix == "**") "bold" else "italic"
            val inserted = "$prefix$placeholder$suffix"
            newText = text.substring(0, start) + inserted + text.substring(end)
            newCursor = start + inserted.length
        }
        bodyField = TextFieldValue(newText, TextRange(newCursor.coerceIn(0, newText.length)))
        onNoteChange(activeTitle, newText)
    }

    var isFormatBarVisible by remember(note?.id) { mutableStateOf(true) }
    val scrollState = rememberScrollState()
    val formatBarNestedScroll = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput) {
                    if (available.y < -6f) {
                        isFormatBarVisible = false
                    } else if (available.y > 6f) {
                        isFormatBarVisible = true
                    }
                }
                return Offset.Zero
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding()
            .nestedScroll(formatBarNestedScroll)
            .pointerInput(note?.id) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    var dySum = 0f
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val change = event.changes.firstOrNull() ?: break
                        if (!change.pressed) break
                        dySum += change.positionChange().y
                        if (dySum < -18f) {
                            isFormatBarVisible = false
                            dySum = 0f
                        } else if (dySum > 18f) {
                            isFormatBarVisible = true
                            dySum = 0f
                        }
                    }
                }
            },
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .align(Alignment.TopCenter)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(top = 12.dp, bottom = 124.dp)
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 320.dp)
            ) {
                if (bodyField.text.isEmpty()) {
                    Text(
                        text = "Start writing…",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                    )
                }
                BasicTextField(
                    value = bodyField,
                    onValueChange = { next ->
                        isFormatBarVisible = true
                        if (next.text != bodyField.text) {
                            undoStack.add(bodyField.text)
                            if (undoStack.size > 30) undoStack.removeAt(0)
                            onNoteChange(activeTitle, next.text)
                        }
                        bodyField = next
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(PrimaryContainer),
                    visualTransformation = remember { MarkdownStyleTransformation() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 320.dp)
                        .testTag("note_body_input")
                )
            }
        }

        // Bottom Frosted-Glass Metadata Pill + Formatting Bar (auto-hides on scroll up, reappears on scroll down)
        AnimatedVisibility(
            visible = isFormatBarVisible,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow)
            ) + fadeIn(tween(200)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(180)
            ) + fadeOut(tween(150)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f),
                    border = BorderStroke(1.dp, GlassBorderWhite),
                    shadowElevation = 6.dp
                ) {
                    Text(
                        text = "$dateLabel • $words ${if (words == 1) "word" else "words"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .testTag("note_meta_text")
                    )
                }

                Surface(
                    modifier = Modifier
                        .widthIn(max = 560.dp)
                        .fillMaxWidth()
                        .staggeredEntrance(1)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = GlassShadowNavy,
                        spotColor = GlassShadowNavy
                    ),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.80f),
                border = BorderStroke(1.dp, GlassBorderWhite)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        ToolbarFormatButton(
                            icon = IconifyIcons.FormatBold,
                            contentDescription = "Bold",
                            onClick = { applyFormatting("**", "**") },
                            testTag = "format_bold_button"
                        )
                        ToolbarFormatButton(
                            icon = IconifyIcons.FormatItalic,
                            contentDescription = "Italic",
                            onClick = { applyFormatting("*", "*") },
                            testTag = "format_italic_button"
                        )
                        ToolbarFormatButton(
                            icon = IconifyIcons.ListView,
                            contentDescription = "Bulleted list",
                            onClick = { applyFormatting("- ", lineLevel = true) },
                            testTag = "format_bullet_button"
                        )
                        ToolbarFormatButton(
                            icon = IconifyIcons.TextTitle,
                            contentDescription = "Heading",
                            onClick = { applyFormatting("## ", lineLevel = true) },
                            testTag = "format_heading_button"
                        )
                        ToolbarFormatButton(
                            icon = if (isVoiceSpeaking) IconifyIcons.Stop else IconifyIcons.VolumeLoud,
                            contentDescription = if (isVoiceSpeaking) "Stop reading" else "Read note aloud",
                            onClick = { onToggleReadAloud(bodyField.text) },
                            testTag = "note_read_aloud_button"
                        )
                    }

                    val undoInteraction = remember { MutableInteractionSource() }
                    val undoTint by animateColorAsState(
                        targetValue = if (undoStack.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant,
                        animationSpec = tween(180),
                        label = "undo_tint"
                    )
                    IconButton(
                        onClick = {
                            if (undoStack.isNotEmpty()) {
                                val prev = undoStack.removeAt(undoStack.lastIndex)
                                bodyField = TextFieldValue(prev, TextRange(prev.length))
                                onNoteChange(activeTitle, prev)
                            }
                        },
                        enabled = undoStack.isNotEmpty(),
                        interactionSource = undoInteraction,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .pressBounceScale(undoInteraction, pressedScale = 0.88f, enabled = undoStack.isNotEmpty())
                            .testTag("format_undo_button")
                    ) {
                        Icon(
                            imageVector = IconifyIcons.Undo,
                            contentDescription = "Undo",
                            tint = undoTint
                        )
                    }
                }
            }
            }
        }
    }
}

@Composable
private fun ToolbarFormatButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    testTag: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    IconButton(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier
            .size(48.dp)
            .pressBounceScale(interactionSource, pressedScale = 0.88f)
            .clip(RoundedCornerShape(14.dp))
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
    }
}

private class MarkdownStyleTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val styled = buildAnnotatedString {
            append(raw)
            // Style ## Headings
            val headingRegex = Regex("(?m)^## .*$")
            headingRegex.findAll(raw).forEach { match ->
                addStyle(
                    SpanStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Primary),
                    match.range.first,
                    match.range.last + 1
                )
            }
            // Style **bold**
            val boldRegex = Regex("\\*\\*(.+?)\\*\\*")
            boldRegex.findAll(raw).forEach { match ->
                addStyle(
                    SpanStyle(fontWeight = FontWeight.Bold),
                    match.range.first,
                    match.range.last + 1
                )
            }
            // Style *italic*
            val italicRegex = Regex("(?<!\\*)\\*(?!\\*)(.+?)(?<!\\*)\\*(?!\\*)")
            italicRegex.findAll(raw).forEach { match ->
                addStyle(
                    SpanStyle(fontStyle = FontStyle.Italic),
                    match.range.first,
                    match.range.last + 1
                )
            }
        }
        return TransformedText(styled, OffsetMapping.Identity)
    }
}

private fun WorkspaceRepositoryCountWords(text: String): Int {
    val cleaned = text.replace(Regex("[#*\\-`]+"), " ").trim()
    return if (cleaned.isEmpty()) 0 else cleaned.split(Regex("\\s+")).size
}
