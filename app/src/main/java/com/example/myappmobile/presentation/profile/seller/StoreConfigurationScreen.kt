package com.example.myappmobile.presentation.profile.seller

import android.content.Intent
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.UploadFile
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myappmobile.R
import com.example.myappmobile.core.components.AuthTextField
import com.example.myappmobile.core.components.CircularIconButton
import com.example.myappmobile.core.components.FloraRemoteImage
import com.example.myappmobile.core.components.OutlineButton
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
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            val label = context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null,
            )?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
            }.orEmpty().ifBlank { uri.lastPathSegment.orEmpty().substringAfterLast('/') }
            viewModel.onDocumentSelected(uri.toString(), label)
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
            val canEdit = uiState.isSeller &&
                uiState.approvalStatus != SellerApprovalStatus.APPROVED &&
                uiState.approvalStatus != SellerApprovalStatus.PENDING
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
                        ownerName = uiState.fullName,
                        approvalStatus = uiState.approvalStatus,
                        submittedAt = uiState.submittedAt,
                    )
                }

                item {
                    VerificationStatusCard(
                        approvalStatus = uiState.approvalStatus,
                        rejectionReason = uiState.rejectionReason,
                        reviewedAt = uiState.reviewedAt,
                    )
                }

                item {
                    VerificationForm(
                        uiState = uiState,
                        canEdit = canEdit,
                        onFullNameChange = viewModel::onFullNameChange,
                        onPhoneNumberChange = viewModel::onPhoneNumberChange,
                        onNationalIdChange = viewModel::onNationalIdChange,
                        onAddressChange = viewModel::onAddressChange,
                        onShopNameChange = viewModel::onShopNameChange,
                        onDescriptionChange = viewModel::onDescriptionChange,
                        onPickDocument = { documentPickerLauncher.launch(arrayOf("image/*", "application/pdf")) },
                        onSubmit = viewModel::submitVerification,
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
    submittedAt: String?,
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
                    .background(StoneFaint),
                contentAlignment = Alignment.Center,
            ) {
                if (logoUri.isBlank()) {
                    Icon(
                        imageVector = Icons.Outlined.Storefront,
                        contentDescription = null,
                        tint = FloraBrown,
                        modifier = Modifier.size(30.dp),
                    )
                } else {
                    FloraRemoteImage(
                        imageUrl = logoUri,
                        contentDescription = shopName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
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
                        SellerApprovalStatus.UNKNOWN ->
                            stringResource(R.string.store_verification_status_not_verified_message)
                        SellerApprovalStatus.NOT_VERIFIED ->
                            stringResource(R.string.seller_status_not_verified_supporting)
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
                if (!submittedAt.isNullOrBlank()) {
                    Text(
                        text = stringResource(R.string.store_verification_submitted_at, submittedAt),
                        style = MaterialTheme.typography.labelMedium,
                        color = FloraBrown,
                    )
                }
            }
        }
    }
}

@Composable
private fun VerificationStatusCard(
    approvalStatus: SellerApprovalStatus,
    rejectionReason: String?,
    reviewedAt: String?,
) {
    val icon = when (approvalStatus) {
        SellerApprovalStatus.UNKNOWN -> Icons.Outlined.Info
        SellerApprovalStatus.NOT_VERIFIED -> Icons.Outlined.Info
        SellerApprovalStatus.PENDING -> Icons.Outlined.HourglassTop
        SellerApprovalStatus.APPROVED -> Icons.Outlined.MarkEmailRead
        SellerApprovalStatus.REJECTED -> Icons.Outlined.Description
    }
    val title = when (approvalStatus) {
        SellerApprovalStatus.UNKNOWN -> stringResource(R.string.store_verification_status_not_verified_title)
        SellerApprovalStatus.NOT_VERIFIED -> stringResource(R.string.store_verification_status_not_verified_title)
        SellerApprovalStatus.PENDING -> stringResource(R.string.store_verification_status_pending_title)
        SellerApprovalStatus.APPROVED -> stringResource(R.string.store_verification_status_approved_title)
        SellerApprovalStatus.REJECTED -> stringResource(R.string.store_verification_status_rejected_title)
    }
    val description = when (approvalStatus) {
        SellerApprovalStatus.UNKNOWN -> stringResource(R.string.store_verification_status_not_verified_message)
        SellerApprovalStatus.NOT_VERIFIED -> stringResource(R.string.store_verification_status_not_verified_message)
        SellerApprovalStatus.PENDING -> stringResource(R.string.store_verification_status_pending_message)
        SellerApprovalStatus.APPROVED -> stringResource(R.string.store_verification_status_approved_message)
        SellerApprovalStatus.REJECTED -> rejectionReason
            ?.takeIf { it.isNotBlank() }
            ?: stringResource(R.string.store_verification_status_rejected_message)
    }
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = FloraSelectedCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = FloraCardBg,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = FloraBrown,
                    modifier = Modifier.padding(12.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = FloraText,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = FloraTextSecondary,
                )
                if (!reviewedAt.isNullOrBlank()) {
                    Text(
                        text = stringResource(R.string.store_verification_reviewed_at, reviewedAt),
                        style = MaterialTheme.typography.labelMedium,
                        color = FloraBrown,
                    )
                }
            }
        }
    }
}

