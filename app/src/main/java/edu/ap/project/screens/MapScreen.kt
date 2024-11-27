package edu.ap.project.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import java.net.URL

@Composable
fun MapScreen() {
    val context = LocalContext.current

    // OSMDroid configuratie laden
    Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))

    // State voor de zoekbalk en schuifregelaar
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var mapCenter by remember { mutableStateOf(GeoPoint(51.2302, 4.4165)) }
    var searchRadius by remember { mutableStateOf(20f) }
    var errorMessage by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        // Kaartweergave (achtergrond)
        AndroidView(
            factory = { ctx ->
                val mapView = MapView(ctx)
                mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)

                val mapController = mapView.controller
                mapController.setZoom(15.0)
                mapController.setCenter(mapCenter)

                mapView
            },
            modifier = Modifier
                .fillMaxSize()
                .zIndex(0f) // Zorgt ervoor dat de kaart achter andere UI-elementen ligt
        )

        // Zoekbalk en schuifregelaar (voorgrond)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color.White)
                .padding(16.dp)
                .zIndex(1f) // Zorgt ervoor dat deze UI bovenop de kaart blijft
        ) {
            // Zoekbalk met knop
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { query ->
                        searchQuery = query
                    },
                    label = { Text("Zoek een locatie") },
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = {
                        scope.launch {
                            val result = geocodeLocation(searchQuery.text)
                            if (result != null) {
                                mapCenter = GeoPoint(result.first, result.second)
                                errorMessage = ""
                            } else {
                                errorMessage = "Locatie niet gevonden."
                            }
                        }
                    }
                ) {
                    Text("Zoek")
                }
            }

            // Foutmelding (indien van toepassing)
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Schuifregelaar voor straal
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Straal: ${searchRadius.toInt()} km", style = MaterialTheme.typography.bodyMedium)

                Slider(
                    value = searchRadius,
                    onValueChange = { newValue ->
                        searchRadius = newValue
                    },
                    valueRange = 5f..75f,
                    steps = 13,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// Geocoding functie
suspend fun geocodeLocation(query: String): Pair<Double, Double>? {
    return withContext(Dispatchers.IO) {
        try {
            val url =
                "https://nominatim.openstreetmap.org/search?q=${query.replace(" ", "+")}&format=json&limit=1"
            val response = URL(url).readText()
            val jsonArray = JSONArray(response)

            if (jsonArray.length() > 0) {
                val jsonObject = jsonArray.getJSONObject(0)
                val lat = jsonObject.getDouble("lat")
                val lon = jsonObject.getDouble("lon")
                Pair(lat, lon)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
