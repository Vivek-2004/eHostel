package com.nshm.hostelout.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// Sealed class for Bottom Nav items
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : BottomNavItem("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Profile : BottomNavItem("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(onSignOut: () -> Unit) {
    val mainNavController = rememberNavController()
    var currentScreenTitle by remember { mutableStateOf(BottomNavItem.Home.title) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentScreenTitle) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            MainBottomNavigation(
                navController = mainNavController,
                onTitleChange = { currentScreenTitle = it }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mainNavController.navigate("new_request") }
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Leave Request")
            }
        }
    ) { paddingValues ->
        // This NavHost is for the *main app* (Home, Profile, etc.)
        NavHost(
            navController = mainNavController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreen()
            }
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(onSignOut = onSignOut)
            }
            composable("new_request") {
                // This is a full-screen composable, not a tab
                // We pass a lambda to navigate back when the form is submitted/closed
                LeaveFormScreen(onClose = { mainNavController.popBackStack() })
            }
        }
    }
}

@Composable
fun MainBottomNavigation(
    navController: NavHostController,
    onTitleChange: (String) -> Unit
) {
    val items = listOf(BottomNavItem.Home, BottomNavItem.Profile)
    var selectedItemIndex by remember { mutableStateOf(0) }

    NavigationBar {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedItemIndex == index,
                onClick = {
                    selectedItemIndex = index
                    onTitleChange(item.title)
                    navController.navigate(item.route) {
                        // Pop up to the start destination to avoid building up a stack
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                label = { Text(item.title) },
                icon = {
                    Icon(
                        if (selectedItemIndex == index) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title
                    )
                }
            )
        }
    }
}