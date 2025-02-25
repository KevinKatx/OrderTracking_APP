package com.example.ordertrackingapp

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
import java.time.LocalDate
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import com.example.ordertrackingapp.databases.Tables.*
import com.example.ordertrackingapp.databases.handlers.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OrderScreen(navController: NavController) {
    val context = LocalContext.current
    val orderHandler = remember { OrderHandler(context) }
    val orders = remember { mutableStateOf(orderHandler.readData()) }
    var selectedOrder by remember { mutableStateOf<Order?>(null) }

    // Log orders when the screen is first composed
    LaunchedEffect(Unit) {
        Log.d("DB_DEBUG", "Orders retrieved: ${orders.value}")
    }

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
                        }
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { navController.navigate("order_insert") }) {
                Text("Insert")
            }

            Button(onClick = { orders.value = orderHandler.readData() }) {
                Text("Read")
            }

            Button(
                onClick = { selectedOrder?.let { navController.navigate("order_edit/${it.orderID}") } },
                enabled = selectedOrder != null
            ) {
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
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
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
                    order.value?.orderID ?: 0,
                    customerID.toIntOrNull() ?: 0,
                    totalPrice.toFloatOrNull() ?: 0f,
                    promoID.toIntOrNull() ?: 0,
                    status,
                    LocalDate.parse(orderDate),
                    paymentType
                ).apply {if (orderID != null) this.orderID = orderID }

                if (orderID != null) {
                    orderHandler.updateData(updatedOrder)
                } else {
                    orderHandler.insertData(updatedOrder)
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


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OrderInsert(navController: NavController) {
    val context = LocalContext.current
    val orderHandler = OrderHandler(context)

    var orderID by remember { mutableStateOf("") }
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
        TextField(value = customerID, onValueChange = { customerID = it }, label = { Text("Customer ID") })
        TextField(value = totalPrice, onValueChange = { totalPrice = it }, label = { Text("Total Price") })
        TextField(value = promoID, onValueChange = { promoID = it }, label = { Text("Promo ID") })
        TextField(value = status, onValueChange = { status = it }, label = { Text("Status") })
        TextField(value = orderDate, onValueChange = { orderDate = it }, label = { Text("Order Date") })
        TextField(value = paymentType, onValueChange = { paymentType = it }, label = { Text("Payment Type") })

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                val newOrder = Order(
                    orderID.toIntOrNull() ?: 0,
                    customerID.toIntOrNull() ?: 0,
                    totalPrice.toFloatOrNull() ?: 0f,
                    promoID.toIntOrNull() ?: 0,
                    status,
                    LocalDate.parse(orderDate),
                    paymentType
                )
                orderHandler.insertData(newOrder)
                navController.popBackStack()
            }) {
                Text("Insert")
            }

            Button(onClick = { navController.popBackStack() }) {
                Text("Back")
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
                    selectedProduct?.let { productHandler.deleteData(it.Product_ID)}
                    products.value = productHandler.readData()
                },
                enabled = selectedProduct != null
            ) {
                Text("Delete")
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(value = productName, onValueChange = { productName = it }, label = { Text("Product Name") })
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
            }) {
                Text("Insert")
            }

            Button(onClick = { navController.popBackStack() }) {
                Text("Back")
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


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        TextField(value = productName, onValueChange = { productName = it }, label = { Text("Product Name") })
        TextField(value = price, onValueChange = { price = it }, label = { Text("Price") })


        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                val updatedProduct = Products(
                    product.value?.Product_ID ?: 0,
                    productName.toString() ?: "",
                    price.toIntOrNull() ?: 0,

                ).apply {if (productID != null) this.Product_ID = productID }

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
                                    Log.d("DB_QUERY", "Retrieved Product ID: ${customer.Customer_ID}")
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


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
        TextField(value = type, onValueChange = { type = it }, label = { Text("Type") })
        TextField(value = address, onValueChange = { address = it }, label = { Text("Address") })


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
            }) {
                Text("Insert")
            }

            Button(onClick = { navController.popBackStack() }) {
                Text("Back")
            }
        }
    }
}

@Composable
fun CustomerEdit(navController: NavController, customerID: Int? = null){
    val context = LocalContext.current
    val customerHandler = CustomerHandler(context)
    val customer = remember { mutableStateOf(customerID?.let { customerHandler.readData(it).firstOrNull() }) }



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
        TextField(value = type, onValueChange = { type = it }, label = { Text("Type") })
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