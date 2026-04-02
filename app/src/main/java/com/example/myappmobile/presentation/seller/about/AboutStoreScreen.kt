package com.example.myappmobile.presentation.seller.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
fun AboutStoreScreen(
    viewModel: AboutStoreViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val store = uiState.store

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
        ) {
            Text(store?.name.orEmpty(), style = MaterialTheme.typography.headlineMedium)
            Text(store?.story.orEmpty(), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
