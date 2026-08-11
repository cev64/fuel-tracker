package com.personal.fuel.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.material3.ripple
import com.personal.fuel.utilities.FuelHaptic
import com.personal.fuel.utilities.rememberFuelHaptics
import com.personal.fuel.ui.theme.DmSansFamily
import com.personal.fuel.ui.theme.FuelTheme

/**
 * Text field styled like the web app's inputs: filled, hairline outline, accent
 * outline on focus. Built on [BasicTextField] because the Material text fields
 * bring label/indicator chrome the Fuel design does not use.
 */
@Composable
fun FuelTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit = {},
    textAlign: TextAlign = TextAlign.Start,
    contentDescription: String? = null,
) {
    val label = contentDescription
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val borderColor by animateColorAsState(
        targetValue = if (focused) FuelTheme.colors.accentOnSurface else FuelTheme.colors.border,
        label = "fieldBorder",
    )
    val background by animateColorAsState(
        targetValue = if (focused) FuelTheme.colors.surface3 else FuelTheme.colors.surface2,
        label = "fieldBackground",
    )

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(background)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .then(
                if (label != null) Modifier.semantics { this.contentDescription = label } else Modifier
            ),
        textStyle = LocalTextStyle.current.merge(
            TextStyle(
                fontFamily = DmSansFamily,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = textAlign,
            )
        ),
        singleLine = true,
        cursorBrush = SolidColor(FuelTheme.colors.accentOnSurface),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onDone = { onImeAction() },
            onNext = { onImeAction() },
            onGo = { onImeAction() },
        ),
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                contentAlignment = if (textAlign == TextAlign.Center) {
                    Alignment.Center
                } else {
                    Alignment.CenterStart
                },
            ) {
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = FuelTheme.colors.textTertiary,
                        textAlign = textAlign,
                        modifier = if (textAlign == TextAlign.Center) Modifier.fillMaxWidth() else Modifier,
                    )
                }
                innerTextField()
            }
        },
    )
}

/** One of the three centred macro fields (cals / protein / fiber). */
@Composable
fun MacroField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        FieldLabel(text = label, centered = true, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(6.dp))
        FuelTextField(
            value = value,
            onValueChange = { onValueChange(it.filter { ch -> ch.isDigit() || ch == '.' }) },
            placeholder = "0",
            keyboardType = KeyboardType.Decimal,
            imeAction = imeAction,
            onImeAction = onImeAction,
            textAlign = TextAlign.Center,
            contentDescription = label,
        )
    }
}

/** Cals / Protein / Fiber laid out in a single row. */
@Composable
fun MacroFieldRow(
    calories: String,
    protein: String,
    fiber: String,
    onCaloriesChange: (String) -> Unit,
    onProteinChange: (String) -> Unit,
    onFiberChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onSubmit: () -> Unit = {},
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        MacroField(
            label = "Cals",
            value = calories,
            onValueChange = onCaloriesChange,
            modifier = Modifier.weight(1f),
        )
        MacroField(
            label = "Protein g",
            value = protein,
            onValueChange = onProteinChange,
            modifier = Modifier.weight(1f),
        )
        MacroField(
            label = "Fiber g",
            value = fiber,
            onValueChange = onFiberChange,
            modifier = Modifier.weight(1f),
            imeAction = ImeAction.Done,
            onImeAction = onSubmit,
        )
    }
}

/** Full-width accent button; dips slightly while held, like the web original. */
@Composable
fun FuelButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    haptic: FuelHaptic = FuelHaptic.Confirm,
) {
    val haptics = rememberFuelHaptics()
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.98f else 1f, label = "buttonScale")
    val background by animateColorAsState(
        targetValue = when {
            !enabled -> FuelTheme.colors.surface3
            pressed -> FuelTheme.colors.accentPressed
            else -> FuelTheme.colors.accent
        },
        label = "buttonBackground",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(10.dp))
            .background(background)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = FuelTheme.colors.onAccent),
                enabled = enabled,
                onClick = {
                    haptics.perform(haptic)
                    onClick()
                },
            )
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = if (enabled) FuelTheme.colors.onAccent else FuelTheme.colors.textTertiary,
        )
    }
}

/** Small circular outline button used for day/month navigation and row actions. */
@Composable
fun CircleIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    tint: Color = FuelTheme.colors.textSecondary,
    haptic: FuelHaptic = FuelHaptic.Light,
) {
    val haptics = rememberFuelHaptics()
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, FuelTheme.colors.border, CircleShape)
            .clickable {
                haptics.perform(haptic)
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(size * 0.45f),
        )
    }
}

/** Secondary, quieter button used beside [FuelButton]. */
@Composable
fun FuelTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
    haptic: FuelHaptic = FuelHaptic.Press,
) {
    val haptics = rememberFuelHaptics()
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, FuelTheme.colors.border, RoundedCornerShape(10.dp))
            .clickable {
                haptics.perform(haptic)
                onClick()
            }
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = FuelTheme.colors.textSecondary,
        )
    }
}
