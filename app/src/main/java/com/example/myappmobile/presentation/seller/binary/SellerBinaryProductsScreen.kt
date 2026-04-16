package com.example.myappmobile.presentation.seller.binary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myappmobile.core.components.CircularIconButton
import com.example.myappmobile.core.theme.Cream
import com.example.myappmobile.core.theme.FloraCardBg
import com.example.myappmobile.core.theme.FloraText
import com.example.myappmobile.core.theme.FloraTextSecondary
import com.example.myappmobile.domain.model.Product

@Composable
fun SellerBinaryProductsScreen(
    onBack: () -> Unit = {},
    viewModel: SellerBinaryProductsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Cream,
        topBar = { SellerBinaryProductsTopBar(onBack = onBack) },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Cream)
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Text(
                    text = "${uiState.storeName.ifBlank { "Seller" }} product feed",
                    style = MaterialTheme.typography.headlineSmall,
                    color = FloraText,
                )
                Text(
                    text = "Binary view interpreted as a structured raw product ledger for fast review.",
                    style = MaterialTheme.typography.bodySmall,
                    color = FloraTextSecondary,
                )
            }

            items(uiState.products, key = Product::id) { product ->
                BinaryProductCard(product = product)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SellerBinaryProductsTopBar(onBack: () -> Unit) {
    CenterAlignedTopAppBar(
        title = { Text("Seller Product Feed", color = FloraText) },
        navigationIcon = {
            CircularIconButton(
                icon = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
                onClick = onBack,
            )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Cream,
            scrolledContainerColor = Cream,
        ),
    )
}

@Composable
private fun BinaryProductCard(product: Product) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = FloraCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium,
                color = FloraText,
            )
            BinaryLine("id", product.id)
            BinaryLine("storeId", product.storeId.ifBlank { "unknown" })
            BinaryLine("studio", product.studio)
            BinaryLine("price", "$${"%.2f".format(product.price)}")
            BinaryLine("stockCount", product.stockCount.toString())
            BinaryLine("category", product.category.ifBlank { "uncategorized" })
            BinaryLine("binaryId", product.id.encodeAsBinary())
        }
    }
}

@Composable
private fun BinaryLine(label: String, value: String) {
    Text(
        text = "$label: $value",
        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
        color = FloraText,
    )
}

private fun String.encodeAsBinary(): String = take(12)
    .map { character -> character.code.toString(2).padStart(8, '0') }
    .joinToString(separator = " ")
