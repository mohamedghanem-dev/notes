package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.FolderDao
import com.example.data.dao.NoteDao
import com.example.data.model.FolderEntity
import com.example.data.model.NoteEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [NoteEntity::class, FolderEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "notes_database"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Prepopulate default folders on creation
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                populateInitialData(database.folderDao(), database.noteDao())
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(folderDao: FolderDao, noteDao: NoteDao) {
            val personalId = folderDao.insertFolder(FolderEntity(name = "Personal", colorHex = "#3F51B5", iconName = "person"))
            val workId = folderDao.insertFolder(FolderEntity(name = "Work", colorHex = "#E91E63", iconName = "work"))
            val ideasId = folderDao.insertFolder(FolderEntity(name = "Ideas & Thoughts", colorHex = "#009688", iconName = "lightbulb"))
            
            // Insert initial welcome note
            noteDao.insertNote(
                NoteEntity(
                    title = "Welcome to Notes! 📝",
                    content = "Thank you for using Notes. Here are some key features:\n\n" +
                            "• Offline SQLite storage powered by Room\n" +
                            "• Encrypted password & privacy lock protection\n" +
                            "• Bilingual English and Arabic RTL interface\n" +
                            "• Folder, Tags, Starred, and Archive organization\n" +
                            "• Custom touch drawing pad and formatting bar\n\n" +
                            "Enjoy taking notes securely!",
                    folderId = personalId,
                    isPinned = true,
                    isFavorite = true,
                    colorHex = "#FEF3C7",
                    tags = "Welcome,Guide"
                )
            )
        }
    }
}
