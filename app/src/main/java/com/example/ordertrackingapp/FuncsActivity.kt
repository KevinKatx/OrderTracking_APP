@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ordertrackingapp

import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import java.time.LocalDate
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ordertrackingapp.databases.DatabaseHelper
import com.example.ordertrackingapp.databases.Tables.*
import com.example.ordertrackingapp.databases.handlers.*
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.Date
import java.util.Locale


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OrderScreen(navController: NavController) {
    val context = LocalContext.current
    val orderHandler = remember { OrderHandler(context) }
    val orderDetailHandler = remember { OrderDetailsHandler(context) }
    val promosHandler = remember { PromosHandler(context) }
    val orders = remember { mutableStateOf(orderHandler.readData()) }
    var selectedOrder by remember { mutableStateOf<Order?>(null) }

    // Function to get product IDs for an order
    fun getProductIdsForOrder(orderID: Int): String {
        val orderDetails = orderDetailHandler.readData(orderID)
        return orderDetails.map { it.productID }.joinToString(", ")
    }

    fun getPromoName(promoID: Int): String {
        val promoList = promosHandler.readData(promoID)

        return if (promoList.isNotEmpty()) {
            promoList.first().Name // Get the first element's name
        } else {
            "No Promo Found"
        }
    }

    // Log orders when the screen is first composed
    LaunchedEffect(Unit) {
        Log.d("DB_DEBUG", "Orders retrieved: ${orders.value}")
    }

    Box(modifier = Modifier
        .background(Color.White)
        .fillMaxSize()){
        Image(
            painter = painterResource(id = R.drawable.foodstop_header),
            contentDescription = "My Image",
            modifier = Modifier
                .size(600.dp)
                .offset(y = -215.dp) // Adjust the image position
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)  // ✅ Allows LazyColumn to scroll
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 16.dp) // Prevents cut-off at the bottom
                ) {
                    items(orders.value, key = { it.orderID }) { order ->
                        val isSelected = selectedOrder?.orderID == order.orderID
                        val productIds = getProductIdsForOrder(order.orderID)
                        val promoName = getPromoName(order.promoID)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable {
                                    Log.d("DB_QUERY", "Retrieved Order ID: ${order.orderID}")
                                    selectedOrder = order
                                },
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color.Gray else Color.White
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Order ID: ${order.orderID}")
                                Text("Customer ID: ${order.customerID}")
                                Text("Total Price: ${order.totalPrice}")
                                Text("Promo Name: $promoName")
                                Text("Status: ${order.status}")
                                Text("Order Date: ${order.orderDate}")
                                Text("Payment Type: ${order.paymentType}")
                                Text("Product IDs: $productIds")
                            }
                        }
                    }
                }
            }

            // Rest of the existing code remains the same
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Button(onClick = { navController.navigate("order_insert") },
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
                    ) {
                    Text("Insert")
                }

                Button(
                    onClick = { selectedOrder?.let { navController.navigate("order_edit/${it.orderID}") } },
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500)),
                    enabled = selectedOrder != null
                ) {
                    Text("Edit")
                }
                Button(
                    onClick = {
                        selectedOrder?.let {
                            orderHandler.deleteData(it.orderID)
                            // Also delete associated order details
                            OrderDetailsHandler(context).deleteData(it.orderID)
                            orders.value = orderHandler.readData()
                        }
                    },
                    enabled = selectedOrder != null,
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
                ) {
                    Text("Delete")
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OrderEdit(navController: NavController, orderID: Int? = null) {
    val context = LocalContext.current
    val orderHandler = OrderHandler(context)
    val orderDetailHandler = OrderDetailsHandler(context)
    val productHandler = remember { ProductsHandler(context) }
    val promoHandler = remember { PromosHandler(context) }
    val customerHandler = CustomerHandler(context)
    val gson = Gson()

    val customers = remember { customerHandler.readData() }
    var expandedCustomer by remember { mutableStateOf(false) }

    val order = remember { mutableStateOf(orderID?.let { orderHandler.readData(it).firstOrNull() }) }

    var customerID by remember { mutableStateOf(order.value?.customerID?.toString() ?: "") }
    var totalPrice by remember { mutableStateOf(0) }
    var status by remember { mutableStateOf(order.value?.status ?: "") }
    var orderDate by remember { mutableStateOf(order.value?.orderDate?.toString() ?: LocalDate.now().toString()) }
    var paymentType by remember { mutableStateOf(order.value?.paymentType ?: "") }
    var expandedStat by remember { mutableStateOf(false)}
    val statuses = listOf("Pending", "Completed", "Cancelled")
    val paymentMethods = listOf("Cash On Delivery", "GCash", "Credit Card")
    var expandedPay by remember { mutableStateOf(false)}

    val showDatePicker = remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // Manage selected products
    var selectedProducts by remember { mutableStateOf<List<Pair<Products, Int>>>(emptyList()) }
    var selectedPromo by remember { mutableStateOf<Promos?>(null) }

    var updatedPrice by remember { mutableStateOf(totalPrice) }

    fun calculateUpdatedPrice2() {
        updatedPrice = selectedPromo?.let { promo ->
            var discountedPrice = totalPrice

            // Apply percentage discount
            if (promo.DiscountPercent > 0) {
                discountedPrice = (totalPrice * (1 - promo.DiscountPercent / 100.0)).toInt()
            }

            // Apply flat discount
            if (promo.DiscountFlat > 0) {
                discountedPrice -= promo.DiscountFlat
                if (discountedPrice < 0) discountedPrice = 0
            }

            discountedPrice
        } ?: totalPrice
    }


    // Load existing order details when editing
    LaunchedEffect(orderID) {
        orderID?.let { id ->
            // Fetch existing order details
            val existingOrderDetails = orderDetailHandler.readData(id)

            // Convert order details to selected products, EXCLUDING products with quantity = 0
            selectedProducts = existingOrderDetails.mapNotNull { orderDetail ->
                val product = productHandler.readData().find { it.Product_ID == orderDetail.productID }
                product?.let { Pair(it, orderDetail.quantity) }
            }.filter { it.second > 0 } // Filter out products with quantity 0

            // Recalculate total price based only on valid products
            totalPrice = selectedProducts.sumOf { (product, quantity) ->
                product.Price * quantity
            }
            order.value?.promoID?.let { promoId ->
                if (promoId > 0) {
                    selectedPromo = promoHandler.readData().find { it.Promo_ID == promoId }
                    calculateUpdatedPrice2()
                }
            }
        }
    }

    // Handle product selection from navigation
    LaunchedEffect(navController.currentBackStackEntry) {
        Log.d("DEBUG", "LaunchedEffect Triggered")

        val gson = Gson()
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.get<String>("selected_products")
            ?.let { json ->
                Log.d("DEBUG", "JSON Data: $json")

                val type = object : TypeToken<List<Map<String, Any>>>() {}.type
                val productList: List<Map<String, Any>> = gson.fromJson(json, type)

                selectedProducts = productList.map { map ->
                    val productMap = map["first"]
                    Log.d("DEBUG", "Raw Product Map: $productMap")  // Check what's inside "first"

                    val product = if (productMap is Map<*, *>) {
                        Products(
                            productMap["Product_ID"]?.let { (it as? Double)?.toInt() } ?: -1, // Fix here
                            productMap["Product_name"] as? String ?: "Unknown",
                            (productMap["Price"] as? Double)?.toInt() ?: 0
                        )
                    } else {
                        Log.e("ERROR", "Invalid product map structure: $productMap")
                        Products(-1, "Invalid", 0)
                    }

                    val quantity = (map["second"] as? Double)?.toInt() ?: 0

                    Log.d("DEBUG", "Extracted Product ID: ${product.Product_ID}, Quantity: $quantity")

                    Pair(product, quantity)
                }
                totalPrice = selectedProducts.sumOf { (product, quantity) ->
                    product.Price * quantity
                }
                calculateUpdatedPrice2()

                selectedProducts.forEach { (product, qty) ->
                    Log.d("DEBUG", "Final Product List -> ID: ${product.Product_ID}, Name: ${product.Product_name}, Qty: $qty")
                }
            }

        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.get<String>("selected_promo")
            ?.let { json ->
                Log.d("DEBUG", "Selected Promo JSON: $json")
                selectedPromo = gson.fromJson(json, Promos::class.java)
                calculateUpdatedPrice2()
            }
    }

    // Function to calculate price after promo is applied
    fun calculateUpdatedPrice() {
        updatedPrice = selectedPromo?.let { promo ->
            var discountedPrice = totalPrice

            // Apply percentage discount
            if (promo.DiscountPercent > 0) {
                discountedPrice = (totalPrice * (1 - promo.DiscountPercent / 100.0)).toInt()
            }

            // Apply flat discount
            if (promo.DiscountFlat > 0) {
                discountedPrice -= promo.DiscountFlat
                if (discountedPrice < 0) discountedPrice = 0
            }

            discountedPrice
        } ?: totalPrice
    }

    Box(modifier = Modifier
        .background(Color.White)
        .fillMaxSize()){
        Image(
            painter = painterResource(id = R.drawable.foodstop_header),
            contentDescription = "My Image",
            modifier = Modifier
                .size(600.dp)
                .offset(y = -215.dp) // Adjust the image position
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            ExposedDropdownMenuBox(
                expanded = expandedCustomer,
                onExpandedChange = { expandedCustomer = it }
            ) {
                TextField(
                    value = if (customerID.isNotBlank()) {
                        customers.find { it.Customer_ID.toString() == customerID }?.Name ?: ""
                    } else "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Customer") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCustomer)
                    },
                    modifier = Modifier.menuAnchor(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFFFA500),
                        unfocusedLabelColor = Color(0xFFA26D00)
                    )
                )
                ExposedDropdownMenu(
                    expanded = expandedCustomer,
                    onDismissRequest = { expandedCustomer = false }
                ) {
                    customers.forEach { customer ->
                        DropdownMenuItem(
                            text = { Text("${customer.Customer_ID}: ${customer.Name}") },
                            onClick = {
                                customerID = customer.Customer_ID.toString()
                                expandedCustomer = false
                            }
                        )
                    }
                }
            }





            Button(
                onClick = {
                    val jsonProducts = gson.toJson(selectedProducts)
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("selected_products", jsonProducts)
                    navController.navigate("select_products")
                },
                modifier = Modifier.width(280.dp),
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
            ) {
                Text(if (selectedProducts.isEmpty()) "Products: None Selected" else "Products: ${selectedProducts.size} Selected")
            }

            TextField(
                value = totalPrice.toString(),
                onValueChange = {totalPrice = it.toInt()},
                readOnly = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFFFA500),
                    unfocusedLabelColor = Color(0xFFA26D00)
                ),
                label = { Text("Total Price") })

            Button(
                onClick = {
                    navController.navigate("select_promos")
                },
                modifier = Modifier.width(280.dp),
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
            ) {
                Text(selectedPromo?.Name ?: "Select Promo")
            }

            TextField(
                value = updatedPrice.toString(),
                onValueChange = {},
                readOnly = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFFFA500),
                    unfocusedLabelColor = Color(0xFFA26D00)
                ),
                label = { Text("Updated Price") }
            )

            ExposedDropdownMenuBox(expanded = expandedStat, onExpandedChange = { expandedStat = it }
            ) {
                TextField(
                    value = status,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStat) },
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedStat,
                    onDismissRequest = { expandedStat = false }
                ) {
                    statuses.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                status = option
                                expandedStat = false
                            }
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .clickable {
                        Log.d("DEBUG", "DatePicker clicked") // Debugging
                        showDatePicker.value = true
                    }
                    .background(Color(0xFFFFA500), shape = RoundedCornerShape(8.dp))
                    .padding(4.dp)
            ) {
                TextField(
                    value = orderDate,
                    onValueChange = {},
                    readOnly = true, // Prevents user input but allows clicks on parent Box
                    enabled = false, // Keeps the greyed-out effect
                    colors = TextFieldDefaults.colors(
                        disabledContainerColor = Color(0xFFFFA500), // Orange background
                        disabledTextColor = Color.Black, // Ensures text is visible
                        disabledLabelColor = Color(0xFFA26D00) // Darker orange for label
                    ),
                    label = { Text("Order Date") }
                )
            }
            ExposedDropdownMenuBox(
                expanded = expandedPay,
                onExpandedChange = { expandedPay = it }) {
                TextField(value = paymentType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Payment Method") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPay) },
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedPay,
                    onDismissRequest = { expandedPay = false }
                ) {
                    paymentMethods.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                paymentType = option
                                expandedPay = false
                            }
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    val updatedOrder = Order(
                        order.value?.orderID ?: 0,
                        customerID.toIntOrNull() ?: 0,
                        updatedPrice, // Use the updated price after promo
                        selectedPromo?.Promo_ID ?: 0,
                        status,
                        LocalDate.parse(orderDate),
                        paymentType
                    )

                    // Determine if we're updating or inserting
                    val insertedOrderId = if (orderID != null) {
                        // Update existing order
                        orderHandler.updateData(updatedOrder)
                        Toast.makeText(context, "Order Updated", Toast.LENGTH_SHORT).show()
                        orderID

                    } else {
                        // Insert new order and get its ID
                        if (orderHandler.insertData(updatedOrder)) {
                            orderHandler.getLatestOrderID()
                        } else {
                            Toast.makeText(context, "Failed to insert order", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                    }


                    selectedProducts.forEach { (product, quantity) ->
                        val orderDetails = OrderDetails(
                            orderID = orderID ?: return@forEach,
                            productID = product.Product_ID,
                            quantity = quantity
                        )
                        Log.d("DEBUG","Final Product List -> ID: ${orderDetails.orderID}, Name: ${orderDetails.productID}, Qty: ${orderDetails.quantity}")
                        orderDetailHandler.updateData(orderDetails)
                        Toast.makeText(context, "Order updated successfully!", Toast.LENGTH_SHORT).show()
                    }




                    navController.popBackStack()
                },
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))) {
                    Text("OK")
                }

                Button(onClick = { navController.popBackStack() },
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
                    ) {
                    Text("Back")
                }
            }
            if (showDatePicker.value) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker.value = false },
                    confirmButton = {
                        Button(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val selectedDate = Instant.ofEpochMilli(millis)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                                orderDate = selectedDate.toString()
                            }
                            showDatePicker.value = false
                        }) { Text("OK") }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            LaunchedEffect(datePickerState.selectedDateMillis) {
                datePickerState.selectedDateMillis?.let { millis ->
                    val selectedDate = Instant.ofEpochMilli(millis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    orderDate = selectedDate.toString()
                }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OrderInsert(navController: NavController) {
    val context = LocalContext.current
    val orderHandler = OrderHandler(context)
    val orderDetailHandler = OrderDetailsHandler(context)
    val customerHandler = CustomerHandler(context)
    val promosHandler = PromosHandler(context)
    val showDatePicker = remember { mutableStateOf(false) }
    val customers = remember { customerHandler.readData() }
    var expandedCustomer by remember { mutableStateOf(false) }



    var tempOrderID by remember { mutableStateOf(orderHandler.getLatestOrderID()) }
    var orderID by remember { mutableStateOf("") }
    var customerID by rememberSaveable { mutableStateOf("") }
    var totalPrice by remember { mutableStateOf(0) }
    var status by rememberSaveable { mutableStateOf("") }
    var orderDate by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var paymentType by rememberSaveable { mutableStateOf("") }
    var expandedStat by rememberSaveable { mutableStateOf(false) }
    val statuses = listOf("Pending", "Completed", "Cancelled")
    val paymentMethods = listOf("Cash On Delivery", "GCash", "Credit Card")
    var expandedPay by rememberSaveable { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    var updatedPrice by remember { mutableStateOf(totalPrice) }

    var selectedProducts by remember { mutableStateOf<List<Pair<Products, Int>>>(emptyList()) } // Product + Quantity Pair
    var selectedPromo by remember { mutableStateOf<Promos?>(null) }

    fun calculateUpdatedPrice() {
        updatedPrice = selectedPromo?.let { promo ->
            var discountedPrice = totalPrice

            // Apply percentage discount
            if (promo.DiscountPercent > 0) {
                discountedPrice = (totalPrice * (1 - promo.DiscountPercent / 100.0)).toInt()
            }

            // Apply flat discount
            if (promo.DiscountFlat > 0) {
                discountedPrice -= promo.DiscountFlat
                if (discountedPrice < 0) discountedPrice = 0
            }

            discountedPrice
        } ?: totalPrice
    }



    // Calculate total price automatically when products are received
    LaunchedEffect(navController.currentBackStackEntry) {
        Log.d("DEBUG", "LaunchedEffect Triggered")

        val gson = Gson()
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.get<String>("selected_products")
            ?.let { json ->
                Log.d("DEBUG", "JSON Data: $json")

                val type = object : TypeToken<List<Map<String, Any>>>() {}.type
                val productList: List<Map<String, Any>> = gson.fromJson(json, type)

                selectedProducts = productList.map { map ->
                    val productMap = map["first"]
                    Log.d("DEBUG", "Raw Product Map: $productMap")  // Check what's inside "first"

                    val product = if (productMap is Map<*, *>) {
                        Products(
                            productMap["Product_ID"]?.let { (it as? Double)?.toInt() } ?: -1, // Fix here
                            productMap["Product_name"] as? String ?: "Unknown",
                            (productMap["Price"] as? Double)?.toInt() ?: 0
                        )
                    } else {
                        Log.e("ERROR", "Invalid product map structure: $productMap")
                        Products(-1, "Invalid", 0)
                    }

                    val quantity = (map["second"] as? Double)?.toInt() ?: 0

                    Log.d("DEBUG", "Extracted Product ID: ${product.Product_ID}, Quantity: $quantity")

                    Pair(product, quantity)
                }
                totalPrice = selectedProducts.sumOf { (product, quantity) ->
                    product.Price * quantity
                }
                calculateUpdatedPrice()

                selectedProducts.forEach { (product, qty) ->
                    Log.d("DEBUG", "Final Product List -> ID: ${product.Product_ID}, Name: ${product.Product_name}, Qty: $qty")
                }
            }
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.get<String>("selected_promo")
            ?.let { json ->
                Log.d("DEBUG", "Selected Promo JSON: $json")
                selectedPromo = gson.fromJson(json, Promos::class.java)
                calculateUpdatedPrice()
            }
    }

    Box(modifier = Modifier
        .background(Color.White)
        .fillMaxSize()){
        Image(
            painter = painterResource(id = R.drawable.foodstop_header),
            contentDescription = "My Image",
            modifier = Modifier
                .size(600.dp)
                .offset(y = -215.dp) // Adjust the image position
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ExposedDropdownMenuBox(
                expanded = expandedCustomer,
                onExpandedChange = { expandedCustomer = it }
            ) {
                TextField(
                    value = if (customerID.isNotBlank()) {
                        customers.find { it.Customer_ID.toString() == customerID }?.Name ?: ""
                    } else "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Customer") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCustomer)
                    },
                    modifier = Modifier.menuAnchor(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFFFA500),
                        unfocusedLabelColor = Color(0xFFA26D00)
                    )
                )
                ExposedDropdownMenu(
                    expanded = expandedCustomer,
                    onDismissRequest = { expandedCustomer = false }
                ) {
                    customers.forEach { customer ->
                        DropdownMenuItem(
                            text = { Text("${customer.Customer_ID}: ${customer.Name}") },
                            onClick = {
                                customerID = customer.Customer_ID.toString()
                                expandedCustomer = false
                            }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    val gson = Gson()
                    val jsonProducts = gson.toJson(selectedProducts)
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("selected_products", jsonProducts)
                    navController.navigate("select_products")
                },
                modifier = Modifier.width(280.dp),
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
            ) {
                Text(if (selectedProducts.isEmpty()) "Products: None Selected" else "Products: ${selectedProducts.size} Selected")
            }

            TextField(
                value = totalPrice.toString(),
                onValueChange = {totalPrice = it.toInt()},
                readOnly = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFFFA500),
                    unfocusedLabelColor = Color(0xFFA26D00)
                ),
                label = { Text("Total Price") }
            )

            Button(
                onClick = {
                    navController.navigate("select_promos")
                },
                modifier = Modifier.width(280.dp),
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
            ) {
                Text(selectedPromo?.Name ?: "Select Promo")
            }

            TextField(
                value = updatedPrice.toString(),
                onValueChange = {},
                readOnly = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFFFA500),
                    unfocusedLabelColor = Color(0xFFA26D00)
                ),
                label = { Text("Updated Price") }
            )

            ExposedDropdownMenuBox(
                expanded = expandedStat,
                onExpandedChange = { expandedStat = it }) {
                TextField(value = status,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStat) },
                    modifier = Modifier.menuAnchor(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFFFA500),
                        unfocusedLabelColor = Color(0xFFA26D00)
                    ),
                )
                ExposedDropdownMenu(
                    expanded = expandedStat,
                    onDismissRequest = { expandedStat = false }) {
                    statuses.forEach { option ->
                        DropdownMenuItem(text = { Text(option) }, onClick = {
                            status = option
                            expandedStat = false
                        })
                    }
                }
            }

            Box(
                modifier = Modifier
                    .clickable {
                        Log.d("DEBUG", "DatePicker clicked") // Debugging
                        showDatePicker.value = true
                    }
                    .background(Color(0xFFFFA500), shape = RoundedCornerShape(8.dp))
                    .padding(4.dp)
            ) {
                TextField(
                    value = orderDate,
                    onValueChange = {},
                    readOnly = true, // Prevents user input but allows clicks on parent Box
                    enabled = false, // Keeps the greyed-out effect
                    colors = TextFieldDefaults.colors(
                        disabledContainerColor = Color(0xFFFFA500), // Orange background
                        disabledTextColor = Color.Black, // Ensures text is visible
                        disabledLabelColor = Color(0xFFA26D00) // Darker orange for label
                    ),
                    label = { Text("Order Date") }
                )
            }

            ExposedDropdownMenuBox(
                expanded = expandedPay,
                onExpandedChange = { expandedPay = it }) {
                TextField(value = paymentType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Payment Method") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPay) },
                    modifier = Modifier.menuAnchor(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFFFA500),
                        unfocusedLabelColor = Color(0xFFA26D00)
                    )
                )
                ExposedDropdownMenu(
                    expanded = expandedPay,
                    onDismissRequest = { expandedPay = false }) {
                    paymentMethods.forEach { option ->
                        DropdownMenuItem(text = { Text(option) }, onClick = {
                            paymentType = option
                            expandedPay = false
                        })
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    // First, insert the order
                    val newOrder = Order(
                        orderID.toIntOrNull() ?: 0,
                        customerID.toIntOrNull() ?: 0,
                        updatedPrice, // Use the updated price after promo
                        selectedPromo?.Promo_ID ?: 0,
                        status,
                        LocalDate.parse(orderDate),
                        paymentType
                    )

                    // Get the actual inserted order ID
                    val insertedOrderId = if (orderHandler.insertData(newOrder)) {
                        // If order insertion is successful, get the latest order ID
                        orderHandler.getLatestOrderID()
                    } else {
                        // If order insertion fails, return without inserting order details
                        Toast.makeText(context, "Failed to insert order", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    // Then, insert order details for each selected product


                    selectedProducts.forEach { (product, quantity) ->
                        val orderDetail = OrderDetails(
                            orderID = insertedOrderId,
                            productID = product.Product_ID,
                            quantity = quantity
                        )
                        orderDetailHandler.insertData(orderDetail)
                    }

                    navController.popBackStack()
                },
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
                ) {
                    Text("Insert")
                }

                Button(onClick = {
                    if (orderDetailHandler.readData(tempOrderID).isNotEmpty()) {
                        orderDetailHandler.deleteData(tempOrderID)
                        navController.popBackStack()
                    }
                    navController.popBackStack()
                },
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
                ) {
                    Text("Back")
                }
            }

            if (showDatePicker.value) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker.value = false },
                    confirmButton = {
                        Button(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val selectedDate = Instant.ofEpochMilli(millis)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                                orderDate = selectedDate.toString()
                            }
                            showDatePicker.value = false
                        }) { Text("OK") }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            LaunchedEffect(datePickerState.selectedDateMillis) {
                datePickerState.selectedDateMillis?.let { millis ->
                    val selectedDate = Instant.ofEpochMilli(millis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    orderDate = selectedDate.toString()
                }
            }



        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SeeMenu(navController: NavController) {
    val context = LocalContext.current
    val productHandler = remember { ProductsHandler(context) }
    val products = remember { mutableStateOf(productHandler.readData()) }
    var selectedProduct by remember { mutableStateOf<Products?>(null) }



    Box(modifier = Modifier
        .background(Color.White)
        .fillMaxSize()){
        Image(
            painter = painterResource(id = R.drawable.foodstop_header),
            contentDescription = "My Image",
            modifier = Modifier
                .size(600.dp)
                .offset(y = -215.dp) // Adjust the image position
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)  // ✅ Allows LazyColumn to scroll
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 16.dp) // Prevents cut-off at the bottom
                ) {
                    items(products.value, key = { it.Product_ID }) { product ->
                        val isSelected = selectedProduct?.Product_ID == product.Product_ID
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable {
                                    Log.d("DB_QUERY", "Retrieved Product ID: ${product.Product_ID}")
                                    selectedProduct = product
                                },
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color.Gray else Color.White
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Product ID: ${product.Product_ID}")
                                Text("Product Name: ${product.Product_name}")
                                Text("Price: ${product.Price}")

                            }
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { navController.navigate("product_insert") },
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500)))
                {
                    Text("Insert")
                }

                Button(
                    onClick = { selectedProduct?.let { navController.navigate("product_edit/${it.Product_ID}") } },
                    enabled = selectedProduct != null,
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
                ) {
                    Text("Edit")
                }

                Button(
                    onClick = {
                        selectedProduct?.let { productHandler.deleteData(it.Product_ID) }
                        products.value = productHandler.readData()
                    },
                    enabled = selectedProduct != null,
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
                ) {
                    Text("Delete")
                }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProductInsert(navController: NavController){
    val context = LocalContext.current
    val productHandler = ProductsHandler(context)

    var productID by remember { mutableStateOf("")}
    var productName by remember { mutableStateOf("")}
    var price by remember { mutableStateOf("")}

    Box(modifier = Modifier
        .background(Color.White)
        .fillMaxSize()){
        Image(
            painter = painterResource(id = R.drawable.foodstop_header),
            contentDescription = "My Image",
            modifier = Modifier
                .size(600.dp)
                .offset(y = -215.dp) // Adjust the image position
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = 300.dp)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Add a Product", fontSize = 30.sp)
            TextField(
                value = productName,
                onValueChange = { productName = it },
                label = { Text("Product Name") })
            TextField(value = price, onValueChange = { price = it }, label = { Text("Price") })


            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    val newProduct = Products(
                        productID.toIntOrNull() ?: 0,
                        productName,
                        price.toIntOrNull() ?: 0,

                        )
                    productHandler.insertData(newProduct)
                    navController.popBackStack()
                },
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
                    ) {
                    Text("Insert")
                }

                Button(onClick = { navController.popBackStack() },
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
                ) {
                    Text("Back")
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProductEdit(navController: NavController, productID: Int? = null) {
    val context = LocalContext.current
    val productHandler = ProductsHandler(context)
    val product = remember { mutableStateOf(productID?.let { productHandler.readData(it).firstOrNull() }) }


    var productName by remember { mutableStateOf(product.value?.Product_name?.toString() ?: "") }
    var price by remember { mutableStateOf(product.value?.Price?.toString() ?: "") }


    Box(modifier = Modifier
        .background(Color.White)
        .fillMaxSize()){
        Image(
            painter = painterResource(id = R.drawable.foodstop_header),
            contentDescription = "My Image",
            modifier = Modifier
                .size(600.dp)
                .offset(y = -215.dp) // Adjust the image position
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            TextField(
                value = productName,
                onValueChange = { productName = it },
                label = { Text("Product Name") })
            TextField(value = price, onValueChange = { price = it }, label = { Text("Price") })


            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    val updatedProduct = Products(
                        product.value?.Product_ID ?: 0,
                        productName.toString() ?: "",
                        price.toIntOrNull() ?: 0,

                        ).apply { if (productID != null) this.Product_ID = productID }

                    if (productID != null) {
                        productHandler.updateData(updatedProduct)
                    } else {
                        productHandler.insertData(updatedProduct)
                    }
                    navController.popBackStack()
                },
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
                    ) {
                    Text("OK")
                }

                Button(onClick = { navController.popBackStack() },
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
                    ) {
                    Text("Back")
                }


            }
        }
    }
}

