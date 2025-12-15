package com.nshm.hostelout.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nshm.hostelout.model.ComplaintDTO
import com.nshm.hostelout.network.RetrofitClient
import com.nshm.hostelout.utils.SessionManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintListScreen(
    onNavigateToCreate: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var complaints by remember { mutableStateOf<List<ComplaintDTO>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    fun fetchComplaints() {
        if (SessionManager.userType == SessionManager.UserRole.TEACHER) {
            isLoading = false
            return
        }

        isLoading = true
        scope.launch {
            try {
                val response = RetrofitClient.apiService.getAllComplaints()
                if (response.isSuccessful) {
                    val allComplaints = response.body() ?: emptyList()

                    complaints = if (SessionManager.userType == SessionManager.UserRole.STUDENT) {
                        allComplaints.filter { it.createdByStudentId == SessionManager.userId }
                    } else {
                        allComplaints
                    }.reversed()

                } else {
                    Toast.makeText(context, "Failed to load complaints", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchComplaints()
    }

    Scaffold(
        containerColor = Color(0xFFF5F7FA),
        floatingActionButton = {
            if (SessionManager.userType == SessionManager.UserRole.STUDENT) {
                FloatingActionButton(
                    onClick = onNavigateToCreate,
                    containerColor = Color(0xFFFF6B6B),
                    contentColor = Color.White,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Complaint")
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (SessionManager.userType == SessionManager.UserRole.TEACHER) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color(0xFF9E9E9E)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Access Denied",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF424242)
                    )
                }
            } else if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = Color(0xFFFF6B6B),
                        strokeWidth = 3.dp
                    )
                }
            } else if (complaints.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color(0xFFBDBDBD)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No complaints yet",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF424242),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Your complaints will appear here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF757575)
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(complaints) { complaint ->
                        ComplaintItemCard(complaint)
                    }
                }
            }
        }
    }
}

@Composable
fun ComplaintItemCard(complaint: ComplaintDTO) {
    val isResolved = complaint.status.equals("Resolved", ignoreCase = true)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(6.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = if (isResolved) {
                                listOf(Color(0xFF43A047), Color(0xFF66BB6A))
                            } else {
                                listOf(Color(0xFFFF6B6B), Color(0xFFFF8E53))
                            }
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 20.dp)
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
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isResolved) Color(0xFFE8F5E9)
                                    else Color(0xFFFFEBEE)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isResolved) Color(0xFF43A047) else Color(0xFFFF6B6B),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Text(
                            text = "#${complaint.id}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = 20.sp,
                            color = Color(0xFF212121)
                        )
                    }

                    Surface(
                        color = if (isResolved) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(20.dp),
                        shadowElevation = 2.dp
                    ) {
                        Text(
                            text = complaint.status ?: "Pending",
                            color = if (isResolved) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF8F9FA)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = complaint.message,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp),
                        lineHeight = 22.sp,
                        color = Color(0xFF212121)
                    )
                }

                if (!complaint.createdAt.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF9E9E9E))
                        )
                        Text(
                            text = complaint.createdAt,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF757575),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintFormScreen(onBack: () -> Unit) {
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(4.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFEBEE)),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .padding(8.dp)
                                .clip(CircleShape)
                                .background(Color(0x20FF6B6B))
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFFFF6B6B)
                            )
                        }
                    }

                    Text(
                        "Lodge a Complaint",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = {
                        Text(
                            text = "Describe your issue in detail",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF757575)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    maxLines = 8,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF6B6B),
                        focusedLabelColor = Color(0xFFFF6B6B),
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedTextColor = Color(0xFF212121),
                        unfocusedTextColor = Color(0xFF212121),
                        unfocusedLabelColor = Color(0xFF757575)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    placeholder = { Text("Tell us what happened...", color = Color(0xFF9E9E9E)) }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                if (message.isBlank()) {
                    Toast.makeText(context, "Please enter a message", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                isLoading = true
                scope.launch {
                    try {
                        val dto = ComplaintDTO(
                            createdByStudentId = SessionManager.userId,
                            message = message,
                            status = "Pending"
                        )
                        val response = RetrofitClient.apiService.createComplaint(dto)
                        if (response.isSuccessful) {
                            Toast.makeText(context, "Complaint Submitted", Toast.LENGTH_SHORT)
                                .show()
                            onBack()
                        } else {
                            Toast.makeText(context, "Submission Failed", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF6B6B)
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(6.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    "Submit Complaint",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}