package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.components.GlassCard
import com.example.ui.components.IconifyIcons
import com.example.ui.components.pressBounceScale
import com.example.ui.components.staggeredEntrance
import com.example.ui.theme.GlassBorderWhite
import com.example.ui.theme.OnPrimary
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.PrimaryFixed
import kotlin.math.roundToInt

@Composable
fun ImageEditorScreen(
    loadBitmapFromUri: (Uri) -> Bitmap?,
    onExportPdf: (Bitmap, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var sourceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var titleInput by remember { mutableStateOf("Edited Document") }
    var brightness by remember { mutableFloatStateOf(0f) }
    var contrast by remember { mutableFloatStateOf(1f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var rotationDegrees by remember { mutableIntStateOf(0) }
    var flipHorizontal by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            sourceBitmap = loadBitmapFromUri(uri)
            brightness = 0f
            contrast = 1f
            saturation = 1f
            rotationDegrees = 0
            flipHorizontal = false
        }
    }

    val liveAndroidMatrix = remember(brightness, contrast, saturation) {
        buildAdjustmentColorMatrix(brightness, contrast, saturation)
    }
    val composeColorMatrix = remember(liveAndroidMatrix) {
        androidx.compose.ui.graphics.ColorMatrix(liveAndroidMatrix.array)
    }

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Preview Card
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredEntrance(0),
                cornerRadius = 24.dp,
                alpha = 0.78f
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val bmp = sourceBitmap
                    if (bmp == null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.7f))
                                .clickable {
                                    imagePicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                .testTag("image_editor_pick_empty"),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = PrimaryFixed,
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = IconifyIcons.GalleryAdd,
                                        contentDescription = null,
                                        tint = PrimaryContainer,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "Choose a photo to edit",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Adjust tone, rotate, flip, and export to PDF",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerLow),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Edited photo preview",
                                contentScale = ContentScale.Fit,
                                colorFilter = ColorFilter.colorMatrix(composeColorMatrix),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        rotationZ = rotationDegrees.toFloat()
                                        scaleX = if (flipHorizontal) -1f else 1f
                                    }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        EditorTransformChip(
                            label = "Pick photo",
                            icon = IconifyIcons.GalleryAdd,
                            onClick = {
                                imagePicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            testTag = "image_editor_pick_button",
                            modifier = Modifier.weight(1f)
                        )
                        EditorTransformChip(
                            label = "Rotate",
                            icon = IconifyIcons.RotateRight,
                            onClick = { rotationDegrees = (rotationDegrees + 90) % 360 },
                            testTag = "image_editor_rotate_button",
                            modifier = Modifier.weight(1f)
                        )
                        EditorTransformChip(
                            label = "Flip",
                            icon = IconifyIcons.FlipHorizontal,
                            onClick = { flipHorizontal = !flipHorizontal },
                            testTag = "image_editor_flip_button",
                            modifier = Modifier.weight(1f)
                        )
                        EditorTransformChip(
                            label = "Reset",
                            icon = IconifyIcons.Undo,
                            onClick = {
                                brightness = 0f
                                contrast = 1f
                                saturation = 1f
                                rotationDegrees = 0
                                flipHorizontal = false
                            },
                            testTag = "image_editor_reset_button",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Color & Tone Adjustments Card
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredEntrance(1),
                cornerRadius = 24.dp,
                alpha = 0.72f
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AdjustmentSliderRow(
                        icon = IconifyIcons.Sun,
                        label = "Brightness",
                        valueLabel = brightness.roundToInt().toString(),
                        value = brightness,
                        valueRange = -100f..100f,
                        onValueChange = { brightness = it }
                    )
                    AdjustmentSliderRow(
                        icon = IconifyIcons.Contrast,
                        label = "Contrast",
                        valueLabel = String.format("%.2fx", contrast),
                        value = contrast,
                        valueRange = 0.5f..2.0f,
                        onValueChange = { contrast = it }
                    )
                    AdjustmentSliderRow(
                        icon = IconifyIcons.Droplet,
                        label = "Saturation",
                        valueLabel = String.format("%.2fx", saturation),
                        value = saturation,
                        valueRange = 0f..2.0f,
                        onValueChange = { saturation = it }
                    )
                }
            }

            // Export Card
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredEntrance(2),
                cornerRadius = 24.dp,
                alpha = 0.72f
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("Export filename") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("image_editor_title_input")
                    )
                    val exportInteraction = remember { MutableInteractionSource() }
                    Button(
                        onClick = {
                            val bmp = sourceBitmap ?: return@Button
                            val exported = renderTransformedBitmap(
                                source = bmp,
                                colorMatrix = liveAndroidMatrix,
                                rotationDegrees = rotationDegrees,
                                flipHorizontal = flipHorizontal
                            )
                            onExportPdf(exported, titleInput)
                        },
                        enabled = sourceBitmap != null,
                        interactionSource = exportInteraction,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryContainer,
                            contentColor = OnPrimary
                        ),
                        contentPadding = PaddingValues(vertical = 14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .pressBounceScale(exportInteraction, pressedScale = 0.96f, enabled = sourceBitmap != null)
                            .testTag("image_editor_export_pdf_button")
                    ) {
                        Icon(
                            imageVector = IconifyIcons.FilePdfLinear,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Export as PDF", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun EditorTransformChip(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    Surface(
        modifier = modifier
            .height(52.dp)
            .pressBounceScale(interaction, pressedScale = 0.94f)
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = interaction,
                indication = androidx.compose.material3.ripple(),
                onClick = onClick
            )
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, GlassBorderWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = PrimaryContainer,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun AdjustmentSliderRow(
    icon: ImageVector,
    label: String,
    valueLabel: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryContainer,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = valueLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = PrimaryContainer,
                activeTrackColor = PrimaryContainer
            )
        )
    }
}

private fun buildAdjustmentColorMatrix(
    brightness: Float,
    contrast: Float,
    saturation: Float
): ColorMatrix {
    val satMatrix = ColorMatrix().apply { setSaturation(saturation) }
    val translate = (1f - contrast) * 128f + brightness
    val contrastBrightness = ColorMatrix(
        floatArrayOf(
            contrast, 0f, 0f, 0f, translate,
            0f, contrast, 0f, 0f, translate,
            0f, 0f, contrast, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        )
    )
    contrastBrightness.preConcat(satMatrix)
    return contrastBrightness
}

private fun renderTransformedBitmap(
    source: Bitmap,
    colorMatrix: ColorMatrix,
    rotationDegrees: Int,
    flipHorizontal: Boolean
): Bitmap {
    val filtered = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
    val filterCanvas = Canvas(filtered)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        colorFilter = ColorMatrixColorFilter(colorMatrix)
    }
    filterCanvas.drawBitmap(source, 0f, 0f, paint)

    val matrix = Matrix().apply {
        if (flipHorizontal) {
            postScale(-1f, 1f)
        }
        if (rotationDegrees != 0) {
            postRotate(rotationDegrees.toFloat())
        }
    }
    return Bitmap.createBitmap(filtered, 0, 0, filtered.width, filtered.height, matrix, true)
}
