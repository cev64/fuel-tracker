package com.personal.fuel.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
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
import androidx.glance.layout.RowScope
import androidx.glance.layout.Spacer
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
import com.personal.fuel.domain.model.DailyGoals
import com.personal.fuel.domain.model.DaySummary
import com.personal.fuel.domain.model.WidgetBackground
import com.personal.fuel.domain.usecase.GoalProgress
import com.personal.fuel.ui.navigation.FuelDestination
import com.personal.fuel.utilities.FuelFormat
import kotlin.math.min
import java.time.LocalDate

/**
 * Today's three macros as progress rings against their daily goals, with the
 * quick-add button.
 *
 * Built for a 4x2 cell and up, where there is room for the rings to be the point
 * rather than decoration. Everything is measured from the cell: the rings take
 * whatever diameter fits once the button and captions have their space, and the
 * figure inside each ring is sized from the longest of the three values so all
 * three match.
 */
class RingsWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val container = context.appContainer
        val summary = container.repository.getDaySummary(LocalDate.now())
        val goals = container.settingsRepository.currentGoals()
        val background = container.settingsRepository.currentWidgetBackground()

        provideContent { RingsContent(summary = summary, goals = goals, background = background) }
    }
}

class RingsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = RingsWidget()
}

private const val PADDING = 14f
private const val COLUMN_GAP = 8f

/** Width of a digit relative to the font size, for the bold face widgets use. */
private const val DIGIT_EM = 0.62f

@Composable
private fun RingsContent(
    summary: DaySummary,
    goals: DailyGoals,
    background: WidgetBackground,
) {
    val context = LocalContext.current
    val size = LocalSize.current
    val width = size.width.value
    val height = size.height.value

    val values = listOf(
        FuelFormat.number(summary.calories),
        FuelFormat.number(summary.protein),
        FuelFormat.number(summary.fiber),
    )

    // Once there is room for a full-width button underneath, the rings stop
    // competing with it for width and grow noticeably instead.
    val tall = height >= 185f

    val circleButton = if (tall) 0f else min(height * 0.28f, width * 0.15f).coerceIn(38f, 56f)
    val pillHeight = if (tall) (height * 0.20f).coerceIn(40f, 52f) else 0f
    val captionSize = (height * 0.075f).coerceIn(10f, 14f)
    val showFooter = summary.hasBurn && height >= 210f
    val footerHeight = if (showFooter) captionSize + 14f else 0f

    val ringWidth = if (tall) {
        (width - 2 * PADDING - 2 * COLUMN_GAP) / 3f
    } else {
        (width - 2 * PADDING - circleButton - 3 * COLUMN_GAP) / 3f
    }
    val ringHeight = height - 2 * PADDING - captionSize - 6f - footerHeight -
        if (tall) pillHeight + 12f else 0f
    val ring = min(ringWidth, ringHeight).coerceIn(44f, 132f)

    val stroke = (ring * 0.11f).coerceIn(5f, 12f)
    // Longest value decides the type size, so the three rings match.
    val longest = values.maxOf { it.length }
    // The ring is a fixed circle in dp while text is in sp, so a large system
    // font scale would push the figure outside its ring. Dividing it back out
    // keeps the number inside the circle at any font setting.
    val fontScale = context.resources.configuration.fontScale.coerceAtLeast(1f)
    val valueSize =
        (((ring - 2 * stroke - 8f) / (longest * DIGIT_EM)) / fontScale).coerceIn(10f, 30f)

    val rings: @Composable RowScope.() -> Unit = {
        MacroRing(
            value = values[0],
            caption = "kcal",
            progress = GoalProgress.fraction(summary.calories, goals.calories),
            color = FuelGlanceColors.calories,
            colorArgb = CALORIES_ARGB,
            background = background,
            ringSize = ring.dp,
            stroke = stroke.dp,
            valueSize = valueSize,
            captionSize = captionSize,
            modifier = GlanceModifier.defaultWeight(),
        )
        Spacer(modifier = GlanceModifier.width(COLUMN_GAP.dp))
        MacroRing(
            value = values[1],
            caption = "protein",
            progress = GoalProgress.fraction(summary.protein, goals.protein),
            color = FuelGlanceColors.protein,
            colorArgb = PROTEIN_ARGB,
            background = background,
            ringSize = ring.dp,
            stroke = stroke.dp,
            valueSize = valueSize,
            captionSize = captionSize,
            modifier = GlanceModifier.defaultWeight(),
        )
        Spacer(modifier = GlanceModifier.width(COLUMN_GAP.dp))
        MacroRing(
            value = values[2],
            caption = "fiber",
            progress = GoalProgress.fraction(summary.fiber, goals.fiber),
            color = FuelGlanceColors.fiber,
            colorArgb = FIBER_ARGB,
            background = background,
            ringSize = ring.dp,
            stroke = stroke.dp,
            valueSize = valueSize,
            captionSize = captionSize,
            modifier = GlanceModifier.defaultWeight(),
        )
    }

    FuelWidgetSurface(
        modifier = GlanceModifier.fillMaxSize(),
        contentPadding = PADDING.dp,
        background = background,
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                verticalAlignment = Alignment.Vertical.CenterVertically,
            ) {
                rings()
                if (!tall) {
                    Spacer(modifier = GlanceModifier.width(COLUMN_GAP.dp))
                    AddRingButton(size = circleButton.dp)
                }
            }

            if (showFooter) {
                Spacer(modifier = GlanceModifier.height(8.dp))
                Footer(
                    summary = summary,
                    captionSize = captionSize,
                    modifier = GlanceModifier.clickable(
                        openAppAction(context, FuelDestination.Today)
                    ),
                )
            }

            if (tall) {
                Spacer(modifier = GlanceModifier.height(12.dp))
                AddFoodPill(height = pillHeight.dp)
            }
        }
    }
}

