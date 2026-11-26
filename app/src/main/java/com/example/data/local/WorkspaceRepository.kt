package com.example.data.local

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.data.services.PdfToolsEngine
import com.example.data.services.VoiceAudioManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File

class WorkspaceRepository(
    private val context: Context,
    private val database: AppDatabase,
    val preferences: PreferencesRepository,
    val pdfEngine: PdfToolsEngine,
    val voiceManager: VoiceAudioManager
) {
    private val fileDao = database.localFileDao()
    private val noteDao = database.noteDao()
    private val checklistDao = database.checklistDao()
    private val memoDao = database.voiceMemoDao()
    private val annotationDao = database.pdfAnnotationDao()
    private val photoNoteDao = database.photoNoteDao()
    private val mindMapDao = database.mindMapDao()

    val allFiles: Flow<List<LocalFileEntity>> = fileDao.observeAllFiles()
    val allNotes: Flow<List<NoteEntity>> = noteDao.observeAllNotes()
    val shoppingItems: Flow<List<ChecklistItemEntity>> = checklistDao.observeItems(ChecklistType.SHOPPING)
    val todoItems: Flow<List<ChecklistItemEntity>> = checklistDao.observeItems(ChecklistType.TODO)
    val allMemos: Flow<List<VoiceMemoEntity>> = memoDao.observeAllMemos()
    val allPhotoNotes: Flow<List<PhotoNoteEntity>> = photoNoteDao.observeAllPhotoNotes()
    val allMindMaps: Flow<List<MindMapEntity>> = mindMapDao.observeAllMindMaps()

    fun observeFile(fileId: Long): Flow<LocalFileEntity?> = fileDao.observeFileById(fileId)
    fun observeAnnotations(fileId: Long): Flow<List<PdfAnnotationEntity>> =
        annotationDao.observeAnnotationsForFile(fileId)

    suspend fun ensureSeeded() = withContext(Dispatchers.IO) {
        val prefs = preferences.preferencesFlow.first()
        val transparentMarker = pdfEngine.resolveWorkspaceFile("docs/.clean_seeds_v2")
        if (prefs.isSeeded && fileDao.countFiles() > 0) {
            if (!transparentMarker.exists()) {
                val notesFile = pdfEngine.resolveWorkspaceFile("docs/Notes.pdf")
                if (notesFile.exists()) pdfEngine.seedNotesPdf(notesFile)
                val receiptFile = pdfEngine.resolveWorkspaceFile("docs/Receipt_Scan.pdf")
                if (receiptFile.exists()) pdfEngine.seedReceiptPdf(receiptFile)
                transparentMarker.parentFile?.mkdirs()
                transparentMarker.writeText("1")
            }
            ensureProductivitySeeds(System.currentTimeMillis())
            return@withContext
        }
        if (!transparentMarker.exists()) {
            transparentMarker.parentFile?.mkdirs()
            transparentMarker.writeText("1")
        }

        val now = System.currentTimeMillis()
        val hour = 3_600_000L
        val day = 86_400_000L

        if (fileDao.countFiles() == 0) {
            val notesPdfFile = pdfEngine.resolveWorkspaceFile("docs/Notes.pdf")
            val notesPages = pdfEngine.seedNotesPdf(notesPdfFile)
            val notesId = fileDao.insertFile(
                LocalFileEntity(
                    name = "Notes.pdf",
                    category = FileCategory.PDF,
                    sizeBytes = notesPdfFile.length().coerceAtLeast(48_000L),
                    relativePath = "docs/Notes.pdf",
                    pageCount = notesPages,
                    snippetTitle = "Design Review & Architecture Notes",
                    snippetBody = "Quick review of the reader layout, typography spacing, and annotation highlights.",
                    lastModifiedAt = now - 2 * hour
                )
            )
            annotationDao.insertAnnotation(
                PdfAnnotationEntity(
                    fileId = notesId,
                    pageIndex = 0,
                    type = PdfAnnotationType.HIGHLIGHT,
                    payload = "0.10,0.44,0.88,0.52"
                )
            )

            val budgetFile = pdfEngine.resolveWorkspaceFile("docs/Budget.xlsx")
            budgetFile.parentFile?.mkdirs()
            budgetFile.writeText(
                "Q4 Household & Studio Budget\n" +
                    "Category,Planned,Actual,Status\n" +
                    "Studio Supplies,£240,£184,On Track\n" +
                    "Groceries,£520,£465,Under Budget\n" +
                    "Books & Research,£150,£112,On Track\n" +
                    "Utilities,£195,£195,Paid"
            )
            fileDao.insertFile(
                LocalFileEntity(
                    name = "Budget.xlsx",
                    category = FileCategory.EXCEL,
                    sizeBytes = 420_000L,
                    relativePath = "docs/Budget.xlsx",
                    pageCount = 1,
                    snippetTitle = "Q4 Studio & Household Ledger",
                    snippetBody = "Monthly breakdown across studio supplies, groceries, books, and utilities. Total saved vs planned: +£149.",
                    lastModifiedAt = now - day
                )
            )

            val reportFile = pdfEngine.resolveWorkspaceFile("docs/Report.docx")
            reportFile.writeText(
                "Q4 Milestone Report\n\n" +
                    "Summary:\n" +
                    "• Smooth PDF page rendering and crisp typography across views.\n" +
                    "• Clean frosted-glass cards and 48dp touch targets.\n" +
                    "• Voice memos recorded as uncompressed 16-bit WAV."
            )
            fileDao.insertFile(
                LocalFileEntity(
                    name = "Report.docx",
                    category = FileCategory.WORD,
                    sizeBytes = 890_000L,
                    relativePath = "docs/Report.docx",
                    pageCount = 3,
                    snippetTitle = "Q4 Milestone Report",
                    snippetBody = "Executive summary of Q4 reader performance, visual polish, and upcoming deliverables.",
                    lastModifiedAt = now - 3 * day
                )
            )

            val slidesFile = pdfEngine.resolveWorkspaceFile("docs/Slides_Q3.pptx")
            slidesFile.writeText(
                "Slide 1: Calm Workspace Productivity\n" +
                    "Slide 2: Clean Media & Document Flows\n" +
                    "Slide 3: PDF Reader & Annotations\n" +
                    "Slide 4: Audio Studio"
            )
            fileDao.insertFile(
                LocalFileEntity(
                    name = "Slides_Q3.pptx",
                    category = FileCategory.POWERPOINT,
                    sizeBytes = 3_200_000L,
                    relativePath = "docs/Slides_Q3.pptx",
                    pageCount = 12,
                    snippetTitle = "Q3 Product Review Deck",
                    snippetBody = "12 slides covering frosted glass visual tokens, PDF tools, and document workflows.",
                    lastModifiedAt = now - 6 * day
                )
            )

            val receiptFile = pdfEngine.resolveWorkspaceFile("docs/Receipt_Scan.pdf")
            val receiptPages = pdfEngine.seedReceiptPdf(receiptFile)
            fileDao.insertFile(
                LocalFileEntity(
                    name = "Receipt_Scan.pdf",
                    category = FileCategory.PDF,
                    sizeBytes = receiptFile.length().coerceAtLeast(32_000L),
                    relativePath = "docs/Receipt_Scan.pdf",
                    pageCount = receiptPages,
                    snippetTitle = "Stationery & Studio Supplies Receipt",
                    snippetBody = "Merchant: Paper & Ink Co. · Total: £38.40 · Receipt archived.",
                    lastModifiedAt = now - 10 * day
                )
            )
        }

        if (noteDao.getLatestNote() == null) {
            val initialContent = """
                This week felt noticeably calmer once I blocked off two-hour morning windows for deep reading. Stepping away from constant notifications made it much easier to finish the architecture draft without rushing.

                ## What worked well
                - Keeping all research PDFs in a single folder before starting notes.
                - Taking a 15-minute walk after lunch instead of scrolling through feeds.
                - Reviewing open tasks every Friday afternoon so Monday feels clear.

                Next week, I want to carry that same quiet pace into the Q4 planning doc and keep meetings strictly to the afternoon.
            """.trimIndent()
            noteDao.insertNote(
                NoteEntity(
                    title = "Weekly Reflections",
                    content = initialContent,
                    wordCount = countWords(initialContent),
                    updatedAt = now - hour
                )
            )
        }

        if (checklistDao.countByType(ChecklistType.SHOPPING) == 0) {
            val shoppingDefaults = listOf(
                "Rice" to false,
                "Milk" to false,
                "Tea" to true,
                "Wholemeal bread" to false,
                "Olive oil" to true,
                "Apples" to false
            )
            shoppingDefaults.forEachIndexed { index, (title, done) ->
                checklistDao.insertItem(
                    ChecklistItemEntity(
                        listType = ChecklistType.SHOPPING,
                        title = title,
                        isCompleted = done,
                        createdAt = now + index
                    )
                )
            }
        }

        if (checklistDao.countByType(ChecklistType.TODO) == 0) {
            val todoDefaults = listOf(
                "Review Q3 architecture notes" to false,
                "Organize scanned receipts into PDF" to true,
                "Record weekly reflection memo" to false,
                "Export budget spreadsheet" to false
            )
            todoDefaults.forEachIndexed { index, (title, done) ->
                checklistDao.insertItem(
                    ChecklistItemEntity(
                        listType = ChecklistType.TODO,
                        title = title,
                        isCompleted = done,
                        createdAt = now + index
                    )
                )
            }
        }

        if (memoDao.countMemos() == 0) {
            val memos = listOf(
                Triple("Morning idea", 392.0, 4),
                Triple("Book quote", 330.0, 3),
                Triple("Grocery reminder", 440.0, 2)
            )
            memos.forEachIndexed { idx, (title, freq, dur) ->
                val rel = "memos/sample_${idx + 1}.wav"
                voiceManager.generateSampleWav(voiceManager.resolveMemoFile(rel), freq, dur)
                memoDao.insertMemo(
                    VoiceMemoEntity(
                        title = title,
                        relativePath = rel,
                        durationSeconds = dur,
                        createdAt = now - idx * day
                    )
                )
            }
        }

        ensureProductivitySeeds(now)
        preferences.markSeeded()
    }

    private suspend fun ensureProductivitySeeds(now: Long) {
        if (photoNoteDao.countPhotoNotes() == 0) {
            val rel = "photos/whiteboard_sketch.jpg"
            pdfEngine.seedSamplePhotoNoteImage(pdfEngine.resolveWorkspaceFile(rel))
            photoNoteDao.insertPhotoNote(
                PhotoNoteEntity(
                    title = "Whiteboard Sketch",
                    caption = "Desk layout and floating toolbar alignment notes.",
                    imageRelativePath = rel,
                    createdAt = now - 3_600_000L
                )
            )
        }
        if (mindMapDao.countMindMaps() == 0) {
            val defaultNodes = """
                [
                  {"id":1,"parentId":0,"label":"Q4 Workspace","x":0.5,"y":0.22,"color":"#1F55E0"},
                  {"id":2,"parentId":1,"label":"PDF Toolkit","x":0.22,"y":0.52,"color":"#6E56CF"},
                  {"id":3,"parentId":1,"label":"Quick Notes","x":0.5,"y":0.56,"color":"#0F766E"},
                  {"id":4,"parentId":1,"label":"Voice & Scan","x":0.78,"y":0.52,"color":"#C2410C"}
                ]
            """.trimIndent()
            mindMapDao.insertMindMap(
                MindMapEntity(
                    title = "Q4 Project Architecture",
                    nodesJson = defaultNodes,
                    updatedAt = now - 1_800_000L
                )
            )
        }
    }

    suspend fun getFileById(id: Long): LocalFileEntity? = fileDao.getFileById(id)

    suspend fun saveNote(noteId: Long?, title: String, content: String): Long {
        val cleanTitle = title.ifBlank { "Untitled Note" }
        val words = countWords(content)
        val now = System.currentTimeMillis()
        return if (noteId != null && noteId > 0) {
            val updated = NoteEntity(
                id = noteId,
                title = cleanTitle,
                content = content,
                wordCount = words,
                updatedAt = now
            )
            noteDao.updateNote(updated)
            noteId
        } else {
            noteDao.insertNote(
                NoteEntity(
                    title = cleanTitle,
                    content = content,
                    wordCount = words,
                    updatedAt = now
                )
            )
        }
    }

    suspend fun getLatestOrCreateNote(): NoteEntity {
        return noteDao.getLatestNote() ?: run {
            val newId = saveNote(null, "Quick Note", "")
            noteDao.getNoteById(newId)!!
        }
    }

    suspend fun getNoteById(id: Long): NoteEntity? = noteDao.getNoteById(id)

    suspend fun addChecklistItem(type: ChecklistType, title: String) {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return
        checklistDao.insertItem(
            ChecklistItemEntity(
                listType = type,
                title = trimmed,
                isCompleted = false,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun toggleChecklistItem(item: ChecklistItemEntity) {
        checklistDao.updateItem(item.copy(isCompleted = !item.isCompleted))
    }

    suspend fun clearCompleted(type: ChecklistType) {
        checklistDao.clearCompleted(type)
    }

    suspend fun addRecordedMemo(title: String, relativePath: String, durationSeconds: Int) {
        memoDao.insertMemo(
            VoiceMemoEntity(
                title = title,
                relativePath = relativePath,
                durationSeconds = durationSeconds,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteMemo(memo: VoiceMemoEntity) {
        voiceManager.resolveMemoFile(memo.relativePath).delete()
        memoDao.deleteMemoById(memo.id)
    }

    suspend fun toggleBookmark(fileId: Long, pageIndex: Int, currentlyBookmarked: Boolean) {
        if (currentlyBookmarked) {
            annotationDao.deleteByPageAndType(fileId, pageIndex, PdfAnnotationType.BOOKMARK)
        } else {
            annotationDao.insertAnnotation(
                PdfAnnotationEntity(
                    fileId = fileId,
                    pageIndex = pageIndex,
                    type = PdfAnnotationType.BOOKMARK,
                    payload = "Page ${pageIndex + 1}"
                )
            )
        }
    }

    suspend fun addPdfAnnotation(
        fileId: Long,
        pageIndex: Int,
        type: PdfAnnotationType,
        payload: String
    ) {
        annotationDao.insertAnnotation(
            PdfAnnotationEntity(
                fileId = fileId,
                pageIndex = pageIndex,
                type = type,
                payload = payload
            )
        )
    }

    suspend fun deletePdfAnnotation(id: Long) {
        annotationDao.deleteById(id)
    }

    suspend fun importExternalDocument(uri: Uri): LocalFileEntity = withContext(Dispatchers.IO) {
        val name = queryDisplayName(uri) ?: "Document_${System.currentTimeMillis()}.pdf"
        val lower = name.lowercase()
        val category = when {
            lower.endsWith(".doc") || lower.endsWith(".docx") -> FileCategory.WORD
            lower.endsWith(".xls") || lower.endsWith(".xlsx") || lower.endsWith(".csv") -> FileCategory.EXCEL
            lower.endsWith(".ppt") || lower.endsWith(".pptx") -> FileCategory.POWERPOINT
            else -> FileCategory.PDF
        }
        val relPath = "docs/${System.currentTimeMillis()}_$name"
        val dest = pdfEngine.resolveWorkspaceFile(relPath)
        val bytes = pdfEngine.copyUriToFile(uri, dest)
        val pages = if (category == FileCategory.PDF) pdfEngine.getPageCount(dest) else 1
        val snippet = if (category == FileCategory.PDF) {
            "Imported PDF with $pages ${if (pages == 1) "page" else "pages"}."
        } else {
            "Imported ${category.label} document."
        }
        val entity = LocalFileEntity(
            name = name,
            category = category,
            sizeBytes = bytes,
            relativePath = relPath,
            pageCount = pages,
            snippetTitle = name.substringBeforeLast('.'),
            snippetBody = snippet,
            lastModifiedAt = System.currentTimeMillis()
        )
        val id = fileDao.insertFile(entity)
        entity.copy(id = id)
    }

    suspend fun registerGeneratedPdf(
        fileName: String,
        destFile: File,
        pageCount: Int,
        snippetTitle: String,
        snippetBody: String
    ): LocalFileEntity {
        val relPath = destFile.relativeTo(context.filesDir).path
        val entity = LocalFileEntity(
            name = fileName,
            category = FileCategory.PDF,
            sizeBytes = destFile.length(),
            relativePath = relPath,
            pageCount = pageCount,
            snippetTitle = snippetTitle,
            snippetBody = snippetBody,
            lastModifiedAt = System.currentTimeMillis()
        )
        val id = fileDao.insertFile(entity)
        return entity.copy(id = id)
    }

    suspend fun lockFileWithPassword(file: LocalFileEntity, password: String): LocalFileEntity {
        val plain = pdfEngine.resolveWorkspaceFile(file.relativePath)
        val lockedRel = "${file.relativePath}.locked"
        val lockedDest = pdfEngine.resolveWorkspaceFile(lockedRel)
        val (saltHex, hashHex) = pdfEngine.encryptFileWithPassword(plain, lockedDest, password)
        plain.delete()
        val updated = file.copy(
            relativePath = lockedRel,
            sizeBytes = lockedDest.length(),
            isLocked = true,
            passwordSalt = saltHex,
            passwordHash = hashHex,
            lastModifiedAt = System.currentTimeMillis()
        )
        fileDao.updateFile(updated)
        return updated
    }

    suspend fun unlockFileTemp(file: LocalFileEntity, password: String): File? {
        if (!file.isLocked) return pdfEngine.resolveWorkspaceFile(file.relativePath)
        val salt = file.passwordSalt ?: return null
        val hash = file.passwordHash ?: return null
        val encrypted = pdfEngine.resolveWorkspaceFile(file.relativePath)
        val tempFile = File(context.cacheDir, "unlocked_${file.id}_${file.name}")
        val ok = pdfEngine.verifyAndDecrypt(encrypted, tempFile, password, salt, hash)
        return if (ok) tempFile else null
    }

    suspend fun renameFile(file: LocalFileEntity, newName: String) {
        val clean = newName.trim()
        if (clean.isEmpty()) return
        fileDao.updateFile(
            file.copy(
                name = clean,
                lastModifiedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteFile(file: LocalFileEntity) = withContext(Dispatchers.IO) {
        pdfEngine.resolveWorkspaceFile(file.relativePath).delete()
        fileDao.deleteFileById(file.id)
    }

    suspend fun saveDocumentText(file: LocalFileEntity, newText: String): LocalFileEntity =
        withContext(Dispatchers.IO) {
            val target = pdfEngine.resolveWorkspaceFile(file.relativePath)
            target.parentFile?.mkdirs()
            target.writeText(newText)
            val snippet = newText.lineSequence()
                .map { it.trim() }
                .firstOrNull { it.isNotEmpty() }
                ?.take(120)
                ?: file.snippetBody
            val updated = file.copy(
                sizeBytes = target.length().coerceAtLeast(120L),
                snippetBody = snippet,
                lastModifiedAt = System.currentTimeMillis()
            )
            fileDao.updateFile(updated)
            updated
        }

    suspend fun addPhotoNote(title: String, caption: String, sourceUri: Uri): PhotoNoteEntity =
        withContext(Dispatchers.IO) {
            val rel = "photos/photo_${System.currentTimeMillis()}.jpg"
            val dest = pdfEngine.resolveWorkspaceFile(rel)
            pdfEngine.copyUriToFile(sourceUri, dest)
            val entity = PhotoNoteEntity(
                title = title.trim().ifBlank { "Photo Note" },
                caption = caption.trim(),
                imageRelativePath = rel,
                createdAt = System.currentTimeMillis()
            )
            val id = photoNoteDao.insertPhotoNote(entity)
            entity.copy(id = id)
        }

    suspend fun deletePhotoNote(note: PhotoNoteEntity) = withContext(Dispatchers.IO) {
        pdfEngine.resolveWorkspaceFile(note.imageRelativePath).delete()
        photoNoteDao.deletePhotoNoteById(note.id)
    }

    suspend fun saveMindMap(map: MindMapEntity) {
        mindMapDao.updateMindMap(map.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun createMindMap(title: String): MindMapEntity {
        val clean = title.trim().ifBlank { "New Mind Map" }
        val starter = """[{"id":1,"parentId":0,"label":"$clean","x":0.5,"y":0.24,"color":"#1F55E0"}]"""
        val entity = MindMapEntity(
            title = clean,
            nodesJson = starter,
            updatedAt = System.currentTimeMillis()
        )
        val id = mindMapDao.insertMindMap(entity)
        return entity.copy(id = id)
    }

    suspend fun deleteMindMap(id: Long) {
        mindMapDao.deleteMindMapById(id)
    }

    private fun queryDisplayName(uri: Uri): String? {
        return context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            }
    }

    companion object {
        fun countWords(text: String): Int {
            val cleaned = text.replace(Regex("[#*\\-`]+"), " ").trim()
            return if (cleaned.isEmpty()) 0 else cleaned.split(Regex("\\s+")).size
        }
    }
}
