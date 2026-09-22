package com.getbetterhub.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.getbetterhub.app.ui.components.*
import com.getbetterhub.app.ui.theme.*

data class Drill(
    val category: String,   // e.g. "VOCALS · BEGINNER · WARMUP"
    val title: String,      // e.g. "Lip Trill Ladder"
    val description: String,
    val suggestedMinutes: Int
)

@Composable
fun PracticeRouletteScreen(
    drill: Drill?,
    timerSeconds: Int,
    isTimerRunning: Boolean,
    recentSessions: List<String>,
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
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Text(text = "Practice Roulette", color = TextPrimary, style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(6.dp))
            Text(
                text = "A random drill matched to your instrument and level.",
                color = TextMuted,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(20.dp))

            if (drill == null) {
                HubEmptyState()
            } else {
                HubCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        HubSectionLabel(text = drill.category)
                        Spacer(Modifier.height(10.dp))
                        Text(text = drill.title, color = TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(10.dp))
                        Text(text = drill.description, color = TextMuted, style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(10.dp))
                        Text(text = "Suggested: ${drill.suggestedMinutes} min", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(18.dp))

                        val mins = timerSeconds / 60
                        val secs = timerSeconds % 60
                        Text(
                            text = "%d:%02d".format(mins, secs),
                            color = TextPrimary,
                            style = MaterialTheme.typography.headlineLarge
                        )
                        Spacer(Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            HubPrimaryButton(
                                label = if (isTimerRunning) "Pause timer" else "Start timer",
                                onClick = onStartTimer,
                                modifier = Modifier.weight(1f)
                            )
                            HubOutlinedButton(
                                label = "Mark complete",
                                onClick = onMarkComplete,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            HubFilledButton(
                label = if (drill == null) "Spin the roulette" else "Spin again",
                fill = AccentCoral,
                textColor = AccentCoralOnFill,
                onClick = onSpin,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
            HubSectionLabel(text = "Recent sessions")
            Spacer(Modifier.height(12.dp))
            if (recentSessions.isEmpty()) {
                Text(text = "Nothing here yet.", color = TextMuted, style = MaterialTheme.typography.bodyLarge)
            } else {
                recentSessions.forEach { session ->
                    Text(text = session, color = TextPrimary, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 8.dp))
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}
