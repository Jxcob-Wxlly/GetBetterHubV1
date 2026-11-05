package com.getbetterhub.app.ui.screens

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.stringResource
import com.getbetterhub.app.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.getbetterhub.app.ui.components.HubOutlinedButton
import com.getbetterhub.app.ui.components.HubPrimaryButton
import com.getbetterhub.app.ui.components.HubTextField
import com.getbetterhub.app.ui.theme.*

@Composable
fun RegisterScreen(
    displayName: String,
    onDisplayNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    onCreateAccount: () -> Unit,
    onContinueWithGoogle: () -> Unit,
    onGoToSignIn: () -> Unit,
    errorMessage: String? = null,
    isLoading: Boolean = false
) {
    Scaffold(containerColor = AppBackground) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(32.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).background(AccentOrange, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("G", color = AccentOrangeOnFill, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium)
                }
                Spacer(Modifier.width(12.dp))
                Text("GET BETTER HUB", color = TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(32.dp))
            Text(stringResource(R.string.create_account), color = TextPrimary, style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(24.dp))

            Text(stringResource(R.string.display_name), color = TextMuted, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            HubTextField(value = displayName, onValueChange = onDisplayNameChange, placeholder = "")
            Spacer(Modifier.height(18.dp))

            Text(stringResource(R.string.email), color = TextMuted, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            HubTextField(value = email, onValueChange = onEmailChange, placeholder = "")
            Spacer(Modifier.height(18.dp))

            Text(stringResource(R.string.password), color = TextMuted, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            HubTextField(value = password, onValueChange = onPasswordChange, placeholder = "", isPassword = true)
            Spacer(Modifier.height(24.dp))

            HubPrimaryButton(
                label = if (isLoading) stringResource(R.string.creating_account) else stringResource(R.string.create_account),
                onClick = onCreateAccount,
                modifier = Modifier.fillMaxWidth()
            )
            if (errorMessage != null) {
                Spacer(Modifier.height(12.dp))
                Text(errorMessage, color = AccentRed, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f).height(1.dp).background(SurfaceCardBorder))
                Text(" " + stringResource(R.string.or) + " ", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                Box(Modifier.weight(1f).height(1.dp).background(SurfaceCardBorder))
            }
            Spacer(Modifier.height(20.dp))

            HubOutlinedButton(label = stringResource(R.string.continue_with_google), onClick = onContinueWithGoogle, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.google_note),
                color = TextMuted,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(28.dp))

            Text(
                text = stringResource(R.string.already_have_account),
                color = TextMuted,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onGoToSignIn() },
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
