package com.example.ordertrackingapp.databases.handlers

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.ordertrackingapp.databases.Tables.Order
import java.time.LocalDate

class OrderHandler (var context: Context) : SQLiteOpenHelper(context,"FoodStopDB",null,1){
    override fun onCreate(db: SQLiteDatabase?){
        val createTable = "CREATE TABLE Orders (" +
                "order_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "customerID INTEGER, " +
                "TotalPrice INTEGER, " +
                "PromoID INTEGER, " +
                "Status TEXT, " +
                "OrderDate TEXT DEFAULT (date('now')), " +
                "PaymentType TEXT)"

        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun insertData(order : Order): Boolean {
        val db = this.writableDatabase
        val cv = ContentValues().apply {
            put("customerID", order.customerID)
            put("TotalPrice", order.TotalPrice)
            put("PromoID", order.PromoID)
            put("Status", order.Status)
            put("OrderDate", order.OrderDate.toString()) // Ensure it's stored as a string
            put("PaymentType", order.PaymentType)
        }

        val result = db.insert("Orders", null, cv)
        db.close()

        return if (result == -1L) {
            Toast.makeText(context, "Insert Failed", Toast.LENGTH_SHORT).show()
            false
        } else {
            Toast.makeText(context, "Insert Successful", Toast.LENGTH_SHORT).show()
            true
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun readData(): MutableList<Order> {
        val list: MutableList<Order> = ArrayList()
        val db = this.readableDatabase
        val query = "SELECT * FROM Orders"
        val result = db.rawQuery(query, null)

        if (result.moveToFirst()) {
            do {
                // Debug column indexes
                val orderIDIndex = result.getColumnIndex("order_ID")
                val customerIDIndex = result.getColumnIndex("customerID")
                val totalPriceIndex = result.getColumnIndex("TotalPrice")
                val promoIDIndex = result.getColumnIndex("PromoID")
                val statusIndex = result.getColumnIndex("Status")
                val orderDateIndex = result.getColumnIndex("OrderDate")
                val paymentTypeIndex = result.getColumnIndex("PaymentType")

                if (orderIDIndex == -1 || customerIDIndex == -1 || totalPriceIndex == -1 ||
                    promoIDIndex == -1 || statusIndex == -1 || orderDateIndex == -1 || paymentTypeIndex == -1) {
                    Log.e("DB_ERROR", "One or more column names are incorrect!")
                    continue
                }

                // Get values
                val orderID = result.getInt(orderIDIndex)
                val customerID = result.getInt(customerIDIndex)
                val totalPrice = result.getInt(totalPriceIndex)
                val promoID = result.getInt(promoIDIndex)
                val status = result.getString(statusIndex)
                val paymentType = result.getString(paymentTypeIndex)

                // Handle Date Parsing
                val orderDateString = result.getString(orderDateIndex)
                var orderDate: LocalDate? = null
                try {
                    orderDate = LocalDate.parse(orderDateString)
                } catch (e: Exception) {
                    Log.e("DB_ERROR", "Failed to parse date: $orderDateString", e)
                }

                val order = Order(customerID, totalPrice, promoID, status, orderDate ?: LocalDate.now(), paymentType)
                order.order_ID = orderID

                list.add(order)

                // Debugging log
                Log.d("DB_DEBUG", "Order Read: $order")

            } while (result.moveToNext())
        }

        result.close()
        db.close()
        return list
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateData(order: Order): Int {
        val db = this.writableDatabase
        val cv = ContentValues()

        // Setting updated values
        cv.put("customerID", order.customerID)
        cv.put("TotalPrice", order.TotalPrice)
        cv.put("PromoID", order.PromoID)
        cv.put("Status", order.Status)
        cv.put("OrderDate", order.OrderDate.toString()) // Convert LocalDate to String
        cv.put("PaymentType", order.PaymentType)

        // Updating the row where order_ID matches
        val result = db.update("Orders", cv, "order_ID = ?", arrayOf(order.order_ID.toString()))

        db.close()
        return result // Returns number of rows affected
    }
}