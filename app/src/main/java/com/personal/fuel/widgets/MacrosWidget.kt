package com.personal.fuel.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
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
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.personal.fuel.appContainer
import com.personal.fuel.domain.model.DaySummary
import com.personal.fuel.domain.model.WidgetBackground
import com.personal.fuel.ui.navigation.FuelDestination
import com.personal.fuel.utilities.FuelFormat
import java.time.LocalDate
import kotlin.math.min

/**
 * The small one: today's calories, protein and fiber, and a plus button that
 * opens the quick-log sheet. Nothing else.
 *
 * Uses [SizeMode.Exact] rather than fixed breakpoints so the type is sized from
 * the real cell the widget was dropped into — a 4x2 slot gets numbers twice the
 * size of a 4x1 rather than the same ones with empty space around them.
 *
 * Two layouts:
 *  - short cells: the three figures in a single row beside the button;
 *  - cells two rows tall or more: calories large on their own line with the
 *    button beside them, protein and fiber underneath.
 */
class MacrosWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val container = context.appContainer
        val summary = container.repository.getDaySummary(LocalDate.now())
        val background = container.settingsRepository.currentWidgetBackground()
        provideContent { MacrosContent(summary, background) }
    }
}

class MacrosWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MacrosWidget()
}

/** Roughly how many ems a five-character bold figure such as "1,842" occupies. */
private const val FIGURE_EMS = 3.2f

private const val PADDING = 14f

@Composable
private fun MacrosContent(summary: DaySummary, background: WidgetBackground) {
    val size = LocalSize.current
    val width = size.width.value
    val height = size.height.value

    FuelWidgetSurface(
        modifier = GlanceModifier.fillMaxSize(),
        contentPadding = PADDING.dp,
        background = background,
    ) {
        if (height >= 100f) {
            StackedMacros(summary = summary, width = width, height = height)
        } else {
            RowMacros(summary = summary, width = width, height = height)
        }
    }
}

/** One row: kcal, protein, fiber, then the button. For 1-row-tall cells. */
@Composable
private fun RowMacros(summary: DaySummary, width: Float, height: Float) {
    val context = LocalContext.current

    val button = (height * 0.45f).coerceIn(34f, 56f)
    val columnWidth = (width - 2 * PADDING - button - 12f) / 3f
    val valueSize = min(columnWidth / FIGURE_EMS, height * 0.36f).coerceIn(13f, 30f)
    val showCaptions = height >= 56f && valueSize >= 15f

    Row(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
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
                valueSize = valueSize.sp,
                captionSize = (valueSize * 0.42f).coerceIn(9f, 13f).sp,
                showCaption = showCaptions,
                modifier = GlanceModifier.defaultWeight(),
            )
            MacroValue(
                value = FuelFormat.number(summary.protein),
                caption = "protein",
                color = FuelGlanceColors.protein,
                valueSize = valueSize.sp,
                captionSize = (valueSize * 0.42f).coerceIn(9f, 13f).sp,
                showCaption = showCaptions,
                modifier = GlanceModifier.defaultWeight(),
            )
            MacroValue(
                value = FuelFormat.number(summary.fiber),
                caption = "fiber",
                color = FuelGlanceColors.fiber,
                valueSize = valueSize.sp,
                captionSize = (valueSize * 0.42f).coerceIn(9f, 13f).sp,
                showCaption = showCaptions,
                modifier = GlanceModifier.defaultWeight(),
            )
        }

        Spacer(modifier = GlanceModifier.width(10.dp))
        AddButton(size = button.dp)
    }
}

/**
 * Calories large on the first line with the button beside them, protein and
 * fiber underneath. For cells two rows tall or more, where a single row of small
 * figures would leave most of the widget empty.
 */
@Composable
private fun StackedMacros(summary: DaySummary, width: Float, height: Float) {
    val context = LocalContext.current

    // Width-aware as well as height-aware, so a narrow 2x2 cell does not end up
    // mostly button.
    val button = min(height * 0.34f, width * 0.28f).coerceIn(36f, 72f)
    val calorieSize = min(
        height * 0.27f,
        (width - 2 * PADDING - button - 16f) / FIGURE_EMS,
    ).coerceIn(22f, 48f)
    val secondarySize = (calorieSize * 0.52f).coerceIn(16f, 30f)
    val captionSize = (calorieSize * 0.27f).coerceIn(10f, 15f)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(openAppAction(context, FuelDestination.Today)),
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically,
        ) {
            MacroValue(
                value = FuelFormat.number(summary.calories),
                caption = "kcal",
                color = FuelGlanceColors.calories,
                valueSize = calorieSize.sp,
                captionSize = captionSize.sp,
                showCaption = true,
                modifier = GlanceModifier.defaultWeight(),
            )
            AddButton(size = button.dp)
        }

        Spacer(modifier = GlanceModifier.defaultWeight())

        Row(modifier = GlanceModifier.fillMaxWidth()) {
            MacroValue(
                value = FuelFormat.number(summary.protein),
                caption = "protein",
                color = FuelGlanceColors.protein,
                valueSize = secondarySize.sp,
                captionSize = captionSize.sp,
                showCaption = true,
                modifier = GlanceModifier.defaultWeight(),
            )
            MacroValue(
                value = FuelFormat.number(summary.fiber),
                caption = "fiber",
                color = FuelGlanceColors.fiber,
                valueSize = secondarySize.sp,
                captionSize = captionSize.sp,
                showCaption = true,
                modifier = GlanceModifier.defaultWeight(),
            )
        }

        Spacer(modifier = GlanceModifier.height(2.dp))
    }
}

@Composable
private fun MacroValue(
    value: String,
    caption: String,
    color: ColorProvider,
    valueSize: TextUnit,
    captionSize: TextUnit,
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
                style = TextStyle(color = FuelGlanceColors.textSecondary, fontSize = captionSize),
                maxLines = 1,
            )
        }
    }
}

/** Round accent button that opens the quick-log sheet over the home screen. */
@Composable
private fun AddButton(size: Dp) {
    val context = LocalContext.current
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
