package edu.ap.project

data class User(
    val uid: String = "", // Firebase Auth UID
    val email: String = "",
    val username: String = "",
    val profileImageUrl: String? = null,
    val location: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    constructor() : this("", "", "", null, null)
}