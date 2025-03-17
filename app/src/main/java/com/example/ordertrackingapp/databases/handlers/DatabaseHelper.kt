package com.example.ordertrackingapp.databases

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "FoodStopDB", null, 1) {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(
            "CREATE TABLE Orders (" +
                    "orderID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "customerID INTEGER, " +
                    "TotalPrice FLOAT, " +
                    "PromoID INTEGER, " +
                    "Status TEXT CHECK(Status IN ('Pending', 'Completed', 'Cancelled')), " +
                    "OrderDate TEXT DEFAULT (date('now')), " +
                    "PaymentType TEXT CHECK(PaymentType IN ('Cash On Delivery', 'GCash', 'Credit Card')), " +
                    "FOREIGN KEY(customerID) REFERENCES Customers(customerID), " +
                    "FOREIGN KEY(PromoID) REFERENCES Promos(promo_ID)" +
                    ")"
        )

        db?.execSQL(
            "CREATE TABLE Delivery(" +
                    "deliveryID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "orderID INTEGER, " +
                    "deliveryDate TEXT DEFAULT (date('now')), " +
                    "deliveryStart TEXT, " +
                    "deliveryEnd TEXT, " +
                    "status TEXT CHECK(status IN ('Pending', 'In Transit', 'Delivered', 'Cancelled'))," +
                    "FOREIGN KEY(orderID) REFERENCES Orders(orderID)" +
                    ")"
        )

        db?.execSQL(
            "CREATE TABLE OrderDetails (" +
                    "orderID INTEGER, " +
                    "productID INTEGER, " +
                    "quantity INTEGER NOT NULL DEFAULT 1, " +
                    "PRIMARY KEY(orderID, productID), " +
                    "FOREIGN KEY(orderID) REFERENCES Orders(orderID) ON DELETE CASCADE, " +
                    "FOREIGN KEY(productID) REFERENCES Products(prod_ID) ON DELETE CASCADE" +
                    ")"
        )

        db?.execSQL(
            "CREATE TABLE IF NOT EXISTS Promos (" +
                    "promo_ID INTEGER PRIMARY KEY AUTOINCREMENT, " + "name TEXT, " +
                    "type TEXT, discount_percent INTEGER, discount_flat INTEGER)"
        )

        db?.execSQL(
            "CREATE TABLE Customers (" +
                    "customerID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT, " +
                    "type TEXT, " +
                    "address TEXT)"
        )

        db?.execSQL(
            "CREATE TABLE Products (" +
                    "productID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "productName TEXT NOT NULL," +
                    "price INT NOT NULL)"
        )

//        db?.execSQL(
//            "CREATE TABLE Delivery (" +
//                    "deliveryID INTEGER PRIMARY KEY AUTOINCREMENT, " +
//                    "orderID INTEGER, " +
//                    "deliveryStart DATETIME, " +
//                    "deliveryEnd DATETIME, " +
//                    "deliveryDate TEXT DEFAULT (date('now')), " +
//                    "status TEXT CHECK(status IN ('Pending', 'Out for Delivery', 'Delivered', 'Cancelled')), " +
//                    "FOREIGN KEY(orderID) REFERENCES Orders(orderID)" +
//                    ")"
//        )

        // New Users table for authentication
        db?.execSQL(
            "CREATE TABLE Users (" +
                    "userID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT UNIQUE NOT NULL, " +
                    "password TEXT NOT NULL, " +
                    "email TEXT, " +
                    "isAdmin INTEGER DEFAULT 0" +  // 0 for false, 1 for true
                    ")"
        )

        // System passkey table
        db?.execSQL(
            "CREATE TABLE SystemConfig (" +
                    "configKey TEXT PRIMARY KEY, " +
                    "configValue TEXT NOT NULL" +
                    ")"
        )

        // Insert default admin user and passkey
        db?.execSQL("INSERT INTO Users (username, password, email, isAdmin) VALUES ('admin', 'Password', 'admin@foodstop.com', 1)")
        db?.execSQL("INSERT INTO SystemConfig (configKey, configValue) VALUES ('registration_passkey', 'M4pUaUN1v3RsltY')")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS OrderDetails")
        db?.execSQL("DROP TABLE IF EXISTS Orders")
        db?.execSQL("DROP TABLE IF EXISTS Promos")
        db?.execSQL("DROP TABLE IF EXISTS Customers")
        db?.execSQL("DROP TABLE IF EXISTS Products")
        db?.execSQL("DROP TABLE IF EXISTS Delivery")
        db?.execSQL("DROP TABLE IF EXISTS Delivery")
        onCreate(db)
        if (oldVersion < newVersion) {
            // Add new tables for users and system config if upgrading
            db?.execSQL("CREATE TABLE IF NOT EXISTS Users (" +
                    "userID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT UNIQUE NOT NULL, " +
                    "password TEXT NOT NULL, " +
                    "email TEXT, " +
                    "isAdmin INTEGER DEFAULT 0" +
                    ")")

            db?.execSQL("CREATE TABLE IF NOT EXISTS SystemConfig (" +
                    "configKey TEXT PRIMARY KEY, " +
                    "configValue TEXT NOT NULL" +
                    ")")

            // Insert default admin user and passkey if tables were just created
            db?.execSQL("INSERT OR IGNORE INTO Users (username, password, email, isAdmin) VALUES ('admin', 'Password', 'admin@foodstop.com', 1)")
            db?.execSQL("INSERT OR IGNORE INTO SystemConfig (configKey, configValue) VALUES ('registration_passkey', 'M4pUaUN1v3RsltY')")
      }
    }

    override fun onDowngrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS OrderDetails")
        db?.execSQL("DROP TABLE IF EXISTS Orders")
        db?.execSQL("DROP TABLE IF EXISTS Promos")
        db?.execSQL("DROP TABLE IF EXISTS Customers")
        db?.execSQL("DROP TABLE IF EXISTS Products")
        db?.execSQL("DROP TABLE IF EXISTS Delivery")
        db?.execSQL("DROP TABLE IF EXISTS Users")
        db?.execSQL("DROP TABLE IF EXISTS SystemConfig")
        onCreate(db)
    }
    companion object {
        @Volatile
        private var instance: DatabaseHelper? = null

        fun getInstance(context: Context): DatabaseHelper {
            return instance ?: synchronized(this) {
                instance ?: DatabaseHelper(context.applicationContext).also { instance = it }
            }
        }
    }
}
