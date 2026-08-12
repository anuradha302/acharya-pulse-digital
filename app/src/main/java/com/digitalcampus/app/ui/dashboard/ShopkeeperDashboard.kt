package com.digitalcampus.app.ui.dashboard

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.digitalcampus.app.SupabaseManager
import com.digitalcampus.app.models.Order
import com.digitalcampus.app.models.OrderItem
import com.digitalcampus.app.models.Product
import com.digitalcampus.app.models.CampusShop
import com.digitalcampus.app.supabase
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

@Composable
fun ShopkeeperDashboard(
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var currentCampusShop by remember { mutableStateOf<CampusShop?>(null) }
    var shopOpen by remember { mutableStateOf(false) }
    var openingTime by remember { mutableStateOf("8:00 AM") }
    var closingTime by remember { mutableStateOf("8:00 PM") }
    val products = remember { mutableStateListOf<Product>() }

    var isLoading by remember { mutableStateOf(true) }
    var dashboardError by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    fun refreshShopData() {
        val user = supabase.auth.currentUserOrNull()
        if (user != null) {
            scope.launch {
                try {
                    val shop = SupabaseManager.getCampusShopByOwner(user.id)
                    if (shop != null) {
                        currentCampusShop = shop
                        shopOpen = shop.is_open
                        openingTime = shop.opening_time ?: "08:00 AM"
                        closingTime = shop.closing_time ?: "08:00 PM"
                        val prods = SupabaseManager.getProducts(shop.id!!)
                        products.clear()
                        products.addAll(prods)
                        dashboardError = null
                    } else {
                        dashboardError = "No shop found for this account."
                    }
                } catch (e: Exception) {
                    dashboardError = "Error loading shop: ${e.message}"
                } finally {
                    isLoading = false
                }
            }
        }
    }

    LaunchedEffect(Unit) { refreshShopData() }

    var showProducts by remember { mutableStateOf(false) }
    var showAddProduct by remember { mutableStateOf(false) }
    var showTiming by remember { mutableStateOf(false) }
    var showOrders by remember { mutableStateOf(false) }
    var showRejectionDialog by remember { mutableStateOf<Order?>(null) }
    var rejectionReason by remember { mutableStateOf("") }
    var orderRefreshTrigger by remember { mutableStateOf(0) }
    
    var showVerifyCodeDialog by remember { mutableStateOf<Order?>(null) }
    var enteredCode by remember { mutableStateOf("") }
    var verificationError by remember { mutableStateOf<String?>(null) }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Shopkeeper Dashboard", style = MaterialTheme.typography.headlineMedium)

            if (dashboardError != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    Text(text = dashboardError!!, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = if (shopOpen) "🟢 Shop is OPEN" else "🔴 Shop is CLOSED", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Timings: $openingTime - $closingTime")
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            currentCampusShop?.let { shop ->
                                scope.launch {
                                    val newStatus = !shopOpen
                                    if (SupabaseManager.updateCampusShopStatus(shop.id!!, newStatus) == null) refreshShopData()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(if (shopOpen) "Close Shop" else "Open Shop") }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { showProducts = true }, modifier = Modifier.fillMaxWidth()) { Text("🍔 Manage Menu / Products") }
            Spacer(modifier = Modifier.height(10.dp))
            Button(onClick = { showAddProduct = true }, modifier = Modifier.fillMaxWidth()) { Text("➕ Add Product") }
            Spacer(modifier = Modifier.height(10.dp))
            Button(onClick = { showTiming = true }, modifier = Modifier.fillMaxWidth()) { Text("⏰ Manage Shop Timing") }
            Spacer(modifier = Modifier.height(10.dp))
            Button(onClick = { showOrders = true }, modifier = Modifier.fillMaxWidth()) { Text("📦 Incoming Orders") }
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) { Text("Logout") }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showOrders) {
        var incomingOrders by remember { mutableStateOf<List<Order>>(emptyList()) }
        var orderItemsMap by remember { mutableStateOf<Map<String, List<OrderItem>>>(emptyMap()) }
        var isRefreshing by remember { mutableStateOf(false) }

        fun loadOrders() {
            scope.launch {
                currentCampusShop?.id?.let { id ->
                    isRefreshing = true
                    incomingOrders = SupabaseManager.getCampusShopOrders(id)
                    val itemsMap = mutableMapOf<String, List<OrderItem>>()
                    incomingOrders.forEach { order ->
                        itemsMap[order.id] = SupabaseManager.getOrderItems(order.id)
                    }
                    orderItemsMap = itemsMap
                    isRefreshing = false
                }
            }
        }

        LaunchedEffect(orderRefreshTrigger) { loadOrders() }

        AlertDialog(
            onDismissRequest = { showOrders = false },
            title = { Text("📦 Incoming Orders") },
            text = {
                Column(modifier = Modifier.heightIn(max = 500.dp).verticalScroll(rememberScrollState())) {
                    if (isRefreshing) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    
                    if (incomingOrders.isEmpty() && !isRefreshing) {
                        Text("No orders yet.")
                    } else {
                        incomingOrders.forEach { order ->
                            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(text = "Order #${order.id.takeLast(6)}", style = MaterialTheme.typography.titleMedium)
                                        Text(text = order.status, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                    }
                                    
                                    val items = orderItemsMap[order.id] ?: emptyList()
                                    Text("Items: ${items.joinToString(", ") { "${it.product_name} x${it.quantity}" }}")
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Payment: ${order.payment_status}")
                                    Text("Delivery: ${order.delivery_method ?: "Not Selected"}")
                                    
                                    if (order.preparation_time_minutes != null) {
                                        Text("Prep Time: ${order.preparation_time_minutes} mins", color = MaterialTheme.colorScheme.secondary)
                                    }

                                    if (order.delivery_method == "PICKUP") {
                                        if (order.status == "PAID" || order.status == "ACCEPTED") {
                                             Button(onClick = { scope.launch { if (SupabaseManager.updateOrderStatus(order.id, "READY_FOR_PICKUP") == null) loadOrders() } }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                                                Text("Mark Ready for Pickup")
                                            }
                                        } else if (order.status == "READY_FOR_PICKUP") {
                                            Button(onClick = { showVerifyCodeDialog = order; enteredCode = ""; verificationError = null }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                                                Text("Verify Pickup Code")
                                            }
                                        } else if (order.status == "COLLECTED") {
                                            Text("✅ Order Handed Over", color = MaterialTheme.colorScheme.primary)
                                        }
                                    } else if (order.delivery_method == "DESK_DELIVERY") {
                                        if (order.status == "PAID" || order.status == "ACCEPTED") {
                                            Button(onClick = { scope.launch { if (SupabaseManager.updateOrderStatus(order.id, "READY_FOR_PICKUP") == null) loadOrders() } }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                                                Text("Mark Ready (for Rider)")
                                            }
                                        }
                                        Text("Rider: ${if (order.rider_id == null) "Not Assigned" else "Assigned"}")
                                        if (order.rider_id != null) {
                                            Text("Rider Step: ${order.status}", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }

                                    if (order.status == "PENDING") {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(onClick = { scope.launch { if (SupabaseManager.updateOrderStatus(order.id, "ACCEPTED") == null) loadOrders() } }, modifier = Modifier.weight(1f)) { Text("Accept") }
                                            OutlinedButton(onClick = { showRejectionDialog = order }, modifier = Modifier.weight(1f)) { Text("Reject") }
                                        }
                                    } else if (order.status != "COLLECTED" && order.status != "DELIVERED" && order.status != "REJECTED") {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Set Prep Time:", style = MaterialTheme.typography.labelMedium)
                                        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            listOf(5, 10, 15, 20, 30).forEach { mins ->
                                                FilterChip(selected = order.preparation_time_minutes == mins, onClick = { scope.launch { if (SupabaseManager.updateOrderPreparationTime(order.id, mins) == null) loadOrders() } }, label = { Text("${mins}m") })
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showOrders = false }) { Text("Close") } }
        )
    }

    if (showVerifyCodeDialog != null) {
        AlertDialog(
            onDismissRequest = { showVerifyCodeDialog = null },
            title = { Text("Verify Pickup Code") },
            text = {
                Column {
                    Text("Enter code from student screen:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = enteredCode, onValueChange = { enteredCode = it.uppercase() }, label = { Text("Pickup Code") }, modifier = Modifier.fillMaxWidth())
                    if (verificationError != null) Text(text = verificationError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(onClick = {
                    val order = showVerifyCodeDialog!!
                    if (enteredCode == order.pickup_qr_token) {
                        scope.launch {
                            if (SupabaseManager.updateOrderStatus(order.id, "COLLECTED") == null) {
                                if (order.payment_status != "PAID") SupabaseManager.updateOrderPayment(order.id, "PAID")
                                showVerifyCodeDialog = null
                                orderRefreshTrigger++
                            }
                        }
                    } else { verificationError = "Invalid code." }
                }) { Text("Verify & Handover") }
            }
        )
    }

    if (showRejectionDialog != null) {
        AlertDialog(
            onDismissRequest = { showRejectionDialog = null },
            title = { Text("Reject Order") },
            text = { OutlinedTextField(value = rejectionReason, onValueChange = { rejectionReason = it }, label = { Text("Reason") }, modifier = Modifier.fillMaxWidth()) },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        if (SupabaseManager.updateOrderStatus(showRejectionDialog!!.id, "REJECTED", rejectionReason) == null) {
                            showRejectionDialog = null
                            rejectionReason = ""
                            orderRefreshTrigger++
                        }
                    }
                }) { Text("Confirm") }
            }
        )
    }

    if (showProducts) {
        AlertDialog(
            onDismissRequest = { showProducts = false },
            title = { Text("Manage Products") },
            text = {
                Column(modifier = Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState())) {
                    products.forEachIndexed { index, product ->
                        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(product.name, style = MaterialTheme.typography.titleMedium)
                                Text("₹${product.price} • ${if (product.available) "Available" else "Unavailable"}")
                                Spacer(modifier = Modifier.height(6.dp))
                                TextButton(onClick = {
                                    scope.launch {
                                        val updated = product.copy(available = !product.available)
                                        if (SupabaseManager.updateProduct(updated)) products[index] = updated
                                    }
                                }) { Text(if (product.available) "Mark Unavailable" else "Make Available") }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showProducts = false }) { Text("Done") } }
        )
    }

    if (showAddProduct) {
        var productName by remember { mutableStateOf("") }
        var productPrice by remember { mutableStateOf("") }
        var productDescription by remember { mutableStateOf("") }
        var addProductError by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { showAddProduct = false },
            title = { Text("Add Product") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    if (addProductError != null) { Text(text = addProductError!!, color = MaterialTheme.colorScheme.error); Spacer(modifier = Modifier.height(8.dp)) }
                    OutlinedTextField(value = productName, onValueChange = { productName = it }, label = { Text("Product name") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(value = productDescription, onValueChange = { productDescription = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(value = productPrice, onValueChange = { productPrice = it }, label = { Text("Price") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val priceInt = productPrice.toIntOrNull()
                    if (productName.isNotBlank() && priceInt != null && currentCampusShop != null) {
                        scope.launch {
                            val newProduct = Product(shop_id = currentCampusShop!!.id!!, name = productName, description = productDescription, price = priceInt)
                            if (SupabaseManager.addProduct(newProduct)) {
                                products.clear()
                                products.addAll(SupabaseManager.getProducts(currentCampusShop!!.id!!))
                                showAddProduct = false
                            } else { addProductError = "Failed." }
                        }
                    }
                }) { Text("Add") }
            }
        )
    }

    if (showTiming) {
        var newOpening by remember { mutableStateOf(openingTime) }
        var newClosing by remember { mutableStateOf(closingTime) }
        AlertDialog(
            onDismissRequest = { showTiming = false },
            title = { Text("Shop Timing") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(value = newOpening, onValueChange = { newOpening = it }, label = { Text("Opening") })
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(value = newClosing, onValueChange = { newClosing = it }, label = { Text("Closing") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    currentCampusShop?.let { shop ->
                        scope.launch {
                            if (SupabaseManager.updateCampusShopTiming(shop.id!!, newOpening, newClosing) == null) { refreshShopData(); showTiming = false }
                        }
                    }
                }) { Text("Save") }
            }
        )
    }
}