@Composable
fun CustomerScreen(navController: NavHostController) {
    val context = LocalContext.current
    val customerHandler = remember { CustomerHandler(context) }
    val customers = remember { mutableStateOf(customerHandler.readData()) }
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)  // ✅ Allows LazyColumn to scroll
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 16.dp) // Prevents cut-off at the bottom
                ) {
                    items(customers.value, key = { it.Customer_ID }) { customer ->
                        val isSelected = selectedCustomer?.Customer_ID == customer.Customer_ID
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable {
                                    Log.d(
                                        "DB_QUERY",
                                        "Retrieved Product ID: ${customer.Customer_ID}"
                                    )
                                    selectedCustomer = customer
                                },
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color.Gray else Color.White
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Customer ID: ${customer.Customer_ID}")
                                Text("Name: ${customer.Name}")
                                Text("Type: ${customer.Type}")
                                Text("Address: ${customer.Address}")

                            }
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { navController.navigate("customer_insert") },
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
                    ) {
                    Text("Insert")
                }

                Button(
                    onClick = { selectedCustomer?.let { navController.navigate("customer_edit/${it.Customer_ID}") } },
                    enabled = selectedCustomer != null,
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
                ) {
                    Text("Edit")
                }

                Button(
                    onClick = {
                        selectedCustomer?.let { customerHandler.deleteData(it.Customer_ID)}
                        customers.value = customerHandler.readData()
                    },
                    enabled = selectedCustomer != null,
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
                ) {
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
fun CustomerInsert(navController: NavController) {
    val context = LocalContext.current
    val customerHandler = CustomerHandler(context)

    var customerID by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    val customerTypes = listOf("New", "Regular", "Returning")
    var expandedCusTyp by remember { mutableStateOf(false)}

    Box(modifier = Modifier
        .background(Color.White)
        .fillMaxSize()){
        Image(
            painter = painterResource(id = R.drawable.foodstop_header),
            contentDescription = "My Image",
            modifier = Modifier
                .size(600.dp)
                .offset(y = -215.dp) // Adjust the image position
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = 300.dp)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Add a Customer", fontSize = 30.sp)
            TextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
            ExposedDropdownMenuBox(
                expanded = expandedCusTyp,
                onExpandedChange = { expandedCusTyp = it }) {
                TextField(value = type,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Customer Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCusTyp) },
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedCusTyp,
                    onDismissRequest = { expandedCusTyp = false }
                ) {
                    customerTypes.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                type = option
                                expandedCusTyp = false
                            }
                        )
                    }
                }
            }
            TextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address") })


            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    val newCustomer = Customer(
                        customerID.toIntOrNull() ?: 0,
                        name,
                        type,
                        address
                    )
                    customerHandler.insertData(newCustomer)
                    navController.popBackStack()
                },
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
                    ) {
                    Text("Insert")
                }

                Button(onClick = { navController.popBackStack() },
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
                ) {
                    Text("Back")
                }
            }
        }
    }
}

