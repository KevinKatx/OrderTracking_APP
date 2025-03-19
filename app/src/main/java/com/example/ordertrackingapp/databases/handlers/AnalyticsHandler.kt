package com.example.ordertrackingapp.databases.handlers

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import com.example.ordertrackingapp.databases.DatabaseHelper
import com.example.ordertrackingapp.databases.Tables.OrderDetails
import com.example.ordertrackingapp.databases.Tables.Products
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class AnalyticsHandler(private val db: SQLiteDatabase) {

    fun getAnalyticsSummary(startDate:MutableState<String>, endDate:MutableState<String>): String {
        // Get first and last day of the current month
        val topOrders = getTopOrders(startDate, endDate)
        val ordersPerDay = getOrdersCount(startDate.value, endDate.value)
        val boughtTogether = freqBoughtTogether(startDate, endDate)
        val productPrices = getItemPrices().joinToString("\n") { "${it.first}: ₱${it.second}" }

        val topOrdersText = topOrders.joinToString("\n") { "${it.first}: ${it.second} orders" }
        val ordersPerDayText = ordersPerDay.toString()
        val boughtTogetherText = boughtTogether.joinToString("\n") { "${it.first} & ${it.second}" }

        return """
        Order Analytics Summary (From ${startDate.value} to ${endDate.value}):
        🔹 **Top Ordered Items**:
        $topOrdersText
        
        🔹 **Total Orders**:
        $ordersPerDayText
        
        🔹 **Frequently Bought Together**:
        $boughtTogetherText
        
        🔹 **ItemPrices**:
        $productPrices
    """.trimIndent()
    }



    fun getItemPrices(): List<Pair<String, Double>> {
        val itemPrices = mutableListOf<Pair<String, Double>>()

        val query = "SELECT productName, price FROM Products"  // Adjust based on your DB schema
        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()) {
            val itemName = cursor.getString(0)
            val price = cursor.getDouble(1)
            itemPrices.add(itemName to price)
        }

        cursor.close()

        return itemPrices
    }


    fun freqBoughtTogether(startDateState: MutableState<String>, endDateState: MutableState<String>): MutableList<Pair<String, String>> {
        val productPairs = mutableListOf<Pair<String, String>>()
        val startDate = startDateState.value
        val endDate = endDateState.value

        Log.d("DEBUG", "Fetching top orders between: $startDate and $endDate")
        val query = """
            SELECT p1.productName AS Product1, p2.productName AS Product2, COUNT(*) AS Frequency
            FROM OrderDetails od1
            JOIN OrderDetails od2 
                ON od1.orderID = od2.orderID 
                AND od1.productID < od2.productID
            JOIN Orders o ON od1.orderID = o.orderID
            JOIN Products p1 ON od1.productID = p1.productID
            JOIN Products p2 ON od2.productID = p2.productID
            WHERE o.orderDate BETWEEN ? AND ?
            GROUP BY p1.productName, p2.productName
            HAVING COUNT(*) > 1
            ORDER BY Frequency DESC
        """

        val cursor = db.rawQuery(query, arrayOf(startDate, endDate))

        if (cursor.moveToFirst()) {
            do {
                val product1 = cursor.getString(cursor.getColumnIndexOrThrow("Product1"))
                val product2 = cursor.getString(cursor.getColumnIndexOrThrow("Product2"))
                Log.d("DEBUG", "Bought Together: $product1 & $product2")
                productPairs.add(Pair(product1, product2))
            } while (cursor.moveToNext())
        }

        cursor.close()
        return productPairs
    }


    fun getOrdersCount(startDate: String, endDate: String): List<Pair<String, Int>> {
        val orderCounts = mutableListOf<Pair<String, Int>>()
        val query = """
            SELECT OrderDate, COUNT(orderID) as orderCount 
            FROM Orders 
            WHERE OrderDate BETWEEN ? AND ? 
            GROUP BY OrderDate 
            ORDER BY OrderDate ASC
        """
        val cursor = db.rawQuery(query, arrayOf(startDate, endDate))

        while (cursor.moveToNext()) {
            val date = cursor.getString(0)
            val count = cursor.getInt(1)
            orderCounts.add(Pair(date, count))
        }
        cursor.close()
        return orderCounts
    }

    // Get top 5 most frequently ordered products in a date range
    fun getTopOrders(startDateState: MutableState<String>, endDateState: MutableState<String>): List<Pair<String, Int>> {
        val topOrders = mutableListOf<Pair<String, Int>>()

        val startDate = startDateState.value
        val endDate = endDateState.value
        Log.d("DEBUG", "Fetching top orders between: $startDate and $endDate")
        val query = """
            SELECT p.productName, SUM(od.quantity) as totalQuantity 
            FROM OrderDetails od
            JOIN Orders o ON od.orderID = o.orderID
            JOIN Products p ON od.productID = p.productID
            WHERE o.OrderDate BETWEEN ? AND ?
            GROUP BY od.productID
            ORDER BY totalQuantity DESC
            LIMIT 5;
        """
        val cursor = db.rawQuery(query, arrayOf(startDate, endDate))
        Log.d("DEBUG", "Top Order Rows Found: ${cursor.count}")
        while (cursor.moveToNext()) {
            val productName = cursor.getString(0)
            val totalQuantity = cursor.getInt(1)
            Log.d("DEBUG", "Top Order: $productName - $totalQuantity")
            topOrders.add(Pair(productName, totalQuantity))
        }
        cursor.close()
        return topOrders
    }

    fun getOrdersCountByDateRange(startDate: String, endDate: String): List<Pair<String, Int>> {
        val ordersCountList = mutableListOf<Pair<String, Int>>()

        val query = """
            SELECT OrderDate, COUNT(orderID) as order_count 
            FROM Orders 
            WHERE OrderDate BETWEEN ? AND ? 
            GROUP BY OrderDate 
            ORDER BY OrderDate ASC
        """

        val cursor = db.rawQuery(query, arrayOf(startDate, endDate))

        while (cursor.moveToNext()) {
            val orderDate = cursor.getString(0)  // OrderDate
            val orderCount = cursor.getInt(1)    // Count of orders on that date
            ordersCountList.add(orderDate to orderCount)
        }

        cursor.close()
        return ordersCountList
    }







}
