package com.example.ordertrackingapp.databases.handlers

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.ordertrackingapp.databases.DatabaseHelper
import com.example.ordertrackingapp.databases.Tables.Delivery
import java.time.LocalDate
import java.time.LocalTime

class DeliveryHandler (private val context: Context){
    private val dbHelper = DatabaseHelper(context)

    @RequiresApi(Build.VERSION_CODES.O)
    fun insertData(delivery: Delivery): Boolean {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("deliveryID", delivery.Delivery_ID)
            put("orderID", delivery.Order_ID)
            put("deliveryDate", delivery.deliveryDate.toString())
            put("deliveryStart", delivery.deliveryStart.toString())
            put("deliveryEnd", delivery.deliveryEnd.toString())
            put("status", delivery.status)
        }

        val result = db.insert("Delivery", null, cv)
        db.close()

        Log.d("DB_INSERT", "Insert Delivery result: $result")
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
    fun readData(deliveryID: Int? = null): MutableList<Delivery>{
        val list: MutableList<Delivery> = ArrayList()
        val db = dbHelper.readableDatabase
        val query = if (deliveryID != null) {
            "SELECT * FROM Delivery WHERE deliveryID = ?"
        } else {
            "SELECT * FROM Delivery"
        }
        val result = if (deliveryID != null) {
            db.rawQuery(query, arrayOf(deliveryID.toString()))
        } else {
            db.rawQuery(query, null)
        }
        if(result.moveToFirst()){
            do {
                val deliveryID = result.getInt(result.getColumnIndexOrThrow("deliveryID"))
                val orderID = result.getInt(result.getColumnIndexOrThrow("orderID"))
                val deliveryDateString = result.getString(result.getColumnIndexOrThrow("deliveryDate"))
                val deliveryDate = try {
                    LocalDate.parse(deliveryDateString)
                } catch (e: Exception) {
                    Log.e("DB_ERROR", "Failed to parse date: $deliveryDateString", e)
                    LocalDate.now()
                }
                val deliveryStartString = result.getString(result.getColumnIndexOrThrow("deliveryStart"))
                val deliveryStart = try {
                    LocalTime.parse(deliveryStartString)
                } catch (e: Exception) {
                    Log.e("DB_ERROR", "Failed to parse time: $deliveryStartString", e)
                    LocalTime.now()
                }
                val deliveryEndString = result.getString(result.getColumnIndexOrThrow("deliveryEnd"))
                val deliveryEnd = try {
                    LocalTime.parse(deliveryEndString)
                } catch (e:Exception){
                    Log.e("DB_ERROR", "Failed to parse time: $deliveryEndString", e)
                    LocalTime.now()
                }
                val status = result.getString(result.getColumnIndexOrThrow("status"))

                val delivery = Delivery(deliveryID, orderID, deliveryDate, deliveryStart, deliveryEnd, status)
                list.add(delivery)
            } while(result.moveToNext())
        } else {
            Log.i("DB_INFO", "No deliveries found in database.")
        }

        result.close()
        db.close()
        return list
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun UpdateData(delivery: Delivery): Int{
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply{
            put("deliveryID", delivery.Delivery_ID)
            put("orderID", delivery.Order_ID)
            put("deliveryDate", delivery.deliveryDate.toString())
            put("deliveryStart", delivery.deliveryStart.toString())
            put("deliveryEnd", delivery.deliveryEnd.toString())
            put("status", delivery.status)
        }
        val result = db.update("Delivery", cv, "deliveryID = ?", arrayOf(delivery.Delivery_ID.toString()))
        db.close()
        return result
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun deleteData(deliveryID: Int): Boolean {
        val db = dbHelper.writableDatabase
        val result = db.delete("Delivery", "deliveryID = ?", arrayOf(deliveryID.toString()))
        db.close()
        return if (result>0) {
            Toast.makeText(context, "Delete Delivery Successful", Toast.LENGTH_SHORT).show()
            true
        } else {
            Toast.makeText(context, "Delete Delivery Failed", Toast.LENGTH_SHORT).show()
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun insertDelivery(orderID: Int) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("orderID", orderID)
            put("status", "Pending") // Default status for new deliveries
            put("deliveryDate", LocalDate.now().toString()) // Example: today's date
        }
        db.insert("Delivery", null, values)
        db.close()
    }

    fun deleteDeliveryByOrderID(orderID: Int) {
        val db = dbHelper.writableDatabase
        db.delete("Delivery", "orderID = ?", arrayOf(orderID.toString()))
        db.close()
    }

    fun deliveryExistsForOrder(orderID: Int): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM Delivery WHERE orderID = ?", arrayOf(orderID.toString()))
        val exists = if (cursor.moveToFirst()) cursor.getInt(0) > 0 else false
        cursor.close()
        db.close()
        return exists
    }

}