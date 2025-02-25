package com.example.ordertrackingapp.databases.handlers

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.Toast
import com.example.ordertrackingapp.databases.Tables.Customer


class CustomerHandler(var context: Context) : SQLiteOpenHelper(context,"FoodStopDB",null,2) {
    /*fun createTable() {
        val db = writableDatabase
        val createCustomerTableQuery = """
        CREATE TABLE Customers (
            customerID INTEGER PRIMARY KEY AUTOINCREMENT, 
            name TEXT, 
            type TEXT, 
            address TEXT
        )
    """.trimIndent()

        db.execSQL(createCustomerTableQuery)
    }*/
    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = "CREATE TABLE Customers (" +
                "customerID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "type TEXT, " +
                "address TEXT)"

        db?.execSQL(createTable)

        val insertDummyCustomer = "INSERT INTO Customers (customerID, name, type, address) " +
                "VALUES (32, 'John Doe', 'Regular', '123 Main St')"

        db?.execSQL(insertDummyCustomer)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS Customers")
        onCreate(db)
    }


    fun insertData(customer: Customer): Boolean{
        val db = this.writableDatabase
        val cv = ContentValues().apply(){
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
            "SELECT * FROM Customers"
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
                val nameIndex = result.getColumnIndex("name")
                val typeIndex = result.getColumnIndex("type")
                val addressIndex = result.getColumnIndex("address")



                Log.d("DB_DEBUG", "name index: $nameIndex")
                Log.d("DB_DEBUG", "type index: $typeIndex")
                Log.d("DB_DEBUG", "address index: $addressIndex")


                if (customerIDIndex == -1 ||  nameIndex == -1 || typeIndex == -1||
                    addressIndex == -1) {

                    if (nameIndex == -1) Log.e("DB_ERROR", "Column 'name' is missing!")
                    if (typeIndex == -1) Log.e("DB_ERROR", "Column 'type' is missing!")
                    if (addressIndex == -1) Log.e("DB_ERROR", "Column 'address' is missing!")
                    continue
                }



                val customerID = result.getInt(customerIDIndex)
                val name = result.getString(nameIndex)
                val type = result.getString(typeIndex)
                val address = result.getString(addressIndex)



                val customer = Customer(customerID, name, type, address)


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
        cv.put("name", customer.Name)
        cv.put("type", customer.Type)
        cv.put("address", customer.Address)



        // Updating the row where orderID matches
        val result = db.update("Customers", cv, "customerID = ?", arrayOf(customer.Customer_ID.toString()))
        db.close()
        return result // Returns number of rows affected
    }

    fun deleteData(customerID: Int): Boolean {
        val db = this.writableDatabase
        val result = db.delete("Customers", "customerID = ?", arrayOf(customerID.toString()))
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