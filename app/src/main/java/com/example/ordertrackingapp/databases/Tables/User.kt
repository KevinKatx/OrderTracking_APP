package com.example.ordertrackingapp.databases.Tables

class User {
    var userID: Int = 0 // Primary Key
    var username: String = ""
    var password: String = ""
    var email: String = ""
    var isAdmin: Boolean = false

    constructor(
        userID: Int,
        username: String,
        password: String,
        email: String,
        isAdmin: Boolean
    ) {
        this.userID = userID
        this.username = username
        this.password = password
        this.email = email
        this.isAdmin = isAdmin
    }

    constructor(
        username: String,
        password: String,
        email: String,
        isAdmin: Boolean
    ) {
        this.username = username
        this.password = password
        this.email = email
        this.isAdmin = isAdmin
    }

    constructor()

    override fun toString(): String {
        return "User(userID=$userID, username='$username', email='$email', isAdmin=$isAdmin)"
    }
}