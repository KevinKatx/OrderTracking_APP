package com.example.ordertrackingapp

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import android.graphics.Paint.Align
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ordertrackingapp.ui.theme.OrderTrackingAPPTheme
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.navigation.NavHostController
import com.example.ordertrackingapp.databases.Tables.Order
import com.example.ordertrackingapp.databases.handlers.OrderHandler
import java.time.LocalDate


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Hide the status bar
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars()) // ✅ This works on lower APIs
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        setContent {
            OrderTrackingAPPTheme {
                AppNavigation()

            }
        }
    }
}


class CustomButton(
    val label: String,
    val onClick: () -> Unit,
    val iconId: Int,
    val backgroundColor: Color = Color(0xFFF6B819),
    val contentColor: Color = Color.Black
) {
    @Composable
    fun CreateButton() {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()

        // Animate the button's background color based on the pressed state
        val animatedBackgroundColor by animateColorAsState(
            targetValue = if (isPressed) Color.Black else backgroundColor,
            label = "Button Background Color Animation"
        )

        // Animate the content color based on the pressed state
        val animatedContentColor by animateColorAsState(
            targetValue = if (isPressed) Color(0xFFF6B819) else contentColor,
            label = "Button Content Color Animation"
        )

        // Animate the scale of the button based on the pressed state
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.95f else 1f,
            label = "Button Scale Animation"
        )

        Button(
            onClick = { onClick() }, // Call the provided onClick function
            modifier = Modifier
                .width(200.dp)
                .height(120.dp)
                .padding(10.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale),
            interactionSource = interactionSource, // Pass the interaction source
            shape = RoundedCornerShape(8.dp), // Optional: Add rounded corners
            colors = ButtonDefaults.buttonColors(
                containerColor = animatedBackgroundColor,
                contentColor = animatedContentColor
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = iconId),
                    contentDescription = label,
                    modifier = Modifier.size(45.dp),
                    tint = animatedContentColor // Use animated content color
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = label,
                    color = animatedContentColor // Use animated content color
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true,
    widthDp=360,
    heightDp=806
)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController = navController)
        }
        composable("login") {
            LoginScreen(navController = navController) // Your Login Screen Composable
        }
        composable("order") {
            OrderScreen(navController = navController)
        }
        composable("inventory") {
            InventoryScreen(navController = navController)
        }
        composable("analytics") {
            AnalyticsScreen(navController = navController)
        }
        composable("customer") {
            CustomerScreen(navController = navController)
        }

    }
}

@Composable
fun InventoryScreen(navController: NavHostController) {

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


@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    var text by remember { mutableStateOf("Initial") }

    // Use a Box to position elements manually
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

        // Center the column content
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .wrapContentSize(),  // Avoid filling the entire screen
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            TextField(
                value = email,
                onValueChange = {email = it},
                label = {Text("Username")},
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(40.dp))

            TextField(
                value = password,
                onValueChange = {password = it},
                label = {Text("Password")},
                singleLine = true,
                visualTransformation = PasswordVisualTransformation() // Hides password
            )
        }

        // Button placement
        SubmitBTN(onClick = {
            if((email == "admin" && password == "Password")||(email == "" && password == "")){
                navController.navigate("home")
            } else {
                Toast.makeText(context, "Invalid Username or Password!", Toast.LENGTH_SHORT).show()
            }

        })

        // Forgot password and Register User links
        Row(
            modifier = Modifier
                .offset(y = 650.dp)
                .fillMaxWidth(),  // Ensure the Row takes up the full width
            horizontalArrangement = Arrangement.Center

        ) {
            Text(
                "Forgot Password",
                modifier = Modifier
                    .clickable {
                        Toast.makeText(
                            context,
                            "Forgot Password Clicked",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .padding(0.dp, 0.dp, 0.dp, 0.dp)
            )
            Text(
                "Register User",
                modifier = Modifier
                    .clickable {
                        Toast.makeText(context, "Register Clicked", Toast.LENGTH_SHORT).show()
                    }
                    .padding(40.dp, 0.dp, 0.dp, 0.dp)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OrderScreen(navController: NavController) {
    val context = LocalContext.current
    val orderHandler = OrderHandler(context)

    var customerID by remember { mutableStateOf("") }
    var totalPrice by remember { mutableStateOf("") }
    var promoID by remember { mutableStateOf("") }
    var dishName by remember { mutableStateOf("") }
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
        TextField(value = dishName, onValueChange = { dishName = it }, label = { Text("Dish Name") })
        TextField(value = status, onValueChange = { status = it }, label = { Text("Status") })
        TextField(value = orderDate, onValueChange = { orderDate = it }, label = { Text("Order Date") })
        TextField(value = paymentType, onValueChange = { paymentType = it }, label = { Text("Payment Type") })

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                val order = Order(
                    customerID.toIntOrNull() ?: 0,
                    totalPrice.toIntOrNull() ?: 0,
                    promoID.toIntOrNull() ?: 0,
                    dishName,
                    status,
                    LocalDate.parse(orderDate),
                    paymentType
                )
                orderHandler.insertData(order)
            }) {
                Text("Insert")
            }

            Button(onClick = {
                val orders = orderHandler.readData()
                orders.forEach { Log.d("Order", it.toString()) }
            }) {
                Text("Read")
            }

            Button(onClick = {
                val order = Order(
                    customerID.toIntOrNull() ?: 0,
                    totalPrice.toIntOrNull() ?: 0,
                    promoID.toIntOrNull() ?: 0,
                    dishName,
                    status,
                    LocalDate.parse(orderDate),
                    paymentType
                )
                order.order_ID = 1 // Assume updating order with ID 1, modify as needed
                orderHandler.updateData(order)
            }) {
                Text("Update")
            }
        }
    }
}


