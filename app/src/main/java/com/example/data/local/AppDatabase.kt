package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class ReaderTypeConverters {
    @TypeConverter
    fun fromFileCategory(value: FileCategory): String = value.name

    @TypeConverter
    fun toFileCategory(value: String): FileCategory = FileCategory.valueOf(value)

    @TypeConverter
    fun fromChecklistType(value: ChecklistType): String = value.name

    @TypeConverter
    fun toChecklistType(value: String): ChecklistType = ChecklistType.valueOf(value)

    @TypeConverter
    fun fromPdfAnnotationType(value: PdfAnnotationType): String = value.name

    @TypeConverter
    fun toPdfAnnotationType(value: String): PdfAnnotationType = PdfAnnotationType.valueOf(value)
}

@Database(
    entities = [
        LocalFileEntity::class,
        NoteEntity::class,
        ChecklistItemEntity::class,
        VoiceMemoEntity::class,
        PdfAnnotationEntity::class,
        PhotoNoteEntity::class,
        MindMapEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(ReaderTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun localFileDao(): LocalFileDao
    abstract fun noteDao(): NoteDao
    abstract fun checklistDao(): ChecklistDao
    abstract fun voiceMemoDao(): VoiceMemoDao
    abstract fun pdfAnnotationDao(): PdfAnnotationDao
    abstract fun photoNoteDao(): PhotoNoteDao
    abstract fun mindMapDao(): MindMapDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "reader.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
