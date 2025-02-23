package com.example.ordertrackingapp.databases.handlers


import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.ordertrackingapp.databases.Tables.Products

class ProductsHandler (var context: Context) : SQLiteOpenHelper(context,"FoodStopDB",null,1){
    fun createTable() {
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
    }

    override fun onCreate(db: SQLiteDatabase?){
        val createTable = "CREATE TABLE Products (" +
                "prod_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "ingredient_ID INTEGER, " +
                "dish_name VARCHAR(255), " +
                "price FLOAT, " +
                "prod_name VARCHAR(255)," +
                "quantity INTEGER)"

        db?.execSQL(createTable)

        val insertDummyProduct = "INSERT INTO Products (prod_ID, ingredient_ID, dish_name, price, prod_name, quantity) " +
                "VALUES (4201337, 1, 'Sample Dish', 9.99, 'Sample Product', 10)"
        db?.execSQL(insertDummyProduct)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun insertData(products: Products): Boolean{
        val db = this.writableDatabase
        val cv = ContentValues().apply {
            put("prod_ID", products.Product_ID)
            put("ingredient_ID", products.Ingredient_ID)
            put("dish_name", products.Dish_Name)
            put("price", products.Price)
            put("prod_name", products.Product_Name)
            put("quantity", products.Quantity)
        }
        val result = db.insert("Products", null,cv)
        db.close()

        return if (result == -1L){
            Toast.makeText(context, "InsertFailed", Toast.LENGTH_SHORT).show()
            false
        } else {
            Toast.makeText(context, "Insert Successful", Toast.LENGTH_SHORT).show()
            true
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun readData(): MutableList<Products> {
        val list: MutableList<Products> = ArrayList()
        val db = this.readableDatabase
        val query = "SELECT * FROM Products"
        val result = db.rawQuery(query, null)

        if (result.moveToFirst()) {
            do {
                val product = Products()

                // Prevent column index errors
                val productID_Index = result.getColumnIndex("prod_ID")
                val ingredientID_Index = result.getColumnIndex("ingredient_ID")
                val DishName_Index = result.getColumnIndex("dish_name")
                val PriceIndex = result.getColumnIndex("price")
                val ProductName_Index = result.getColumnIndex("product_Name")
                val Quantity_Index = result.getColumnIndex("quantity")


                // Only set values if the column exists
                if (productID_Index != -1) product.Product_ID = result.getInt(productID_Index)
                if (ingredientID_Index != -1) product.Ingredient_ID = result.getInt(ingredientID_Index)
                if (DishName_Index != -1) product.Dish_Name = result.getString(DishName_Index)
                if (PriceIndex != -1) product.Price = result.getFloat(PriceIndex)
                if (ProductName_Index != -1) product.Product_Name = result.getString(ProductName_Index)
                if (Quantity_Index != -1) product.Quantity = result.getInt(Quantity_Index)

                list.add(product)
            }while(result.moveToNext())
        }
        result.close()
        db.close()
        return list
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateData(products: Products): Int{
        val db = this.writableDatabase
        val cv = ContentValues()

        cv.put("ingredient_ID", products.Ingredient_ID)
        cv.put("dish_name", products.Dish_Name)
        cv.put("price", products.Price)
        cv.put("product_name", products.Product_Name)
        cv.put("quantity", products.Quantity)

        val result = db.update("Table", cv, "prod_ID = ?", arrayOf(products.Product_ID.toString()))
        db.close()
        return result
    }


}