@Composable
fun CustomerEdit(navController: NavController, customerID: Int? = null){
    val context = LocalContext.current
    val customerHandler = CustomerHandler(context)
    val customer = remember { mutableStateOf(customerID?.let { customerHandler.readData(it).firstOrNull() }) }
    val customerTypes = listOf("New", "Regular", "Returning")
    var expandedCusTyp by remember { mutableStateOf(false)}

    var name by remember {mutableStateOf(customer.value?.Name?.toString() ?: "")}
    var type by remember {mutableStateOf(customer.value?.Type?.toString() ?: "")}
    var address by remember {mutableStateOf(customer.value?.Address?.toString() ?: "")}



    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(value = name, onValueChange = {name = it }, label = { Text("Name") })
        ExposedDropdownMenuBox(expanded = expandedCusTyp, onExpandedChange = {expandedCusTyp = it}) {
            TextField(value = type, onValueChange = {}, readOnly = true, label = {Text("Customer Type")},
                trailingIcon = {ExposedDropdownMenuDefaults.TrailingIcon(expanded=expandedCusTyp)},
                modifier = Modifier.menuAnchor())
            ExposedDropdownMenu(
                expanded = expandedCusTyp,
                onDismissRequest = {expandedCusTyp = false}
            ) {
                customerTypes.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            type = option
                            expandedCusTyp = false
                        }
                    )
                }
            }
        }
        TextField(value = address, onValueChange = { address = it }, label = { Text("Address") })


        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                val updatedCustomer = Customer(
                    customer.value?.Customer_ID ?: 0,
                    name,
                    type,
                    address
                ).apply {if (customerID != null) this.Customer_ID = customerID }

                if (customerID != null) {
                    customerHandler.updateData(updatedCustomer)
                } else {
                    customerHandler.insertData(updatedCustomer)
                }
                navController.popBackStack()
            }) {
                Text("OK")
            }

            Button(onClick = { navController.popBackStack() },
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
                ) {
                Text("Back")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val dbHelper = DatabaseHelper(context)
    val analyticsHandler = AnalyticsHandler(dbHelper.readableDatabase)

    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    // State for selected dates
    val startDate = remember { mutableStateOf(dateFormatter.format(Date())) }
    val endDate = remember { mutableStateOf(dateFormatter.format(Date())) }

    // State for showing DatePicker dialogs
    val showStartDatePicker = remember { mutableStateOf(false) }
    val showEndDatePicker = remember { mutableStateOf(false) }

    // State to store orders data
    val ordersData = remember { mutableStateOf(emptyList<Pair<String, Int>>()) }

    // Function to fetch and update orders data
    val fetchOrders = remember {
        {
            val fetchedOrders = analyticsHandler.getOrdersCountByDateRange(startDate.value, endDate.value)
            Log.d("DEBUG", "Fetched Orders: $fetchedOrders")

            if (fetchedOrders.isEmpty()) {
                Log.d("DEBUG", "No orders found in date range")
            } else {
                Log.d("DEBUG", "Updating ordersData with: $fetchedOrders")
            }

            ordersData.value = fetchedOrders
        }
    }

    // Fetch data when screen is first loaded
    LaunchedEffect(Unit) {
        fetchOrders()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Orders Graph
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Takes half of the screen
        ) {
            OrdersGraph(ordersData.value) // This will recompose when ordersData changes
        }

        Spacer(modifier = Modifier.height(16.dp))

        // DATE SELECTION ROW
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = { showStartDatePicker.value = true }) {
                Text(startDate.value)
            }

            Spacer(modifier = Modifier.width(20.dp))

            Text("→", fontSize = 24.sp, color = Color.Black) // Arrow between dates

            Spacer(modifier = Modifier.width(20.dp))

            Button(onClick = { showEndDatePicker.value = true }) {
                Text(endDate.value)
            }

            Spacer(modifier = Modifier.width(20.dp))

            Button(onClick = {
                ordersData.value = emptyList()
                fetchOrders()

            }) {
                Text("Update")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // START DATE PICKER DIALOG
    if (showStartDatePicker.value) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker.value = false },
            confirmButton = {
                Button(onClick = { showStartDatePicker.value = false }) { Text("OK") }
            }
        ) {
            val datePickerState = rememberDatePickerState()
            DatePicker(state = datePickerState)

            LaunchedEffect(datePickerState.selectedDateMillis) {
                datePickerState.selectedDateMillis?.let {
                    startDate.value = dateFormatter.format(Date(it))
                    Log.d("DEBUG", "Start Date Selected: ${startDate.value}")
                }
            }
        }
    }

    // END DATE PICKER DIALOG
    if (showEndDatePicker.value) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker.value = false },
            confirmButton = {
                Button(onClick = { showEndDatePicker.value = false }) { Text("OK") }
            }
        ) {
            val datePickerState = rememberDatePickerState()
            DatePicker(state = datePickerState)

            LaunchedEffect(datePickerState.selectedDateMillis) {
                datePickerState.selectedDateMillis?.let {
                    endDate.value = dateFormatter.format(Date(it))
                    Log.d("DEBUG", "End Date Selected: ${endDate.value}")
                }
            }
        }
    }
}




