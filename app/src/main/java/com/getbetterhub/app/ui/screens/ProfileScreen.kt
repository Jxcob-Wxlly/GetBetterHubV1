package com.getbetterhub.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.getbetterhub.app.R
import com.getbetterhub.app.core.rememberFileImage
import com.getbetterhub.app.domain.AchievementProgress
import com.getbetterhub.app.domain.Instruments
import com.getbetterhub.app.domain.Levels
import com.getbetterhub.app.domain.Profile
import com.getbetterhub.app.ui.components.*
import com.getbetterhub.app.ui.theme.*

/** Numbers shown on the four stat tiles and the level card. */
data class ProfileStats(
    val dayStreak: Int,
    val level: Int,
    val xp: Int,
    val xpTarget: Int,
    val sessions: Int,
    val challenges: Int
)

/** One row of "Exercise progress": a completed drill session. */
data class ExerciseRow(val title: String, val durationSeconds: Int)

@Composable
fun instrumentName(key: String): String = stringResource(
    when (key.lowercase()) {
        "vocals" -> R.string.inst_vocals
        "guitar" -> R.string.inst_guitar
        "piano" -> R.string.inst_piano
        "drums" -> R.string.inst_drums
        "bass" -> R.string.inst_bass
        "production" -> R.string.inst_production
        else -> R.string.inst_songwriting
    }
)

@Composable
fun levelName(key: String): String = stringResource(
    when (key.lowercase()) {
        "intermediate" -> R.string.lvl_intermediate
        "advanced" -> R.string.lvl_advanced
        else -> R.string.lvl_beginner
    }
)

fun formatDuration(totalSeconds: Int): String = "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)

/**
 * Profile & Settings editor (opened by tapping the G logo). Quick toggles (private account,
 * avatar style, visibility) save immediately; the text fields save with their Save buttons.
 * Everything is stored on the phone first so it works offline.
 */
