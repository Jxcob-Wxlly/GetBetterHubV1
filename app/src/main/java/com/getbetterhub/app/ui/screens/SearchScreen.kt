package com.getbetterhub.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.getbetterhub.app.R
import com.getbetterhub.app.domain.PersonSummary
import com.getbetterhub.app.ui.components.*
import com.getbetterhub.app.ui.theme.*

/**
 * Find other players by name or @handle, follow / unfollow them, and approve follow requests
 * to your private account. Searching needs a connection; the screen says so when offline.
 */
@Composable
fun SearchScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    results: List<PersonSummary>,
    requests: List<PersonSummary>,
    isOnline: Boolean,
    isLoading: Boolean,
    onFollow: (PersonSummary) -> Unit,
    onUnfollow: (PersonSummary) -> Unit,
    onApprove: (PersonSummary) -> Unit,
    onDecline: (PersonSummary) -> Unit,
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
            HubTextField(value = query, onValueChange = onQueryChange, placeholder = stringResource(R.string.search_hint))
            Spacer(Modifier.height(16.dp))

            if (!isOnline) {
                HubEmptyState(stringResource(R.string.search_needs_online))
            } else {
                if (requests.isNotEmpty()) {
                    HubSectionLabel(stringResource(R.string.alert_follow_requests))
                    Spacer(Modifier.height(10.dp))
                    requests.forEach { person ->
                        PersonRow(person) {
                            HubFilledButton(stringResource(R.string.approve), AccentOrange, AccentOrangeOnFill, onClick = { onApprove(person) })
                            Spacer(Modifier.width(8.dp))
                            HubOutlinedButton(stringResource(R.string.decline), onClick = { onDecline(person) }, borderColor = PillBorder, modifier = Modifier.width(96.dp))
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
                if (query.isNotBlank()) {
                    if (results.isEmpty() && !isLoading) {
                        HubEmptyState(stringResource(R.string.no_results))
                    } else {
                        results.forEach { person ->
                            PersonRow(person) {
                                when (person.relationship) {
                                    "following" -> HubOutlinedButton(stringResource(R.string.unfollow), onClick = { onUnfollow(person) }, borderColor = PillBorder, modifier = Modifier.width(120.dp))
                                    "requested" -> HubOutlinedButton(stringResource(R.string.requested), onClick = { onUnfollow(person) }, borderColor = PillBorder, textColor = TextMuted, modifier = Modifier.width(120.dp))
                                    else -> HubFilledButton(stringResource(R.string.follow), AccentOrange, AccentOrangeOnFill, onClick = { onFollow(person) })
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PersonRow(person: PersonSummary, actions: @Composable RowScope.() -> Unit) {
    HubCard(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).background(AccentOrange, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(person.displayName.take(1).uppercase(), color = AccentOrangeOnFill, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(person.displayName, color = TextPrimary, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                Text("@${person.handle} · " + stringResource(R.string.lv_short, person.level), color = TextMuted, style = MaterialTheme.typography.bodyMedium)
            }
            actions()
        }
    }
}
