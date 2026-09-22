package com.getbetterhub.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.getbetterhub.app.ui.theme.*

/** Rounded card matching the mockups' tile/card style (e.g. "Today's session", quick-access tiles). */
@Composable
fun HubCard(
    modifier: Modifier = Modifier,
    fill: Color = SurfaceCard,
    border: Color = SurfaceCardBorder,
    padding: PaddingValues = PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(fill, RoundedCornerShape(16.dp))
            .border(1.dp, border, RoundedCornerShape(16.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(padding)
    ) {
        content()
    }
}

/** Rounded pill/chip, e.g. mood selector (Flat / Fired up / Foggy) or language selector. */
@Composable
fun HubPill(
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                if (active) AccentOrange else Color.Transparent,
                RoundedCornerShape(50)
            )
            .border(
                1.dp,
                if (active) AccentOrange else PillBorder,
                RoundedCornerShape(50)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            color = if (active) AccentOrangeOnFill else TextPrimary,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
        )
    }
}

/** Full-width primary action button, e.g. "Spin again", "Log in", "Create account". */
@Composable
fun HubPrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    HubFilledButton(label = label, fill = AccentOrange, textColor = AccentOrangeOnFill, onClick = onClick, modifier = modifier)
}

/** Full-width filled button with a caller-supplied fill/text color, e.g. coral "Spin the roulette". */
@Composable
fun HubFilledButton(
    label: String,
    fill: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Box(
        modifier = modifier
            .background(fill, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(vertical = 16.dp, horizontal = 20.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.foundation.layout.Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            if (icon != null) {
                androidx.compose.material3.Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(18.dp)
                )
                androidx.compose.foundation.layout.Spacer(Modifier.width(8.dp))
            }
            Text(
                text = label,
                color = textColor,
                fontWeight = FontWeight.Bold,
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium
            )
        }
    }
}

/** Full-width outlined button, e.g. "Start drill", "Continue with Google". */
@Composable
fun HubOutlinedButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    borderColor: Color = TextPrimary,
    textColor: Color = TextPrimary
) {
    Box(
        modifier = modifier
            .border(1.5.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontWeight = FontWeight.Bold,
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium
        )
    }
}

/** Muted, small-caps monospace section label, e.g. "TODAY'S SESSION", "THIS WEEK". */
@Composable
fun HubSectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        color = TextMuted,
        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
        modifier = modifier
    )
}

/** Dashed-border empty-state placeholder, e.g. "Nothing here yet." used across list screens. */
@Composable
fun HubEmptyState(text: String = "Nothing here yet.", modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                color = PillBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(vertical = 28.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(text = text, color = TextMuted, style = androidx.compose.material3.MaterialTheme.typography.bodyLarge)
    }
}

/** Small status tag, e.g. "SYNCED" (green) or "PENDING" (amber), used on drafts and recordings. */
@Composable
fun HubStatusTag(
    label: String,
    fill: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(fill, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(
            text = label.uppercase(),
            color = textColor,
            fontWeight = FontWeight.Bold,
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall
        )
    }
}

/** Single-line rounded text input, e.g. Email, Password, Title fields. */
@Composable
fun HubTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(AppBackground, RoundedCornerShape(50))
            .border(1.dp, SurfaceCardBorder, RoundedCornerShape(50))
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(
                color = TextPrimary,
                fontSize = androidx.compose.material3.MaterialTheme.typography.bodyLarge.fontSize
            ),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(AccentOrange),
            visualTransformation = if (isPassword) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(text = placeholder, color = TextMuted, style = androidx.compose.material3.MaterialTheme.typography.bodyLarge)
                }
                inner()
            }
        )
    }
}
