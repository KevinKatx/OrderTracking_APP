package com.example.ordertrackingapp


import android.graphics.Paint.Align
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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


class MainActivity : ComponentActivity() {
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
                LoginForm()
                
            }
        }
    }
}

@Preview(showBackground = true,
    widthDp=360,
    heightDp=806
)

@Composable
fun LoginForm() {
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
                .offset(y = -200.dp) // Adjust the image position
        )

        // Center the column content
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .wrapContentSize(),  // Avoid filling the entire screen
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Username:",
                modifier = Modifier
                    .align(Alignment.Start)
                    .offset(x = 100.dp)
            )
            TextField(value = "", onValueChange = {})
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                "Password:",
                modifier = Modifier
                    .align(Alignment.Start)
                    .offset(x = 100.dp)
            )
            TextField(value = "", onValueChange = {})
        }

        // Button placement
        SubmitBTN(onClick = {
            text = "Button Clicked"
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
