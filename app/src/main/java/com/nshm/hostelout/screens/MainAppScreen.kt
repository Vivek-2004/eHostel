package com.nshm.hostelout.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    val (topBarColor, fabColor) = when (userRole) {
        SessionManager.UserRole.STUDENT -> Pair(
            listOf(Color(0xFF667eea), Color(0xFF764ba2)),
            Color(0xFF667eea)
        )
        SessionManager.UserRole.TEACHER -> Pair(
            listOf(Color(0xFF00897B), Color(0xFF00695C)),
            Color(0xFF00897B)
        )
        SessionManager.UserRole.WARDEN -> Pair(
            listOf(Color(0xFF7B1FA2), Color(0xFF6A1B9A)),
            Color(0xFF7B1FA2)
        )
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier.background(
                    Brush.horizontalGradient(topBarColor)
                )
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = currentScreenTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = Color.White
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White
                    ),
                    actions = {
                        Surface(
                            color = Color.White.copy(alpha = 0.25f),
                            shape = CircleShape,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = userRole.name.first().toString(),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                    }
                )
            }
        },
        containerColor = Color(0xFFF5F7FA),
        bottomBar = {
            MainBottomNavigation(
                navController = mainNavController,
                userRole = userRole,
                onTitleChange = { currentScreenTitle = it }
            )
        },
        floatingActionButton = {
            if (currentScreenTitle == BottomNavItem.Home.title &&
                userRole == SessionManager.UserRole.STUDENT) {
                FloatingActionButton(
                    containerColor = fabColor,
                    contentColor = Color.White,
                    onClick = { mainNavController.navigate("new_request") },
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(8.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "New Leave Request",
                        modifier = Modifier.size(28.dp)
                    )
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
    val items = if (userRole == SessionManager.UserRole.TEACHER) {
        listOf(BottomNavItem.Home, BottomNavItem.Profile)
    } else {
        listOf(BottomNavItem.Home, BottomNavItem.Complaints, BottomNavItem.Profile)
    }

    var selectedItemIndex by remember { mutableStateOf(0) }

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    LaunchedEffect(currentRoute) {
        items.forEachIndexed { index, item ->
            if (item.route == currentRoute) {
                selectedItemIndex = index
                onTitleChange(item.title)
            }
        }
    }

    Card(
        elevation = CardDefaults.cardElevation(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(
            topStart = 24.dp,
            topEnd = 24.dp
        )
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = selectedItemIndex == index

                NavigationBarItem(
                    selected = isSelected,
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
                    label = {
                        Text(
                            item.title,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = if (isSelected) 13.sp else 12.sp,
                            color = if (isSelected) {
                                when(userRole) {
                                    SessionManager.UserRole.STUDENT -> Color(0xFF667eea)
                                    SessionManager.UserRole.TEACHER -> Color(0xFF00897B)
                                    SessionManager.UserRole.WARDEN -> Color(0xFF7B1FA2)
                                }
                            } else {
                                Color(0xFF757575)
                            }
                        )
                    },
                    icon = {
                        Box(
                            modifier = if (isSelected) {
                                Modifier
                                    .clip(CircleShape)
                                    .background(
                                        when(userRole) {
                                            SessionManager.UserRole.STUDENT -> Color(0xFF667eea).copy(alpha = 0.15f)
                                            SessionManager.UserRole.TEACHER -> Color(0xFF00897B).copy(alpha = 0.15f)
                                            SessionManager.UserRole.WARDEN -> Color(0xFF7B1FA2).copy(alpha = 0.15f)
                                        }
                                    )
                                    .padding(8.dp)
                            } else {
                                Modifier.padding(8.dp)
                            },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title,
                                modifier = Modifier.size(24.dp),
                                tint = if (isSelected) {
                                    when(userRole) {
                                        SessionManager.UserRole.STUDENT -> Color(0xFF667eea)
                                        SessionManager.UserRole.TEACHER -> Color(0xFF00897B)
                                        SessionManager.UserRole.WARDEN -> Color(0xFF7B1FA2)
                                    }
                                } else {
                                    Color(0xFF9E9E9E)
                                }
                            )
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = when(userRole) {
                            SessionManager.UserRole.STUDENT -> Color(0xFF667eea)
                            SessionManager.UserRole.TEACHER -> Color(0xFF00897B)
                            SessionManager.UserRole.WARDEN -> Color(0xFF7B1FA2)
                        },
                        selectedTextColor = when(userRole) {
                            SessionManager.UserRole.STUDENT -> Color(0xFF667eea)
                            SessionManager.UserRole.TEACHER -> Color(0xFF00897B)
                            SessionManager.UserRole.WARDEN -> Color(0xFF7B1FA2)
                        },
                        unselectedIconColor = Color(0xFF9E9E9E),
                        unselectedTextColor = Color(0xFF757575),
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}

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