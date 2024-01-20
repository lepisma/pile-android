package com.example.pile.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.pile.OrgNode
import com.example.pile.isDailyNode
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalView(nodes: List<OrgNode>, openNode: (OrgNode) -> Unit) {
    var text by remember { mutableStateOf("") }

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
                    Calendar(nodes.map { it.datetime.toLocalDate()!! }) { date ->
                        println("Clicked on $date")
                    }
                }
            } else {
                NodeList(
                    nodes = nodes
                        .filter { isDailyNode(it) }
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

@Composable
fun Calendar(dates: List<LocalDate>, onClick: (LocalDate) -> Unit) {
    val currentDate = LocalDate.now()

    var selectedDate by remember { mutableStateOf(currentDate) }
    var selectedYearMonth by remember { mutableStateOf(YearMonth.of(selectedDate.year, selectedDate.monthValue)) }

    val years = (dates.min().year..(max(dates.max().year, currentDate.year))).toList()
    val months = listOf("January", "February", "March", "April", "June", "July", "August", "September", "October", "November", "December")

    var yearDropdownExpanded by remember { mutableStateOf(false) }
    var yearDropdownSelectedIndex by remember { mutableIntStateOf(years.indexOf(selectedYearMonth.year)) }

    var monthDropdownExpanded by remember { mutableStateOf(false) }
    var monthDropdownSelectedIndex by remember { mutableIntStateOf(selectedYearMonth.monthValue - 1) }

    Row {
        // Year dropdown
        Box {
            Text(
                years[yearDropdownSelectedIndex].toString() + " ▾",
                modifier = Modifier
                    .clickable(onClick = { yearDropdownExpanded = true })
                    .padding(20.dp),
                style = MaterialTheme.typography.headlineMedium
            )
            DropdownMenu(
                expanded = yearDropdownExpanded,
                onDismissRequest = { yearDropdownExpanded = false }) {
                years.forEachIndexed { index, year ->
                    DropdownMenuItem(text = { Text(year.toString(), style = MaterialTheme.typography.headlineMedium) }, onClick = {
                        yearDropdownSelectedIndex = index
                        yearDropdownExpanded = false
                        selectedYearMonth = selectedYearMonth.withYear(year)
                    })
                }
            }
        }

        // Month dropdown
        Box {
            Text(
                months[monthDropdownSelectedIndex] + " ▾",
                modifier = Modifier
                    .clickable(onClick = { monthDropdownExpanded = true })
                    .padding(20.dp),
                style = MaterialTheme.typography.headlineMedium
            )
            DropdownMenu(
                expanded = monthDropdownExpanded,
                onDismissRequest = { monthDropdownExpanded = false }) {
                months.forEachIndexed { index, month ->
                    DropdownMenuItem(text = { Text(month, style = MaterialTheme.typography.headlineMedium) }, onClick = {
                        monthDropdownSelectedIndex = index
                        monthDropdownExpanded = false
                        selectedYearMonth = selectedYearMonth.withMonth(months.indexOf(month) + 1)
                    })
                }
            }
        }
    }

    val daysInMonth = selectedYearMonth.lengthOfMonth()

    LazyVerticalGrid(columns = GridCells.Fixed(7)) {
        items(DayOfWeek.values()) { day ->
            Box(
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = day.name.take(3), color = Color.Gray)
            }
        }

        items(max(LocalDate.of(selectedYearMonth.year, selectedYearMonth.monthValue, 1).dayOfWeek.value % 7 - 1, 0)) {
            Box(
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {}
        }

        items((1..daysInMonth).toList()) { day ->
            Box(modifier = Modifier
                .height(50.dp)
                .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = day.toString())
            }
        }
    }
}