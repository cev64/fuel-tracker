package com.personal.fuel.widgets

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
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
import com.personal.fuel.domain.model.FoodEntry
import com.personal.fuel.domain.model.FoodTemplate
import com.personal.fuel.ui.navigation.FuelDestination
import com.personal.fuel.ui.quicklog.QuickLogActivity
import com.personal.fuel.utilities.FuelFormat
import java.time.LocalDate

/**
 * The Log screen, on the home screen.
 *
 * Android app widgets cannot host text input, so the widget does the two things
 * the Log screen is actually used for without opening the app first:
 *  - "Add food" opens a small quick-log sheet directly over the home screen;
 *  - recently logged foods are re-logged to today with a single tap.
 *
 * It resizes across three breakpoints: totals plus the add button at the small
 * size, and progressively more one-tap foods as it grows.
 */
class QuickLogWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(FuelWidgetSizes.Small, FuelWidgetSizes.Medium, FuelWidgetSizes.Large)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = context.appContainer.repository
        val today = LocalDate.now()
        val summary = repository.getDaySummary(today)
        val recents = repository.getRecentFoods(MAX_RECENTS)

        provideContent { QuickLogContent(summary = summary, recents = recents) }
    }

    private companion object {
        const val MAX_RECENTS = 5
    }
}

class QuickLogWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuickLogWidget()
}

@Composable
private fun QuickLogContent(summary: DaySummary, recents: List<FoodTemplate>) {
    val context = LocalContext.current
    val size = LocalSize.current
    val compact = size.height < FuelWidgetSizes.Medium.height
    val recentSlots = when {
        compact -> 0
        size.height < FuelWidgetSizes.Large.height -> 2
        else -> 5
    }

    FuelWidgetSurface(modifier = GlanceModifier.fillMaxSize()) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .clickable(openAppAction(context, FuelDestination.Today)),
                verticalAlignment = Alignment.Vertical.CenterVertically,
            ) {
                WidgetWordmark(fontSize = 15.sp)
                Spacer(modifier = GlanceModifier.defaultWeight())
                Text(
                    text = "${FuelFormat.number(summary.calories)} kcal",
                    style = TextStyle(
                        color = FuelGlanceColors.calories,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }

            if (!compact) {
                Spacer(modifier = GlanceModifier.height(10.dp))
                Row(modifier = GlanceModifier.fillMaxWidth()) {
                    WidgetStat(
                        value = FuelFormat.number(summary.protein),
                        label = "protein",
                        color = FuelGlanceColors.protein,
                        valueSize = 18.sp,
                        modifier = GlanceModifier.defaultWeight(),
                    )
                    WidgetStat(
                        value = FuelFormat.number(summary.fiber),
                        label = "fiber",
                        color = FuelGlanceColors.fiber,
                        valueSize = 18.sp,
                        modifier = GlanceModifier.defaultWeight(),
                    )
                    val deficit = summary.deficit
                    WidgetStat(
                        value = deficit?.let { FuelFormat.signedNumber(it) } ?: "—",
                        label = "deficit",
                        color = when {
                            deficit == null -> FuelGlanceColors.textTertiary
                            deficit >= 0 -> FuelGlanceColors.accent
                            else -> FuelGlanceColors.calories
                        },
                        valueSize = 18.sp,
                        modifier = GlanceModifier.defaultWeight(),
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(10.dp))
            AddFoodButton(context = context)

            if (recentSlots > 0 && recents.isNotEmpty()) {
                Spacer(modifier = GlanceModifier.height(10.dp))
                Text(
                    text = "QUICK ADD",
                    style = TextStyle(
                        color = FuelGlanceColors.textTertiary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                )
                Spacer(modifier = GlanceModifier.height(6.dp))
                recents.take(recentSlots).forEach { template ->
                    RecentFoodRow(template = template)
                    Spacer(modifier = GlanceModifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
private fun AddFoodButton(context: Context) {
    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(FuelGlanceColors.accent)
            .cornerRadius(12.dp)
            .padding(vertical = 10.dp)
            .clickable(
                actionStartActivity(
                    Intent(context, QuickLogActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "+  Add food",
            style = TextStyle(
                color = FuelGlanceColors.onAccent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}

@Composable
private fun RecentFoodRow(template: FoodTemplate) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(FuelGlanceColors.surface)
            .cornerRadius(10.dp)
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .clickable(
                actionRunCallback<LogRecentFoodAction>(
                    actionParametersOf(
                        LogRecentFoodAction.NAME to template.name,
                        LogRecentFoodAction.CALORIES to template.calories.toFloat(),
                        LogRecentFoodAction.PROTEIN to template.protein.toFloat(),
                        LogRecentFoodAction.FIBER to template.fiber.toFloat(),
                    )
                )
            ),
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        Text(
            text = template.name,
            style = TextStyle(color = FuelGlanceColors.textPrimary, fontSize = 13.sp),
            maxLines = 1,
            modifier = GlanceModifier.defaultWeight(),
        )
        Spacer(modifier = GlanceModifier.width(8.dp))
        Text(
            text = FuelFormat.number(template.calories),
            style = TextStyle(
                color = FuelGlanceColors.calories,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

/** Logs a previously eaten food to today straight from the widget. */
class LogRecentFoodAction : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val name = parameters[NAME] ?: return
        // The repository refreshes every widget once the row is written.
        context.appContainer.repository.addEntry(
            FoodEntry(
                date = LocalDate.now(),
                name = name,
                calories = (parameters[CALORIES] ?: 0f).toDouble(),
                protein = (parameters[PROTEIN] ?: 0f).toDouble(),
                fiber = (parameters[FIBER] ?: 0f).toDouble(),
            )
        )
    }

    companion object {
        val NAME = ActionParameters.Key<String>("name")
        val CALORIES = ActionParameters.Key<Float>("calories")
        val PROTEIN = ActionParameters.Key<Float>("protein")
        val FIBER = ActionParameters.Key<Float>("fiber")
    }
}