@Composable
fun OrdersGraph(ordersCount: List<Pair<String, Int>>) {
    Log.d("DEBUG", "OrdersGraph Received Data: $ordersCount")

    if (ordersCount.isEmpty()) {
        Log.d("DEBUG", "No data for graph")
        return
    }

    AndroidView(
        factory = { ctx ->
            BarChart(ctx).apply {
                description.isEnabled = false
                setFitBars(true)

                // Convert dates into index-based entries
                val entries = ordersCount.mapIndexed { index, data ->
                    Log.d("DEBUG", "Mapping Data: index=$index, date=${data.first}, count=${data.second}")
                    BarEntry(index.toFloat(), data.second.toFloat())
                }

                if (entries.isEmpty()) {
                    Log.d("DEBUG", "Graph Entries is empty!")
                } else {
                    Log.d("DEBUG", "Graph Entries: $entries")
                }

                val dataSet = BarDataSet(entries, "Orders Per Day").apply {
                    color = Color.Blue.toArgb()
                    valueTextSize = 12f
                }

                val barData = BarData(dataSet)
                this.data = barData

                // Format X-axis labels to show dates
                val xAxis = this.xAxis
                xAxis.valueFormatter = IndexAxisValueFormatter(ordersCount.map { it.first }) // Map indices to dates
                xAxis.granularity = 1f
                xAxis.position = XAxis.XAxisPosition.BOTTOM

                axisLeft.granularity = 1f // Ensure whole numbers on Y-axis
                axisRight.isEnabled = false

                // Refresh chart
                invalidate()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(16.dp)
    )
}








@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SelectProducts(navController: NavController) {
    val context = LocalContext.current
    val productHandler = remember { ProductsHandler(context) }
    val products = remember { mutableStateOf(productHandler.readData()) }
    val gson = Gson()

    val receivedProducts = remember {
        navController.previousBackStackEntry
            ?.savedStateHandle
            ?.get<String>("selected_products")
            ?.let { json ->
                Log.d("DEBUG", "Retrieved JSON: $json")  // Debugging
                val type = object : TypeToken<List<Map<String, Any>>>() {}.type
                val rawList: List<Map<String, Any>> = gson.fromJson(json, type)

                rawList.mapNotNull { map ->
                    val productMap = map["first"] as? Map<String, Any> ?: return@mapNotNull null
                    val quantity = (map["second"] as? Double)?.toInt() ?: return@mapNotNull null

                    val productJson = gson.toJson(productMap)
                    val product = gson.fromJson(productJson, Products::class.java)

                    product to quantity
                }
            } ?: emptyList()
    }

    val selectedProducts = remember { mutableStateListOf<Pair<Products, Int>>() }
    var showDialog by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<Products?>(null) }
    var quantity by remember { mutableStateOf("") }

    // Add received products to the list with default quantity
    LaunchedEffect(Unit) {
        Log.d("DEBUG", "Clearing selectedProducts")
        selectedProducts.clear()
        Log.d("DEBUG", "Received products: $receivedProducts")
        selectedProducts.addAll(receivedProducts)
        Log.d("DEBUG", "Updated selectedProducts: $selectedProducts")
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Select Products",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(products.value, key = { it.Product_ID }) { product ->
                val selected = selectedProducts.find { it.first.Product_ID == product.Product_ID }
                val isSelected = selected != null

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable {
                            selectedProduct = product
                            quantity = selected?.second?.toString() ?: ""
                            showDialog = true
                        },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color.Gray else Color.White
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Product ID: ${product.Product_ID}")
                            Text("Product Name: ${product.Product_name}")
                            Text("Price: ${product.Price}")
                        }
                        if (isSelected) {
                            Text("Qty: ${selected?.second}", color = Color.Blue)
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = { navController.popBackStack() }) {
                Text("Cancel")
            }

            Text("${selectedProducts.size} Products Selected")

            Button(
                onClick = {
                    val filteredProducts = selectedProducts.filter { it.second > 0 }
                    val selectedProductsJson = gson.toJson(filteredProducts.map {
                        mapOf("first" to it.first, "second" to it.second)
                    })

                    Log.d("DEBUG", "Saving selected products: $selectedProductsJson")

                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("selected_products", selectedProductsJson)

                    navController.popBackStack()
                },
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
            ) {
                Text("Ok")
            }
        }
    }

    // Quantity Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Enter Quantity") },
            text = {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (quantity.isNotBlank()) {
                            val qty = quantity.toInt()
                            if (qty > 0) {
                                // If quantity is greater than 0, update the list
                                selectedProducts.removeIf { it.first.Product_ID == selectedProduct?.Product_ID }
                                selectedProducts.add(selectedProduct!! to qty)
                            } else {
                                // If quantity is 0, remove the product
                                selectedProducts.removeIf { it.first.Product_ID == selectedProduct?.Product_ID }
                            }
                            showDialog = false
                        }

                        Log.d("DEBUG", "Updated selectedProducts: ${selectedProducts}")
                    },
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
                ) {
                    Text("Ok")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false },
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))) {
                    Text("Cancel")
                }
            }
        )
    }
}




