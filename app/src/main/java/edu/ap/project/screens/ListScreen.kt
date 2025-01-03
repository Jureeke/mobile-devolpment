package edu.ap.project.screens

import ItemViewModel
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import edu.ap.project.model.Item
import edu.ap.project.model.ItemType

@Composable
fun ItemBox(item: Item,navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .background(
                color = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Row voor afbeelding en tekst
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Afbeelding aan de linkerkant
                if (!item.photo.isNullOrEmpty()) {
                    AsyncImage(
                        model = item.photo,
                        contentDescription = "Afbeelding van ${item.title}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(120.dp) // Stel de grootte van de afbeelding in
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
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

                // Tekst aan de rechterkant
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f) // Zorgt ervoor dat de tekst de resterende ruimte opvult
                ) {
                    // Titel
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Prijs
                    Text(
                        text = "€${item.price} /per dag",
                        style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Beschrijving
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Type en knop in een row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        // Type tekst
                        Text(
                            text = "Type: ${item.type}",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                        )

                        // Knop naast de type tekst
                        Spacer(modifier = Modifier.weight(1f)) // Zorgt ervoor dat de knop naar rechts verschuift
                        IconButton(
                            onClick = { navController.navigate("detail/${item.uid}") },
                            modifier = Modifier.size(36.dp) // Kleinere knop
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ArrowForward,
                                contentDescription = "Naar detailpagina",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ListScreen(itemViewModel: ItemViewModel = viewModel(), navController: NavController) {
    val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
    val userItems = itemViewModel.userItems.observeAsState(emptyList())
    val allItems = itemViewModel.allItems.observeAsState(emptyList())
    val isViewingUserItems = remember { mutableStateOf(false) }
    var selectedType = remember { mutableStateOf<String?>(null) }
    var expanded = remember { mutableStateOf(false)}

    LaunchedEffect(currentUserUid) {
        itemViewModel.getItemsForUser(currentUserUid)
        Log.d("test", userItems.value.size.toString())
        itemViewModel.getAllItems()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Titel afhankelijk van de state
        Text(
            text = if (isViewingUserItems.value) "Mijn Items" else "Alle Items",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Filter Row: Toggle button + Dropdown
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp), // Ruimte tussen de items
            verticalAlignment = Alignment.CenterVertically // Uitlijning op één lijn
        ) {
            // Knop
            Button(
                onClick = { isViewingUserItems.value = !isViewingUserItems.value },
                modifier = Modifier.height(56.dp) // Zorgt ervoor dat de knop dezelfde hoogte heeft als de dropdown
            ) {
                Text(text = if (isViewingUserItems.value) "Toon alle Items" else "Mijn Items")
            }

            // Dropdown
            Box(
                modifier = Modifier
                    .weight(1f) // Laat de dropdown de resterende ruimte opvullen
                    .height(56.dp) // Zelfde hoogte als de knop
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    .clickable { expanded.value = true }
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Text(
                    text = selectedType.value ?: "Alle Categorieën",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier.align(Alignment.CenterStart)
                )
            }

            DropdownMenu(
                expanded = expanded.value,
                onDismissRequest = { expanded.value = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                // Optie: Alle categorieën
                DropdownMenuItem(
                    onClick = {
                        selectedType.value = null
                        expanded.value = false
                    },
                    text = { Text("Alle Categorieën") }
                )
                Divider() // Scheiding tussen opties
                // Dynamische opties gebaseerd op ItemType
                ItemType.values().forEach { type ->
                    DropdownMenuItem(
                        onClick = {
                            selectedType.value = type.typeName
                            expanded.value = false
                        },
                        text = { Text(type.typeName) }
                    )
                }
            }
        }


        // Filter items op basis van de geselecteerde filters
        val itemsToShow = (if (isViewingUserItems.value) userItems.value else allItems.value)
            .filter { item ->
                selectedType.value == null || item.type == selectedType.value
            }

// Controleer of er items zijn
        if (itemsToShow.isEmpty()) {
            // Toon een bericht als er geen items zijn
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Geen items gevonden in deze categorie",
                    style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        } else {
            // Toon de lijst met items
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items = itemsToShow) { item ->
                    ItemBox(item = item, navController = navController)
                }
            }
        }
    }
}


