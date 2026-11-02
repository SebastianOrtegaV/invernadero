package cl.inacap.invernadero.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.inacap.invernadero.ui.UiState
import cl.inacap.invernadero.ui.components.LineChart
import java.time.ZoneId
import java.time.format.DateTimeFormatter
private val rowFormat = DateTimeFormatter.ofPattern("dd/MM HH:mm:ss").withZone(ZoneId.systemDefault())
@Composable
fun HistoryScreen(state: UiState) {
    val readings = state.readings
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                "Últimas ${readings.size} lecturas",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        item {
            LineChart(
                values = readings.mapNotNull { it.temperature },
                label = "Temperatura",
                unit = "°C",
                color = MaterialTheme.colorScheme.primary
            )
        }
        item {
            LineChart(
                values = readings.mapNotNull { it.humidity },
                label = "Humedad",
                unit = "%",
                color = MaterialTheme.colorScheme.tertiary
            )
        }
        item {
            Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text("Hora", Modifier.weight(1.4f), style =
                    MaterialTheme.typography.labelLarge)
                Text("Temp.", Modifier.weight(1f), style =
                    MaterialTheme.typography.labelLarge)
                Text("Hum.", Modifier.weight(1f), style =
                    MaterialTheme.typography.labelLarge)
            }
            HorizontalDivider()
        }
        // Mas recientes primero
        items(readings.reversed()) { r ->
            Row(Modifier.fillMaxWidth()) {
                Text(rowFormat.format(r.time), Modifier.weight(1.4f))
                Text(r.temperature?.let { "%.1f °C".format(it) } ?: "--",
                    Modifier.weight(1f))
                Text(r.humidity?.let { "%.0f %%".format(it) } ?: "--",
                    Modifier.weight(1f))
            }
        }
    }
}
