package edu.ap.project.model

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.Timestamp

data class Item(
    val description: String = "",
    val endDate: Timestamp? = null,
    val startDate: Timestamp? = null,
    val location: GeoPoint? = null,
    val address: String = "",
    val owner: String = "",
    val photo: String = "",
    val price: Double = 0.0,
    val renter: String? = null,
    val title: String = "",
    val uid: String = "",
    val type: String = "Overig"

)

