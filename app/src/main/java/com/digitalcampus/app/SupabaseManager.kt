package com.digitalcampus.app

import com.digitalcampus.app.models.Order
import com.digitalcampus.app.models.OrderItem
import com.digitalcampus.app.models.OrderFeedback
import com.digitalcampus.app.models.Product
import com.digitalcampus.app.models.RiderProfile
import com.digitalcampus.app.models.CampusShop
import com.digitalcampus.app.models.UserProfile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order as SupabaseOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.serializer

object SupabaseManager {

    suspend fun getUserProfile(userId: String): UserProfile? = withContext(Dispatchers.IO) {
        try {
            val result = supabase.postgrest["profiles"]
                .select(columns = Columns.ALL) {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingleOrNull<UserProfile>()
            result
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun createUserProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["profiles"].insert(profile)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getCurrentUserRole(): String? {
        val user = supabase.auth.currentUserOrNull() ?: return null
        val profile = getUserProfile(user.id)
        return profile?.role
    }

    // SHOP OPERATIONS

    suspend fun getCampusShopByOwner(ownerId: String): CampusShop? = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["shops"]
                .select(columns = Columns.ALL) {
                    filter {
                        eq("owner_id", ownerId)
                    }
                }
                .decodeSingleOrNull<CampusShop>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun updateCampusShopStatus(shopId: Long, isOpen: Boolean): String? = withContext(Dispatchers.IO) {
        try {
            val updateData = buildJsonObject {
                put("is_open", isOpen)
            }
            supabase.postgrest["shops"].update(updateData) {
                filter {
                    eq("id", shopId)
                }
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            "Supabase Error: ${e.message ?: e.toString()}"
        }
    }

    suspend fun updateCampusShopTiming(shopId: Long, opening: String, closing: String): String? = withContext(Dispatchers.IO) {
        try {
            val updateData = buildJsonObject {
                put("opening_time", opening)
                put("closing_time", closing)
            }
            supabase.postgrest["shops"].update(updateData) {
                filter {
                    eq("id", shopId)
                }
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            "Supabase Error: ${e.message ?: e.toString()}"
        }
    }

    suspend fun getShopById(shopId: Long): CampusShop? = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["shops"]
                .select(columns = Columns.ALL) {
                    filter {
                        eq("id", shopId)
                    }
                }
                .decodeSingleOrNull<CampusShop>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getCampusShopsByType(type: String): List<CampusShop> = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["shops"]
                .select(columns = Columns.ALL) {
                    filter {
                        eq("shop_type", type)
                    }
                }
                .decodeList<CampusShop>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getProducts(shopId: Long): List<Product> = withContext(Dispatchers.IO) {
        try {
            val result = supabase.postgrest["products"]
                .select(columns = Columns.ALL) {
                    filter {
                        eq("shop_id", shopId)
                    }
                }
                .decodeList<Product>()
            result
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun addProduct(product: Product) = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["products"].insert(product)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateProduct(product: Product) = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["products"].update(product) {
                filter {
                    eq("id", product.id!!)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteProduct(productId: Int) = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["products"].delete {
                filter {
                    eq("id", productId)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // ORDER OPERATIONS

    suspend fun createOrder(order: Order, items: List<OrderItem>) = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["orders"].insert(order)
            supabase.postgrest["order_items"].insert(items)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getStudentOrders(studentId: String): List<Order> = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["orders"]
                .select(columns = Columns.ALL) {
                    filter {
                        eq("student_id", studentId)
                    }
                    order("created_at", SupabaseOrder.DESCENDING)
                }
                .decodeList<Order>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getCampusShopOrders(shopId: Long): List<Order> = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["orders"]
                .select(columns = Columns.ALL) {
                    filter {
                        eq("shop_id", shopId)
                    }
                    order("created_at", SupabaseOrder.DESCENDING)
                }
                .decodeList<Order>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getOrderItems(orderId: String): List<OrderItem> = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["order_items"]
                .select(columns = Columns.ALL) {
                    filter {
                        eq("order_id", orderId)
                    }
                }
                .decodeList<OrderItem>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun updateOrderStatus(
        orderId: String,
        status: String,
        rejectionReason: String? = null
    ): String? = withContext(Dispatchers.IO) {
        try {
            val updateData = buildJsonObject {
                put("status", status)
                put("rejection_reason", rejectionReason)
            }
            supabase.postgrest["orders"].update(updateData) {
                filter {
                    eq("id", orderId)
                }
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            "Supabase Error: ${e.message ?: e.toString()}"
        }
    }

    suspend fun updateOrderPreparationTime(orderId: String, minutes: Int): String? = withContext(Dispatchers.IO) {
        try {
            val updateData = buildJsonObject {
                put("preparation_time_minutes", minutes)
            }
            supabase.postgrest["orders"].update(updateData) {
                filter {
                    eq("id", orderId)
                }
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            "Supabase Error: ${e.message ?: e.toString()}"
        }
    }

    suspend fun updateOrderDelivery(
        orderId: String,
        method: String,
        fee: Int,
        total: Int,
        qrToken: String? = null
    ): String? = withContext(Dispatchers.IO) {
        try {
            val updateData = buildJsonObject {
                put("delivery_method", method)
                put("delivery_fee", fee)
                put("total", total)
                put("pickup_qr_token", qrToken)
                put("status", "PAYMENT_PENDING")
            }
            supabase.postgrest["orders"].update(updateData) {
                filter {
                    eq("id", orderId)
                }
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            "Supabase Error: ${e.message ?: e.toString()}"
        }
    }

    suspend fun updateOrderPayment(orderId: String, paymentStatus: String): String? = withContext(Dispatchers.IO) {
        try {
            val order = supabase.postgrest["orders"]
                .select(columns = Columns.ALL) {
                    filter { eq("id", orderId) }
                }
                .decodeSingleOrNull<Order>()

            val nextStatus = if (paymentStatus == "PAID") {
                if (order?.delivery_method == "PICKUP") "WAITING_FOR_PICKUP" else "PAID"
            } else {
                "PAYMENT_PENDING"
            }

            val updateData = buildJsonObject {
                put("payment_status", paymentStatus)
                put("status", nextStatus)
            }
            supabase.postgrest["orders"].update(updateData) {
                filter {
                    eq("id", orderId)
                }
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            "Supabase Error: ${e.message ?: e.toString()}"
        }
    }

    suspend fun submitFeedback(feedback: OrderFeedback): String? = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["feedback"].insert(feedback)
            null
        } catch (e: Exception) {
            e.printStackTrace()
            "Feedback Submission Error: ${e.message ?: e.toString()}"
        }
    }

    // RIDER OPERATIONS

    suspend fun getRiderAvailability(): Boolean = withContext(Dispatchers.IO) {
        try {
            val riders = supabase.postgrest["rider_profiles"]
                .select(columns = Columns.ALL) {
                    filter {
                        eq("is_available", true)
                    }
                }
                .decodeList<RiderProfile>()
            riders.isNotEmpty()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateRiderAvailability(riderId: String, available: Boolean): String? = withContext(Dispatchers.IO) {
        try {
            val upsertData = buildJsonObject {
                put("rider_id", riderId)
                put("is_available", available)
            }
            supabase.postgrest["rider_profiles"].upsert(upsertData)
            null
        } catch (e: Exception) {
            e.printStackTrace()
            val msg = e.message ?: e.toString()
            "Supabase Update Failed: $msg"
        }
    }

    suspend fun getRiderProfile(riderId: String): RiderProfile? = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["rider_profiles"]
                .select(columns = Columns.ALL) {
                    filter {
                        eq("rider_id", riderId)
                    }
                }
                .decodeSingleOrNull<RiderProfile>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getAvailableDeliveries(): List<Order> = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["orders"]
                .select(columns = Columns.ALL) {
                    filter {
                        or {
                            eq("status", "PAID")
                            eq("status", "PREPARING")
                            eq("status", "READY_FOR_PICKUP")
                        }
                        eq("delivery_method", "DESK_DELIVERY")
                        exact("rider_id", null)
                    }
                    order("created_at", SupabaseOrder.ASCENDING)
                }
                .decodeList<Order>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun assignRiderToOrder(orderId: String, riderId: String): String? = withContext(Dispatchers.IO) {
        try {
            // 1. Verify eligibility atomically
            val order = supabase.postgrest["orders"]
                .select(columns = Columns.ALL) {
                    filter { eq("id", orderId) }
                }
                .decodeSingleOrNull<Order>()

            if (order == null) return@withContext "Order not found."
            if (order.rider_id != null) return@withContext "Order already accepted by another rider."
            if (order.delivery_method != "DESK_DELIVERY") return@withContext "Not a Desk Delivery order."
            
            val validStatuses = listOf("PAID", "PREPARING", "READY_FOR_PICKUP")
            if (order.status !in validStatuses) {
                return@withContext "Order is not ready for delivery (Status: ${order.status})."
            }

            val updateData = buildJsonObject {
                put("rider_id", riderId)
                put("status", "RIDER_ASSIGNED")
            }
            
            val response = supabase.postgrest["orders"].update(updateData) {
                filter {
                    eq("id", orderId)
                    exact("rider_id", null)
                }
                select()
            }
            
            if (response.decodeList<Order>().isEmpty()) {
                return@withContext "Failed to accept delivery. It may have been claimed just now."
            }
            
            null
        } catch (e: Exception) {
            e.printStackTrace()
            "Supabase Error: ${e.message ?: e.toString()}"
        }
    }

    suspend fun getRiderAssignedOrders(riderId: String): List<Order> = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["orders"]
                .select(columns = Columns.ALL) {
                    filter {
                        eq("rider_id", riderId)
                        neq("status", "DELIVERED")
                    }
                }
                .decodeList<Order>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
