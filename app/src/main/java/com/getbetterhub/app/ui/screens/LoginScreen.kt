package com.getbetterhub.app.ui.screens

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
fun LoginScreen(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    onSignIn: () -> Unit,
    onContinueWithGoogle: () -> Unit,
    onGoToRegister: () -> Unit
) {
    Scaffold(containerColor = AppBackground) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(40.dp))
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

            Spacer(Modifier.height(40.dp))
            Text("Sign in", color = TextPrimary, style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(28.dp))

            Text("Email", color = TextMuted, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            HubTextField(value = email, onValueChange = onEmailChange, placeholder = "")
            Spacer(Modifier.height(20.dp))

            Text("Password", color = TextMuted, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            HubTextField(value = password, onValueChange = onPasswordChange, placeholder = "", isPassword = true)
            Spacer(Modifier.height(24.dp))

            HubPrimaryButton(label = "Sign in", onClick = onSignIn, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f).height(1.dp).background(SurfaceCardBorder))
                Text(" or ", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                Box(Modifier.weight(1f).height(1.dp).background(SurfaceCardBorder))
            }
            Spacer(Modifier.height(20.dp))

            HubOutlinedButton(label = "Continue with Google", onClick = onContinueWithGoogle, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(20.dp))

            Text(
                text = "Google sign-in uses the managed OAuth provider with real backend token validation and automatic account provisioning. If the provider is not configured, this button reports the real error rather than pretending to sign you in.",
                color = TextMuted,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(28.dp))

            Text(
                text = "New here? Create an account",
                color = TextMuted,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onGoToRegister() },
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
