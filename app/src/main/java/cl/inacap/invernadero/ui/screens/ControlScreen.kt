package cl.inacap.invernadero.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.inacap.invernadero.ui.UiState
@Composable
fun ControlScreen(state: UiState, onToggleFan: (Boolean) -> Unit, onToggleWater: (Boolean) -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Actuadores del invernadero", style = MaterialTheme.typography.titleLarge)
        ActuatorCard("Ventilador", state.fanOn, !state.sendingCommand && state.settings.isConfigured, onToggleFan)
        ActuatorCard("Riego (manguera)", state.waterOn, !state.sendingCommand && state.settings.isConfigured, onToggleWater)
    }
}

@Composable
private fun ActuatorCard(name: String, on: Boolean, enabled: Boolean, onChange: (Boolean) -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.titleMedium)
                Text(if (on) "Encendido" else "Apagado", style = MaterialTheme.typography.bodyMedium)
            }
            Switch(checked = on, onCheckedChange = onChange, enabled = enabled)
        }
    }
}