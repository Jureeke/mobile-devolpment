package edu.ap.project

data class User(
    val uid: String = "", // Firebase Auth UID
    val email: String = "",
    val username: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    constructor() : this("", "", "")
}