@Composable
private fun VerificationForm(
    uiState: StoreConfigurationUiState,
    canEdit: Boolean,
    onFullNameChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onNationalIdChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onShopNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPickDocument: () -> Unit,
    onSubmit: () -> Unit,
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
                text = stringResource(R.string.store_verification_form_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = FloraText,
            )
            Text(
                text = stringResource(R.string.store_verification_form_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = FloraTextSecondary,
            )

            AuthTextField(
                label = stringResource(R.string.store_verification_full_name_label),
                value = uiState.fullName,
                onValueChange = onFullNameChange,
                placeholder = stringResource(R.string.store_verification_full_name_placeholder),
                enabled = canEdit && !uiState.isSaving,
                isError = uiState.fieldErrors.fullName != null,
                errorMessage = uiState.fieldErrors.fullName,
            )

            AuthTextField(
                label = stringResource(R.string.store_verification_phone_label),
                value = uiState.phoneNumber,
                onValueChange = onPhoneNumberChange,
                placeholder = stringResource(R.string.store_verification_phone_placeholder),
                enabled = canEdit && !uiState.isSaving,
                isError = uiState.fieldErrors.phoneNumber != null,
                errorMessage = uiState.fieldErrors.phoneNumber,
            )

            AuthTextField(
                label = stringResource(R.string.store_verification_document_number_label),
                value = uiState.nationalId,
                onValueChange = onNationalIdChange,
                placeholder = stringResource(R.string.store_verification_document_number_placeholder),
                enabled = canEdit && !uiState.isSaving,
                isError = uiState.fieldErrors.nationalId != null,
                errorMessage = uiState.fieldErrors.nationalId,
            )

            AuthTextField(
                label = stringResource(R.string.store_verification_address_label),
                value = uiState.address,
                onValueChange = onAddressChange,
                placeholder = stringResource(R.string.store_verification_address_placeholder),
                enabled = canEdit && !uiState.isSaving,
                isError = uiState.fieldErrors.address != null,
                errorMessage = uiState.fieldErrors.address,
            )

            AuthTextField(
                label = stringResource(R.string.store_config_shop_name_label),
                value = uiState.shopName,
                onValueChange = onShopNameChange,
                placeholder = stringResource(R.string.store_config_shop_name_placeholder),
                enabled = canEdit && !uiState.isSaving,
                isError = uiState.fieldErrors.shopName != null,
                errorMessage = uiState.fieldErrors.shopName,
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
                    enabled = canEdit && !uiState.isSaving,
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
                        disabledTextColor = FloraTextMuted,
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = FloraText),
                )
                uiState.fieldErrors.description?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = FloraError,
                    )
                }
            }

            VerificationDocumentRow(
                documentLabel = uiState.documentLabel.ifBlank {
                    if (uiState.documentUri.isBlank()) "" else uiState.documentUri.substringAfterLast('/')
                },
                documentError = uiState.fieldErrors.document,
                canEdit = canEdit && !uiState.isSaving,
                onPickDocument = onPickDocument,
            )

            StoreInfoHint(
                icon = Icons.Outlined.Badge,
                text = stringResource(R.string.store_verification_hint_backend),
            )
            StoreInfoHint(
                icon = Icons.Outlined.UploadFile,
                text = stringResource(R.string.store_verification_hint_document),
            )

            uiState.errorMessage?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall, color = FloraError)
            }

            uiState.successMessage?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall, color = FloraBrown)
            }

            PrimaryButton(
                text = when {
                    uiState.isSaving -> stringResource(R.string.common_saving)
                    uiState.approvalStatus == SellerApprovalStatus.REJECTED -> stringResource(R.string.store_verification_resubmit)
                    uiState.approvalStatus == SellerApprovalStatus.APPROVED -> stringResource(R.string.store_verification_approved_button)
                    uiState.approvalStatus == SellerApprovalStatus.PENDING -> stringResource(R.string.store_verification_pending_button)
                    else -> stringResource(R.string.store_verification_submit)
                },
                onClick = onSubmit,
                isLoading = uiState.isSaving,
                enabled = canEdit && !uiState.isSaving,
            )
        }
    }
}

@Composable
private fun VerificationDocumentRow(
    documentLabel: String,
    documentError: String?,
    canEdit: Boolean,
    onPickDocument: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.store_verification_document_label),
            style = MaterialTheme.typography.labelSmall,
            color = FloraTextSecondary,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlineButton(
                text = if (documentLabel.isBlank()) {
                    stringResource(R.string.store_verification_pick_document)
                } else {
                    stringResource(R.string.store_verification_replace_document)
                },
                onClick = onPickDocument,
                modifier = Modifier.weight(1f),
                enabled = canEdit,
                leadingIcon = Icons.Outlined.UploadFile,
            )
            if (documentLabel.isNotBlank()) {
                Surface(
                    color = FloraCardBg,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Description,
                            contentDescription = null,
                            tint = FloraBrown,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = documentLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = FloraText,
                            maxLines = 2,
                        )
                    }
                }
            }
        }
        if (!documentError.isNullOrBlank()) {
            Text(
                text = documentError,
                style = MaterialTheme.typography.bodySmall,
                color = FloraError,
            )
        }
    }
}

@Composable
private fun StoreInfoHint(
    icon: ImageVector,
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
