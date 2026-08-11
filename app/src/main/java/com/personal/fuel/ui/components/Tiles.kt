package com.personal.fuel.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.personal.fuel.ui.theme.FuelTheme
import com.personal.fuel.utilities.FuelFormat
import com.personal.fuel.ui.theme.TileValueStyle

/**
 * A totals tile: one large accent-coloured number over a small caption. The
 * outline is a 25%-opacity version of the value colour, as in the web app.
 */
@Composable
fun TotalTile(
    value: String,
    label: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
    accented: Boolean = true,
) {
    TileFrame(
        modifier = modifier,
        borderColor = if (accented) valueColor.copy(alpha = 0.25f) else FuelTheme.colors.border,
    ) {
        Text(text = value, style = TileValueStyle, color = valueColor)
        TileCaption(label)
    }
}

/** The burn tile, whose value doubles as an inline number field. */
@Composable
fun BurnTile(
    value: String,
    onValueChange: (String) -> Unit,
    onCommit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TileFrame(
        modifier = modifier,
        borderColor = FuelTheme.colors.burn.copy(alpha = 0.25f),
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (value.isEmpty()) {
                Text(
                    text = "—",
                    style = MaterialTheme.typography.headlineSmall,
                    color = FuelTheme.colors.textTertiary,
                )
            }
            BasicTextField(
                value = value,
                onValueChange = { onValueChange(it.filter { ch -> ch.isDigit() || ch == '.' }) },
                textStyle = TileValueStyle.copy(color = FuelTheme.colors.burn),
                singleLine = true,
                cursorBrush = SolidColor(FuelTheme.colors.burn),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { onCommit() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (!it.isFocused && !it.hasFocus) onCommit() },
            )
        }
        TileCaption("burned kcal")
    }
}

/** Deficit tile: green above zero, red below, muted when no burn is recorded. */
@Composable
fun DeficitTile(
    deficit: Double?,
    modifier: Modifier = Modifier,
) {
    val color = when {
        deficit == null -> FuelTheme.colors.textTertiary
        deficit >= 0 -> FuelTheme.colors.accentOnSurface
        else -> FuelTheme.colors.calories
    }
    TotalTile(
        value = deficit?.let { FuelFormat.signedNumber(it) } ?: "—",
        label = "deficit",
        valueColor = color,
        modifier = modifier,
        accented = deficit != null,
    )
}

@Composable
private fun TileFrame(
    modifier: Modifier = Modifier,
    borderColor: Color,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        content = content,
    )
}

@Composable
private fun TileCaption(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = FuelTheme.colors.textSecondary,
        textAlign = TextAlign.Center,
    )
}

/** The three macro totals side by side. */
@Composable
fun TotalsStrip(
    calories: Double,
    protein: Double,
    fiber: Double,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TotalTile(
            value = FuelFormat.number(calories),
            label = "kcal",
            valueColor = FuelTheme.colors.calories,
            modifier = Modifier.weight(1f),
        )
        TotalTile(
            value = FuelFormat.number(protein),
            label = "protein",
            valueColor = FuelTheme.colors.protein,
            modifier = Modifier.weight(1f),
        )
        TotalTile(
            value = FuelFormat.number(fiber),
            label = "fiber",
            valueColor = FuelTheme.colors.fiber,
            modifier = Modifier.weight(1f),
        )
    }
}
