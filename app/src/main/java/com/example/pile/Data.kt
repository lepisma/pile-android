package com.example.pile

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * Data class representing each node in the zettelkasten. As of now this maps to file based nodes
 * and not Org header based ones.
 *
 * @property pinned Pile-Android specific flag to tell if this node is pinned. This can be moved to
 *                  files later.
 * @property tags   Org mode file tags.
 */
@Entity(tableName = "nodes")
data class OrgNode(
    @PrimaryKey
    val id: String,
    val title: String,
    val datetime: LocalDateTime,
    val fileString: String,
    val file: DocumentFile? = null,
    val pinned: Boolean = false,
    val tags: List<String> = listOf()
)

enum class OrgNodeType {
    CONCEPT, LITERATURE, DAILY
}

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

    @Query("SELECT * FROM nodes")
    fun getAllNodes(): List<OrgNode>

    @Update
    fun updateNode(node: OrgNode)

    @Delete
    fun deleteNode(node: OrgNode)

    @Query("DELETE FROM nodes")
    fun deleteAll()

    @Query("UPDATE nodes SET pinned = :pinned WHERE id = :id")
    fun togglePinned(id: String, pinned: Boolean)
}

@Database(entities = [OrgNode::class], version = 4)
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

fun readFile(context: Context, file: DocumentFile): String {
    val contentResolver = context.contentResolver
    val inputStream = contentResolver.openInputStream(file.uri)
    val stringBuilder = StringBuilder()
    inputStream?.use { stream ->
        BufferedReader(InputStreamReader(stream)).use { reader ->
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                stringBuilder.append(line).append("\n")
            }
        }
    }

    return stringBuilder.toString()
}

fun generateInitialContent(noteTitle: String, nodeId: String, nodeRef: String?, nodeTags: List<String>?): String {
    var content = """
    :PROPERTIES:
    :ID:      $nodeId
    ${if (nodeRef != null) ":ROAM_REFS: $nodeRef" else "" }
    :END:
    ${if (nodeTags != null) "#+TAGS: ${nodeTags.joinToString(", ")}" else ""}
    #+TITLE: $noteTitle
    """.trimIndent()

    content = content
        .split("\n")
        .filter { it.isNotBlank() }
        .joinToString("\n")

    return content + "\n"
}

/**
 * Generate file name for the given node type.
 */
fun generateFileName(title: String, datetime: LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
    val snakeCaseTitle = title.split(Regex("\\s+")).joinToString("_").lowercase()
    return "${datetime.format(formatter)}-$snakeCaseTitle.org"
}

private fun createNewLiteratureNode(context: Context, noteTitle: String, rootUri: Uri, nodeRef: String?, nodeTags: List<String>?): OrgNode? {
    DocumentFile.fromTreeUri(context, rootUri)?.let { root ->
        val nodeId = UUID.randomUUID().toString()
        val datetime = LocalDateTime.now()
        val fileName = generateFileName(noteTitle, datetime)
        val initialContent = generateInitialContent(noteTitle, nodeId, nodeRef, nodeTags)
        val directory = root.findFile("literature") ?: root.createDirectory("literature")

        directory?.let { dir ->
            createAndWriteToFile(context, dir, fileName, initialContent)

            dir.findFile(fileName)?.let {
                return OrgNode(
                    id = nodeId,
                    title = noteTitle,
                    datetime = datetime,
                    fileString = it.uri.toString(),
                    file = it
                )
            }
        }
    }
    return null
}

private fun createNewConceptNode(context: Context, noteTitle: String, rootUri: Uri, nodeTags: List<String>?): OrgNode? {
    DocumentFile.fromTreeUri(context, rootUri)?.let { root ->
        val nodeId = UUID.randomUUID().toString()
        val datetime = LocalDateTime.now()
        val fileName = generateFileName(noteTitle, datetime)
        val initialContent = generateInitialContent(noteTitle, nodeId, null, nodeTags)

        createAndWriteToFile(context, root, fileName, initialContent)

        root.findFile(fileName)?.let {
            return OrgNode(
                id = nodeId,
                title = noteTitle,
                datetime = datetime,
                fileString = it.uri.toString(),
                file = it
            )
        }
    }
    return null
}

private fun createNewDailyNode(context: Context, noteTitle: String, rootUri: Uri, nodeTags: List<String>?): OrgNode? {
    if (!noteTitle.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
        println("Node title $noteTitle doesn't match daily title format.")
        return null
    }

    DocumentFile.fromTreeUri(context, rootUri)?.let { root ->
        val nodeId = UUID.randomUUID().toString()
        val datetime = LocalDateTime.now()
        val fileName = "$noteTitle.org"
        val initialContent = generateInitialContent(noteTitle, nodeId, null, nodeTags)
        val directory = root.findFile("daily") ?: root.createDirectory("daily")

        directory?.let { dir ->
            createAndWriteToFile(context, dir, fileName, initialContent)

            dir.findFile(fileName)?.let {
                return OrgNode(
                    id = nodeId,
                    title = noteTitle,
                    datetime = datetime,
                    fileString = it.uri.toString(),
                    file = it
                )
            }
        }
    }
    return null
}

