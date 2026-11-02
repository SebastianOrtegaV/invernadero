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
                val led = thingSpeak.getLedState(s.controlChannelId,
                    s.controlReadKey)
                _state.update {
                    it.copy(
                        readings = readings,
                        ledOn = if (it.sendingCommand) it.ledOn else led,
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
    fun setLed(on: Boolean) {
        val s = _state.value.settings
        val now = Instant.now()
        val last = lastCommandAt
        if (last != null) {
            val wait = COMMAND_COOLDOWN_S - Duration.between(last, now).seconds
            if (wait > 0) {
                _state.update { it.copy(message = "ThingSpeak permite 1 escritura cada 15 s. Espera $wait s.") }
                return
            }
        }
        viewModelScope.launch {
            _state.update { it.copy(sendingCommand = true, ledOn = on,
                message = null) }
            try {
                val ok = thingSpeak.sendLedCommand(s.controlWriteKey, on)
                if (ok) {
                    lastCommandAt = Instant.now()
                    _state.update {
                        it.copy(sendingCommand = false, message = "Comando enviado a ThingSpeak.")
                    }
                } else {
                    _state.update {
                        it.copy(sendingCommand = false, ledOn = !on, message
                        = "ThingSpeak rechazó el comando (revisa la Write Key o espera 15 s).")
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(sendingCommand = false, ledOn = !on,
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