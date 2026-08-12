package com.digitalcampus.app

import androidx.compose.runtime.mutableStateListOf

data class CampusOrder(
    val id: String,
    val shopName: String,
    val items: List<String>,
    val total: Int,
    val status: String,
    val deliveryOption: String = "",
    val deliveryCharge: Int = 0,
    val paymentStatus: String = "Not Required"
)

object OrderManager {

    val orders = mutableStateListOf<CampusOrder>()

    var riderAvailable = true

    fun addOrder(order: CampusOrder) {
        orders.add(order)
    }

    fun updateOrderStatus(
        orderId: String,
        newStatus: String
    ) {
        val index = orders.indexOfFirst {
            it.id == orderId
        }

        if (index != -1) {
            orders[index] = orders[index].copy(
                status = newStatus
            )
        }
    }

    fun updateDeliveryOption(
        orderId: String,
        option: String,
        charge: Int
    ) {
        val index = orders.indexOfFirst {
            it.id == orderId
        }

        if (index != -1) {
            orders[index] = orders[index].copy(
                deliveryOption = option,
                deliveryCharge = charge
            )
        }
    }

    fun updatePaymentStatus(
        orderId: String,
        paymentStatus: String
    ) {
        val index = orders.indexOfFirst {
            it.id == orderId
        }

        if (index != -1) {
            orders[index] = orders[index].copy(
                paymentStatus = paymentStatus
            )
        }
    }
}