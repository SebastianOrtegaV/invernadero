package cl.inacap.invernadero.data

import java.time.Instant
/** Una lectura ya convertida a tipos utiles para la interfaz. */
data class Reading(
    val time: Instant,
    val temperature: Float?,
    val humidity: Float?
)
/** Unica puerta de entrada a ThingSpeak: la UI y el ViewModel no conocen
Retrofit. */
class ThingSpeakRepository(private val api: ThingSpeakApi =
                               ThingSpeakApi.create()) {
    suspend fun getReadings(channelId: String, readKey: String, results: Int = 100): List<Reading> {
        val response = api.getFeeds(channelId, readKey.ifBlank { null },
            results)
        return response.feeds.orEmpty().mapNotNull { feed ->
            val time = runCatching { Instant.parse(feed.createdAt)
            }.getOrNull() ?: return@mapNotNull null
            Reading(
                time = time,
                temperature = feed.field1?.trim()?.toFloatOrNull(),
                humidity = feed.field2?.trim()?.toFloatOrNull()
            )
        }
    }
    /** Devuelve true si el ultimo valor del LED es "1". */
    suspend fun getActuators(channelId: String, readKey: String): Pair<Boolean, Boolean> {
        val k = readKey.ifBlank { null }
        val fan = api.getLastField(channelId, 1, k).trim() == "1"
        val water = api.getLastField(channelId, 2, k).trim() == "1"
        return fan to water
    }

    suspend fun sendCommand(writeKey: String, fan: Boolean, water: Boolean): Boolean {
        val id = api.update(writeKey, if (fan) "1" else "0", if (water) "1" else "0")
        return id.trim() != "0"
    }
}