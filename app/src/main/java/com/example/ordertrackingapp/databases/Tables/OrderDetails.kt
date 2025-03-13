package com.example.ordertrackingapp.databases.Tables

class OrderDetails {
    var orderID: Int = 0
    var productID: Int = 0
    var quantity: Int = 0

    constructor(
        orderID: Int, productID: Int, quantity: Int
    ){
        this.orderID = orderID
        this.productID = productID
        this.quantity = quantity
    }

    constructor()
}