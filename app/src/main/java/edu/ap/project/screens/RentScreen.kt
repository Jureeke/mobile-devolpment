package edu.ap.project.screens

import ItemViewModel
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun RentScreen(
    itemId: String,
    itemViewModel: ItemViewModel = viewModel(),
) {

    LaunchedEffect(itemId) {
        Log.d("ItemViewModel",itemId)
        itemViewModel.fetchItemById(itemId = itemId)
    }

    val currentItem = itemViewModel.currentItem.observeAsState().value
    var selectedDateRange by remember { mutableStateOf("") }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var totalPrice by remember { mutableStateOf(0.0) }

    var _startDate: Date? by remember { mutableStateOf(null) }
    var _endDate: Date? by remember { mutableStateOf(null) }

    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())


    // Handle the date range selection
    val onDateRangeSelected: (Pair<Long?, Long?>) -> Unit = { dateRange ->
        val startDate = dateRange.first?.let { Date(it) }
        val endDate = dateRange.second?.let { Date(it) }

        if (startDate != null) {
            _startDate = startDate
        }

        if (endDate != null) {
            _endDate = endDate
        }

        // Calculate the total price if both start and end dates are selected
        if (startDate != null && endDate != null) {
            val daysBetween = ((endDate.time - startDate.time) / (1000 * 60 * 60 * 24)).toInt()
            totalPrice = daysBetween * currentItem?.price!!

            // Format the date range as a string
            selectedDateRange = "${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}"
        }
    }

    // Handle dismissing the date picker dialog
    val onDismissDatePicker = { showDatePickerDialog = false }

    // Show DatePickerDialog when needed
    if (showDatePickerDialog) {
        DateRangePickerModal(
            onDateRangeSelected = onDateRangeSelected,
            onDismiss = onDismissDatePicker,
            disabledDates = emptyList() // Pass the list of disabled dates here if needed
        )
    }

    // Display the UI
    Column(modifier = Modifier.padding(16.dp)) {
        // Display Title, Description, and Price
        Text(text = currentItem?.title ?: "no title", style = MaterialTheme.typography.headlineMedium)
        Text(text = currentItem?.description ?: "no description", style = MaterialTheme.typography.bodyMedium)
        Text(text = "Prijs per dag: €${currentItem?.price}", style = MaterialTheme.typography.bodyLarge)

        // Select Date Range Button
        TextButton(onClick = { showDatePickerDialog = true }) {
            Text("Kies Periode")
        }

        // Display selected date range
        Text(text = "Geselecteerde periode: $selectedDateRange")

        // Display the total price if dates are selected
        if (totalPrice > 0) {
            Text(text = "Totale prijs: €$totalPrice")
        }

        // Huur button - only visible after date range is selected
        if (totalPrice > 0) {
            TextButton(onClick = {
                // Call the rent function with the selected dates
                itemViewModel.rentItem(itemId, _startDate!!, _endDate!!)
            }) {
                Text("Huur")
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerModal(
    onDateRangeSelected: (Pair<Long?, Long?>) -> Unit,
    onDismiss: () -> Unit,
    disabledDates: List<Date>
) {
    val today = Calendar.getInstance()
    today.set(Calendar.HOUR_OF_DAY, 0)
    today.set(Calendar.MINUTE, 0)
    today.set(Calendar.SECOND, 0)
    today.set(Calendar.MILLISECOND, 0)

    // Create a list of all dates that are within rental periods
    val allDisabledDates = mutableSetOf<Triple<Int, Int, Int>>()
    disabledDates.forEach { date ->
        val cal = Calendar.getInstance()
        cal.time = date
        allDisabledDates.add(
            Triple(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
        )
    }

    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = null,
        initialSelectedEndDateMillis = null,
        yearRange = IntRange(
            today.get(Calendar.YEAR),
            today.get(Calendar.YEAR) + 1
        ),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = utcTimeMillis

                // Check if date is not in the past
                val isNotPast = calendar.timeInMillis >= today.timeInMillis

                // Check if date is not in disabled dates
                val dateTriple = Triple(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
                val isNotDisabled = !allDisabledDates.contains(dateTriple)

                return isNotPast && isNotDisabled
            }
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onDateRangeSelected(
                        Pair(
                            dateRangePickerState.selectedStartDateMillis,
                            dateRangePickerState.selectedEndDateMillis
                        )
                    )
                    onDismiss()
                },
                enabled = dateRangePickerState.selectedStartDateMillis != null &&
                        dateRangePickerState.selectedEndDateMillis != null
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DateRangePicker(
            state = dateRangePickerState,
            title = {
                Text(
                    "Select Rental Period",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp)
                )
            },
            headline = {
                Text(
                    "Choose your start and end dates",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
                )
            },
            showModeToggle = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp),
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.primary,
                headlineContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                weekdayContentColor = MaterialTheme.colorScheme.onSurface,
                subheadContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                yearContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                currentYearContentColor = MaterialTheme.colorScheme.primary,
                selectedYearContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedYearContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                dayContentColor = MaterialTheme.colorScheme.onSurface,
                selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                todayContentColor = MaterialTheme.colorScheme.primary,
                todayDateBorderColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

