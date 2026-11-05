package com.getbetterhub.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.getbetterhub.app.R
import com.getbetterhub.app.domain.Drill
import com.getbetterhub.app.domain.SessionRecord
import com.getbetterhub.app.ui.components.*
import com.getbetterhub.app.ui.theme.*

@Composable
fun categoryName(key: String): String = stringResource(
    when (key) {
        "scales" -> R.string.cat_scales
        "technique" -> R.string.cat_technique
        "rhythm" -> R.string.cat_rhythm
        "melody" -> R.string.cat_melody
        "theory" -> R.string.cat_theory
        "ear" -> R.string.cat_ear
        "creative" -> R.string.cat_creative
        else -> R.string.cat_warmup
    }
)

/**
 * Practice Roulette: spins a random drill from the instruments ticked on the profile, at the
 * profile's level. Shows a timer against the drill's suggested time, then logs exercise progress.
 */
@Composable
fun PracticeRouletteScreen(
    drill: Drill?,
    practisedInstruments: List<String>,
    primaryInstrument: String,
    timerSeconds: Int,
    isTimerRunning: Boolean,
    exercises: List<ExerciseRow>,
    recentSessions: List<SessionRecord>,
    noDrillsMessage: Boolean,
    onSpin: () -> Unit,
    onStartTimer: () -> Unit,
    onMarkComplete: () -> Unit,
    topBar: @Composable () -> Unit,
    bottomBar: @Composable () -> Unit
) {
    Scaffold(containerColor = AppBackground, topBar = topBar, bottomBar = bottomBar) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Text(text = stringResource(R.string.practice_roulette), color = TextPrimary, style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.practice_roulette_subtitle),
                color = TextMuted,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(20.dp))

            // Which instruments are in the rotation right now.
            HubCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    HubSectionLabel(stringResource(R.string.drills_in_rotation))
                    Spacer(Modifier.height(10.dp))
                    practisedInstruments.chunked(3).forEach { rowItems ->
                        Row(Modifier.padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowItems.forEach { key ->
                                SelectChip(
                                    label = instrumentName(key),
                                    selected = true,
                                    onClick = {},
                                    marker = if (key == primaryInstrument) stringResource(R.string.main_tag) else null
                                )
                            }
                        }
                    }
                    Text(stringResource(R.string.rotation_hint), color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(Modifier.height(20.dp))

            if (drill == null) {
                HubEmptyState(if (noDrillsMessage) stringResource(R.string.no_instruments) else stringResource(R.string.nothing_here_yet))
            } else {
                HubCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(
                            text = "${instrumentName(drill.instrument)} · ${levelName(drill.level)} · ${categoryName(drill.category)}".uppercase(),
                            color = AccentOrange,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(text = drill.title, color = TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(10.dp))
                        Text(text = drill.description, color = TextMuted, style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(10.dp))
                        Text(text = stringResource(R.string.suggested_min, drill.suggestedMinutes), color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(18.dp))

                        Text(text = formatDuration(timerSeconds), color = TextPrimary, style = MaterialTheme.typography.displaySmall)
                        Spacer(Modifier.height(8.dp))
                        val suggestedSeconds = drill.suggestedMinutes * 60
                        val fraction = (timerSeconds.toFloat() / suggestedSeconds).coerceIn(0f, 1f)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stringResource(R.string.pct_suggested, (fraction * 100).toInt()), color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                            Text(stringResource(R.string.time_remaining, formatDuration((suggestedSeconds - timerSeconds).coerceAtLeast(0))), color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(Modifier.height(8.dp))
                        ThinProgressBar(fraction, AccentOrange)
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            HubPrimaryButton(
                                label = if (isTimerRunning) stringResource(R.string.pause_timer) else stringResource(R.string.start_timer),
                                onClick = onStartTimer
                            )
                            HubOutlinedButton(
                                label = stringResource(R.string.mark_complete),
                                onClick = onMarkComplete,
                                borderColor = PillBorder,
                                textColor = if (timerSeconds > 0) TextPrimary else TextMuted,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            HubFilledButton(
                label = if (drill == null) stringResource(R.string.spin_the_roulette) else stringResource(R.string.spin_again),
                fill = AccentCoral,
                textColor = AccentCoralOnFill,
                onClick = onSpin,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
            HubSectionLabel(text = stringResource(R.string.exercise_progress))
            Spacer(Modifier.height(12.dp))
            if (exercises.isEmpty()) {
                Text(text = stringResource(R.string.nothing_here_yet), color = TextMuted, style = MaterialTheme.typography.bodyLarge)
            } else {
                exercises.take(6).forEach { ExerciseProgressRow(it) }
            }

            Spacer(Modifier.height(16.dp))
            HubSectionLabel(text = stringResource(R.string.recent_sessions))
            Spacer(Modifier.height(12.dp))
            if (recentSessions.isEmpty()) {
                Text(text = stringResource(R.string.nothing_here_yet), color = TextMuted, style = MaterialTheme.typography.bodyLarge)
            } else {
                recentSessions.take(8).forEach { session ->
                    HubCard(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(session.label, color = TextPrimary, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            Text(formatDuration(session.durationSeconds), color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.width(10.dp))
                            HubStatusTag(
                                label = if (session.synced) stringResource(R.string.synced) else stringResource(R.string.pending),
                                fill = if (session.synced) SyncedToastBg else PendingTagBg,
                                textColor = if (session.synced) SyncedToastText else PendingTagText
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
