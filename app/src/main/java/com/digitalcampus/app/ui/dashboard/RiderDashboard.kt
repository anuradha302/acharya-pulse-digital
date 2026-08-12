package com.digitalcampus.app.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.digitalcampus.app.SupabaseManager
import com.digitalcampus.app.models.Order
import com.digitalcampus.app.supabase
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

@Composable
fun RiderDashboard(
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var riderOnline by remember { mutableStateOf(false) }
    var availableOrders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var assignedOrders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var isLoadingOrders by remember { mutableStateOf(false) }
    var isAccepting by remember { mutableStateOf<String?>(null) } // Order ID being accepted
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun refreshDashboard() {
        scope.launch {
            val user = supabase.auth.currentUserOrNull()
            if (user != null) {
                isLoadingOrders = true
                errorMessage = null
                try {
                    val profile = SupabaseManager.getRiderProfile(user.id)
                    riderOnline = profile?.is_available ?: false
                    if (riderOnline) {
                        availableOrders = SupabaseManager.getAvailableDeliveries()
                        assignedOrders = SupabaseManager.getRiderAssignedOrders(user.id)
                    } else {
                        availableOrders = emptyList()
                        assignedOrders = emptyList()
                    }
                } catch (e: Exception) {
                    errorMessage = "Failed to load dashboard data: ${e.message}"
                } finally {
                    isLoadingOrders = false
                }
            } else {
                errorMessage = "Rider authentication not found"
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshDashboard()
    }

    Scaffold { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 24.dp,
                end = 24.dp,
                top = paddingValues.calculateTopPadding() + 24.dp,
                bottom = paddingValues.calculateBottomPadding() + 100.dp
            )
        ) {
            item {
                Text(
                    text = "🚴 Rider Dashboard",
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Manage your deliveries and update your status.")
            }

            if (errorMessage != null) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = if (riderOnline) "🟢 ONLINE" else "🔴 OFFLINE",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    val user = supabase.auth.currentUserOrNull()
                                    if (user != null) {
                                        errorMessage = null
                                        val newStatus = !riderOnline
                                        val error = SupabaseManager.updateRiderAvailability(user.id, newStatus)
                                        if (error == null) {
                                            riderOnline = newStatus
                                            refreshDashboard()
                                        } else {
                                            errorMessage = "Status update failed: $error"
                                        }
                                    } else {
                                        errorMessage = "Rider authentication not found"
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (riderOnline) "Go Offline" else "Go Online")
                        }
                    }
                }
            }

            if (isLoadingOrders) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }

            // ACTIVE DELIVERIES
            if (assignedOrders.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(text = "📦 Your Active Deliveries", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                items(assignedOrders, key = { it.id }) { order ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Order #${order.id.takeLast(6)}", style = MaterialTheme.typography.titleMedium)
                                Text(text = order.status, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                            }
                            
                            if (order.preparation_time_minutes != null) {
                                Text(text = "🍳 Shop Prep Time: ${order.preparation_time_minutes} mins", style = MaterialTheme.typography.bodySmall)
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            
                            when (order.status) {
                                "RIDER_ASSIGNED" -> {
                                    Text(
                                        text = "Waiting for shop to mark ready...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                "READY_FOR_PICKUP" -> {
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                errorMessage = SupabaseManager.updateOrderStatus(order.id, "PICKED_UP")
                                                if (errorMessage == null) refreshDashboard()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Confirm Pickup")
                                    }
                                }
                                "PICKED_UP" -> {
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                errorMessage = SupabaseManager.updateOrderStatus(order.id, "ON_THE_WAY")
                                                if (errorMessage == null) refreshDashboard()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Start Delivery")
                                    }
                                }
                                "ON_THE_WAY" -> {
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                errorMessage = SupabaseManager.updateOrderStatus(order.id, "DELIVERED")
                                                if (errorMessage == null) refreshDashboard()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Mark Delivered")
                                    }
                                }
                                "DELIVERED" -> {
                                    Text("✅ Delivered", color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }

            // AVAILABLE ORDERS
            if (riderOnline) {
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(text = "🛒 Available for Delivery", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                if (availableOrders.isEmpty() && !isLoadingOrders) {
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "No available delivery orders.",
                                modifier = Modifier.padding(16.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    items(availableOrders, key = { it.id }) { order ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "Order #${order.id.takeLast(6)}", style = MaterialTheme.typography.titleMedium)
                                Text(text = "Amount: ₹${order.total}")
                                if (order.preparation_time_minutes != null) {
                                    Text(text = "🍳 Prep: ${order.preparation_time_minutes} mins")
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                val currentlyAccepting = isAccepting == order.id
                                Button(
                                    onClick = {
                                        scope.launch {
                                            val user = supabase.auth.currentUserOrNull()
                                            if (user != null) {
                                                isAccepting = order.id
                                                errorMessage = null
                                                val error = SupabaseManager.assignRiderToOrder(order.id, user.id)
                                                if (error == null) {
                                                    refreshDashboard()
                                                } else {
                                                    errorMessage = "Unable to accept delivery: $error"
                                                }
                                                isAccepting = null
                                            } else {
                                                errorMessage = "Rider authentication not found"
                                            }
                                        }
                                    },
                                    enabled = isAccepting == null,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(if (currentlyAccepting) "Accepting..." else "Accept Delivery")
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Logout")
                }
            }
        }
    }
}
