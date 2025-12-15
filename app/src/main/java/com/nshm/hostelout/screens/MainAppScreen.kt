package com.nshm.hostelout.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    object Complaints : BottomNavItem("complaints", "Complaints", Icons.Filled.Warning, Icons.Outlined.Warning)
    object Profile : BottomNavItem("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(onSignOut: () -> Unit) {
    val mainNavController = rememberNavController()
    var currentScreenTitle by remember { mutableStateOf(BottomNavItem.Home.title) }

    val userRole = SessionManager.userType

    // Differentiated UI Colors based on Role
    val topBarColor = when (userRole) {
        SessionManager.UserRole.STUDENT -> MaterialTheme.colorScheme.primary
        SessionManager.UserRole.TEACHER -> Color(0xFF00796B) // Teal for Teacher
        SessionManager.UserRole.WARDEN -> Color(0xFF6A1B9A) // Purple for Warden
    }

    val topBarContentColor = Color.White

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Differentiated Title
                    Text("$currentScreenTitle (${userRole.name.lowercase().capitalize()})")
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topBarColor,
                    titleContentColor = topBarContentColor,
                    actionIconContentColor = topBarContentColor
                )
            )
        },
        bottomBar = {
            MainBottomNavigation(
                navController = mainNavController,
                userRole = userRole,
                onTitleChange = { currentScreenTitle = it }
            )
        },
        floatingActionButton = {
            // Only Students can add requests via FAB on Home Screen
            if (currentScreenTitle == BottomNavItem.Home.title &&
                userRole == SessionManager.UserRole.STUDENT) {
                FloatingActionButton(
                    containerColor = topBarColor,
                    contentColor = topBarContentColor,
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

            composable(BottomNavItem.Complaints.route) {
                ComplaintListScreen(
                    onNavigateToCreate = { mainNavController.navigate("new_complaint") }
                )
            }

            composable(BottomNavItem.Profile.route) { ProfileScreen(onSignOut = onSignOut) }

            // Nested routes
            composable("new_request") {
                LeaveFormScreen(onClose = { mainNavController.popBackStack() })
            }
            composable("new_complaint") {
                ComplaintFormScreen(onBack = { mainNavController.popBackStack() })
            }
        }
    }
}

@Composable
fun MainBottomNavigation(
    navController: NavHostController,
    userRole: SessionManager.UserRole,
    onTitleChange: (String) -> Unit
) {
    // Logic: Teachers do not need to see Complaints
    val items = if (userRole == SessionManager.UserRole.TEACHER) {
        listOf(BottomNavItem.Home, BottomNavItem.Profile)
    } else {
        listOf(BottomNavItem.Home, BottomNavItem.Complaints, BottomNavItem.Profile)
    }

    var selectedItemIndex by remember { mutableStateOf(0) }

    // Update selected index based on current route
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    LaunchedEffect(currentRoute) {
        items.forEachIndexed { index, item ->
            if (item.route == currentRoute) {
                selectedItemIndex = index
                onTitleChange(item.title)
            }
        }
    }

    NavigationBar {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedItemIndex == index,
                onClick = {
                    selectedItemIndex = index
                    onTitleChange(item.title)
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
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

// Helper to observe back stack for updating bottom bar selection
@Composable
fun androidx.navigation.NavController.currentBackStackEntryAsState(): State<androidx.navigation.NavBackStackEntry?> {
    val currentNavBackStackEntry = remember { mutableStateOf(currentBackStackEntry) }
    DisposableEffect(this) {
        val listener = androidx.navigation.NavController.OnDestinationChangedListener { controller, destination, _ ->
            currentNavBackStackEntry.value = controller.currentBackStackEntry
        }
        addOnDestinationChangedListener(listener)
        onDispose {
            removeOnDestinationChangedListener(listener)
        }
    }
    return currentNavBackStackEntry
}