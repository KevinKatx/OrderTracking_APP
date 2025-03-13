package com.example.ordertrackingapp.databases.Tables

import kotlin.collections.List
import kotlin.reflect.full.memberProperties

class Customer {
    var Customer_ID: Int = 0 // Primary Key
    // Foreign Key Orders
    var Name: String = ""
    var Type: String = ""
    var Address: String = ""

    constructor(Customer_ID: Int, Name: String, Type:String, Address: String){
        this.Customer_ID = Customer_ID
        this.Name = Name
        this.Type = Type
        this.Address = Address
    }
}