package com.example.test_auth

data class Note(
    var id: String = "", // Ідентифікатор нотатки
    var name: String = "",
    var description: String = ""
) {
    // Конструктор без параметрів для Firebase
    constructor() : this("", "", "")
}
