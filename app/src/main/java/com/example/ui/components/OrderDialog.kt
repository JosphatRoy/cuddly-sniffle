package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun OrderDialog(
    availableRoutes: List<String>,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        phone: String,
        address: String,
        liters: Double,
        price: Double,
        route: String,
        notes: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var litersStr by remember { mutableStateOf("5.0") }
    var priceStr by remember { mutableStateOf("120.0") }
    var selectedRoute by remember { mutableStateOf(availableRoutes.firstOrNull() ?: "") }
    var notes by remember { mutableStateOf("") }

    var expandedRouteMenu by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf(false) }
    var addressError by remember { mutableStateOf(false) }
    var litersError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add New Milk Order",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = it.isBlank()
                    },
                    label = { Text("Customer Name") },
                    isError = nameError,
                    supportingText = { if (nameError) Text("Name cannot be empty") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_customer_name_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = {
                        address = it
                        addressError = it.isBlank()
                    },
                    label = { Text("Delivery Address") },
                    isError = addressError,
                    supportingText = { if (addressError) Text("Address cannot be empty") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_address_input"),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = litersStr,
                        onValueChange = {
                            litersStr = it
                            litersError = it.toDoubleOrNull() == null || it.toDouble() <= 0
                        },
                        label = { Text("Milk (Liters)") },
                        isError = litersError,
                        supportingText = { if (litersError) Text("Enter valid liters") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_liters_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = {
                            priceStr = it
                            priceError = it.toDoubleOrNull() == null || it.toDouble() < 0
                        },
                        label = { Text("Price/Liter (KSh)") },
                        isError = priceError,
                        supportingText = { if (priceError) Text("Enter valid price") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Route Dropdown Selection
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedRoute,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Assigned Delivery Route") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown arrow",
                                modifier = Modifier.clickable { expandedRouteMenu = true }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedRouteMenu = true }
                    )
                    DropdownMenu(
                        expanded = expandedRouteMenu,
                        onDismissRequest = { expandedRouteMenu = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        availableRoutes.forEach { route ->
                            DropdownMenuItem(
                                text = { Text(route) },
                                onClick = {
                                    selectedRoute = route
                                    expandedRouteMenu = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Delivery Notes / Instructions") },
                    placeholder = { Text("e.g. Leave near porch cooler, dog on loose") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val isNameValid = name.isNotBlank()
                    val isAddressValid = address.isNotBlank()
                    val doubleLiters = litersStr.toDoubleOrNull()
                    val doublePrice = priceStr.toDoubleOrNull()
                    
                    nameError = !isNameValid
                    addressError = !isAddressValid
                    litersError = doubleLiters == null || doubleLiters <= 0
                    priceError = doublePrice == null || doublePrice < 0

                    if (isNameValid && isAddressValid && !litersError && !priceError) {
                        onSave(
                            name.trim(),
                            phone.trim(),
                            address.trim(),
                            doubleLiters!!,
                            doublePrice!!,
                            selectedRoute,
                            notes.trim()
                        )
                    }
                },
                modifier = Modifier.testTag("dialog_save_button")
            ) {
                Text("Save Order")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
