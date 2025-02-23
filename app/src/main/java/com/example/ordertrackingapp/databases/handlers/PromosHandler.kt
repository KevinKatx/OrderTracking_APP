package com.example.ordertrackingapp.databases.handlers

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class PromosHandler(var context: Context) : SQLiteOpenHelper(context,"FoodStopDB",null,1){
    fun createTable() {
        val db = writableDatabase
        val createPromoTableQuery = """
        CREATE TABLE Promos (
            promoID INTEGER PRIMARY KEY AUTOINCREMENT,
            discount REAL NOT NULL,
            description TEXT NOT NULL
        )
    """.trimIndent()
        db.execSQL(createPromoTableQuery)
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = "CREATE TABLE IF NOT EXISTS Promos (" +
                "promo_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "type TEXT, discount_percent INTEGER, discount_flat INTEGER)"
        db?.execSQL(createTable)

        val insertDummyPromo = "INSERT INTO Promos (promo_ID, type, discount_percent, discount_flat) " +
                "VALUES (4201337, 'Percentage', 10, 0)"
        db?.execSQL(insertDummyPromo)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS Promos")
        onCreate(db)
    }


}