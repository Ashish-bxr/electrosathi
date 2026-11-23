package com.electrosathi.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.electrosathi.app.R
import com.electrosathi.app.data.UserProfile

/**
 * Student sign-in — Batch 6, made fully offline in Batch 14.
 *
 * Two ways in, both on-device (the PS4 rule is 100% offline — no
 * cloud backend anywhere, so there is no Google sign-in anymore):
 *  - Email + password, checked against the account store in the
 *    phone's own SQLite database, with a sign-in / create toggle.
 *    Accounts are created on this phone and stay on this phone.
 *  - Continue as guest — no account, just a local flag.
 *
 * On any successful sign-in the display name is resolved locally
 * and handed to the caller.
 */
@Composable
fun AuthScreen(
    onSignedIn: (String) -> Unit,
    onGuest: () -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var createMode by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    val completeSignIn: () -> Unit = {
        UserProfile.setGuest(context, false)
        UserProfile.upsertProfile(context) { name ->
            busy = false
            onSignedIn(name)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF0A0E1C), Color(0xFF0D1B3E)))
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(36.dp))
            Image(
                painter = painterResource(R.drawable.logo_mark),
                contentDescription = null,
                modifier = Modifier.size(84.dp).clip(RoundedCornerShape(20.dp))
            )
            Spacer(Modifier.height(20.dp))
            Text(
                "Welcome to ElectroSathi",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Sign in so ElectroSathi knows you — your name, your progress, your plan.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))

            // ---- Offline note (Batch 14) ----
            Text(
                "Your account is created on this phone and stays on this " +
                    "phone — ElectroSathi works fully offline.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))

            // ---- Email + password ----
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            error?.let {
                Spacer(Modifier.height(10.dp))
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    busy = true
                    error = null
                    val done: (Boolean, String?) -> Unit = { ok, msg ->
                        if (ok) completeSignIn()
                        else {
                            busy = false
                            error = msg
                        }
                    }
                    if (createMode) UserProfile.signUpEmail(context, email, password, done)
                    else UserProfile.signInEmail(context, email, password, done)
                },
                enabled = !busy && email.isNotBlank() && password.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    when {
                        busy -> "Please wait…"
                        createMode -> "Create account"
                        else -> "Sign in"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
            TextButton(
                onClick = {
                    createMode = !createMode
                    error = null
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (createMode) "Already have an account? Sign in"
                    else "New to ElectroSathi? Create an account"
                )
            }
            Spacer(Modifier.height(8.dp))

            // ---- Guest ----
            OutlinedButton(
                onClick = {
                    UserProfile.setGuest(context, true)
                    onGuest()
                },
                enabled = !busy,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(50)
            ) {
                Text("Continue as guest")
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "Guests can explore everything — sign in later from the side menu to make it yours.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
