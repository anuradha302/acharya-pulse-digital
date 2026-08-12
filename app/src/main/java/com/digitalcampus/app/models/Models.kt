package com.digitalcampus.app.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val created_at: String? = null
)

@Serializable
data class CampusShop(
    @SerialName("id") val id: Long? = null,
    @SerialName("name") val name: String,
    @SerialName("shop_type") val shop_type: String,
    @SerialName("owner_id") val owner_id: String,
    @SerialName("is_open") val is_open: Boolean = true,
    @SerialName("opening_time") val opening_time: String? = null,
    @SerialName("closing_time") val closing_time: String? = null,
    @SerialName("image_url") val image_url: String? = null,
    @SerialName("delivery_fee") val delivery_fee: Int = 20
)

@Serializable
data class Product(
    @SerialName("id") val id: Long? = null,
    @SerialName("shop_id") val shop_id: Long,
    @SerialName("name") val name: String,
    @SerialName("description") val description: String? = null,
    @SerialName("price") val price: Int,
    @SerialName("image_url") val image_url: String? = null,
    @SerialName("available") val available: Boolean = true,
    @SerialName("created_at") val created_at: String? = null
)

@Serializable
data class Order(
    val id: String,
    val student_id: String,
    val shop_id: Long,
    val status: String,
    val delivery_method: String? = null,
    val rider_id: String? = null,
    val subtotal: Int,
    val delivery_fee: Int = 0,
    val total: Int,
    val payment_status: String = "PENDING",
    val pickup_qr_token: String? = null,
    val preparation_time_minutes: Int? = null,
    val rejection_reason: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)

@Serializable
data class OrderFeedback(
    val id: Long? = null,
    val order_id: String,
    val student_id: String,
    val shop_id: Long,
    val rating: Int,
    val feedback_text: String? = null,
    val created_at: String? = null
)

@Serializable
data class OrderItem(
    val id: Long? = null,
    val order_id: String,
    val product_id: Long,
    val product_name: String,
    val quantity: Int,
    val unit_price: Int,
    val subtotal: Int
)

@Serializable
data class RiderProfile(
    val rider_id: String,
    val is_available: Boolean = false
)
