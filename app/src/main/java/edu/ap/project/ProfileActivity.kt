package edu.ap.project

import UserViewModel
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import okhttp3.OkHttpClient
import okhttp3.Request
import androidx.lifecycle.viewmodel.compose.viewModel as viewModel
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import edu.ap.project.model.User
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray


class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProfileScreen()
        }
    }
}

@Composable
fun ProfileScreen(userViewModel: UserViewModel = viewModel()) {
    val user by userViewModel.userData.collectAsState()
    val context = LocalContext.current // Get context here

    // Controleer of de gegevens van de gebruiker beschikbaar zijn
    if (user == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        // Verkrijg de gegevens van de gebruiker
        val imageUrl = user?.profileImageUrl ?: ""
        val name = user?.username ?: ""
        val email = user?.email ?: ""
        val location = user?.location ?: ""

        // Mutable state voor het bewerken van de gegevens
        var newUsername by remember { mutableStateOf(name) }
        var newEmail by remember { mutableStateOf(email) }
        var newLocation by remember { mutableStateOf(location) }
        var newImageUrl by remember { mutableStateOf(imageUrl) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Profielfoto
            Image(
                painter = rememberAsyncImagePainter(imageUrl),
                contentDescription = "Profielfoto",
                modifier = Modifier
                    .size(120.dp)
                    .padding(16.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Bewerkbare naam
            TextField(
                value = newUsername,
                onValueChange = { newUsername = it },
                label = { Text("Naam") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )

            // Bewerkbare e-mail
            TextField(
                value = newEmail,
                onValueChange = { newEmail = it },
                label = { Text("E-mail") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )

            // Bewerkbare locatie
            TextField(
                value = newLocation,
                onValueChange = { newLocation = it },
                label = { Text("Locatie") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input voor een nieuwe profielfoto URL
            Box(
                modifier = Modifier
                    .fillMaxWidth().padding(bottom = 8.dp)
                    .verticalScroll(rememberScrollState()) // Add vertical scroll
                    .heightIn(min = 56.dp, max = 100.dp)
            ) {
                TextField(
                    value = newImageUrl,
                    onValueChange = { newImageUrl = it },
                    label = { Text("Nieuwe Profielfoto URL") },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Knop om de gegevens bij te werken
            Button(
                onClick = {
                    // Use a coroutine to call the suspend function and get coordinates
                    GlobalScope.launch(Dispatchers.Main) {
                        val coordinates = getCoordinatesFromLocation(newLocation, context)
                        if (coordinates != null) {
                            val (latitude, longitude) = coordinates

                            // Log the coordinates
                            val updatedUser = User(
                                uid = user?.uid ?: "",
                                email = newEmail,
                                username = newUsername,
                                profileImageUrl = newImageUrl,
                                location = newLocation,
                                locationCoordinates = GeoPoint(latitude, longitude), // Set the coordinates
                                createdAt = user?.createdAt ?: System.currentTimeMillis()
                            )
                            userViewModel.updateUserProfile(updatedUser)
                        } else {
                            Toast.makeText(context, "Locatie niet gevonden", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(text = "Gegevens bijwerken")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen()
}

suspend fun getCoordinatesFromLocation(locationName: String, context: Context): Pair<Double, Double>? {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val url = "https://nominatim.openstreetmap.org/search?q=$locationName&format=json&addressdetails=1&lang=nl"
        val request = Request.Builder()
            .url(url)
            .build()

        try {
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                responseBody?.let {
                    try {
                        val jsonResponse = JSONArray(it)
                        if (jsonResponse.length() > 0) {
                            val result = jsonResponse.getJSONObject(0)
                            val lat = result.getDouble("lat")
                            val lon = result.getDouble("lon")

                            // Return the coordinates as a Pair
                            return@withContext Pair(lat, lon)
                        } else {
                            Log.e("UserViewModel", "Geen Locatie Meegegeven")
                        }
                    } catch (e: Exception) {
                        Log.e("UserViewModel", "Error parsing response: ${e.message}")
                    }
                }
            } else {
                Log.e("UserViewModel", "API request failed with code: ${response.code}")
            }
        } catch (e: Exception) {
            Log.e("UserViewModel", "Error fetching location: ${e.message}")
        }
        // Return null if no coordinates found or an error occurred
        return@withContext null
    }
}


