package com.example.pile

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import java.time.LocalDateTime
import io.github.serpro69.kfaker.Faker
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors

data class OrgNode(
    val title: String,
    val datetime: LocalDateTime,
    val file: DocumentFile,
    val id: String,
    val ref: String? = null
)

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
        return LocalDateTime.parse(normalMatch.groups[1]!!.value, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
    }

    // Else fallback to using last modified time
    val lastModifiedMillis = file.lastModified()
    return Instant.ofEpochMilli(lastModifiedMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
}

fun parseFileTitle(context: Context, file: DocumentFile): String {
    val contentResolver = context.contentResolver
    val inputStream = contentResolver.openInputStream(file.uri)
    inputStream?.use { stream ->
        BufferedReader(InputStreamReader(stream)).use { reader ->
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                if (line!!.startsWith("#+TITLE:")) {
                    return line!!.substringAfter("#+TITLE:").trim()
                }
            }
        }
    }

    return "<NA>"
}
fun parseFileOrgNode(context: Context, file: DocumentFile): OrgNode {
    val content = ""
    val title = parseFileTitle(context, file)
    val nodeId = ""

    return OrgNode(title, parseFileDatetime(file), file, nodeId)
}

fun readFilesFromDirectory(context: Context, uri: Uri): List<OrgNode> {
    println(uri.toString())

    val fileList: MutableList<DocumentFile> = mutableListOf()
    val root = DocumentFile.fromTreeUri(context, uri)
    if (root != null) {
        traverseOrgFiles(root, fileList)
    }

    return fileList.map { parseFileOrgNode(context, it) }
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