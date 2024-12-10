package edu.ap.project.screens

import ItemViewModel
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun DetailScreen(
    itemId: String,
    itemViewModel: ItemViewModel = viewModel(),
    navController: NavController
) {
    val currentItem = itemViewModel.currentItem.observeAsState().value
    val isLoading = itemViewModel.loadingState.observeAsState(false)
    val ownerName by itemViewModel.ownerName.observeAsState("Loading...")
    val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val formattedEndDate = currentItem?.endDate?.let {
        dateFormat.format(it.toDate())
    } ?: "Niet Verhuurd"

    LaunchedEffect(itemId) {
        itemViewModel.fetchItemById(itemId)
    }

    LaunchedEffect(currentItem) {
        currentItem?.owner?.let { itemViewModel.fetchOwnerName(it) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading.value -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            currentItem != null -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        // Back Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBack,
                                    contentDescription = "Ga terug",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = "Details van ${currentItem.title}",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }

                    item {
                        // Item Details Card
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = currentItem.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(text = currentItem.description)
                                Text(text = "Prijs: €${currentItem.price}", fontWeight = FontWeight.SemiBold)
                                Text(text = "Einddatum: $formattedEndDate")
                                Text(text = "Locatie: ${currentItem.address}")
                                Text(text = "Eigenaar: $ownerName")
                                Text(text = "Type: ${currentItem.type}")

                                if (!currentItem.photo.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = currentItem.photo,
                                        contentDescription = "Afbeelding van ${currentItem.title}",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Warning,
                                            contentDescription = "Geen afbeelding",
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                            modifier = Modifier.size(48.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (currentItem.location!!.latitude != null && currentItem.location.longitude != null) {
                        item {
                            // OpenStreetMap Integration
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            ) {
                                AndroidView(
                                    modifier = Modifier.fillMaxSize(),
                                    factory = { context ->
                                        val mapView = org.osmdroid.views.MapView(context).apply {
                                            setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
                                            controller.setZoom(15.0)
                                            controller.setCenter(
                                                org.osmdroid.util.GeoPoint(
                                                    currentItem.location.latitude,
                                                    currentItem.location.longitude
                                                )
                                            )
                                            overlays.clear() // Clear default overlays
                                        }

                                        // Add a marker to the map
                                        val marker = org.osmdroid.views.overlay.Marker(mapView).apply {
                                            position = org.osmdroid.util.GeoPoint(currentItem.location.latitude, currentItem.location.longitude)
                                            setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM)
                                            title = currentItem.title
                                            snippet = currentItem.address
                                        }
                                        mapView.overlays.add(marker)

                                        mapView
                                    }
                                )
                            }
                        }
                    }


                    item {
                        // Actions
                        if (currentItem.renter == null && currentItem.owner != currentUserUid) {
                            Button(
                                onClick = { navController.navigate("rent/${currentItem.uid}") },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "Huren")
                            }
                        }

                        if (currentItem.owner == currentUserUid && currentItem.renter == null) {
                            Button(
                                onClick = { itemViewModel.deleteItem(currentItem.uid) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "Verwijderen")
                            }
                        }
                    }
                }
            }
        }
    }
}

