package com.example.myappmobile.presentation.checkout.address

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.example.myappmobile.core.components.PrimaryButton

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
                value = uiState.state,
                onValueChange = viewModel::onStateChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("State") },
            )
            OutlinedTextField(
                value = uiState.municipality,
                onValueChange = viewModel::onMunicipalityChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Municipality") },
            )
            OutlinedTextField(
                value = uiState.neighborhood,
                onValueChange = viewModel::onNeighborhoodChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Neighborhood") },
            )
            OutlinedTextField(
                value = uiState.streetAddress,
                onValueChange = viewModel::onStreetChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Street address") },
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
            PrimaryButton(
                text = "Continue to Shipping",
                onClick = { viewModel.saveAddress(onContinue) },
                enabled = uiState.isValid,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckoutTopBar(title: String) {
    TopAppBar(title = { Text(title) })
}
