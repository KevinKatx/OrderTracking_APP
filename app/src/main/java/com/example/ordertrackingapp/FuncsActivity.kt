package com.example.ordertrackingapp

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.ordertrackingapp.databases.Tables.Order
import com.example.ordertrackingapp.databases.handlers.OrderHandler
import java.time.LocalDate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OrderScreen(navController: NavController) {
    val context = LocalContext.current
    val orderHandler = OrderHandler(context)
    val orders = remember { mutableStateOf(orderHandler.readData()) }
    var selectedOrder by remember { mutableStateOf<Order?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            val orders = orderHandler.readData()

            items(orders) { order ->
                val isSelected = selectedOrder?.orderID == order.orderID
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable { selectedOrder = order },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isSelected) Color.Gray else Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Order ID: ${order.orderID}")
                        Text("Customer ID: ${order.customerID}")
                        Text("Total Price: ${order.totalPrice}")
                        Text("Promo ID: ${order.promoID}")
                        Text("Status: ${order.status}")
                        Text("Order Date: ${order.orderDate}")
                        Text("Payment Type: ${order.paymentType}")
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                navController.navigate("order_edit")
            }) {
                Text("Insert")
            }

            Button(onClick = {
                orders.value = orderHandler.readData()
            }) {
                Text("Read")
            }

            Button(onClick = {
                selectedOrder?.let {
                    navController.navigate("order_edit/${it.orderID}")
                }
            }, enabled = selectedOrder != null) {
                Text("Edit")
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OrderEdit(navController: NavController, orderID: Int? = null) {
    val context = LocalContext.current
    val orderHandler = OrderHandler(context)
    val order = remember { mutableStateOf(orderID?.let { orderHandler.readData(it).firstOrNull() }) }

    var customerID by remember { mutableStateOf(order.value?.customerID?.toString() ?: "") }
    var totalPrice by remember { mutableStateOf(order.value?.totalPrice?.toString() ?: "") }
    var promoID by remember { mutableStateOf(order.value?.promoID?.toString() ?: "") }
    var status by remember { mutableStateOf(order.value?.status ?: "") }
    var orderDate by remember { mutableStateOf(order.value?.orderDate?.toString() ?: LocalDate.now().toString()) }
    var paymentType by remember { mutableStateOf(order.value?.paymentType ?: "") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(value = customerID, onValueChange = { customerID = it }, label = { Text("Customer ID") })
        TextField(value = totalPrice, onValueChange = { totalPrice = it }, label = { Text("Total Price") })
        TextField(value = promoID, onValueChange = { promoID = it }, label = { Text("Promo ID") })
        TextField(value = status, onValueChange = { status = it }, label = { Text("Status") })
        TextField(value = orderDate, onValueChange = { orderDate = it }, label = { Text("Order Date") })
        TextField(value = paymentType, onValueChange = { paymentType = it }, label = { Text("Payment Type") })

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                val updatedOrder = Order(
                    customerID.toIntOrNull() ?: 0,
                    totalPrice.toIntOrNull() ?: 0,
                    promoID.toIntOrNull() ?: 0,
                    status,
                    LocalDate.parse(orderDate),
                    paymentType
                ).apply { orderID?.let { this.orderID = it } }

                if (orderID != null) {
                    orderHandler.updateData(updatedOrder)
                } else {
                    orderHandler.insertData(updatedOrder)
                }
                navController.popBackStack()
            }) {
                Text("OK")
            }

            Button(onClick = {
                navController.popBackStack()
            }) {
                Text("Back")
            }
        }
    }
}



@Composable
fun ProductsScreen(navController: NavController){
    Box(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter // Align content in the top center

    ) {
        Image(
            painter = painterResource(id = R.drawable.foodstop_header),
            contentDescription = "My Image",
            modifier = Modifier
                .size(600.dp)
                .offset(y = -215.dp) // Adjust the image position
        )
    }
}

@Composable
fun InventoryScreen(navController: NavHostController) {
    Box(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter // Align content in the top center

    ) {
        Image(
            painter = painterResource(id = R.drawable.foodstop_header),
            contentDescription = "My Image",
            modifier = Modifier
                .size(600.dp)
                .offset(y = -215.dp) // Adjust the image position
        )
    }
}

@Composable
fun CustomerScreen(navController: NavHostController) {
    Box(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter // Align content in the top center

    ) {
        Image(
            painter = painterResource(id = R.drawable.foodstop_header),
            contentDescription = "My Image",
            modifier = Modifier
                .size(600.dp)
                .offset(y = -215.dp) // Adjust the image position
        )
    }
}

@Composable
fun AnalyticsScreen(navController: NavHostController) {
    Box(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter // Align content in the top center

    ) {
        Image(
            painter = painterResource(id = R.drawable.foodstop_header),
            contentDescription = "My Image",
            modifier = Modifier
                .size(600.dp)
                .offset(y = -215.dp) // Adjust the image position
        )
    }
}