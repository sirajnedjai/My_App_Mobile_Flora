package com.example.myappmobile.presentation.seller.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myappmobile.core.utils.formatPriceDzd

@Composable
fun StoreProductsScreen(
    viewModel: StoreProductsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val errorMessage = uiState.errorMessage

    Scaffold { padding ->
        when {
            uiState.isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                CircularProgressIndicator(modifier = Modifier.padding(20.dp))
            }
            errorMessage != null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp),
            ) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
            }
            uiState.products.isEmpty() -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp),
            ) {
                Text("No products are available for this store yet.")
            }
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(uiState.products, key = { it.id }) { product ->
                    Card {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(product.name, style = MaterialTheme.typography.titleMedium)
                            Text(product.description.ifBlank { product.category }, style = MaterialTheme.typography.bodyMedium)
                            Text(formatPriceDzd(product.price), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
