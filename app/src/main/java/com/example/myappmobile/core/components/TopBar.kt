package com.example.myappmobile.core.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
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
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector = Icons.Outlined.Menu,
                        contentDescription = "Open menu",
                        tint = FloraText,
                    )
                }
            }
        },
        actions = {
            if (showCartIcon) {
                IconButton(onClick = onCartClick) {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingBag,
                        contentDescription = "Shopping cart",
                        tint = FloraText,
                    )
                }
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
    title: String = "The Atelier",
    onBack: (() -> Unit)? = null,
    onSearch: () -> Unit = {},
    onCart: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = CharcoalDark,
                    )
                }
            }
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = CharcoalDark,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        actions = {
            IconButton(onClick = onSearch) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = CharcoalDark,
                )
            }
            IconButton(onClick = onCart) {
                Icon(
                    imageVector = Icons.Outlined.ShoppingBag,
                    contentDescription = "Cart",
                    tint = CharcoalDark,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Cream,
            scrolledContainerColor = Cream,
        ),
        modifier = modifier,
    )
}
