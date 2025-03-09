package com.example.ordertrackingapp.databases.handlers

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.ordertrackingapp.databases.DatabaseHelper
import com.example.ordertrackingapp.databases.Tables.Order
import com.example.ordertrackingapp.databases.Tables.OrderDetails
import java.time.LocalDate

class OrderDetailsHandler(private val context: Context) {
    private val dbHelper = DatabaseHelper(context)

    @RequiresApi(Build.VERSION_CODES.O)
    fun insertData(orderDetails: OrderDetails): Boolean {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("orderID", orderDetails.orderID)
            put("productID", orderDetails.productID)
            put("quantity", orderDetails.quantity)

        }

        val result = db.insert("OrderDetails", null, cv)
        db.close()

        Log.d("DB_INSERT", "Insert OrderDetals result: $result")
        return if (result == -1L) {
            Toast.makeText(context, "Insert Failed", Toast.LENGTH_SHORT).show()
            Log.e("DB_ERROR", "Insert failed")
            false
        } else {
            Toast.makeText(context, "Insert OrderDetails Successful", Toast.LENGTH_SHORT).show()
            Log.d("DB_SUCCESS", "Insert OrderDetails successful with ID: $result")
            true
        }
    }



    @RequiresApi(Build.VERSION_CODES.O)
    fun readData(orderID: Int? = null): MutableList<OrderDetails> {
        val list: MutableList<OrderDetails> = ArrayList()
        val db = dbHelper.readableDatabase
        val query = if (orderID != null) {
            "SELECT * FROM OrderDetails WHERE orderID = ?"
        } else {
            "SELECT * FROM OrderDetails"
        }
        val result = if (orderID != null) {
            db.rawQuery(query, arrayOf(orderID.toString()))
        } else {
            db.rawQuery(query, null)
        }

        if (result.moveToFirst()) {
            do {
                val orderID = result.getInt(result.getColumnIndexOrThrow("orderID"))
                val productID = result.getInt(result.getColumnIndexOrThrow("productID"))
                val quantity = result.getInt(result.getColumnIndexOrThrow("quantity"))


                val orderDetail = OrderDetails(orderID, productID, quantity)
                list.add(orderDetail)
            } while (result.moveToNext())
        } else {
            Log.i("DB_INFO", "No orders found in database.")
        }

        result.close()
        db.close()
        return list
    }

    fun updateData(orderDetails: OrderDetails): Boolean {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("quantity", orderDetails.quantity)
        }

        val result = db.update(
            "OrderDetails",
            cv,
            "orderID = ? AND productID = ?",
            arrayOf(orderDetails.orderID.toString(), orderDetails.productID.toString())
        )

        db.close()

        return if (result > 0) {
            Toast.makeText(context, "Update Successful", Toast.LENGTH_SHORT).show()
            Log.d("DB_UPDATE", "Updated OrderDetails (orderID: ${orderDetails.orderID}, productID: ${orderDetails.productID}) to quantity: $${orderDetails.quantity}")
            true
        } else {
            Toast.makeText(context, "Update Failed", Toast.LENGTH_SHORT).show()
            Log.e("DB_ERROR", "Update failed for orderID: ${orderDetails.orderID}, productID: ${orderDetails.productID}")
            false
        }
    }




    fun deleteData(orderID: Int): Boolean {
        val db = dbHelper.writableDatabase
        val result = db.delete("OrderDetails", "orderID = ?", arrayOf(orderID.toString()))
        db.close()

        return if (result > 0) {
            Toast.makeText(context, "Delete Successful", Toast.LENGTH_SHORT).show()
            true
        } else {
            Toast.makeText(context, "Delete Failed", Toast.LENGTH_SHORT).show()
            false
        }
    }


}

