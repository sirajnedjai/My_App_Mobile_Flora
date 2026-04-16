package com.example.myappmobile.presentation.seller.manageproducts

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myappmobile.core.components.CircularIconButton
import com.example.myappmobile.core.components.DangerButton
import com.example.myappmobile.core.components.OutlineButton
import com.example.myappmobile.core.components.PrimaryButton
import com.example.myappmobile.core.components.SecondaryButton
import com.example.myappmobile.core.components.SmallActionButton
import com.example.myappmobile.core.theme.FloraBeige
import com.example.myappmobile.core.theme.FloraBrown
import com.example.myappmobile.core.theme.FloraCardBg
import com.example.myappmobile.core.theme.FloraDivider
import com.example.myappmobile.core.theme.FloraError
import com.example.myappmobile.core.theme.FloraText
import com.example.myappmobile.core.theme.FloraTextSecondary
import com.example.myappmobile.core.theme.SerifFontFamily
import com.example.myappmobile.core.theme.StoneFaint
import com.example.myappmobile.core.theme.White
import com.example.myappmobile.domain.model.Product

@Composable
fun SellerProductManagementScreen(
    onBack: () -> Unit = {},
    viewModel: SellerProductManagementViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    SellerProductManagementContent(
        uiState = uiState,
        onBack = onBack,
        onAddProduct = viewModel::showAddProductDialog,
        onEditProduct = viewModel::editProduct,
        onDeleteRequest = viewModel::requestDelete,
        onDismissEditor = viewModel::dismissEditor,
        onNameChanged = viewModel::onNameChanged,
        onPriceChanged = viewModel::onPriceChanged,
        onCategoryChanged = viewModel::onCategoryChanged,
        onDescriptionChanged = viewModel::onDescriptionChanged,
        onImageUrlChanged = viewModel::onImageUrlChanged,
        onStockCountChanged = viewModel::onStockCountChanged,
        onSaveProduct = viewModel::saveProduct,
        onDismissDelete = viewModel::dismissDelete,
        onConfirmDelete = viewModel::deleteProduct,
    )
}

@Composable
private fun SellerProductManagementContent(
    uiState: SellerProductManagementUiState,
    onBack: () -> Unit,
    onAddProduct: () -> Unit,
    onEditProduct: (Product) -> Unit,
    onDeleteRequest: (Product) -> Unit,
    onDismissEditor: () -> Unit,
    onNameChanged: (String) -> Unit,
    onPriceChanged: (String) -> Unit,
    onCategoryChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onImageUrlChanged: (String) -> Unit,
    onStockCountChanged: (String) -> Unit,
    onSaveProduct: () -> Unit,
    onDismissDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
) {
    Scaffold(
        containerColor = FloraBeige,
        topBar = { SellerManagementTopBar(onBack = onBack) },
    ) { paddingValues ->
        if (!uiState.isSeller) {
            AccessRestrictedState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(FloraBeige)
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                item {
                    SellerManagementHero(
                        productCount = uiState.products.size,
                        onAddProduct = onAddProduct,
                    )
                }

                items(uiState.products, key = { it.id }) { product ->
                    SellerProductCard(
                        product = product,
                        onEdit = { onEditProduct(product) },
                        onDelete = { onDeleteRequest(product) },
                    )
                }

                if (uiState.products.isEmpty()) {
                    item {
                        EmptySellerCatalogCard(onAddProduct = onAddProduct)
                    }
                }
            }
        }
    }

    if (uiState.isEditorVisible) {
        SellerProductEditorDialog(
            uiState = uiState,
            onDismiss = onDismissEditor,
            onNameChanged = onNameChanged,
            onPriceChanged = onPriceChanged,
            onCategoryChanged = onCategoryChanged,
            onDescriptionChanged = onDescriptionChanged,
            onImageUrlChanged = onImageUrlChanged,
            onStockCountChanged = onStockCountChanged,
            onSave = onSaveProduct,
        )
    }

    uiState.pendingDelete?.let { product ->
        DeleteProductDialog(
            product = product,
            onDismiss = onDismissDelete,
            onConfirm = onConfirmDelete,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SellerManagementTopBar(onBack: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Seller Studio",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
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
private fun SellerManagementHero(
    productCount: Int,
    onAddProduct: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(White, FloraCardBg),
                    ),
                )
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(
                text = "Craft your next collection",
                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = SerifFontFamily),
                color = FloraText,
            )
            Text(
                text = "Add, refine, and curate the pieces your customers discover across FLORA.",
                style = MaterialTheme.typography.bodyMedium,
                color = FloraTextSecondary,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SellerMetricChip(
                    icon = Icons.Outlined.Inventory2,
                    label = "Active Products",
                    value = productCount.toString(),
                )
                SecondaryButton(
                    text = "Add New Piece",
                    onClick = onAddProduct,
                    fillMaxWidth = false,
                    leadingIcon = Icons.Outlined.Add,
                )
            }
        }
    }
}

@Composable
private fun SellerMetricChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = StoneFaint,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = FloraBrown)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelMedium, color = FloraTextSecondary)
                Text(value, style = MaterialTheme.typography.titleMedium, color = FloraText)
            }
        }
    }
}

