package cl.inacap.invernadero.data

import android.content.Context
/** Datos de conexion que el usuario ingresa en la pestana Ajustes. */
data class AppSettings(
    val sensorChannelId: String = "",
    val sensorReadKey: String = "",
    val controlChannelId: String = "",
    val controlReadKey: String = "",
    val controlWriteKey: String = "",
    val tempThreshold: Float = 28f
) {
    val isConfigured: Boolean
        get() = sensorChannelId.isNotBlank() && controlChannelId.isNotBlank()
                && controlWriteKey.isNotBlank()
}
/** Guarda y lee los ajustes en SharedPreferences (quedan en el telefono). */
class SettingsRepository(context: Context) {
    private val prefs = context.getSharedPreferences("thingspeak_settings",

                Context.MODE_PRIVATE)
    fun load(): AppSettings = AppSettings(
        sensorChannelId = prefs.getString(KEY_SENSOR_ID, "") ?: "",
        sensorReadKey = prefs.getString(KEY_SENSOR_READ, "") ?: "",
        controlChannelId = prefs.getString(KEY_CONTROL_ID, "") ?: "",
        controlReadKey = prefs.getString(KEY_CONTROL_READ, "") ?: "",
        controlWriteKey = prefs.getString(KEY_CONTROL_WRITE, "") ?: "",
        tempThreshold = prefs.getFloat(KEY_THRESHOLD, 28f)
    )
    fun save(s: AppSettings) {
        prefs.edit()
            .putString(KEY_SENSOR_ID, s.sensorChannelId.trim())
            .putString(KEY_SENSOR_READ, s.sensorReadKey.trim())
            .putString(KEY_CONTROL_ID, s.controlChannelId.trim())
            .putString(KEY_CONTROL_READ, s.controlReadKey.trim())
            .putString(KEY_CONTROL_WRITE, s.controlWriteKey.trim())
            .putFloat(KEY_THRESHOLD, s.tempThreshold)
            .apply()
    }
    private companion object {
        const val KEY_SENSOR_ID = "sensor_channel_id"
        const val KEY_SENSOR_READ = "sensor_read_key"
        const val KEY_CONTROL_ID = "control_channel_id"
        const val KEY_CONTROL_READ = "control_read_key"
        const val KEY_CONTROL_WRITE = "control_write_key"
        const val KEY_THRESHOLD = "temp_threshold"
    }
}