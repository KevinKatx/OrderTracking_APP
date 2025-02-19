package com.example.ordertrackingapp

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.material3.Button
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


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OrderScreen(navController: NavController) {
    val context = LocalContext.current
    val orderHandler = OrderHandler(context)

    var readBTNstate by remember { mutableStateOf(true)}
    var insertBTNstate by remember { mutableStateOf(true)}
    var updateBTNstate by remember { mutableStateOf(false)}
    var backBTNstate by remember { mutableStateOf(false)}
    var editBTNstate by remember { mutableStateOf(true)}
    var fetchBTNstate by remember { mutableStateOf(false)}

    var orderIDField by remember { mutableStateOf(false)}
    var orderID by remember { mutableStateOf("")}
    var customerID by remember { mutableStateOf("") }
    var totalPrice by remember { mutableStateOf("") }
    var promoID by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var orderDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var paymentType by remember { mutableStateOf("") }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(){
            if (orderIDField){
                TextField(value = orderID, modifier = Modifier
                    .size(width = 200.dp, height = 50.dp)

                    ,onValueChange = { orderID = it }, label = {Text("Order ID") })
            }
            if (fetchBTNstate){
                Button(onClick = {
                    val singleOrder = orderHandler.readData(orderID.toInt()).firstOrNull()
                    if (singleOrder != null) {
                        customerID = singleOrder.customerID.toString()
                        totalPrice = singleOrder.totalPrice.toString()
                        promoID = singleOrder.promoID.toString()
                        status = singleOrder.status
                        paymentType = singleOrder.paymentType
                    } else {
                        Log.d("OrderFetch", "No order found with ID $orderID")
                    }
                }) {
                    Text("Fetch")
                }
            }
        }

        TextField(value = customerID, onValueChange = { customerID = it }, label = { Text("Customer ID") })
        TextField(value = totalPrice, onValueChange = { totalPrice = it }, label = { Text("Total Price") })
        TextField(value = promoID, onValueChange = { promoID = it }, label = { Text("Promo ID") })
        TextField(value = status, onValueChange = { status = it }, label = { Text("Status") })
        TextField(value = orderDate, onValueChange = { orderDate = it }, label = { Text("Order Date") })
        TextField(value = paymentType, onValueChange = { paymentType = it }, label = { Text("Payment Type") })

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

            if(insertBTNstate){
                Button(onClick = {
                    val order = Order(
                        customerID.toIntOrNull() ?: 0,
                        totalPrice.toIntOrNull() ?: 0,
                        promoID.toIntOrNull() ?: 0,
                        status,
                        LocalDate.parse(orderDate),
                        paymentType
                    )
                    orderHandler.insertData(order)
                }) {
                    Text("Insert")
                }
            }

            if(readBTNstate){
                Button(onClick = {
                    val orders = orderHandler.readData()
                    orders.forEach { Log.d("Order", it.toString()) }
                }) {
                    Text("Read")
                }
            }




            if(editBTNstate){
                Button(onClick = {
                    updateBTNstate = true
                    insertBTNstate = false
                    readBTNstate = false
                    backBTNstate = true
                    editBTNstate = false
                    orderIDField = true
                    fetchBTNstate = true
                    customerID = ""
                    totalPrice = ""
                    promoID = ""
                    status = ""
                    paymentType = ""
                }) {
                    Text("Edit")
                }
            }

            if (backBTNstate){
                Button(onClick = {
                    updateBTNstate = false
                    insertBTNstate = true
                    readBTNstate = true
                    backBTNstate = false
                    editBTNstate = true
                    orderIDField = false
                    fetchBTNstate = false
                    customerID = ""
                    totalPrice = ""
                    promoID = ""
                    status = ""
                    paymentType = ""
                }) {
                    Text("back")
                }
            }


            if (updateBTNstate){
                Button(onClick = {
                    val order = Order(
                        customerID.toIntOrNull() ?: 0,
                        totalPrice.toIntOrNull() ?: 0,
                        promoID.toIntOrNull() ?: 0,
                        status,
                        LocalDate.parse(orderDate),
                        paymentType
                    )
                    order.orderID = orderID.toInt() // Assume updating order with ID 1, modify as needed
                    orderHandler.updateData(order)
                }) {
                    Text("Update")
                }
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