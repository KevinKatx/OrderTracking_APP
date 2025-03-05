@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ordertrackingapp

import android.os.Build
import android.util.Log
import androidx.compose.ui.unit.sp
import android.widget.Toast
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
import java.time.LocalDate
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.input.KeyboardType
import com.example.ordertrackingapp.databases.Tables.*
import com.example.ordertrackingapp.databases.handlers.*
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OrderScreen(navController: NavController) {
    val context = LocalContext.current
    val orderHandler = remember { OrderHandler(context) }
    val orderDetailHandler = remember { OrderDetailsHandler(context) }
    val orders = remember { mutableStateOf(orderHandler.readData()) }
    var selectedOrder by remember { mutableStateOf<Order?>(null) }

    // Function to get product IDs for an order
    fun getProductIdsForOrder(orderID: Int): String {
        val orderDetails = orderDetailHandler.readData(orderID)
        return orderDetails.map { it.productID }.joinToString(", ")
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
                                Text("Promo ID: ${order.promoID}")
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
                Button(onClick = { navController.navigate("order_insert") }) {
                    Text("Insert")
                }

                Button(
                    onClick = { selectedOrder?.let { navController.navigate("order_edit/${it.orderID}") } },
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
                    enabled = selectedOrder != null
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
    val order = remember { mutableStateOf(orderID?.let { orderHandler.readData(it).firstOrNull() }) }

    var customerID by remember { mutableStateOf(order.value?.customerID?.toString() ?: "") }
    var totalPrice by remember { mutableStateOf(order.value?.totalPrice?.toString() ?: "") }
    var promoID by remember { mutableStateOf(order.value?.promoID?.toString() ?: "") }
    var status by remember { mutableStateOf(order.value?.status ?: "") }
    var orderDate by remember { mutableStateOf(order.value?.orderDate?.toString() ?: LocalDate.now().toString()) }
    var paymentType by remember { mutableStateOf(order.value?.paymentType ?: "") }
    var expandedStat by remember { mutableStateOf(false)}
    val statuses = listOf("Pending", "Completed", "Cancelled")
    val paymentMethods = listOf("Cash On Delivery", "GCash", "CreditCard")
    var expandedPay by remember { mutableStateOf(false)}
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
                value = customerID,
                onValueChange = { customerID = it },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFFFA500),
                    unfocusedLabelColor = Color(0xFFA26D00)
                ),
                label = { Text("Customer ID") })

            TextField(
                value = totalPrice,
                onValueChange = { totalPrice = it },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFFFA500),
                    unfocusedLabelColor = Color(0xFFA26D00)
                ),
                label = { Text("Total Price") })
            TextField(
                value = promoID,
                onValueChange = { promoID = it },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFFFA500),
                    unfocusedLabelColor = Color(0xFFA26D00)
                ),
                label = { Text("Promo ID") })
            ExposedDropdownMenuBox(expanded = expandedStat, onExpandedChange = { expandedStat = it }
            ) {
                TextField(
                    value = status,
                    onValueChange = {},
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFFFA500),
                        unfocusedLabelColor = Color(0xFFA26D00)
                    ),
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
            TextField(
                value = orderDate,
                onValueChange = { orderDate = it },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFFFA500),
                    unfocusedLabelColor = Color(0xFFA26D00)
                ),
                label = { Text("Order Date") })
            ExposedDropdownMenuBox(
                expanded = expandedPay,
                onExpandedChange = { expandedPay = it }) {
                TextField(value = paymentType,
                    onValueChange = {},
                    readOnly = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFFFA500),
                        unfocusedLabelColor = Color(0xFFA26D00)
                    ),
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
                        totalPrice.toFloatOrNull() ?: 0f,
                        promoID.toIntOrNull() ?: 0,
                        status,
                        LocalDate.parse(orderDate),
                        paymentType
                    ).apply { if (orderID != null) this.orderID = orderID }

                    if (orderID != null) {
                        orderHandler.updateData(updatedOrder)
                    } else {
                        orderHandler.insertData(updatedOrder)
                    }
                    navController.popBackStack()
                },
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
                ) {
                    Text("OK")
                }

                Button(onClick = { navController.popBackStack() },  shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))) {
                    Text("Back")
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

    var tempOrderID by remember { mutableStateOf(orderHandler.getLatestOrderID()) }
    var orderID by remember { mutableStateOf("") }
    var customerID by rememberSaveable { mutableStateOf("") }
    var totalPrice by remember { mutableStateOf(0) }
    var promoID by rememberSaveable { mutableStateOf("") }
    var status by rememberSaveable { mutableStateOf("") }
    var orderDate by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var paymentType by rememberSaveable { mutableStateOf("") }
    var expandedStat by rememberSaveable { mutableStateOf(false) }
    val statuses = listOf("Pending", "Completed", "Cancelled")
    val paymentMethods = listOf("Cash On Delivery", "GCash", "CreditCard")
    var expandedPay by rememberSaveable { mutableStateOf(false) }

    var selectedProducts by remember { mutableStateOf<List<Pair<Products, Int>>>(emptyList()) } // Product + Quantity Pair

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

                // Clear list every time before adding new products
                selectedProducts = emptyList()

                selectedProducts = productList.map { map ->
                    val productMap = map["first"] as? Map<String, Any> ?: emptyMap()

                    val product = Products(
                        productMap["Product_ID"] as? Int ?: 0,
                        productMap["Product_name"] as? String ?: "",
                        (productMap["Price"] as? Double)?.toInt() ?: 0
                    )

                    val quantity = (map["second"] as? Double)?.toInt() ?: 0

                    Log.d("DEBUG", "Product: ${product.Product_name}, Quantity: $quantity")

                    Pair(product, quantity)
                }

                // Calculate Total Price
                totalPrice = selectedProducts.sumOf { (product, quantity) ->
                    product.Price * quantity
                }
                Log.d("DEBUG", "Total Price: $totalPrice")
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
            TextField(
                value = customerID,
                onValueChange = { customerID = it },
                label = { Text("Customer ID") }

            )

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

            TextField(
                value = promoID,
                onValueChange = { promoID = it },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFFFA500),
                    unfocusedLabelColor = Color(0xFFA26D00)
                ),
                label = { Text("Promo ID") })

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

            TextField(
                value = orderDate,
                onValueChange = { orderDate = it },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFFFA500),
                    unfocusedLabelColor = Color(0xFFA26D00)
                ),
                label = { Text("Order Date") })

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
                        totalPrice.toFloat(),
                        promoID.toIntOrNull() ?: 0,
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
                Button(onClick = { navController.navigate("product_insert") }) {
                    Text("Insert")
                }

                Button(
                    onClick = { selectedProduct?.let { navController.navigate("product_edit/${it.Product_ID}") } },
                    enabled = selectedProduct != null
                ) {
                    Text("Edit")
                }

                Button(
                    onClick = {
                        selectedProduct?.let { productHandler.deleteData(it.Product_ID) }
                        products.value = productHandler.readData()
                    },
                    enabled = selectedProduct != null
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
            Text(
                text = "Add a Product",
                fontSize = 30.sp
            )
            TextField(
                value = productName,
                onValueChange = { productName = it },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFFFA500),
                    unfocusedLabelColor = Color(0xFFA26D00)
                ),

                label = { Text("Product Name") })
            TextField(
                value = price,
                onValueChange = { price = it },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFFFA500),
                    unfocusedLabelColor = Color(0xFFA26D00)
                ),

                label = { Text("Price") })


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
                }) {
                    Text("OK")
                }

                Button(onClick = { navController.popBackStack() }) {
                    Text("Back")
                }


            }
        }
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
                Button(onClick = { navController.navigate("customer_insert") }) {
                    Text("Insert")
                }

                Button(
                    onClick = { selectedCustomer?.let { navController.navigate("customer_edit/${it.Customer_ID}") } },
                    enabled = selectedCustomer != null
                ) {
                    Text("Edit")
                }

                Button(
                    onClick = {
                        selectedCustomer?.let { customerHandler.deleteData(it.Customer_ID)}
                        customers.value = customerHandler.readData()
                    },
                    enabled = selectedCustomer != null
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
            Text(
                text = "Add Customer",
                fontSize = 30.sp
            )
            TextField(value = name, onValueChange = { name = it }, colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFFFA500),
                unfocusedLabelColor = Color(0xFFA26D00)
            ),label = { Text("Name") })
            ExposedDropdownMenuBox(
                expanded = expandedCusTyp,
                onExpandedChange = { expandedCusTyp = it }) {
                TextField(value = type,
                    onValueChange = {},
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFFFA500),
                        unfocusedLabelColor = Color(0xFFA26D00)
                    ),
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
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFFFA500),
                    unfocusedLabelColor = Color(0xFFA26D00)
                ),
                label = { Text("Address") }
            )


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

                Button(onClick = { navController.popBackStack()}, shape = RoundedCornerShape(0.dp),
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

            Button(onClick = { navController.popBackStack() }) {
                Text("Back")
            }
        }
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SelectProducts(navController: NavController) {
    val context = LocalContext.current
    val productHandler = remember { ProductsHandler(context) }
    val products = remember { mutableStateOf(productHandler.readData()) }
    val gson = Gson()

    val receivedProducts = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<String>("selected_products")
        ?.let { json ->
            gson.fromJson(json, Array<Products>::class.java).toList()
        } ?: emptyList()

    val selectedProducts = remember { mutableStateListOf<Pair<Products, Int>>() }
    var showDialog by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<Products?>(null) }
    var quantity by remember { mutableStateOf("") }

    // Add received products to the list with default quantity
    LaunchedEffect(true) {
        // Always clear the list every time the screen is opened
        selectedProducts.clear()
        receivedProducts.forEach {
            selectedProducts.add(it to 1) // Add received products with default quantity
        }
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
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("selected_products", gson.toJson(selectedProducts))
                    navController.popBackStack()
                }
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
                Button(onClick = {
                    if (quantity.isNotBlank()) {
                        val qty = quantity.toInt()
                        if (qty > 0) {
                            selectedProducts.removeIf { it.first.Product_ID == selectedProduct?.Product_ID }
                            selectedProducts.add(selectedProduct!! to qty)
                        }
                        showDialog = false
                    }
                }) {
                    Text("Ok")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
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
                Button(onClick = { navController.navigate("promo_insert") }) {
                    Text("Insert")
                }

                Button(
                    onClick = { selectedPromo?.let { navController.navigate("promo_edit/${it.Promo_ID}") } },
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
                    enabled = selectedPromo != null
                ) {
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
fun PromoInsert(navController: NavController){
    val context = LocalContext.current
    val promoHandler = PromosHandler(context)

    var promoID by remember { mutableStateOf("")}
    var type by remember { mutableStateOf("")}
    var discountPercent by remember { mutableStateOf("")}
    var discountFlat by remember { mutableStateOf("")}

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
                value = type,
                onValueChange = { type = it },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFFFA500),
                    unfocusedLabelColor = Color(0xFFA26D00)
                ),

                label = { Text("Type") })
            TextField(
                value = discountPercent,
                onValueChange = { discountPercent = it },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFFFA500),
                    unfocusedLabelColor = Color(0xFFA26D00)
                ),

                label = { Text("Discount Percent") })

            TextField(
                value = discountFlat,
                onValueChange = { discountFlat = it },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFFFA500),
                    unfocusedLabelColor = Color(0xFFA26D00)
                ),

                label = { Text("Discount Flat") })


            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    val newPromo = Promos(
                        promoID.toIntOrNull() ?: 0,
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
