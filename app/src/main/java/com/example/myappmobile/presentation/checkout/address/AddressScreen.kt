package com.example.myappmobile.presentation.checkout.address

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myappmobile.core.theme.FloraBeige
import com.example.myappmobile.presentation.checkout.CheckoutSectionCard
import com.example.myappmobile.presentation.checkout.CheckoutStepHeader
import com.example.myappmobile.presentation.checkout.CheckoutTextField
import com.example.myappmobile.presentation.checkout.CheckoutTopBar
import com.example.myappmobile.presentation.checkout.FloraPrimaryButton

@Composable
fun AddressScreen(
    onContinue: () -> Unit = {},
    viewModel: AddressViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = FloraBeige,
        topBar = { CheckoutTopBar(title = "Shipping Address") },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            CheckoutStepHeader(
                stepLabel = "STEP 1 OF 4",
                title = "Where should we deliver your order?",
                subtitle = "Add a complete address so checkout, confirmation, and delivery details stay accurate.",
                modifier = Modifier.padding(top = 12.dp),
            )

            CheckoutSectionCard(
                title = "Recipient Details",
                subtitle = "Use the contact information that should appear on the order.",
                icon = Icons.Outlined.Person,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    CheckoutTextField(
                        value = uiState.fullName,
                        onValueChange = viewModel::onFullNameChange,
                        label = "Full name",
                        leadingIcon = Icons.Outlined.Person,
                    )
                    CheckoutTextField(
                        value = uiState.phoneNumber,
                        onValueChange = viewModel::onPhoneNumberChange,
                        label = "Phone number",
                        leadingIcon = Icons.Outlined.Phone,
                    )
                }
            }

            CheckoutSectionCard(
                title = "Delivery Address",
                subtitle = "We will use this location for shipment and order updates.",
                icon = Icons.Outlined.LocationOn,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    CheckoutTextField(
                        value = uiState.state,
                        onValueChange = viewModel::onStateChange,
                        label = "State",
                        leadingIcon = Icons.Outlined.LocationOn,
                    )
                    CheckoutTextField(
                        value = uiState.municipality,
                        onValueChange = viewModel::onMunicipalityChange,
                        label = "Municipality",
                        leadingIcon = Icons.Outlined.LocationOn,
                    )
                    CheckoutTextField(
                        value = uiState.neighborhood,
                        onValueChange = viewModel::onNeighborhoodChange,
                        label = "Neighborhood",
                        leadingIcon = Icons.Outlined.LocationOn,
                    )
                    CheckoutTextField(
                        value = uiState.streetAddress,
                        onValueChange = viewModel::onStreetChange,
                        label = "Street address",
                        leadingIcon = Icons.Outlined.LocationOn,
                    )
                    CheckoutTextField(
                        value = uiState.postalCode,
                        onValueChange = viewModel::onPostalCodeChange,
                        label = "Postal code",
                        leadingIcon = Icons.Outlined.LocationOn,
                    )
                    CheckoutTextField(
                        value = uiState.country,
                        onValueChange = viewModel::onCountryChange,
                        label = "Country",
                        leadingIcon = Icons.Outlined.LocationOn,
                    )
                }
            }

            FloraPrimaryButton(
                text = "Continue to Shipping",
                onClick = { viewModel.saveAddress(onContinue) },
                enabled = uiState.isValid,
                modifier = Modifier.padding(PaddingValues(bottom = 20.dp)),
            )
        }
    }
}
