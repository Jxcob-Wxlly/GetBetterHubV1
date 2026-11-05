package com.getbetterhub.app.ui.screens

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.stringResource
import com.getbetterhub.app.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.getbetterhub.app.ui.components.*
import com.getbetterhub.app.ui.theme.*

data class HomeStat(val value: String, val label: String)

/** The five feature shortcuts on Home (the Home tab itself is not one of them). */
private val jumpBackInTabs = listOf(GbhTab.Practice, GbhTab.Mood, GbhTab.Lyrics, GbhTab.Journal, GbhTab.Arena)

@Composable
fun HomeScreen(
    greeting: String,          // e.g. "Good evening"
    username: String,
    dayStreak: Int,
    level: Int,
    xp: Int,
    xpTargetForLevel: Int,
    recentSessions: List<String>,
    onJumpBackIn: (GbhTab) -> Unit,
    topBar: @Composable () -> Unit,
    bottomBar: @Composable () -> Unit
) {
    Scaffold(containerColor = AppBackground, topBar = topBar, bottomBar = bottomBar) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBackground)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Text(text = greeting, color = TextMuted, style = MaterialTheme.typography.bodyLarge)
            Text(text = username, color = TextPrimary, style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(20.dp))

            // Day streak / Level / XP stat row
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(
                    HomeStat("$dayStreak", stringResource(R.string.day_streak)),
                    HomeStat("$level", stringResource(R.string.level)),
                    HomeStat("$xp", stringResource(R.string.xp))
                ).forEach { stat ->
                    HubCard(modifier = Modifier.weight(1f)) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = stat.value, color = AccentOrange, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            HubSectionLabel(text = stat.label)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Level progress card
            HubCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = stringResource(R.string.level_n, level), color = TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(text = stringResource(R.string.xp_progress, xp, xpTargetForLevel), color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(Modifier.height(12.dp))
                    val progress = (xp.toFloat() / xpTargetForLevel.toFloat()).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(SurfaceCardBorder, RoundedCornerShape(4.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .fillMaxHeight()
                                .background(TextMuted, RoundedCornerShape(4.dp))
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            HubSectionLabel(text = stringResource(R.string.jump_back_in))
            Spacer(Modifier.height(12.dp))

            // Two-column grid of pill-shaped shortcuts, matching the reference layout (2, 2, 1).
            jumpBackInTabs.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { tab ->
                        HubCard(
                            modifier = Modifier.weight(1f),
                            onClick = { onJumpBackIn(tab) }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                androidx.compose.material3.Icon(
                                    imageVector = tab.icon,
                                    contentDescription = null,
                                    tint = AccentOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(text = stringResource(tab.labelRes), color = TextPrimary, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                    if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(12.dp))
            HubSectionLabel(text = stringResource(R.string.recent_sessions))
            Spacer(Modifier.height(12.dp))

            if (recentSessions.isEmpty()) {
                HubEmptyState()
            } else {
                recentSessions.forEach { session ->
                    HubCard(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                        Text(text = session, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}
