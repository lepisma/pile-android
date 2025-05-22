package com.example.pile.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pile.OrgNodeType
import com.example.pile.viewmodel.SharedViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalView(
    viewModel: SharedViewModel,
    openNodeById: (String) -> Unit,
    createAndOpenNode: (nodeTitle: String, nodeType: OrgNodeType, refLink: String?, tags: List<String>?) -> Unit
) {
    var text by remember { mutableStateOf("") }
    val currentDate = LocalDate.now()
    val context = LocalContext.current

    val dailyNodes by viewModel.dailyNodes.collectAsState()

    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
        if (dailyNodes.isNotEmpty()) {
            if (text == "") {
                Text(
                    text = "Journal",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.outline,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Text(
                    text = "Create and search your org-roam dailies here",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
                Spacer(Modifier.weight(1f))
                ElevatedCard(
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Calendar(
                        dates = dailyNodes.map { it.datetime.toLocalDate()!! }
                    ) { date ->
                        val dateNode = dailyNodes.find { it.datetime.toLocalDate()!! == date }
                        val dateIsToday = date == currentDate

                        if (dateNode != null) {
                            openNodeById(dateNode.id)
                        } else if (dateIsToday) {
                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                            createAndOpenNode(date.format(formatter), OrgNodeType.DAILY, null, null)
                        } else {
                            Toast.makeText(
                                context,
                                "Node creation not allowed for days other than today",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } else {
                NodeList(
                    nodes = dailyNodes
                        .filter { text.lowercase() in it.title.lowercase() }
                        .take(5),
                    heading = null,
                    onClick = openNodeById
                )
            }
        }
        FindField(text = text, onTextEntry = { text = it }, label = "Find", placeholder = "Node Name")
    }
}