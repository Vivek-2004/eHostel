package com.nshm.hostelout.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nshm.hostelout.model.LeaveDTO

@Composable
fun LeaveRequestCard(request: LeaveDTO) {
    // Map Backend Status strings to Colors
    val statusColor = when (request.status) {
        "Approved by Warden" -> Color(0xFF388E3C) // Green (Final Approval)
        "Approved by Teacher" -> Color(0xFFF57C00) // Orange (Intermediate)
        "Rejected by Teacher", "Rejected by Warden" -> Color(0xFFD32F2F) // Red
        else -> Color.Gray // "Applied" or null is treated as Pending
    }

    // Display friendly status text
    val displayStatus = when (request.status) {
        null -> "PENDING"
        "Applied" -> "PENDING"
        else -> request.status!!.uppercase()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "From: ${request.fromDate}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = displayStatus,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            Text(
                text = "To: ${request.toDate}",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Reason:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = request.reason,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}