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
    fun insertData(orderDetails: OrderDetails): Int {
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
            0
        } else {
            Toast.makeText(context, "Insert OrderDetails Successful", Toast.LENGTH_SHORT).show()
            Log.d("DB_SUCCESS", "Insert OrderDetails successful with ID: $result")
            1
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateData(orderDetails: OrderDetails): Int {
        val db = dbHelper.writableDatabase

        if (orderDetails.quantity == 0) {
            // If quantity is zero, remove the specific product from the order
            val deleteResult = db.delete(
                "OrderDetails",
                "orderID = ? AND productID = ?",
                arrayOf(orderDetails.orderID.toString(), orderDetails.productID.toString())
            )

            Toast.makeText(context, "Product removed from order", Toast.LENGTH_SHORT).show()
            db.close()
            return deleteResult
        }

        // Otherwise, update the existing order detail
        val values = ContentValues().apply {
            put("quantity", orderDetails.quantity) // Assuming there is a price column
        }

        val updateResult = db.update(
            "OrderDetails",
            values,
            "orderID = ? AND productID = ?",
            arrayOf(orderDetails.orderID.toString(), orderDetails.productID.toString())
        )

        // If no rows were updated, it means the product isn't in the order, so insert it
        if (updateResult == 0) {
            insertData(orderDetails)
        }

        db.close()
        return updateResult
    }







    fun deleteData(orderID: Int): Boolean {
        val db = dbHelper.writableDatabase
        val result = db.delete("OrderDetails", "orderID = ?", arrayOf(orderID.toString()))
        db.close()

        return if (result > 0) {
            true
        } else {
            false
        }
    }


}

