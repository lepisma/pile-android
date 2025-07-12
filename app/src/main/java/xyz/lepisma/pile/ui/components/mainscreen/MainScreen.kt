package xyz.lepisma.pile.ui.components.mainscreen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.lepisma.pile.ui.components.mainscreen.home.HomeView
import xyz.lepisma.pile.ui.theme.PileTheme
import xyz.lepisma.pile.viewmodel.SharedViewModel
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Book
import compose.icons.fontawesomeicons.solid.Glasses
import compose.icons.fontawesomeicons.solid.Hammer


@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: SharedViewModel,
    openNodeById: (String) -> Unit,
    captureLinkInitial: String?
) {
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedNavIndex by remember { mutableIntStateOf(0) }
    var showCaptureSheet by remember { mutableStateOf(captureLinkInitial != null) }

    PileTheme {
        Scaffold (
            topBar = {
                Box {
                    if (isLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            },
            bottomBar = {
                NavigationBar {
                    listOf(
                        Pair("Notes", FontAwesomeIcons.Solid.Book),
                        Pair("Search", FontAwesomeIcons.Solid.Glasses),
                        Pair("Settings", FontAwesomeIcons.Solid.Hammer)
                    ).forEachIndexed { index, (label, icon) ->
                        NavigationBarItem(
                            selected = (selectedNavIndex == index),
                            onClick = { selectedNavIndex = index },
                            icon = {
                                Icon(
                                    icon,
                                    modifier = Modifier.size(18.dp),
                                    contentDescription = label
                                )
                            },
                            label = { Text(label) })
                    }
                }

            }
        ) { innerPadding ->
            Column(
                modifier = Modifier.padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Spacer(Modifier.weight(1f))

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    when (selectedNavIndex) {
                        0 -> HomeView(viewModel, openNodeById)
                        1 -> SearchView()
                        2 -> SettingsView(viewModel)
                    }

                    if (showCaptureSheet) {
                        CaptureSheet(
                            captureLink = captureLinkInitial!!,
                            viewModel = viewModel,
                            openNodeById = openNodeById,
                            onDismiss = { showCaptureSheet = false }
                        )
                    }
                }
            }
        }
    }
}