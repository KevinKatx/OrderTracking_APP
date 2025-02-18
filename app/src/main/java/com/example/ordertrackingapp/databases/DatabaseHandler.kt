package com.example.ordertrackingapp.databases

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

const val DATABASE_NAME = "FoodStopDB"

class DatabaseHandler(
    context: Context,
    private val tableName: String,
    private val cols: Array<String>
    ):SQLiteOpenHelper(context, DATABASE_NAME, null, 1){


    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = createTableQuery()
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }

    private fun createTableQuery(): String {
        return "CREATE TABLE $tableName (" + tableName +
                "_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                cols.joinToString(", ") { "$it TEXT" } +
                ");"
    }
}