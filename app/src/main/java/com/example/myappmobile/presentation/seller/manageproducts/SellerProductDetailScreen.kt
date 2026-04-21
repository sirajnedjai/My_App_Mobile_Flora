package com.example.myappmobile.presentation.seller.manageproducts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myappmobile.core.components.FloraRemoteImage
import com.example.myappmobile.core.components.CircularIconButton
import com.example.myappmobile.core.components.PrimaryButton
import com.example.myappmobile.core.theme.FloraBeige
import com.example.myappmobile.core.theme.FloraBrown
import com.example.myappmobile.core.theme.FloraSelectedCard
import com.example.myappmobile.core.theme.FloraText
import com.example.myappmobile.core.theme.FloraTextSecondary
import com.example.myappmobile.core.theme.SerifFontFamily
import com.example.myappmobile.core.theme.White

@Composable
fun SellerProductDetailScreen(
    onBack: () -> Unit,
    onDeleted: () -> Unit = onBack,
    viewModel: SellerProductDetailViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.deleted) {
        if (uiState.deleted) {
            onDeleted()
        }
    }

    Scaffold(
        containerColor = FloraBeige,
        topBar = { SellerProductDetailTopBar(onBack = onBack) },
    ) { paddingValues ->
        when {
            uiState.isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Text("Loading product details...", color = FloraTextSecondary)
            }

            !uiState.isSeller -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Text(uiState.errorMessage ?: "Seller tools are available only for seller accounts.", color = FloraTextSecondary)
            }

            uiState.details == null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(uiState.errorMessage ?: "Product details are not available.", color = FloraTextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    PrimaryButton(text = "Retry", onClick = viewModel::refresh, fillMaxWidth = false)
                }
            }

            else -> SellerProductDetailContent(
                uiState = uiState,
                onRetry = viewModel::refresh,
                onDeleteRequest = viewModel::requestDelete,
                modifier = Modifier.padding(paddingValues),
            )
        }
    }

    if (uiState.pendingDelete) {
        uiState.details?.product?.let { product ->
            DeleteProductDialog(
                product = product,
                onDismiss = viewModel::dismissDelete,
                onConfirm = viewModel::deleteProduct,
                isDeleting = uiState.isDeleting,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SellerProductDetailTopBar(onBack: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Product Details",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = SerifFontFamily,
                    fontStyle = FontStyle.Italic,
                ),
                color = FloraText,
            )
        },
        navigationIcon = {
            CircularIconButton(
                icon = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
                onClick = onBack,
            )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = FloraBeige,
            scrolledContainerColor = FloraBeige,
        ),
    )
}

@Composable
private fun SellerProductDetailContent(
    uiState: SellerProductDetailUiState,
    onRetry: () -> Unit,
    onDeleteRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val details = requireNotNull(uiState.details)
    val product = details.product

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FloraBeige),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        uiState.errorMessage?.takeIf { it.isNotBlank() }?.let { message ->
            item {
                DetailCard(title = "Refresh issue") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(message, color = FloraTextSecondary)
                        PrimaryButton(text = "Retry", onClick = onRetry, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    FloraRemoteImage(
                        imageUrl = product.imageUrl.ifBlank { product.images.firstOrNull().orEmpty() },
                        contentDescription = product.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(24.dp)),
                        contentScale = ContentScale.Crop,
                    )
                    Text(
                        text = product.category.ifBlank { "FLORA Piece" }.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = FloraBrown,
                    )
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = FloraText,
                    )
                    Text(
                        text = product.description.ifBlank { "No product description is available yet." },
                        style = MaterialTheme.typography.bodyMedium,
                        color = FloraTextSecondary,
                    )
                }
            }
        }

        item {
            DetailCard(title = "Catalog Summary") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryRow(label = "Price", value = "$${"%.2f".format(product.price)}")
                    SummaryRow(label = "Stock", value = product.stockCount.toString())
                    SummaryRow(label = "Status", value = details.status.ifBlank { "Not provided" })
                    SummaryRow(label = "Seller", value = details.sellerName.ifBlank { product.studio.ifBlank { "Not provided" } })
                    SummaryRow(label = "Product ID", value = product.id)
                }
            }
        }

        item {
            DetailCard(title = "Dates") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryRow(label = "Created", value = details.createdAt.ifBlank { "Not provided" })
                    SummaryRow(label = "Updated", value = details.updatedAt.ifBlank { "Not provided" })
                }
            }
        }

        if (product.images.size > 1) {
            item {
                DetailCard(title = "Gallery") {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        userScrollEnabled = false,
                    ) {
                        items(product.images.filter { it.isNotBlank() }) { imageUrl ->
                            FloraRemoteImage(
                                imageUrl = imageUrl,
                                contentDescription = product.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(20.dp)),
                                contentScale = ContentScale.Crop,
                            )
                        }
                    }
                }
            }
        }

        item {
            DetailCard(title = "Seller actions") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Delete uses the authenticated seller `/products/{id}` backend route and only updates the UI after the server confirms the product was removed.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FloraTextSecondary,
                    )
                    PrimaryButton(
                        text = if (uiState.isDeleting) "Deleting..." else "Delete Product",
                        onClick = onDeleteRequest,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isDeleting,
                        isLoading = uiState.isDeleting,
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = FloraSelectedCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = FloraText,
            )
            content()
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = FloraTextSecondary)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = FloraText)
    }
}
