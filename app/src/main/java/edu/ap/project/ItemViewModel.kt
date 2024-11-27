import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import edu.ap.project.model.Item
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ItemViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email

    private val _itemAddedStatus = MutableLiveData<Boolean>()
    val itemAddedStatus: LiveData<Boolean> get() = _itemAddedStatus

    private val _currentUserUid = MutableLiveData<String?>()
    val currentUserUid: LiveData<String?> get() = _currentUserUid

    private val _currentUserLocationCoordinates = MutableLiveData<GeoPoint?>()
    val currentUserLocationCoordinates: LiveData<GeoPoint?> get() = _currentUserLocationCoordinates

    private val _userItems = MutableLiveData<List<Item>>()
    val userItems: LiveData<List<Item>> get() = _userItems

    private val _allItems = MutableLiveData<List<Item>>()
    val allItems: LiveData<List<Item>> get() = _allItems

    private val _currentItem = MutableLiveData<Item?>()
    val currentItem: LiveData<Item?> get() = _currentItem

    private val _loadingState = MutableLiveData<Boolean>()
    val loadingState: LiveData<Boolean> get() = _loadingState

    private val _errorState = MutableLiveData<String?>()
    val errorState: LiveData<String?> get() = _errorState

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
        location: GeoPoint,
        type: String
    ) {
        val parsedPrice = price.toDoubleOrNull() ?: 0.0

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
                "location" to location,
                "owner" to "/users/$userUid",
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


    fun getItemsForUser(currentUserUid: String?) {
        if (currentUserUid != null) {
            Log.d("count", currentUserUid)
            firestore.collection("items")
                .whereEqualTo("owner", "/users/$currentUserUid")  // Query based on the user UID
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


    private val _ownerName = MutableLiveData<String?>()
    val ownerName: LiveData<String?> get() = _ownerName

    fun fetchOwnerName(ownerUid: String) {
        var string = ownerUid.split('/')
        var id = string.get(2)

        firestore.collection("users")
            .document(id)
            .get()
            .addOnSuccessListener { document ->
                val name = document.getString("username")
                _ownerName.value = name
            }
            .addOnFailureListener {
                _ownerName.value = "Unknown"
            }
        }

    }

