package com.example.ordertrackingapp.databases.Tables

import android.os.Build
import androidx.annotation.RequiresApi
import kotlin.collections.List
import kotlin.reflect.full.memberProperties

class Products {
    var Product_ID: Int = 0
    var Product_name: String = ""
    var Price: Int = 0

    constructor()
    constructor(
        Product_ID: Int, Product_name: String, Price: Int
    ){
        this.Product_ID = Product_ID
        this.Product_name = Product_name
        this.Price = Price
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun toString(): String {
        return "Products(productID=$Product_ID,  productName=$Product_name, price=$Price)"
    }

}