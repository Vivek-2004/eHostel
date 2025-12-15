package com.nshm.hostelout.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.nshm.hostelout.network.RetrofitClient
import com.nshm.hostelout.utils.SessionManager
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(onSignOut: () -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var name by remember { mutableStateOf("Loading...") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var extraInfo by remember { mutableStateOf<List<ProfileInfoItem>>(emptyList()) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                when(SessionManager.userType) {
                    SessionManager.UserRole.STUDENT -> {
                        val res = RetrofitClient.apiService.getStudentById(SessionManager.userId)
                        res.body()?.let {
                            name = it.name
                            email = it.email
                            phone = it.phone
                            extraInfo = listOf(
                                ProfileInfoItem("Room", it.roomNumber, Icons.Default.Home),
                                ProfileInfoItem("Dept", it.department, Icons.Default.Home),
                                ProfileInfoItem("Guardian", it.guardianPhone, Icons.Default.Phone)
                            )
                        }
                    }
                    SessionManager.UserRole.TEACHER -> {
                        val res = RetrofitClient.apiService.getTeacherById(SessionManager.userId)
                        res.body()?.let {
                            name = it.name
                            email = it.email
                            phone = it.phone
                            extraInfo = listOf(ProfileInfoItem("Dept", it.department, Icons.Default.Home))
                        }
                    }
                    SessionManager.UserRole.WARDEN -> {
                        val res = RetrofitClient.apiService.getWardenById(SessionManager.userId)
                        res.body()?.let {
                            name = it.name
                            email = it.email
                            phone = it.phone
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ProfileInfoCard(
            title = "Personal Info",
            items = listOf(
                ProfileInfoItem("Name", name, Icons.Default.Person),
                ProfileInfoItem("Email", email, Icons.Default.Email),
                ProfileInfoItem("Phone", phone, Icons.Default.Phone)
            )
        )

        if (extraInfo.isNotEmpty()) {
            ProfileInfoCard(title = "Details", items = extraInfo)
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                SessionManager.clearSession()
                onSignOut()
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Sign Out")
        }
    }
}

@Composable
private fun ProfileInfoCard(title: String, items: List<ProfileInfoItem>) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            items.forEach { item ->
                ProfileInfoRow(label = item.label, value = item.value, icon = item.icon)
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String, icon: ImageVector) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

data class ProfileInfoItem(val label: String, val value: String, val icon: ImageVector)