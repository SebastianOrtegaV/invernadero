package cl.inacap.invernadero.data

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
// ---------- Modelos: reflejan el JSON que entrega ThingSpeak ----------
data class FeedResponse(
    val channel: ChannelInfo?,
    val feeds: List<Feed>?
)
data class ChannelInfo(
    val id: Long,
    val name: String?,
    @SerializedName("last_entry_id") val lastEntryId: Long?
)
data class Feed(
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("entry_id") val entryId: Long,
    val field1: String?,
    val field2: String?
)
// ---------- Endpoints de la API REST ----------
interface ThingSpeakApi {
    /** Ultimas [results] entradas de un canal (JSON). */
    @GET("channels/{id}/feeds.json")
    suspend fun getFeeds(
        @Path("id") channelId: String,
        @Query("api_key") readKey: String?,
        @Query("results") results: Int = 20
    ): FeedResponse
    /** Ultimo valor de un campo, como texto plano (por ejemplo "1"). */
    @GET("channels/{id}/fields/{field}/last.txt")
    suspend fun getLastField(
        @Path("id") channelId: String,
        @Path("field") field: Int,
        @Query("api_key") readKey: String?
    ): String
    /** Escribe en un canal. Responde el id de la entrada, o "0" si fue
    rechazada. */
    @GET("update")
    suspend fun update(
        @Query("api_key") writeKey: String,
        @Query("field1") field1: String,
        @Query("field2") field2: String
    ): String
    companion object {
        private const val BASE_URL = "https://api.thingspeak.com/"
        fun create(): ThingSpeakApi {
            val logging = HttpLoggingInterceptor().apply{
                level = HttpLoggingInterceptor.Level.BASIC
                }
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build()
            return Retrofit.Builder().baseUrl(BASE_URL).client(client).addConverterFactory(ScalarsConverterFactory.create()).addConverterFactory(GsonConverterFactory.create()).build().create(ThingSpeakApi::class.java)
        }
    }
}