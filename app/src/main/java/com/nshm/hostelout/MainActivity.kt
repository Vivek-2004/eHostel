package com.nshm.hostelout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.nshm.hostelout.navigation.AppNavigation
import com.nshm.hostelout.navigation.Routes
import com.nshm.hostelout.ui.theme.HostelOutTheme
import com.nshm.hostelout.utils.SessionManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Initialize SessionManager
        SessionManager.init(applicationContext)

        // 2. Check if user is already logged in
        val startScreen = if (SessionManager.isLoggedIn) {
            Routes.MAIN_APP
        } else {
            Routes.LOGIN
        }

        setContent {
            HostelOutTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 3. Pass the start screen to Navigation
                    AppNavigation(startDestination = startScreen)
                }
            }
        }
    }
}