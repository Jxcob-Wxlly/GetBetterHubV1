package com.getbetterhub.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.getbetterhub.app.R
import com.getbetterhub.app.domain.AchievementProgress
import com.getbetterhub.app.domain.Rarity
import com.getbetterhub.app.domain.UnitKind
import com.getbetterhub.app.ui.components.HubCard
import com.getbetterhub.app.ui.components.HubStatusTag
import com.getbetterhub.app.ui.components.ThinProgressBar
import com.getbetterhub.app.ui.theme.*

private data class RarityStyle(val tagBg: Color, val tagText: Color, val bar: Color)

private fun styleFor(rarity: Rarity) = when (rarity) {
    Rarity.Common -> RarityStyle(Color(0xFF2A2630), TextMuted, Color(0xFFA6A2B0))
    Rarity.Rare -> RarityStyle(Color(0xFF3A1F1C), Color(0xFFE0675C), Color(0xFFC0524A))
    Rarity.Epic -> RarityStyle(Color(0xFF2A2040), Color(0xFF8B66C8), Color(0xFF7B52B8))
    Rarity.Legendary -> RarityStyle(Color(0xFF3D2B1D), Color(0xFFD9962B), Color(0xFFD9962B))
}

@Composable
private fun rarityLabel(rarity: Rarity) = stringResource(
    when (rarity) {
        Rarity.Common -> R.string.rarity_common
        Rarity.Rare -> R.string.rarity_rare
        Rarity.Epic -> R.string.rarity_epic
        Rarity.Legendary -> R.string.rarity_legendary
    }
)

@Composable
private fun instrumentLabel(key: String) = stringResource(
    when (key) {
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
private fun unitLabel(p: AchievementProgress): String = when (p.def.unit) {
    UnitKind.Sessions -> stringResource(R.string.unit_sessions)
    UnitKind.Drafts -> stringResource(R.string.unit_drafts)
    UnitKind.StreakDays -> stringResource(R.string.unit_streak_days)
    UnitKind.Comments -> stringResource(R.string.unit_comments)
    UnitKind.Recordings -> stringResource(R.string.unit_recordings)
    UnitKind.Posts -> stringResource(R.string.unit_posts)
    UnitKind.Challenges -> stringResource(R.string.unit_challenges)
    UnitKind.Words -> stringResource(R.string.unit_words)
    UnitKind.Minutes -> stringResource(R.string.unit_minutes)
    UnitKind.InstrumentSessions -> stringResource(R.string.unit_instrument_sessions, instrumentLabel(p.def.instrument ?: "songwriting"))
}

/** Achievements header ("Achievements   3/18 unlocked") followed by one card per achievement. */
@Composable
fun AchievementsSection(items: List<AchievementProgress>, modifier: Modifier = Modifier) {
    Column(modifier) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.achievements).uppercase(), color = TextMuted, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.achievements_unlocked, items.count { it.unlocked }, items.size),
                color = TextMuted,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Spacer(Modifier.height(12.dp))
        items.forEach { p ->
            val style = styleFor(p.def.rarity)
            val unit = unitLabel(p)
            HubCard(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier.size(52.dp).background(Color(0xFF211A29), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (p.unlocked) Text(p.def.emoji, style = MaterialTheme.typography.headlineSmall)
                        else Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                p.def.name,
                                color = if (p.unlocked) TextPrimary else TextMuted,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(Modifier.width(8.dp))
                            HubStatusTag(rarityLabel(p.def.rarity), style.tagBg, style.tagText)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(p.def.tagline, color = TextMuted, fontStyle = FontStyle.Italic, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = if (p.unlocked) "✓ " + stringResource(R.string.ach_unlocked)
                            else stringResource(R.string.ach_locked, p.def.requirement),
                            color = TextMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            val progressText = if (p.def.xpTarget != null) {
                                stringResource(R.string.ach_progress, p.xpCurrent ?: 0, p.def.xpTarget, "XP") + " · " +
                                    stringResource(R.string.ach_progress, p.current, p.target, unit)
                            } else stringResource(R.string.ach_progress, p.current, p.target, unit)
                            Text(progressText, color = TextPrimary, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            Text("${p.percent}%", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(Modifier.height(6.dp))
                        ThinProgressBar(p.percent / 100f, style.bar)
                        if (!p.unlocked) {
                            Spacer(Modifier.height(8.dp))
                            val remaining = if (p.def.xpTarget != null) {
                                stringResource(R.string.ach_remaining, p.xpRemaining, "XP") + " · " +
                                    stringResource(R.string.ach_remaining, p.remaining, unit)
                            } else stringResource(R.string.ach_remaining, p.remaining, unit)
                            Text(remaining, color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
