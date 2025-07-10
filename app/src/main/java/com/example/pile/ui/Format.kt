package com.example.pile.ui

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.annotation.RequiresApi
import androidx.documentfile.provider.DocumentFile
import java.net.URLDecoder
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@RequiresApi(Build.VERSION_CODES.S)
fun formatRelativeTime(lastModifiedMillis: Long): String {
    val nowInstant = Instant.now()
    val lastModifiedInstant = Instant.ofEpochMilli(lastModifiedMillis)

    val zoneId = ZoneId.systemDefault()
    val lastModifiedZoned = lastModifiedInstant.atZone(zoneId)
    val nowZoned = nowInstant.atZone(zoneId)

    val duration = Duration.between(lastModifiedInstant, nowInstant)
    val seconds = duration.toSeconds()
    val minutes = duration.toMinutes()
    val hours = duration.toHours()

    val daysBetween = ChronoUnit.DAYS.between(lastModifiedZoned.toLocalDate(), nowZoned.toLocalDate())

    return when {
        seconds < 60 -> "few seconds ago"
        minutes < 60 -> "$minutes minutes ago"
        hours < 24 -> "$hours hours ago"
        daysBetween == 1L -> "Yesterday"
        daysBetween > 1 && daysBetween <= 7 -> "$daysBetween days ago"
        lastModifiedZoned.year == nowZoned.year -> {
            val formatter = DateTimeFormatter.ofPattern("MMM dd")
            lastModifiedZoned.format(formatter)
        }
        else -> {
            val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
            lastModifiedZoned.format(formatter)
        }
    }
}

/**
 * Formats a given Android Uri (especially SAF Uris) into a human-readable string.
 *
 * @param context The application context, needed to query document information.
 * @param uri The Uri to format. Can be null.
 * @return A pretty-formatted string representing the Uri, or a default message if null/unparseable.
 */
fun formatUriForDisplay(context: Context, uri: Uri?): String {
    if (uri == null) {
        return "No URI set"
    }

    val documentFile = DocumentFile.fromTreeUri(context, uri)
    val displayName = documentFile?.name

    if (uri.scheme == "content" && DocumentsContract.isTreeUri(uri)) {
        val documentId = DocumentsContract.getTreeDocumentId(uri)
        val parts = documentId.split(":", limit = 2) // Split into volume and path

        val volumeName = if (parts.isNotEmpty()) parts[0] else ""
        val path = if (parts.size > 1) parts[1] else ""

        val decodedPath = try {
            URLDecoder.decode(path, "UTF-8")
        } catch (e: Exception) {
            path
        }

        // Map common volume names
        val friendlyVolumeName = when (volumeName) {
            "primary" -> "Internal Storage"
            else -> {
                "SD Card"
            }
        }

        return if (decodedPath.isNotEmpty()) {
            "$friendlyVolumeName/$decodedPath"
        } else {
            friendlyVolumeName
        }
    } else if (uri.scheme == "file") {
        return uri.path?.let {
            try {
                URLDecoder.decode(it, "UTF-8")
            } catch (e: Exception) {
                it
            }
        } ?: uri.toString()
    }
    return displayName ?: uri.toString()
}