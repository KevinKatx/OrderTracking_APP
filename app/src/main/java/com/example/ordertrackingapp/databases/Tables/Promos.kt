package com.example.ordertrackingapp.databases.Tables

import kotlin.collections.List
import kotlin.reflect.full.memberProperties

class Promos {
    var Promo_ID: Int = 0
    var Type: String = ""
    var DiscountPercent: Int = 0
    var DiscountFlat: Int = 0

    constructor(
        Promo_ID: Int, Type: String, DiscountPercent: Int, DiscountFlat: Int
    ){
        this.Promo_ID = Promo_ID
        this.Type = Type
        this.DiscountPercent = DiscountPercent
        this.DiscountFlat = DiscountFlat
    }

    constructor()

}