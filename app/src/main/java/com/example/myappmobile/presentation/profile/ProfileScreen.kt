package com.example.myappmobile.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myappmobile.core.navigation.AppBottomBar
import com.example.myappmobile.core.components.CircularIconButton
import com.example.myappmobile.core.components.SellerVerificationStatusChip
import com.example.myappmobile.core.components.SellerVerifiedIcon
import com.example.myappmobile.core.components.SmallActionButton
import com.example.myappmobile.core.localization.LanguageManager
import com.example.myappmobile.core.theme.FloraBeige
import com.example.myappmobile.core.theme.FloraBrown
import com.example.myappmobile.core.theme.FloraDivider
import com.example.myappmobile.core.theme.FloraSelectedCard
import com.example.myappmobile.core.theme.FloraText
import com.example.myappmobile.core.theme.FloraTextMuted
import com.example.myappmobile.core.theme.FloraTextSecondary
import com.example.myappmobile.core.theme.FloraTheme
import com.example.myappmobile.core.theme.SerifFontFamily
import com.example.myappmobile.core.theme.StoneFaint
import com.example.myappmobile.data.local.dummy.DummyUsers
import com.example.myappmobile.R
import com.example.myappmobile.core.localization.AppLanguage
import com.example.myappmobile.domain.model.SellerApprovalStatus

@Composable
fun ProfileScreen(
    onBack: () -> Unit = {},
    onSettingClick: (String) -> Unit = {},
    onLogoutClick: () -> Unit = {},
    selectedRoute: String = "profile",
    onBottomNavClick: (String) -> Unit = {},
    viewModel: ProfileViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val showLanguageDialog = remember { mutableStateOf(false) }

    ProfileScreenContent(
        uiState = uiState,
        onBack = onBack,
        onSettingClick = { settingId ->
            if (settingId == "application_language") {
                showLanguageDialog.value = true
            } else {
                onSettingClick(settingId)
            }
        },
        onDarkModeToggle = viewModel::onDarkModeToggled,
        onLogoutClick = onLogoutClick,
        selectedRoute = selectedRoute,
        onBottomNavClick = onBottomNavClick,
    )

    if (showLanguageDialog.value) {
        LanguageSelectionDialog(
            selectedLanguageCode = uiState.selectedLanguageCode,
            onDismiss = { showLanguageDialog.value = false },
            onSelect = { code ->
                showLanguageDialog.value = false
                viewModel.onLanguageSelected(code)
            },
        )
    }
}

