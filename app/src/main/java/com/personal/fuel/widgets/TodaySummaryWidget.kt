package com.personal.fuel.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.personal.fuel.appContainer
import com.personal.fuel.domain.model.DaySummary
import com.personal.fuel.domain.model.WidgetBackground
import com.personal.fuel.domain.model.FoodEntry
import com.personal.fuel.ui.navigation.FuelDestination
import com.personal.fuel.utilities.FuelFormat
import java.time.LocalDate

/**
 * Today's totals at a glance: calories eaten, protein, fiber, and the deficit
 * against the burn recorded for the day. Larger sizes also list what has been
 * eaten so far. Tapping anywhere opens the Today screen.
 */
class TodaySummaryWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(FuelWidgetSizes.Small, FuelWidgetSizes.Medium, FuelWidgetSizes.Large)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val container = context.appContainer
        val repository = container.repository
        val background = container.settingsRepository.currentWidgetBackground()
        val today = LocalDate.now()
        val summary = repository.getDaySummary(today)
        val entries = repository.getEntries(today)

        provideContent { TodaySummaryContent(summary, entries, background) }
    }
}

class TodaySummaryWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodaySummaryWidget()
}

@Composable
private fun TodaySummaryContent(
    summary: DaySummary,
    entries: List<FoodEntry>,
    background: WidgetBackground,
) {
    val context = LocalContext.current
    val size = LocalSize.current
    val compact = size.height < FuelWidgetSizes.Medium.height
    val itemSlots = if (size.height >= FuelWidgetSizes.Large.height) 4 else 0
    val deficit = summary.deficit

    FuelWidgetSurface(
        modifier = GlanceModifier.fillMaxSize(),
        onClick = openAppAction(context, FuelDestination.Today),
        background = background,
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.CenterVertically,
            ) {
                Text(
                    text = "TODAY",
                    style = TextStyle(
                        color = FuelGlanceColors.textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                WidgetWordmark(fontSize = 13.sp)
            }

            Spacer(modifier = GlanceModifier.height(6.dp))

            Row(verticalAlignment = Alignment.Vertical.Bottom) {
                Text(
                    text = FuelFormat.number(summary.calories),
                    style = TextStyle(
                        color = FuelGlanceColors.calories,
                        fontSize = if (compact) 26.sp else 32.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Spacer(modifier = GlanceModifier.width(5.dp))
                Text(
                    text = "kcal",
                    style = TextStyle(color = FuelGlanceColors.textSecondary, fontSize = 12.sp),
                )
            }

            Spacer(modifier = GlanceModifier.height(10.dp))

            Row(modifier = GlanceModifier.fillMaxWidth()) {
                WidgetStat(
                    value = "${FuelFormat.number(summary.protein)}g",
                    label = "protein",
                    color = FuelGlanceColors.protein,
                    valueSize = 16.sp,
                    modifier = GlanceModifier.defaultWeight(),
                )
                WidgetStat(
                    value = "${FuelFormat.number(summary.fiber)}g",
                    label = "fiber",
                    color = FuelGlanceColors.fiber,
                    valueSize = 16.sp,
                    modifier = GlanceModifier.defaultWeight(),
                )
                if (!compact) {
                    WidgetStat(
                        value = if (summary.hasBurn) FuelFormat.number(summary.burned) else "—",
                        label = "burned",
                        color = if (summary.hasBurn) {
                            FuelGlanceColors.burn
                        } else {
                            FuelGlanceColors.textTertiary
                        },
                        valueSize = 16.sp,
                        modifier = GlanceModifier.defaultWeight(),
                    )
                }
                WidgetStat(
                    value = deficit?.let { FuelFormat.signedNumber(it) } ?: "—",
                    label = "deficit",
                    color = when {
                        deficit == null -> FuelGlanceColors.textTertiary
                        deficit >= 0 -> FuelGlanceColors.accent
                        else -> FuelGlanceColors.calories
                    },
                    valueSize = 16.sp,
                    modifier = GlanceModifier.defaultWeight(),
                )
            }

            if (itemSlots > 0) {
                Spacer(modifier = GlanceModifier.height(12.dp))
                if (entries.isEmpty()) {
                    Text(
                        text = "Nothing logged yet",
                        style = TextStyle(color = FuelGlanceColors.textTertiary, fontSize = 12.sp),
                    )
                } else {
                    entries.takeLast(itemSlots).reversed().forEach { entry ->
                        LoggedItemRow(entry = entry, background = background)
                        Spacer(modifier = GlanceModifier.height(5.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun LoggedItemRow(entry: FoodEntry, background: WidgetBackground) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(background.rowSurface())
            .cornerRadius(10.dp)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        Text(
            text = entry.name,
            style = TextStyle(color = FuelGlanceColors.textPrimary, fontSize = 12.sp),
            maxLines = 1,
            modifier = GlanceModifier.defaultWeight(),
        )
        Spacer(modifier = GlanceModifier.width(8.dp))
        Text(
            text = FuelFormat.number(entry.calories),
            style = TextStyle(color = FuelGlanceColors.calories, fontSize = 12.sp),
        )
    }
}