@Composable
fun PromoInsert(navController: NavController){
    val context = LocalContext.current
    val promoHandler = PromosHandler(context)

    var promoID by remember { mutableStateOf("")}
    var name by remember { mutableStateOf("")}
    var type by remember { mutableStateOf("")}
    var discountPercent by remember { mutableStateOf("")}
    var discountFlat by remember { mutableStateOf("")}
    val types = listOf("Percentage", "Flat")
    var expandedType by rememberSaveable { mutableStateOf(false) }
    var percentField by rememberSaveable { mutableStateOf(false)}
    var flatField by rememberSaveable { mutableStateOf(false)}

    Box(modifier = Modifier
        .background(Color.White)
        .fillMaxSize()){
        Image(
            painter = painterResource(id = R.drawable.foodstop_header),
            contentDescription = "My Image",
            modifier = Modifier
                .size(600.dp)
                .offset(y = -215.dp) // Adjust the image position
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = 300.dp)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Add a Promo",
                fontSize = 30.sp
            )

            TextField(
                value = name,
                onValueChange = { name = it },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFFFA500),
                    unfocusedLabelColor = Color(0xFFA26D00)
                ),
                label = { Text("Promo Name") }
            )

            ExposedDropdownMenuBox(
                expanded = expandedType,
                onExpandedChange = { expandedType = it }) {
                TextField(value = type,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                    modifier = Modifier.menuAnchor(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFFFA500),
                        unfocusedLabelColor = Color(0xFFA26D00)
                    ),
                )
                ExposedDropdownMenu(
                    expanded = expandedType,
                    onDismissRequest = { expandedType = false }) {
                    types.forEach { option ->
                        DropdownMenuItem(text = { Text(option) }, onClick = {
                            type = option
                            expandedType = false
                            if(option=="Flat") {
                                flatField = true
                                percentField = false
                            }
                            else if (option=="Percentage"){
                                flatField=false
                                percentField=true
                            }

                        })
                    }
                }
            }
            if(percentField){
                TextField(
                    value = discountPercent,
                    onValueChange = { discountPercent = it },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFFFA500),
                        unfocusedLabelColor = Color(0xFFA26D00)
                    ),

                    label = { Text("Discount Percent") })
            }

            if(flatField){
                TextField(
                    value = discountFlat,
                    onValueChange = { discountFlat = it },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFFFA500),
                        unfocusedLabelColor = Color(0xFFA26D00)
                    ),

                    label = { Text("Discount Flat") })
            }


            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    val newPromo = Promos(
                        promoID.toIntOrNull() ?: 0,
                        name,
                        type,
                        discountPercent.toIntOrNull() ?: 0,
                        discountFlat.toIntOrNull() ?: 0

                    )
                    promoHandler.insertData(newPromo)
                    navController.popBackStack()
                },
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
                ) {
                    Text("Insert")
                }

                Button(
                    onClick = {
                        navController.popBackStack()
                    },
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))) {
                    Text("Back")
                }
            }
        }
    }
}

