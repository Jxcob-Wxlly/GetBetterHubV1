package com.getbetterhub.app.ui.screens

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.stringResource
import com.getbetterhub.app.R
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

data class MoodOption(val key: String, @androidx.annotation.StringRes val labelRes: Int)

val moodOptions = listOf(
    MoodOption("energised", R.string.mood_energised),
    MoodOption("flat", R.string.mood_flat),
    MoodOption("anxious", R.string.mood_anxious),
    MoodOption("blocked", R.string.mood_blocked),
    MoodOption("frustrated", R.string.mood_frustrated),
    MoodOption("inspired", R.string.mood_inspired)
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Text(text = stringResource(R.string.mood_coach), color = TextPrimary, style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.mood_coach_subtitle),
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
                            label = stringResource(mood.labelRes),
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
                    Text(text = stringResource(R.string.energy_level_n, energyLevel), color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
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
                label = stringResource(R.string.get_my_plan),
                fill = if (selectedMood != null) AccentOrange else AccentMutedGold,
                textColor = if (selectedMood != null) AccentOrangeOnFill else AccentMutedGoldText,
                onClick = onGetPlan,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))
        }
    }
}
