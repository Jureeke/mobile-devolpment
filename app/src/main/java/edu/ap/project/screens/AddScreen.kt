package edu.ap.project.screens

import ItemViewModel
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberImagePainter
import com.google.firebase.firestore.GeoPoint
import edu.ap.project.model.ItemType

@Composable
fun AddScreen(itemViewModel: ItemViewModel = viewModel()) {
    val currentUserUid by itemViewModel.currentUserUid.observeAsState()

    val itemAddedStatus by itemViewModel.itemAddedStatus.observeAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ItemType.values().first().typeName) }
    var expanded by remember { mutableStateOf(false) }

    val location = GeoPoint(0.0, 0.0)

    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Voeg een nieuw item toe",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Titel
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Titel") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            singleLine = true
        )

        // Beschrijving
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Beschrijving") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            maxLines = 3
        )

        // Prijs
        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("Prijs") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )

        // Foto
        OutlinedTextField(
            value = imageUrl,
            onValueChange = { imageUrl = it },
            label = { Text("Foto van het item URL") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            singleLine = true
        )

        // Foto Preview
        if (imageUrl.isNotEmpty()) {
            val painter = rememberImagePainter(imageUrl)
            Image(
                painter = painter,
                contentDescription = "Afbeelding preview",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(top = 8.dp)
                    .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }


        Box(modifier = Modifier
            .fillMaxWidth().padding(vertical = 8.dp)
            .clickable { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Type van het item") },
                modifier = Modifier.fillMaxWidth(),
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.zIndex(1f)
            ) {
                ItemType.values().forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.typeName) },
                        onClick = {
                            selectedType = type.typeName
                            expanded = false
                        }
                    )
                }
            }
        }

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Button(
            onClick = {
                if (title.isEmpty() || description.isEmpty() || price.isEmpty() || imageUrl.isEmpty()) {
                    errorMessage = "Vul alle velden in."
                } else {
                    errorMessage = ""
                    if (currentUserUid != null) {
                        itemViewModel.addItem(title, description, price, imageUrl, location, selectedType)
                    }
                }
            },
            modifier = Modifier.padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Toevoegen", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        itemAddedStatus?.let { status ->
            if (status) {
                Text("Item succesvol toegevoegd!", color = Color.Green, modifier = Modifier.padding(top = 16.dp))
            } else {
                Text("Fout bij het toevoegen van het item. Probeer het opnieuw.", color = Color.Red, modifier = Modifier.padding(top = 16.dp))
            }
        }
    }
}
