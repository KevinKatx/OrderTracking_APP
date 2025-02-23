package com.example.ordertrackingapp.databases.Tables

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate

class Order {
    var orderID: Int = 0 // Primary Key (PK)
    var productID: Int = 0
    var customerID: Int = 0 // Foreign Key (FK1) to Customer
    var totalPrice: Float = 0.0f // FK2 to Product
    var promoID: Int = 0 // FK3 to Discount
    var status: String = ""

    @RequiresApi(Build.VERSION_CODES.O)
    var orderDate: LocalDate = LocalDate.now()
    var paymentType: String = ""

    @RequiresApi(Build.VERSION_CODES.O)
    constructor(
        productID: Int, customerID: Int, totalPrice: Float, promoID: Int,
        status: String, orderDate: LocalDate, paymentType: String
    ) {
        this.productID = productID
        this.customerID = customerID
        this.totalPrice = totalPrice
        this.promoID = promoID
        this.status = status
        this.orderDate = orderDate
        this.paymentType = paymentType
    }

    constructor()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun toString(): String {
        return "Order(orderID=$orderID, productID=$productID, customerID=$customerID, " +
                "totalPrice=$totalPrice, promoID=$promoID, status=$status, " +
                "orderDate=$orderDate, paymentType=$paymentType)"
    }
}