@Composable
private fun ProfileScreenContent(
    uiState: ProfileUiState,
    onBack: () -> Unit,
    onSettingClick: (String) -> Unit,
    onDarkModeToggle: (Boolean) -> Unit,
    onLogoutClick: () -> Unit,
    selectedRoute: String,
    onBottomNavClick: (String) -> Unit,
) {
    Scaffold(
        containerColor = FloraBeige,
        topBar = {
            ProfileHeader(
                onBack = onBack,
            )
        },
        bottomBar = {
            AppBottomBar(
                selectedRoute = selectedRoute,
                onNavigate = onBottomNavClick,
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(FloraBeige)
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                ProfileCard(
                    user = uiState.user,
                    phoneNumber = uiState.phoneNumber,
                    address = uiState.address,
                    sellerApprovalStatus = if (uiState.showSellerTools) uiState.sellerApprovalStatus else null,
                )
            }

            if (uiState.buyerSettings.isNotEmpty()) {
                item {
                    SettingsSection(
                        title = stringResource(R.string.profile_buyer_account),
                        items = uiState.buyerSettings,
                        onItemClick = onSettingClick,
                    )
                }
            }

            if (uiState.showSellerTools && uiState.sellerSettings.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = stringResource(R.string.profile_seller_dashboard),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = FloraText,
                        )
                        when {
                            uiState.sellerDashboardSummary != null -> SellerDashboardCard(summary = uiState.sellerDashboardSummary)
                            uiState.sellerDashboardLoading -> DashboardStatusCard(
                                message = "Loading your live seller overview...",
                            )
                            !uiState.sellerDashboardError.isNullOrBlank() -> DashboardStatusCard(
                                message = uiState.sellerDashboardError.orEmpty(),
                            )
                        }
                        SettingsSection(
                            title = stringResource(R.string.profile_seller_dashboard),
                            items = uiState.sellerSettings,
                            onItemClick = onSettingClick,
                            showTitle = false,
                        )
                    }
                }
            }

            item {
                ToggleItem(
                    icon = Icons.Outlined.DarkMode,
                    title = stringResource(R.string.profile_dark_mode),
                    subtitle = stringResource(R.string.profile_dark_mode_subtitle),
                    checked = uiState.darkModeEnabled,
                    onCheckedChange = onDarkModeToggle,
                )
            }

            item {
                SmallActionButton(
                    text = stringResource(R.string.profile_logout),
                    onClick = onLogoutClick,
                    leadingIcon = Icons.AutoMirrored.Outlined.Logout,
                    danger = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileHeader(
    onBack: () -> Unit,
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.profile_settings),
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
fun ProfileCard(
    user: com.example.myappmobile.domain.model.User?,
    phoneNumber: String,
    address: String,
    sellerApprovalStatus: SellerApprovalStatus? = null,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = FloraSelectedCard.copy(alpha = 0.88f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(StoneFaint)
                    .border(2.dp, FloraSelectedCard.copy(alpha = 0.7f), CircleShape),
            ) {
                AsyncImage(
                    model = user?.avatarUrl,
                    contentDescription = user?.fullName ?: "Profile avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = user?.fullName ?: "Elena Vance",
                    style = MaterialTheme.typography.headlineSmall,
                    color = FloraText,
                )
                if (sellerApprovalStatus == SellerApprovalStatus.APPROVED) {
                    SellerVerifiedIcon()
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = user?.email ?: "elena.vance@flora.com",
                style = MaterialTheme.typography.bodyMedium,
                color = FloraTextSecondary,
            )
            sellerApprovalStatus?.let {
                Spacer(modifier = Modifier.height(10.dp))
                SellerVerificationStatusChip(status = it)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = user?.membershipTier ?: stringResource(R.string.profile_authenticated_member),
                style = MaterialTheme.typography.labelMedium,
                color = FloraBrown,
            )
            if (phoneNumber.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = phoneNumber,
                    style = MaterialTheme.typography.bodySmall,
                    color = FloraTextSecondary,
                )
            }
            if (address.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = address,
                    style = MaterialTheme.typography.bodySmall,
                    color = FloraTextMuted,
                )
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    items: List<ProfileSettingItemUi>,
    onItemClick: (String) -> Unit,
    showTitle: Boolean = true,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (showTitle) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = FloraText,
            )
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = FloraSelectedCard.copy(alpha = 0.92f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                items.forEachIndexed { index, item ->
                    SettingsItem(
                        item = item,
                        onClick = { onItemClick(item.id) },
                    )
                    if (index != items.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 18.dp),
                            color = FloraDivider.copy(alpha = 0.7f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsItem(
    item: ProfileSettingItemUi,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SettingsLeadingIcon(icon = item.icon)
        Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(item.titleRes),
                        style = MaterialTheme.typography.titleMedium,
                        color = FloraText,
                    )
                Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.subtitleOverride ?: stringResource(item.subtitleRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = FloraTextSecondary,
                    )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = FloraTextMuted,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun SellerDashboardCard(summary: SellerDashboardSummaryUi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = FloraSelectedCard.copy(alpha = 0.92f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = summary.storeName.ifBlank { "Seller Workspace" },
                    style = MaterialTheme.typography.titleLarge,
                    color = FloraText,
                )
                Text(
                    text = "Live summary from your FLORA store operations.",
                    style = MaterialTheme.typography.bodySmall,
                    color = FloraTextSecondary,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                MetricColumn(label = "Products", value = summary.totalProducts.toString(), modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(10.dp))
                MetricColumn(label = "Orders", value = summary.totalOrders.toString(), modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(10.dp))
                MetricColumn(label = "Pending", value = summary.pendingOrders.toString(), modifier = Modifier.weight(1f))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                MetricColumn(label = "Delivered", value = summary.deliveredOrders.toString(), modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(10.dp))
                MetricColumn(label = "Low Stock", value = summary.lowStockProducts.toString(), modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(10.dp))
                MetricColumn(label = "Balance", value = "%.0f".format(summary.availableBalance), modifier = Modifier.weight(1f))
            }

            if (summary.insight.isNotBlank()) {
                Text(
                    text = summary.insight,
                    style = MaterialTheme.typography.bodySmall,
                    color = FloraTextSecondary,
                )
            }
        }
    }
}

@Composable
private fun DashboardStatusCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = FloraSelectedCard.copy(alpha = 0.92f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = FloraTextSecondary,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
        )
    }
}

@Composable
private fun MetricColumn(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = FloraText,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = FloraTextSecondary,
        )
    }
}

@Composable
private fun LanguageSelectionDialog(
    selectedLanguageCode: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.language_dialog_title),
                color = FloraText,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LanguageManager.supportedLanguages.forEach { language ->
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (selectedLanguageCode == language.code) {
                            FloraSelectedCard
                        } else {
                            Color.Transparent
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(language.code) },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Language,
                                contentDescription = null,
                                tint = if (selectedLanguageCode == language.code) FloraBrown else FloraTextSecondary,
                            )
                            Column {
                                Text(
                                    text = when (language) {
                                        AppLanguage.ARABIC -> stringResource(R.string.language_arabic)
                                        AppLanguage.ENGLISH -> stringResource(R.string.language_english)
                                        AppLanguage.FRENCH -> stringResource(R.string.language_french)
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = FloraText,
                                )
                                Text(
                                    text = stringResource(R.string.language_code_format, language.code),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = FloraTextSecondary,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {},
        containerColor = FloraBeige,
    )
}

@Composable
fun ToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = FloraSelectedCard.copy(alpha = 0.92f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SettingsLeadingIcon(icon = icon)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = FloraText,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = FloraTextSecondary,
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = FloraSelectedCard,
                    checkedTrackColor = FloraBrown,
                    uncheckedThumbColor = FloraSelectedCard,
                    uncheckedTrackColor = FloraDivider,
                ),
            )
        }
    }
}

@Composable
private fun SettingsLeadingIcon(icon: ImageVector) {
    Surface(
        modifier = Modifier.size(42.dp),
        shape = CircleShape,
        color = StoneFaint,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = FloraBrown,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    FloraTheme {
        ProfileScreenContent(
            uiState = ProfileUiState(
                user = DummyUsers.elena.copy(membershipTier = "Authenticated Member"),
                phoneNumber = DummyUsers.elena.phone,
                address = "West Village, New York",
                darkModeEnabled = true,
                buyerSettings = emptyList(),
                sellerSettings = emptyList(),
            ),
            onBack = {},
            onSettingClick = {},
            onDarkModeToggle = {},
            onLogoutClick = {},
            selectedRoute = "profile",
            onBottomNavClick = {},
        )
    }
}
