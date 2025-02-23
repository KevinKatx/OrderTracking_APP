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
import java.time.LocalDate

class OrderHandler (var context: Context) : SQLiteOpenHelper(context,"FoodStopDB",null,1){

    init {
        ProductsHandler(context).createTable()
        CustomerHandler(context).createTable()
        PromosHandler(context).createTable()
    }

    override fun onCreate(db: SQLiteDatabase?){
        val createOrdersTable = "CREATE TABLE Orders (" +
                "orderID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "customerID INTEGER, " +
                "TotalPrice FLOAT, " +
                "PromoID INTEGER, " +
                "Status TEXT, " +
                "OrderDate TEXT DEFAULT (date('now')), " +
                "PaymentType TEXT, " +
                "FOREIGN KEY(customerID) REFERENCES Customers(customerID), " +
                "FOREIGN KEY(PromoID) REFERENCES Promos(promo_ID)" +
                ")"

        val createOrderDetailsTable = "CREATE TABLE OrderDetails (" +
                "orderID INTEGER, " +
                "productID INTEGER, " +
                "quantity INTEGER NOT NULL DEFAULT 1, " +
                "PRIMARY KEY(orderID, productID), " +
                "FOREIGN KEY(orderID) REFERENCES Orders(orderID) ON DELETE CASCADE, " +
                "FOREIGN KEY(productID) REFERENCES Products(prod_ID) ON DELETE CASCADE" +
                ")"

        db?.execSQL(createOrdersTable)
        db?.execSQL(createOrderDetailsTable)

        val insertDummyOrder = "INSERT INTO Orders (orderID, customerID, TotalPrice, PromoID, Status, PaymentType) " +
                "VALUES (4201337, 4201337, 100, 4201337, 'Pending', 'Credit Card')"
        db?.execSQL(insertDummyOrder)

        val insertDummyOrderDetails = "INSERT INTO OrderDetails (orderID, productID, quantity) " +
                "VALUES (4201337, 4201337, 10)"
        db?.execSQL(insertDummyOrderDetails)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS Orders")
        onCreate(db)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun insertData(order : Order): Boolean {
        val db = this.writableDatabase
        val cv = ContentValues().apply {
            put("productID", order.productID)
            put("customerID", order.customerID)
            put("TotalPrice", order.totalPrice)
            put("PromoID", order.promoID)
            put("Status", order.status)
            put("OrderDate", order.orderDate.toString()) // Ensure it's stored as a string
            put("PaymentType", order.paymentType)
        }

        val result = db.insert("Orders", null, cv)
        db.close()

        return if (result == -1L) {
            Toast.makeText(context, "Insert Failed", Toast.LENGTH_SHORT).show()
            false
        } else {
            Toast.makeText(context, "Insert Successful", Toast.LENGTH_SHORT).show()
            true
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun readData(orderID: Int? = null): MutableList<Order> {
        val list: MutableList<Order> = ArrayList()
        val db = this.readableDatabase
        val query = if (orderID != null) {
            "SELECT * FROM Orders WHERE orderID = ?"
        } else {
            "SELECT * FROM Orders"
        }
        val result = if (orderID != null) {
            db.rawQuery(query, arrayOf(orderID.toString()))
        } else {
            db.rawQuery(query, null)
        }

        if (result.moveToFirst()) {
            do {
                val orderIDIndex = result.getColumnIndex("orderID")
                val customerIDIndex = result.getColumnIndex("customerID")
                val totalPriceIndex = result.getColumnIndex("TotalPrice")
                val promoIDIndex = result.getColumnIndex("PromoID")
                val statusIndex = result.getColumnIndex("Status")
                val orderDateIndex = result.getColumnIndex("OrderDate")
                val paymentTypeIndex = result.getColumnIndex("PaymentType")

                if (orderIDIndex == -1 || customerIDIndex == -1 || totalPriceIndex == -1 ||
                    promoIDIndex == -1 || statusIndex == -1 || orderDateIndex == -1 || paymentTypeIndex == -1) {
                    Log.e("DB_ERROR", "One or more required columns are missing!")
                    continue
                }

                val orderID = result.getInt(orderIDIndex)
                val customerID = result.getInt(customerIDIndex)
                val totalPrice = result.getFloat(totalPriceIndex)
                val promoID = result.getInt(promoIDIndex)
                val status = result.getString(statusIndex)
                val paymentType = result.getString(paymentTypeIndex)

                val orderDateString = result.getString(orderDateIndex)
                val orderDate = try {
                    LocalDate.parse(orderDateString)
                } catch (e: Exception) {
                    Log.e("DB_ERROR", "Failed to parse date: $orderDateString", e)
                    LocalDate.now()
                }

                val order = Order(orderID, customerID, totalPrice, promoID, status, orderDate, paymentType)
                list.add(order)
            } while (result.moveToNext())
        } else {
            Log.i("DB_INFO", "No orders found in database.")
        }

        result.close()
        db.close()
        return list
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateData(order: Order): Int {
        val db = this.writableDatabase
        val cv = ContentValues()

        // Setting updated values
        cv.put("productID", order.productID)
        cv.put("customerID", order.customerID)
        cv.put("TotalPrice", order.totalPrice)
        cv.put("PromoID", order.promoID)
        cv.put("Status", order.status)
        cv.put("OrderDate", order.orderDate.toString()) // Convert LocalDate to String
        cv.put("PaymentType", order.paymentType)

        // Updating the row where orderID matches
        val result = db.update("Orders", cv, "orderID = ?", arrayOf(order.orderID.toString()))

        db.close()
        return result // Returns number of rows affected
    }
}