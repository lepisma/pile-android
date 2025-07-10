package com.example.pile.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.example.pile.orgmode.parseFileDatetime
import com.example.pile.orgmode.parseId
import com.example.pile.orgmode.parseOrgRef
import com.example.pile.orgmode.parsePileOptions
import com.example.pile.orgmode.parseTags
import com.example.pile.orgmode.parseTitle
import com.example.pile.orgmode.readOrgPreamble
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

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
    var snakeCaseTitle = title.split(Regex("\\s+")).joinToString("_").lowercase()

    // There is some sanitization that the file writing system does on its own. We will try to
    // replicate that here
    snakeCaseTitle = snakeCaseTitle.replace(Regex("[\\\\/:*?\"<>|]"), "_")
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
            val createdFile = createAndWriteToFile(context, dir, fileName, initialContent)

            createdFile?.let { file ->
                return OrgNode(
                    id = nodeId,
                    title = noteTitle,
                    datetime = datetime,
                    fileString = file.uri.toString(),
                    file = file,
                    tags = nodeTags ?: listOf(),
                    lastModified = file.lastModified(),
                    nodeType = OrgNodeType.LITERATURE,
                    ref = nodeRef
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

        val createdFile = createAndWriteToFile(context, root, fileName, initialContent)

        createdFile?.let { file ->
            return OrgNode(
                id = nodeId,
                title = noteTitle,
                datetime = datetime,
                fileString = file.uri.toString(),
                file = file,
                tags = nodeTags ?: listOf(),
                lastModified = file.lastModified(),
                nodeType = OrgNodeType.CONCEPT,
                ref = null
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
            val createdFile = createAndWriteToFile(context, dir, fileName, initialContent)

            createdFile?.let { file ->
                return OrgNode(
                    id = nodeId,
                    title = noteTitle,
                    datetime = datetime,
                    fileString = file.uri.toString(),
                    file = file,
                    tags = nodeTags ?: listOf(),
                    lastModified = file.lastModified(),
                    nodeType = OrgNodeType.DAILY,
                    ref = null
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
        OrgNodeType.LITERATURE -> createNewLiteratureNode(
            context,
            noteTitle,
            rootUri,
            nodeRef,
            nodeTags
        )
        OrgNodeType.DAILY -> createNewDailyNode(context, noteTitle, rootUri, nodeTags)
        else -> createNewConceptNode(context, noteTitle, rootUri, nodeTags)
    }
}

fun createAndWriteToFile(context: Context, directory: DocumentFile, fileName: String, text: String): DocumentFile? {
    val newFile = directory.createFile("application/octet-stream", fileName)

    newFile?.uri?.let { uri ->
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(text.toByteArray())
        }
    }
    return newFile
}

fun writeFile(context: Context, file: DocumentFile, text: String) {
    context.contentResolver.openOutputStream(file.uri, "wt")?.use { outputStream ->
        outputStream.write(text.toByteArray(Charsets.UTF_8))
    }
}

// TODO: Use orgmode-kmp for this. This has to be fast and we only need preamble content, nothing
//  beyond that
fun parseFileOrgNode(context: Context, file: DocumentFile): OrgNode {
    val preamble = readOrgPreamble(context, file)
    val title = parseTitle(preamble)
    val tags = parseTags(preamble)
    val ref = parseOrgRef(preamble)
    val pileOptions = parsePileOptions(preamble)

    // This is not correct since UUID is probably not the way org-id works
    val nodeId = parseId(preamble) ?: UUID.randomUUID().toString()
    val fileString = file.uri.toString()

    // This will need more work after I allow configuring paths for literature and daily nodes
    val nodeType = if (title.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
        // Any node with Date like pattern in title is a daily note
        OrgNodeType.DAILY
    } else if (ref != null) {
        // A node with ref set is literature note
        OrgNodeType.LITERATURE
    } else {
        OrgNodeType.CONCEPT
    }

    return OrgNode(
        id = nodeId,
        title = title,
        datetime = parseFileDatetime(file),
        fileString = fileString,
        file = file,
        tags = tags,
        lastModified = file.lastModified(),
        ref = ref,
        nodeType = nodeType,
        pinned = pileOptions.pinned
    )
}

/**
 * Data class to bypass DocumentFile for faster directory scanning
 */
data class OptimizedFileInfo(
    val uri: Uri,
    val name: String,
    val isDirectory: Boolean,
    val lastModified: Long
)

/**
 * List all org node files from the given directory
 */
fun nodeFilesFromDirectory(context: Context, uri: Uri): List<OptimizedFileInfo> {
    val fileList: MutableList<OptimizedFileInfo> = mutableListOf()
    val contentResolver = context.contentResolver

    try {
        val rootDocumentId = DocumentsContract.getTreeDocumentId(uri)
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(uri, rootDocumentId)

        traverseOrgFiles(contentResolver, childrenUri, fileList)
    } catch (e: Exception) {
        Log.e("FS", "Error initializing SAF traversal for URI: $uri", e)
    }

    return fileList
}

private fun traverseOrgFiles(
    contentResolver: ContentResolver,
    currentUri: Uri,
    fileList: MutableList<OptimizedFileInfo>
) {
    val projection = arrayOf(
        DocumentsContract.Document.COLUMN_DOCUMENT_ID,
        DocumentsContract.Document.COLUMN_MIME_TYPE,
        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
        DocumentsContract.Document.COLUMN_LAST_MODIFIED
    )

    var cursor: android.database.Cursor? = null

    try {
        cursor = contentResolver.query(
            currentUri,
            projection,
            null,
            null,
            null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val documentId = it.getString(it.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID))
                val mimeType = it.getString(it.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE))
                val displayName = it.getString(it.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME))
                val lastModified = it.getLong(it.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_LAST_MODIFIED))

                val childUri = DocumentsContract.buildDocumentUriUsingTree(currentUri, documentId)

                if (DocumentsContract.Document.MIME_TYPE_DIR == mimeType) {
                    traverseOrgFiles(
                        contentResolver,
                        DocumentsContract.buildChildDocumentsUriUsingTree(childUri, documentId),
                        fileList
                    )
                } else if (displayName.endsWith(".org", ignoreCase = true)) {
                    fileList.add(
                        OptimizedFileInfo(
                            uri = childUri,
                            name = displayName,
                            isDirectory = false,
                            lastModified = lastModified
                        )
                    )
                }
            }
        }
    } catch (e: Exception) {
        Log.e("FS", "Error traversing SAF tree at URI: $currentUri - ${e.message}", e)
    } finally {
        cursor?.close()
    }
}