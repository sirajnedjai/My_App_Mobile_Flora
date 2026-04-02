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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myappmobile.core.navigation.AppBottomBar
import com.example.myappmobile.core.theme.FloraBeige
import com.example.myappmobile.core.theme.FloraBrown
import com.example.myappmobile.core.theme.FloraDivider
import com.example.myappmobile.core.theme.FloraError
import com.example.myappmobile.core.theme.FloraText
import com.example.myappmobile.core.theme.FloraTextMuted
import com.example.myappmobile.core.theme.FloraTextSecondary
import com.example.myappmobile.core.theme.FloraTheme
import com.example.myappmobile.core.theme.SerifFontFamily
import com.example.myappmobile.core.theme.StoneFaint
import com.example.myappmobile.core.theme.White
import com.example.myappmobile.data.local.dummy.DummyUsers

@Composable
fun ProfileScreen(
    onBack: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onSettingClick: (String) -> Unit = {},
    onDeleteAccountClick: () -> Unit = {},
    selectedRoute: String = "profile",
    onBottomNavClick: (String) -> Unit = {},
    viewModel: ProfileViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    ProfileScreenContent(
        uiState = uiState,
        onBack = onBack,
        onMenuClick = onMenuClick,
        onSettingClick = onSettingClick,
        onDarkModeToggle = viewModel::onDarkModeToggled,
        onDeleteAccountClick = onDeleteAccountClick,
        selectedRoute = selectedRoute,
        onBottomNavClick = onBottomNavClick,
    )
}

@Composable
private fun ProfileScreenContent(
    uiState: ProfileUiState,
    onBack: () -> Unit,
    onMenuClick: () -> Unit,
    onSettingClick: (String) -> Unit,
    onDarkModeToggle: (Boolean) -> Unit,
    onDeleteAccountClick: () -> Unit,
    selectedRoute: String,
    onBottomNavClick: (String) -> Unit,
) {
    Scaffold(
        containerColor = FloraBeige,
        topBar = {
            ProfileHeader(
                onBack = onBack,
                onMenuClick = onMenuClick,
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
                ProfileCard(user = uiState.user)
            }

            item {
                SettingsSection(
                    title = "Buyer Account",
                    items = uiState.buyerSettings,
                    onItemClick = onSettingClick,
                )
            }

            item {
                SettingsSection(
                    title = "Seller Dashboard",
                    items = uiState.sellerSettings,
                    onItemClick = onSettingClick,
                )
            }

            item {
                ToggleItem(
                    icon = Icons.Outlined.DarkMode,
                    title = "Dark Mode",
                    subtitle = "Switch the atelier into an evening ambiance.",
                    checked = uiState.darkModeEnabled,
                    onCheckedChange = onDarkModeToggle,
                )
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Transparent,
                ) {
                    Text(
                        text = "Delete Account & Data",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = FloraError,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.large)
                            .clickable(onClick = onDeleteAccountClick)
                            .padding(vertical = 12.dp),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileHeader(
    onBack: () -> Unit,
    onMenuClick: () -> Unit,
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = SerifFontFamily,
                    fontStyle = FontStyle.Italic,
                ),
                color = FloraText,
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = FloraText,
                )
            }
        },
        actions = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Outlined.MoreHoriz,
                    contentDescription = "More options",
                    tint = FloraText,
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = FloraBeige,
            scrolledContainerColor = FloraBeige,
        ),
    )
}

@Composable
fun ProfileCard(user: com.example.myappmobile.domain.model.User?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = White.copy(alpha = 0.72f)),
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
                    .border(2.dp, White.copy(alpha = 0.7f), CircleShape),
            ) {
                AsyncImage(
                    model = user?.avatarUrl,
                    contentDescription = user?.fullName ?: "Profile avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = user?.fullName ?: "Elena Vance",
                style = MaterialTheme.typography.headlineSmall,
                color = FloraText,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = user?.email ?: "elena.vance@flora.com",
                style = MaterialTheme.typography.bodyMedium,
                color = FloraTextSecondary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = user?.membershipTier ?: "Authenticated Member",
                style = MaterialTheme.typography.labelMedium,
                color = FloraBrown,
            )
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    items: List<ProfileSettingItemUi>,
    onItemClick: (String) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = FloraText,
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = White.copy(alpha = 0.8f)),
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
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                color = FloraText,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.subtitle,
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
        colors = CardDefaults.cardColors(containerColor = White.copy(alpha = 0.8f)),
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
                    checkedThumbColor = White,
                    checkedTrackColor = FloraBrown,
                    uncheckedThumbColor = White,
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
                darkModeEnabled = true,
                buyerSettings = emptyList(),
                sellerSettings = emptyList(),
            ),
            onBack = {},
            onMenuClick = {},
            onSettingClick = {},
            onDarkModeToggle = {},
            onDeleteAccountClick = {},
            selectedRoute = "profile",
            onBottomNavClick = {},
        )
    }
}
