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
import com.example.ordertrackingapp.databases.Tables.Products
import java.time.LocalDate

class ProductsHandler (var context: Context) : SQLiteOpenHelper(context,"FoodStopDB",null,2){
    /*fun createTable() {
        val db = writableDatabase
        val createProductTableQuery = """
        CREATE TABLE IF NOT EXISTS Products (
            productID INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            price REAL NOT NULL,
            stock INTEGER NOT NULL
        )
    """.trimIndent()
        db.execSQL(createProductTableQuery)
    }*/

    override fun onCreate(db: SQLiteDatabase?){
        val createTable = "CREATE TABLE Products (" +
                "productID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "productName TEXT NOT NULL," +
                "price INT NOT NULL)"

        db?.execSQL(createTable)

        val insertDummyProduct = "INSERT INTO Products (productID, productName, price) " +
                "VALUES (1, 'Sample Dish', 200)"
        db?.execSQL(insertDummyProduct)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS Products")
        onCreate(db)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun insertData(products: Products): Boolean{
        val db = this.writableDatabase
        val cv = ContentValues().apply {
            put("productName", products.Product_name)
            put("price", products.Price)
        }
        val result = db.insert("Products", null,cv)
        db.close()

        return if (result == -1L){
            Toast.makeText(context, "Insert Failed", Toast.LENGTH_SHORT).show()
            false
        } else {
            Toast.makeText(context, "Insert Successful", Toast.LENGTH_SHORT).show()
            true
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun readData(productID: Int? = null): MutableList<Products> {
        val list: MutableList<Products> = ArrayList()
        val db = this.readableDatabase
        val query = if (productID != null) {
            "SELECT * FROM Products WHERE productID = ?"
        } else {
            "SELECT * FROM Products"
        }
        val result = if (productID != null) {
            db.rawQuery(query, arrayOf(productID.toString()))
        } else {
            db.rawQuery(query, null)
        }

        if (result.moveToFirst()) {
            do {
                // Prevent column index errors
                val productIDIndex = result.getColumnIndex("productID")
                val productNameIndex = result.getColumnIndex("productName")
                val priceIndex = result.getColumnIndex("price")

                // Only set values if the column exists
                if (productIDIndex == -1 || productNameIndex == -1 || priceIndex == -1) {
                    Log.e("DB_ERROR", "One or more required columns are missing!")
                    continue
                }
                val prodID = result.getInt(productIDIndex)
                val prodName = result.getString(productNameIndex)
                val price = result.getInt(priceIndex)




                val product = Products(prodID, prodName, price)
                list.add(product)

            } while (result.moveToNext())
        } else {
            Log.i("DB_INFO", "No orders found in database.")
        }

        result.close()
        db.close()
        return list
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateData(products: Products): Int{
        val db = this.writableDatabase
        val cv = ContentValues()

       cv.put("productName", products.Product_name)
        cv.put("price", products.Price)

        val result = db.update("Products", cv, "productID = ?", arrayOf(products.Product_ID.toString()))
        db.close()
        return result
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun deleteData(productID: Int): Boolean {
        val db = this.writableDatabase
        val result = db.delete("Products", "productID = ?", arrayOf(productID.toString()))
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

