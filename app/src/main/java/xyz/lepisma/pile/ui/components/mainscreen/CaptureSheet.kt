package xyz.lepisma.pile.ui.components.mainscreen

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import xyz.lepisma.pile.data.OrgNodeType
import xyz.lepisma.pile.viewmodel.SharedViewModel
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Regular
import compose.icons.fontawesomeicons.regular.Bookmark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureSheet(
    captureLink: String,
    viewModel: SharedViewModel,
    openNodeById: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val captureSheetState = rememberModalBottomSheetState()
    var link by remember { mutableStateOf(captureLink) }
    var title by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(captureLink) {
        title = try {
            withContext(Dispatchers.IO) {
                val document = Jsoup.connect(link).get()
                document.title()
            }
        } catch (e: Exception) {
            "Link title resolution failed"
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = captureSheetState
    ) {
        var readChecked by remember { mutableStateOf(false) }

        Column(
            modifier = modifier
                .padding(horizontal = 40.dp, vertical = 40.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = FontAwesomeIcons.Regular.Bookmark,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 10.dp),
                    contentDescription = "Literature Node"
                )
                Text(
                    "Capture Node",
                    color = Color.Gray,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            TextField(
                value = link,
                onValueChange = { link = it },
                label = { Text("Note link") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent
                )
            )
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(60.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(checked = readChecked, onCheckedChange = { readChecked = it })
                Text("Read", modifier = Modifier.padding(start = 10.dp))
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                FilledTonalButton(onClick = {
                    viewModel.createNode(
                        title = title,
                        nodeType = OrgNodeType.LITERATURE,
                        ref = link,
                        tags = if (readChecked) null else listOf("unsorted")
                    ) { node ->
                        onDismiss()
                        Toast.makeText(context, "Link captured", Toast.LENGTH_SHORT).show()
                        openNodeById(node.id)
                    }
                }) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Save",
                            modifier = Modifier.padding(end = 10.dp)
                        )
                        Text("Save")
                    }
                }
            }
        }
    }
}