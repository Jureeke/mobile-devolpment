package edu.ap.project

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import edu.ap.project.model.User
import edu.ap.project.theme.ProjectTheme

class RegisterActivity : ComponentActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setContent {
            ProjectTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    RegisterScreen(
                        modifier = Modifier.padding(innerPadding),
                        auth = mAuth,
                        firestore = db,
                        onRegistrationSuccess = {
                            startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    auth: FirebaseAuth,
    firestore: FirebaseFirestore,
    onRegistrationSuccess: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Logo bovenaan
        Image(
            painter = painterResource(id = R.drawable.leasy), // Pas dit aan met je logo
            contentDescription = "App Logo",
            modifier = Modifier
                .size(250.dp)
                .padding(top = 32.dp)
        )

        // "Maak een account aan" tekst
        Text(
            text = "Maak een account aan",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Email veld
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-mailadres") },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            enabled = !isLoading
        )

        // Gebruikersnaam veld
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Gebruikersnaam") },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            enabled = !isLoading
        )

        // Wachtwoord veld
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Wachtwoord") },
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            enabled = !isLoading
        )

        // Wachtwoord bevestigen veld
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Bevestig wachtwoord") },
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            enabled = !isLoading
        )

        // Register knop
        Button(
            onClick = {
                val trimmedEmail = email.trim()
                val trimmedUsername = username.trim()
                val trimmedPassword = password.trim()
                val trimmedConfirmPassword = confirmPassword.trim()

                when {
                    trimmedEmail.isEmpty() || trimmedPassword.isEmpty() ||
                            trimmedConfirmPassword.isEmpty() || trimmedUsername.isEmpty() -> {
                        Toast.makeText(
                            context,
                            "Vul alle velden in.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    trimmedPassword != trimmedConfirmPassword -> {
                        Toast.makeText(
                            context,
                            "De wachtwoorden komen niet overeen.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    trimmedPassword.length < 6 -> {
                        Toast.makeText(
                            context,
                            "Het wachtwoord moet minimaal 6 tekens bevatten.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {
                        isLoading = true
                        auth.createUserWithEmailAndPassword(trimmedEmail, trimmedPassword)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val firebaseUser = auth.currentUser
                                    if (firebaseUser != null) {
                                        val user = User(
                                            uid = firebaseUser.uid,
                                            email = trimmedEmail,
                                            username = trimmedUsername,
                                            profileImageUrl = null,
                                            location = null,
                                            locationCoordinates = GeoPoint(0.0, 0.0),
                                            createdAt = System.currentTimeMillis()
                                        )

                                        firestore.collection("users")
                                            .document(firebaseUser.uid)
                                            .set(user)
                                            .addOnSuccessListener {
                                                isLoading = false
                                                Toast.makeText(
                                                    context,
                                                    "Registratie succesvol.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                onRegistrationSuccess()
                                            }
                                            .addOnFailureListener { e ->
                                                isLoading = false
                                                Toast.makeText(
                                                    context,
                                                    "Gebruikersgegevens opslaan mislukt: ${e.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }
                                } else {
                                    isLoading = false
                                    Toast.makeText(
                                        context,
                                        "Registratie mislukt: ${task.exception?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Registreren")
            }
        }

        // "Heb je al een account? Inloggen" tekst
        TextButton(
            onClick = {
                context.startActivity(Intent(context, LoginActivity::class.java))
            },
            modifier = Modifier.padding(top = 8.dp),
            enabled = !isLoading
        ) {
            Text(
                text = "Heb je al een account? Inloggen",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
