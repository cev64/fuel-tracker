package com.personal.fuel.widgets

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import kotlin.math.min

/**
 * Draws a progress ring into a bitmap.
 *
 * Glance has no canvas — a widget is `RemoteViews` underneath — so anything that
 * is not a box, a row or a piece of text has to arrive as an image. The ring is
 * rendered here and handed to `Image`.
 */
object RingRenderer {

    /**
     * Bitmaps travel to the launcher inside the RemoteViews payload, which is
     * size-limited, so rings are rendered at most this large and scaled up if
     * the display is denser. A smooth arc survives that comfortably.
     */
    private const val MAX_PX = 200

    private const val START_ANGLE = -90f

    fun ring(
        context: Context,
        diameterDp: Float,
        strokeDp: Float,
        progress: Float,
        color: Int,
        trackColor: Int,
    ): Bitmap {
        val density = context.resources.displayMetrics.density
        val px = (diameterDp * density).toInt().coerceIn(48, MAX_PX)
        // Stroke has to scale with whatever resolution the bitmap ended up at,
        // not with screen density, or a capped ring gets a too-thin line.
        val stroke = strokeDp * (px / diameterDp)

        val bitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val inset = stroke / 2f
        val bounds = RectF(inset, inset, px - inset, px - inset)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = stroke
            strokeCap = Paint.Cap.ROUND
        }

        paint.color = trackColor
        canvas.drawArc(bounds, 0f, 360f, false, paint)

        if (progress > 0f) {
            paint.color = color
            canvas.drawArc(bounds, START_ANGLE, min(progress, 1f) * 360f, false, paint)

            // Past the goal a second, lighter lap keeps going, so a big
            // overshoot does not look the same as landing exactly on target.
            if (progress > 1f) {
                paint.color = lighten(color)
                canvas.drawArc(bounds, START_ANGLE, min(progress - 1f, 1f) * 360f, false, paint)
            }
        }

        return bitmap
    }

    private fun lighten(color: Int): Int = Color.rgb(
        ((Color.red(color) + 255) / 2),
        ((Color.green(color) + 255) / 2),
        ((Color.blue(color) + 255) / 2),
    )
}
