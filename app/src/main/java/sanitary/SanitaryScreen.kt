package sanitary

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
import com.digitalcampus.app.models.Product
import com.digitalcampus.app.models.CampusShop
import com.digitalcampus.app.supabase
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

@Composable
fun SanitaryScreen(
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var shops by remember { mutableStateOf<List<CampusShop>>(emptyList()) }
    var selectedCampusShop by remember { mutableStateOf<CampusShop?>(null) }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var cart by remember { mutableStateOf<List<Product>>(emptyList()) }
    var orderPlaced by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        shops = SupabaseManager.getCampusShopsByType("PINK_PACKAGING")
        isLoading = false
    }

    LaunchedEffect(selectedCampusShop) {
        selectedCampusShop?.id?.let { id ->
            products = SupabaseManager.getProducts(id)
        }
    }

    Scaffold { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 24.dp,
                end = 24.dp,
                top = paddingValues.calculateTopPadding() + 24.dp,
                bottom = paddingValues.calculateBottomPadding() + 100.dp
            ),
            verticalArrangement = Arrangement.Top
        ) {
            if (selectedCampusShop == null) {
                item {
                    Text(
                        text = "🩷 Pink Packaging",
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Available Shops")
                    Spacer(modifier = Modifier.height(24.dp))
                }

                if (isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (shops.isEmpty()) {
                    item {
                        Text("No Pink Packaging shops available.")
                    }
                } else {
                    items(shops) { shop ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(shop.name, style = MaterialTheme.typography.titleLarge)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (shop.is_open) "🟢 Open • ${shop.opening_time ?: "8:00 AM"} - ${shop.closing_time ?: "8:00 PM"}" else "🔴 Closed",
                                    color = if (shop.is_open) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { selectedCampusShop = shop },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("View Products")
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                        Text("Back")
                    }
                }
            } else {
                item {
                    Text(
                        text = selectedCampusShop?.name ?: "",
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Products")
                    Spacer(modifier = Modifier.height(20.dp))
                }

                if (products.isEmpty()) {
                    item {
                        Text("No products available.")
                    }
                } else {
                    items(products) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(item.name, style = MaterialTheme.typography.titleMedium)
                                    Text("₹${item.price}")
                                    if (!item.available) {
                                        Text("Out of stock", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                                Button(
                                    onClick = { cart = cart + item },
                                    enabled = item.available && (selectedCampusShop?.is_open == true)
                                ) {
                                    Text("Add")
                                }
                            }
                        }
                    }
                }

                if (cart.isNotEmpty()) {
                    val total = cart.sumOf { it.price }
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Cart: ${cart.size} item(s) - Total: ₹$total", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val user = supabase.auth.currentUserOrNull()
                                if (user != null && selectedCampusShop != null) {
                                    scope.launch {
                                        val orderId = "DC${System.currentTimeMillis()}"
                                        val order = Order(
                                            id = orderId,
                                            student_id = user.id,
                                            shop_id = selectedCampusShop!!.id!!,
                                            status = "PENDING",
                                            subtotal = total,
                                            total = total,
                                            payment_status = "PENDING"
                                        )
                                        val items = cart.map {
                                            OrderItem(
                                                order_id = orderId,
                                                product_id = it.id!!,
                                                product_name = it.name,
                                                quantity = 1,
                                                unit_price = it.price,
                                                subtotal = it.price
                                            )
                                        }
                                        if (SupabaseManager.createOrder(order, items)) {
                                            cart = emptyList()
                                            orderPlaced = true
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Place Order")
                        }
                    }
                }

                if (orderPlaced) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("✅ Order placed successfully!", style = MaterialTheme.typography.titleMedium)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            selectedCampusShop = null
                            cart = emptyList()
                            orderPlaced = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Back to Shops")
                    }
                }
            }
        }
    }
}
