package cl.inacap.invernadero.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
// Colores del semaforo (se usan en el dashboard)
val SemaforoVerde = Color(0xFF2E7D32)
val SemaforoAmarillo = Color(0xFFF9A825)
val SemaforoRojo = Color(0xFFC62828)

private val LightColors = lightColorScheme(
    primary = Color(0xFF0B6E4F),
    secondary = Color(0xFF08A045),
    tertiary = Color(0xFF1565C0)
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFF6BD4A8),
    secondary = Color(0xFF7FE0A0),
    tertiary = Color(0xFF90CAF9)
)
@Composable
fun ThingSpeakIoTTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}