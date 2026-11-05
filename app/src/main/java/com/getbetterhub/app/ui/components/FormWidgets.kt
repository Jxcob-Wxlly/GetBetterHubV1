package com.getbetterhub.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.getbetterhub.app.R
import com.getbetterhub.app.core.rememberFileImage
import com.getbetterhub.app.domain.Profile
import com.getbetterhub.app.ui.theme.*

/** Label + helper text with an orange checkbox on the right (Settings and privacy toggles). */
@Composable
fun ToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(2.dp))
            Text(description, color = TextMuted, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.width(12.dp))
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = AccentOrange,
                checkmarkColor = AccentOrangeOnFill,
                uncheckedColor = TextMuted
            )
        )
    }
}

/** Two-segment Public | Private switch used in "Who can see this". */
@Composable
fun VisibilityToggle(isPublic: Boolean, onChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .border(1.dp, PillBorder, RoundedCornerShape(50))
            .padding(3.dp)
    ) {
        Segment(stringResource(R.string.visibility_public), isPublic) { onChange(true) }
        Segment(stringResource(R.string.visibility_private), !isPublic) { onChange(false) }
    }
}

@Composable
private fun Segment(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(if (selected) AccentOrange else Color.Transparent, RoundedCornerShape(50))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = if (selected) AccentOrangeOnFill else TextMuted,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/** Profile avatar: the chosen photo if there is one, otherwise a coloured shape with an emoji or initial. */
@Composable
fun ProfileAvatar(profile: Profile, size: Dp, modifier: Modifier = Modifier, ring: Boolean = false) {
    val shape = if (profile.avatarRound) CircleShape else RoundedCornerShape(size / 4)
    val image = rememberFileImage(profile.avatarPath, maxPx = 512)
    val color = Color(Profile.avatarColors.getOrElse(profile.avatarColorIndex) { Profile.avatarColors[0] })
    Box(
        modifier = modifier
            .size(size)
            .then(if (ring) Modifier.border(3.dp, AccentOrange, shape) else Modifier)
            .padding(if (ring) 5.dp else 0.dp)
            .clip(shape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        if (image != null) {
            Image(bitmap = image, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            val text = profile.avatarEmoji.ifBlank { profile.displayName.take(1).uppercase() }
            Text(text, color = AccentOrangeOnFill, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium)
        }
    }
}

/** Slim progress bar. [fraction] is 0..1. */
@Composable
fun ThinProgressBar(fraction: Float, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .background(SurfaceCardBorder, RoundedCornerShape(4.dp))
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .fillMaxHeight()
                .background(color, RoundedCornerShape(4.dp))
        )
    }
}

/** Selectable chip used for instruments and languages; [marker] adds a suffix like "· Primary". */
@Composable
fun SelectChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, marker: String? = null) {
    Box(
        modifier = modifier
            .background(if (selected) Color(0xFF3A2A1D) else Color.Transparent, RoundedCornerShape(50))
            .border(1.dp, if (selected) AccentOrange else PillBorder, RoundedCornerShape(50))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 9.dp)
    ) {
        Text(
            text = if (marker != null) "$label · $marker" else label,
            color = if (selected) AccentOrange else TextMuted,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
