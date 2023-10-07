package com.example.pile

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
    val lines = preamble.lines()

    val titleLine = lines.firstOrNull {
        it.startsWith("#+TITLE:", ignoreCase = true)
    } ?: return "<NA>"

    return Regex("#\\+TITLE:", RegexOption.IGNORE_CASE).find(titleLine)?.let {
        titleLine.substring(it.range.last + 1).trim()
    } ?: "<NA>"
}

fun parseId(preamble: String): String? {
    val lines = preamble.lines()

    val idLine = lines.firstOrNull {
        it.startsWith(":ID:", ignoreCase = true)
    } ?: return null

    return Regex(":ID:", RegexOption.IGNORE_CASE).find(idLine)?.let {
        idLine.substring(it.range.last + 1).trim()
    }
}

fun parseOrgBody(text: String): String {
    val lines = text.lines().dropWhile {
        it.startsWith(" ") || it.startsWith( ":") || it.startsWith("#")
    }

    return lines.joinToString("\n").trim()
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