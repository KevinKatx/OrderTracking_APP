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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel = viewModel(factory = ChatViewModelFactory(DatabaseHelper(LocalContext.current)))) {
    var userInput by remember { mutableStateOf("") }
    var chatHistory by remember { mutableStateOf(listOf<Pair<String, Boolean>>()) }
    var isLoading by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val calendar = Calendar.getInstance()

    calendar.set(Calendar.DAY_OF_MONTH, 1)
    var startDate by remember { mutableStateOf(dateFormatter.format(calendar.time)) }

// Set endDate to the last day of the current month
    calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
    var endDate by remember { mutableStateOf(dateFormatter.format(calendar.time)) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

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

        // Date Picker Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { showStartDatePicker = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFA500),
                    contentColor = Color.Black
                )
            ) {
                Text("Start Date: $startDate")
            }

            Button(
                onClick = { showEndDatePicker = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFA500),
                    contentColor = Color.Black
                )
            ) {
                Text("End Date: $endDate")
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
                        sendMessage(userInput, viewModel, chatHistory, startDate, startDate) { newHistory, loading ->
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
                        sendMessage(userInput, viewModel, chatHistory, startDate, endDate) { newHistory, loading ->
                            chatHistory = newHistory
                            isLoading = loading
                        }
                        userInput = ""
                    }
                },
                shape = RoundedCornerShape(5.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500), contentColor = Color.Black)
            ) {
                Text("Send")
            }
        }
    }

    // Date Pickers
    if (showStartDatePicker) {
        DatePickerDialogComponent(
            onDateSelected = { selectedDate ->
                startDate = selectedDate
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker) {
        DatePickerDialogComponent(
            onDateSelected = { selectedDate ->
                endDate = selectedDate
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialogComponent(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val calendar = Calendar.getInstance()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    ) {
        val state = rememberDatePickerState()

        DatePicker(
            state = state,
            showModeToggle = true
        )

        LaunchedEffect(state.selectedDateMillis) {
            state.selectedDateMillis?.let {
                val selectedDate = dateFormatter.format(Date(it))
                onDateSelected(selectedDate)
            }
        }
    }
}


private fun sendMessage(
    message: String,
    viewModel: ChatViewModel,
    chatHistory: List<Pair<String, Boolean>>,
    startDate: String, // Added parameter
    endDate: String,
    updateChatState: (List<Pair<String, Boolean>>, Boolean) -> Unit

) {
    if (message.isNotBlank()) {
        val newHistory = chatHistory + (message to true)
        updateChatState(newHistory, true)
        Log.d("ChatDebug", "User message sent: $message")

        viewModel.sendMessage(message, mutableStateOf(startDate), mutableStateOf(endDate)) { response ->
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
