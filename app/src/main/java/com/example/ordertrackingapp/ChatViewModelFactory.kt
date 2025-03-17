package com.example.ordertrackingapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ordertrackingapp.databases.handlers.AnalyticsHandler
import com.example.ordertrackingapp.databases.DatabaseHelper

class ChatViewModelFactory(private val dbHelper: DatabaseHelper) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(AnalyticsHandler(dbHelper.writableDatabase)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
