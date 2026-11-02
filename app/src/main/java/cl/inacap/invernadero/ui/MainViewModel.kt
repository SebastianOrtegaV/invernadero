package cl.inacap.invernadero.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.inacap.invernadero.data.AppSettings
import cl.inacap.invernadero.data.Reading
import cl.inacap.invernadero.data.SettingsRepository
import cl.inacap.invernadero.data.ThingSpeakRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
/** Todo lo que la interfaz necesita dibujar, en un solo objeto inmutable. */
data class UiState(
    val settings: AppSettings = AppSettings(),
    val readings: List<Reading> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val lastRefresh: Instant? = null,
    val fanOn: Boolean = false,
    val waterOn: Boolean = false,
    val sendingCommand: Boolean = false,
    val message: String? = null
) {
    val latest: Reading? get() = readings.lastOrNull()
    val latestTemperature: Float?
        get() = readings.lastOrNull { it.temperature != null }?.temperature

    val latestHumidity: Float?
        get() = readings.lastOrNull { it.humidity != null }?.humidity
    /** Consideramos el dispositivo "en linea" si envio datos en los ultimos
    2 minutos. */
    val deviceOnline: Boolean
        get() = latest?.let { Duration.between(it.time, Instant.now()).seconds < 120 } ?: false
}
class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val settingsRepo = SettingsRepository(app)
    private val thingSpeak = ThingSpeakRepository()
    private val _state = MutableStateFlow(UiState(settings =
        settingsRepo.load()))
    val state: StateFlow<UiState> = _state.asStateFlow()
    private var pollingJob: Job? = null
    private var lastCommandAt: Instant? = null
    init {
        startPolling()
    }
    /** Consulta ThingSpeak cada 15 segundos mientras el ViewModel exista. */
    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                refresh()
                delay(POLL_INTERVAL_MS)
            }
        }
    }
    fun refresh() {
        val s = _state.value.settings
        if (!s.isConfigured) {
            _state.update { it.copy(error = "Configura los canales en la pestaña Ajustes") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            try {
                val readings = thingSpeak.getReadings(s.sensorChannelId,
                    s.sensorReadKey)
                val (fan, water) = thingSpeak.getActuators(s.controlChannelId, s.controlReadKey)
                _state.update {
                    it.copy(
                        readings = readings,
                        fanOn = if (it.sendingCommand) it.fanOn else fan,
                        waterOn = if (it.sendingCommand) it.waterOn else water,
                        loading = false,
                        error = null,
                        lastRefresh = Instant.now()
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = "Error de conexión: ${e.message}") }
            }
        }
    }
    fun setFan(on: Boolean) = sendActuators(on, _state.value.waterOn)
    fun setWater(on: Boolean) = sendActuators(_state.value.fanOn, on)

    private fun sendActuators(fan: Boolean, water: Boolean) {
        val prev = _state.value
        val s = prev.settings
        val now = Instant.now()
        lastCommandAt?.let { last ->
            val wait = COMMAND_COOLDOWN_S - Duration.between(last, now).seconds
            if (wait > 0) {
                _state.update { it.copy(message = "Espera $wait s.") }
                return
            }
        }
        viewModelScope.launch {
            _state.update { it.copy(sendingCommand = true, fanOn = fan, waterOn = water, message = null) }
            try {
                val ok = thingSpeak.sendCommand(s.controlWriteKey, fan, water)
                if (ok) lastCommandAt = Instant.now()
                _state.update {
                    if (ok) it.copy(sendingCommand = false, message = "Activando")
                    else it.copy(sendingCommand = false, fanOn = prev.fanOn, waterOn = prev.waterOn,
                        message = "Comando rechazado (revisa la Write Key o espera 15 s).")
                }
            } catch (e: Exception) {
                _state.update { it.copy(sendingCommand = false, fanOn = prev.fanOn, waterOn = prev.waterOn,
                    message = "Error de conexión: ${e.message}") }
            }
        }
    }
    fun saveSettings(newSettings: AppSettings) {
        settingsRepo.save(newSettings)
        _state.update { it.copy(settings = settingsRepo.load(), message =
            "Ajustes guardados") }
        startPolling()
    }
    fun clearMessage() {
        _state.update { it.copy(message = null) }
    }
    private companion object {
        const val POLL_INTERVAL_MS = 15_000L
        const val COMMAND_COOLDOWN_S = 15L
    }
}