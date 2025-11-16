package com.nshm.hostelout

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LoginNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "Login/${""}") {
        composable("Login/{mail}") {
            val mail: String? = it.arguments?.getString("mail")
            if (mail != null) {
                LoginScreen(navController, mail)
            } else {
                LoginScreen(navController, "")
            }
        }
        composable("Create/{mail}") { backStackEntry ->
            val mail: String? = backStackEntry.arguments?.getString("mail")
            if (mail != null) {
                CreateAccount(navController, mail)
            } else {
                CreateAccount(navController, "")
            }
        }
        composable("Forgot/{mail}") {
            val mail: String? = it.arguments?.getString("mail")
            if (mail != null) {
                ForgotPassword(navController, mail)
            } else {
                ForgotPassword(navController, "")
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "Account") {
        composable("Account") {
            AccountScreen(navController)
        }
        composable("Form") {
            LeaveForm()
        }
    }
}