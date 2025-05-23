package com.example.pile.ui

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.annotation.RequiresApi
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
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
 * Replace portions from annotated string and style them.
 *
 * @param input Input annotated string
 * @param pattern Regex pattern to find matches for, this should work well with the replacementFn
 * @param replacementFn Function that runs on the regex match and annotatedString match and returns
 *                      replacement annotatedString.
 */
fun formatPattern(
    input: AnnotatedString,
    pattern: Regex,
    replacementFn: (MatchResult) -> AnnotatedString
): AnnotatedString {
    val pieces = mutableListOf<AnnotatedString>()

    var lastIndex = 0
    pattern.findAll(input.text).forEach { matchResult ->
        pieces.add(input.subSequence(lastIndex, matchResult.range.first))
        pieces.add(replacementFn(matchResult))
        lastIndex = matchResult.range.last + 1
    }
    if (lastIndex <= input.text.length) {
        pieces.add(input.subSequence(lastIndex, input.text.length))
    }

    return pieces.reduce { acc, annotatedString -> acc + annotatedString }
}

fun formatLinkPattern(
    annotatedString: AnnotatedString,
    colorScheme: ColorScheme
): AnnotatedString {
    return formatPattern(
        annotatedString,
        Regex("""\[\[(?<orglink>.+?)\](?:\[(?<label>[^\]]*?)\])?\]|(?<rawlink>https?:\/\/\S+)""")
    ) { matchResult ->
        val url = matchResult.groups["orglink"]?.value ?: matchResult.groups["rawlink"]?.value ?: ""
        val label = matchResult.groups["label"]?.value ?: url

        val isInternalUrl = url.startsWith("id:")
        val isAttachmentUrl = url.startsWith("attachment:")

        buildAnnotatedString {
            if (isInternalUrl) {
                append("‹ ")
            }
            if (isAttachmentUrl) {
                append("[")
            }

            withStyle(
                SpanStyle(
                    color = colorScheme.primary,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(label)
            }

            val tag: String
            val annotation: String

            if (url.startsWith("http")) {
                tag = "EXTERNAL"
                annotation = url
            } else if (isInternalUrl) {
                tag = "NODE"
                annotation = url.substring(3)
            } else if (isAttachmentUrl) {
                tag = "ATTACHMENT"
                annotation = url.substring(11)
            } else {
                tag = "UNK"
                annotation = url
            }

            addStringAnnotation(tag, annotation, start = length - label.length, length)

            if (isInternalUrl) {
                append(" ›")
            }
            if (isAttachmentUrl) {
                append("]")
            }
        }
    }
}

fun formatItalicPattern(annotatedString: AnnotatedString): AnnotatedString {
    return formatPattern(
        annotatedString,
        Regex("""(?<=^|\s)\/(?<text>\S(.*?)\S)\/""")
    ) { matchResult ->
        buildAnnotatedString {
            withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                append(matchResult.groups["text"]?.value ?: "")
            }
        }
    }
}

fun formatDatePattern(
    annotatedString: AnnotatedString,
    colorScheme: ColorScheme,
    fontSize: TextUnit
): AnnotatedString {
    return formatPattern(
        annotatedString,
        Regex("""[<\[](?<stamp>\d{4}-\d{2}-\d{2}( [a-zA-Z]+)?(\s+\d{1,2}:\d{2})?)[>\]]""")
    ) { matchResult ->
        buildAnnotatedString {
            withStyle(
                SpanStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = fontSize,
                    background = colorScheme.secondaryContainer,
                    color = colorScheme.onSecondaryContainer
                )
            ) {
                append(" ${matchResult.groups["stamp"]?.value} ")
            }
        }
    }
}

fun formatInlineCodePattern(
    annotatedString: AnnotatedString,
    fontSize: TextUnit
): AnnotatedString {
    return formatPattern(
        annotatedString,
        Regex("""(?<=^|\s)[~=](?<text>\S(.*?)\S)[~=]""")
    ) { matchResult ->
        buildAnnotatedString {
            withStyle(
                SpanStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = fontSize
                )
            ) {
                append(matchResult.groups["text"]?.value ?: "")
            }
        }
    }
}

fun formatBoldPattern(annotatedString: AnnotatedString): AnnotatedString {
    return formatPattern(
        annotatedString,
        Regex("""(?<=^|\s)\*(?<text>\S(.*?)\S)\*""")
    ) { matchResult ->
        buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(matchResult.groups["text"]?.value ?: "")
            }
        }
    }
}

fun formatTitlePattern(
    annotatedString: AnnotatedString,
    colorScheme: ColorScheme
): AnnotatedString {
    return formatPattern(
        annotatedString,
        Regex("""^(?<state>TODO|NEXT|ACTIVE|DONE)?(?<title>.*?)(?<completion>\[\d{1,5}\/\d{1,5}\])?$""")
    ) { matchResult ->
        buildAnnotatedString {
            val state = matchResult.groups["state"]?.value
            val title = matchResult.groups["title"]?.value
            val completion = matchResult.groups["completion"]?.value

            if (state != null) {
                withStyle(
                    SpanStyle(
                        background = colorScheme.primaryContainer,
                        color = if (state == "DONE") Color.Gray else colorScheme.onPrimaryContainer
                    )
                ) {
                    append(" $state ")
                }
            }

            if (state == "DONE") {
                withStyle(
                    SpanStyle(
                        color = Color.Gray,
                        textDecoration = TextDecoration.LineThrough
                    )
                ) {
                    append(title)
                }
            } else {
                append(title)
            }

            if (completion != null) {
                withStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        color = colorScheme.primary
                    )
                ) {
                    append(completion)
                }
            }
        }
    }
}

fun formatCheckboxPattern(annotatedString: AnnotatedString): AnnotatedString {
    return formatPattern(
        annotatedString,
        Regex("""(?s)^\[(?<sym>[ X-])\] (?<content>.*)""")
    ) { matchResult ->
        buildAnnotatedString {
            val sym = matchResult.groups["sym"]?.value ?: ""

            val box = when (sym) {
                "X" -> "\uD83D\uDFE9"
                "-" -> "\uD83D\uDD33"
                " " -> "⬜"
                else -> "⬜"
            }

            val content = matchResult.groups["content"]?.value

            if (sym == "X") {
                append("$box ")
                withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                    append(content)
                }
            } else {
                append("$box $content")
            }
        }
    }
}

/**
 * Format special tags like # and @
 */
fun formatTagPattern(annotatedString: AnnotatedString, colorScheme: ColorScheme): AnnotatedString {
    return formatPattern(
        annotatedString,
        Regex("""[@#]\S*""")
    ) { matchResult ->
        buildAnnotatedString {
            withStyle(SpanStyle(color = colorScheme.primary)) {
                append(matchResult.value)
            }
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