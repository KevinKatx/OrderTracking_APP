package com.example.ordertrackingapp.databases.Tables

import kotlin.collections.List
import kotlin.reflect.full.memberProperties

class Ingredients {
    var Ingredient_ID: Int = 0
    var IngredientName: String = ""
    var IdealStock: Int = 0
    var CurrentStock: Int = 0

    // Returns columns
    fun getColumns(): List<String> {
        val properties = this::class.memberProperties.map { it.name }
        val idColumns = properties.filter { it.endsWith("_ID") }
        val otherColumns = properties.filterNot { it.endsWith("_ID") }

        return idColumns + otherColumns // Ensures ID columns come first
    }

    // Gets table name by removing `_ID`
    fun getTableName(): String {
        return this::class.memberProperties
            .firstOrNull { it.name.endsWith("_ID") }
            ?.name?.removeSuffix("_ID") ?: "UnknownTable"
    }
}