@Composable
fun PromoEdit(navController: NavController, promoID: Int? = null) {
    val context = LocalContext.current
    val promosHandler = PromosHandler(context)
    val promo = remember { mutableStateOf(promoID?.let { promosHandler.readData(it).firstOrNull() }) }

    var name by remember { mutableStateOf(promo.value?.Name ?: "") }
    var type by remember { mutableStateOf(promo.value?.Type ?: "") }
    var discountPercent by remember { mutableStateOf(promo.value?.DiscountPercent?.toString() ?: "") }
    var discountFlat by remember { mutableStateOf(promo.value?.DiscountFlat?.toString() ?: "") }

    val types = listOf("Percentage", "Flat")
    var expandedType by rememberSaveable { mutableStateOf(false) }

    // Initialize percentField and flatField based on the loaded promo type
    var percentField by rememberSaveable { mutableStateOf(promo.value?.Type == "Percentage") }
    var flatField by rememberSaveable { mutableStateOf(promo.value?.Type == "Flat") }

    Box(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.foodstop_header),
            contentDescription = "My Image",
            modifier = Modifier
                .size(600.dp)
                .offset(y = -215.dp) // Adjust the image position
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = 300.dp) // Match the offset from PromoInsert
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Edit Promo",
                fontSize = 30.sp
            )

            TextField(
                value = name,
                onValueChange = { name = it },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFFFA500),
                    unfocusedLabelColor = Color(0xFFA26D00)
                ),
                label = { Text("Promo Name") }
            )

            ExposedDropdownMenuBox(
                expanded = expandedType,
                onExpandedChange = { expandedType = it }
            ) {
                TextField(
                    value = type,
                    onValueChange = { type = it },
                    readOnly = true,
                    label = { Text("Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                    modifier = Modifier.menuAnchor(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFFFA500),
                        unfocusedLabelColor = Color(0xFFA26D00)
                    ),
                )
                ExposedDropdownMenu(
                    expanded = expandedType,
                    onDismissRequest = { expandedType = false }
                ) {
                    types.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                type = option
                                expandedType = false
                                if (option == "Flat") {
                                    flatField = true
                                    percentField = false
                                } else if (option == "Percentage") {
                                    flatField = false
                                    percentField = true
                                }
                            }
                        )
                    }
                }
            }

            if (percentField) {
                TextField(
                    value = discountPercent,
                    onValueChange = { discountPercent = it },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFFFA500),
                        unfocusedLabelColor = Color(0xFFA26D00)
                    ),
                    label = { Text("Discount Percent") }
                )
            }

            if (flatField) {
                TextField(
                    value = discountFlat,
                    onValueChange = { discountFlat = it },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFFFA500),
                        unfocusedLabelColor = Color(0xFFA26D00)
                    ),
                    label = { Text("Discount Flat") }
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val updatedPromo = Promos(
                            promo.value?.Promo_ID ?: 0,
                            name,
                            type,
                            discountPercent.toIntOrNull() ?: 0,
                            discountFlat.toIntOrNull() ?: 0
                        ).apply { if (promoID != null) this.Promo_ID = promoID }

                        if (promoID != null) {
                            promosHandler.updateData(updatedPromo)
                        } else {
                            promosHandler.insertData(updatedPromo)
                        }

                        navController.popBackStack()
                    },
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
                ) {
                    Text("Update")
                }

                Button(
                    onClick = {
                        navController.popBackStack()
                    },
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
                ) {
                    Text("Back")
                }
            }
        }
    }
}

