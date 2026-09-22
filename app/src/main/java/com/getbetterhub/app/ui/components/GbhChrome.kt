package com.getbetterhub.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.getbetterhub.app.ui.theme.*

/**
 * Persistent top bar: "G" logo + wordmark, a live sync-status pill, and a logout icon button.
 * Present on every authenticated screen, matching the reference build exactly.
 */
@Composable
fun GbhTopBar(
    synced: Boolean,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).background(AccentOrange, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("G", color = AccentOrangeOnFill, fontWeight = FontWeight.Bold, fontSize = MaterialTheme.typography.titleMedium.fontSize)
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = "GET BETTER HUB",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            SyncStatusPill(synced = synced)
            Spacer(Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .border(1.dp, SurfaceCardBorder, CircleShape)
                    .clickable { onLogout() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Log out",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SyncStatusPill(synced: Boolean) {
    Row(
        modifier = Modifier
            .background(SurfaceCard, RoundedCornerShape(50))
            .border(1.dp, SurfaceCardBorder, RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (synced) Icons.Default.Check else Icons.Default.Sync,
            contentDescription = null,
            tint = AccentGreen,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = if (synced) "Synced" else "Syncing…",
            color = AccentGreen,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

enum class GbhTab(val label: String, val route: String, val icon: ImageVector) {
    Home("Home", "home", Icons.Default.Home),
    Practice("Practice", "practice", Icons.Default.Casino),
    Mood("Mood", "mood", Icons.Default.Favorite),
    Lyrics("Lyrics", "lyrics", Icons.AutoMirrored.Filled.MenuBook),
    Journal("Journal", "journal", Icons.Default.Mic),
    Arena("Arena", "arena", Icons.Default.EmojiEvents)
}

/** Bottom navigation bar with all six primary destinations, matching the reference build. */
@Composable
fun GbhBottomNav(
    current: GbhTab,
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onSelect(tab) }
            ) {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = tab.label,
                    tint = if (active) AccentOrange else TextMuted,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = tab.label,
                    color = if (active) AccentOrange else TextMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
