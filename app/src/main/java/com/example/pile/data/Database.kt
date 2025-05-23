package com.example.pile.data

import androidx.documentfile.provider.DocumentFile
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

object TagsConverter {
    @TypeConverter
    @JvmStatic
    fun toTags(value: String): List<String> {
        return value.split(",").map { it -> it.trim() }
    }

    @TypeConverter
    @JvmStatic
    fun fromTags(tags: List<String>): String {
        if (tags.isEmpty()) {
            return ""
        }

        return tags.joinToString(", ")
    }
}

object LocalDateTimeConverter {
    @TypeConverter
    @JvmStatic
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it) }
    }

    @TypeConverter
    @JvmStatic
    fun fromLocalDateTime(date: LocalDateTime?): String? {
        return date?.toString()
    }
}

object DocumentFileConverter {
    @TypeConverter
    @JvmStatic
    fun toDocumentFile(value: String?): DocumentFile? {
        return null
    }

    @TypeConverter
    @JvmStatic
    fun fromDocumentFile(value: DocumentFile?): String? {
        return value?.toString()
    }
}

@Dao
interface NodeDao {
    @Insert
    fun insert(node: OrgNode)

    @Insert
    fun insertAll(vararg nodes: OrgNode)

    @Query("SELECT count(*) FROM nodes")
    fun countNodes(): Flow<Int>

    @Query("SELECT * FROM nodes")
    fun getAllNodes(): Flow<List<OrgNode>>

    @Query("SELECT * FROM nodes ORDER BY lastModified DESC LIMIT :limit")
    fun getRecentNodes(limit: Int): Flow<List<OrgNode>>

    @Query("SELECT * FROM nodes WHERE id = :id")
    fun getNodeById(id: String): OrgNode?

    @Query("SELECT * FROM nodes WHERE title LIKE '____-__-__' AND STRFTIME('%Y-%m-%d', title) = title")
    fun getDailyNodes(): Flow<List<OrgNode>>

    @Query("SELECT * FROM nodes WHERE LOWER(title) LIKE '%' || LOWER(:query) || '%' ORDER BY title ASC")
    fun searchNodesByTitle(query: String): Flow<List<OrgNode>>

    @Update
    fun updateNode(node: OrgNode)

    @Delete
    fun deleteNode(node: OrgNode)

    @Query("DELETE FROM nodes")
    fun deleteAll()

    @Query("UPDATE nodes SET pinned = :pinned WHERE id = :id")
    fun togglePinned(id: String, pinned: Boolean)
}

@Database(entities = [OrgNode::class], version = 5)
@TypeConverters(LocalDateTimeConverter::class, DocumentFileConverter::class, TagsConverter::class)
abstract class PileDatabase : RoomDatabase() {
    abstract fun nodeDao(): NodeDao
}

val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE nodes ADD COLUMN bookmarked INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_2_3: Migration = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // This is assuming we don't have field renaming capability in the SQLite version
        database.execSQL("""
            CREATE TABLE nodes_new (
                id TEXT NOT NULL PRIMARY KEY,
                title TEXT NOT NULL,
                datetime TEXT NOT NULL,
                fileString TEXT NOT NULL,
                file TEXT,
                pinned INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())

        database.execSQL("""
            INSERT INTO nodes_new (id, title, datetime, fileString, file, pinned)
            SELECT id, title, datetime, fileString, file, bookmarked
            FROM nodes
        """.trimIndent())

        database.execSQL("DROP TABLE nodes")

        database.execSQL("ALTER TABLE nodes_new RENAME TO nodes")
    }
}

val MIGRATION_3_4: Migration = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE nodes ADD COLUMN tags TEXT NOT NULL DEFAULT ''")
    }
}

val MIGRATION_4_5: Migration = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE nodes ADD COLUMN lastModified INTEGER NOT NULL DEFAULT 0")
    }
}