package com.example.ordertrackingapp.databases.handlers

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import com.example.ordertrackingapp.databases.DatabaseHelper
import com.example.ordertrackingapp.databases.Tables.Products
import com.example.ordertrackingapp.databases.Tables.Promos

class PromosHandler(private val context: Context) {

    private val dbHelper = DatabaseHelper(context)

    fun readData(promoID: Int? =null): List<Promos>{
        val db = dbHelper.readableDatabase
        val list = mutableListOf<Promos>()
        val query = if (promoID != null) "SELECT * FROM Promos WHERE promo_ID = ?" else "SELECT * FROM Promos"

        db.rawQuery(query, promoID?.let { arrayOf(it.toString()) }).use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow("promo_ID")
            val nameIndex = cursor.getColumnIndexOrThrow("name")
            val typeIndex = cursor.getColumnIndexOrThrow("type")
            val dcpercentIndex = cursor.getColumnIndexOrThrow("discount_percent")
            val dcflatIndex = cursor.getColumnIndexOrThrow("discount_flat")

            while (cursor.moveToNext()) {
                list.add(
                    Promos(
                        cursor.getInt(idIndex),
                        cursor.getString(nameIndex),
                        cursor.getString(typeIndex),
                        cursor.getInt(dcpercentIndex),
                        cursor.getInt(dcflatIndex)
                    )
                )
            }
        }

        db.close()
        return list
    }

    fun insertData(promo: Promos): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("name", promo.Name)
            put("type", promo.Type)
            put("discount_percent", promo.DiscountPercent)
            put("discount_flat", promo.DiscountFlat)
        }

        val result = db.insert("Promos", null, values)
        db.close()

        return if (result == -1L) {
            Toast.makeText(context, "Insert Failed", Toast.LENGTH_SHORT).show()
            false
        } else {
            Toast.makeText(context, "Insert Successful", Toast.LENGTH_SHORT).show()
            true
        }
    }

    fun deleteData(promoID: Int): Boolean {
        val db = dbHelper.writableDatabase
        val result = db.delete("Promos", "promo_ID = ?", arrayOf(promoID.toString()))
        db.close()

        return if (result > 0) {
            Toast.makeText(context, "Delete Successful", Toast.LENGTH_SHORT).show()
            true
        } else {
            Toast.makeText(context, "Delete Failed", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun updateData(promo: Promos): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("name", promo.Name)
            put("type", promo.Type)
            put("discount_percent", promo.DiscountPercent)
            put("discount_flat", promo.DiscountFlat)
        }
        val result = db.update("Promos", values, "promo_ID = ?", arrayOf(promo.Promo_ID.toString()))
        db.close()
        return result
    }
}