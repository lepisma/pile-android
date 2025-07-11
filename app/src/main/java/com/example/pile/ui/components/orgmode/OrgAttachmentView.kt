package com.example.pile.ui.components.orgmode

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.pile.viewmodel.SharedViewModel
import xyz.lepisma.orgmode.OrgInlineElem
import xyz.lepisma.orgmode.formatInlineElemsToPlaintext


@Composable
private fun OrgImageAttachmentView(file: DocumentFile, link: OrgInlineElem.Link) {
    val context = LocalContext.current
    val description = link.title

    Column(modifier = Modifier.padding(16.dp)) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(file.uri)
                .crossfade(true)
                .build(),
            // placeholder = painterResource(R.drawable.placeholder),
            contentDescription = if (description != null) {
                formatInlineElemsToPlaintext(description)
            } else {
                null
            },
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color.LightGray)
                .clickable {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = file.uri
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                    }
                },
        )
        if (description != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = formatInlineElemsToPlaintext(description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Composable
private fun OrgFileAttachmentView(file: DocumentFile, link: OrgInlineElem.Link) {
    val context = LocalContext.current
    val description = link.title

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = file.uri
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (description != null) {
                Text(
                    text = formatInlineElemsToPlaintext(description),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Text(
                text = link.target,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 1
            )
        }
    }
}

private fun isImageFile(fileName: String): Boolean {
    val lowerCaseFileName = fileName.lowercase()
    return lowerCaseFileName.endsWith(".jpg") ||
            lowerCaseFileName.endsWith(".jpeg") ||
            lowerCaseFileName.endsWith(".png") ||
            lowerCaseFileName.endsWith(".gif") ||
            lowerCaseFileName.endsWith(".bmp") ||
            lowerCaseFileName.endsWith(".webp")
}

@Composable
fun OrgAttachmentView(link: OrgInlineElem.Link, viewModel: SharedViewModel) {
    // We assume that the links of type `attachment` here
    var attachmentFile by remember { mutableStateOf<DocumentFile?>(null) }

    // First we get handle on the attachment file
    LaunchedEffect(link.target) {
        attachmentFile = viewModel.getAttachment(link.target)
    }

    attachmentFile?.let { file ->
        if (isImageFile(link.target)) {
            OrgImageAttachmentView(file, link)
        } else {
            OrgFileAttachmentView(file, link)
        }
    }
}