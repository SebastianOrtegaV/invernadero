package cl.inacap.invernadero.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
/**
 * Grafico de lineas minimo dibujado con Canvas (sin librerias externas).
 * Muestra la serie, sus puntos y el valor minimo y maximo.
 */
@Composable
fun LineChart(
    values: List<Float>,
    label: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        if (values.size < 2) {
            Text("$label: se necesitan al menos 2 lecturas", style =
                MaterialTheme.typography.bodySmall)
            return@Column
        }
        val min = values.min()
        val max = values.max()
        Row(Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
            Text(label, style = MaterialTheme.typography.titleSmall, modifier
            = Modifier.weight(1f))
            Text(
                "min %.1f%s  max %.1f%s".format(min, unit, max, unit),
                style = MaterialTheme.typography.bodySmall
            )
        }
        val gridColor = MaterialTheme.colorScheme.outlineVariant
        Canvas(Modifier.fillMaxWidth().height(140.dp)) {
            val range = (max - min).takeIf { it > 0f } ?: 1f
            val stepX = size.width / (values.size - 1)
            fun point(i: Int) = Offset(
                x = i * stepX,
                y = size.height - ((values[i] - min) / range) * size.height
            )
            // lineas guia: arriba, centro y abajo
            listOf(0f, 0.5f, 1f).forEach { f ->
                drawLine(gridColor, Offset(0f, size.height * f),
                    Offset(size.width, size.height * f))
            }
            val path = Path().apply {
                moveTo(point(0).x, point(0).y)
                for (i in 1 until values.size) lineTo(point(i).x, point(i).y)
            }
            drawPath(path, color, style = Stroke(width = 3.dp.toPx()))
            for (i in values.indices) drawCircle(color, radius = 3.dp.toPx(),
                center = point(i))
        }
    }
}