@Composable
fun ProfileScreen(
    profile: Profile,
    stats: ProfileStats,
    achievements: List<AchievementProgress>,
    exercises: List<ExerciseRow>,
    message: String?,
    handleError: String?,
    onUpdate: (Profile) -> Unit,
    onSaveHandle: (String) -> Unit,
    onBannerPicked: (Uri) -> Unit,
    onAvatarPicked: (Uri) -> Unit,
    onRemovePhoto: () -> Unit,
    onViewPublicProfile: () -> Unit,
    topBar: @Composable () -> Unit,
    bottomBar: @Composable () -> Unit
) {
    var handle by remember(profile.handle) { mutableStateOf(profile.handle) }
    var displayName by remember(profile.displayName) { mutableStateOf(profile.displayName) }
    var bio by remember(profile.bio) { mutableStateOf(profile.bio) }
    var primary by remember(profile.primaryInstrument) { mutableStateOf(profile.primaryInstrument) }
    var skill by remember(profile.level) { mutableStateOf(profile.level) }
    var practised by remember(profile.instruments) { mutableStateOf(profile.instruments) }

    val bannerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> if (uri != null) onBannerPicked(uri) }
    val avatarLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> if (uri != null) onAvatarPicked(uri) }
    val bannerImage = rememberFileImage(profile.bannerPath)

    Scaffold(containerColor = AppBackground, topBar = topBar, bottomBar = bottomBar) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.profile), color = TextPrimary, style = MaterialTheme.typography.headlineLarge)
            if (message != null) {
                Spacer(Modifier.height(10.dp))
                Text(message, color = AccentGreen, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(16.dp))

            // ---- Banner + username + private account
            HubCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(0.dp)) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                            .background(Color(0xFF285CA5))
                    ) {
                        if (bannerImage != null) {
                            Image(bitmap = bannerImage, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        }
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(12.dp)
                                .background(AppBackground, RoundedCornerShape(50))
                                .clickable { bannerLauncher.launch("image/*") }
                                .padding(horizontal = 14.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.change_banner), color = TextPrimary, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Column(Modifier.padding(16.dp)) {
                        HubSectionLabel(stringResource(R.string.username))
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            HandleField(value = handle, onValueChange = { handle = it.lowercase().filter { c -> c.isLetterOrDigit() || c == '_' }.take(20) }, modifier = Modifier.weight(1f))
                            Spacer(Modifier.width(10.dp))
                            HubFilledButton(
                                label = stringResource(R.string.save),
                                fill = if (handle != profile.handle) AccentOrange else AccentMutedGold,
                                textColor = if (handle != profile.handle) AccentOrangeOnFill else AccentMutedGoldText,
                                onClick = { if (handle != profile.handle) onSaveHandle(handle) }
                            )
                        }
                        if (handleError != null) {
                            Spacer(Modifier.height(6.dp))
                            Text(handleError, color = AccentRed, style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(Modifier.height(10.dp))
                        HubOutlinedButton(
                            label = stringResource(R.string.view_my_profile),
                            onClick = onViewPublicProfile,
                            borderColor = PillBorder,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(stringResource(R.string.private_account), color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                                Text(stringResource(R.string.private_account_desc), color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                            }
                            androidx.compose.material3.Checkbox(
                                checked = profile.isPrivate,
                                onCheckedChange = { onUpdate(profile.copy(isPrivate = it)) },
                                colors = androidx.compose.material3.CheckboxDefaults.colors(checkedColor = AccentOrange, checkmarkColor = AccentOrangeOnFill, uncheckedColor = TextMuted)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---- Avatar + name + level
            HubCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        ProfileAvatar(profile, size = 96.dp, ring = true)
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(36.dp)
                                .background(AppBackground, CircleShape)
                                .border(1.dp, SurfaceCardBorder, CircleShape)
                                .clickable { avatarLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(Modifier.width(18.dp))
                    Column {
                        Text(profile.displayName, color = TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                        Text(
                            stringResource(R.string.level_n, stats.level) + " · " + stringResource(R.string.xp_progress, stats.xp, stats.xpTarget),
                            color = TextMuted,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---- Avatar style
            HubCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    HubSectionLabel(stringResource(R.string.avatar_style))
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Profile.avatarColors.forEachIndexed { index, argb ->
                            Box(
                                modifier = Modifier
                                    .padding(end = 10.dp)
                                    .size(38.dp)
                                    .then(if (index == profile.avatarColorIndex) Modifier.border(3.dp, TextPrimary, CircleShape) else Modifier)
                                    .padding(if (index == profile.avatarColorIndex) 4.dp else 0.dp)
                                    .background(Color(argb), CircleShape)
                                    .clickable { onUpdate(profile.copy(avatarColorIndex = index)) }
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        SelectChip(
                            label = stringResource(if (profile.avatarRound) R.string.shape_round else R.string.shape_square),
                            selected = false,
                            onClick = { onUpdate(profile.copy(avatarRound = !profile.avatarRound)) }
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    SelectChip(label = stringResource(R.string.remove_photo), selected = false, onClick = onRemovePhoto)
                    Spacer(Modifier.height(12.dp))
                    // Emoji picker, wraps over two rows like the mockup.
                    Profile.avatarEmojis.chunked(7).forEach { rowEmojis ->
                        Row(Modifier.padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowEmojis.forEach { emoji ->
                                val selected = profile.avatarEmoji == emoji
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .border(if (selected) 2.dp else 1.dp, if (selected) AccentOrange else PillBorder, CircleShape)
                                        .clickable { onUpdate(profile.copy(avatarEmoji = emoji)) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(if (emoji.isEmpty()) "∅" else emoji, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---- Who can see this
            HubCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    HubSectionLabel(stringResource(R.string.who_can_see))
                    Spacer(Modifier.height(6.dp))
                    Text(stringResource(R.string.who_can_see_desc), color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(10.dp))
                    VisibilityRow(stringResource(R.string.bio), profile.bioPublic) { onUpdate(profile.copy(bioPublic = it)) }
                    VisibilityRow(stringResource(R.string.instruments), profile.instrumentsPublic) { onUpdate(profile.copy(instrumentsPublic = it)) }
                    VisibilityRow(stringResource(R.string.profile_picture), profile.picturePublic) { onUpdate(profile.copy(picturePublic = it)) }
                    VisibilityRow(stringResource(R.string.earned_badges), profile.badgesPublic, last = true) { onUpdate(profile.copy(badgesPublic = it)) }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---- Stat tiles
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(stats.dayStreak.toString(), stringResource(R.string.day_streak), Modifier.weight(1f))
                StatTile(stats.level.toString(), stringResource(R.string.level), Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(stats.sessions.toString(), stringResource(R.string.sessions), Modifier.weight(1f))
                StatTile(stats.challenges.toString(), stringResource(R.string.challenges), Modifier.weight(1f))
            }

            Spacer(Modifier.height(16.dp))

            // ---- Edit details
            HubCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    HubSectionLabel(stringResource(R.string.display_name))
                    Spacer(Modifier.height(8.dp))
                    HubTextField(value = displayName, onValueChange = { displayName = it }, placeholder = "")
                    Spacer(Modifier.height(18.dp))

                    HubSectionLabel(stringResource(R.string.bio))
                    Spacer(Modifier.height(8.dp))
                    HubCard(fill = AppBackground, modifier = Modifier.fillMaxWidth().height(120.dp)) {
                        BasicTextField(
                            value = bio,
                            onValueChange = { bio = it.take(300) },
                            textStyle = TextStyle(color = TextPrimary, fontSize = MaterialTheme.typography.bodyLarge.fontSize),
                            cursorBrush = SolidColor(AccentOrange),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(Modifier.height(18.dp))

                    HubSectionLabel(stringResource(R.string.instrument_label))
                    Spacer(Modifier.height(8.dp))
                    HubDropdown(
                        valueLabel = instrumentName(primary),
                        options = Instruments.all.map { it to instrumentName(it) },
                        onSelect = {
                            primary = it
                            if (it !in practised) practised = practised + it
                        }
                    )
                    Spacer(Modifier.height(18.dp))

                    HubSectionLabel(stringResource(R.string.level_label))
                    Spacer(Modifier.height(8.dp))
                    HubDropdown(
                        valueLabel = levelName(skill),
                        options = Levels.all.map { it to levelName(it) },
                        onSelect = { skill = it }
                    )
                    Spacer(Modifier.height(18.dp))

                    HubSectionLabel(stringResource(R.string.instruments_you_practise))
                    Spacer(Modifier.height(10.dp))
                    Instruments.all.chunked(3).forEach { rowItems ->
                        Row(Modifier.padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowItems.forEach { key ->
                                val isPrimary = key == primary
                                SelectChip(
                                    label = instrumentName(key),
                                    selected = key in practised,
                                    marker = if (isPrimary) stringResource(R.string.primary_tag) else null,
                                    onClick = {
                                        if (!isPrimary) practised = if (key in practised) practised - key else practised + key
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    HubPrimaryButton(
                        label = stringResource(R.string.save),
                        onClick = {
                            onUpdate(
                                profile.copy(
                                    displayName = displayName.trim().ifBlank { profile.displayName },
                                    bio = bio.trim(),
                                    primaryInstrument = primary,
                                    level = skill,
                                    instruments = practised
                                )
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            AchievementsSection(achievements)

            Spacer(Modifier.height(12.dp))
            HubSectionLabel(stringResource(R.string.exercise_progress))
            Spacer(Modifier.height(12.dp))
            if (exercises.isEmpty()) {
                Text(stringResource(R.string.nothing_here_yet), color = TextMuted, style = MaterialTheme.typography.bodyLarge)
            } else {
                exercises.take(10).forEach { row ->
                    ExerciseProgressRow(row)
                }
            }

            Spacer(Modifier.height(24.dp))
            HubSectionLabel(stringResource(R.string.badges))
            Spacer(Modifier.height(12.dp))
            val earned = achievements.filter { it.unlocked }
            if (earned.isEmpty()) {
                HubEmptyState()
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    earned.take(8).forEach { Text(it.def.emoji, style = MaterialTheme.typography.headlineMedium) }
                }
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
fun ExerciseProgressRow(row: ExerciseRow) {
    Column(Modifier.fillMaxWidth().padding(bottom = 14.dp)) {
        Text(row.title, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.sessions_completed, 1, 1), color = TextMuted, style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.time_practised, formatDuration(row.durationSeconds)), color = TextMuted, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(6.dp))
        ThinProgressBar(1f, AccentOrange)
    }
}

@Composable
private fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    HubCard(modifier = modifier, padding = PaddingValues(vertical = 22.dp, horizontal = 12.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(value, color = AccentOrange, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))
            HubSectionLabel(label)
        }
    }
}

@Composable
private fun VisibilityRow(label: String, isPublic: Boolean, last: Boolean = false, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
        VisibilityToggle(isPublic = isPublic, onChange = onChange)
    }
    if (!last) Box(Modifier.fillMaxWidth().height(1.dp).background(SurfaceCardBorder))
}

@Composable
private fun HandleField(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(AppBackground, RoundedCornerShape(50))
            .border(1.dp, SurfaceCardBorder, RoundedCornerShape(50))
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("@", color = TextMuted, style = MaterialTheme.typography.bodyLarge)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(color = TextPrimary, fontSize = MaterialTheme.typography.bodyLarge.fontSize),
            cursorBrush = SolidColor(AccentOrange),
            modifier = Modifier.weight(1f)
        )
    }
}

/** Pill-shaped select box that opens a menu of (key, label) options. */
@Composable
fun HubDropdown(valueLabel: String, options: List<Pair<String, String>>, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppBackground, RoundedCornerShape(50))
                .border(1.dp, SurfaceCardBorder, RoundedCornerShape(50))
                .clickable { open = true }
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(valueLabel, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextMuted)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }, modifier = Modifier.background(SurfaceCard)) {
            options.forEach { (key, label) ->
                DropdownMenuItem(
                    text = { Text(label, color = TextPrimary) },
                    onClick = { onSelect(key); open = false }
                )
            }
        }
    }
}
