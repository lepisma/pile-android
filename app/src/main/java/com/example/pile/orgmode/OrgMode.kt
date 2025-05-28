package com.example.pile.orgmode

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.example.pile.data.OrgNode
import com.example.pile.data.isDailyNode
import com.example.pile.data.isLiteratureNode
import com.orgzly.org.parser.OrgParsedFile
import com.orgzly.org.parser.OrgParser
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

sealed class OrgParagraph {
    abstract var text: String

    data class OrgList(override var text: String, val type: OrgListType, val items: List<OrgListItem>) : OrgParagraph()
    data class OrgListItem(override var text: String, val items: List<OrgParagraph>) : OrgParagraph()
    data class OrgPlainParagraph(override var text: String) : OrgParagraph()
    data class OrgQuote(override var text: String) : OrgParagraph()
    data class OrgBlock(override var text: String) : OrgParagraph()
    data class OrgTable(override var text: String) : OrgParagraph()
    data class OrgHorizontalLine(override var text: String) : OrgParagraph()
    data class OrgLogBook(override var text: String) : OrgParagraph()
}

/**
 * Options specific to a file in pile
 *
 * These are specified using #+PILE: <key>:<value>, ... syntax. Boolean values are either t or nil
 * (default, unless specified). More options could be present but the app only relies on the ones
 * listed below at the moment.
 *
 * @property pinned  Specifies whether the note is pinned and so should be displayed preferentially.
 */
data class PileOptions(
    var pinned: Boolean,
)

fun parseNodeLinks(orgText: String): List<String> {
    val pattern = Regex("\\[\\[id:([0-9a-fA-F\\-]+)](\\[(.*?)])?]")

    return pattern.findAll(orgText).toList().mapNotNull {
        it.groups[1]?.value
    }
}

fun parseOrgParagraphs(text: String): List<OrgParagraph> {
    val brokenTexts = breakHeadingContent(text)

    val brokenOrgParagraphs = brokenTexts.map {
        if (it.matches(Regex("-----"))) {
            OrgParagraph.OrgHorizontalLine(it)
        } else if (it.matches(Regex("^\\|.*"))) {
            OrgParagraph.OrgTable(it)
        } else if (it.matches(Regex("(?s)^(\\+|-|\\d+\\.) .*"))) {
            parseOrgList(it)
        } else if (it.matches(Regex("(?is)(#\\+begin_quote).*"))) {
            parseOrgQuote(it)
        } else if (it.matches(Regex("(?is)(#\\+begin).*"))) {
            OrgParagraph.OrgBlock(it)
        } else if (it.matches(Regex("(?is):LOGBOOK:.*"))) {
            OrgParagraph.OrgLogBook(it)
        } else {
            OrgParagraph.OrgPlainParagraph(it)
        }
    }

    return brokenOrgParagraphs.fold(mutableListOf<OrgParagraph>()) { acc, it ->
        if (acc.isEmpty()) {
            acc.add(it)
        } else {
            if (acc.last()::class == it::class) {
                when (it::class) {
                    OrgParagraph.OrgList::class -> {
                        val a = acc.last() as OrgParagraph.OrgList
                        val b = it as OrgParagraph.OrgList
                        acc[acc.lastIndex] = OrgParagraph.OrgList(
                            a.text + "\n" + b.text, a.type, a.items + b.items
                        )
                    }
                    OrgParagraph.OrgTable::class -> {
                        val a = acc.last() as OrgParagraph.OrgTable
                        val b = it as OrgParagraph.OrgTable
                        acc[acc.lastIndex] = OrgParagraph.OrgTable(a.text + "\n" + b.text)
                    }
                    else -> { acc.add(it) }
                }
            } else {
                acc.add(it)
            }
        }
        acc
    }
}

fun parseOrgList(text: String): OrgParagraph.OrgList {
    val type = if (text.matches(Regex("(?s)^\\d.*"))) OrgListType.ORDERED else OrgListType.UNORDERED

    val items = ("\n" + text).split(Regex("\\n(?=(\\+|-|\\d+\\.) )"))
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map {
            val nIndent = (Regex("\n +").find(it)?.value?.length ?: 1) - 1
            val processedText = it.replace(Regex("^(\\d+\\.|\\+|-) "), " ".repeat(nIndent)).trimIndent()
            OrgParagraph.OrgListItem(processedText, items = parseOrgParagraphs(processedText))
        }

    return OrgParagraph.OrgList(text, type = type, items = items)
}

fun parseOrgQuote(text: String) =
    OrgParagraph.OrgQuote(text.replace(Regex("(?i)#\\+begin_quote|#\\+end_quote"), "").trim())

/*
 Break text based on begin and end blocks
 */
fun breakBlocks(text: String): List<String> {
    return text.split(Regex("(?i)(\\n(?=#\\+begin)|(?<=#\\+end_[a-z]{1,20})\\n)"))
        .map { it.trim() }
        .filter { it.isNotEmpty() }
}

/*
 Tokenizer for texts below a heading. High level heading parsing happens via Org-Java, but this
 takes care of the parsing for body text within a heading (also used in org file preface).
 */
fun breakHeadingContent(text: String): List<String> {
    val blocks = breakBlocks(text)

    return blocks.fold(listOf<String>()) { acc, it ->
        if (it.startsWith("#+")) {
            acc + it
        } else {
            acc + it.split(Regex("\\n((?=\\|)|\\n|(?=(\\+|-|\\d+\\.) ))"))
        }
    }
        .map { it.trim() }
        .filter { it.isNotEmpty() }
}

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

fun parseOrg(text: String): OrgParsedFile {
    val orgParser = OrgParser.Builder()
    return orgParser.setInput(text).build().parse()
}

fun dropPreamble(text: String): String {
    val lines = text.lines()

    return lines.dropWhile { it.matches(Regex("^(?!#\\+begin)([:#].*)?$")) }.joinToString("\n")
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

fun readOrgPreamble(context: Context, file: DocumentFile): String {
    val contentResolver = context.contentResolver
    val inputStream = contentResolver.openInputStream(file.uri)
    val stringBuilder = StringBuilder()
    inputStream?.use { stream ->
        BufferedReader(InputStreamReader(stream)).use { reader ->
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                if (line!!.matches(Regex("^(?!#\\+begin)([:#].*)?$"))) {
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