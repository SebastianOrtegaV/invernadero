package cl.inacap.invernadero.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cl.inacap.invernadero.ui.UiState
import cl.inacap.invernadero.ui.theme.SemaforoAmarillo
import cl.inacap.invernadero.ui.theme.SemaforoRojo
import cl.inacap.invernadero.ui.theme.SemaforoVerde
import java.time.ZoneId
import java.time.format.DateTimeFormatter
@RequiresApi(Build.VERSION_CODES.O)
private val timeFormat =
    DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault())
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DashboardScreen(state: UiState, onRefresh: () -> Unit) {
    Column(

        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error)
        }
        // Estado del dispositivo
        Card(Modifier.fillMaxWidth()) {
            Row(Modifier.padding(16.dp), verticalAlignment =
                Alignment.CenterVertically) {
                Dot(if (state.deviceOnline) SemaforoVerde else Color.Gray)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Invernadero", style =
                        MaterialTheme.typography.titleMedium)
                    Text(
                        if (state.deviceOnline) "En línea" else "Desconectado(sin datos en 2 min)",
                                style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
// Lecturas actuales
        val latest = state.latest
        val temp = latest?.temperature
        val threshold = state.settings.tempThreshold
        val semaforo = when {
            temp == null -> Color.Gray
            temp >= threshold -> SemaforoRojo
            temp >= threshold - 2f -> SemaforoAmarillo
            else -> SemaforoVerde
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ValueCard(
                title = "Temperatura",
                value = temp?.let { "%.1f °C".format(it) } ?: "--",
                indicator = semaforo,
                modifier = Modifier.weight(1f)
            )
            ValueCard(
                title = "Humedad",
                value = latest?.humidity?.let { "%.0f %%".format(it) } ?: "--",
                        indicator = null,
                modifier = Modifier.weight(1f)
            )
        }
        Text(
            "Umbral de alerta: %.1f °C (verde < %.1f, amarillo < %.1f, rojo desde %.1f)"
        .format(threshold, threshold - 2f, threshold, threshold),
        style = MaterialTheme.typography.bodySmall
        )
        // Datos de actualizacion
        Text(
            "Última lectura del dispositivo: " + (latest?.let {
                timeFormat.format(it.time) } ?: "sin datos"),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            "Última consulta a ThingSpeak: " + (state.lastRefresh?.let {
                timeFormat.format(it) } ?: "--"),
            style = MaterialTheme.typography.bodySmall
        )
        OutlinedButton(onClick = onRefresh) { Text("Actualizar ahora") }
    }
}
@Composable
private fun ValueCard(title: String, value: String, indicator: Color?,
                      modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f))
                if (indicator != null) Dot(indicator)
            }
            Spacer(Modifier.size(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium)
        }
    }
}
@Composable
private fun Dot(color: Color) {
    Box(Modifier.size(14.dp).background(color, CircleShape))
}