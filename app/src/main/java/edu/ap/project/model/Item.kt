package edu.ap.project.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.PropertyName

data class Item (
    val description: String = "",
    val endDate: Timestamp? = null,
    val location: GeoPoint? = null,
    val owner: String = "", // Reference to user ID
    val photo: String = "",
    val price: Double = 0.0,
    val renter: String = "", // Reference to user ID
    val title: String = "",
    @get:PropertyName("typeName") val type: ItemType = ItemType.OTHERS
)
