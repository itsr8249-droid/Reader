package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.example.ui.components.IconifyIcons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.example.data.local.FileCategory
import com.example.data.local.LocalFileEntity
import com.example.ui.components.AppMotion
import com.example.ui.components.GlassCard
import com.example.ui.components.staggeredEntrance
import com.example.ui.theme.ErrorContainer
import com.example.ui.theme.OnErrorContainer
import com.example.ui.theme.OnPrimary
import com.example.ui.theme.OnPrimaryFixedVariant
import com.example.ui.theme.OnSecondaryFixedVariant
import com.example.ui.theme.OnTertiaryFixedVariant
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.PrimaryFixed
import com.example.ui.theme.SecondaryFixed
import com.example.ui.theme.TertiaryFixed
import com.example.ui.viewmodel.ToolFeedback
import java.io.File

private enum class PdfToolKind(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val containerColor: Color,
    val contentColor: Color
) {
    MERGE("Merge", "Combine PDFs", IconifyIcons.Layers, PrimaryFixed, OnPrimaryFixedVariant),
    SPLIT("Split", "Divide pages", IconifyIcons.Scissors, SecondaryFixed, OnSecondaryFixedVariant),
    COMPRESS("Compress", "Shrink file size", IconifyIcons.Compress, TertiaryFixed, OnTertiaryFixedVariant),
    ROTATE("Rotate", "Adjust orientation", IconifyIcons.RotateRight, PrimaryFixed, OnPrimaryFixedVariant),
    EXTRACT("Extract pages", "Pull key pages", IconifyIcons.ExtractPages, SecondaryFixed, OnSecondaryFixedVariant),
    WATERMARK("Watermark", "Stamp PDF pages", IconifyIcons.Watermark, TertiaryFixed, OnTertiaryFixedVariant),
    PROTECT("Protect", "Encrypt with password", IconifyIcons.Lock, ErrorContainer, OnErrorContainer),
    SCAN("Scan to PDF", "Camera capture", IconifyIcons.Scanner, PrimaryFixed, OnPrimaryFixedVariant),
    IMAGES("Images to PDF", "Convert gallery", IconifyIcons.GalleryAdd, SecondaryFixed, OnSecondaryFixedVariant),
    IMAGE_EDITOR("Image editor", "Adjust & export", IconifyIcons.ImageEdit, TertiaryFixed, OnTertiaryFixedVariant)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    files: List<LocalFileEntity>,
    toolBusy: Boolean,
    toolFeedback: ToolFeedback?,
    onCreateScanTempUri: () -> Pair<Uri, File>,
    onMergePdfs: (List<LocalFileEntity>) -> Unit,
    onSplitPdf: (LocalFileEntity) -> Unit,
    onCompressPdf: (LocalFileEntity, Int) -> Unit,
    onRotatePdf: (LocalFileEntity, Int) -> Unit,
    onExtractPages: (LocalFileEntity, String) -> Unit,
    onWatermarkPdf: (LocalFileEntity, String) -> Unit = { _, _ -> },
    onProtectPdf: (LocalFileEntity, String) -> Unit,
    onImagesToPdf: (List<Uri>, String) -> Unit,
    onOpenImageEditor: () -> Unit = {},
    onOpenGeneratedFile: (Long) -> Unit,
    onDismissFeedback: () -> Unit,
    modifier: Modifier = Modifier
) {
    val unlockedPdfs = remember(files) {
        files.filter { it.category == FileCategory.PDF && !it.isLocked }
    }
    var activeTool by remember { mutableStateOf<PdfToolKind?>(null) }
    var pendingScanFile by remember { mutableStateOf<File?>(null) }

    val galleryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 12)
    ) { uris ->
        if (uris.isNotEmpty()) {
            onImagesToPdf(uris, "Images")
        }
    }

    val cameraScanLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val file = pendingScanFile
        if (success && file != null && file.exists()) {
            onImagesToPdf(listOf(file.toUri()), "Scan")
        }
        pendingScanFile = null
    }

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
            AnimatedVisibility(
                visible = toolBusy,
                enter = AppMotion.SoftExpandEnter,
                exit = AppMotion.SoftCollapseExit
            ) {
                GlassCard(modifier = Modifier.fillMaxWidth(), alpha = 0.82f) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp,
                            color = PrimaryContainer
                        )
                        Text(
                            text = "Processing…",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = toolFeedback != null,
                enter = AppMotion.PopCardEnter,
                exit = AppMotion.PopCardExit
            ) {
                val feedback = toolFeedback
                if (feedback != null) {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tool_feedback_banner"),
                        cornerRadius = 20.dp,
                        alpha = 0.86f
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = feedback.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = feedback.message,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            val genId = feedback.generatedFileId
                            if (genId != null) {
                                Button(
                                    onClick = {
                                        onDismissFeedback()
                                        onOpenGeneratedFile(genId)
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PrimaryContainer,
                                        contentColor = OnPrimary
                                    ),
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Text("Open")
                                }
                            } else {
                                TextButton(onClick = onDismissFeedback) {
                                    Text("Dismiss")
                                }
                            }
                        }
                    }
                }
            }

            // 2-Column Tool Grid
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                PdfToolKind.entries.chunked(2).forEachIndexed { rowIdx, pair ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .staggeredEntrance(rowIdx),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        pair.forEach { tool ->
                            GlassCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(142.dp)
                                    .testTag("tool_tile_${tool.name.lowercase()}"),
                                cornerRadius = 24.dp,
                                alpha = 0.68f,
                                onClick = {
                                    when (tool) {
                                        PdfToolKind.IMAGES -> galleryPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                        PdfToolKind.SCAN -> {
                                            val (uri, file) = onCreateScanTempUri()
                                            pendingScanFile = file
                                            cameraScanLauncher.launch(uri)
                                        }
                                        PdfToolKind.IMAGE_EDITOR -> onOpenImageEditor()
                                        else -> activeTool = tool
                                    }
                                }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(18.dp),
                                    verticalArrangement = Arrangement.SpaceBetween,
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = tool.containerColor,
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = tool.icon,
                                                contentDescription = null,
                                                tint = tool.contentColor,
                                                modifier = Modifier.size(26.dp)
                                            )
                                        }
                                    }
                                    Column {
                                        Text(
                                            text = tool.title,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = tool.subtitle,
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
                }
            }
        }
    }

    val selectedTool = activeTool
    if (selectedTool != null) {
        PdfToolConfigSheet(
            tool = selectedTool,
            availablePdfs = unlockedPdfs,
            onDismiss = { activeTool = null },
            onMerge = { list ->
                activeTool = null
                onMergePdfs(list)
            },
            onSplit = { file ->
                activeTool = null
                onSplitPdf(file)
            },
            onCompress = { file, quality ->
                activeTool = null
                onCompressPdf(file, quality)
            },
            onRotate = { file, deg ->
                activeTool = null
                onRotatePdf(file, deg)
            },
            onExtract = { file, pages ->
                activeTool = null
                onExtractPages(file, pages)
            },
            onWatermark = { file, stamp ->
                activeTool = null
                onWatermarkPdf(file, stamp)
            },
            onProtect = { file, pass ->
                activeTool = null
                onProtectPdf(file, pass)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PdfToolConfigSheet(
    tool: PdfToolKind,
    availablePdfs: List<LocalFileEntity>,
    onDismiss: () -> Unit,
    onMerge: (List<LocalFileEntity>) -> Unit,
    onSplit: (LocalFileEntity) -> Unit,
    onCompress: (LocalFileEntity, Int) -> Unit,
    onRotate: (LocalFileEntity, Int) -> Unit,
    onExtract: (LocalFileEntity, String) -> Unit,
    onWatermark: (LocalFileEntity, String) -> Unit,
    onProtect: (LocalFileEntity, String) -> Unit
) {
    val multiSelected = remember { mutableStateListOf<Long>() }
    var singleSelectedId by remember { mutableStateOf(availablePdfs.firstOrNull()?.id) }
    var rotateDegrees by remember { mutableIntStateOf(90) }
    var compressQuality by remember { mutableIntStateOf(55) }
    var pagesInput by remember { mutableStateOf("1, 2") }
    var watermarkInput by remember { mutableStateOf("CONFIDENTIAL") }
    var password by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = tool.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = tool.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (availablePdfs.isEmpty()) {
                Text(
                    text = "No unlocked PDF files available yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            } else if (tool == PdfToolKind.MERGE) {
                Text("Select 2 or more PDFs:", style = MaterialTheme.typography.labelLarge)
                availablePdfs.forEach { pdf ->
                    val checked = multiSelected.contains(pdf.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                if (checked) multiSelected.remove(pdf.id)
                                else multiSelected.add(pdf.id)
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = {
                                if (checked) multiSelected.remove(pdf.id)
                                else multiSelected.add(pdf.id)
                            }
                        )
                        Text(
                            text = "${pdf.name} (${pdf.pageCount}p)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                Text("Choose a PDF:", style = MaterialTheme.typography.labelLarge)
                availablePdfs.forEach { pdf ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { singleSelectedId = pdf.id }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = singleSelectedId == pdf.id,
                            onClick = { singleSelectedId = pdf.id }
                        )
                        Text(
                            text = "${pdf.name} (${pdf.pageCount}p)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                when (tool) {
                    PdfToolKind.ROTATE -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(90, 180, 270).forEach { deg ->
                                val selected = rotateDegrees == deg
                                val chipBg by animateColorAsState(
                                    targetValue = if (selected) PrimaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                                    animationSpec = tween(200),
                                    label = "rotate_chip_bg"
                                )
                                val chipFg by animateColorAsState(
                                    targetValue = if (selected) OnPrimary else MaterialTheme.colorScheme.onSurface,
                                    animationSpec = tween(200),
                                    label = "rotate_chip_fg"
                                )
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = chipBg,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { rotateDegrees = deg }
                                ) {
                                    Text(
                                        text = "${deg}°",
                                        color = chipFg,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                    }
                    PdfToolKind.COMPRESS -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(75 to "Light", 55 to "Balanced", 35 to "Compact").forEach { (q, lbl) ->
                                val selected = compressQuality == q
                                val chipBg by animateColorAsState(
                                    targetValue = if (selected) PrimaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                                    animationSpec = tween(200),
                                    label = "compress_chip_bg"
                                )
                                val chipFg by animateColorAsState(
                                    targetValue = if (selected) OnPrimary else MaterialTheme.colorScheme.onSurface,
                                    animationSpec = tween(200),
                                    label = "compress_chip_fg"
                                )
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = chipBg,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { compressQuality = q }
                                ) {
                                    Text(
                                        text = lbl,
                                        color = chipFg,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                    }
                    PdfToolKind.EXTRACT -> {
                        OutlinedTextField(
                            value = pagesInput,
                            onValueChange = { pagesInput = it },
                            label = { Text("Pages (e.g. 1, 3)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    PdfToolKind.WATERMARK -> {
                        OutlinedTextField(
                            value = watermarkInput,
                            onValueChange = { watermarkInput = it },
                            label = { Text("Watermark text") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    PdfToolKind.PROTECT -> {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password (min 4 chars)") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    else -> Unit
                }
            }

            val selectedSingle = availablePdfs.firstOrNull { it.id == singleSelectedId }
            val actionEnabled = when (tool) {
                PdfToolKind.MERGE -> multiSelected.size >= 2
                PdfToolKind.WATERMARK -> selectedSingle != null && watermarkInput.isNotBlank()
                PdfToolKind.PROTECT -> selectedSingle != null && password.length >= 4
                else -> selectedSingle != null
            }

            Button(
                onClick = {
                    when (tool) {
                        PdfToolKind.MERGE -> onMerge(availablePdfs.filter { multiSelected.contains(it.id) })
                        PdfToolKind.SPLIT -> selectedSingle?.let(onSplit)
                        PdfToolKind.COMPRESS -> selectedSingle?.let { onCompress(it, compressQuality) }
                        PdfToolKind.ROTATE -> selectedSingle?.let { onRotate(it, rotateDegrees) }
                        PdfToolKind.EXTRACT -> selectedSingle?.let { onExtract(it, pagesInput) }
                        PdfToolKind.WATERMARK -> selectedSingle?.let { onWatermark(it, watermarkInput) }
                        PdfToolKind.PROTECT -> selectedSingle?.let { onProtect(it, password) }
                        else -> Unit
                    }
                },
                enabled = actionEnabled,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryContainer,
                    contentColor = OnPrimary
                ),
                contentPadding = PaddingValues(vertical = 14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
            ) {
                Text("Run ${tool.title}", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
