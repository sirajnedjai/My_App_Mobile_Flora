package com.example.myappmobile.presentation.seller.reviews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun StoreReviewsScreen(
    viewModel: StoreReviewsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(uiState.reviews, key = { it.id }) { review ->
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(review.authorName, style = MaterialTheme.typography.titleMedium)
                        Text("Rating: ${review.rating}", style = MaterialTheme.typography.bodyMedium)
                        Text(review.text, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
