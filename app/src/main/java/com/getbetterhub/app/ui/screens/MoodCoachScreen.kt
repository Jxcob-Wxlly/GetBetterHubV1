package com.getbetterhub.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.getbetterhub.app.ui.components.HubCard
import com.getbetterhub.app.ui.components.HubFilledButton
import com.getbetterhub.app.ui.components.HubPill
import com.getbetterhub.app.ui.theme.*

data class MoodOption(val key: String, val label: String)

val moodOptions = listOf(
    MoodOption("energised", "Energised"),
    MoodOption("flat", "Flat"),
    MoodOption("anxious", "Anxious"),
    MoodOption("blocked", "Blocked"),
    MoodOption("frustrated", "Frustrated"),
    MoodOption("inspired", "Inspired")
)

@Composable
fun MoodCoachScreen(
    selectedMood: String?,
    onMoodSelected: (String) -> Unit,
    energyLevel: Int, // 1..5
    onEnergyLevelChange: (Int) -> Unit,
    onGetPlan: () -> Unit,
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
            Text(text = "Mood Coach", color = TextPrimary, style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Tell us where your head is at and we will pick the work.",
                color = TextMuted,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(20.dp))

            moodOptions.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { mood ->
                        HubPill(
                            label = mood.label,
                            active = selectedMood == mood.key,
                            onClick = { onMoodSelected(mood.key) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            HubCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(text = "Energy level: $energyLevel", color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                    Slider(
                        value = energyLevel.toFloat(),
                        onValueChange = { onEnergyLevelChange(it.toInt()) },
                        valueRange = 1f..5f,
                        steps = 3,
                        colors = SliderDefaults.colors(
                            thumbColor = AccentOrange,
                            activeTrackColor = AccentOrange,
                            inactiveTrackColor = SurfaceCardBorder
                        )
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            HubFilledButton(
                label = "Get my plan",
                fill = if (selectedMood != null) AccentOrange else AccentMutedGold,
                textColor = if (selectedMood != null) AccentOrangeOnFill else AccentMutedGoldText,
                onClick = onGetPlan,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))
        }
    }
}
