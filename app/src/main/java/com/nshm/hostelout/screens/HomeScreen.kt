package com.nshm.hostelout.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                    val allLeaves = response.body() ?: emptyList()
                    leaves = when (userRole) {
                        SessionManager.UserRole.TEACHER -> allLeaves.filter {
                            it.status.equals(
                                "Applied", ignoreCase = true
                            )
                        }

                        SessionManager.UserRole.WARDEN -> allLeaves.filter {
                            it.status.equals(
                                "Approved by Teacher", ignoreCase = true
                            )
                        }

                        else -> allLeaves
                    }.reversed()
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

    LaunchedEffect(Unit) { fetchData() }

    Scaffold(
        containerColor = Color(0xFFF5F7FA),
        floatingActionButton = {
            // Refresh Button for Everyone
            FloatingActionButton(
                onClick = { fetchData() },
                containerColor = when (userRole) {
                    SessionManager.UserRole.TEACHER -> Color(0xFF00897B)
                    SessionManager.UserRole.WARDEN -> Color(0xFF7B1FA2)
                    else -> Color.White
                },
                contentColor = when (userRole) {
                    SessionManager.UserRole.TEACHER -> Color.White
                    SessionManager.UserRole.WARDEN -> Color.White
                    else -> Color(0xFF667eea)
                },
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(if (userRole == SessionManager.UserRole.STUDENT) 6.dp else 8.dp),
                modifier = if (userRole == SessionManager.UserRole.STUDENT) Modifier.size(48.dp) else Modifier
            ) {
                Icon(
                    Icons.Default.Refresh,
                    "Refresh",
                    modifier = if (userRole == SessionManager.UserRole.STUDENT) Modifier.size(24.dp) else Modifier
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF667eea))
                }
            } else if (leaves.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Info,
                        null,
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFFBDBDBD)
                    )
                    Text(
                        "No leave requests",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF212121)
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(leaves) { leave ->
                        LeaveItemCard(
                            leave = leave, userRole = userRole, onAction = { isApproved ->
                                scope.launch {
                                    handleApproval(
                                        context, leave.id!!, isApproved
                                    ) { fetchData() }
                                }
                            })
                    }
                }
            }
        }
    }
}

suspend fun handleApproval(
    context: android.content.Context, leaveId: Long, isApproved: Boolean, onSuccess: () -> Unit
) {
    try {
        val response = if (SessionManager.userType == SessionManager.UserRole.TEACHER) {
            RetrofitClient.apiService.updateLeaveStatusByTeacher(
                leaveId, SessionManager.userId, isApproved
            )
        } else {
            RetrofitClient.apiService.updateLeaveStatusByWarden(
                leaveId, SessionManager.userId, isApproved
            )
        }
        if (response.isSuccessful) {
            Toast.makeText(context, if (isApproved) "Approved" else "Rejected", Toast.LENGTH_SHORT)
                .show()
            onSuccess()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun LeaveItemCard(leave: LeaveDTO, userRole: SessionManager.UserRole, onAction: (Boolean) -> Unit) {
    val canAct = when (userRole) {
        SessionManager.UserRole.TEACHER -> leave.status.equals("Applied", ignoreCase = true)
        SessionManager.UserRole.WARDEN -> leave.status.equals(
            "Approved by Teacher", ignoreCase = true
        )

        else -> false
    }

    val (statusColor, statusBg) = when (leave.status) {
        "Approved by Warden" -> Color(0xFF1B5E20) to Color(0xFFE8F5E9)
        "Approved by Teacher" -> Color(0xFFE65100) to Color(0xFFFFF3E0)
        "Rejected by Teacher", "Rejected by Warden" -> Color(0xFFB71C1C) to Color(0xFFFFEBEE)
        else -> Color(0xFF1565C0) to Color(0xFFE3F2FD)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(6.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "#${leave.id}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF212121)
                )
                Surface(color = statusBg, shape = RoundedCornerShape(20.dp)) {
                    Text(
                        leave.status ?: "Unknown",
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("From", fontSize = 12.sp, color = Color(0xFF757575))
                    Text(
                        leave.fromDate,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "To", fontSize = 12.sp, color = Color(0xFF757575)
                    )
                    Text(
                        leave.toDate,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Reason: ${leave.reason}",
                fontSize = 14.sp,
                color = Color(0xFF424242)
            )
            if (canAct) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { onAction(false) }, modifier = Modifier.weight(1f)
                    ) { Text("Reject", color = Color(0xFFD32F2F)) }
                    Button(
                        onClick = { onAction(true) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
                    ) { Text("Approve", color = Color.White) }
                }
            }
        }
    }
}