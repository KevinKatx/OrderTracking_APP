package com.example.ordertrackingapp.databases.Tables

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import kotlin.collections.List
import kotlin.reflect.full.memberProperties

class Order {
    var order_ID: Int = 0 // Primary Key (PK)
    var customerID: Int = 0 // Foreign Key (FK1) to Customer
    var TotalPrice: Int = 0 // FK2 to Product
    var PromoID: Int = 0 // FK3 to Discount
    var Status: String = ""
    @RequiresApi(Build.VERSION_CODES.O)
    var OrderDate: LocalDate = LocalDate.now()
    var PaymentType: String = ""

    @RequiresApi(Build.VERSION_CODES.O)
    constructor(customerID: Int, TotalPrice: Int, PromoID: Int,
                Status: String, OrderDate: LocalDate, PaymentType: String) {
        this.customerID = customerID
        this.TotalPrice = TotalPrice
        this.PromoID = PromoID
        this.Status = Status
        this.OrderDate = OrderDate
        this.PaymentType = PaymentType


    }
    constructor(){}

   /* // Returns columns
    fun getColumns(): List<String> {
        val properties = this::class.memberProperties.map { it.name }
        val idColumns = properties.filter { it.endsWith("_ID") }
        val otherColumns = properties.filterNot { it.endsWith("_ID") }

        return idColumns + otherColumns // Ensures ID columns come first
    }

    // Gets table name by removing `_ID`
    fun getTableName(): String {
        return this::class.memberProperties
            .firstOrNull { it.name.endsWith("_ID") }
            ?.name?.removeSuffix("_ID") ?: "UnknownTable"
    }*/
}