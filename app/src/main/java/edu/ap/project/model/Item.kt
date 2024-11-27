package edu.ap.project.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.PropertyName

data class Item(
    val description: String = "",
    val endDate: Timestamp? = null,
    val location: GeoPoint? = null,
    val owner: String = "", // Reference to user ID
    val photo: String = "",
    val price: Double = 0.0,
    val renter: String? = null, // Reference to user ID, nullable
    val title: String = "",
    val uid: String = "",

    @get:PropertyName("typeName")
    val typeName: String = "Overig" // Default to OTHERS if missing
) {
    // Convert typeName (string) to ItemType enum
    val type: ItemType
        get() = ItemType.fromString(typeName)
}
