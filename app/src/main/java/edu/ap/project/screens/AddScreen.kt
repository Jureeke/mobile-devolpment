package edu.ap.project.screens

import ItemViewModel
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.GeoPoint
import edu.ap.project.model.ItemType

@Composable
fun AddScreen(itemViewModel: ItemViewModel = viewModel()) {
    // Observing the state of the current user's UID
    val currentUserUid by itemViewModel.currentUserUid.observeAsState()

    // Observing the status of the item addition (success/failure)
    val itemAddedStatus by itemViewModel.itemAddedStatus.observeAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ItemType.values().first().typeName) }
    var expanded by remember { mutableStateOf(false) }  // Set to false initially

    val location = GeoPoint(0.0, 0.0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Voeg een nieuw item toe")
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Titel") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Beschrijving") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )
        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("Prijs") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .heightIn(min = 56.dp, max = 100.dp)
        ) {
            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("Foto van het item URL") },
            )
        }

        // Dropdown for selecting item type
        Box(modifier = Modifier
            .fillMaxWidth().padding(vertical = 20.dp)
            .clickable {
                expanded = !expanded // Toggle the dropdown visibility
            }
        ) {
            OutlinedTextField(
                value = selectedType,
                onValueChange = {}, // Read-only, no changes allowed here
                readOnly = true,
                label = { Text("Type van het item") },
                modifier = Modifier
                    .fillMaxWidth()
            )
            // Dropdown menu that only shows when expanded is true
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                    Log.d("DropdownDebug", "Dropdown dismissed") // Debug log
                },
                modifier = Modifier.zIndex(1f) // Ensure dropdown is above other components
            ) {
                // Loop through all ItemType values and create a menu item for each one
                ItemType.values().forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.typeName) },
                        onClick = {
                            selectedType = type.typeName // Update selected type
                            expanded = false // Close the dropdown when an item is selected
                        }
                    )
                }
            }
        }

        Button(
            onClick = {
                // Check if the current user's UID is available
                if (currentUserUid != null) {
                    itemViewModel.addItem(title, description, price, imageUrl, location, selectedType)
                }
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Toevoegen")
        }
    }
}


