package com.example.pile.ui.components.orgmode

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import com.example.pile.orgmode.unfillText
import com.example.pile.ui.formatBoldPattern
import com.example.pile.ui.formatCheckboxPattern
import com.example.pile.ui.formatDatePattern
import com.example.pile.ui.formatInlineCodePattern
import com.example.pile.ui.formatItalicPattern
import com.example.pile.ui.formatLinkPattern
import com.example.pile.ui.formatTagPattern
import com.example.pile.viewmodel.SharedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun OrgText(text: String, viewModel: SharedViewModel, openNodeById: (String) -> Unit, modifier: Modifier = Modifier) {
    val coroutineScope = rememberCoroutineScope()
    var formattedString by remember { mutableStateOf<AnnotatedString?>(null) }
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val localUriHandler = LocalUriHandler.current

    val context = LocalContext.current

    LaunchedEffect(text) {
        coroutineScope.launch(Dispatchers.Default) {
            var annotatedString = buildAnnotatedString { append(unfillText(text)) }
            annotatedString = formatCheckboxPattern(annotatedString)
            annotatedString = formatLinkPattern(annotatedString, colorScheme)
            annotatedString = formatTagPattern(annotatedString, colorScheme)
            annotatedString = formatItalicPattern(annotatedString)
            annotatedString = formatBoldPattern(annotatedString)
            annotatedString = formatInlineCodePattern(annotatedString, typography.bodyMedium.fontSize)
            annotatedString = formatDatePattern(annotatedString, colorScheme, typography.bodyMedium.fontSize)
            withContext(Dispatchers.Main) { formattedString = annotatedString }
        }
    }

    if (formattedString != null) {
        ClickableText(
            text = formattedString!!,
            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            onClick = { offset ->
                formattedString!!.getStringAnnotations("NODE", offset, offset).firstOrNull()?.let {
                    openNodeById(it.item)
                }
                formattedString!!.getStringAnnotations("EXTERNAL", offset, offset).firstOrNull()?.let {
                    localUriHandler.openUri(it.item)
                }
                formattedString!!.getStringAnnotations("ATTACHMENT", offset, offset).firstOrNull()?.let {
                    val fileName = it.item
                    try {
                        coroutineScope.launch {
                            withContext(Dispatchers.IO) {
                                val filePath = viewModel.getAttachmentFile(fileName)
                                if (filePath != null) {
                                    localUriHandler.openUri(filePath.uri.toString())
                                } else {
                                    Toast.makeText(context, "Unable to get and open path for $fileName", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        val errorMessage = "Failed to open ${fileName}: $e"
                        Log.d("FileInteractionError", errorMessage)
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = modifier.padding(bottom = 10.dp)
        )
    } else {
        Text(text)
    }
}