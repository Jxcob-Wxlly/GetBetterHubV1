package com.getbetterhub.app.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.getbetterhub.app.R
import com.getbetterhub.app.ui.theme.*

enum class SyncPillState { Synced, Syncing, Offline }

/** One row in the bell dropdown. [text] is already translated by the caller. */
data class InboxItem(val id: String, val text: String, val read: Boolean)

/**
 * Persistent top bar on every signed-in screen: the "G" logo (opens Profile & Settings), search,
 * a live Synced / Syncing / Offline pill, a notifications bell with dropdown, and log out.
 */
@Composable
fun GbhTopBar(
    syncState: SyncPillState,
    unreadCount: Int,
    inbox: List<InboxItem>,
    bellOpen: Boolean,
    onProfileClick: () -> Unit,
    onSearchClick: () -> Unit,
    onBellClick: () -> Unit,
    onBellDismiss: () -> Unit,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    onSyncClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f).clickable { onProfileClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(AccentOrange, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("G", color = AccentOrangeOnFill, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = "GET\nBETTER\nHUB",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 3
            )
        }

        CircleIconButton(icon = Icons.Default.Search, description = stringResource(R.string.cd_search), onClick = onSearchClick)
        Spacer(Modifier.width(6.dp))
        SyncStatusPill(syncState, onSyncClick)
        Spacer(Modifier.width(6.dp))

        Box {
            CircleIconButton(icon = Icons.Default.Notifications, description = stringResource(R.string.cd_notifications), onClick = onBellClick)
            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(16.dp)
                        .background(AccentCoral, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                        color = AccentCoralOnFill,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            DropdownMenu(
                expanded = bellOpen,
                onDismissRequest = onBellDismiss,
                modifier = Modifier.background(SurfaceCard).widthIn(min = 260.dp, max = 320.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.notifications), color = TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = stringResource(R.string.settings),
                        color = TextMuted,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.clickable { onOpenSettings() }
                    )
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(SurfaceCardBorder))
                if (inbox.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_notifications),
                        color = TextMuted,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 28.dp, horizontal = 16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                } else {
                    inbox.take(8).forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!item.read) {
                                Box(Modifier.size(8.dp).background(AccentOrange, CircleShape))
                                Spacer(Modifier.width(8.dp))
                            }
                            Text(item.text, color = if (item.read) TextMuted else TextPrimary, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.width(6.dp))
        CircleIconButton(
            icon = Icons.Default.ExitToApp,
            description = stringResource(R.string.cd_logout),
            onClick = onLogout
        )
    }
}

@Composable
private fun CircleIconButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .border(1.dp, SurfaceCardBorder, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = description, tint = TextMuted, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun SyncStatusPill(state: SyncPillState, onClick: () -> Unit = {}) {
    val (icon, color, label) = when (state) {
        SyncPillState.Synced -> Triple(Icons.Default.Check, AccentGreen, stringResource(R.string.synced))
        SyncPillState.Syncing -> Triple(Icons.Default.Sync, PendingTagText, stringResource(R.string.syncing))
        SyncPillState.Offline -> Triple(Icons.Default.CloudOff, TextMuted, stringResource(R.string.offline_pill))
    }
    Row(
        modifier = Modifier
            .widthIn(max = 132.dp)
            .clip(RoundedCornerShape(50))
            .clickable { onClick() }
            .background(SurfaceCard, RoundedCornerShape(50))
            .border(1.dp, SurfaceCardBorder, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

enum class GbhTab(@StringRes val labelRes: Int, val route: String, val icon: ImageVector) {
    Home(R.string.nav_home, "home", Icons.Default.Home),
    Practice(R.string.nav_practice, "practice", Icons.Default.Casino),
    Mood(R.string.nav_mood, "mood", Icons.Default.Favorite),
    Lyrics(R.string.nav_lyrics, "lyrics", Icons.Default.MenuBook),
    Journal(R.string.nav_journal, "journal", Icons.Default.Mic),
    Arena(R.string.nav_arena, "arena", Icons.Default.EmojiEvents)
}

/** Bottom navigation bar with all six primary destinations. [current] is null on Profile/Settings/Search. */
@Composable
fun GbhBottomNav(
    current: GbhTab?,
    onSelect: (GbhTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppBackground)
            .border(width = 1.dp, color = SurfaceCardBorder)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        GbhTab.values().forEach { tab ->
            val active = tab == current
            val label = stringResource(tab.labelRes)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f).clickable { onSelect(tab) }
            ) {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = label,
                    tint = if (active) AccentOrange else TextMuted,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = label,
                    color = if (active) AccentOrange else TextMuted,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
