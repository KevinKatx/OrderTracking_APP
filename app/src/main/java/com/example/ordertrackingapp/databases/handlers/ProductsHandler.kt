package com.example.ordertrackingapp.databases.handlers

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import android.widget.Toast
import com.example.ordertrackingapp.databases.DatabaseHelper

import com.example.ordertrackingapp.databases.Tables.Products

class ProductsHandler(private val context: Context) {

    private val dbHelper = DatabaseHelper(context)

    fun insertData(product: Products): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("productName", product.Product_name)
            put("price", product.Price)
        }

        val result = db.insert("Products", null, values)
        db.close()

        return if (result == -1L) {
            Toast.makeText(context, "Insert Failed", Toast.LENGTH_SHORT).show()
            false
        } else {
            Toast.makeText(context, "Insert Successful", Toast.LENGTH_SHORT).show()
            true
        }
    }

    fun readData(productID: Int? = null): List<Products> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<Products>()
        val query = if (productID != null) "SELECT * FROM Products WHERE productID = ?" else "SELECT * FROM Products"

        db.rawQuery(query, productID?.let { arrayOf(it.toString()) }).use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow("productID")
            val nameIndex = cursor.getColumnIndexOrThrow("productName")
            val priceIndex = cursor.getColumnIndexOrThrow("price")

            while (cursor.moveToNext()) {
                list.add(
                    Products(
                        cursor.getInt(idIndex),
                        cursor.getString(nameIndex),
                        cursor.getInt(priceIndex)
                    )
                )
            }
        }

        db.close()
        return list
    }

    fun updateData(product: Products): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("productName", product.Product_name)
            put("price", product.Price)
        }

        val result = db.update("Products", values, "productID = ?", arrayOf(product.Product_ID.toString()))
        db.close()
        return result
    }

    fun deleteData(productID: Int): Boolean {
        val db = dbHelper.writableDatabase
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
