package edu.ap.project.screens

import ItemViewModel
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.material.datepicker.MaterialDatePicker
import androidx.core.util.Pair
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RentScreen(
    itemId: String,
    itemViewModel: ItemViewModel = viewModel(),
) {
    var selectedDateRange by remember { mutableStateOf<String>("") }
    val context = LocalContext.current // Get the context

    // Initialize MaterialDatePicker for date range selection
    val picker = MaterialDatePicker.Builder.dateRangePicker()
        .setTitleText("Selecteer de huurperiode")
        .setSelection(Pair<Long, Long>(null, null)) // Use androidx.core.util.Pair
        .build()

    // Button to open the MaterialDatePicker
    Button(onClick = {
        // Ensure that the context is a FragmentActivity
        val activity = context as? ComponentActivity
        activity?.supportFragmentManager?.let { fragmentManager ->
            picker.show(fragmentManager, "tag")
        }
    }) {
        Text("Selecteer een huurperiode")
    }

    // Listen for the date range selection
    picker.addOnPositiveButtonClickListener { selection ->
        val startDate = selection.first?.let { Date(it) }
        val endDate = selection.second?.let { Date(it) }
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        if (startDate != null && endDate != null) {
            val formattedStartDate = dateFormat.format(startDate)
            val formattedEndDate = dateFormat.format(endDate)
            selectedDateRange = "$formattedStartDate - $formattedEndDate" // Update the selected date range
        }
    }

    // Display the selected date range
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Geselecteerde periode: $selectedDateRange")
    }
}
