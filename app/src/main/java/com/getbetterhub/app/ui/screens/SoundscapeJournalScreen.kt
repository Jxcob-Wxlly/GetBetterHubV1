package com.getbetterhub.app.ui.screens

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.stringResource
import com.getbetterhub.app.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.getbetterhub.app.ui.components.*
import com.getbetterhub.app.ui.theme.*

data class JournalTake(
    val id: String,
    val title: String,
    val synced: Boolean, // false = "Pending"
    val positionSeconds: Int,
    val durationSeconds: Int,
    val isPlaying: Boolean,
    val localFilePath: String? = null, // where the recorded clip lives on-device; null if missing (e.g. reinstalled app)
    val audioUrl: String? = null // set once the clip has been uploaded and synced to the server
)

@Composable
fun SoundscapeJournalScreen(
    titleDraft: String,
    onTitleChange: (String) -> Unit,
    timerSeconds: Int,
    isRecording: Boolean,
    onToggleRecord: () -> Unit,
    takes: List<JournalTake>,
    onTogglePlay: (JournalTake) -> Unit,
    onDelete: (JournalTake) -> Unit,
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
            Text(text = stringResource(R.string.soundscape_journal), color = TextPrimary, style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.soundscape_journal_subtitle),
                color = TextMuted,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(20.dp))

            HubCard(modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    HubTextField(value = titleDraft, onValueChange = onTitleChange, placeholder = stringResource(R.string.title_hint))
                    Spacer(Modifier.height(20.dp))
                    val mins = timerSeconds / 60
                    val secs = timerSeconds % 60
                    Text(
                        text = "%d:%02d".format(mins, secs),
                        color = TextPrimary,
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Spacer(Modifier.height(16.dp))
                    HubFilledButton(
                        label = if (isRecording) stringResource(R.string.stop) else stringResource(R.string.record),
                        fill = if (isRecording) AccentCoral else AccentOrange,
                        textColor = if (isRecording) AccentCoralOnFill else AccentOrangeOnFill,
                        icon = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        onClick = onToggleRecord
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            if (takes.isEmpty()) {
                HubEmptyState()
            } else {
                takes.forEach { take ->
                    HubCard(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = take.title, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    HubStatusTag(
                                        label = if (take.synced) stringResource(R.string.synced) else stringResource(R.string.pending),
                                        fill = if (take.synced) SyncedToastBg else PendingTagBg,
                                        textColor = if (take.synced) SyncedToastText else PendingTagText
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = stringResource(R.string.delete),
                                        tint = TextMuted,
                                        modifier = Modifier.size(18.dp).clickable { onDelete(take) }
                                    )
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (take.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = stringResource(R.string.play),
                                    tint = TextPrimary,
                                    modifier = Modifier.size(20.dp).clickable { onTogglePlay(take) }
                                )
                                Spacer(Modifier.width(8.dp))
                                val posMin = take.positionSeconds / 60
                                val posSec = take.positionSeconds % 60
                                val durMin = take.durationSeconds / 60
                                val durSec = take.durationSeconds % 60
                                Text(
                                    text = "%d:%02d / %d:%02d".format(posMin, posSec, durMin, durSec),
                                    color = TextMuted,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(Modifier.width(10.dp))
                                Slider(
                                    value = if (take.durationSeconds > 0) take.positionSeconds.toFloat() / take.durationSeconds else 0f,
                                    onValueChange = {},
                                    modifier = Modifier.weight(1f),
                                    colors = SliderDefaults.colors(
                                        thumbColor = TextPrimary,
                                        activeTrackColor = TextPrimary,
                                        inactiveTrackColor = ScrubberTrack
                                    )
                                )
                                Spacer(Modifier.width(8.dp))
                                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Icon(Icons.Default.MoreVert, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}
