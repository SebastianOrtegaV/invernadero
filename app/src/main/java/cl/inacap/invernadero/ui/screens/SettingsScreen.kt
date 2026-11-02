package cl.inacap.invernadero.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cl.inacap.invernadero.data.AppSettings
@Composable
fun SettingsScreen(current: AppSettings, onSave: (AppSettings) -> Unit) {
// Copias locales editables; solo se guardan al presionar el boton
    var sensorId by rememberSaveable {
        mutableStateOf(current.sensorChannelId) }
    var sensorRead by rememberSaveable {
        mutableStateOf(current.sensorReadKey) }
    var controlId by rememberSaveable {
        mutableStateOf(current.controlChannelId) }
    var controlRead by rememberSaveable {
        mutableStateOf(current.controlReadKey) }
    var controlWrite by rememberSaveable {
        mutableStateOf(current.controlWriteKey) }
    var threshold by rememberSaveable {
        mutableStateOf(current.tempThreshold.toString()) }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Canal Sensores (escribe el dispositivo)", style =
            MaterialTheme.typography.titleMedium)
        Field("Channel ID", sensorId, KeyboardType.Number) { sensorId = it }
        Field("Read API Key", sensorRead) { sensorRead = it }
        Text(
            "Canal Control (la app escribe)",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
        Field("Channel ID", controlId, KeyboardType.Number) { controlId = it
        }
        Field("Write API Key", controlWrite) { controlWrite = it }
        Field("Read API Key", controlRead) { controlRead = it }
        Text("Alertas", style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp))
        Field("Umbral de temperatura (°C)", threshold, KeyboardType.Decimal)
        { threshold = it }
        Button(
            onClick = {
                onSave(
                    AppSettings(
                        sensorChannelId = sensorId,
                        sensorReadKey = sensorRead,
                        controlChannelId = controlId,
                        controlReadKey = controlRead,
                        controlWriteKey = controlWrite,
                        tempThreshold = threshold.replace(',',
                            '.').toFloatOrNull() ?: 28f
                    )
                )
            },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) { Text("Guardar") }
    }
}
@Composable
private fun Field(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
    value = value,
    onValueChange = onChange,
    label = { Text(label) },
    singleLine = true,
    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
    modifier = Modifier.fillMaxWidth()
    )
}