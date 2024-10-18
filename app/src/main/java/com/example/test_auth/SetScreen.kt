package com.example.test_auth

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SetScreen(navController: NavController) {
    // Контейнер для всіх елементів
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Кнопка "Вийти"
        Button(
            onClick = { signOut(navController) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF88837)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Log out",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Кнопка "Видалити акаунт"
        Button(
            onClick = { deleteAccount(navController) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF88837)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Delete",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Кнопка "Назад"
        Button(
            onClick = { navController.navigate("home") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF88837)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Back",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Функція для виходу з акаунта
private fun signOut(navController: NavController) {
    FirebaseAuth.getInstance().signOut() // Вихід з акаунта
    navController.navigate("login") // Перехід на екран входу
}

// Функція для видалення акаунта
fun deleteAccount(navController: NavController) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    currentUser?.let { user ->
        val userId = user.uid

        // Видалення всіх нотаток користувача з Firestore
        val notesRef = FirebaseFirestore.getInstance().collection("users").document(userId).collection("notes")
        notesRef.get()
            .addOnSuccessListener { snapshot ->
                val batch = FirebaseFirestore.getInstance().batch() // Пакетне видалення нотаток

                // Видаляємо кожну нотатку
                snapshot.documents.forEach { document ->
                    batch.delete(document.reference)
                }

                // Після видалення нотаток виконуємо пакетну операцію
                batch.commit()
                    .addOnSuccessListener {
                        // Нотатки успішно видалені, тепер видаляємо акаунт
                        user.delete()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d("Auth", "User account deleted successfully")
                                    navController.navigate("login") // Після видалення акаунта переходь на екран входу
                                } else {
                                    Log.e("Auth", "Error deleting user account", task.exception)
                                }
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error deleting notes: ", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error retrieving notes: ", e)
            }
    }
}

