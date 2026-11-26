package com.example.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.AppPreferences
import com.example.data.local.ChecklistItemEntity
import com.example.data.local.ChecklistType
import com.example.data.local.FileCategory
import com.example.data.local.LocalFileEntity
import com.example.data.local.MindMapEntity
import com.example.data.local.NoteEntity
import com.example.data.local.PdfAnnotationEntity
import com.example.data.local.PdfAnnotationType
import com.example.data.local.PhotoNoteEntity
import com.example.data.local.PreferencesRepository
import com.example.data.local.VoiceMemoEntity
import com.example.data.local.WorkspaceRepository
import com.example.data.services.PdfToolsEngine
import com.example.data.services.ReadAloudManager
import com.example.data.services.VoiceAudioManager
import android.graphics.BitmapFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class FileSortMode(val label: String) {
    RECENT("Recent"),
    NAME("Name"),
    SIZE("Size")
}

enum class SaveStatus {
    SAVED,
    SAVING
}

data class ToolFeedback(
    val title: String,
    val message: String,
    val generatedFileId: Long? = null
)

class WorkspaceViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val preferences = PreferencesRepository(application)
    private val pdfEngine = PdfToolsEngine(application)
    val voiceManager = VoiceAudioManager(application)
    val readAloudManager = ReadAloudManager(application)
    private val repository = WorkspaceRepository(
        context = application,
        database = database,
        preferences = preferences,
        pdfEngine = pdfEngine,
        voiceManager = voiceManager
    )

    val allFiles: StateFlow<List<LocalFileEntity>> = repository.allFiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotes: StateFlow<List<NoteEntity>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shoppingItems: StateFlow<List<ChecklistItemEntity>> = repository.shoppingItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todoItems: StateFlow<List<ChecklistItemEntity>> = repository.todoItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val voiceMemos: StateFlow<List<VoiceMemoEntity>> = repository.allMemos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val photoNotes: StateFlow<List<PhotoNoteEntity>> = repository.allPhotoNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mindMaps: StateFlow<List<MindMapEntity>> = repository.allMindMaps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appPreferences: StateFlow<AppPreferences> = preferences.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppPreferences())

    private val _selectedFileFilter = MutableStateFlow<FileCategory?>(null)
    val selectedFileFilter: StateFlow<FileCategory?> = _selectedFileFilter.asStateFlow()

    private val _sortMode = MutableStateFlow(FileSortMode.RECENT)
    val sortMode: StateFlow<FileSortMode> = _sortMode.asStateFlow()

    private val _isGridView = MutableStateFlow(false)
    val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

    val filteredFiles: StateFlow<List<LocalFileEntity>> = combine(
        allFiles,
        _selectedFileFilter,
        _sortMode
    ) { files, filter, sort ->
        val filtered = if (filter == null) files else files.filter { it.category == filter }
        when (sort) {
            FileSortMode.RECENT -> filtered.sortedByDescending { it.lastModifiedAt }
            FileSortMode.NAME -> filtered.sortedBy { it.name.lowercase() }
            FileSortMode.SIZE -> filtered.sortedByDescending { it.sizeBytes }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _unlockedPaths = MutableStateFlow<Map<Long, File>>(emptyMap())

    private val _toolBusy = MutableStateFlow(false)
    val toolBusy: StateFlow<Boolean> = _toolBusy.asStateFlow()

    private val _toolFeedback = MutableStateFlow<ToolFeedback?>(null)
    val toolFeedback: StateFlow<ToolFeedback?> = _toolFeedback.asStateFlow()

    private val _activeNote = MutableStateFlow<NoteEntity?>(null)
    val activeNote: StateFlow<NoteEntity?> = _activeNote.asStateFlow()

    private val _saveStatus = MutableStateFlow(SaveStatus.SAVED)
    val saveStatus: StateFlow<SaveStatus> = _saveStatus.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    fun setFileFilter(category: FileCategory?) {
        _selectedFileFilter.value = category
    }

    fun setSortMode(mode: FileSortMode) {
        _sortMode.value = mode
    }

    fun setGridView(grid: Boolean) {
        _isGridView.value = grid
    }

    fun cycleSortMode() {
        val modes = FileSortMode.entries
        val next = (modes.indexOf(_sortMode.value) + 1) % modes.size
        _sortMode.value = modes[next]
    }

    fun toggleGridView() {
        _isGridView.value = !_isGridView.value
    }

    fun unlockFile(file: LocalFileEntity, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val unlocked = repository.unlockFileTemp(file, password)
            if (unlocked != null) {
                _unlockedPaths.value = _unlockedPaths.value + (file.id to unlocked)
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    fun isFileUnlocked(file: LocalFileEntity): Boolean {
        return !file.isLocked || _unlockedPaths.value.containsKey(file.id)
    }

    fun getReadableFile(file: LocalFileEntity): File {
        return _unlockedPaths.value[file.id] ?: pdfEngine.resolveWorkspaceFile(file.relativePath)
    }

    fun observeFile(fileId: Long): Flow<LocalFileEntity?> = repository.observeFile(fileId)

    fun observePdfAnnotations(fileId: Long): Flow<List<PdfAnnotationEntity>> =
        repository.observeAnnotations(fileId)

    suspend fun loadPdfPageBitmap(file: LocalFileEntity, pageIndex: Int): Bitmap? {
        val readable = getReadableFile(file)
        return pdfEngine.renderPageBitmap(readable, pageIndex, 1080)
    }

    fun readDocumentText(file: LocalFileEntity): String {
        val readable = getReadableFile(file)
        return if (readable.exists() && file.category != FileCategory.PDF) {
            runCatching { readable.readText() }.getOrDefault(file.snippetBody)
        } else {
            file.snippetBody
        }
    }

    fun togglePdfBookmark(fileId: Long, pageIndex: Int, isBookmarked: Boolean) {
        viewModelScope.launch {
            repository.toggleBookmark(fileId, pageIndex, isBookmarked)
        }
    }

    fun addPdfAnnotation(fileId: Long, pageIndex: Int, type: PdfAnnotationType, payload: String) {
        viewModelScope.launch {
            repository.addPdfAnnotation(fileId, pageIndex, type, payload)
        }
    }

    fun removePdfAnnotation(id: Long) {
        viewModelScope.launch {
            repository.deletePdfAnnotation(id)
        }
    }

    fun importExternalFile(uri: Uri, onImported: ((LocalFileEntity) -> Unit)? = null) {
        viewModelScope.launch {
            val imported = repository.importExternalDocument(uri)
            onImported?.invoke(imported)
        }
    }

    fun shareFile(file: LocalFileEntity) {
        val ctx = getApplication<Application>()
        val readable = getReadableFile(file)
        if (!readable.exists()) return
        runCatching {
            val contentUri = FileProvider.getUriForFile(
                ctx,
                "${ctx.packageName}.fileprovider",
                readable
            )
            val mime = when (file.category) {
                FileCategory.PDF -> "application/pdf"
                FileCategory.WORD -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                FileCategory.EXCEL -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                FileCategory.POWERPOINT -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            }
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mime
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, "Share ${file.name}").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ctx.startActivity(chooser)
        }
    }

    fun createScanTempUri(): Pair<Uri, File> {
        val ctx = getApplication<Application>()
        val scanFile = File(ctx.cacheDir, "scan_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", scanFile)
        return uri to scanFile
    }

    // --- Offline PDF Tools ---

    fun mergePdfs(files: List<LocalFileEntity>) {
        if (files.size < 2) return
        viewModelScope.launch {
            _toolBusy.value = true
            val name = "Merged_${timestampTag()}.pdf"
            val dest = pdfEngine.resolveWorkspaceFile("docs/$name")
            val sources = files.map { getReadableFile(it) }
            val pages = pdfEngine.mergePdfs(sources, dest)
            val created = repository.registerGeneratedPdf(
                fileName = name,
                destFile = dest,
                pageCount = pages,
                snippetTitle = "Merged PDF (${files.size} files)",
                snippetBody = "Combined ${files.joinToString(", ") { it.name }} into $pages pages."
            )
            _toolBusy.value = false
            _toolFeedback.value = ToolFeedback("Merged PDF Ready", "$name ($pages pages) saved to Files.", created.id)
        }
    }

    fun splitPdf(file: LocalFileEntity) {
        viewModelScope.launch {
            _toolBusy.value = true
            val base = file.name.substringBeforeLast('.')
            val outDir = pdfEngine.resolveWorkspaceFile("docs/split_${timestampTag()}")
            val parts = pdfEngine.splitPdf(getReadableFile(file), outDir, base)
            var firstId: Long? = null
            parts.forEachIndexed { idx, partFile ->
                val saved = repository.registerGeneratedPdf(
                    fileName = partFile.name,
                    destFile = partFile,
                    pageCount = 1,
                    snippetTitle = "$base — Page ${idx + 1}",
                    snippetBody = "Extracted page ${idx + 1} from ${file.name}."
                )
                if (firstId == null) firstId = saved.id
            }
            _toolBusy.value = false
            _toolFeedback.value = ToolFeedback("Split Complete", "${parts.size} single-page PDFs saved to Files.", firstId)
        }
    }

    fun compressPdf(file: LocalFileEntity, quality: Int) {
        viewModelScope.launch {
            _toolBusy.value = true
            val base = file.name.substringBeforeLast('.')
            val name = "${base}_compressed.pdf"
            val dest = pdfEngine.resolveWorkspaceFile("docs/${timestampTag()}_$name")
            val pages = pdfEngine.compressPdf(getReadableFile(file), dest, quality)
            val saved = repository.registerGeneratedPdf(
                fileName = name,
                destFile = dest,
                pageCount = pages,
                snippetTitle = "$base (Compressed)",
                snippetBody = "Compressed at ${quality}% JPEG quality (${formatFileSize(dest.length())})."
            )
            _toolBusy.value = false
            _toolFeedback.value = ToolFeedback("Compressed PDF Saved", "$name (${formatFileSize(dest.length())}) saved to Files.", saved.id)
        }
    }

    fun rotatePdf(file: LocalFileEntity, degrees: Int) {
        viewModelScope.launch {
            _toolBusy.value = true
            val base = file.name.substringBeforeLast('.')
            val name = "${base}_rot${degrees}.pdf"
            val dest = pdfEngine.resolveWorkspaceFile("docs/${timestampTag()}_$name")
            val pages = pdfEngine.rotatePdf(getReadableFile(file), dest, degrees)
            val saved = repository.registerGeneratedPdf(
                fileName = name,
                destFile = dest,
                pageCount = pages,
                snippetTitle = "$base (Rotated ${degrees}°)",
                snippetBody = "All $pages pages rotated ${degrees}° clockwise."
            )
            _toolBusy.value = false
            _toolFeedback.value = ToolFeedback("Rotation Complete", "$name saved to Files.", saved.id)
        }
    }

    fun extractPdfPages(file: LocalFileEntity, pagesInput: String) {
        val parsed = pagesInput.split(",", " ")
            .mapNotNull { it.trim().toIntOrNull() }
            .filter { it in 1..file.pageCount }
            .distinct()
        val targetPages = if (parsed.isEmpty()) listOf(1) else parsed
        viewModelScope.launch {
            _toolBusy.value = true
            val base = file.name.substringBeforeLast('.')
            val name = "${base}_extract.pdf"
            val dest = pdfEngine.resolveWorkspaceFile("docs/${timestampTag()}_$name")
            val count = pdfEngine.extractPages(getReadableFile(file), dest, targetPages)
            val saved = repository.registerGeneratedPdf(
                fileName = name,
                destFile = dest,
                pageCount = count,
                snippetTitle = "$base (Extracted Pages)",
                snippetBody = "Extracted pages ${targetPages.joinToString(", ")} from ${file.name}."
            )
            _toolBusy.value = false
            _toolFeedback.value = ToolFeedback("Pages Extracted", "$name ($count pages) saved to Files.", saved.id)
        }
    }

    fun protectPdf(file: LocalFileEntity, password: String) {
        if (password.length < 4) return
        viewModelScope.launch {
            _toolBusy.value = true
            val locked = repository.lockFileWithPassword(file, password)
            _unlockedPaths.value = _unlockedPaths.value - file.id
            _toolBusy.value = false
            _toolFeedback.value = ToolFeedback(
                "Document Protected",
                "${locked.name} encrypted with AES-256.",
                locked.id
            )
        }
    }

    fun imagesToPdf(uris: List<Uri>, prefix: String = "Images") {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            _toolBusy.value = true
            val name = "${prefix}_${timestampTag()}.pdf"
            val dest = pdfEngine.resolveWorkspaceFile("docs/$name")
            val count = pdfEngine.imagesToPdf(uris, dest)
            val saved = repository.registerGeneratedPdf(
                fileName = name,
                destFile = dest,
                pageCount = count,
                snippetTitle = "$prefix to PDF ($count pages)",
                snippetBody = "Converted $count images into a PDF."
            )
            _toolBusy.value = false
            _toolFeedback.value = ToolFeedback("PDF Created", "$name ($count pages) saved to Files.", saved.id)
        }
    }

    fun clearToolFeedback() {
        _toolFeedback.value = null
    }

    fun watermarkPdf(file: LocalFileEntity, watermarkText: String) {
        viewModelScope.launch {
            _toolBusy.value = true
            val base = file.name.substringBeforeLast('.')
            val name = "${base}_watermarked.pdf"
            val dest = pdfEngine.resolveWorkspaceFile("docs/${timestampTag()}_$name")
            val count = pdfEngine.watermarkPdf(getReadableFile(file), dest, watermarkText)
            val saved = repository.registerGeneratedPdf(
                fileName = name,
                destFile = dest,
                pageCount = count,
                snippetTitle = "$base (Watermarked)",
                snippetBody = "Stamped \"${watermarkText.ifBlank { "CONFIDENTIAL" }}\" across $count pages."
            )
            _toolBusy.value = false
            _toolFeedback.value = ToolFeedback("Watermark Applied", "$name saved to Files.", saved.id)
        }
    }

    fun exportEditedImageAsPdf(bitmap: Bitmap, title: String) {
        viewModelScope.launch {
            _toolBusy.value = true
            val clean = title.trim().ifBlank { "Edited_Image" }.replace(" ", "_")
            val name = "${clean}_${timestampTag()}.pdf"
            val dest = pdfEngine.resolveWorkspaceFile("docs/$name")
            val count = pdfEngine.saveBitmapAsPdf(bitmap, dest)
            val saved = repository.registerGeneratedPdf(
                fileName = name,
                destFile = dest,
                pageCount = count,
                snippetTitle = clean.replace("_", " "),
                snippetBody = "Exported edited image to PDF."
            )
            _toolBusy.value = false
            _toolFeedback.value = ToolFeedback("Image Exported", "$name saved to Files.", saved.id)
        }
    }

    fun renameFile(file: LocalFileEntity, newName: String) {
        viewModelScope.launch {
            repository.renameFile(file, newName)
        }
    }

    fun deleteFile(file: LocalFileEntity) {
        viewModelScope.launch {
            repository.deleteFile(file)
        }
    }

    fun saveDocumentText(file: LocalFileEntity, newText: String) {
        viewModelScope.launch {
            repository.saveDocumentText(file, newText)
        }
    }

    fun loadImageBitmapFromUri(uri: Uri): Bitmap? {
        return runCatching {
            getApplication<Application>().contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        }.getOrNull()
    }

    fun loadWorkspaceImageBitmap(relativePath: String): Bitmap? {
        val file = pdfEngine.resolveWorkspaceFile(relativePath)
        if (!file.exists()) return null
        return runCatching { BitmapFactory.decodeFile(file.absolutePath) }.getOrNull()
    }

    // --- Photo Notes & Mind Maps ---

    fun addPhotoNote(title: String, caption: String, sourceUri: Uri) {
        viewModelScope.launch {
            repository.addPhotoNote(title, caption, sourceUri)
        }
    }

    fun deletePhotoNote(note: PhotoNoteEntity) {
        viewModelScope.launch {
            repository.deletePhotoNote(note)
        }
    }

    fun saveMindMap(map: MindMapEntity) {
        viewModelScope.launch {
            repository.saveMindMap(map)
        }
    }

    fun createMindMap(title: String) {
        viewModelScope.launch {
            repository.createMindMap(title)
        }
    }

    fun deleteMindMap(id: Long) {
        viewModelScope.launch {
            repository.deleteMindMap(id)
        }
    }

    // --- Note Editor ---

    fun openLatestNote() {
        viewModelScope.launch {
            _activeNote.value = repository.getLatestOrCreateNote()
        }
    }

    fun openNoteById(noteId: Long) {
        viewModelScope.launch {
            _activeNote.value = repository.getNoteById(noteId) ?: repository.getLatestOrCreateNote()
        }
    }


    fun createNewBlankNote(onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.saveNote(null, "Quick Note", "")
            _activeNote.value = repository.getNoteById(id)
            onCreated(id)
        }
    }

    fun updateActiveNote(title: String, content: String) {
        val current = _activeNote.value ?: return
        val updatedLocal = current.copy(
            title = title,
            content = content,
            wordCount = WorkspaceRepository.countWords(content),
            updatedAt = System.currentTimeMillis()
        )
        _activeNote.value = updatedLocal
        viewModelScope.launch {
            _saveStatus.value = SaveStatus.SAVING
            val id = repository.saveNote(current.id, title, content)
            _activeNote.value = updatedLocal.copy(id = id)
            delay(250)
            _saveStatus.value = SaveStatus.SAVED
        }
    }

    // --- Checklists ---

    fun addChecklistItem(type: ChecklistType, title: String) {
        viewModelScope.launch {
            repository.addChecklistItem(type, title)
        }
    }

    fun toggleChecklistItem(item: ChecklistItemEntity) {
        viewModelScope.launch {
            repository.toggleChecklistItem(item)
        }
    }

    fun clearCompleted(type: ChecklistType) {
        viewModelScope.launch {
            repository.clearCompleted(type)
        }
    }

    // --- Voice Memos ---

    fun toggleRecording() {
        if (voiceManager.isRecording.value) {
            viewModelScope.launch {
                val result = voiceManager.stopRecording()
                if (result != null) {
                    val (relPath, duration) = result
                    val count = voiceMemos.value.size + 1
                    repository.addRecordedMemo(
                        title = "Voice Memo #$count",
                        relativePath = relPath,
                        durationSeconds = duration
                    )
                }
            }
        } else {
            val relPath = "memos/memo_${System.currentTimeMillis()}.wav"
            voiceManager.startRecording(viewModelScope, relPath)
        }
    }

    fun toggleMemoPlayback(memo: VoiceMemoEntity) {
        voiceManager.togglePlayback(memo.id, memo.relativePath)
    }

    fun deleteMemo(memo: VoiceMemoEntity) {
        if (voiceManager.playingMemoId.value == memo.id) {
            voiceManager.stopPlayback()
        }
        viewModelScope.launch {
            repository.deleteMemo(memo)
        }
    }

    // --- Settings ---

    fun setVoiceProfile(profile: String) {
        viewModelScope.launch {
            preferences.setVoiceProfile(profile)
        }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch {
            preferences.setLanguage(language)
        }
    }

    fun toggleVoicePreview() {
        readAloudManager.togglePreview(appPreferences.value.voiceProfile)
    }

    fun speakDocumentText(text: String) {
        readAloudManager.speakText(text, appPreferences.value.voiceProfile)
    }

    fun stopSpeaking() {
        readAloudManager.stop()
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.release()
        readAloudManager.release()
    }

    private fun timestampTag(): String =
        SimpleDateFormat("HHmmss", Locale.US).format(Date())

    companion object {
        fun formatFileSize(bytes: Long): String {
            return when {
                bytes >= 1_000_000L -> String.format(Locale.US, "%.1f MB", bytes / 1_000_000f)
                bytes >= 1_000L -> String.format(Locale.US, "%d KB", (bytes / 1_000L).coerceAtLeast(1))
                else -> "$bytes B"
            }
        }

        fun formatRelativeDate(timestamp: Long): String {
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply { timeInMillis = timestamp }
            return when {
                now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                    now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR) -> "Today"
                now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                    now.get(Calendar.DAY_OF_YEAR) - target.get(Calendar.DAY_OF_YEAR) == 1 -> "Yesterday"
                else -> SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(timestamp))
            }
        }

        fun formatTimeAgo(timestamp: Long, nowMs: Long = System.currentTimeMillis()): String {
            val diffMs = (nowMs - timestamp).coerceAtLeast(0L)
            val minutes = diffMs / 60_000L
            val hours = diffMs / 3_600_000L
            val days = diffMs / 86_400_000L
            return when {
                minutes < 1L -> "just now"
                minutes < 60L -> "$minutes ${if (minutes == 1L) "minute" else "minutes"} ago"
                hours < 24L -> "$hours ${if (hours == 1L) "hour" else "hours"} ago"
                days < 7L -> "$days ${if (days == 1L) "day" else "days"} ago"
                else -> SimpleDateFormat("d MMM, h:mm a", Locale.getDefault()).format(Date(timestamp))
            }
        }

        fun formatDuration(seconds: Int): String {
            val m = seconds / 60
            val s = seconds % 60
            return String.format(Locale.US, "%02d:%02d", m, s)
        }
    }
}
