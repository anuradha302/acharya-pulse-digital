package orders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.digitalcampus.app.SupabaseManager
import com.digitalcampus.app.models.Order
import com.digitalcampus.app.models.OrderItem
import com.digitalcampus.app.models.OrderFeedback
import com.digitalcampus.app.models.CampusShop
import com.digitalcampus.app.supabase
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun OrdersScreen(
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var riderAvailable by remember { mutableStateOf(false) }

    fun refreshOrders() {
        scope.launch {
            val user = supabase.auth.currentUserOrNull()
            if (user != null) {
                orders = SupabaseManager.getStudentOrders(user.id)
                riderAvailable = SupabaseManager.getRiderAvailability()
            }
        }
    }

    LaunchedEffect(Unit) {
        val user = supabase.auth.currentUserOrNull()
        if (user != null) {
            orders = SupabaseManager.getStudentOrders(user.id)
            riderAvailable = SupabaseManager.getRiderAvailability()
        }
        isLoading = false
    }

    Scaffold { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = paddingValues.calculateTopPadding() + 20.dp,
                bottom = paddingValues.calculateBottomPadding() + 120.dp
            ),
            verticalArrangement = Arrangement.Top
        ) {
            item {
                Text(
                    text = "📦 My Orders",
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (orders.isEmpty()) {
                item {
                    Text(
                        text = "You haven't placed any orders yet."
                    )
                }
            } else {
                items(orders, key = { it.id }) { order ->
                    OrderCard(order, riderAvailable, scope) {
                        refreshOrders()
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back to Dashboard")
                }
            }
        }
    }
}

@Composable
fun OrderCard(
    order: Order,
    riderAvailable: Boolean,
    scope: kotlinx.coroutines.CoroutineScope,
    onUpdate: () -> Unit
) {
    var items by remember { mutableStateOf<List<OrderItem>>(emptyList()) }
    var selectedPaymentType by remember { mutableStateOf<String?>(null) } // "ONLINE" or "CASH"
    var selectedOnlineOption by remember { mutableStateOf<String?>(null) } // "UPI", "CARD", etc.
    var isConfirmingPayment by remember { mutableStateOf(false) }
    var operationError by remember { mutableStateOf<String?>(null) }
    
    // Feedback state
    var showFeedbackForm by remember { mutableStateOf(false) }
    var rating by remember { mutableIntStateOf(5) }
    var feedbackText by remember { mutableStateOf("") }
    var feedbackSubmitted by remember { mutableStateOf(false) }

    LaunchedEffect(order.id) {
        items = SupabaseManager.getOrderItems(order.id)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "Order #${order.id.takeLast(6)}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = when(order.status) {
                        "PENDING" -> "🟡 Pending"
                        "ACCEPTED" -> "🟢 Shop Accepted"
                        "REJECTED" -> "❌ Rejected"
                        "PAYMENT_PENDING" -> "💳 Payment"
                        "PAID" -> "💰 Order Paid"
                        "PREPARING" -> "🍳 Preparing"
                        "READY_FOR_PICKUP" -> "✅ Ready"
                        "RIDER_ASSIGNED" -> "🛵 Assigned"
                        "PICKED_UP" -> "📦 Picked Up"
                        "ON_THE_WAY" -> "🛵 On Way"
                        "DELIVERED" -> "✅ Delivered"
                        "COLLECTED" -> "✅ Collected"
                        "COMPLETED" -> "✅ Completed"
                        else -> order.status
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = if (order.status == "REJECTED") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }

            if (operationError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = operationError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Items: ${items.joinToString(", ") { it.product_name }}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Progress tracking
            if (order.delivery_method == "PICKUP") {
                if (order.status == "PREPARING" && order.preparation_time_minutes != null) {
                    Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.small) {
                        Text(
                            "🍳 Preparing: Ready in ~${order.preparation_time_minutes} mins",
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                } else if (order.status == "READY_FOR_PICKUP") {
                    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.small) {
                        Text(
                            "✅ Order is ready! Please collect from shop.",
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            } else if (order.delivery_method == "DESK_DELIVERY") {
                if (order.payment_status == "PAID" || order.status != "PAYMENT_PENDING") {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text("Delivery Progress:", style = MaterialTheme.typography.labelLarge)
                        
                        Text(
                            text = when(order.status) {
                                "PAID" -> "🍳 Shop is preparing your order"
                                "PREPARING" -> "🍳 Shop is preparing your order"
                                "READY_FOR_PICKUP" -> "✅ Order ready - waiting for rider"
                                "RIDER_ASSIGNED" -> "🛵 Rider assigned"
                                "PICKED_UP" -> "📦 Rider picked up order"
                                "ON_THE_WAY" -> "🛵 Rider is on the way"
                                "DELIVERED" -> "✅ Delivered"
                                else -> "Status: ${order.status}"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Subtotal: ₹${order.subtotal}")

            if (order.delivery_fee > 0) {
                Text(text = "Delivery Fee: ₹${order.delivery_fee}")
            }

            Text(
                text = "Total: ₹${order.total}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            if (order.status == "REJECTED") {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Reason: ${order.rejection_reason}", color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // DELIVERY SELECTION
            if (order.status == "ACCEPTED" && order.delivery_method == null) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "Delivery Option:",
                    style = MaterialTheme.typography.titleSmall
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (riderAvailable) {
                    Button(
                        onClick = {
                            scope.launch {
                                operationError = null
                                val error = SupabaseManager.updateOrderDelivery(order.id, "DESK_DELIVERY", 20, order.subtotal + 20)
                                if (error == null) onUpdate() else operationError = error
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🛵 Desk Delivery (+₹20)")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                } else {
                    Text("🛵 Desk delivery currently unavailable (no riders online).", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            operationError = null
                            val qrToken = "QR-${order.id.takeLast(4)}-${UUID.randomUUID().toString().take(6).uppercase()}"
                            val error = SupabaseManager.updateOrderDelivery(order.id, "PICKUP", 0, order.subtotal, qrToken)
                            if (error == null) onUpdate() else operationError = error
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🏪 Shop Pickup (Free)")
                }
            }

            // PAYMENT SELECTION
            if (order.delivery_method != null && order.payment_status == "PENDING" && order.status == "PAYMENT_PENDING") {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                Text(
                    text = "Payment Method",
                    style = MaterialTheme.typography.titleSmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(selected = selectedPaymentType == "ONLINE", onClick = { selectedPaymentType = "ONLINE" })
                    Text("Online Payment")
                }

                if (selectedPaymentType == "ONLINE") {
                    Column(modifier = Modifier.padding(start = 32.dp)) {
                        listOf("UPI", "Debit Card", "Credit Card").forEach { option ->
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                RadioButton(selected = selectedOnlineOption == option, onClick = { selectedOnlineOption = option })
                                Text(option)
                            }
                        }
                    }
                }

                val cashLabel = if (order.delivery_method == "PICKUP") "Cash at Shop" else "Cash on Delivery"
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(selected = selectedPaymentType == "CASH", onClick = { selectedPaymentType = "CASH"; selectedOnlineOption = null })
                    Text(cashLabel)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        scope.launch {
                            isConfirmingPayment = true
                            operationError = null
                            if (selectedPaymentType == "ONLINE" && selectedOnlineOption != null) {
                                val error = SupabaseManager.updateOrderPayment(order.id, "PAID")
                                if (error == null) onUpdate() else operationError = error
                            } else if (selectedPaymentType == "CASH") {
                                val nextStatus = if (order.delivery_method == "PICKUP") "WAITING_FOR_PICKUP" else "PAID"
                                val error = SupabaseManager.updateOrderStatus(order.id, nextStatus)
                                if (error == null) onUpdate() else operationError = error
                            }
                            isConfirmingPayment = false
                        }
                    },
                    enabled = (selectedPaymentType != null && (selectedPaymentType != "ONLINE" || selectedOnlineOption != null)) && !isConfirmingPayment,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (selectedPaymentType == "ONLINE") "Confirm & Pay ₹${order.total}" else "Confirm Order")
                }
            }

            // PICKUP QR CODE
            if (order.delivery_method == "PICKUP" && order.pickup_qr_token != null && order.status == "READY_FOR_PICKUP") {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        Text(
                            "COLLECTION CODE",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Surface(
                            modifier = Modifier.size(140.dp),
                            color = androidx.compose.ui.graphics.Color.White,
                            border = androidx.compose.foundation.BorderStroke(2.dp, androidx.compose.ui.graphics.Color.Black)
                        ) {
                            Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                                Text(
                                    text = order.pickup_qr_token!!,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = androidx.compose.ui.graphics.Color.Black,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            "Show this code at the shop to collect.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // FEEDBACK SECTION
            if (order.status == "DELIVERED" || order.status == "COLLECTED" || order.status == "COMPLETED") {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                
                if (!feedbackSubmitted) {
                    if (!showFeedbackForm) {
                        Button(
                            onClick = { showFeedbackForm = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Rate your experience")
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Rate your experience", style = MaterialTheme.typography.titleSmall)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                (1..5).forEach { star ->
                                    IconButton(onClick = { rating = star }) {
                                        Text(if (star <= rating) "⭐" else "☆", style = MaterialTheme.typography.headlineSmall)
                                    }
                                }
                            }
                            OutlinedTextField(
                                value = feedbackText,
                                onValueChange = { feedbackText = it },
                                label = { Text("Optional feedback") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    scope.launch {
                                        val user = supabase.auth.currentUserOrNull()
                                        if (user != null) {
                                            val feedback = OrderFeedback(
                                                order_id = order.id,
                                                student_id = user.id,
                                                shop_id = order.shop_id,
                                                rating = rating,
                                                feedback_text = feedbackText
                                            )
                                            val error = SupabaseManager.submitFeedback(feedback)
                                            if (error == null) {
                                                feedbackSubmitted = true
                                            } else {
                                                operationError = error
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Submit Feedback")
                            }
                        }
                    }
                } else {
                    Text("✅ Thank you for your feedback!", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
