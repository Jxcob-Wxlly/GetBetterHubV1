package com.getbetterhub.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.getbetterhub.app.R
import com.getbetterhub.app.core.AppLanguage
import com.getbetterhub.app.ui.components.*
import com.getbetterhub.app.ui.theme.*
import java.text.DateFormat
import java.util.Date

/** Everything the Settings screen shows and changes. Language changes apply to the whole app instantly. */
data class SettingsState(
    val language: AppLanguage,
    val practiceReminders: Boolean,
    val alertNewFollowers: Boolean,
    val alertFollowRequests: Boolean,
    val alertRequestsApproved: Boolean,
    val isOnline: Boolean,
    val pendingCount: Int,
    val lastSyncMillis: Long
)

@Composable
fun SettingsScreen(
    state: SettingsState,
    message: String?,
    onLanguageChange: (AppLanguage) -> Unit,
    onRemindersChange: (Boolean) -> Unit,
    onAlertNewFollowers: (Boolean) -> Unit,
    onAlertFollowRequests: (Boolean) -> Unit,
    onAlertRequestsApproved: (Boolean) -> Unit,
    onTestNotification: () -> Unit,
    onSyncNow: () -> Unit,
    onSignOut: () -> Unit,
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
            if (message != null) {
                Spacer(Modifier.height(4.dp))
                HubCard(fill = SyncedToastBg, border = AccentGreen, modifier = Modifier.fillMaxWidth()) {
                    Text("✓  $message", color = SyncedToastText, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.settings), color = TextPrimary, style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(16.dp))

            // ---- Language
            HubCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(stringResource(R.string.language), color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))
                    AppLanguage.values().toList().chunked(3).forEach { rowItems ->
                        Row(Modifier.padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowItems.forEach { lang ->
                                HubPill(label = lang.nativeName, active = lang == state.language, onClick = { onLanguageChange(lang) })
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---- Practice reminders
            HubCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    ToggleRow(
                        title = stringResource(R.string.practice_reminders),
                        description = stringResource(R.string.practice_reminders_desc),
                        checked = state.practiceReminders,
                        onCheckedChange = onRemindersChange
                    )
                    Spacer(Modifier.height(8.dp))
                    HubOutlinedButton(
                        label = stringResource(R.string.send_test_notification),
                        onClick = onTestNotification,
                        borderColor = PillBorder,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---- Follower alerts
            HubCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(stringResource(R.string.follower_alerts), color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    ToggleRow(stringResource(R.string.alert_new_followers), stringResource(R.string.alert_new_followers_desc), state.alertNewFollowers, onAlertNewFollowers)
                    ToggleRow(stringResource(R.string.alert_follow_requests), stringResource(R.string.alert_follow_requests_desc), state.alertFollowRequests, onAlertFollowRequests)
                    ToggleRow(stringResource(R.string.alert_requests_approved), stringResource(R.string.alert_requests_approved_desc), state.alertRequestsApproved, onAlertRequestsApproved)
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---- Offline sync
            HubCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                        Text(stringResource(R.string.offline_sync), color = TextPrimary, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(if (state.isOnline) R.string.status_online else R.string.status_offline),
                            color = if (state.isOnline) AccentGreen else TextMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    val lastSync = if (state.lastSyncMillis > 0)
                        stringResource(R.string.last_sync, DateFormat.getTimeInstance().format(Date(state.lastSyncMillis)))
                    else stringResource(R.string.never_synced)
                    Text(
                        text = if (state.pendingCount > 0) stringResource(R.string.pending_changes, state.pendingCount)
                        else stringResource(R.string.all_saved_cloud) + " " + lastSync,
                        color = TextMuted,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(14.dp))
                    HubOutlinedButton(
                        label = stringResource(R.string.sync_now),
                        onClick = onSyncNow,
                        borderColor = PillBorder,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            HubOutlinedButton(
                label = stringResource(R.string.sign_out),
                onClick = onSignOut,
                borderColor = AccentRed,
                textColor = AccentRed,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(28.dp))
        }
    }
}
