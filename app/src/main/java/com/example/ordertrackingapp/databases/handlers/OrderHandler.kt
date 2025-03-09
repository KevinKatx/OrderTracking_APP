package com.example.ordertrackingapp.databases.handlers

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.ordertrackingapp.databases.DatabaseHelper
import android.os.Build
import com.example.ordertrackingapp.databases.Tables.Order
import java.time.LocalDate

class OrderHandler(private val context: Context) {

    private val dbHelper = DatabaseHelper(context)


    @RequiresApi(Build.VERSION_CODES.O)
    fun insertData(order: Order): Boolean {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("customerID", order.customerID)
            put("PromoID", order.promoID)
            put("TotalPrice", order.totalPrice)
            put("Status", order.status)
            put("OrderDate", order.orderDate.toString()) // Ensure it's stored as a string
            put("PaymentType", order.paymentType)
        }

        val result = db.insert("Orders", null, cv)
        db.close()

        Log.d("DB_INSERT", "Insert Order result: $result")
        return if (result == -1L) {
            Toast.makeText(context, "Insert Failed", Toast.LENGTH_SHORT).show()
            Log.e("DB_ERROR", "Insert failed")
            false
        } else {
            Toast.makeText(context, "Insert Order Successful", Toast.LENGTH_SHORT).show()
            Log.d("DB_SUCCESS", "Insert Order successful with ID: $result")
            true
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun readData(orderID: Int? = null): MutableList<Order> {
        val list: MutableList<Order> = ArrayList()
        val db = dbHelper.readableDatabase
        val query = if (orderID != null) {
            "SELECT * FROM Orders WHERE orderID = ?"
        } else {
            "SELECT * FROM Orders"
        }
        val result = if (orderID != null) {
            db.rawQuery(query, arrayOf(orderID.toString()))
        } else {
            db.rawQuery(query, null)
        }

        if (result.moveToFirst()) {
            do {
                val orderID = result.getInt(result.getColumnIndexOrThrow("orderID"))
                val customerID = result.getInt(result.getColumnIndexOrThrow("customerID"))
                val totalPrice = result.getInt(result.getColumnIndexOrThrow("TotalPrice"))
                val promoID = result.getInt(result.getColumnIndexOrThrow("PromoID"))
                val status = result.getString(result.getColumnIndexOrThrow("Status"))
                val paymentType = result.getString(result.getColumnIndexOrThrow("PaymentType"))

                val orderDateString = result.getString(result.getColumnIndexOrThrow("OrderDate"))
                val orderDate = try {
                    LocalDate.parse(orderDateString)
                } catch (e: Exception) {
                    Log.e("DB_ERROR", "Failed to parse date: $orderDateString", e)
                    LocalDate.now()
                }

                val order = Order(orderID, customerID, totalPrice, promoID, status, orderDate, paymentType)
                list.add(order)
            } while (result.moveToNext())
        } else {
            Log.i("DB_INFO", "No orders found in database.")
        }

        result.close()
        db.close()
        return list
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateData(order: Order): Int {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("customerID", order.customerID)
            put("TotalPrice", order.totalPrice)
            put("PromoID", order.promoID)
            put("Status", order.status)
            put("OrderDate", order.orderDate.toString()) // Convert LocalDate to String
            put("PaymentType", order.paymentType)
        }

        val result = db.update("Orders", cv, "orderID = ?", arrayOf(order.orderID.toString()))
        db.close()
        return result // Returns number of rows affected
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun deleteData(productID: Int): Boolean {
        val db = dbHelper.writableDatabase
        val result = db.delete("Orders", "orderID = ?", arrayOf(productID.toString()))
        db.close()
        val result2 = db.delete("OrderDetails", "orderID = ?", arrayOf(productID.toString()))

        return if (result > 0 && result2 > 0) {
            Toast.makeText(context, "Delete Order and OrderDetails Successful", Toast.LENGTH_SHORT).show()
            true
        } else {
            Toast.makeText(context, "Order Deletion Failed", Toast.LENGTH_SHORT).show()
            false
        }


    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getLatestOrderID(): Int {
        val db = dbHelper.readableDatabase
        val query = "SELECT MAX(orderID) FROM `Orders`" // Query without curly braces
        val cursor = db.rawQuery(query, null) // No need to pass arrayOf(orderID)

        var latestOrderID = 0 // Default value if no records found

        if (cursor.moveToFirst() && !cursor.isNull(0)) { // Check if cursor is not empty and not null
            latestOrderID = cursor.getInt(0)
        }

        cursor.close() // Always close the cursor
        db.close() // Close the database connection

        return latestOrderID
    }

}
