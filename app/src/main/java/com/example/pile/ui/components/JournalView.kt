package com.example.pile.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.pile.OrgNode
import com.example.pile.OrgNodeType
import com.example.pile.isDailyNode
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalView(nodes: List<OrgNode>, openNode: (OrgNode) -> Unit, createAndOpenNode: (String, nodeType: OrgNodeType, String?) -> Unit) {
    var text by remember { mutableStateOf("") }
    val currentDate = LocalDate.now()
    val context = LocalContext.current

    val dailyNodes = nodes.filter { isDailyNode(it) }

    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
        if (nodes.isNotEmpty()) {
            if (text == "") {
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
                            openNode(dateNode)
                        } else if (dateIsToday) {
                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                            createAndOpenNode(date.format(formatter), OrgNodeType.DAILY, null)
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
                    onClick = openNode
                )
            }
        }
        FindField(text = text, onTextEntry = { text = it }, label = "Find", placeholder = "Node Name")
    }
}