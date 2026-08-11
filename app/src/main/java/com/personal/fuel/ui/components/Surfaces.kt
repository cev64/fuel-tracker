package com.personal.fuel.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.personal.fuel.ui.theme.FuelTheme
import com.personal.fuel.ui.theme.SyneFamily

/** The rounded, hairline-outlined card every Fuel surface is built from. */
@Composable
fun FuelCard(
    modifier: Modifier = Modifier,
    borderColor: Color = FuelTheme.colors.border,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(contentPadding),
        content = content,
    )
}

/** Uppercase Syne caption used as a card heading. */
@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = FuelTheme.colors.textSecondary,
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        style = MaterialTheme.typography.titleSmall,
        color = color,
    )
}

@Composable
fun FieldLabel(text: String, modifier: Modifier = Modifier, centered: Boolean = false) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        style = MaterialTheme.typography.labelSmall,
        color = FuelTheme.colors.textTertiary,
        textAlign = if (centered) TextAlign.Center else TextAlign.Start,
    )
}

@Composable
fun EmptyState(
    icon: String,
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(text = icon, fontSize = 32.sp, color = FuelTheme.colors.textTertiary)
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = FuelTheme.colors.textTertiary,
        )
    }
}

/** The "fuel." wordmark, with the dot in the accent colour. */
@Composable
fun Wordmark(modifier: Modifier = Modifier, fontSize: TextUnit = 28.sp) {
    val tracking = (fontSize.value * -0.03f).sp
    Row(modifier = modifier, verticalAlignment = Alignment.Bottom) {
        Text(
            text = "fuel",
            fontFamily = SyneFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = fontSize,
            letterSpacing = tracking,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = ".",
            fontFamily = SyneFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = fontSize,
            letterSpacing = tracking,
            color = FuelTheme.colors.accentOnSurface,
        )
    }
}
