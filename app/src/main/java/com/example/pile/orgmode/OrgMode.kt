package com.example.pile.orgmode

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.example.pile.data.OrgNode
import com.example.pile.data.isDailyNode
import com.example.pile.data.isLiteratureNode
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


enum class OrgListType {
    ORDERED,
    UNORDERED
}

/**
 * Any parsed org element which keeps tracks of the tokens used in parsing
 */
sealed interface OrgElem {
    val tokens: List<Token>
}

/**
 * Represents org document with all info present 'in' the file.
 */
data class OrgDocument (
    val preamble: OrgPreamble,
    val preface: OrgPreface,
    val content: List<OrgSection>,
    override val tokens: List<Token>
) : OrgElem

/**
 * Preamble is everything that comes in the start before the actual content starts
 */
data class OrgPreamble(
    val title: OrgLine,
    val author: String? = null,
    val email: String? = null,
    val date: LocalDate? = null,
    val category: String? = null,
    val filetags: OrgTags? = null,
    val tags: OrgTags? = null,  // NOTE: I use this wrongly in pile
    val options: OrgOptions? = null,
    val pile: OrgOptions? = null,
    val properties: OrgProperties? = null,
    override val tokens: List<Token>
) : OrgElem

/**
 * Options that could go in top of the file, code block headers, etc.
 */
data class OrgOptions(
    val map: Map<String, String>,
    override val tokens: List<Token>
) : OrgElem

/**
 * Unlike simple options, org properties could have full fledged org-mode text
 */
data class OrgProperties(
    val map: Map<String, OrgLine>,
    override val tokens: List<Token>
) : OrgElem

/**
 * Tags for files or headings or anywhere else
 */
data class OrgTags(
    val tags: List<String>,
    override val tokens: List<Token>
) : OrgElem

/**
 * Preface contains the chunks before first heading
 */
data class OrgPreface(
    val body: List<OrgChunk>,
    override val tokens: List<Token>
) : OrgElem

/**
 * A chunk is a block of org mode text that can be of various types as listed here
 */
sealed class OrgChunk {
    data class OrgParagraph(
        val items: List<OrgInlineElem>,
        override val tokens: List<Token>
    ) : OrgChunk(), OrgElem

    data class OrgCommentLine(
        val text: String,
        override val tokens: List<Token>
    ) : OrgChunk(), OrgElem

    data class OrgHorizontalLine(
        override val tokens: List<Token>
    ) : OrgChunk(), OrgElem

    data class OrgTable(
        val dim: Pair<Int, Int>,
        val header: OrgTableRow?,
        val subtables: List<List<OrgTableRow>>,
        val formulaLine: String,
        override val tokens: List<Token>
    ) : OrgChunk(), OrgElem
}

data class OrgTableRow(
    val cells: List<OrgLine>,
    override val tokens: List<Token>
) : OrgElem

/**
 * A single line string with Org Mode formatting enabled
 */
data class OrgLine(
    val items: List<OrgInlineElem>,
    override val tokens: List<Token>
) : OrgElem

/**
 * Represents parsing error of all kinds
 */
data class OrgParsingError(
    val message: String,
    override val tokens: List<Token> = emptyList()
) : OrgElem


/**
 * Represents a null parse (as compared to error)
 */
data class OrgNothing(
    override val tokens: List<Token> = emptyList()
) : OrgElem

/**
 * Represents a plain token parse
 */
data class OrgToken(
    override val tokens: List<Token>
) : OrgElem

data class OrgElemList(
    val items: List<OrgElem>,
    override val tokens: List<Token>
) : OrgElem

/**
 * Options specific to a file in pile
 *
 * These are specified using #+PILE: <key>:<value>, ... syntax. Boolean values are either t or nil
 * (default, unless specified). More options could be present but the app only relies on the ones
 * listed below at the moment.
 *
 * @property pinned  Specifies whether the note is pinned and so should be displayed preferentially.
 * TODO: This is deprecated
 */
data class PileOptions(
    var pinned: Boolean,
)

fun parseLastModified(file: DocumentFile): LocalDateTime {
    val lastModifiedMillis = file.lastModified()
    return Instant.ofEpochMilli(lastModifiedMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
}

/*
 We assume a certain structure of node entries where each file's name has datetime information. And
 we are not considering the nodes that could be found within a file right now.

 Here is an example filename structure:
 20200516002256-studio-vs-war-room.org
 */
fun parseFileDatetime(file: DocumentFile): LocalDateTime {
    // We will search for 3 types of datetime patterns
    // First for daily notes
    val dailyRegex = """(\d{4}-\d{2}-\d{2})\.org""".toRegex()
    val dailyMatch = dailyRegex.find(file.name ?: "")

    if (dailyMatch != null) {
        return LocalDate.parse(dailyMatch.groups[1]!!.value).atStartOfDay()
    }

    // These are regular notes created using org-roam
    val normalRegex = """(\d{14})-.*\.org""".toRegex()
    val normalMatch = normalRegex.find(file.name ?: "")

    if (normalMatch != null) {
        return LocalDateTime.parse(
            normalMatch.groups[1]!!.value,
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        )
    }

    // Else fallback to using last modified time
    return parseLastModified(file)
}

/**
 * Parse pile options from the note's preamble
 */
fun parsePileOptions(preamble: String): PileOptions {
    val options = PileOptions(pinned = false)

    val optionsPattern = Regex("^#\\+PILE:\\s*(.*)", setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))
    val match = optionsPattern.find(preamble)
    val optionsString = match?.groups?.get(1)?.value?.trim() ?: return options

    optionsString.split(",").forEach {
        val kv = it.split(":", limit = 2)

        if (kv.size == 2) {
            if (kv[0] == "pinned") {
                when (kv[1]) {
                    "t" -> options.pinned = true
                    "nil" -> options.pinned = false
                    else -> println("Error in parsing value of pinned option: ${it}")
                }
            } else {
                println("Error in parsing option value: ${it}")
            }
        } else {
            println("Error in parsing option value: ${it}")
        }
    }

    return options
}

