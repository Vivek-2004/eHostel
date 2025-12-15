package com.nshm.hostelout.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
                        SessionManager.UserRole.TEACHER -> {
                            allLeaves.filter { it.status.equals("Applied", ignoreCase = true) }
                        }
                        SessionManager.UserRole.WARDEN -> {
                            allLeaves.filter { it.status.equals("Approved by Teacher", ignoreCase = true) }
                        }
                        else -> {
                            allLeaves
                        }
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

    LaunchedEffect(Unit) {
        fetchData()
    }

    Scaffold(
        containerColor = Color(0xFFF5F7FA),
        floatingActionButton = {
            if (userRole != SessionManager.UserRole.STUDENT) {
                FloatingActionButton(
                    onClick = { fetchData() },
                    containerColor = when(userRole) {
                        SessionManager.UserRole.TEACHER -> Color(0xFF00897B)
                        SessionManager.UserRole.WARDEN -> Color(0xFF7B1FA2)
                        else -> MaterialTheme.colorScheme.primary
                    },
                    contentColor = Color.White,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(8.dp)
                ) {
                    Icon(Icons.Default.Refresh, "Refresh")
                }
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = when(userRole) {
                                SessionManager.UserRole.TEACHER -> Color(0xFF00897B)
                                SessionManager.UserRole.WARDEN -> Color(0xFF7B1FA2)
                                else -> Color(0xFF667eea)
                            },
                            strokeWidth = 3.dp
                        )
                        Text(
                            "Loading requests...",
                            color = Color(0xFF757575),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else if (leaves.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF0F0F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFFBDBDBD)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "No leave requests",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color(0xFF424242),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "All caught up!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF757575)
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
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

    val canAct = when(userRole) {
        SessionManager.UserRole.TEACHER -> leave.status.equals("Applied", ignoreCase = true)
        SessionManager.UserRole.WARDEN -> leave.status.equals("Approved by Teacher", ignoreCase = true)
        else -> false
    }

    val (statusColor, statusBg, statusGradient) = when (leave.status) {
        "Approved by Warden" -> Triple(
            Color(0xFF1B5E20),
            Color(0xFFE8F5E9),
            listOf(Color(0xFF43A047), Color(0xFF66BB6A))
        )
        "Approved by Teacher" -> Triple(
            Color(0xFFE65100),
            Color(0xFFFFF3E0),
            listOf(Color(0xFFFF9800), Color(0xFFFFB74D))
        )
        "Rejected by Teacher", "Rejected by Warden" -> Triple(
            Color(0xFFB71C1C),
            Color(0xFFFFEBEE),
            listOf(Color(0xFFE53935), Color(0xFFEF5350))
        )
        else -> Triple(
            Color(0xFF1565C0),
            Color(0xFFE3F2FD),
            listOf(Color(0xFF1976D2), Color(0xFF42A5F5))
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(6.dp)
                    .background(Brush.verticalGradient(statusGradient))
            )

            Column(
                modifier = Modifier.padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(statusBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Leave Request",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF757575),
                                fontSize = 12.sp
                            )
                            Text(
                                text = "#${leave.id}",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.headlineSmall,
                                fontSize = 24.sp,
                                color = Color(0xFF212121)
                            )
                        }
                    }

                    Surface(
                        color = statusBg,
                        shape = RoundedCornerShape(20.dp),
                        shadowElevation = 3.dp
                    ) {
                        Text(
                            text = leave.status ?: "Unknown",
                            color = statusColor,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF8F9FA)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                "From",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF757575),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                leave.fromDate,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF212121)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(40.dp)
                                .background(Color(0xFFE0E0E0))
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                "To",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF757575),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                leave.toDate,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF212121)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF9E9E9E))
                        )
                        Text(
                            "Reason",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF757575),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF8F9FA)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            leave.reason,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp),
                            lineHeight = 20.sp,
                            color = Color(0xFF212121)
                        )
                    }
                }

                if (canAct) {
                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onAction(false) },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFE53935)
                            ),
                            border = ButtonDefaults.outlinedButtonBorder
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Reject",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    maxLines = 1
                                )
                            }
                        }

                        Button(
                            onClick = { onAction(true) },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF43A047)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            elevation = ButtonDefaults.buttonElevation(4.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Approve",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}