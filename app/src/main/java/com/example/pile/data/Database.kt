package com.example.pile.data

import android.content.Context
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

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

object NodeTypeConverter {
    @TypeConverter
    @JvmStatic
    fun toNodeType(value: String?): OrgNodeType? {
        if (value == null) {
            return null
        }

        return when (value) {
            "CONCEPT" -> OrgNodeType.CONCEPT
            "LITERATURE" -> OrgNodeType.LITERATURE
            "DAILY" -> OrgNodeType.DAILY
            else -> null
        }
    }

    @TypeConverter
    @JvmStatic
    fun fromNodeType(value: OrgNodeType?): String? {
        return value?.toString()
    }
}

/**
 * Tag used for org nodes. We just maintain objects of this class for DB operations, everywhere else,
 * plain strings are used to represent tags.
 */
@Entity(tableName = "tags")
data class Tag(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

/**
 * Join between nodes and their tags
 */
@Entity(
    tableName = "node_tags",
    primaryKeys = ["nodeId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = OrgNode::class,
            parentColumns = ["id"],
            childColumns = ["nodeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Tag::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class NodeTag(
    val nodeId: String,
    val tagId: Long
)

/**
 * A link between a source and target node
 */
@Entity(
    tableName = "links",
    primaryKeys = ["sourceId", "targetId"],
    foreignKeys = [
        ForeignKey(
            entity = OrgNode::class,
            parentColumns = ["id"],
            childColumns = ["sourceId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = OrgNode::class,
            parentColumns = ["id"],
            childColumns = ["targetId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Link(
    val sourceId: String,
    val targetId: String,
    val context: String? = null
)

/**
 * Information regarding a node that's relevant for working with file system
 */
data class FileInfo(
    val id: String,
    val fileString: String,
    val lastModified: Long
)

@Dao
interface NodeDao {
    @Insert
    fun insert(node: OrgNode)

    @Insert
    fun insertAll(vararg nodes: OrgNode)

    @Query("SELECT count(*) FROM nodes")
    fun countNodes(): Flow<Int>

    @Query("SELECT id, fileString, lastModified FROM nodes")
    fun getFileInfo(): List<FileInfo>

    @Query("SELECT * FROM nodes")
    fun getAllNodes(): Flow<List<OrgNode>>

    @Query("SELECT * FROM nodes WHERE pinned = 1")
    fun getPinnedNodes(): Flow<List<OrgNode>>

    @Query("SELECT * FROM nodes ORDER BY lastModified DESC LIMIT :limit")
    fun getRecentNodes(limit: Int): Flow<List<OrgNode>>

    @Query("SELECT * FROM nodes WHERE id = :id")
    fun getNodeById(id: String): OrgNode?

    @Query("SELECT * FROM nodes WHERE nodeType = 'DAILY'")
    fun getDailyNodes(): Flow<List<OrgNode>>

    @Query("SELECT * FROM nodes WHERE LOWER(title) LIKE '%' || LOWER(:query) || '%' ORDER BY title ASC")
    fun searchNodesByTitle(query: String): Flow<List<OrgNode>>

    @Update
    fun updateNode(node: OrgNode)

    @Delete
    fun deleteNode(node: OrgNode)

    @Query("DELETE FROM nodes WHERE id = :id")
    fun deleteNodeById(id: String)

    @Query("DELETE FROM nodes")
    fun deleteAll()

    @Query("UPDATE nodes SET pinned = :pinned WHERE id = :id")
    fun togglePinned(id: String, pinned: Boolean)
}

@Dao
interface TagDao {
    @Insert
    fun insert(tag: Tag): Long

    @Query("SELECT * FROM tags WHERE name = :name")
    fun getTagByName(name: String): Tag?

    @Query("SELECT * FROM tags")
    fun getAllTags(): Flow<List<Tag>>

    @Delete
    fun delete(tag: Tag)
}

@Dao
interface NodeTagsDao {
    @Insert
    fun insert(nodeTag: NodeTag)

    @Insert
    fun insertAll(vararg nodeTag: NodeTag)

    @Delete
    fun delete(nodeTag: NodeTag)

    @Query("DELETE FROM node_tags WHERE nodeId = :nodeId")
    fun deleteTagsForNode(nodeId: String)

    @Query("SELECT T.* FROM tags AS T JOIN node_tags AS NT ON T.id = NT.tagId WHERE NT.nodeId = :nodeId")
    fun getTagsForNode(nodeId: String): Flow<List<Tag>>

    @Query("SELECT N.* FROM nodes AS N JOIN node_tags AS NT ON N.id = NT.nodeId WHERE NT.tagId = :tagId")
    fun getNodesForTag(tagId: Long): Flow<List<OrgNode>>
}

@Dao
interface LinkDao {
    @Insert
    fun insert(link: Link)

    @Delete
    fun delete(link: Link)

    @Query("SELECT * FROM links WHERE sourceId = :sourceId")
    fun getTargetLinks(sourceId: String): Flow<List<Link>>

    @Query("SELECT * FROM links WHERE targetId = :targetId")
    fun getSourceLinks(targetId: String): Flow<List<Link>>

    @Query("DELETE FROM links WHERE sourceId = :sourceId")
    fun deleteLinksFrom(sourceId: String)
}

@Database(
    entities = [OrgNode::class, Tag::class, NodeTag::class, Link::class],
    version = 7
)
@TypeConverters(LocalDateTimeConverter::class, NodeTypeConverter::class)
abstract class PileDatabase : RoomDatabase() {
    abstract fun nodeDao(): NodeDao
    abstract fun tagDao(): TagDao
    abstract fun nodeTagsDao(): NodeTagsDao
    abstract fun linkDao(): LinkDao
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

class Migration_5_6(
    private val applicationContext: Context
) : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE nodes ADD COLUMN ref TEXT")
        database.execSQL("ALTER TABLE nodes ADD COLUMN nodeType TEXT NOT NULL DEFAULT 'CONCEPT'")

        // We need to parse existing nodes to get these values
        val cursor = database.query("SELECT id, fileString FROM nodes")
        cursor.use {
            while (it.moveToNext()) {
                val nodeId = it.getString(it.getColumnIndexOrThrow("id"))
                val fileString = it.getString(it.getColumnIndexOrThrow("fileString"))
                val file = DocumentFile.fromTreeUri(applicationContext, fileString.toUri())

                try {
                    val node = parseFileOrgNode(applicationContext, file!!)
                    if (node != null) {
                        database.execSQL(
                            "UPDATE nodes SET nodeType = ?, ref = ? WHERE id = ?",
                            arrayOf(node.nodeType.toString(), node.ref, nodeId)
                        )
                    } else {
                        throw Exception("Error parsing the org node")
                    }
                } catch (e: Exception) {
                    println("Error parsing node from ${fileString}: ${e.message}")
                }
            }
        }
    }
}

val MIGRATION_6_7: Migration = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE tags (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL UNIQUE
            )
        """.trimIndent())

        database.execSQL("""
            CREATE TABLE node_tags (
                nodeId TEXT NOT NULL,
                tagId INTEGER NOT NULL,
                PRIMARY KEY (nodeId, tagId),
                FOREIGN KEY (nodeId) REFERENCES nodes(id) ON DELETE CASCADE,
                FOREIGN KEY (tagId) REFERENCES tags(id) ON DELETE CASCADE
            )
        """.trimIndent())

        database.execSQL("""
            CREATE TABLE links (
                sourceId TEXT NOT NULL,
                targetId TEXT NOT NULL,
                context TEXT,
                PRIMARY KEY (sourceId, targetId)
                FOREIGN KEY (sourceId) REFERENCES nodes(id) ON DELETE CASCADE,
                FOREIGN KEY (targetId) REFERENCES nodes(id) ON DELETE CASCADE
            )
        """.trimIndent())

        // Moving existing tags to tags table
        val cursor = database.query("SELECT id, tags FROM nodes WHERE tags IS NOT NULL AND tags != ''")
        cursor.use {
            while (it.moveToNext()) {
                val nodeId = it.getString(it.getColumnIndexOrThrow("id"))
                val tagsString = it.getString(it.getColumnIndexOrThrow("tags"))
                val tags = tagsString.split(",").map { it.trim() }.filter { it.isNotBlank() }

                for (tag in tags) {
                    var tagId: Long = -1
                    val tagCursor = database.query("SELECT id FROM tags WHERE name = ?", arrayOf(tag))
                    tagCursor.use { tagC ->
                        if (tagC.moveToFirst()) {
                            tagId = tagC.getLong(tagC.getColumnIndexOrThrow("id"))
                        }
                    }

                    if (tagId == -1L) {
                        database.execSQL("INSERT INTO tags (name) VALUES (?)", arrayOf(tag))
                        val lastIdCursor = database.query("SELECT last_insert_rowid()")
                        lastIdCursor.use { lastIdC ->
                            if (lastIdC.moveToFirst()) {
                                tagId = lastIdC.getLong(0)
                            }
                        }
                    }

                    if (tagId != -1L) {
                        try {
                            database.execSQL("INSERT INTO node_tags (nodeId, tagId) VALUES (?, ?)", arrayOf(nodeId, tagId))
                        } catch (e: Exception) {
                            println("Error inserting into node_tags: ${e.message}")
                        }
                    }
                }
            }
        }

        // Dropping tags and file from table. Tags are handled using separate tables, file is
        // recovered from fileString during read
        database.execSQL("""
            CREATE TABLE nodes_new (
                id TEXT NOT NULL PRIMARY KEY,
                title TEXT NOT NULL,
                datetime TEXT NOT NULL,
                fileString TEXT NOT NULL,
                pinned INTEGER NOT NULL DEFAULT 0,
                lastModified INTEGER NOT NULL DEFAULT 0,
                ref TEXT,
                nodeType TEXT NOT NULL DEFAULT 'CONCEPT'
            )
        """.trimIndent())

        database.execSQL("""
            INSERT INTO nodes_new (id, title, datetime, fileString, pinned, lastModified, ref, nodeType)
            SELECT id, title, datetime, fileString, pinned, lastModified, ref, nodeType
            FROM nodes
        """.trimIndent())

        database.execSQL("DROP TABLE nodes")
        database.execSQL("ALTER TABLE nodes_new RENAME TO nodes")
    }
}