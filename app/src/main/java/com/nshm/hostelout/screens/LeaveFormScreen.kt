package com.nshm.hostelout.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
fun LeaveFormScreen(onClose: () -> Unit) {
    var fromDate by remember { mutableStateOf("") }
    var toDate by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(0xFF667eea),
        focusedLabelColor = Color(0xFF667eea),
        unfocusedBorderColor = Color(0xFFE0E0E0),
        focusedTextColor = Color(0xFF212121),
        unfocusedTextColor = Color(0xFF212121),
        unfocusedLabelColor = Color(0xFF757575)
    )

    Scaffold(
        containerColor = Color(0xFFF5F7FA),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "New Leave Request",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(Color(0x20667eea))
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Close",
                            tint = Color(0xFF667eea)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                Box {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF667eea),
                                        Color(0xFF764ba2)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Column {
                                Text(
                                    "Request Leave",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    "Fill in the details below",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF667eea))
                        )
                        Text(
                            "Leave Duration",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF667eea)
                        )
                    }

                    OutlinedTextField(
                        value = fromDate,
                        onValueChange = { fromDate = it },
                        label = { Text("From Date") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                tint = Color(0xFF667eea)
                            )
                        },
                        placeholder = { Text("YYYY-MM-DD", color = Color(0xFF9E9E9E)) },
                        colors = textFieldColors,
                        shape = RoundedCornerShape(14.dp)
                    )

                    OutlinedTextField(
                        value = toDate,
                        onValueChange = { toDate = it },
                        label = { Text("To Date") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                tint = Color(0xFF667eea)
                            )
                        },
                        placeholder = { Text("YYYY-MM-DD", color = Color(0xFF9E9E9E)) },
                        colors = textFieldColors,
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF667eea))
                        )
                        Text(
                            "Reason for Leave",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF667eea)
                        )
                    }

                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Describe your reason") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        leadingIcon = {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = null,
                                tint = Color(0xFF667eea),
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        },
                        placeholder = { Text("Tell us why you need leave...", color = Color(0xFF9E9E9E)) },
                        colors = textFieldColors,
                        shape = RoundedCornerShape(14.dp),
                        maxLines = 6
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (fromDate.isBlank() || toDate.isBlank() || reason.isBlank()) {
                        Toast.makeText(context, "Fill all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isSubmitting = true
                    scope.launch {
                        try {
                            val leave = LeaveDTO(
                                studentCollegeRegistrationNo = SessionManager.userId,
                                fromDate = fromDate,
                                toDate = toDate,
                                reason = reason
                            )
                            val response = RetrofitClient.apiService.applyLeave(leave)
                            if (response.isSuccessful) {
                                Toast.makeText(context, "Request Submitted", Toast.LENGTH_SHORT).show()
                                onClose()
                            } else {
                                Toast.makeText(context, "Failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF667eea)
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(6.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Submit Request",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}