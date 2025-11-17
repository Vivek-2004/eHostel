package com.nshm.hostelout.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nshm.hostelout.screens.ForgotPasswordScreen
import com.nshm.hostelout.screens.LoginScreen
import com.nshm.hostelout.screens.MainAppScreen
import com.nshm.hostelout.screens.SignUpScreen

// Define route constants
object Routes {
    const val LOGIN = "login"
    const val SIGN_UP = "signup"
    const val FORGOT_PASSWORD = "forgot_password"
    const val MAIN_APP = "main_app"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // A simple NavHost
    // For a real app, you'd have logic here to check if the user
    // is already logged in, and if so, start at Routes.MAIN_APP

    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.MAIN_APP) {
                        // Pop all auth screens off the back stack
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToSignUp = { navController.navigate(Routes.SIGN_UP) },
                onNavigateToForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) }
            )
        }

        composable(Routes.SIGN_UP) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(Routes.MAIN_APP) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onPasswordResetSent = { navController.popBackStack() },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(Routes.MAIN_APP) {
            MainAppScreen(
                onSignOut = {
                    navController.navigate(Routes.LOGIN) {
                        // Pop all main app screens off the back stack
                        popUpTo(Routes.MAIN_APP) { inclusive = true }
                    }
                }
            )
        }
    }
}