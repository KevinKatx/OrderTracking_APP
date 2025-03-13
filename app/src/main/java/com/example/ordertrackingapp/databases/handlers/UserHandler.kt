package com.example.ordertrackingapp.databases.handlers

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteException
import android.util.Log
import android.widget.Toast
import com.example.ordertrackingapp.databases.DatabaseHelper
import com.example.ordertrackingapp.databases.Tables.User

class UserHandler(private val context: Context) {

    private val dbHelper = DatabaseHelper(context)
    private val TAG = "UserHandler"

    // Insert a new user
    fun insertUser(user: User): Boolean {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("username", user.username)
            put("password", user.password)
            put("email", user.email)
            put("isAdmin", if (user.isAdmin) 1 else 0)
        }

        try {
            val result = db.insert("Users", null, cv)
            db.close()

            return if (result == -1L) {
                Toast.makeText(context, "User creation failed", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Insert user failed for username: ${user.username}")
                false
            } else {
                Toast.makeText(context, "User created successfully", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "User created successfully with ID: $result")
                true
            }
        } catch (e: SQLiteException) {
            Log.e(TAG, "Database error when inserting user: ${e.message}")
            Toast.makeText(context, "Database error: ${e.message}", Toast.LENGTH_SHORT).show()
            db.close()
            return false
        }
    }

    // Authenticate a user
    fun authenticateUser(username: String, password: String): User? {
        val db = dbHelper.readableDatabase
        var user: User? = null

        try {
            val query = "SELECT * FROM Users WHERE username = ? AND password = ?"
            val cursor = db.rawQuery(query, arrayOf(username, password))

            if (cursor.moveToFirst()) {
                user = User(
                    cursor.getInt(cursor.getColumnIndexOrThrow("userID")),
                    cursor.getString(cursor.getColumnIndexOrThrow("username")),
                    cursor.getString(cursor.getColumnIndexOrThrow("password")),
                    cursor.getString(cursor.getColumnIndexOrThrow("email")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("isAdmin")) == 1
                )
                Log.d(TAG, "User authenticated: $username")
            } else {
                Log.d(TAG, "Authentication failed for username: $username")
            }

            cursor.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error authenticating user: ${e.message}")
        } finally {
            db.close()
        }

        return user
    }

    // Check if username exists
    fun isUsernameExists(username: String): Boolean {
        val db = dbHelper.readableDatabase
        var exists = false

        try {
            val query = "SELECT COUNT(*) FROM Users WHERE username = ?"
            val cursor = db.rawQuery(query, arrayOf(username))

            if (cursor.moveToFirst()) {
                exists = cursor.getInt(0) > 0
            }

            cursor.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking username existence: ${e.message}")
        } finally {
            db.close()
        }

        return exists
    }

    // Validate system passkey
    fun validatePasskey(passkey: String): Boolean {
        val db = dbHelper.readableDatabase
        var isValid = false

        try {
            val query = "SELECT configValue FROM SystemConfig WHERE configKey = 'registration_passkey'"
            val cursor = db.rawQuery(query, null)

            if (cursor.moveToFirst()) {
                val storedPasskey = cursor.getString(0)
                isValid = storedPasskey == passkey
            }

            cursor.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error validating passkey: ${e.message}")
        } finally {
            db.close()
        }

        return isValid
    }

    // Get all users
    fun getAllUsers(): List<User> {
        val userList = mutableListOf<User>()
        val db = dbHelper.readableDatabase

        try {
            val query = "SELECT * FROM Users"
            val cursor = db.rawQuery(query, null)

            if (cursor.moveToFirst()) {
                do {
                    val user = User(
                        cursor.getInt(cursor.getColumnIndexOrThrow("userID")),
                        cursor.getString(cursor.getColumnIndexOrThrow("username")),
                        cursor.getString(cursor.getColumnIndexOrThrow("password")),
                        cursor.getString(cursor.getColumnIndexOrThrow("email")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("isAdmin")) == 1
                    )
                    userList.add(user)
                } while (cursor.moveToNext())
            }

            cursor.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all users: ${e.message}")
        } finally {
            db.close()
        }

        return userList
    }

    // Update user
    fun updateUser(user: User): Boolean {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("username", user.username)
            put("password", user.password)
            put("email", user.email)
            put("isAdmin", if (user.isAdmin) 1 else 0)
        }

        try {
            val result = db.update("Users", cv, "userID = ?", arrayOf(user.userID.toString()))
            db.close()

            return if (result > 0) {
                Log.d(TAG, "User updated successfully: ${user.username}")
                true
            } else {
                Log.e(TAG, "User update failed for ID: ${user.userID}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user: ${e.message}")
            db.close()
            return false
        }
    }

    // Delete user
    fun deleteUser(userID: Int): Boolean {
        val db = dbHelper.writableDatabase

        try {
            val result = db.delete("Users", "userID = ?", arrayOf(userID.toString()))
            db.close()

            return if (result > 0) {
                Log.d(TAG, "User deleted successfully: ID $userID")
                true
            } else {
                Log.e(TAG, "User deletion failed for ID: $userID")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting user: ${e.message}")
            db.close()
            return false
        }
    }
}