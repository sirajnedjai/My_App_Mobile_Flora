package com.example.myappmobile.presentation.profile.seller

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myappmobile.R
import com.example.myappmobile.core.components.AuthTextField
import com.example.myappmobile.core.components.CircularIconButton
import com.example.myappmobile.core.components.PrimaryButton
import com.example.myappmobile.core.components.SellerApprovalBadge
import com.example.myappmobile.core.theme.FloraBeige
import com.example.myappmobile.core.theme.FloraBrown
import com.example.myappmobile.core.theme.FloraCardBg
import com.example.myappmobile.core.theme.FloraError
import com.example.myappmobile.core.theme.FloraInputUnderline
import com.example.myappmobile.core.theme.FloraSelectedCard
import com.example.myappmobile.core.theme.FloraText
import com.example.myappmobile.core.theme.FloraTextMuted
import com.example.myappmobile.core.theme.FloraTextSecondary
import com.example.myappmobile.core.theme.SerifFontFamily
import com.example.myappmobile.core.theme.StoneFaint
import com.example.myappmobile.domain.model.SellerApprovalStatus

@Composable
fun StoreConfigurationScreen(
    onBack: () -> Unit = {},
    viewModel: StoreConfigurationViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            viewModel.onLogoSelected(uri.toString())
        }
    }

    Scaffold(
        containerColor = FloraBeige,
        topBar = { SellerSettingsTopBar(title = stringResource(R.string.profile_store_configuration), onBack = onBack) },
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(R.string.store_config_loading), color = FloraTextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(FloraBeige)
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                item {
                    StoreIdentityCard(
                        logoUri = uiState.logoUri,
                        shopName = uiState.shopName,
                        ownerName = uiState.ownerName,
                        approvalStatus = uiState.approvalStatus,
                        onChangeLogo = { logoPickerLauncher.launch(arrayOf("image/*")) },
                    )
                }

                item {
                    StoreConfigurationForm(
                        uiState = uiState,
                        onShopNameChange = viewModel::onShopNameChange,
                        onEstablishmentDateChange = viewModel::onEstablishmentDateChange,
                        onOwnerNameChange = viewModel::onOwnerNameChange,
                        onDescriptionChange = viewModel::onDescriptionChange,
                        onSave = viewModel::saveStoreConfiguration,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerSettingsTopBar(
    title: String,
    onBack: () -> Unit,
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
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
                contentDescription = stringResource(R.string.common_back),
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
private fun StoreIdentityCard(
    logoUri: String,
    shopName: String,
    ownerName: String,
    approvalStatus: SellerApprovalStatus,
    onChangeLogo: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = FloraSelectedCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(StoneFaint)
                    .clickable(onClick = onChangeLogo),
            ) {
                AsyncImage(
                    model = logoUri,
                    contentDescription = shopName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = shopName.ifBlank { stringResource(R.string.store_config_default_shop_name) },
                    style = MaterialTheme.typography.headlineSmall,
                    color = FloraText,
                )
                Text(
                    text = ownerName.ifBlank { stringResource(R.string.store_config_owner_not_set) },
                    style = MaterialTheme.typography.bodyMedium,
                    color = FloraTextSecondary,
                )
                SellerApprovalBadge(status = approvalStatus)
                Text(
                    text = when (approvalStatus) {
                        SellerApprovalStatus.APPROVED ->
                            stringResource(R.string.seller_status_approved_supporting)
                        SellerApprovalStatus.PENDING ->
                            stringResource(R.string.seller_status_pending_supporting)
                        SellerApprovalStatus.REJECTED ->
                            stringResource(R.string.seller_status_rejected_supporting)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = FloraTextSecondary,
                )
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = FloraCardBg,
                    modifier = Modifier.clickable(onClick = onChangeLogo),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CameraAlt,
                            contentDescription = null,
                            tint = FloraBrown,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = stringResource(R.string.store_config_choose_logo),
                            style = MaterialTheme.typography.labelMedium,
                            color = FloraBrown,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StoreConfigurationForm(
    uiState: StoreConfigurationUiState,
    onShopNameChange: (String) -> Unit,
    onEstablishmentDateChange: (String) -> Unit,
    onOwnerNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = FloraSelectedCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.store_config_store_details),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = FloraText,
            )

            AuthTextField(
                label = stringResource(R.string.store_config_shop_name_label),
                value = uiState.shopName,
                onValueChange = onShopNameChange,
                placeholder = stringResource(R.string.store_config_shop_name_placeholder),
            )

            AuthTextField(
                label = stringResource(R.string.store_config_established_label),
                value = uiState.establishmentDate,
                onValueChange = onEstablishmentDateChange,
                placeholder = stringResource(R.string.store_config_established_placeholder),
            )

            AuthTextField(
                label = stringResource(R.string.store_config_owner_name_label),
                value = uiState.ownerName,
                onValueChange = onOwnerNameChange,
                placeholder = stringResource(R.string.store_config_owner_name_placeholder),
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.store_config_description_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = FloraTextSecondary,
                )
                TextField(
                    value = uiState.description,
                    onValueChange = onDescriptionChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.store_config_description_placeholder),
                            color = FloraTextMuted,
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                        disabledContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                        errorContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                        focusedIndicatorColor = FloraBrown,
                        unfocusedIndicatorColor = FloraInputUnderline,
                        cursorColor = FloraBrown,
                        focusedTextColor = FloraText,
                        unfocusedTextColor = FloraText,
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = FloraText),
                )
            }

            StoreInfoHint(icon = Icons.Outlined.Storefront, text = stringResource(R.string.store_config_hint_identity))
            StoreInfoHint(icon = Icons.Outlined.CalendarMonth, text = stringResource(R.string.store_config_hint_date))
            StoreInfoHint(icon = Icons.Outlined.Description, text = stringResource(R.string.store_config_hint_saved))

            uiState.errorMessage?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall, color = FloraError)
            }

            uiState.successMessage?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall, color = FloraBrown)
            }

            PrimaryButton(
                text = if (uiState.isSaving) stringResource(R.string.common_saving) else stringResource(R.string.store_config_save),
                onClick = onSave,
                isLoading = uiState.isSaving,
            )
        }
    }
}

@Composable
private fun StoreInfoHint(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = FloraCardBg,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = FloraBrown,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = FloraTextSecondary,
            )
        }
    }
}