@Composable
private fun SellerProductCard(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(22.dp)),
                    contentScale = ContentScale.Crop,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = product.category.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = FloraBrown,
                    )
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = FloraText,
                    )
                    Text(
                        text = product.description.ifBlank { "A crafted piece ready for your FLORA edit." },
                        style = MaterialTheme.typography.bodyMedium,
                        color = FloraTextSecondary,
                        maxLines = 3,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                        Text(
                            text = "$${"%.2f".format(product.price)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = FloraText,
                        )
                        Text(
                            text = "Stock ${product.stockCount}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = FloraTextSecondary,
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = FloraDivider,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlineButton(
                    text = "Edit",
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    fillMaxWidth = false,
                    leadingIcon = Icons.Outlined.Edit,
                )
                SmallActionButton(
                    text = "Delete",
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    leadingIcon = Icons.Outlined.DeleteOutline,
                    danger = true,
                )
            }
        }
    }
}

@Composable
private fun EmptySellerCatalogCard(onAddProduct: () -> Unit) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = White),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Inventory2,
                contentDescription = null,
                modifier = Modifier.size(34.dp),
                tint = FloraBrown,
            )
            Text(
                text = "Your studio is ready for its first listing.",
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = SerifFontFamily),
            )
            Text(
                text = "Add a product to start surfacing your collection in the FLORA shop.",
                style = MaterialTheme.typography.bodyMedium,
                color = FloraTextSecondary,
            )
            PrimaryButton(
                text = "Create Product",
                onClick = onAddProduct,
                fillMaxWidth = false,
            )
        }
    }
}

@Composable
private fun AccessRestrictedState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = White),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Seller access required",
                    style = MaterialTheme.typography.headlineSmall.copy(fontFamily = SerifFontFamily),
                )
                Text(
                    text = "Sign in with a seller account to manage products in your FLORA studio.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FloraTextSecondary,
                )
            }
        }
    }
}

@Composable
private fun SellerProductEditorDialog(
    uiState: SellerProductManagementUiState,
    onDismiss: () -> Unit,
    onNameChanged: (String) -> Unit,
    onPriceChanged: (String) -> Unit,
    onCategoryChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onImageUrlChanged: (String) -> Unit,
    onStockCountChanged: (String) -> Unit,
    onSave: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(30.dp),
            color = White,
            tonalElevation = 0.dp,
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (uiState.editingProductId == null) "Add Product" else "Edit Product",
                            style = MaterialTheme.typography.headlineMedium.copy(fontFamily = SerifFontFamily),
                        )
                        Text(
                            text = "Present your piece with enough detail for buyers to trust the craft.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = FloraTextSecondary,
                        )
                    }
                }

                item {
                    SellerPreviewCard(
                        imageUrl = uiState.imageUrl,
                        name = uiState.name,
                        category = uiState.category,
                        price = uiState.price,
                    )
                }

                item {
                    SectionTitle("Core Details")
                }

                item {
                    FloralInput(
                        value = uiState.name,
                        onValueChange = onNameChanged,
                        label = "Product Name",
                        leadingIcon = Icons.Outlined.Sell,
                    )
                }

                item {
                    FloralInput(
                        value = uiState.description,
                        onValueChange = onDescriptionChanged,
                        label = "Description",
                        minLines = 4,
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            FloralInput(
                                value = uiState.price,
                                onValueChange = onPriceChanged,
                                label = "Price",
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            FloralInput(
                                value = uiState.stockCount,
                                onValueChange = onStockCountChanged,
                                label = "Stock",
                                leadingIcon = Icons.Outlined.Inventory2,
                            )
                        }
                    }
                }

                item {
                    FloralInput(
                        value = uiState.category,
                        onValueChange = onCategoryChanged,
                        label = "Category",
                    )
                }

                item {
                    SectionTitle("Presentation")
                }

                item {
                    FloralInput(
                        value = uiState.imageUrl,
                        onValueChange = onImageUrlChanged,
                        label = "Image URL",
                        leadingIcon = Icons.Outlined.Link,
                    )
                }

                uiState.formError?.let { error ->
                    item {
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = FloraError,
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlineButton(
                            text = "Cancel",
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            fillMaxWidth = false,
                        )
                        PrimaryButton(
                            text = if (uiState.isSaving) "Saving..." else "Save Product",
                            onClick = onSave,
                            modifier = Modifier.weight(1f),
                            enabled = !uiState.isSaving,
                            isLoading = uiState.isSaving,
                            fillMaxWidth = false,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SellerPreviewCard(
    imageUrl: String,
    name: String,
    category: String,
    price: String,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = FloraCardBg),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(82.dp)
                    .clip(RoundedCornerShape(18.dp)),
                contentScale = ContentScale.Crop,
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = category.ifBlank { "CATEGORY" }.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = FloraBrown,
                )
                Text(
                    text = name.ifBlank { "Your new FLORA piece" },
                    style = MaterialTheme.typography.titleMedium,
                    color = FloraText,
                )
                Text(
                    text = if (price.isBlank()) "$0.00" else "$$price",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FloraTextSecondary,
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = FloraText,
    )
}

@Composable
private fun FloralInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    minLines: Int = 1,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        minLines = minLines,
        maxLines = if (minLines == 1) 1 else 6,
        shape = RoundedCornerShape(20.dp),
        leadingIcon = leadingIcon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = FloraTextSecondary,
                )
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = White,
            unfocusedContainerColor = White,
            focusedBorderColor = FloraBrown,
            unfocusedBorderColor = FloraDivider,
        ),
    )
}

@Composable
private fun DeleteProductDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = White,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Delete Product",
                    style = MaterialTheme.typography.headlineSmall.copy(fontFamily = SerifFontFamily),
                )
                Text(
                    text = "Remove ${product.name} from your FLORA catalog? This will also remove it from the shop list.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FloraTextSecondary,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlineButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        fillMaxWidth = false,
                    )
                    DangerButton(
                        text = "Delete",
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        fillMaxWidth = false,
                    )
                }
            }
        }
    }
}
