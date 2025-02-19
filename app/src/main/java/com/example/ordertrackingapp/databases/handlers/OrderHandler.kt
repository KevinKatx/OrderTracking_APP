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
                "orderID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "customerID INTEGER, " +
                "TotalPrice INTEGER, " +
                "PromoID INTEGER, " +
                "Status TEXT, " +
                "OrderDate TEXT DEFAULT (date('now')), " +
                "PaymentType TEXT)"

        db?.execSQL(createTable)

        val insertDummyOrder = "INSERT INTO Orders (customerID, TotalPrice, PromoID, Status, PaymentType) " +
                "VALUES (1, 100, 0, 'Pending', 'Credit Card')"

        db?.execSQL(insertDummyOrder)

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS Orders")
        onCreate(db)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun insertData(order : Order): Boolean {
        val db = this.writableDatabase
        val cv = ContentValues().apply {
            put("customerID", order.customerID)
            put("TotalPrice", order.totalPrice)
            put("PromoID", order.promoID)
            put("Status", order.status)
            put("OrderDate", order.orderDate.toString()) // Ensure it's stored as a string
            put("PaymentType", order.paymentType)
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
    fun readData(orderID: Int? = null): MutableList<Order> {
        val list: MutableList<Order> = ArrayList()
        val db = this.readableDatabase
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
                // Ensure columns match exactly
                val orderIDIndex = result.getColumnIndex("orderID")
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

                // Extract values
                val orderID = result.getInt(orderIDIndex)
                val customerID = result.getInt(customerIDIndex)
                val totalPrice = result.getInt(totalPriceIndex)
                val promoID = result.getInt(promoIDIndex)
                val status = result.getString(statusIndex)
                val paymentType = result.getString(paymentTypeIndex)

                // Parse OrderDate
                val orderDateString = result.getString(orderDateIndex)
                val orderDate = try {
                    LocalDate.parse(orderDateString)
                } catch (e: Exception) {
                    Log.e("DB_ERROR", "Failed to parse date: $orderDateString", e)
                    LocalDate.now()
                }

                val order = Order(customerID, totalPrice, promoID, status, orderDate, paymentType)
                order.orderID = orderID

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
        val db = this.writableDatabase
        val cv = ContentValues()

        // Setting updated values
        cv.put("customerID", order.customerID)
        cv.put("TotalPrice", order.totalPrice)
        cv.put("PromoID", order.promoID)
        cv.put("Status", order.status)
        cv.put("OrderDate", order.orderDate.toString()) // Convert LocalDate to String
        cv.put("PaymentType", order.paymentType)

        // Updating the row where orderID matches
        val result = db.update("Orders", cv, "orderID = ?", arrayOf(order.orderID.toString()))

        db.close()
        return result // Returns number of rows affected
    }
}