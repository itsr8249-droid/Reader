package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class FileCategory(val label: String) {
    PDF("PDF"),
    WORD("Word"),
    EXCEL("Excel"),
    POWERPOINT("PowerPoint")
}

enum class ChecklistType {
    SHOPPING,
    TODO
}

enum class PdfAnnotationType {
    BOOKMARK,
    HIGHLIGHT,
    INK,
    TEXT,
    SIGNATURE
}

@Entity(tableName = "local_files")
data class LocalFileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: FileCategory,
    val sizeBytes: Long,
    val relativePath: String,
    val pageCount: Int,
    val snippetTitle: String,
    val snippetBody: String,
    val isLocked: Boolean = false,
    val passwordSalt: String? = null,
    val passwordHash: String? = null,
    val lastModifiedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val wordCount: Int,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "checklist_items")
data class ChecklistItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val listType: ChecklistType,
    val title: String,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "voice_memos")
data class VoiceMemoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val relativePath: String,
    val durationSeconds: Int,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "pdf_annotations")
data class PdfAnnotationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileId: Long,
    val pageIndex: Int,
    val type: PdfAnnotationType,
    val payload: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "photo_notes")
data class PhotoNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val caption: String,
    val imageRelativePath: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "mind_maps")
data class MindMapEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val nodesJson: String,
    val updatedAt: Long = System.currentTimeMillis()
)
