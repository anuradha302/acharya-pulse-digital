package com.digitalcampus.app.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StudentDashboard(
    onLogout: () -> Unit,
    onCanteenClick: () -> Unit,
    onStationeryClick: () -> Unit,
    onMartClick: () -> Unit,
    onSanitaryClick: () -> Unit,
    onOrdersClick: () -> Unit
) {

    Scaffold { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),

            horizontalAlignment = Alignment.CenterHorizontally,

            verticalArrangement = Arrangement.Top
        ) {

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Digital Campus",
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Welcome, Student 👋"
            )

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = onCanteenClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🍔 Canteen")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onStationeryClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📚 Stationery")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onMartClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🛒 Campus Mart")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onSanitaryClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🩷 Pink Packaging")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onOrdersClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📦 My Orders")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout")
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
