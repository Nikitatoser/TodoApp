package com.example.test_auth

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun HomeScreen(navController: NavController) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val displayEmail = currentUser?.email ?: "Anonymous"
    val db = FirebaseFirestore.getInstance()

    // Змінні для нотаток
    val notesList = remember { mutableStateListOf<Note>() }

    // Отримання нотаток для поточного користувача
    LaunchedEffect(currentUser) {
        currentUser?.let {
            db.collection("users")
                .document(it.uid)
                .collection("notes")
                .orderBy("name")
                .get()
                .addOnSuccessListener { result ->
                    notesList.clear()
                    for (document in result) {
                        val note = document.toObject(Note::class.java)
                        notesList.add(note)
                    }
                }
        }
    }

    // Видалення нотатки
    fun deleteNoteFromFirestore(note: Note) {
        currentUser?.let {
            // Створюємо посилання на документ нотатки з використанням ID нотатки
            val noteRef = db.collection("users")
                .document(it.uid) // UID користувача
                .collection("notes")
                .document(note.id) // ID нотатки, який ми зберегли раніше

            noteRef.delete()
                .addOnSuccessListener {
                    // Успішне видалення нотатки з Firestore
                    notesList.remove(note)
                    Log.d("Firestore", "Note successfully deleted!")
                }
                .addOnFailureListener { e ->
                    // Помилка при видаленні
                    Log.e("Firestore", "Error deleting note", e)
                }
        }
    }



    // Оновлення нотатки
    fun updateNoteInFirestore(note: Note, newTitle: String, newDescription: String) {
        currentUser?.let {
            val noteRef = db.collection("users")
                .document(it.uid) // UID користувача
                .collection("notes")
                .document(note.id) // ID нотатки

            noteRef.update("name", newTitle, "description", newDescription)
                .addOnSuccessListener {
                    // Успішне оновлення в Firestore
                    val index = notesList.indexOfFirst { it.id == note.id }
                    if (index != -1) {
                        notesList[index] = note.copy(name = newTitle, description = newDescription)
                    }
                    Log.d("Firestore", "Note updated successfully!")
                }
                .addOnFailureListener { e ->
                    // Обробка помилки
                    Log.e("Firestore", "Error updating note", e)
                }
        }
    }


    // Функція для збереження нотатки в Firestore
    fun saveNoteToFirestore(title: String, description: String, db: FirebaseFirestore, currentUser: FirebaseUser) {
        val note = Note(name = title, description = description)

        db.collection("users")
            .document(currentUser.uid) // UID користувача
            .collection("notes")
            .add(note)
            .addOnSuccessListener { documentReference ->
                // Зберігаємо згенерований Firestore ID у нотатці
                note.id = documentReference.id
                Log.d("Firestore", "Note saved successfully with ID: ${note.id}")

                // Оновлюємо документ нотатки, додаючи ID
                documentReference.update("id", note.id)
                    .addOnSuccessListener {
                        Log.d("Firestore", "Note ID updated successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error updating note ID", e)
                    }

                // Оновлюємо список нотаток
                notesList.add(note)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error adding note", e)
            }
    }




    // Змінні для діалогу редагування
    var showEditDialog by remember { mutableStateOf(false) }
    var noteToEdit by remember { mutableStateOf<Note?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 30.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .shadow(8.dp, shape = RoundedCornerShape(40.dp))
                    .clip(RoundedCornerShape(40.dp))
                    .background(Color.White)
                    .clickable { navController.navigate("set") }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.oruser),
                        contentDescription = "user",
                        modifier = Modifier.size(45.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = displayEmail,
                        color = Color.Gray,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Light,
                        textAlign = TextAlign.Center
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp)
            ) {
                items(notesList) { note ->
                    NoteCard(
                        note = note,
                        onDelete = { deleteNoteFromFirestore(note) },
                        onEdit = {
                            noteToEdit = note
                            showEditDialog = true
                        }
                    )
                }
            }
        }

        var showDialog by remember { mutableStateOf(false) }
        Button(
            onClick = { showDialog = true },
            modifier = Modifier
                .padding(vertical = 60.dp, horizontal = 16.dp)
                .align(Alignment.BottomEnd)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF88837)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.plus),
                contentDescription = "add note",
                modifier = Modifier.size(50.dp)
            )
        }
        // Показуємо діалог для додавання нової нотатки
        if (showDialog) {
            CustomDialog(
                onDismiss = { showDialog = false },
                onSave = { title, description ->
                    currentUser?.let {
                        saveNoteToFirestore(title, description, db, it)
                    }
                    showDialog = false
                }
            )
        }
        if (showEditDialog && noteToEdit != null) {
            EditNoteDialog(
                note = noteToEdit!!,
                onDismiss = { showEditDialog = false },
                onSave = { title, description ->
                    updateNoteInFirestore(noteToEdit!!, title, description)
                    showEditDialog = false
                }
            )
        }
    }
}



