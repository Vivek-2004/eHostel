package com.nshm.hostelout.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen(onSignOut: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // In a real app, this data would come from Firebase/ViewModel
        ProfileInfoCard(
            title = "Personal Info",
            items = listOf(
                ProfileInfoItem("Name", "Vivek Ghosh", Icons.Default.Person),
                ProfileInfoItem("Email", "vivek.ghosh@example.com", Icons.Default.Email)
            )
        )

        ProfileInfoCard(
            title = "Hostel Info",
            items = listOf(
                ProfileInfoItem("Hostel Block", "A", Icons.Default.Home),
                ProfileInfoItem("Room No.", "101", Icons.Default.Home),
                ProfileInfoItem("Guardian Phone", "+91 1234567890", Icons.Default.Phone)
            )
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onSignOut,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Sign Out")
        }
    }
}

@Composable
private fun ProfileInfoCard(title: String, items: List<ProfileInfoItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

private data class ProfileInfoItem(
    val label: String,
    val value: String,
    val icon: ImageVector
)