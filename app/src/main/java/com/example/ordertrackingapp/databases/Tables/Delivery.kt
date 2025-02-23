package com.example.ordertrackingapp.databases.Tables

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalTime
import java.time.LocalDate
import kotlin.collections.List
import kotlin.reflect.full.memberProperties

class Delivery {
    var Delivery_ID: Int = 0 // Primary Key
    var OrderID: MutableList<Order> = mutableListOf() //FK order
    @RequiresApi(Build.VERSION_CODES.O)
    var DeliveryStart: LocalTime = LocalTime.MIDNIGHT
    @RequiresApi(Build.VERSION_CODES.O)
    var DeliveryEnd: LocalTime = LocalTime.MIDNIGHT
    @RequiresApi(Build.VERSION_CODES.O)
    var DeliveryDate: LocalDate = LocalDate.now()
    var Status: String = ""

    // Returns columns
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
    }

}