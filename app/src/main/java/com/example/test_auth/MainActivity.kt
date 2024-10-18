package com.example.test_auth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.delay
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.*
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            val navController = rememberNavController()
            Navigation()
        }
    }
}


@Composable
fun SplashScreen(navController: NavHostController) {
    // Використовуємо LaunchedEffect для затримки і перевірки авторизації
    LaunchedEffect(key1 = true) {
        // Отримуємо поточного користувача з FirebaseAuth
        val currentUser = FirebaseAuth.getInstance().currentUser

        // Затримка на 3 секунди (можна прибрати або скоротити)
        delay(3000)

        // Перевіряємо чи є користувач авторизованим
        if (currentUser != null) {
            // Якщо користувач авторизований, переходимо на головний екран
            navController.navigate("home") {
                popUpTo("splash") { inclusive = true } // Видаляємо сплеш-екран з навігації
            }
        } else {
            // Якщо користувач не авторизований, переходимо на екран входу
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true } // Видаляємо сплеш-екран з навігації
            }
        }
    }

    // Відображення контенту сплеш-скріну
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Логотип
            Image(
                painter = painterResource(id = R.drawable.mylogo),
                contentDescription = "Logo",
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Текст під логотипом
            Text(
                text = "TodoNote", fontSize = 50.sp, color = Color(0xFFF88837),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}



@Composable
fun Navigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash_screen") {
        composable("splash_screen") { SplashScreen(navController) }
        composable("home") { HomeScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("set") { SetScreen(navController) }
    }
}