/** Full-width add button, used where the widget is tall enough to carry one. */
@Composable
private fun AddFoodPill(height: Dp) {
    val context = LocalContext.current
    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(height)
            .background(FuelGlanceColors.accent)
            .cornerRadius(height / 2)
            .clickable(actionStartActivity(quickLogIntent(context))),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "+  Add food",
            style = TextStyle(
                color = FuelGlanceColors.onAccent,
                fontSize = (height.value * 0.34f).coerceIn(13f, 18f).sp,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}

@Composable
private fun MacroRing(
    value: String,
    caption: String,
    progress: Float,
    color: ColorProvider,
    colorArgb: Int,
    background: WidgetBackground,
    ringSize: Dp,
    stroke: Dp,
    valueSize: Float,
    captionSize: Float,
    modifier: GlanceModifier = GlanceModifier,
) {
    val context = LocalContext.current
    val bitmap = RingRenderer.ring(
        context = context,
        diameterDp = ringSize.value,
        strokeDp = stroke.value,
        progress = progress,
        color = colorArgb,
        trackColor = background.ringTrack(),
    )

    Column(
        modifier = modifier.clickable(openAppAction(context, FuelDestination.Today)),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
    ) {
        Box(
            modifier = GlanceModifier.size(ringSize),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                provider = ImageProvider(bitmap),
                contentDescription = null,
                modifier = GlanceModifier.size(ringSize),
            )
            Text(
                text = value,
                style = TextStyle(
                    color = color,
                    fontSize = valueSize.sp,
                    fontWeight = FontWeight.Bold,
                ),
                maxLines = 1,
            )
        }
        Spacer(modifier = GlanceModifier.height(6.dp))
        Text(
            text = caption,
            style = TextStyle(
                color = background.captionColor(),
                fontSize = captionSize.sp,
            ),
            maxLines = 1,
        )
    }
}

/** Burned and deficit, shown only when the cell is tall enough to earn it. */
@Composable
private fun Footer(
    summary: DaySummary,
    captionSize: Float,
    modifier: GlanceModifier = GlanceModifier,
) {
    val deficit = summary.deficit
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
    ) {
        Text(
            text = "burned ${FuelFormat.number(summary.burned)}",
            style = TextStyle(color = FuelGlanceColors.burn, fontSize = captionSize.sp),
            maxLines = 1,
        )
        Spacer(modifier = GlanceModifier.width(10.dp))
        Text(
            text = if (deficit == null) "" else "deficit ${FuelFormat.signedNumber(deficit)}",
            style = TextStyle(
                color = if (deficit != null && deficit >= 0) {
                    FuelGlanceColors.accent
                } else {
                    FuelGlanceColors.calories
                },
                fontSize = captionSize.sp,
            ),
            maxLines = 1,
        )
    }
}

@Composable
private fun AddRingButton(size: Dp) {
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

// Canvas drawing needs raw colour ints; these mirror FuelGlanceColors.
private const val CALORIES_ARGB = 0xFFFF5C5C.toInt()
private const val PROTEIN_ARGB = 0xFF5CB8FF.toInt()
private const val FIBER_ARGB = 0xFFC8F060.toInt()
