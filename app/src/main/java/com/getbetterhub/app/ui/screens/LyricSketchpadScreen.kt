package com.getbetterhub.app.ui.screens

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.stringResource
import com.getbetterhub.app.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.getbetterhub.app.ui.components.*
import com.getbetterhub.app.ui.theme.*

data class LyricDraft(
    val id: String,
    val title: String,
    val content: String,
    val synced: Boolean
)

@Composable
fun LyricSketchpadScreen(
    drafts: List<LyricDraft>,
    openDraft: LyricDraft?,
    onNewDraft: () -> Unit,
    onOpenDraft: (LyricDraft) -> Unit,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: (LyricDraft) -> Unit,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(R.string.lyric_sketchpad), color = TextPrimary, style = MaterialTheme.typography.headlineLarge)
                HubFilledButton(
                    label = stringResource(R.string.new_draft),
                    fill = AccentOrange,
                    textColor = AccentOrangeOnFill,
                    onClick = onNewDraft
                )
            }
            Spacer(Modifier.height(20.dp))

            if (openDraft != null) {
                HubCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HubStatusTag(
                                label = if (openDraft.synced) stringResource(R.string.synced) else stringResource(R.string.pending),
                                fill = if (openDraft.synced) SyncedToastBg else PendingTagBg,
                                textColor = if (openDraft.synced) SyncedToastText else PendingTagText
                            )
                            Icon(
                                modifier = Modifier.size(20.dp).clickable { onDelete(openDraft) },
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete_draft),
                                tint = TextMuted
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        Text(text = stringResource(R.string.title_label), color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(6.dp))
                        HubTextField(value = openDraft.title, onValueChange = onTitleChange, placeholder = stringResource(R.string.untitled))
                        Spacer(Modifier.height(16.dp))
                        Text(text = stringResource(R.string.lyrics_label), color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(6.dp))
                        HubCard(
                            fill = AppBackground,
                            border = SurfaceCardBorder,
                            modifier = Modifier.fillMaxWidth().height(280.dp)
                        ) {
                            BasicTextField(
                                value = openDraft.content,
                                onValueChange = onContentChange,
                                textStyle = TextStyle(color = TextPrimary, fontSize = MaterialTheme.typography.bodyLarge.fontSize),
                                cursorBrush = SolidColor(AccentOrange),
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        HubOutlinedButton(label = stringResource(R.string.save), onClick = onSave)
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            if (drafts.isEmpty() && openDraft == null) {
                HubEmptyState()
            } else {
                drafts.forEach { draft ->
                    HubCard(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                        onClick = { onOpenDraft(draft) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = draft.title.ifBlank { stringResource(R.string.untitled) }, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
                            HubStatusTag(
                                label = if (draft.synced) stringResource(R.string.synced) else stringResource(R.string.pending),
                                fill = if (draft.synced) SyncedToastBg else PendingTagBg,
                                textColor = if (draft.synced) SyncedToastText else PendingTagText
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}
