package com.nshm.hostelout.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
                val response = if (userRole == SessionManager.UserRole.STUDENT) {
                    RetrofitClient.apiService.getLeavesByStudentId(SessionManager.userId)
                } else {
                    RetrofitClient.apiService.getAllLeaves()
                }

                if (response.isSuccessful) {
                    var allLeaves = response.body() ?: emptyList()
                    // Filter for Roles
                    if (userRole == SessionManager.UserRole.TEACHER) {
                        // Teacher sees Applied leaves to approve, or history
                        // For simplicity, showing all but highlighting actionable ones
                        // Or lets filter to show actionable on top
                    }
                    if (userRole == SessionManager.UserRole.WARDEN) {
                        // Warden sees ApprovedByTeacher to approve
                    }
                    // Sort by newest
                    leaves = allLeaves.reversed()
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
                    Text("No leave records found.")
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
            Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()
            onSuccess()
        } else {
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun LeaveItemCard(leave: LeaveDTO, userRole: SessionManager.UserRole, onAction: (Boolean) -> Unit) {
    // Logic for showing Approve/Reject buttons
    // Teacher can act if status == "Applied"
    // Warden can act if status == "Approved by Teacher"

    val canAct = when(userRole) {
        SessionManager.UserRole.TEACHER -> leave.status == "Applied"
        SessionManager.UserRole.WARDEN -> leave.status == "Approved by Teacher"
        else -> false
    }

    val statusColor = when (leave.status) {
        "Approved by Warden" -> Color(0xFF388E3C) // Final Approval
        "Approved by Teacher" -> Color(0xFFF57C00) // Pending Warden
        "Rejected by Teacher", "Rejected by Warden" -> Color(0xFFD32F2F)
        else -> Color.Gray // Applied
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Date: ${leave.fromDate} to ${leave.toDate}", fontWeight = FontWeight.Bold)
                Text(text = leave.status ?: "Unknown", color = statusColor, style = MaterialTheme.typography.labelSmall)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Reason: ${leave.reason}")

            if (canAct) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = { onAction(false) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                        Text("Reject")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onAction(true) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))) {
                        Text("Approve")
                    }
                }
            }
        }
    }
}