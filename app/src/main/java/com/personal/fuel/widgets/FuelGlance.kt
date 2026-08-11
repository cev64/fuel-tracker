package com.personal.fuel.widgets

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.padding
import androidx.glance.appwidget.cornerRadius
import androidx.glance.action.Action
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.compose.runtime.Composable
import androidx.glance.appwidget.action.actionStartActivity
import com.personal.fuel.MainActivity
import com.personal.fuel.ui.navigation.FuelDestination

/**
 * Widgets are always drawn in the Fuel dark palette regardless of the phone's
 * theme: they sit on the wallpaper rather than inside the app, and the dark card
 * with the lime accent is what makes them recognisable as Fuel.
 *
 * Custom fonts are not available to app widgets (they render through
 * RemoteViews), so widget text uses the system sans face at matching weights.
 */
object FuelGlanceColors {
    val background = ColorProvider(Color(0xFF141414))
    val surface = ColorProvider(Color(0xFF1F1F1F))
    val accent = ColorProvider(Color(0xFFC8F060))
    val onAccent = ColorProvider(Color(0xFF0F0F0F))
    val textPrimary = ColorProvider(Color(0xFFF0F0F0))
    val textSecondary = ColorProvider(Color(0xFF8A8A8A))
    val textTertiary = ColorProvider(Color(0xFF5A5A5A))
    val calories = ColorProvider(Color(0xFFFF5C5C))
    val protein = ColorProvider(Color(0xFF5CB8FF))
    val fiber = ColorProvider(Color(0xFFC8F060))
    val burn = ColorProvider(Color(0xFFFFAA5C))
}

object FuelWidgetSizes {
    /** Roughly 2x1 home-screen cells. */
    val Small = DpSize(160.dp, 100.dp)

    /** Roughly 4x2. */
    val Medium = DpSize(250.dp, 150.dp)

    /** Roughly 4x4 and up — the Fold's inner home screen. */
    val Large = DpSize(250.dp, 280.dp)
}

/** Opens the app on [destination]; widget taps land on the relevant screen. */
fun openAppAction(context: Context, destination: FuelDestination): Action =
    actionStartActivity(
        Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse(destination.deepLink)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
    )

/** The rounded dark panel every Fuel widget sits on. */
@Composable
fun FuelWidgetSurface(
    modifier: GlanceModifier = GlanceModifier,
    onClick: Action? = null,
    content: @Composable () -> Unit,
) {
    val base = modifier
        .background(FuelGlanceColors.background)
        .cornerRadius(24.dp)
        .padding(14.dp)
    Box(modifier = if (onClick != null) base.clickable(onClick) else base) {
        content()
    }
}

/** "fuel." in the widget, with the accent dot. */
@Composable
fun WidgetWordmark(fontSize: TextUnit) {
    Row(verticalAlignment = Alignment.Vertical.Bottom) {
        Text(
            text = "fuel",
            style = TextStyle(
                color = FuelGlanceColors.textPrimary,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
            ),
        )
        Text(
            text = ".",
            style = TextStyle(
                color = FuelGlanceColors.accent,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}

/** One macro value with its caption, used across both widgets. */
@Composable
fun WidgetStat(
    value: String,
    label: String,
    color: ColorProvider,
    valueSize: TextUnit,
    modifier: GlanceModifier = GlanceModifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.Horizontal.Start) {
        Text(
            text = value,
            style = TextStyle(color = color, fontSize = valueSize, fontWeight = FontWeight.Bold),
            maxLines = 1,
        )
        Text(
            text = label,
            style = TextStyle(color = FuelGlanceColors.textSecondary, fontSize = 11.sp),
            maxLines = 1,
        )
    }
}
