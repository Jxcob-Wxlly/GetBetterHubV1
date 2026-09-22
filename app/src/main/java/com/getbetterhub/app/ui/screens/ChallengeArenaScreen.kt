package com.getbetterhub.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
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

enum class ArenaTab(val label: String) { Feed("Feed"), Challenges("Challenges"), Leaderboard("Leaderboard") }

data class ArenaPost(
    val author: String,
    val tags: List<String>,
    val body: String,
    val likes: Int,
    val commentCount: Int
)

data class ArenaChallenge(
    val title: String,
    val description: String,
    val instructions: String,
    val xpReward: Int
)

data class ArenaLeaderboardRow(val rank: Int, val name: String, val level: Int, val xp: Int, val isCurrentUser: Boolean)
data class ArenaBadge(val title: String, val description: String, val earned: Boolean)

@Composable
fun ChallengeArenaScreen(
    currentTab: ArenaTab,
    onTabSelected: (ArenaTab) -> Unit,
    caption: String,
    onCaptionChange: (String) -> Unit,
    selectedChallengeLabel: String,
    selectedSessionLabel: String,
    onSharePost: () -> Unit,
    posts: List<ArenaPost>,
    challenges: List<ArenaChallenge>,
    onJoinChallenge: (ArenaChallenge) -> Unit,
    leaderboard: List<ArenaLeaderboardRow>,
    badges: List<ArenaBadge>,
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
            Text(text = "Challenge Arena", color = TextPrimary, style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Join a challenge, post your evidence, and back other players.",
                color = TextMuted,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard, RoundedCornerShape(50))
                    .padding(4.dp)
            ) {
                ArenaTab.values().forEach { tab ->
                    val active = tab == currentTab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (active) AppBackground else androidx.compose.ui.graphics.Color.Transparent, RoundedCornerShape(50))
                            .clickable { onTabSelected(tab) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.label,
                            color = if (active) TextPrimary else TextMuted,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            Spacer(Modifier.height(20.dp))

            when (currentTab) {
                ArenaTab.Feed -> FeedTab(caption, onCaptionChange, selectedChallengeLabel, selectedSessionLabel, onSharePost, posts)
                ArenaTab.Challenges -> ChallengesTab(challenges, onJoinChallenge)
                ArenaTab.Leaderboard -> LeaderboardTab(leaderboard, badges)
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun FeedTab(
    caption: String,
    onCaptionChange: (String) -> Unit,
    selectedChallengeLabel: String,
    selectedSessionLabel: String,
    onSharePost: () -> Unit,
    posts: List<ArenaPost>
) {
    Column {
        HubCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(text = "Share a post", color = TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                HubCard(fill = AppBackground, border = SurfaceCardBorder, modifier = Modifier.fillMaxWidth().height(90.dp)) {
                    androidx.compose.foundation.text.BasicTextField(
                        value = caption,
                        onValueChange = onCaptionChange,
                        textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary, fontSize = MaterialTheme.typography.bodyLarge.fontSize),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(AccentOrange),
                        decorationBox = { inner ->
                            if (caption.isEmpty()) Text("Caption", color = TextMuted, style = MaterialTheme.typography.bodyLarge)
                            inner()
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(Modifier.height(14.dp))
                Text("Challenge", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(6.dp))
                DropdownRow(selectedChallengeLabel)
                Spacer(Modifier.height(14.dp))
                Text("Practice evidence", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(6.dp))
                DropdownRow(selectedSessionLabel)
                Spacer(Modifier.height(14.dp))
                HubOutlinedButton(label = "Attach audio or image", onClick = {})
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    HubFilledButton(label = "Share a post", fill = AccentOrange, textColor = AccentOrangeOnFill, onClick = onSharePost)
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        if (posts.isEmpty()) {
            HubEmptyState()
        } else {
            posts.forEach { post ->
                HubCard(modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(post.author, color = TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                            post.tags.forEach { tag ->
                                Spacer(Modifier.width(8.dp))
                                HubStatusTag(label = tag, fill = SurfaceCardBorder, textColor = TextMuted)
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(post.body, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(10.dp))
                        Text("${post.likes} likes   ${post.commentCount} comments", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun DropdownRow(label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppBackground, RoundedCornerShape(50))
            .border(1.dp, SurfaceCardBorder, RoundedCornerShape(50))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
        Text("⌄", color = TextMuted, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ChallengesTab(challenges: List<ArenaChallenge>, onJoin: (ArenaChallenge) -> Unit) {
    Column {
        challenges.forEach { challenge ->
            HubCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(challenge.title, color = TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(challenge.description, color = TextMuted, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(12.dp))
                    HubCard(fill = AppBackground, border = SurfaceCardBorder, modifier = Modifier.fillMaxWidth()) {
                        Text(challenge.instructions, color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HubStatusTag(label = "+${challenge.xpReward} XP", fill = PendingTagBg, textColor = PendingTagText)
                        HubFilledButton(
                            label = "Join challenge",
                            fill = AccentOrange,
                            textColor = AccentOrangeOnFill,
                            onClick = { onJoin(challenge) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardTab(leaderboard: List<ArenaLeaderboardRow>, badges: List<ArenaBadge>) {
    Column {
        HubCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                leaderboard.forEachIndexed { index, row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (row.isCurrentUser) PendingTagBg else androidx.compose.ui.graphics.Color.Transparent)
                            .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${row.rank}", color = TextMuted, style = MaterialTheme.typography.titleMedium, modifier = Modifier.width(28.dp))
                            Text(row.name, color = TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                        }
                        Text("Lv ${row.level} · ${row.xp} XP", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                    }
                    if (index != leaderboard.lastIndex) {
                        androidx.compose.material3.HorizontalDivider(color = SurfaceCardBorder)
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        HubSectionLabel(text = "Badges")
        Spacer(Modifier.height(12.dp))
        badges.chunked(2).forEach { rowBadges ->
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowBadges.forEach { badge ->
                    HubCard(modifier = Modifier.weight(1f)) {
                        Column {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = if (badge.earned) AccentOrange else TextMuted,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(badge.title, color = TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.height(4.dp))
                            Text(badge.description, color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                if (rowBadges.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}
