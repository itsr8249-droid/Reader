package com.example.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.example.data.local.PhotoNoteEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.IconifyIcons
import com.example.ui.components.staggeredEntrance
import com.example.ui.theme.OnPrimary
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.PrimaryFixed
import com.example.ui.viewmodel.WorkspaceViewModel
import java.io.File

@Composable
fun PhotoNotesScreen(
    photoNotes: List<PhotoNoteEntity>,
    loadWorkspaceBitmap: (String) -> Bitmap?,
    onCreateCameraTempUri: () -> Pair<Uri, File>,
    onAddPhotoNote: (String, String, Uri) -> Unit,
    onDeletePhotoNote: (PhotoNoteEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var pendingImageUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCameraFile by remember { mutableStateOf<File?>(null) }
    var titleInput by remember { mutableStateOf("") }
    var captionInput by remember { mutableStateOf("") }
    var selectedPreviewNote by remember { mutableStateOf<PhotoNoteEntity?>(null) }

    val galleryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            pendingImageUri = uri
            titleInput = "Photo Note"
            captionInput = ""
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val file = pendingCameraFile
        if (success && file != null && file.exists()) {
            pendingImageUri = file.toUri()
            titleInput = "Captured Note"
            captionInput = ""
        }
        pendingCameraFile = null
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
            // Capture Actions Bar
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggeredEntrance(0),
                cornerRadius = 24.dp,
                alpha = 0.72f
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            galleryPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryContainer,
                            contentColor = OnPrimary
                        ),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("photo_notes_add_gallery")
                    ) {
                        Icon(
                            imageVector = IconifyIcons.GalleryAdd,
                            contentDescription = null,
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("From Gallery", style = MaterialTheme.typography.labelLarge)
                    }

                    OutlinedButton(
                        onClick = {
                            val (uri, file) = onCreateCameraTempUri()
                            pendingCameraFile = file
                            cameraLauncher.launch(uri)
                        },
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("photo_notes_add_camera")
                    ) {
                        Icon(
                            imageVector = IconifyIcons.Camera,
                            contentDescription = null,
                            tint = PrimaryContainer,
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Take Photo", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            if (photoNotes.isEmpty()) {
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
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = PrimaryFixed,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = IconifyIcons.Camera,
                                    contentDescription = null,
                                    tint = PrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Text(
                            text = "No photo notes yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Snap a whiteboard, receipt, or sketch with a caption.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                photoNotes.forEachIndexed { idx, note ->
                    val bitmap = remember(note.id, note.imageRelativePath) {
                        loadWorkspaceBitmap(note.imageRelativePath)
                    }
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .staggeredEntrance(idx + 1)
                            .testTag("photo_note_card_${note.id}"),
                        cornerRadius = 24.dp,
                        alpha = 0.78f,
                        onClick = { selectedPreviewNote = note }
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = note.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(176.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainerLow),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = IconifyIcons.GalleryAdd,
                                        contentDescription = null,
                                        tint = PrimaryContainer,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = note.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (note.caption.isNotBlank()) {
                                        Text(
                                            text = note.caption,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        text = WorkspaceViewModel.formatRelativeDate(note.createdAt),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(
                                    onClick = { onDeletePhotoNote(note) },
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("photo_note_delete_${note.id}")
                                ) {
                                    Icon(
                                        imageVector = IconifyIcons.Delete,
                                        contentDescription = "Delete ${note.title}",
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

    val pendingUri = pendingImageUri
    if (pendingUri != null) {
        AlertDialog(
            onDismissRequest = { pendingImageUri = null },
            title = { Text("Save Photo Note") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("Title") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("photo_note_title_input")
                    )
                    OutlinedTextField(
                        value = captionInput,
                        onValueChange = { captionInput = it },
                        label = { Text("Caption or notes") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("photo_note_caption_input")
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onAddPhotoNote(titleInput, captionInput, pendingUri)
                        pendingImageUri = null
                    },
                    modifier = Modifier.testTag("photo_note_save_confirm")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingImageUri = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    val detailNote = selectedPreviewNote
    if (detailNote != null) {
        val detailBitmap = remember(detailNote.id, detailNote.imageRelativePath) {
            loadWorkspaceBitmap(detailNote.imageRelativePath)
        }
        AlertDialog(
            onDismissRequest = { selectedPreviewNote = null },
            title = { Text(detailNote.title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (detailBitmap != null) {
                        Image(
                            bitmap = detailBitmap.asImageBitmap(),
                            contentDescription = detailNote.title,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 320.dp)
                                .clip(RoundedCornerShape(14.dp))
                        )
                    }
                    if (detailNote.caption.isNotBlank()) {
                        Text(
                            text = detailNote.caption,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedPreviewNote = null }) {
                    Text("Close")
                }
            }
        )
    }
}
