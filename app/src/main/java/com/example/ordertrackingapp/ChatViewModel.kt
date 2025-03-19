package com.example.ordertrackingapp

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ordertrackingapp.databases.handlers.AnalyticsHandler
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class ChatViewModel(private val analyticsHandler: AnalyticsHandler) : ViewModel() {
    private val api = RetrofitClient.api


    fun sendMessage(userMessage: String, startDate: MutableState<String>, endDate: MutableState<String>, callback: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val analyticsContext = analyticsHandler.getAnalyticsSummary(startDate, endDate)

                val request = ChatRequest(
                    messages = listOf(
                        Message("system", "Context: $analyticsContext"),
                        Message("user", userMessage)
                    )
                )
                val response = api.getChatResponse(request)
                val botResponse = response.choices.firstOrNull()?.message?.content ?: "No response"
                callback(botResponse)
            } catch (e: Exception) {
                callback("Error: ${e.message}")
            }
        }
    }
}