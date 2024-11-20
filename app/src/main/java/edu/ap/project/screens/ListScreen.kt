package edu.ap.project.screens

import ItemViewModel
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import edu.ap.project.model.Item
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ItemBox(item: Item) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp))
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            // Title
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.primary),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            // Description
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            // Price
            Text(
                text = "Prijs: €${item.price}",
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            // Renter
            val renter = item.renter
            if (renter != null) {
                Text(
                    text = "Huurder: ${item.renter}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            } else {
                Text(
                    text = "Niet Verhuurd",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface)
                )
            }

            // Type
            Text(
                text = "Type: ${item.type}",
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            // End Date
            val endDate = item.endDate?.toDate()
            if (endDate != null) {
                // If endDate is not null, format it
                Text(
                    text = "Eind Datum: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(endDate)}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface)
                )
            } else {
                // If endDate is null, show a default message
                Text(
                    text = "Eind Datum: N.V.T.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface)
                )
            }
        }
    }
}



@Composable
fun ListScreen(itemViewModel: ItemViewModel = viewModel()) {
    val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
    val userItems = itemViewModel.userItems.observeAsState(emptyList())
    val allItems = itemViewModel.allItems.observeAsState(emptyList())
    val isViewingUserItems = remember { mutableStateOf(false) } // Use state for reactivity

    // Fetch items on initial composition
    LaunchedEffect(currentUserUid) {
        itemViewModel.getItemsForUser(currentUserUid)
        itemViewModel.getAllItems()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = if (isViewingUserItems.value) "Mijn Items" else "Alle Items",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Button(
            onClick = { isViewingUserItems.value = !isViewingUserItems.value }, // Update state
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(text = if (isViewingUserItems.value) "Toon Alle Items" else "Mijn Items")
        }

        val itemsToShow = if (isViewingUserItems.value) userItems.value else allItems.value
        Log.d("test", "showing ${if (isViewingUserItems.value) "user items" else "all items"}")
        itemsToShow.forEach { item ->
            ItemBox(item = item)
        }
    }
}



