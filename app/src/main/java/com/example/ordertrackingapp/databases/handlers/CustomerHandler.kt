package com.example.ordertrackingapp.databases.handlers

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import android.widget.Toast
import com.example.ordertrackingapp.databases.DatabaseHelper
import com.example.ordertrackingapp.databases.Tables.Customer

class CustomerHandler(private val context: Context) {

    private val dbHelper = DatabaseHelper(context)

    fun insertData(customer: Customer): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("name", customer.Name)
            put("type", customer.Type)
            put("address", customer.Address)
        }

        val result = db.insert("Customers", null, values)
        db.close()

        return if (result == -1L) {
            Toast.makeText(context, "Insert Failed", Toast.LENGTH_SHORT).show()
            false
        } else {
            Toast.makeText(context, "Insert Successful", Toast.LENGTH_SHORT).show()
            true
        }
    }

    fun readData(customerID: Int? = null): MutableList<Customer> {
        val customers = mutableListOf<Customer>()
        val db = dbHelper.readableDatabase
        val query = if (customerID != null) {
            "SELECT * FROM Customers WHERE customerID = ?"
        } else {
            "SELECT * FROM Customers"
        }

        val cursor = if (customerID != null) {
            db.rawQuery(query, arrayOf(customerID.toString()))
        } else {
            db.rawQuery(query, null)
        }

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndexOrThrow("customerID"))
                val name = it.getString(it.getColumnIndexOrThrow("name"))
                val type = it.getString(it.getColumnIndexOrThrow("type"))
                val address = it.getString(it.getColumnIndexOrThrow("address"))
                customers.add(Customer(id, name, type, address))
            }
        }
        db.close()
        return customers
    }

    fun updateData(customer: Customer): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("name", customer.Name)
            put("type", customer.Type)
            put("address", customer.Address)
        }

        val rowsAffected = db.update("Customers", values, "customerID = ?", arrayOf(customer.Customer_ID.toString()))
        db.close()
        return rowsAffected
    }

    fun deleteData(customerID: Int): Boolean {
        val db = dbHelper.writableDatabase
        val rowsDeleted = db.delete("Customers", "customerID = ?", arrayOf(customerID.toString()))
        db.close()

        return if (rowsDeleted > 0) {
            Toast.makeText(context, "Delete Successful", Toast.LENGTH_SHORT).show()
            true
        } else {
            Toast.makeText(context, "Delete Failed", Toast.LENGTH_SHORT).show()
            false
        }
    }
}