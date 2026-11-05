package com.getbetterhub.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.getbetterhub.app.R
import com.getbetterhub.app.core.rememberFileImage
import com.getbetterhub.app.domain.Profile
import com.getbetterhub.app.ui.components.*
import com.getbetterhub.app.ui.theme.*

/** How other people see you ("View my profile"): banner, avatar, handle, instruments, counts, shared posts. */
@Composable
fun PublicProfileScreen(
    profile: Profile,
    level: Int,
    posts: List<ArenaPost>,
    badgeEmojis: List<String>,
    onEditProfile: () -> Unit,
    topBar: @Composable () -> Unit,
    bottomBar: @Composable () -> Unit
) {
    val banner = rememberFileImage(profile.bannerPath)
    Scaffold(containerColor = AppBackground, topBar = topBar, bottomBar = bottomBar) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            HubCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(0.dp)) {
                Column {
                    Box(Modifier.fillMaxWidth().height(170.dp)) {
                        Box(Modifier.fillMaxSize().background(Color(0xFF285CA5)))
                        if (banner != null) {
                            Image(bitmap = banner, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        }
                    }
                    Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        Box(
                            Modifier
                                .offset(y = (-48).dp)
                                .background(SurfaceCard, CircleShape)
                                .padding(5.dp)
                        ) {
                            ProfileAvatar(profile, size = 100.dp)
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 12.dp)
                                .border(1.dp, PillBorder, RoundedCornerShape(50))
                                .clickable { onEditProfile() }
                                .padding(horizontal = 22.dp, vertical = 12.dp)
                        ) {
                            Text(stringResource(R.string.edit_profile), color = TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp).offset(y = (-28).dp)) {
                        Text(profile.displayName, color = TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium)
                        Text("@${profile.handle}", color = TextMuted, style = MaterialTheme.typography.bodyLarge)
                        if (profile.bio.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(profile.bio, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            profile.instruments.take(3).forEach { key ->
                                Box(
                                    Modifier
                                        .background(Color(0xFF3A2A1D), RoundedCornerShape(50))
                                        .padding(horizontal = 14.dp, vertical = 7.dp)
                                ) {
                                    Text(instrumentName(key), color = AccentOrange, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.CenterVertically) {
                            Count(posts.size, stringResource(R.string.posts))
                            Count(profile.followers, stringResource(R.string.followers))
                            Count(profile.following, stringResource(R.string.following))
                            Text(stringResource(R.string.lv_short, level), color = TextMuted, style = MaterialTheme.typography.bodyLarge)
                        }
                        if (badgeEmojis.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                badgeEmojis.take(8).forEach { Text(it, style = MaterialTheme.typography.titleLarge) }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            HubSectionLabel(stringResource(R.string.shared_posts))
            Spacer(Modifier.height(12.dp))
            if (posts.isEmpty()) {
                HubEmptyState(stringResource(R.string.no_posts_yet))
            } else {
                posts.forEach { post ->
                    HubCard(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                        Column {
                            Text(post.body, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.height(6.dp))
                            Text(stringResource(R.string.likes_comments, post.likes, post.commentCount), color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Count(value: Int, label: String) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(value.toString(), color = TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.width(5.dp))
        Text(label, color = TextMuted, style = MaterialTheme.typography.bodyLarge)
    }
}
