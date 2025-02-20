package com.example.ordertrackingapp.databases.handlers

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.ordertrackingapp.databases.Tables.Customer
import com.example.ordertrackingapp.databases.Tables.Order
import java.time.LocalDate


class CustomerHandler(var context: Context) : SQLiteOpenHelper(context,"FoodStopDB",null,1) {
    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = "CREATE TABLE Customers (" +
                "customerID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "CompletedOrder TEXT, " +
                "CurrentOrder INTEGER, " +
                "name TEXT, " +
                "type TEXT, " +
                "address TEXT)"

        db?.execSQL(createTable)

        val insertDummyOrder = "INSERT INTO Customers (CompletedOrder, CurrentOrder, name, type, address) " +
                "VALUES ('1, 2, 3', 4, 'lol', 'Active', '0007 Credit Card St., Metro Manila, Manila ')"

        db?.execSQL(insertDummyOrder)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS Customers")
        onCreate(db)
    }

   
    fun insertData(customer: Customer): Boolean{
        val db = this.writableDatabase

        val cv = ContentValues().apply(){
            put("CompletedOrder", customer.CompletedOrder)
            put("CurrentOrder", customer.CurrentOrder)
            put("name", customer.Name)
            put("type", customer.Type)
            put("address", customer.Address)

        }

        val result = db.insert("Customers", null, cv)
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
        val list: MutableList<Customer> = ArrayList()
        val db = this.readableDatabase
        val query = if (customerID != null) {
            "SELECT * FROM Customers WHERE customerID = ?"
        } else {
            "SELECT * FROM Orders"
        }
        val result = if (customerID != null) {
            db.rawQuery(query, arrayOf(customerID.toString()))
        } else {
            db.rawQuery(query, null)
        }

        if (result.moveToFirst()) {
            do {
                // Ensure columns match exactly
                val customerIDIndex = result.getColumnIndex("customerID")
                val completedOrderIndex = result.getColumnIndex("CompletedOrder")
                val currentOrderIndex = result.getColumnIndex("CurrentOrder")
                val nameIndex = result.getColumnIndex("name")
                val typeIndex = result.getColumnIndex("type")
                val addressIndex = result.getColumnIndex("address")


                Log.d("DB_DEBUG", "completedOrder index: $completedOrderIndex")
                Log.d("DB_DEBUG", "currentOrder index: $currentOrderIndex")
                Log.d("DB_DEBUG", "name index: $nameIndex")
                Log.d("DB_DEBUG", "type index: $typeIndex")
                Log.d("DB_DEBUG", "address index: $addressIndex")


                if (customerIDIndex == -1 || completedOrderIndex == -1 || currentOrderIndex == -1 || nameIndex == -1 || typeIndex == -1||
                    addressIndex == -1) {

                    if (completedOrderIndex == -1) Log.e("DB_ERROR", "Column 'CompletedOrder' is missing!")
                    if (currentOrderIndex == -1) Log.e("DB_ERROR", "Column 'CurrentOrder' is missing!")
                    if (nameIndex == -1) Log.e("DB_ERROR", "Column 'name' is missing!")
                    if (typeIndex == -1) Log.e("DB_ERROR", "Column 'type' is missing!")
                    if (addressIndex == -1) Log.e("DB_ERROR", "Column 'address' is missing!")
                    continue
                }

                // Extract values
//                val customerID = result.getInt(customerIDIndex)
//                val totalPrice = result.getInt(totalPriceIndex)
//                val promoID = result.getInt(promoIDIndex)
//                val status = result.getString(statusIndex)
//                val paymentType = result.getString(paymentTypeIndex)

                val customerID = result.getInt(customerIDIndex)
                val completedOrder = result.getInt(completedOrderIndex)
                val currentOrder = result.getInt(currentOrderIndex)
                val name = result.getInt(nameIndex)
                val type = result.getInt(typeIndex)
                val address = result.getInt(addressIndex)



                val customer = Customer()


                list.add(customer)
            } while (result.moveToNext())
        } else {
            Log.i("DB_INFO", "No customers found in database.")
        }

        result.close()
        db.close()
        return list
    }


    fun updateData(customer: Customer): Int {
        val db = this.writableDatabase
        val cv = ContentValues()

        // Setting updated values
        cv.put("CompletedOrder", customer.CompletedOrder)
        cv.put("CurrentOrder", customer.CurrentOrder)
        cv.put("name", customer.Name)
        cv.put("type", customer.Type)
        cv.put("address", customer.Address)



        // Updating the row where orderID matches
        val result = db.update("Customers", cv, "customerID = ?", arrayOf(customer.Customer_ID.toString()))
        db.close()
        return result // Returns number of rows affected
    }
}