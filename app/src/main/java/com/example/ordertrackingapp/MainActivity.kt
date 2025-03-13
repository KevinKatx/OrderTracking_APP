package com.example.ordertrackingapp

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.graphicsLayer
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.ordertrackingapp.databases.DatabaseHelper
import com.example.ordertrackingapp.databases.Tables.User
import com.example.ordertrackingapp.databases.handlers.UserHandler
import kotlinx.coroutines.launch
import androidx.compose.material.*



class MainActivity : ComponentActivity() {
    //M4pUaUN1v3RsltY - Register Key, admin-Password
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
    val context = LocalContext.current
    NavHost(navController = navController, startDestination = "login") {
        composable("home") {
            HomeScreen(navController = navController, context)
        }

        composable("register") {
            RegisterScreen(navController)
        }

        composable("login") {
            LoginScreen(navController = navController) // Your Login Screen Composable
        }

        composable("delivery"){
            DeliveryScreen(navController = navController)
        }

        composable("analytics") {
            AnalyticsScreen(navController = navController)
        }
        composable("customer") {
            CustomerScreen(navController = navController)
        }
        composable("customer_insert") {
            CustomerInsert(navController = navController)
        }
        composable("customer_edit/{id}") { backStackEntry ->
            val customerId = backStackEntry.arguments?.getString("id")
            if (customerId != null) {
                CustomerEdit(navController = navController, customerId.toInt())
            }
        }
        composable("products"){
            SeeMenu(navController = navController)
        }
        composable("product_insert"){
            ProductInsert(navController = navController)
        }
        composable("product_edit/{id}") { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("id")
            if (productId != null) {
                ProductEdit(navController = navController, productId.toInt())
            }
        }
        composable("order") {
            OrderScreen(navController = navController)
        }
        composable("order_insert"){
            OrderInsert(navController=navController)
        }
        composable("order_edit/{id}") { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("id")
            if (orderId != null) {
                OrderEdit(navController = navController, orderId.toInt())
            }
        }

        composable("select_products") {
            SelectProducts(navController)
        }

        composable("promo") {
            PromoScreen(navController = navController) // Your Login Screen Composable
        }

        composable("promo_insert") {
            PromoInsert(navController = navController) // Your Login Screen Composable
        }

        composable("promo_edit/{id}") { backStackEntry ->
            val promoIdString = backStackEntry.arguments?.getString("id")
            val promoId = promoIdString?.toIntOrNull()
            if(promoId != null) {
                PromoEdit(navController = navController, promoID = promoId)
            }
        }
        composable("select_promos") {
            SeePromo(navController=navController)
        }

        composable("delivery_insert") {
            DeliveryInsert(navController)
        }

        composable(
            "delivery_edit/{deliveryID}",
            arguments = listOf(navArgument("deliveryID") { type = NavType.IntType })
        ) { backStackEntry ->
            val deliveryID = backStackEntry.arguments?.getInt("deliveryID")
            DeliveryEdit(navController, deliveryID)
        }

    }
}





@Composable
fun LoginScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Create UserHandler instance
    val userHandler = remember { UserHandler(context) }

    // Use a Box to position elements manually
    Box(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter // Align content in the top center
    ) {
        Image(
            painter = painterResource(id = R.drawable.foodstop_header),
            contentDescription = "FoodStop Header",
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
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(40.dp))

            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation() // Hides password with *
            )
            Spacer(modifier = Modifier.height(40.dp))
        }

        // Button placement
        SubmitBTN(onClick = {
            coroutineScope.launch {
                if (username.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "Username and password cannot be empty", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val user = userHandler.authenticateUser(username, password)
                if (user != null) {
                    // Authentication successful
                    Toast.makeText(context, "Welcome, ${user.username}!", Toast.LENGTH_SHORT).show()
                    navController.navigate("home")
                } else {
                    // Authentication failed
                    Toast.makeText(context, "Invalid username or password!", Toast.LENGTH_SHORT).show()
                }
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
                        navController.navigate("register")
                    }
                    .padding(40.dp, 0.dp, 0.dp, 0.dp)
            )
        }
    }
}



@Composable
fun SubmitBTN(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF9800) // Orange color
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .offset(y = 600.dp)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                "Login",
                color = Color.White
            )
        }
    }
}


@Composable
fun HomeScreen(navController: NavController, context: Context){
    val db = DatabaseHelper.getInstance(context).writableDatabase

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
            ProductsBTN(onClick = {
                navController.navigate("products")
            })
            PromoBTN(onClick = {
                navController.navigate("promo")
            })
            AnalyticsBTN(onClick = {
                navController.navigate("analytics")
            })

            CustomerBTN(onClick = {
                navController.navigate("customer")
            })

            DeliveryBTN(onClick = {
                navController.navigate("delivery")
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
fun DeliveryBTN(onClick: () -> Unit){
    val myButton = CustomButton(
        label = "Delivery",
        onClick = onClick,
        iconId = R.drawable.delivery
    )
    myButton.CreateButton()
}


@Composable
fun AddOrderBTN(onClick: () -> Unit){
    val myButton = CustomButton(
        label = "Add Order",
        onClick = onClick,
        iconId = R.drawable.order,
    )
    myButton.CreateButton()
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
fun ProductsBTN(onClick: () -> Unit) {
    val myButton = CustomButton(
        label = "Products",
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
        iconId = R.drawable.analytics, // Replace with your icon
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

@Composable
fun PromoBTN(onClick: () -> Unit) {
    val myButton = CustomButton(
        label = "Promo",
        onClick = onClick,
        iconId = R.drawable.discount, // Replace with your icon
    )

    // Use the CreateButton function to display the button
    myButton.CreateButton()
}

@Composable
fun RegisterScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var passkey by remember { mutableStateOf("") }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userHandler = remember { UserHandler(context) }

    Box(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Image(
            painter = painterResource(id = R.drawable.foodstop_header),
            contentDescription = "FoodStop Header",
            modifier = Modifier
                .size(600.dp)
                .offset(y = -215.dp)
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .wrapContentSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Register New User",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            TextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = passkey,
                onValueChange = { passkey = it },
                label = { Text("System Passkey") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        // Validate input fields
                        when {
                            username.isBlank() || password.isBlank() || email.isBlank() || passkey.isBlank() -> {
                                Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
                                return@launch
                            }

                            password != confirmPassword -> {
                                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                                return@launch
                            }

                            userHandler.isUsernameExists(username) -> {
                                Toast.makeText(context, "Username already exists", Toast.LENGTH_SHORT).show()
                                return@launch
                            }

                            !userHandler.validatePasskey(passkey) -> {
                                Toast.makeText(context, "Invalid system passkey", Toast.LENGTH_SHORT).show()
                                return@launch
                            }

                            else -> {
                                // All validations passed, create user
                                val newUser = User(
                                    username = username,
                                    password = password,
                                    email = email,
                                    isAdmin = false
                                )

                                if (userHandler.insertUser(newUser)) {
                                    Toast.makeText(context, "Registration successful! Please login.", Toast.LENGTH_SHORT).show()
                                    navController.navigate("login")
                                } else {
                                    Toast.makeText(context, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
            ) {
                Text("Register")
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { navController.navigateUp() }
            ) {
                Text("Back to Login")
            }
        }
    }
}