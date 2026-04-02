package com.example.myappmobile.core.navigation

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myappmobile.core.theme.CharcoalDark
import com.example.myappmobile.core.theme.Cream
import com.example.myappmobile.core.theme.StoneFaint
import com.example.myappmobile.core.theme.StoneGray

@Composable
fun AppBottomBar(
    selectedRoute: String,
    onNavigate: (String) -> Unit,
) {
    NavigationBar(
        containerColor = Cream,
        tonalElevation = 0.dp,
        modifier = Modifier.border(
            width = 0.5.dp,
            color = StoneFaint,
            shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp),
        ),
    ) {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = item.route == selectedRoute,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CharcoalDark,
                    selectedTextColor = CharcoalDark,
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = StoneGray,
                    unselectedTextColor = StoneGray,
                ),
            )
        }
    }
}
