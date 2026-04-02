package com.example.myappmobile

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.myappmobile.core.navigation.AppNavGraph
import com.example.myappmobile.core.theme.FloraTheme

@Composable
fun AtelierApp() {
    FloraTheme {
        val navController = rememberNavController()
        AppNavGraph(navController = navController)
    }
}