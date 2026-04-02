package com.example.myappmobile.presentation.seller.storefront

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myappmobile.core.navigation.AppBottomBar

@Composable
fun StoreFrontScreen(
    onOpenProducts: () -> Unit = {},
    onOpenAbout: () -> Unit = {},
    onOpenReviews: () -> Unit = {},
    selectedRoute: String = "seller",
    onBottomNavClick: (String) -> Unit = {},
    viewModel: StoreFrontViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val store = uiState.store

    Scaffold(
        bottomBar = {
            AppBottomBar(
                selectedRoute = selectedRoute,
                onNavigate = onBottomNavClick,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(store?.name.orEmpty(), style = MaterialTheme.typography.headlineMedium)
            Text(store?.description.orEmpty(), style = MaterialTheme.typography.bodyMedium)
            Text("Location: ${store?.location.orEmpty()}", style = MaterialTheme.typography.bodyMedium)
            Button(onClick = onOpenProducts, modifier = Modifier.fillMaxWidth()) {
                Text("View Products")
            }
            Button(onClick = onOpenAbout, modifier = Modifier.fillMaxWidth()) {
                Text("About the Studio")
            }
            Button(onClick = onOpenReviews, modifier = Modifier.fillMaxWidth()) {
                Text("Customer Reviews")
            }
        }
    }
}
