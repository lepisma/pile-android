package com.example.pile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pile.dropPreamble
import com.example.pile.parseOrg
import com.example.pile.parseRoamRef
import com.example.pile.parseTitle
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Bookmark

@Composable
fun OrgPreview(text: String) {
    Column {
        OrgTitle(title = parseTitle(text))
        OrgRoamRef(text = text)
        OrgBody(text = text)
    }
}

@Composable
fun OrgTitle(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.headlineLarge,
        modifier = Modifier.padding(bottom = 20.dp)
    )
}

@Composable
fun OrgRoamRef(text: String) {
    val context = LocalContext.current

    parseRoamRef(text)?.let {
        OutlinedButton(onClick = { println(it) }) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.Bookmark,
                    contentDescription = "Reference Icon",
                    modifier = Modifier.size(SwitchDefaults.IconSize)
                )
                Text("Reference")
            }
        }
    }
}

@Composable
fun OrgBody(text: String) {
    val parsed = parseOrg(dropPreamble(text))

    Column {
        Text(parsed.file.preface)

        parsed.headsInList.forEach {
            val style = when (it.level) {
                1 -> MaterialTheme.typography.headlineLarge
                2 -> MaterialTheme.typography.headlineMedium
                else -> MaterialTheme.typography.headlineSmall
            }

            Text(
                text = it.head.title,
                style = style,
                modifier = Modifier.padding(top = 20.dp, bottom = 10.dp)
            )
            Text(
                text = it.head.content
            )
        }
    }
}