package cl.inacap.invernadero.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.inacap.invernadero.ui.UiState
@Composable
fun ControlScreen(state: UiState, onToggleLed: (Boolean) -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Actuadores", style = MaterialTheme.typography.titleLarge)
        Card(Modifier.fillMaxWidth()) {
            Row(Modifier.padding(16.dp), verticalAlignment =
                Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("LED del dispositivo", style =
                        MaterialTheme.typography.titleMedium)
                    Text(
                        if (state.ledOn) "Encendido" else "Apagado",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (state.sendingCommand) {
                    CircularProgressIndicator(Modifier.padding(end = 12.dp))
                }
                Switch(
                    checked = state.ledOn,
                    onCheckedChange = onToggleLed,
                    enabled = !state.sendingCommand &&
                            state.settings.isConfigured
                )
            }
        }
        Text(
            "La app escribe 1 o 0 en el field1 del canal de Control. El dispositivo leerá " +
            "ese valor desde ThingSpeak; por ahora pueden verificarlo en la vista del canal.",
                    style = MaterialTheme.typography.bodySmall
        )
    }
}