package com.nshm.hostelout.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nshm.hostelout.model.LeaveDTO
import com.nshm.hostelout.network.RetrofitClient
import com.nshm.hostelout.utils.SessionManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var leaves by remember { mutableStateOf<List<LeaveDTO>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val userRole = SessionManager.userType

    // Function to fetch data
    fun fetchData() {
        scope.launch {
            isLoading = true
            try {
                // 1. Fetch data based on basic API availability
                val response = if (userRole == SessionManager.UserRole.STUDENT) {
                    RetrofitClient.apiService.getLeavesByStudentId(SessionManager.userId)
                } else {
                    RetrofitClient.apiService.getAllLeaves()
                }

                if (response.isSuccessful) {
                    val allLeaves = response.body() ?: emptyList()

                    // 2. Apply Logic Rules for visibility
                    leaves = when (userRole) {
                        SessionManager.UserRole.TEACHER -> {
                            // Teacher: can only see status "Applied"
                            allLeaves.filter { it.status.equals("Applied", ignoreCase = true) }
                        }
                        SessionManager.UserRole.WARDEN -> {
                            // Warden: can only see status "Approved by Teacher"
                            allLeaves.filter { it.status.equals("Approved by Teacher", ignoreCase = true) }
                        }
                        else -> {
                            // Student: sees all their own (already filtered by endpoint)
                            allLeaves
                        }
                    }.reversed() // Sort by newest
                } else {
                    Toast.makeText(context, "Failed to fetch leaves", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchData()
    }

    Scaffold(
        floatingActionButton = {
            // Teachers/Wardens need a way to refresh the list to see new requests
            if (userRole != SessionManager.UserRole.STUDENT) {
                FloatingActionButton(onClick = { fetchData() }) {
                    Icon(Icons.Default.Refresh, "Refresh")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (leaves.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No relevant leave requests found.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(leaves) { leave ->
                        LeaveItemCard(
                            leave = leave,
                            userRole = userRole,
                            onAction = { isApproved ->
                                scope.launch {
                                    handleApproval(context, leave.id!!, isApproved) { fetchData() }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

suspend fun handleApproval(context: android.content.Context, leaveId: Long, isApproved: Boolean, onSuccess: () -> Unit) {
    try {
        val response = if (SessionManager.userType == SessionManager.UserRole.TEACHER) {
            RetrofitClient.apiService.updateLeaveStatusByTeacher(leaveId, SessionManager.userId, isApproved)
        } else {
            RetrofitClient.apiService.updateLeaveStatusByWarden(leaveId, SessionManager.userId, isApproved)
        }

        if (response.isSuccessful) {
            Toast.makeText(context, if(isApproved) "Approved Successfully" else "Rejected", Toast.LENGTH_SHORT).show()
            onSuccess()
        } else {
            Toast.makeText(context, "Operation Failed", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun LeaveItemCard(leave: LeaveDTO, userRole: SessionManager.UserRole, onAction: (Boolean) -> Unit) {

    // Action Logic
    val canAct = when(userRole) {
        SessionManager.UserRole.TEACHER -> leave.status.equals("Applied", ignoreCase = true)
        SessionManager.UserRole.WARDEN -> leave.status.equals("Approved by Teacher", ignoreCase = true)
        else -> false
    }

    // Status Styling
    val (statusColor, statusBg) = when (leave.status) {
        "Approved by Warden" -> Color(0xFF1B5E20) to Color(0xFFC8E6C9) // Green
        "Approved by Teacher" -> Color(0xFFE65100) to Color(0xFFFFE0B2) // Orange
        "Rejected by Teacher", "Rejected by Warden" -> Color(0xFFB71C1C) to Color(0xFFFFCDD2) // Red
        else -> Color(0xFF0D47A1) to Color(0xFFBBDEFB) // Blue (Applied)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Leave #${leave.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = leave.status ?: "Unknown",
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.5f))

            // Dates
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("From", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Text(leave.fromDate, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("To", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Text(leave.toDate, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Reason
            Text("Reason", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Text(leave.reason, style = MaterialTheme.typography.bodyMedium)

            // Action Buttons
            if (canAct) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = { onAction(false) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reject")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = { onAction(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Approve")
                    }
                }
            }
        }
    }
}