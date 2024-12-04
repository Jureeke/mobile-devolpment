import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import edu.ap.project.model.Item
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

class ItemViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email

    private val _itemAddedStatus = MutableLiveData<Boolean>()
    val itemAddedStatus: LiveData<Boolean> get() = _itemAddedStatus

    private val _currentUserUid = MutableLiveData<String?>()
    val currentUserUid: LiveData<String?> get() = _currentUserUid

    private val _currentUserLocationCoordinates = MutableLiveData<GeoPoint?>()
    val currentUserLocationCoordinates: LiveData<GeoPoint?> get() = _currentUserLocationCoordinates

    private val _currentUserAddress = MutableLiveData<String?>()
    val currentUserAddress: LiveData<String?> get() = _currentUserAddress

    private val _userItems = MutableLiveData<List<Item>>()
    val userItems: LiveData<List<Item>> get() = _userItems

    private val _userRentedItems = MutableLiveData<List<Item>>()
    val userRentedItems: LiveData<List<Item>> get() = _userRentedItems

    private val _allItems = MutableLiveData<List<Item>>()
    val allItems: LiveData<List<Item>> get() = _allItems

    private val _currentItem = MutableLiveData<Item?>()
    val currentItem: LiveData<Item?> get() = _currentItem

    private val _loadingState = MutableLiveData<Boolean>()
    val loadingState: LiveData<Boolean> get() = _loadingState

    private val _errorState = MutableLiveData<String?>()
    val errorState: LiveData<String?> get() = _errorState

    private val _ownerName = MutableLiveData<String?>()
    val ownerName: LiveData<String?> get() = _ownerName

    init {
        // Fetch the current user's UID based on email
        fetchCurrentUserUidAndLocation()
    }

    private fun fetchCurrentUserUidAndLocation() {
        firestore.collection("users")
            .whereEqualTo("email", currentUserEmail)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val userUid = querySnapshot.documents.first().getString("uid")
                    _currentUserUid.value = userUid
                    val locationCoordinates = querySnapshot.documents.first().getGeoPoint("locationCoordinates")
                    _currentUserLocationCoordinates.value = locationCoordinates

                    val address = querySnapshot.documents.first().getString("location")
                    _currentUserAddress.value = address

                } else {
                    _currentUserUid.value = null
                    _currentUserLocationCoordinates.value = null
                }
            }
            .addOnFailureListener {
                _currentUserUid.value = null
            }
    }

    fun addItem(
        title: String,
        description: String,
        price: String,
        imageUrl: String,
        type: String
    ) {
        val parsedPrice = price.toDoubleOrNull() ?: 0.0
        fetchCurrentUserUidAndLocation()
        val userUid = _currentUserUid.value
        if (userUid != null) {
            // Nieuwe documentreferentie (maakt automatisch een unieke ID aan)
            val newDocumentRef = firestore.collection("items").document()

            // Data van het item, inclusief het gegenereerde document-ID
            val newItem = hashMapOf(
                "title" to title,
                "description" to description,
                "price" to parsedPrice,
                "endDate" to null,
                "startDate" to null,
                "location" to currentUserLocationCoordinates.value,
                "address" to currentUserAddress.value,
                "owner" to "$userUid",
                "photo" to imageUrl,
                "renter" to null,
                "type" to type,
                "uid" to newDocumentRef.id // Stel het ID van het document in als uid
            )

            // Item opslaan met het expliciete document-ID
            newDocumentRef
                .set(newItem)
                .addOnSuccessListener {
                    _itemAddedStatus.value = true
                }
                .addOnFailureListener {
                    _itemAddedStatus.value = false
                }
        } else {
            _itemAddedStatus.value = false
        }
    }

    fun rentItem(itemId: String, start: Date, end: Date) {
        try {
            // Ophalen van het document
            val documentRef = firestore.collection("items").document(itemId)

            // Controleer of het item bestaat
            documentRef.get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        // Bijwerken van de velden
                        documentRef.update(
                            mapOf(
                                "startDate" to Timestamp(start),
                                "endDate" to Timestamp(end),
                                "renter" to _currentUserUid.value // Gebruik de juiste waarde voor de huidige gebruiker
                            ))
                            .addOnSuccessListener {
                                Log.d("ItemViewModel", "Item succesvol verhuurd!")
                            }
                            .addOnFailureListener { e ->
                                Log.e("ItemViewModel", "Error bij het bijwerken van het item: ${e.message}", e)
                            }
                    } else {
                        Log.e("ItemViewModel", "Item met ID $itemId bestaat niet.")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ItemViewModel", "Error bij het ophalen van item: ${e.message}", e)
                }
        } catch (e: Exception) {
            Log.e("ItemViewModel", "Error bij het huren van item: ${e.message}", e)
        }
    }

    fun getItemsForUser(currentUserUid: String?) {
        if (currentUserUid != null) {
            Log.d("count", currentUserUid)
            firestore.collection("items")
                .whereEqualTo("owner", "$currentUserUid")  // Query based on the user UID
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val items = querySnapshot.documents.mapNotNull { document ->
                        document.toObject(Item::class.java)  // Convert each document to an Item object
                    }
                    _userItems.value = items
                    Log.d("count",items.size.toString())
                }
                .addOnFailureListener {
                    _userItems.value = emptyList()  // Return an empty list on failure
                }
        } else {
            Log.d("count", "empty")

            _userItems.value = emptyList()  // Return an empty list if no user UID is found
        }
    }

    fun getRentedItemsForUser(currentUserUid: String) {
        Log.d("test", currentUserUid.toString())
        firestore.collection("items")
            .whereEqualTo("renter", "$currentUserUid")  // Query based on the user UID
            .get()
            .addOnSuccessListener { querySnapshot ->
                val items = querySnapshot.documents.mapNotNull { document ->
                    document.toObject(Item::class.java)  // Convert each document to an Item object
                }
                _userRentedItems.value = items
                Log.d("count",items.size.toString())
            }
            .addOnFailureListener {
                _userRentedItems.value = emptyList()  // Return an empty list on failure
            }
    }

    fun cancelRent(itemId: String) {
        try {
            // Ophalen van het document
            val documentRef = firestore.collection("items").document(itemId)

            // Controleer of het item bestaat
            documentRef.get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        // Bijwerken van de velden
                        documentRef.update(
                            mapOf(
                                "startDate" to null,
                                "endDate" to null,
                                "renter" to null
                            ))
                            .addOnSuccessListener {
                                Log.d("ItemViewModel", "Item succesvol verhuurd!")
                            }
                            .addOnFailureListener { e ->
                                Log.e("ItemViewModel", "Error bij het bijwerken van het item: ${e.message}", e)
                            }
                    } else {
                        Log.e("ItemViewModel", "Item met ID $itemId bestaat niet.")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ItemViewModel", "Error bij het ophalen van item: ${e.message}", e)
                }
        } catch (e: Exception) {
            Log.e("ItemViewModel", "Error bij het huren van item: ${e.message}", e)
        }
    }

    fun getAllItems() {
        firestore.collection("items")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val items = querySnapshot.documents.mapNotNull { document ->
                    document.toObject(Item::class.java)  // Convert each document to an Item object
                }
                _allItems.value = items
                Log.d("Firestore", "Fetched ${items.size} items.")
            }
            .addOnFailureListener { exception ->
                _allItems.value = emptyList()  // Return an empty list on failure
                Log.e("Firestore", "Failed to fetch items: ${exception.message}", exception)
            }
    }

    fun fetchItemById(itemId: String) {
        viewModelScope.launch {
            try {
                _loadingState.value = true
                _errorState.value = null
                val document = firestore.collection("items").document(itemId).get().await()
                val item = document.toObject(Item::class.java)?.copy(
                    uid = document.id
                )
                _currentItem.value = item
            } catch (e: Exception) {
                Log.e("ItemViewModel", "Error fetching item by ID: $e")
                _currentItem.value = null
                _errorState.value = "Failed to fetch item details"
            } finally {
                _loadingState.value = false
            }
        }
    }

    fun fetchOwnerName(ownerUid: String) {

        firestore.collection("users")
            .document(ownerUid)
            .get()
            .addOnSuccessListener { document ->
                val name = document.getString("username")
                _ownerName.value = name
            }
            .addOnFailureListener {
                _ownerName.value = "Unknown"
            }
        }

    fun deleteItem(itemUid: String) {
        firestore.collection("items")
            .whereEqualTo("uid", itemUid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    document.reference.delete()
                        .addOnSuccessListener {
                            // Handle success
                            Log.d("Firestore", "Item successfully deleted!")
                        }
                        .addOnFailureListener { e ->
                            // Handle failure
                            Log.e("Firestore", "Error deleting item", e)
                        }
                } else {
                    // If no document found with that UID
                    Log.e("Firestore", "No item found with UID: $itemUid")
                }
            }
            .addOnFailureListener { e ->
                // Handle query failure
                Log.e("Firestore", "Error getting item", e)
            }
    }

}