/**
 * Create a new node in file system
 *
 * @param context
 * @param noteTitle Text that defines the name of note. For daily nodes, this should be in the exact
 *                  format YYYY-MM-DD.
 * @param rootUri Uri of the root directory where files are kept
 * @param nodeType
 * @param nodeRef Roam ref link to be added in case of literature node
 * @param nodeTags Org mode style (file level) tags
 */
fun createNewNode(
    context: Context,
    noteTitle: String,
    rootUri: Uri,
    nodeType: OrgNodeType = OrgNodeType.CONCEPT,
    nodeRef: String? = null,
    nodeTags: List<String>? = null): OrgNode? {
    return when (nodeType) {
        OrgNodeType.LITERATURE -> createNewLiteratureNode(context, noteTitle, rootUri, nodeRef, nodeTags)
        OrgNodeType.DAILY -> createNewDailyNode(context, noteTitle, rootUri, nodeTags)
        else -> createNewConceptNode(context, noteTitle, rootUri, nodeTags)
    }
}

/*
 Tell whether this is a daily note node only based on the file title. This would improve and become
 more robust later.
 */
fun isDailyNode(node: OrgNode): Boolean = node.title.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))

fun isLiteratureNodePath(path: String): Boolean {
    val pattern = Regex("%2Fliterature%2F\\d{14}-")
    return pattern.containsMatchIn(path)
}

fun isLiteratureNode(node: OrgNode): Boolean {
    if (node.file?.parentFile?.name == "literature") {
        return true
    }

    return isLiteratureNodePath(node.fileString)
}

// Tell if the node is a literature node which is not sorted (using Raindrop's terminology). These
// are links that have not been read or skimmed.
fun isUnsortedNode(node: OrgNode): Boolean {
    return node.tags.contains("unsorted")
}

fun createAndWriteToFile(context: Context, directory: DocumentFile, fileName: String, text: String) {
    val newFile = directory.createFile("application/octet-stream", fileName)

    newFile?.uri?.let { uri ->
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(text.toByteArray())
        }
    }
}

fun writeFile(context: Context, file: DocumentFile, text: String) {
    context.contentResolver.openOutputStream(file.uri, "wt")?.use { outputStream ->
        outputStream.write(text.toByteArray(Charsets.UTF_8))
    }
}

fun parseFileOrgNode(context: Context, file: DocumentFile): OrgNode {
    val preamble = readOrgPreamble(context, file)
    val title = parseTitle(preamble)
    val tags = parseTags(preamble)
    // This is not correct since UUID is probably not the way org-id works
    val nodeId = parseId(preamble) ?: UUID.randomUUID().toString()

    return OrgNode(nodeId, title, parseFileDatetime(file), file.uri.toString(), file, tags = tags)
}

suspend fun readFilesFromDirectory(context: Context, uri: Uri): List<OrgNode> = coroutineScope {
    val fileList: MutableList<DocumentFile> = mutableListOf()
    val root = DocumentFile.fromTreeUri(context, uri)
    if (root != null) {
        traverseOrgFiles(root, fileList)
    }

    return@coroutineScope fileList.map { file ->
        async { parseFileOrgNode(context, file) }
    }.awaitAll()
}

suspend fun refreshDatabase(context: Context, uri: Uri, nodeDao: NodeDao) {
    val newNodes = readFilesFromDirectory(context, uri).associateBy { it.id }
    val existingNodes = nodeDao.getAllNodes().associateBy { it.id }

    for (newNode in newNodes.values) {
        existingNodes[newNode.id]?.let { existingNode ->
            val updatedNode = newNode.copy(pinned = existingNode.pinned)
            nodeDao.updateNode(updatedNode)
        } ?: run {
            nodeDao.insert(newNode)
        }
    }

    val nodesToDelete = existingNodes.filterKeys { it !in newNodes.keys }
    nodesToDelete.values.forEach { nodeDao.deleteNode(it) }
}

suspend fun loadNodes(context: Context, nodeDao: NodeDao): List<OrgNode> = coroutineScope {
    val nodes = nodeDao.getAllNodes()
    // `file` field has to be recovered
    return@coroutineScope nodes.map { node ->
        node.copy(file = DocumentFile.fromTreeUri(context, Uri.parse(node.fileString)))
    }
}

fun traverseOrgFiles(dir: DocumentFile, fileList: MutableList<DocumentFile>) {
    for (file in dir.listFiles()) {
        if (file.isDirectory) {
            traverseOrgFiles(file, fileList)
        } else if (file.isFile && file.name?.endsWith(".org") == true) {
            fileList.add(file)
        }
    }
}

fun saveRootPath(context: Context, uri: Uri) {
    val sharedPref = context.getSharedPreferences("pile", Context.MODE_PRIVATE)
    with (sharedPref.edit()) {
        putString("root-path", uri.toString())
        apply()
    }

    /* Also take persistent permissions */
    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    context.contentResolver.takePersistableUriPermission(uri, takeFlags)
}

/* Return the saved root path if any */
fun loadRootPath(context: Context): Uri? {
    val saved = context.getSharedPreferences("pile", Context.MODE_PRIVATE).getString("root-path", null)

    return if (saved != null) {
        val uri = Uri.parse(saved)
        val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        context.contentResolver.takePersistableUriPermission(uri, takeFlags)
        uri
    } else {
        null
    }
}