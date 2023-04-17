package com.dvh.clockcompose

import android.graphics.Rect
import android.graphics.Typeface
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.rotate
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CustomAnalogClock(textSize: Float, modifier: Modifier) {
    var boxSize by remember { mutableStateOf(IntSize(0, 0)) }
    var angleH by remember { mutableStateOf(0.0) }
    var angleM by remember { mutableStateOf(0.0) }
    var angleS by remember { mutableStateOf(0.0) }
    var isDay by remember { mutableStateOf(true) }


    LaunchedEffect(key1 = "tick") {
        withContext(Dispatchers.IO) {
            while (true) {
                val calendar = Calendar.getInstance()
                val h = calendar.get(Calendar.HOUR)
                val m = calendar.get(Calendar.MINUTE)
                val s = calendar.get(Calendar.SECOND)
                val millis = calendar.get(Calendar.MILLISECOND)

                val angleMillis = (360f / (60f / millis)).toDouble()
                angleS = (360f / (60f / s)).toDouble() + angleMillis / 1000f
                angleM = (360f / (60f / m)).toDouble() + angleS / 60f
                angleH = (360f / (12f / h)).toDouble() + angleM / 12f

                if (isDay != calendar.get(Calendar.HOUR_OF_DAY) in 6..18) {
                    isDay = calendar.get(Calendar.HOUR_OF_DAY) in 6..18
                }

                delay(100)
            }
        }
    }


    Box(modifier = modifier
        .fillMaxSize()
        .onSizeChanged {
            boxSize = it
        }
        .drawWithContent {
            drawCircle(
                color = Color.Black,
                radius = size.width / 2f,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = if (isDay) Color.White else Color.DarkGray,
                radius = size.width / 2.1f
            )

            drawDecorations(isDay)
            drawClockNumbers(isDay, textSize)

            drawContent()

            drawClockHands(isDay, angleS, angleM, angleH)
        }
    ) {
        DrawTodayDate(isDay, textSize, boxSize, modifier)
    }
}


@Composable
fun DrawTodayDate(
    isDay: Boolean,
    textSize: Float,
    boxSize: IntSize,
    modifier: Modifier = Modifier
) {
    val calendar = SimpleDateFormat("EEE, dd.MM.yyy", Locale.getDefault())
    var calendarTextSize by remember {
        mutableStateOf(IntSize(0, 0))
    }

    Text(
        text = calendar.format(Date()),
        fontSize = textSize.sp,
        color = if (isDay) Color.Black else Color.White,
        modifier = modifier
            .onSizeChanged {
                calendarTextSize = it
            }
            .absoluteOffset {
                IntOffset(
                    boxSize.width / 2 - calendarTextSize.width / 2,
                    boxSize.height / 2 + calendarTextSize.height / 2
                )
            }
            .background(
                color = if (isDay) Color.LightGray else Color.Black,
                shape = RoundedCornerShape(16f)
            )
            .padding(12.dp)
    )
}


val DrawScope.centerX get() = size.width / 2
val DrawScope.centerY get() = size.height / 2

fun DrawScope.drawClockNumbers(isDay: Boolean, commonTextSize: Float) {
    drawIntoCanvas {
        for (i in 0..11) {
            val x = size.width / 2.5f * cos(Math.toRadians(i * 30.0))
            val y = size.width / 2.5f * sin(Math.toRadians(i * 30.0))
            val paint = android.graphics
                .Paint()
                .apply {
                    isAntiAlias = true
                    textSize = commonTextSize.sp.toPx()
                    color =
                        if (isDay) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                    typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                }
            val rect = Rect()
            val number = if (i > 3) "${i + 9 - 12}" else "${i + 9}"
            paint.getTextBounds(
                number,
                0,
                1,
                rect
            )

            it.nativeCanvas.drawText(
                number,
                centerX - x.toFloat() - rect.width(),
                centerY - y.toFloat() + rect.height() / 2.5f,
                paint
            )
        }
    }
}

fun DrawScope.drawDecorations(isDay: Boolean) {
    drawIntoCanvas {
        for (i in 0..35) {
            if ((i * 10) % 30 != 0) {
                drawRect(
                    color = Color.Gray,
                    topLeft = Offset(centerX - 5f, centerY - centerX + 30f),
                    size = Size(7f, 27f)
                )
            }
            it.rotate(10f, centerX, centerY)
        }

        for (i in 0..11) {
            drawRect(
                color = if (isDay) Color.Black else Color.White,
                topLeft = Offset(centerX - 5f, centerY - centerX + 30f),
                size = Size(20f, 20f)
            )
            it.rotate(30f, centerX, centerY)
        }
    }
}

fun DrawScope.drawClockHands(
    isDay: Boolean,
    angleS: Double,
    angleM: Double,
    angleH: Double,
) {
    rotate(90f, Offset(centerX, centerY)) {
        drawHandHours(isDay, angleH)
        drawHandMinutes(isDay, angleM)
        drawHandSeconds(angleS)
    }
    drawCircle(
        radius = 15f,
        brush = Brush.radialGradient(
            colors = listOf(Color.White, Color.DarkGray),
            radius = 15f,
            center = Offset(centerX, centerY)
        )
    )
}

fun DrawScope.drawHandSeconds(angle: Double) {
    val xs = size.width / 2.2f * cos(Math.toRadians(angle))
    val ys = size.width / 2.2f * sin(Math.toRadians(angle))
    drawHand(xs.toFloat(), ys.toFloat(), 10f, Color.Red)
}

fun DrawScope.drawHandMinutes(isDay: Boolean, angle: Double) {
    val xm = size.width / 2.5f * cos(Math.toRadians(angle))
    val ym = size.width / 2.5f * sin(Math.toRadians(angle))
    val color = if (isDay) Color.Black else Color.White
    drawHand(xm.toFloat(), ym.toFloat(), 20f, color)
}

fun DrawScope.drawHandHours(isDay: Boolean, angle: Double) {
    val xh = size.width / 4f * cos(Math.toRadians(angle))
    val yh = size.width / 4f * sin(Math.toRadians(angle))
    val color = if (isDay) Color.Black else Color.White
    drawHand(xh.toFloat(), yh.toFloat(), 25f, color)
}

fun DrawScope.drawHand(x: Float, y: Float, strokeWidth: Float = 10f, color: Color = Color.Black) {
    drawLine(
        color = color,
        start = Offset(centerX, centerY),
        end = Offset(centerX - x, centerY - y),
        strokeWidth = strokeWidth
    )
}