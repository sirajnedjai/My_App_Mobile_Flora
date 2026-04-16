package com.example.myappmobile.core.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import com.example.myappmobile.R
import com.example.myappmobile.core.theme.CharcoalDark
import com.example.myappmobile.core.theme.Cream
import com.example.myappmobile.core.theme.FloraBeige
import com.example.myappmobile.core.theme.FloraText
import com.example.myappmobile.core.theme.SerifFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloraTopBar(
    onMenuClick: () -> Unit = {},
    onCartClick: () -> Unit = {},
    showNavigationIcon: Boolean = true,
    showCartIcon: Boolean = true,
) {
    CenterAlignedTopAppBar(
        modifier = Modifier.fillMaxWidth(),
        title = {
            Text(
                text = "FLORA",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = SerifFontFamily,
                    fontStyle = FontStyle.Italic,
                    fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                ),
                color = FloraText,
            )
        },
        navigationIcon = {
            if (showNavigationIcon) {
                CircularIconButton(
                    icon = Icons.Outlined.Menu,
                    contentDescription = stringResource(R.string.common_open_menu),
                    onClick = onMenuClick,
                )
            }
        },
        actions = {
            if (showCartIcon) {
                CircularIconButton(
                    icon = Icons.Outlined.ShoppingBag,
                    contentDescription = stringResource(R.string.common_shopping_cart),
                    onClick = onCartClick,
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = FloraBeige,
            scrolledContainerColor = FloraBeige,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String = "",
    onBack: (() -> Unit)? = null,
    onSearch: () -> Unit = {},
    onCart: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        navigationIcon = {
            if (onBack != null) {
                CircularIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.common_back),
                    onClick = onBack,
                )
            }
        },
        title = {
            Text(
                text = title.ifBlank { stringResource(R.string.common_the_atelier) },
                style = MaterialTheme.typography.headlineMedium,
                color = CharcoalDark,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        actions = {
            CircularIconButton(
                icon = Icons.Default.Search,
                contentDescription = stringResource(R.string.nav_search),
                onClick = onSearch,
            )
            CircularIconButton(
                icon = Icons.Outlined.ShoppingBag,
                contentDescription = stringResource(R.string.common_cart),
                onClick = onCart,
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Cream,
            scrolledContainerColor = Cream,
        ),
        modifier = modifier,
    )
}