@Composable
fun SubmitBTN(onClick: () -> Unit) {
    FilledTonalButton(
        onClick = { onClick() },
        modifier = Modifier
            .offset(y = 700.dp) // Adjust the button position
            .width(200.dp)  // Set the width for the button
            .height(50.dp)  // Set the height for the button
    ) {
        Text("Login")
    }
}


@Composable
fun HomeScreen(navController: NavController){
    Box(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize(),
        contentAlignment = Alignment.Center // Align content in the top center

    ){
        Image(
            painter = painterResource(id = R.drawable.foodstop_header),
            contentDescription = "My Image",
            modifier = Modifier
                .size(600.dp)
                .offset(y = -215.dp)
                .align(Alignment.TopCenter)
        )
        Column(
            modifier = Modifier
                .offset(y= 110.dp)
        ){

            OrderBTN(onClick = {
                navController.navigate("order")
            })
            InventoryBTN(onClick = {
                navController.navigate("inventory")
            })

            AnalyticsBTN(onClick = {
                navController.navigate("analytics")
            })

            CustomerBTN(onClick = {
                navController.navigate("customer")
            })



            LogoutBTN(
                onClick = {
                    // You can add logic here if needed, such as logging out the user
                    // Example: Clear user session or show a message
                    Toast.makeText(LocalContext.current, "Logging out...", Toast.LENGTH_SHORT).show()
                },
                navController = navController // Pass the NavController here
            )




        }

    }
}



@Composable
fun OrderBTN(onClick: () -> Unit) {
    val myButton = CustomButton(
        label = "Order",
        onClick = onClick,
        iconId = R.drawable.order, // Replace with your icon
    )

    // Use the CreateButton function to display the button
    myButton.CreateButton()
}

@Composable
fun InventoryBTN(onClick: () -> Unit) {
    val myButton = CustomButton(
        label = "Inventory",
        onClick = onClick,
        iconId = R.drawable.inventory, // Replace with your icon
    )

    // Use the CreateButton function to display the button
    myButton.CreateButton()
}

@Composable
fun AnalyticsBTN(onClick: () -> Unit) {
    val myButton = CustomButton(
        label = "Analytics",
        onClick = onClick,
        iconId = R.drawable.delivery, // Replace with your icon
    )

    // Use the CreateButton function to display the button
    myButton.CreateButton()
}

@Composable
fun CustomerBTN(onClick: () -> Unit) {
    val myButton = CustomButton(
        label = "Customer",
        onClick = onClick,
        iconId = R.drawable.customer, // Replace with your icon
    )

    // Use the CreateButton function to display the button
    myButton.CreateButton()
}

@Composable
fun LogoutBTN(onClick:  @Composable () -> Unit, navController: NavController) {
    val myButton = CustomButton(
        label = "Logout",
        onClick = {
            navController.navigate("login") // Navigate to login screen
        },
        iconId = R.drawable.logout, // Replace with your icon
    )

    // Use the CreateButton function to display the button
    myButton.CreateButton()
}
