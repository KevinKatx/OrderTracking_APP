package com.example.ordertrackingapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ordertrackingapp.databases.DatabaseHelper
import kotlinx.coroutines.delay

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel = viewModel(factory = ChatViewModelFactory(DatabaseHelper(LocalContext.current)))) {

    var userInput by remember { mutableStateOf("") }
    var chatHistory by remember { mutableStateOf(listOf<Pair<String, Boolean>>()) }
    var isLoading by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
            .imePadding()
    ) {
        Text("Chat Assistant", fontSize = 22.sp, modifier = Modifier.padding(bottom = 8.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            chatHistory.forEachIndexed { index, (message, isUser) ->
                AnimatedChatBubble(message, isUser, index)
            }

            if (isLoading) {
                ChatBubble("...", isUser = false)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedTextField(
                value = userInput,
                onValueChange = { userInput = it },
                placeholder = { Text("Type a message...") },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (userInput.isNotBlank()) {
                        keyboardController?.hide()
                        sendMessage(userInput, viewModel, chatHistory) { newHistory, loading ->
                            chatHistory = newHistory
                            isLoading = loading
                        }
                        userInput = ""
                    }
                })
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (userInput.isNotBlank()) {
                        keyboardController?.hide()
                        sendMessage(userInput, viewModel, chatHistory) { newHistory, loading ->
                            chatHistory = newHistory
                            isLoading = loading
                        }
                        userInput = ""
                    }
                },
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Send")
            }
        }
    }
}

private fun sendMessage(
    message: String,
    viewModel: ChatViewModel,
    chatHistory: List<Pair<String, Boolean>>,
    updateChatState: (List<Pair<String, Boolean>>, Boolean) -> Unit
) {
    if (message.isNotBlank()) {
        val newHistory = chatHistory + (message to true)
        updateChatState(newHistory, true)
        Log.d("ChatDebug", "User message sent: $message")

        viewModel.sendMessage(message) { response ->
            Log.d("ChatDebug", "Response received: $response")
            val updatedHistory = if (response.isNotBlank()) {
                newHistory + (response to false)
            } else {
                Log.d("ChatDebug", "Empty response received")
                newHistory
            }
            updateChatState(updatedHistory, false)
        }
    }
}

@Composable
fun AnimatedChatBubble(text: String, isUser: Boolean, index: Int) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(index) {
        delay(100)
        isVisible = true
    }

    AnimatedVisibility(visible = isVisible) {
        ChatBubble(text, isUser)
    }
}

@Composable
fun ChatBubble(text: String, isUser: Boolean) {
    val backgroundColor = if (isUser) Color(0xFF4CAF50) else Color(0xFF2196F3)
    val textColor = Color.White
    val alignment = if (isUser) Arrangement.End else Arrangement.Start

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = alignment
    ) {
        Box(
            modifier = Modifier
                .background(backgroundColor, shape = RoundedCornerShape(12.dp))
                .padding(12.dp)
                .widthIn(max = 250.dp)
        ) {
            Text(text = text, color = textColor)
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}
