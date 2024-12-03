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
    val context = LocalContext.current

    if (user == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        val imageUrl = user?.profileImageUrl ?: ""
        val name = user?.username ?: ""
        val email = user?.email ?: ""
        val location = user?.location ?: ""

        var newUsername by remember { mutableStateOf(name) }
        var newEmail by remember { mutableStateOf(email) }
        var newLocation by remember { mutableStateOf(location) }
        var newImageUrl by remember { mutableStateOf(imageUrl) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(top = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profielfoto
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            ) {
                if (newImageUrl.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(newImageUrl),
                        contentDescription = "Profielfoto",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Bewerkbare naam
            TextField(
                value = newUsername,
                onValueChange = { newUsername = it },
                label = { Text("Naam") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // Bewerkbare e-mail
            TextField(
                value = newEmail,
                onValueChange = { newEmail = it },
                label = { Text("E-mail") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // Bewerkbare locatie
            TextField(
                value = newLocation,
                onValueChange = { newLocation = it },
                label = { Text("Adres") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // Profielfoto URL
            TextField(
                value = newImageUrl,
                onValueChange = { newImageUrl = it },
                label = { Text("Profielfoto URL") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Knop om gegevens bij te werken
            Button(
                onClick = {
                    GlobalScope.launch(Dispatchers.Main) {
                        // Verkrijg de coördinaten voor de nieuwe locatie
                        val coordinates = getCoordinatesFromLocation(newLocation, context)
                        if (coordinates != null) {
                            val updatedUser = User(
                                uid = user?.uid ?: "",
                                email = newEmail,
                                username = newUsername,
                                profileImageUrl = newImageUrl,
                                location = newLocation,
                                locationCoordinates = GeoPoint(coordinates.first, coordinates.second),
                                createdAt = user?.createdAt ?: System.currentTimeMillis()
                            )
                            userViewModel.updateUserProfile(updatedUser)
                            Toast.makeText(context, "Gegevens succesvol bijgewerkt!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Kon geen coördinaten vinden voor de opgegeven locatie.", Toast.LENGTH_SHORT).show()
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
        val client = OkHttpClient.Builder()
            .callTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        val encodedLocation = java.net.URLEncoder.encode(locationName, "UTF-8")
        val url = "https://nominatim.openstreetmap.org/search?q=$encodedLocation&format=json&addressdetails=1"
        val request = Request.Builder().url(url).build()

        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.string()?.let { responseBody ->
                    val jsonResponse = JSONArray(responseBody)
                    if (jsonResponse.length() > 0) {
                        val firstResult = jsonResponse.getJSONObject(0)
                        val lat = firstResult.optDouble("lat", Double.NaN)
                        val lon = firstResult.optDouble("lon", Double.NaN)

                        if (!lat.isNaN() && !lon.isNaN()) {
                            return@withContext Pair(lat, lon)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("LocationService", "Error fetching coordinates: ${e.message}")
        }

        return@withContext null
    }
}