// Return list of file level Org Mode tags
fun parseTags(preamble: String): List<String> {
    val tagsPattern = Regex("^#\\+TAGS:\\s*(.*)", setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))
    val match = tagsPattern.find(preamble)
    val tagsString = match?.groups?.get(1)?.value?.trim() ?: return listOf()

    return tagsString.split(",").map { it -> it.trim() }
}

fun parseTitle(preamble: String): String {
    val titlePattern = Regex("^#\\+TITLE:\\s*(.*)", setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))
    val match = titlePattern.find(preamble)

    return match?.groups?.get(1)?.value?.trim() ?: "<NA>"
}

fun parseId(preamble: String): String? {
    val idPattern = Regex("^:ID:\\s*(.*)", setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))
    val match = idPattern.find(preamble)

    return match?.groups?.get(1)?.value?.trim()
}

fun parseOrgRef(preamble: String): String? {
    val idPattern = Regex("^:ROAM_REFS:\\s*(.*)", setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))
    val match = idPattern.find(preamble)

    return match?.groups?.get(1)?.value?.trim()
}

fun unfillText(text: String): String {
    val lines = text.lines()
    val pattern = Regex("^(#|:|\\||[+-]|\\d+\\.)")

    val processedLines = mutableListOf<String>()
    var buffer = ""

    for (i in lines.indices) {
        val currentLine = lines[i]
        if (pattern.containsMatchIn(currentLine) || currentLine.isBlank()) {
            if (buffer.isNotEmpty()) {
                processedLines.add(buffer)
                buffer = ""
            }
            processedLines.add(currentLine)
        } else {
            buffer += if (buffer.isEmpty()) currentLine else " $currentLine"
        }
    }

    if (buffer.isNotEmpty()) {
        processedLines.add(buffer)
    }

    return processedLines.joinToString("\n")
}

/**
 * Regex pattern for lines that are considered part of the Org-mode preamble.
 * This includes lines starting with '#', lines starting with ':', and blank lines,
 * BUT EXCLUDES lines that start with '#+begin'.
 * This ensures the preamble stops before any #+begin block or regular content.
 */
private val PREAMBLE_LINE_REGEX = Regex("^(?!#\\+begin)(#.*|:.*|\\s*)$")

fun dropPreamble(text: String): String {
    val lines = text.lines()

    return lines.dropWhile { it.matches(PREAMBLE_LINE_REGEX) }.joinToString("\n")
}

fun readOrgPreamble(context: Context, file: DocumentFile): String {
    val contentResolver = context.contentResolver
    val inputStream = contentResolver.openInputStream(file.uri)
    val stringBuilder = StringBuilder()
    inputStream?.use { stream ->
        BufferedReader(InputStreamReader(stream)).use { reader ->
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                if (line!!.matches(PREAMBLE_LINE_REGEX)) {
                    stringBuilder.append(line).append("\n")
                } else {
                    break
                }
            }
        }
    }

    return stringBuilder.toString()
}

/**
 * Return exact path to the attachment by the name of `fileName` under the entry with id `id`
 *
 * The path is made up of nested directories by breaking up id in two pieces (first 2 chars and rest
 * of the chars).
 */
fun orgAttachmentPath(attachDir: DocumentFile, id: String, fileName: String): DocumentFile? {
    return attachDir
        .findFile(id.substring(0, 2))
        ?.findFile(id.substring(2))
        ?.findFile(fileName)
}

/**
 * Return the attachment directory of given node, creating it if needed.
 *
 * This is based on the default org-attach setting where a sibling `./data/` dir is used for
 * storing attachments.
 */
fun orgAttachDir(context: Context, rootUri: Uri, node: OrgNode): DocumentFile? {
    val dirName = "data"

    val rootDir = DocumentFile.fromTreeUri(context, rootUri)

    if (rootDir == null || !rootDir.isDirectory) {
        return null
    }

    // There is a chance of inconsistency here since nodes are read as literature nodes based on
    // them having a reference rather than them being in the '/literature' subdirectory.
    val dir = if (isLiteratureNode(node)) {
        val subdir = rootDir.findFile("literature")
        subdir?.findFile(dirName) ?: subdir?.createDirectory(dirName)
    } else if (isDailyNode(node)) {
        val subdir = rootDir.findFile("daily")
        subdir?.findFile(dirName) ?: subdir?.createDirectory(dirName)
    } else {
        rootDir.findFile(dirName) ?: rootDir.createDirectory(dirName)
    }

    return dir
}