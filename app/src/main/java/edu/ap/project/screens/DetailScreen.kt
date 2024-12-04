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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage

@Composable
fun DetailScreen(
    itemId: String,
    itemViewModel: ItemViewModel = viewModel(),
    navController: NavController
) {
    val currentItem = itemViewModel.currentItem.observeAsState().value
    val isLoading = itemViewModel.loadingState.observeAsState(false)
    val ownerName by itemViewModel.ownerName.observeAsState("Loading...")

    // Trigger fetching the item when this composable is displayed
    LaunchedEffect(itemId) {
        Log.d("ItemViewModel",itemId)
        itemViewModel.fetchItemById(itemId = itemId)
    }

    LaunchedEffect(currentItem) {
        // Fetch the owner's name when the current item is loaded
        currentItem?.owner?.let {
            itemViewModel.fetchOwnerName(ownerUid = it)  // Fetch the owner's name by UID
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading.value -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            currentItem != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Back Button
                    IconButton(
                        onClick = { navController.popBackStack()},
                        modifier = Modifier.size(36.dp) // Kleinere knop
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Naar detailpagina",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Item Details
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        shadowElevation = 4.dp,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Title: ${currentItem.title}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text("Description: ${currentItem.description}")
                            Text("Price: €${currentItem.price}")
                            Text("End Date: ${currentItem.endDate ?: "Niet Verhuurd"}")
                            Text("Location: ${currentItem.location?.latitude}, ${currentItem.location?.longitude}")
                            Text(text = "Owner: $ownerName")  // Display the owner name
                            Text("Type: ${currentItem.type}")
                            if (!currentItem.photo.isNullOrEmpty()) {
                                AsyncImage(
                                    model = currentItem.photo,
                                    contentDescription = "Afbeelding van ${currentItem.title}",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(120.dp) // Stel de grootte van de afbeelding in
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            }
                            else {
                                Box(
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(RoundedCornerShape(8.dp))
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
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            )  {
                                Button(
                                    onClick = { navController.navigate("rent/${currentItem.uid}") })
                                {
                                    Text("Huren")
                                }
                            }
                        }

                    }
                }
            }
        }
    }
}