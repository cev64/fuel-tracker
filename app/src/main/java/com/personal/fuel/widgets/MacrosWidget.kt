package com.personal.fuel.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.personal.fuel.appContainer
import com.personal.fuel.domain.model.DaySummary
import com.personal.fuel.ui.navigation.FuelDestination
import com.personal.fuel.utilities.FuelFormat
import java.time.LocalDate

/**
 * The small one: today's calories, protein and fiber, and a plus button that
 * opens the quick-log sheet. Nothing else.
 *
 * Sized for a single home-screen row. It stays legible down to roughly 2x1 by
 * dropping the captions before it drops any of the three numbers — the numbers
 * are the entire point of the widget.
 */
class MacrosWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(TINY, SMALL, WIDE)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val summary = context.appContainer.repository.getDaySummary(LocalDate.now())
        provideContent { MacrosContent(summary) }
    }

    private companion object {
        /** ~2x1: values only. */
        val TINY = DpSize(130.dp, 50.dp)

        /** ~3x1: values with captions. */
        val SMALL = DpSize(190.dp, 60.dp)

        /** ~4x1 and wider. */
        val WIDE = DpSize(260.dp, 60.dp)
    }
}

class MacrosWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MacrosWidget()
}

@Composable
private fun MacrosContent(summary: DaySummary) {
    val context = LocalContext.current
    val width = LocalSize.current.width

    val showCaptions = width >= 190.dp
    val valueSize: TextUnit = if (width >= 260.dp) 20.sp else if (showCaptions) 17.sp else 14.sp
    val buttonSize = if (width >= 260.dp) 42.dp else if (showCaptions) 38.dp else 34.dp

    FuelWidgetSurface(
        modifier = GlanceModifier.fillMaxSize(),
        contentPadding = 12.dp,
    ) {
        Row(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Vertical.CenterVertically,
        ) {
            // Tapping the numbers opens the day they belong to.
            Row(
                modifier = GlanceModifier
                    .defaultWeight()
                    .fillMaxHeight()
                    .clickable(openAppAction(context, FuelDestination.Today)),
                verticalAlignment = Alignment.Vertical.CenterVertically,
            ) {
                MacroValue(
                    value = FuelFormat.number(summary.calories),
                    caption = "kcal",
                    color = FuelGlanceColors.calories,
                    valueSize = valueSize,
                    showCaption = showCaptions,
                    modifier = GlanceModifier.defaultWeight(),
                )
                MacroValue(
                    value = FuelFormat.number(summary.protein),
                    caption = "protein",
                    color = FuelGlanceColors.protein,
                    valueSize = valueSize,
                    showCaption = showCaptions,
                    modifier = GlanceModifier.defaultWeight(),
                )
                MacroValue(
                    value = FuelFormat.number(summary.fiber),
                    caption = "fiber",
                    color = FuelGlanceColors.fiber,
                    valueSize = valueSize,
                    showCaption = showCaptions,
                    modifier = GlanceModifier.defaultWeight(),
                )
            }

            Spacer(modifier = GlanceModifier.width(10.dp))
            AddButton(context = context, size = buttonSize)
        }
    }
}

@Composable
private fun MacroValue(
    value: String,
    caption: String,
    color: ColorProvider,
    valueSize: TextUnit,
    showCaption: Boolean,
    modifier: GlanceModifier = GlanceModifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.Horizontal.Start) {
        Text(
            text = value,
            style = TextStyle(color = color, fontSize = valueSize, fontWeight = FontWeight.Bold),
            maxLines = 1,
        )
        if (showCaption) {
            Text(
                text = caption,
                style = TextStyle(color = FuelGlanceColors.textSecondary, fontSize = 10.sp),
                maxLines = 1,
            )
        }
    }
}

/** Round accent button that opens the quick-log sheet over the home screen. */
@Composable
private fun AddButton(context: Context, size: Dp) {
    Box(
        modifier = GlanceModifier
            .size(size)
            .background(FuelGlanceColors.accent)
            .cornerRadius(size / 2)
            .clickable(actionStartActivity(quickLogIntent(context))),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "+",
            style = TextStyle(
                color = FuelGlanceColors.onAccent,
                fontSize = (size.value * 0.5f).sp,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}
