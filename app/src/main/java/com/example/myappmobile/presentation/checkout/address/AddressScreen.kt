package com.example.myappmobile.presentation.checkout.address

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AddressScreen(
    onContinue: () -> Unit = {},
    viewModel: AddressViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(topBar = { CheckoutTopBar(title = "Shipping Address") }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = uiState.fullName,
                onValueChange = viewModel::onFullNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Full name") },
            )
            OutlinedTextField(
                value = uiState.street,
                onValueChange = viewModel::onStreetChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Street address") },
            )
            OutlinedTextField(
                value = uiState.city,
                onValueChange = viewModel::onCityChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("City") },
            )
            OutlinedTextField(
                value = uiState.postalCode,
                onValueChange = viewModel::onPostalCodeChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Postal code") },
            )
            OutlinedTextField(
                value = uiState.country,
                onValueChange = viewModel::onCountryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Country") },
            )
            Button(
                onClick = { viewModel.saveAddress(onContinue) },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.isValid,
            ) {
                Text("Continue to Shipping")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckoutTopBar(title: String) {
    TopAppBar(title = { Text(title) })
}
