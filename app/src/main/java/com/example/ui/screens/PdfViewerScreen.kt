package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import com.example.ui.components.IconifyIcons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FileCategory
import com.example.data.local.LocalFileEntity
import com.example.data.local.PdfAnnotationEntity
import com.example.data.local.PdfAnnotationType
import com.example.ui.components.AppMotion
import com.example.ui.components.GlassCard
import com.example.ui.components.pressBounceScale
import com.example.ui.components.staggeredEntrance
import com.example.ui.theme.GlassBorderWhite
import com.example.ui.theme.GlassShadowNavy
import com.example.ui.theme.HighlightYellow
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.PrimaryFixed
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private enum class ReaderToolMode(val label: String, val icon: ImageVector) {
    PAGES("Pages", IconifyIcons.GridView),
    BOOKMARK("Bookmark", IconifyIcons.BookmarkLinear),
    HIGHLIGHT("Highlight", IconifyIcons.Highlight),
    DRAW("Draw", IconifyIcons.DrawPen),
    TEXT("Text", IconifyIcons.TextTitle),
    SIGN("Sign", IconifyIcons.Signature)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(
    file: LocalFileEntity?,
    annotations: List<PdfAnnotationEntity>,
    searchQuery: String,
    loadPageBitmap: suspend (LocalFileEntity, Int) -> Bitmap?,
    readNonPdfText: (LocalFileEntity) -> String,
    onSaveDocumentText: (LocalFileEntity, String) -> Unit = { _, _ -> },
    isVoiceSpeaking: Boolean = false,
    onToggleReadAloud: (String) -> Unit = {},
    onToggleBookmark: (Long, Int, Boolean) -> Unit,
    onAddAnnotation: (Long, Int, PdfAnnotationType, String) -> Unit,
    onDeleteAnnotation: (Long) -> Unit,
    isEditBarVisible: Boolean = false,
    onEditBarVisibleChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (file == null) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Document not found", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    var currentPage by remember(file.id) { mutableIntStateOf(0) }
    var zoomScale by remember(file.id) { mutableFloatStateOf(1f) }
    var activeTool by remember { mutableStateOf(ReaderToolMode.HIGHLIGHT) }
    var showPagesSheet by remember { mutableStateOf(false) }
    var pendingTextLocation by remember { mutableStateOf<Offset?>(null) }
    var pendingTextInput by remember { mutableStateOf("") }
    var showSignDialog by remember { mutableStateOf(false) }
    var isPageCountBarVisible by remember(file.id) { mutableStateOf(true) }

    val lazyListState = rememberLazyListState()
    val officeScrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    val pageAnnotations = remember(annotations, currentPage) {
        annotations.filter { it.pageIndex == currentPage }
    }
    val isCurrentPageBookmarked = remember(pageAnnotations) {
        pageAnnotations.any { it.type == PdfAnnotationType.BOOKMARK }
    }

    // Active drag drawing path in normalized 0..1 coordinates (tracked per page)
    val activeDragPoints = remember { mutableStateListOf<Offset>() }
    var activeDragPage by remember { mutableIntStateOf(-1) }
    var highlightDragStart by remember { mutableStateOf<Offset?>(null) }
    var highlightDragEnd by remember { mutableStateOf<Offset?>(null) }
    var highlightDragPage by remember { mutableIntStateOf(-1) }

    val totalPages = file.pageCount.coerceAtLeast(1)

    LaunchedEffect(lazyListState, file.id, file.category, totalPages) {
        if (file.category == FileCategory.PDF) {
            snapshotFlow {
                val layoutInfo = lazyListState.layoutInfo
                val visible = layoutInfo.visibleItemsInfo
                if (visible.isEmpty()) 0
                else {
                    val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                    visible.minByOrNull { item ->
                        abs((item.offset + item.size / 2) - viewportCenter)
                    }?.index ?: lazyListState.firstVisibleItemIndex
                }
            }.collect { idx ->
                currentPage = idx.coerceIn(0, (totalPages - 1).coerceAtLeast(0))
            }
        }
    }

    val pageBarNestedScroll = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput) {
                    if (available.y < -6f) {
                        isPageCountBarVisible = false
                    } else if (available.y > 6f) {
                        isPageCountBarVisible = true
                    }
                }
                return Offset.Zero
            }
        }
    }

    BackHandler(enabled = isEditBarVisible) {
        onEditBarVisibleChange(false)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(pageBarNestedScroll)
            .pointerInput(file.id, isEditBarVisible) {
                if (isEditBarVisible) return@pointerInput
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    var dySum = 0f
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val change = event.changes.firstOrNull() ?: break
                        if (!change.pressed) break
                        dySum += change.positionChange().y
                        if (dySum < -18f) {
                            isPageCountBarVisible = false
                            dySum = 0f
                        } else if (dySum > 18f) {
                            isPageCountBarVisible = true
                            dySum = 0f
                        }
                    }
                }
            }
    ) {
        if (file.category == FileCategory.PDF) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .align(Alignment.TopCenter)
                    .fillMaxSize()
                    .clipToBounds(),
                contentPadding = PaddingValues(
                    top = 8.dp,
                    bottom = if (isEditBarVisible) 148.dp else 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (searchQuery.isNotBlank()) {
                    item(key = "search_banner") {
                        val matchingNotes = annotations.filter {
                            it.type == PdfAnnotationType.TEXT && it.payload.contains(searchQuery, ignoreCase = true)
                        }
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            cornerRadius = 16.dp,
                            alpha = 0.85f
                        ) {
                            Column(Modifier.padding(14.dp)) {
                                Text(
                                    text = if (matchingNotes.isEmpty() && !file.snippetBody.contains(searchQuery, true)) {
                                        "No matches for \"$searchQuery\" on page ${currentPage + 1}"
                                    } else {
                                        "Found matches in document / notes (${matchingNotes.size} notes)"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = PrimaryContainer
                                )
                            }
                        }
                    }
                }

                items(count = totalPages, key = { it }) { pageIdx ->
                    val bmp by produceState<Bitmap?>(
                        initialValue = null,
                        key1 = file.id,
                        key2 = pageIdx,
                        key3 = file.lastModifiedAt
                    ) {
                        value = loadPageBitmap(file, pageIdx)
                    }
                    val itemAnnotations = remember(annotations, pageIdx) {
                        annotations.filter { it.pageIndex == pageIdx }
                    }
                    val aspect = if (bmp != null && bmp!!.height > 0) {
                        bmp!!.width.toFloat() / bmp!!.height.toFloat()
                    } else {
                        0.707f
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        BoxWithConstraints(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(aspect)
                                .graphicsLayer {
                                    scaleX = zoomScale
                                    scaleY = zoomScale
                                }
                                .pointerInput(activeTool, pageIdx, isEditBarVisible) {
                                    if (!isEditBarVisible) return@pointerInput
                                    detectTapGestures { offset ->
                                        currentPage = pageIdx
                                        val nx = (offset.x / size.width).coerceIn(0f, 1f)
                                        val ny = (offset.y / size.height).coerceIn(0f, 1f)
                                        when (activeTool) {
                                            ReaderToolMode.TEXT -> {
                                                pendingTextLocation = Offset(nx, ny)
                                            }
                                            ReaderToolMode.HIGHLIGHT -> {
                                                val x1 = (nx - 0.16f).coerceIn(0.04f, 0.75f)
                                                val x2 = (nx + 0.16f).coerceIn(x1 + 0.08f, 0.96f)
                                                val y1 = (ny - 0.025f).coerceIn(0.04f, 0.92f)
                                                val y2 = (ny + 0.025f).coerceIn(y1 + 0.03f, 0.96f)
                                                onAddAnnotation(
                                                    file.id,
                                                    pageIdx,
                                                    PdfAnnotationType.HIGHLIGHT,
                                                    "$x1,$y1,$x2,$y2"
                                                )
                                            }
                                            else -> Unit
                                        }
                                    }
                                }
                                .pointerInput(activeTool, pageIdx, isEditBarVisible) {
                                    if (!isEditBarVisible) return@pointerInput
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            currentPage = pageIdx
                                            val nx = (offset.x / size.width).coerceIn(0f, 1f)
                                            val ny = (offset.y / size.height).coerceIn(0f, 1f)
                                            if (activeTool == ReaderToolMode.DRAW) {
                                                activeDragPage = pageIdx
                                                activeDragPoints.clear()
                                                activeDragPoints.add(Offset(nx, ny))
                                            } else if (activeTool == ReaderToolMode.HIGHLIGHT) {
                                                highlightDragPage = pageIdx
                                                highlightDragStart = Offset(nx, ny)
                                                highlightDragEnd = Offset(nx, ny)
                                            }
                                        },
                                        onDrag = { change, _ ->
                                            val nx = (change.position.x / size.width).coerceIn(0f, 1f)
                                            val ny = (change.position.y / size.height).coerceIn(0f, 1f)
                                            if (activeTool == ReaderToolMode.DRAW) {
                                                activeDragPoints.add(Offset(nx, ny))
                                            } else if (activeTool == ReaderToolMode.HIGHLIGHT) {
                                                highlightDragEnd = Offset(nx, ny)
                                            }
                                        },
                                        onDragEnd = {
                                            if (activeTool == ReaderToolMode.DRAW && activeDragPoints.size > 1) {
                                                val encoded = activeDragPoints.joinToString(";") { "${it.x},${it.y}" }
                                                onAddAnnotation(file.id, pageIdx, PdfAnnotationType.INK, encoded)
                                                activeDragPoints.clear()
                                                activeDragPage = -1
                                            } else if (activeTool == ReaderToolMode.HIGHLIGHT) {
                                                val s = highlightDragStart
                                                val e = highlightDragEnd
                                                if (s != null && e != null) {
                                                    val x1 = minOf(s.x, e.x)
                                                    val y1 = minOf(s.y, e.y)
                                                    val x2 = maxOf(s.x, e.x).coerceAtLeast(x1 + 0.05f)
                                                    val y2 = maxOf(s.y, e.y).coerceAtLeast(y1 + 0.03f)
                                                    onAddAnnotation(
                                                        file.id,
                                                        pageIdx,
                                                        PdfAnnotationType.HIGHLIGHT,
                                                        "$x1,$y1,$x2,$y2"
                                                    )
                                                }
                                                highlightDragStart = null
                                                highlightDragEnd = null
                                                highlightDragPage = -1
                                            }
                                        }
                                    )
                                }
                        ) {
                            bmp?.let { bitmap ->
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Page ${pageIdx + 1} of ${file.name}",
                                    contentScale = ContentScale.FillBounds,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            // Saved Highlights, Ink Strokes, and Signatures Canvas Overlay
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                itemAnnotations.forEach { ann ->
                                    when (ann.type) {
                                        PdfAnnotationType.HIGHLIGHT -> {
                                            val parts = ann.payload.split(",").mapNotNull { it.toFloatOrNull() }
                                            if (parts.size == 4) {
                                                drawRect(
                                                    color = HighlightYellow,
                                                    topLeft = Offset(parts[0] * size.width, parts[1] * size.height),
                                                    size = Size(
                                                        (parts[2] - parts[0]) * size.width,
                                                        (parts[3] - parts[1]) * size.height
                                                    )
                                                )
                                            }
                                        }
                                        PdfAnnotationType.INK, PdfAnnotationType.SIGNATURE -> {
                                            val pts = ann.payload.split(";").mapNotNull { pair ->
                                                val xy = pair.split(",").mapNotNull { it.toFloatOrNull() }
                                                if (xy.size == 2) Offset(xy[0] * size.width, xy[1] * size.height) else null
                                            }
                                            if (pts.size > 1) {
                                                val path = Path().apply {
                                                    moveTo(pts.first().x, pts.first().y)
                                                    pts.drop(1).forEach { lineTo(it.x, it.y) }
                                                }
                                                drawPath(
                                                    path = path,
                                                    color = if (ann.type == PdfAnnotationType.SIGNATURE) {
                                                        Color(0xFF0C1B35)
                                                    } else {
                                                        PrimaryContainer
                                                    },
                                                    style = Stroke(
                                                        width = if (ann.type == PdfAnnotationType.SIGNATURE) 4.dp.toPx() else 3.dp.toPx(),
                                                        cap = StrokeCap.Round,
                                                        join = StrokeJoin.Round
                                                    )
                                                )
                                            }
                                        }
                                        else -> Unit
                                    }
                                }

                                // Active drag preview
                                if (activeDragPage == pageIdx && activeDragPoints.size > 1) {
                                    val path = Path().apply {
                                        moveTo(activeDragPoints.first().x * size.width, activeDragPoints.first().y * size.height)
                                        activeDragPoints.drop(1).forEach {
                                            lineTo(it.x * size.width, it.y * size.height)
                                        }
                                    }
                                    drawPath(
                                        path = path,
                                        color = PrimaryContainer,
                                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                    )
                                }
                                if (highlightDragPage == pageIdx) {
                                    val hStart = highlightDragStart
                                    val hEnd = highlightDragEnd
                                    if (hStart != null && hEnd != null) {
                                        val x1 = minOf(hStart.x, hEnd.x) * size.width
                                        val y1 = minOf(hStart.y, hEnd.y) * size.height
                                        val w = (maxOf(hStart.x, hEnd.x) - minOf(hStart.x, hEnd.x)) * size.width
                                        val h = (maxOf(hStart.y, hEnd.y) - minOf(hStart.y, hEnd.y)) * size.height
                                        drawRect(color = HighlightYellow, topLeft = Offset(x1, y1), size = Size(w, h))
                                    }
                                }
                            }

                            // Text Stamp Overlays
                            val widthPx = constraints.maxWidth
                            val heightPx = constraints.maxHeight
                            itemAnnotations.filter { it.type == PdfAnnotationType.TEXT }.forEach { noteAnn ->
                                val sep = noteAnn.payload.indexOf('|')
                                if (sep > 0) {
                                    val coords = noteAnn.payload.substring(0, sep).split(",").mapNotNull { it.toFloatOrNull() }
                                    val msg = noteAnn.payload.substring(sep + 1)
                                    if (coords.size == 2) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = PrimaryFixed.copy(alpha = 0.94f),
                                            shadowElevation = 3.dp,
                                            modifier = Modifier
                                                .offset {
                                                    IntOffset(
                                                        (coords[0] * widthPx).roundToInt().coerceIn(8, (widthPx - 160).coerceAtLeast(8)),
                                                        (coords[1] * heightPx).roundToInt().coerceIn(8, (heightPx - 64).coerceAtLeast(8))
                                                    )
                                                }
                                                .clickable { onDeleteAnnotation(noteAnn.id) }
                                        ) {
                                            Text(
                                                text = msg,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (pageIdx < totalPages - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp),
                                thickness = 1.dp,
                                color = GlassBorderWhite.copy(alpha = 0.70f)
                            )
                        }
                    }
                }

                if (isEditBarVisible) {
                    item(key = "edit_tip") {
                        val hint = when (activeTool) {
                            ReaderToolMode.HIGHLIGHT -> "Drag or tap anywhere on the page to add a yellow highlight"
                            ReaderToolMode.DRAW -> "Draw directly on the page with your finger"
                            ReaderToolMode.TEXT -> "Tap anywhere on the page to place a note (tap note to remove)"
                            ReaderToolMode.SIGN -> "Tap Sign in the toolbar to draw and stamp your signature"
                            else -> "Use the toolbar below to navigate or annotate"
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = hint,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            // Native Office / Text Reader & Editor opened directly on default background
            val initialText = remember(file.id, file.lastModifiedAt) { readNonPdfText(file) }
            var draftOfficeText by remember(file.id, initialText) { mutableStateOf(initialText) }

            Column(
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .align(Alignment.TopCenter)
                    .fillMaxSize()
                    .verticalScroll(officeScrollState)
                    .padding(horizontal = 20.dp)
                    .padding(top = 12.dp, bottom = if (isEditBarVisible) 148.dp else 96.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                AnimatedVisibility(
                    visible = searchQuery.isNotBlank(),
                    enter = AppMotion.SoftExpandEnter,
                    exit = AppMotion.SoftCollapseExit
                ) {
                    GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 16.dp, alpha = 0.85f) {
                        Column(Modifier.padding(14.dp)) {
                            Text(
                                text = if (!draftOfficeText.contains(searchQuery, true) && !file.snippetBody.contains(searchQuery, true)) {
                                    "No matches for \"$searchQuery\""
                                } else {
                                    "Found matches in ${file.name}"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = PrimaryContainer
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = file.snippetTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = { onToggleReadAloud(draftOfficeText) },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("office_read_aloud_button")
                        ) {
                            Icon(
                                imageVector = if (isVoiceSpeaking) IconifyIcons.Stop else IconifyIcons.VolumeLoud,
                                contentDescription = if (isVoiceSpeaking) "Stop reading" else "Read aloud",
                                tint = PrimaryContainer
                            )
                        }
                        if (isEditBarVisible) {
                            IconButton(
                                onClick = {
                                    onSaveDocumentText(file, draftOfficeText)
                                    onEditBarVisibleChange(false)
                                },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("office_edit_save_button")
                            ) {
                                Icon(
                                    imageVector = IconifyIcons.Save,
                                    contentDescription = "Save changes",
                                    tint = PrimaryContainer
                                )
                            }
                        }
                    }
                }

                if (isEditBarVisible) {
                    OutlinedTextField(
                        value = draftOfficeText,
                        onValueChange = { draftOfficeText = it },
                        minLines = 6,
                        maxLines = 16,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.56f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("office_editor_input")
                    )
                    Button(
                        onClick = {
                            onSaveDocumentText(file, draftOfficeText)
                            onEditBarVisibleChange(false)
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(IconifyIcons.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Save Document")
                    }
                } else {
                    if (file.category == FileCategory.EXCEL) {
                        val parsedRows = remember(draftOfficeText) {
                            parseSpreadsheetRows(draftOfficeText)
                        }
                        if (parsedRows.chartEntries.isNotEmpty()) {
                            SpreadsheetBarChart(entries = parsedRows.chartEntries)
                        }
                        if (parsedRows.tableRows.isNotEmpty()) {
                            SpreadsheetTableView(rows = parsedRows.tableRows)
                        }
                    }
                    Text(
                        text = draftOfficeText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f)
                    )
                }
            }
        }

        // Bottom Floating Page Count Bar (auto-hides on scroll up, reappears on scroll down) + PDF Edit Dock
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .staggeredEntrance(0),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AnimatedVisibility(
                visible = isPageCountBarVisible || isEditBarVisible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow)
                ) + fadeIn(tween(200)),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(180)
                ) + fadeOut(tween(150))
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                    border = BorderStroke(1.dp, GlassBorderWhite),
                    shadowElevation = 6.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        IconButton(
                            onClick = {
                                if (currentPage > 0) {
                                    val target = currentPage - 1
                                    currentPage = target
                                    if (file.category == FileCategory.PDF) {
                                        coroutineScope.launch { lazyListState.animateScrollToItem(target) }
                                    }
                                }
                            },
                            enabled = currentPage > 0,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("pdf_prev_page_button")
                        ) {
                            Icon(IconifyIcons.ChevronLeft, contentDescription = "Previous page")
                        }
                        Text(
                            text = "${currentPage + 1} / $totalPages",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(
                            onClick = {
                                if (currentPage < totalPages - 1) {
                                    val target = currentPage + 1
                                    currentPage = target
                                    if (file.category == FileCategory.PDF) {
                                        coroutineScope.launch { lazyListState.animateScrollToItem(target) }
                                    }
                                }
                            },
                            enabled = currentPage < totalPages - 1,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("pdf_next_page_button")
                        ) {
                            Icon(IconifyIcons.ChevronRight, contentDescription = "Next page")
                        }

                        if (file.category == FileCategory.PDF) {
                            IconButton(
                                onClick = { zoomScale = (zoomScale - 0.25f).coerceAtLeast(1f) },
                                enabled = zoomScale > 1f,
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("pdf_zoom_out_button")
                            ) {
                                Icon(IconifyIcons.ZoomOut, contentDescription = "Zoom out", modifier = Modifier.size(18.dp))
                            }
                            IconButton(
                                onClick = { zoomScale = (zoomScale + 0.25f).coerceAtMost(2.5f) },
                                enabled = zoomScale < 2.5f,
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("pdf_zoom_in_button")
                            ) {
                                Icon(IconifyIcons.ZoomIn, contentDescription = "Zoom in", modifier = Modifier.size(18.dp))
                            }
                        }

                        IconButton(
                            onClick = {
                                val spoken = "${file.snippetTitle}. ${file.snippetBody}"
                                onToggleReadAloud(spoken)
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("pdf_read_aloud_button")
                        ) {
                            Icon(
                                imageVector = if (isVoiceSpeaking) IconifyIcons.Stop else IconifyIcons.VolumeLoud,
                                contentDescription = if (isVoiceSpeaking) "Stop read aloud" else "Read document aloud",
                                tint = PrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        if (isEditBarVisible) {
                            IconButton(
                                onClick = { onEditBarVisibleChange(false) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("pdf_edit_close_button")
                            ) {
                                Icon(
                                    imageVector = IconifyIcons.Close,
                                    contentDescription = "Hide edit bar",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Floating Frosted Bottom Tools Dock (hidden unless Edit is toggled on)
            AnimatedVisibility(
                visible = isEditBarVisible && file.category == FileCategory.PDF,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = spring(dampingRatio = 0.80f, stiffness = Spring.StiffnessMediumLow)
                ) + fadeIn(tween(200)),
                exit = slideOutVertically(
                    targetOffsetY = { it / 2 },
                    animationSpec = tween(160)
                ) + fadeOut(tween(150))
            ) {
                Surface(
                    modifier = Modifier
                        .widthIn(max = 560.dp)
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(28.dp), ambientColor = GlassShadowNavy, spotColor = GlassShadowNavy),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.80f),
                    border = BorderStroke(1.dp, GlassBorderWhite)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ReaderToolMode.entries.forEach { mode ->
                            val isBookmarkTool = mode == ReaderToolMode.BOOKMARK
                            val selected = if (isBookmarkTool) isCurrentPageBookmarked else activeTool == mode
                            val tint by animateColorAsState(
                                targetValue = if (selected) PrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                animationSpec = tween(200),
                                label = "pdf_tool_tint"
                            )
                            val bg by animateColorAsState(
                                targetValue = if (selected) PrimaryFixed.copy(alpha = 0.82f) else Color.Transparent,
                                animationSpec = tween(220),
                                label = "pdf_tool_bg"
                            )
                            val iconScale by animateFloatAsState(
                                targetValue = if (selected) 1.12f else 1f,
                                animationSpec = AppMotion.BouncySpring,
                                label = "pdf_tool_scale"
                            )
                            val toolInteraction = remember(mode) { MutableInteractionSource() }
                            val icon = if (isBookmarkTool && isCurrentPageBookmarked) IconifyIcons.BookmarkBold else mode.icon

                            Column(
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .pressBounceScale(toolInteraction, pressedScale = 0.90f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(bg)
                                    .clickable(
                                        interactionSource = toolInteraction,
                                        indication = androidx.compose.material3.ripple()
                                    ) {
                                        when (mode) {
                                            ReaderToolMode.PAGES -> showPagesSheet = true
                                            ReaderToolMode.BOOKMARK -> onToggleBookmark(file.id, currentPage, isCurrentPageBookmarked)
                                            ReaderToolMode.SIGN -> showSignDialog = true
                                            else -> activeTool = mode
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                    .testTag("pdf_tool_${mode.name.lowercase()}"),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = mode.label,
                                    tint = tint,
                                    modifier = Modifier
                                        .size(21.dp)
                                        .graphicsLayer {
                                            scaleX = iconScale
                                            scaleY = iconScale
                                        }
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = mode.label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 11.sp,
                                    color = tint,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Pages & Bookmarks Bottom Sheet
    if (showPagesSheet) {
        val bookmarkedPages = annotations
            .filter { it.type == PdfAnnotationType.BOOKMARK }
            .map { it.pageIndex }
            .toSet()
        ModalBottomSheet(
            onDismissRequest = { showPagesSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Pages ($totalPages)", style = MaterialTheme.typography.titleLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    (0 until totalPages).forEach { idx ->
                        val isCurr = idx == currentPage
                        val hasBookmark = bookmarkedPages.contains(idx)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isCurr) PrimaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    currentPage = idx
                                    if (file.category == FileCategory.PDF) {
                                        coroutineScope.launch { lazyListState.animateScrollToItem(idx) }
                                    }
                                    showPagesSheet = false
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${idx + 1}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (isCurr) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (hasBookmark) {
                                        Spacer(Modifier.width(4.dp))
                                        Icon(
                                            IconifyIcons.BookmarkBold,
                                            contentDescription = "Bookmarked",
                                            tint = if (isCurr) MaterialTheme.colorScheme.onPrimary else PrimaryContainer,
                                            modifier = Modifier.size(16.dp)
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

    // Add Text Note Dialog
    val textLoc = pendingTextLocation
    if (textLoc != null) {
        AlertDialog(
            onDismissRequest = {
                pendingTextLocation = null
                pendingTextInput = ""
            },
            title = { Text("Add Local Note") },
            text = {
                OutlinedTextField(
                    value = pendingTextInput,
                    onValueChange = { pendingTextInput = it },
                    label = { Text("Annotation text") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (pendingTextInput.isNotBlank()) {
                            onAddAnnotation(
                                file.id,
                                currentPage,
                                PdfAnnotationType.TEXT,
                                "${textLoc.x},${textLoc.y}|${pendingTextInput.trim()}"
                            )
                        }
                        pendingTextLocation = null
                        pendingTextInput = ""
                    }
                ) {
                    Text("Place")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    pendingTextLocation = null
                    pendingTextInput = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Signature Pad Dialog
    if (showSignDialog) {
        val sigPoints = remember { mutableStateListOf<Offset>() }
        AlertDialog(
            onDismissRequest = { showSignDialog = false },
            title = { Text("Draw Signature") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Sign inside the box below to stamp your signature.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            val nx = 0.55f + (offset.x / size.width).coerceIn(0f, 1f) * 0.36f
                                            val ny = 0.78f + (offset.y / size.height).coerceIn(0f, 1f) * 0.14f
                                            sigPoints.add(Offset(nx, ny))
                                        },
                                        onDrag = { change, _ ->
                                            val nx = 0.55f + (change.position.x / size.width).coerceIn(0f, 1f) * 0.36f
                                            val ny = 0.78f + (change.position.y / size.height).coerceIn(0f, 1f) * 0.14f
                                            sigPoints.add(Offset(nx, ny))
                                        }
                                    )
                                }
                        ) {
                            if (sigPoints.size > 1) {
                                val path = Path().apply {
                                    val first = sigPoints.first()
                                    moveTo(
                                        ((first.x - 0.55f) / 0.36f) * size.width,
                                        ((first.y - 0.78f) / 0.14f) * size.height
                                    )
                                    sigPoints.drop(1).forEach { pt ->
                                        lineTo(
                                            ((pt.x - 0.55f) / 0.36f) * size.width,
                                            ((pt.y - 0.78f) / 0.14f) * size.height
                                        )
                                    }
                                }
                                drawPath(
                                    path = path,
                                    color = Color(0xFF0C1B35),
                                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (sigPoints.size > 1) {
                            val encoded = sigPoints.joinToString(";") { "${it.x},${it.y}" }
                            onAddAnnotation(file.id, currentPage, PdfAnnotationType.SIGNATURE, encoded)
                        }
                        showSignDialog = false
                    }
                ) {
                    Text("Stamp Signature")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private data class SpreadsheetData(
    val tableRows: List<List<String>>,
    val chartEntries: List<Pair<String, Float>>
)

private fun parseSpreadsheetRows(raw: String): SpreadsheetData {
    val lines = raw.lines()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
    val table = mutableListOf<List<String>>()
    val chart = mutableListOf<Pair<String, Float>>()
    lines.forEach { line ->
        val cols = when {
            line.contains(",") -> line.split(",").map { it.trim() }
            line.contains("|") -> line.split("|").map { it.trim() }
            line.contains(":") -> line.split(":").map { it.trim() }
            else -> emptyList()
        }
        if (cols.size >= 2) {
            table.add(cols)
            val numeric = cols.last()
                .replace(Regex("[^0-9.\\-]"), "")
                .toFloatOrNull()
            if (numeric != null && numeric > 0f) {
                chart.add(cols.first().take(10) to numeric)
            }
        }
    }
    return SpreadsheetData(tableRows = table, chartEntries = chart.take(8))
}

@Composable
private fun SpreadsheetBarChart(entries: List<Pair<String, Float>>) {
    val maxVal = entries.maxOfOrNull { it.second }?.coerceAtLeast(1f) ?: 1f
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.78f),
        border = BorderStroke(1.dp, GlassBorderWhite),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Summary Chart",
                style = MaterialTheme.typography.labelMedium,
                color = PrimaryContainer
            )
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                val barCount = entries.size.coerceAtLeast(1)
                val slotWidth = size.width / barCount
                val barWidth = (slotWidth * 0.56f).coerceAtMost(36.dp.toPx())
                entries.forEachIndexed { idx, (_, value) ->
                    val frac = (value / maxVal).coerceIn(0.08f, 1f)
                    val barHeight = frac * (size.height - 10.dp.toPx())
                    val left = idx * slotWidth + (slotWidth - barWidth) / 2f
                    val top = size.height - barHeight
                    drawRoundRect(
                        color = PrimaryContainer.copy(alpha = 0.45f + 0.55f * frac),
                        topLeft = Offset(left, top),
                        size = Size(barWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                entries.forEach { (label, _) ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun SpreadsheetTableView(rows: List<List<String>>) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, GlassBorderWhite),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            rows.take(12).forEachIndexed { rowIdx, cols ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (rowIdx == 0) PrimaryFixed.copy(alpha = 0.65f)
                            else Color.Transparent
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    cols.forEach { cell ->
                        Text(
                            text = cell,
                            style = if (rowIdx == 0) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
