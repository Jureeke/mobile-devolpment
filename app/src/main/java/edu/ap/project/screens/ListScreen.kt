package edu.ap.project.screens

import ItemViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
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
            .shadow(8.dp, RoundedCornerShape(16.dp))  // Add shadow for card effect
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


    // Fetch items on initial composition
    LaunchedEffect(currentUserUid) {
            itemViewModel.getItemsForUser(currentUserUid)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Lijst overzicht")
        // Display the fetched items
        userItems.value.forEach { item ->

            ItemBox(item = item)
        }
    }

}


