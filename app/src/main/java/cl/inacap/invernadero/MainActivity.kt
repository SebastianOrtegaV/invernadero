package cl.inacap.invernadero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.inacap.invernadero.ui.MainViewModel
import cl.inacap.invernadero.ui.screens.ControlScreen
import cl.inacap.invernadero.ui.screens.DashboardScreen
import cl.inacap.invernadero.ui.screens.HistoryScreen
import cl.inacap.invernadero.ui.screens.SettingsScreen
import cl.inacap.invernadero.ui.theme.ThingSpeakIoTTheme
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ThingSpeakIoTTheme {
                IoTApp()
            }
        }
    }
}
/** Las 4 pestanas de la barra inferior. */
private enum class Tab(val title: String, val icon: ImageVector) {
    Inicio("Inicio", Icons.Filled.Home),
    Control("Control", Icons.Filled.Build),
    Historial("Historial", Icons.Filled.List),
    Ajustes("Ajustes", Icons.Filled.Settings)
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IoTApp(vm: MainViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    var selected by rememberSaveable { mutableIntStateOf(0) }
    val tab = Tab.entries[selected]
    val snackbar = remember { SnackbarHostState() }
    // Muestra los mensajes del ViewModel como Snackbar
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            vm.clearMessage()
        }
    }
    // Sin configuracion: abrir directamente Ajustes la primera vez
    LaunchedEffect(Unit) {
        if (!state.settings.isConfigured) selected = Tab.Ajustes.ordinal
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tab.title) },
                actions = {
                    IconButton(onClick = vm::refresh) {
                        Icon(Icons.Filled.Refresh, contentDescription =
                            "Actualizar")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                Tab.entries.forEachIndexed { index, t ->
                    NavigationBarItem(
                        selected = index == selected,
                        onClick = { selected = index },
                        icon = { Icon(t.icon, contentDescription = t.title)
                        },
                        label = { Text(t.title) }
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (tab) {
                Tab.Inicio -> DashboardScreen(state, onRefresh = vm::refresh)
                Tab.Control -> ControlScreen(state, onToggleLed = vm::setLed)
                Tab.Historial -> HistoryScreen(state)
                Tab.Ajustes -> SettingsScreen(state.settings, onSave =
                    vm::saveSettings)
            }
        }
    }
}