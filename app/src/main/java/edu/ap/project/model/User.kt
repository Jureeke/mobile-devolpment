package edu.ap.project.model

import com.google.firebase.firestore.GeoPoint

data class User(
    val uid: String = "", // Firebase Auth UID
    val email: String = "",
    val username: String = "",
    val profileImageUrl: String? = null,
    val location: String? = null,
    val locationCoordinates: GeoPoint?,

    val createdAt: Long = System.currentTimeMillis()
) {
    constructor() : this("", "", "", null, null, null)
}