@Composable
fun CustomDialog(onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var title by remember { mutableStateOf(TextFieldValue()) }
    var description by remember { mutableStateOf(TextFieldValue()) }

    Dialog(onDismissRequest = { onDismiss() }) {
        Box(
            modifier = Modifier
                .width(300.dp)
                .height(400.dp)
                .background(
                    Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
                .clip(RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "Add new note",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Left,
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Black,
                    fontSize = 24.sp
                )

                // Поле для вводу назви
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color(0xFFF88837),
                        unfocusedIndicatorColor = Color(0xFFF88837),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true
                )

                // Поле для вводу опису
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .heightIn(min = 120.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color(0xFFF88837),
                        unfocusedIndicatorColor = Color(0xFFF88837),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    maxLines = 5,
                    textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
                )

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { onDismiss() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF88837)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close")
                    }

                    Button(
                        onClick = {
                            onSave(title.text, description.text)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF88837)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun EditNoteDialog(
    note: Note, // Параметр для передачі поточної нотатки
    onDismiss: () -> Unit, // Функція для закриття діалогу
    onSave: (String, String) -> Unit // Функція для збереження змін у нотатці
) {
    var title by remember { mutableStateOf(note.name) } // Початкове значення для назви
    var description by remember { mutableStateOf(note.description) } // Початкове значення для опису

    Dialog(onDismissRequest = { onDismiss() }) {
        Box(
            modifier = Modifier
                .width(300.dp)
                .height(400.dp)
                .background(Color.White, shape = RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "Edit Note",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Left,
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Black,
                    fontSize = 24.sp
                )

                // Поле для вводу назви
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color(0xFFF88837),
                        unfocusedIndicatorColor = Color(0xFFF88837),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true
                )

                // Поле для вводу опису
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .heightIn(min = 120.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color(0xFFF88837),
                        unfocusedIndicatorColor = Color(0xFFF88837),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    maxLines = 5,
                    textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
                )

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { onDismiss() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF88837)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close")
                    }

                    Button(
                        onClick = {
                            onSave(title, description) // Передаємо нові значення
                            onDismiss() // Закриваємо діалог
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF88837)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}



@Composable
fun NoteCard(
    note: Note,
    onDelete: (Note) -> Unit,
    onEdit: (Note) -> Unit
) {
    Box(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .shadow(8.dp, shape = RoundedCornerShape(12.dp)) // Тінь
            .clip(RoundedCornerShape(12.dp)) // Округлені кути
            .background(Color.White) // Фон прямокутника
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Ліва колонка з текстами
            Column(modifier = Modifier.weight(0.7f)) {
                Text(
                    text = note.name, // Використовуємо дані з Note
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
                Text(
                    text = note.description, // Використовуємо опис з Note
                    fontWeight = FontWeight.Light,
                    color = Color.Gray,
                    fontSize = 18.sp
                )
            }

            // Права колонка з кнопками
            Column(modifier = Modifier.weight(0.3f)) {
                // Кнопка редагування
                Button(
                    onClick = { onEdit(note) },
                    modifier = Modifier
                        .padding(5.dp)
                        .fillMaxWidth()
                        .shadow(8.dp, shape = RoundedCornerShape(12.dp)) // Тінь
                        .clip(RoundedCornerShape(12.dp)) // Округлені кути
                        .background(Color.White), // Колір фону кнопки
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White // Колір фону кнопки
                    )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.pan),
                        contentDescription = "edit",
                        modifier = Modifier.size(35.dp)
                    )
                }

                // Кнопка видалення
                Button(
                    onClick = { onDelete(note) },
                    modifier = Modifier
                        .padding(5.dp)
                        .fillMaxWidth()
                        .shadow(8.dp, shape = RoundedCornerShape(12.dp)) // Тінь
                        .clip(RoundedCornerShape(12.dp)) // Округлені кути
                        .background(Color.White), // Колір фону кнопки
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White // Колір фону кнопки
                    )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.trash),
                        contentDescription = "delete",
                        modifier = Modifier.size(35.dp)
                    )
                }
            }
        }
    }
}
