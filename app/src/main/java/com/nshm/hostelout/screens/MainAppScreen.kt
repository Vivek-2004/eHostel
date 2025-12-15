package com.nshm.hostelout.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nshm.hostelout.utils.SessionManager

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
            // Only Students can add requests
            if (SessionManager.userType == SessionManager.UserRole.STUDENT) {
                FloatingActionButton(
                    onClick = { mainNavController.navigate("new_request") }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Leave Request")
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = mainNavController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(BottomNavItem.Home.route) { HomeScreen() }
            composable(BottomNavItem.Profile.route) { ProfileScreen(onSignOut = onSignOut) }
            composable("new_request") {
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