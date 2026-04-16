package com.example.myappmobile.presentation.profile.account

import android.content.Intent
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
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myappmobile.R
import com.example.myappmobile.core.components.AuthTextField
import com.example.myappmobile.core.components.CircularIconButton
import com.example.myappmobile.core.components.PrimaryButton
import com.example.myappmobile.core.theme.FloraBeige
import com.example.myappmobile.core.theme.FloraBrown
import com.example.myappmobile.core.theme.FloraCardBg
import com.example.myappmobile.core.theme.FloraError
import com.example.myappmobile.core.theme.FloraSelectedCard
import com.example.myappmobile.core.theme.FloraText
import com.example.myappmobile.core.theme.FloraTextSecondary
import com.example.myappmobile.core.theme.SerifFontFamily
import com.example.myappmobile.core.theme.StoneFaint

@Composable
fun PersonalInformationScreen(
    onBack: () -> Unit = {},
    viewModel: PersonalInformationViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            viewModel.onAvatarSelected(uri.toString())
        }
    }

    Scaffold(
        containerColor = FloraBeige,
        topBar = { PersonalInformationTopBar(onBack = onBack) },
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(R.string.personal_info_loading), color = FloraTextSecondary)
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
                    AccountHeroCard(
                        fullName = uiState.fullName,
                        email = uiState.email,
                        membershipTier = uiState.membershipTier,
                        avatarUrl = uiState.avatarUrl,
                        onChangePhoto = { imagePickerLauncher.launch(arrayOf("image/*")) },
                    )
                }

                item {
                    AccountInformationCard(
                        uiState = uiState,
                        onFullNameChange = viewModel::onFullNameChange,
                        onEmailChange = viewModel::onEmailChange,
                        onPhoneChange = viewModel::onPhoneChange,
                        onAddressChange = viewModel::onAddressChange,
                        onSave = viewModel::saveProfile,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonalInformationTopBar(onBack: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.profile_personal_information),
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
private fun AccountHeroCard(
    fullName: String,
    email: String,
    membershipTier: String,
    avatarUrl: String,
    onChangePhoto: () -> Unit,
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
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(82.dp)
                        .clip(CircleShape)
                        .background(StoneFaint)
                        .clickable(onClick = onChangePhoto),
                ) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = fullName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = FloraCardBg,
                    modifier = Modifier.clickable(onClick = onChangePhoto),
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
                            text = stringResource(R.string.personal_info_choose_photo),
                            style = MaterialTheme.typography.labelMedium,
                            color = FloraBrown,
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = fullName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = FloraText,
                )
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = FloraTextSecondary,
                )
                Text(
                    text = membershipTier,
                    style = MaterialTheme.typography.labelMedium,
                    color = FloraBrown,
                )
            }
        }
    }
}

@Composable
private fun AccountInformationCard(
    uiState: PersonalInformationUiState,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
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
                text = stringResource(R.string.personal_info_account_details),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = FloraText,
            )

            AuthTextField(
                label = stringResource(R.string.personal_info_full_name_label),
                value = uiState.fullName,
                onValueChange = onFullNameChange,
                placeholder = stringResource(R.string.personal_info_full_name_placeholder),
            )

            AuthTextField(
                label = stringResource(R.string.personal_info_email_label),
                value = uiState.email,
                onValueChange = onEmailChange,
                placeholder = stringResource(R.string.personal_info_email_placeholder),
            )

            AuthTextField(
                label = stringResource(R.string.personal_info_phone_label),
                value = uiState.phoneNumber,
                onValueChange = onPhoneChange,
                placeholder = stringResource(R.string.personal_info_phone_placeholder),
            )

            AuthTextField(
                label = stringResource(R.string.personal_info_address_label),
                value = uiState.address,
                onValueChange = onAddressChange,
                placeholder = stringResource(R.string.personal_info_address_placeholder),
            )

            ReadOnlyInfoRow(
                icon = Icons.Outlined.Badge,
                label = stringResource(R.string.personal_info_account_role),
                value = uiState.roleLabel,
            )

            ReadOnlyInfoRow(
                icon = Icons.Outlined.LocationOn,
                label = stringResource(R.string.personal_info_address_label),
                value = uiState.address.ifBlank { stringResource(R.string.personal_info_no_saved_address) },
            )

            uiState.errorMessage?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = FloraError,
                )
            }

            uiState.successMessage?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = FloraBrown,
                )
            }

            PrimaryButton(
                text = if (uiState.isSaving) stringResource(R.string.common_saving) else stringResource(R.string.common_save_changes),
                onClick = onSave,
                isLoading = uiState.isSaving,
            )
        }
    }
}

@Composable
private fun ReadOnlyInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = FloraCardBg,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = FloraBrown,
                modifier = Modifier.size(18.dp),
            )
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = FloraTextSecondary,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = FloraText,
                )
            }
        }
    }
}