@Composable
fun PromoScreen(navController: NavController){
    val context = LocalContext.current
    val promoHandler = remember { PromosHandler(context) }
    val promos = remember { mutableStateOf(promoHandler.readData()) }
    var selectedPromo by remember { mutableStateOf<Promos?>(null) }

    Box(modifier = Modifier
        .background(Color.White)
        .fillMaxSize()){
        Image(
            painter = painterResource(id = R.drawable.foodstop_header),
            contentDescription = "My Image",
            modifier = Modifier
                .size(600.dp)
                .offset(y = -215.dp) // Adjust the image position
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)  // ✅ Allows LazyColumn to scroll
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 16.dp) // Prevents cut-off at the bottom
                ) {
                    items(promos.value, key = { it.Promo_ID }) { promo ->
                        val isSelected = selectedPromo?.Promo_ID == promo.Promo_ID


                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable {
                                    Log.d("DB_QUERY", "Retrieved Order ID: ${promo.Promo_ID}")
                                    selectedPromo = promo
                                },
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color.Gray else Color.White
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Promo ID: ${promo.Promo_ID}")
                                Text("Promo Name: ${promo.Name}")
                                Text("Type: ${promo.Type}")
                                Text("Discount Percent: ${promo.DiscountPercent}%")
                                Text("Discount Flat: ${promo.DiscountFlat}")

                            }
                        }
                    }
                }
            }

            // Rest of the existing code remains the same
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Button(onClick = { navController.navigate("promo_insert") },
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
                ) {
                    Text("Insert")
                }

                Button(
                    onClick = { selectedPromo?.let { navController.navigate("promo_edit/${it.Promo_ID}") } },
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500)),
                    enabled = selectedPromo != null
                ) {
                    Text("Edit")
                }
                Button(
                    onClick = {
                        selectedPromo?.let {
                            promoHandler.deleteData(it.Promo_ID)
                            promos.value = promoHandler.readData()
                        }
                    },
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500)),
                    enabled = selectedPromo != null
                ) {
                    Text("Delete")
                }
            }
        }
    }
}


@Composable
fun SeePromo(navController: NavController) {
    val context = LocalContext.current
    val promoHandler = remember { PromosHandler(context) }
    val promos = remember { mutableStateOf(promoHandler.readData()) }
    val gson = Gson()

    var selectedPromo by remember { mutableStateOf<Promos?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Select Promo",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(promos.value, key = { it.Promo_ID }) { promo ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable {
                            selectedPromo = promo
                            // Pass back the selected promo to the previous screen
                            navController.previousBackStackEntry?.savedStateHandle?.set(
                                "selected_promo",
                                gson.toJson(promo)
                            )
                            navController.popBackStack()
                        },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Promo ID: ${promo.Promo_ID}")
                            Text("Name: ${promo.Name}")
                            Text("Type: ${promo.Type}")
                            Text("Discount %: ${promo.DiscountPercent}")
                            Text("Discount Flat: ${promo.DiscountFlat}")
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { navController.popBackStack() },
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
            ) {
                Text("Cancel")
            }

            
        }
    }
}
