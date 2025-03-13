package com.example.ordertrackingapp.databases.Tables

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalTime
import java.time.LocalDate
import kotlin.collections.List
import kotlin.reflect.full.memberProperties

class Delivery {
    var Delivery_ID: Int = 0
    var Order_ID: Int = 0
    @RequiresApi(Build.VERSION_CODES.O)
    var deliveryDate: LocalDate = LocalDate.now()
    @RequiresApi(Build.VERSION_CODES.O)
    var deliveryStart: LocalTime = LocalTime.now()
    @RequiresApi(Build.VERSION_CODES.O)
    var deliveryEnd: LocalTime = LocalTime.now()
    var status: String = ""

    @RequiresApi(Build.VERSION_CODES.O)
    constructor(
        Delivery_ID: Int, Order_ID: Int, deliveryDate: LocalDate,
        deliveryStart: LocalTime, deliveryEnd: LocalTime, status: String
    ) {
        this.Delivery_ID = Delivery_ID
        this.Order_ID = Order_ID
        this.deliveryDate = deliveryDate
        this.deliveryStart = deliveryStart
        this.deliveryEnd = deliveryEnd
        this.status = status
    }

    constructor()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun toString(): String {
        return "Delivery(deliveryID=$Delivery_ID, orderID=$Order_ID, " +
                "orderDate=$deliveryDate, orderStart=$deliveryStart, orderEnd=$deliveryEnd, " +
                "status=$status)"
    }
}