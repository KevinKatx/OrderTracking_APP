package com.example.ordertrackingapp.databases.handlers

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
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
                "Dish_Name TEXT, " +
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
            put("Dish_Name", order.Dish_Name)
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
                val order = Order()

                // Prevent column index errors
                val orderIDIndex = result.getColumnIndex("order_ID")
                val customerIDIndex = result.getColumnIndex("customerID")
                val totalPriceIndex = result.getColumnIndex("TotalPrice")
                val promoIDIndex = result.getColumnIndex("PromoID")
                val dishNameIndex = result.getColumnIndex("Dish_Name")
                val statusIndex = result.getColumnIndex("Status")
                val orderDateIndex = result.getColumnIndex("OrderDate")
                val paymentTypeIndex = result.getColumnIndex("PaymentType")

                // Only set values if the column exists
                if (orderIDIndex != -1) order.order_ID = result.getInt(orderIDIndex)
                if (customerIDIndex != -1) order.customerID = result.getInt(customerIDIndex)
                if (totalPriceIndex != -1) order.TotalPrice = result.getInt(totalPriceIndex)
                if (promoIDIndex != -1) order.PromoID = result.getInt(promoIDIndex)
                if (dishNameIndex != -1) order.Dish_Name = result.getString(dishNameIndex)
                if (statusIndex != -1) order.Status = result.getString(statusIndex)
                if (orderDateIndex != -1) {
                    val dateString = result.getString(orderDateIndex)
                    order.OrderDate = LocalDate.parse(dateString) // Converts string to LocalDate
                }
                if (paymentTypeIndex != -1) order.PaymentType = result.getString(paymentTypeIndex)

                list.add(order)
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
        cv.put("Dish_Name", order.Dish_Name)
        cv.put("Status", order.Status)
        cv.put("OrderDate", order.OrderDate.toString()) // Convert LocalDate to String
        cv.put("PaymentType", order.PaymentType)

        // Updating the row where order_ID matches
        val result = db.update("Orders", cv, "order_ID = ?", arrayOf(order.order_ID.toString()))

        db.close()
        return result // Returns number of rows affected
    }
}