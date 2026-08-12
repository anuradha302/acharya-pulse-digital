package com.digitalcampus.app.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RoleSelectionScreen(
    onRoleSelected: (UserRole) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Select Your Role",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        RoleCard(
            roleName = "Student",
            onClick = { onRoleSelected(UserRole.STUDENT) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        RoleCard(
            roleName = "Shopkeeper",
            onClick = { onRoleSelected(UserRole.SHOPKEEPER) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        RoleCard(
            roleName = "Rider",
            onClick = { onRoleSelected(UserRole.RIDER) }
        )
        
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun RoleCard(
    roleName: String,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = roleName,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}
