import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import edu.ap.project.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserViewModel : ViewModel() {
    private val _userData = MutableStateFlow<User?>(null)
    val userData: StateFlow<User?> = _userData

    init {
        fetchUserData()
    }

    private fun fetchUserData() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // Use coroutine to fetch user data from Firestore
            viewModelScope.launch {
                try {
                    val userId = currentUser.uid
                    val document = FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(userId)
                        .get()
                        .await() // Use await() to suspend and get the result

                    if (document.exists()) {
                        val user = document.toObject(User::class.java)
                        _userData.value = user
                    } else {
                        // Handle the case where document does not exist
                        _userData.value = null
                    }
                } catch (e: Exception) {
                    // Handle error
                    _userData.value = null
                }
            }
        } else {
            // Handle case when no user is logged in
            _userData.value = null
        }
    }

    // Function to update the user profile (excluding password)
    fun updateUserProfile(updatedUser: User) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            viewModelScope.launch {
                try {
                    val userId = currentUser.uid
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(userId)
                        .update(
                            "username", updatedUser.username,
                            "email", updatedUser.email,
                            "profileImageUrl", updatedUser.profileImageUrl,
                            "location", updatedUser.location,
                            "locationCoordinates", updatedUser.locationCoordinates,
                            "createdAt", updatedUser.createdAt
                        )
                        .await() // Use await() to suspend and get the result

                    // After successfully updating, fetch the updated data
                    fetchUserData()

                } catch (e: Exception) {
                    // Handle failure (e.g., log the error or notify the user)
                }
            }
        }
    }

}
