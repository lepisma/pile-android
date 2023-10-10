package com.example.pile

import android.content.Context
import androidx.documentfile.provider.DocumentFile
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

    data class OrgList(override var text: String, val type: OrgListType, val items: List<OrgParagraph>) : OrgParagraph()
    data class OrgPlainParagraph(override var text: String) : OrgParagraph()
    data class OrgQuote(override var text: String) : OrgParagraph()
    data class OrgBlock(override var text: String) : OrgParagraph()
    data class OrgTable(override var text: String) : OrgParagraph()
    data class OrgHorizontalLine(override var text: String): OrgParagraph()
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
                        val b = it as OrgParagraph.OrgList
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

    val items = ("\n" + text).split(Regex("\\n(\\d+\\.|\\+|-) "))
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map {
            OrgParagraph.OrgPlainParagraph(it)
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
    val lastModifiedMillis = file.lastModified()
    return Instant.ofEpochMilli(lastModifiedMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
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

    return lines.dropWhile {
        it.startsWith(" ") || it.startsWith(":") || it.startsWith("#")
    }.joinToString("\n")
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
                if (line!!.startsWith(" ") || line!!.startsWith(":") || line!!.startsWith("#")) {
                    stringBuilder.append(line).append("\n")
                } else {
                    break
                }
            }
        }
    }

    return stringBuilder.toString()
}