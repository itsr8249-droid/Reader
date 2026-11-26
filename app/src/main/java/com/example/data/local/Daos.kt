package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalFileDao {
    @Query("SELECT * FROM local_files ORDER BY lastModifiedAt DESC")
    fun observeAllFiles(): Flow<List<LocalFileEntity>>

    @Query("SELECT * FROM local_files WHERE id = :id")
    fun observeFileById(id: Long): Flow<LocalFileEntity?>

    @Query("SELECT * FROM local_files WHERE id = :id")
    suspend fun getFileById(id: Long): LocalFileEntity?

    @Query("SELECT COUNT(*) FROM local_files")
    suspend fun countFiles(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: LocalFileEntity): Long

    @Update
    suspend fun updateFile(file: LocalFileEntity)

    @Query("DELETE FROM local_files WHERE id = :id")
    suspend fun deleteFileById(id: Long)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun observeAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Long): NoteEntity?

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getLatestNote(): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)
}

@Dao
interface ChecklistDao {
    @Query("SELECT * FROM checklist_items WHERE listType = :type ORDER BY createdAt ASC")
    fun observeItems(type: ChecklistType): Flow<List<ChecklistItemEntity>>

    @Query("SELECT * FROM checklist_items ORDER BY createdAt ASC")
    fun observeAllItems(): Flow<List<ChecklistItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ChecklistItemEntity): Long

    @Update
    suspend fun updateItem(item: ChecklistItemEntity)

    @Query("DELETE FROM checklist_items WHERE listType = :type AND isCompleted = 1")
    suspend fun clearCompleted(type: ChecklistType)

    @Query("DELETE FROM checklist_items WHERE id = :id")
    suspend fun deleteItemById(id: Long)

    @Query("SELECT COUNT(*) FROM checklist_items WHERE listType = :type")
    suspend fun countByType(type: ChecklistType): Int
}

@Dao
interface VoiceMemoDao {
    @Query("SELECT * FROM voice_memos ORDER BY createdAt DESC")
    fun observeAllMemos(): Flow<List<VoiceMemoEntity>>

    @Query("SELECT COUNT(*) FROM voice_memos")
    suspend fun countMemos(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemo(memo: VoiceMemoEntity): Long

    @Query("DELETE FROM voice_memos WHERE id = :id")
    suspend fun deleteMemoById(id: Long)
}

@Dao
interface PdfAnnotationDao {
    @Query("SELECT * FROM pdf_annotations WHERE fileId = :fileId ORDER BY createdAt ASC")
    fun observeAnnotationsForFile(fileId: Long): Flow<List<PdfAnnotationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnotation(annotation: PdfAnnotationEntity): Long

    @Query("DELETE FROM pdf_annotations WHERE fileId = :fileId AND pageIndex = :pageIndex AND type = :type")
    suspend fun deleteByPageAndType(fileId: Long, pageIndex: Int, type: PdfAnnotationType)

    @Query("DELETE FROM pdf_annotations WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface PhotoNoteDao {
    @Query("SELECT * FROM photo_notes ORDER BY createdAt DESC")
    fun observeAllPhotoNotes(): Flow<List<PhotoNoteEntity>>

    @Query("SELECT COUNT(*) FROM photo_notes")
    suspend fun countPhotoNotes(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotoNote(note: PhotoNoteEntity): Long

    @Query("DELETE FROM photo_notes WHERE id = :id")
    suspend fun deletePhotoNoteById(id: Long)
}

@Dao
interface MindMapDao {
    @Query("SELECT * FROM mind_maps ORDER BY updatedAt DESC")
    fun observeAllMindMaps(): Flow<List<MindMapEntity>>

    @Query("SELECT * FROM mind_maps WHERE id = :id")
    suspend fun getMindMapById(id: Long): MindMapEntity?

    @Query("SELECT COUNT(*) FROM mind_maps")
    suspend fun countMindMaps(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMindMap(map: MindMapEntity): Long

    @Update
    suspend fun updateMindMap(map: MindMapEntity)

    @Query("DELETE FROM mind_maps WHERE id = :id")
    suspend fun deleteMindMapById(id: Long)
}
