package com.example.myappmobile.presentation.seller.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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

@Composable
fun AboutStoreScreen(
    viewModel: AboutStoreViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val errorMessage = uiState.errorMessage
    val store = uiState.store

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
            store == null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp),
            ) {
                Text("Store details are unavailable right now.")
            }
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp),
            ) {
                Text(store.name, style = MaterialTheme.typography.headlineMedium)
                Text(store.story.ifBlank { store.description }, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
