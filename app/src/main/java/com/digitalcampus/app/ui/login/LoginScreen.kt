package com.digitalcampus.app.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.digitalcampus.app.SupabaseManager
import com.digitalcampus.app.models.UserProfile
import com.digitalcampus.app.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch


@Composable
fun LoginScreen(
    role: UserRole,
    onBack: () -> Unit,
    onLoginSuccess: (UserRole) -> Unit
) {

    var emailText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),

        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {


        Text(
            text = when(role) {
                UserRole.STUDENT -> "Student Login"
                UserRole.SHOPKEEPER -> "Shopkeeper Login"
                UserRole.RIDER -> "Rider Login"
            },
            style = MaterialTheme.typography.headlineMedium
        )


        Spacer(modifier = Modifier.height(30.dp))


        OutlinedTextField(
            value = emailText,
            onValueChange = { emailText = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )


        Spacer(modifier = Modifier.height(16.dp))


        OutlinedTextField(
            value = passwordText,
            onValueChange = { passwordText = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )


        Spacer(modifier = Modifier.height(20.dp))


        Button(
            onClick = {

                scope.launch {

                    loading = true

                    try {

                        supabase.auth.signInWith(Email) {
                            email = emailText.trim()
                            password = passwordText.trim()
                        }

                        val user = supabase.auth.currentUserOrNull()
                        if (user != null) {
                            var profile = SupabaseManager.getUserProfile(user.id)
                            if (profile == null) {
                                // Create profile if it doesn't exist
                                profile = UserProfile(
                                    id = user.id,
                                    name = emailText.substringBefore("@"),
                                    email = user.email ?: emailText,
                                    role = role.name
                                )
                                SupabaseManager.createUserProfile(profile)
                                if (role == UserRole.RIDER) {
                                    SupabaseManager.updateRiderAvailability(user.id, false)
                                }
                            }

                            if (profile.role == role.name) {
                                message = "Login Successful"
                                onLoginSuccess(role)
                            } else {
                                message = "Role mismatch. Please login with correct role."
                                supabase.auth.signOut()
                            }
                        } else {
                            message = "Login Failed: User not found"
                        }

                    } catch (e: Exception) {

                        message = e.toString()

                    }

                    loading = false

                }

            },

            enabled = !loading,

            modifier = Modifier.fillMaxWidth()

        ) {

            Text(
                if (loading)
                    "Signing In..."
                else
                    "Sign In"
            )

        }


        Spacer(modifier = Modifier.height(15.dp))


        Text(message)


        TextButton(
            onClick = onBack
        ) {
            Text("Back")
        }
        
        Spacer(modifier = Modifier.height(80.dp))

    }
}
