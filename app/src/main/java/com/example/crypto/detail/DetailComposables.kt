package com.example.crypto.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crypto.network.RangeData
import java.lang.Exception
import java.text.DateFormat
import kotlin.random.Random

private const val OFFSET = 0

@Composable
fun Graph(d: RangeData) {
    Surface(color = Color.White) {

        var drag by remember { mutableStateOf(Offset(0f, 0f)) }

        var min = 9_999_999f
        var max = 0f
        var minimumPosition = 0
        var maximumPosition = 0
        val dataSize = d.prices.size - 1

        var position by remember { mutableStateOf(dataSize) }

        // given price value, we find min and max
        for (i in d.prices.indices) {
            val price = d.prices[i][1]
            when {
                price < min -> {
                    min = price.toFloat()
                    minimumPosition = i
                }
                price > max -> {
                    max = price.toFloat()
                    maximumPosition = i
                }
            }
        }

        Column(modifier = Modifier.aspectRatio(1f)) {
            Slider(
                x = drag.x,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Card() {
                    val v =
                        try {
                            d.prices[position][1].toFloat()
                        } catch (e: Exception) {
                            position = dataSize
                            0f
                        }
                    val d = if (d.prices.size > position) DateFormat.getDateInstance().format(d.prices[position][0]) else DateFormat.getDateInstance().format(d.prices[0][0])
                    Text(text = "$d: ${formatValue(v as Float)}")
                }
            }

            Row() {
                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .border(BorderStroke(0.5.dp, Color.Gray))
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { change, dragAmount ->
                                drag =
                                    if (change.position.x < size.width) change.position else Offset(
                                        size.width.toFloat(), 0f
                                    )
                            }
                        }
                        .pointerInput(Unit) {
                            detectTapGestures { p -> drag = p }
                        }
                ) {
                    val canvasHeight = size.height
                    val intervalSize = size.width / dataSize

                    position = (drag.x / (intervalSize)).toInt()

                    for (i in 0 until d.prices.lastIndex) {
                        drawLine(
                            start = Offset(
                                x = i * intervalSize,
                                y = transformY(d.prices[i][1].toFloat(), min, canvasHeight, max)
                            ),
                            end = Offset(
                                x = (i + 1) * intervalSize,
                                y = transformY(d.prices[i + 1][1].toFloat(), min, canvasHeight, max)
                            ),
                            color = Color.Black,
                            strokeWidth = 3F
                        )
                    }

                    drawCircle(
                        radius = 10F,
                        center = Offset(
                            x = ((drag.x / intervalSize).toInt()) * intervalSize,
                            y = (d.prices[position][1].toFloat() - min) * (canvasHeight / (max - min)) * -1 + canvasHeight
                        ),
                        color = Color.Cyan,
                    )

                    drawLine(
                        start = Offset(
                            x = drag.x,
                            y = 0F
                        ),
                        end = Offset(
                            x = drag.x,
                            y = canvasHeight
                        ),
                        color = Color.Gray,
                        strokeWidth = 1F
                    )

                }

                Column(
                    modifier = Modifier
                        .width(44.dp)
                        .fillMaxHeight()
                ) {
                    Card(elevation = 4.dp) {
                        Text(text = formatValue(max), fontSize = 8.sp)
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Card(elevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
                        Text(text = formatValue(min), fontSize = 8.sp)
                    }
                }
            }
        }
    }
}

fun formatValue(value: Float): String {
    return "\$" + ((value * 1000).toInt().toFloat() / 1000).toString()
}

/**
 * Transforms cartesian y coordinate into android y coordinate.
 * Android canvas top left corner is (0,0). If we draw without transforming it,
 * graph would be inverted.
 * [min] is our lowest point so we take min from y.
 * canvasHeight / (max - min) will normilize the graph. Then we invert it.
 * Lastly we return graph to positive range by adding canvasHeight
 */
private fun transformY(
    y: Float,
    min: Float,
    canvasHeight: Float,
    max: Float
) = (y - min) * (canvasHeight / (max - min)) * -1 + canvasHeight + OFFSET

@Preview(showBackground = true)
@Composable
fun DrawLinesPreview() {
    Graph(btcRange)
}

val btcRange = RangeData(
    prices = List(100) { listOf(0.0, Random.nextDouble()) },
    market_caps = listOf(),
    total_volumes = listOf()
)

@Composable
fun Slider(x: Float, modifier: Modifier, content: @Composable () -> Unit) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables: List<Measurable>, constraints: Constraints ->


        val placeables = measurables.map { measurable ->
            measurable.measure(constraints.copy(maxWidth = 1000, minWidth = 0))
        }

        layout(width = constraints.minWidth, height = 60) {
            placeables.forEach { p ->
                val xSafe =
                    if (x > constraints.minWidth - p.width) constraints.minWidth - p.width else if (x < 0) 0 else x
                p.place(x = xSafe.toInt(), y = 0)
            }
        }
    }
}
