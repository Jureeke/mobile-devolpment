package edu.ap.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import coil.compose.rememberAsyncImagePainter
import androidx.lifecycle.viewmodel.compose.viewModel

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
                painter = rememberAsyncImagePainter(newImageUrl),
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
            TextField(
                value = newImageUrl,
                onValueChange = { newImageUrl = it },
                label = { Text("Nieuwe Profielfoto URL") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Knop om de gegevens bij te werken
            Button(
                onClick = {
                    // Maak een nieuw User object met de bijgewerkte gegevens
                    val updatedUser = User(
                        uid = user?.uid ?: "",
                        email = newEmail,
                        username = newUsername,
                        profileImageUrl = newImageUrl,
                        location = newLocation,
                        createdAt = user?.createdAt ?: System.currentTimeMillis()
                    )
                    userViewModel.updateUserProfile(updatedUser)
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
