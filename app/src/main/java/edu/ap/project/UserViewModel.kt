package edu.ap.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
            // Fetch user data from Firestore
            val userId = currentUser.uid
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val user = document.toObject(User::class.java)
                        _userData.value = user
                    }
                }
                .addOnFailureListener {
                    // Handle error
                    _userData.value = null
                }
        } else {
            // Handle case when no user is logged in
            _userData.value = null
        }
    }

    // Functie om gebruikersprofiel te updaten (behalve wachtwoord)
    fun updateUserProfile(updatedUser: User) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .update(
                    "username", updatedUser.username,
                    "email", updatedUser.email,
                    "profileImageUrl", updatedUser.profileImageUrl,
                    "location", updatedUser.location,
                    "createdAt", updatedUser.createdAt
                )
                .addOnSuccessListener {
                    // Bijwerken succesvol, haal de gegevens opnieuw op
                    fetchUserData()
                }
                .addOnFailureListener {
                    // Fout afhandelen (bijv. loggen)
                }
        }
    }
}