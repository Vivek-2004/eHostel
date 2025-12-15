package com.nshm.hostelout.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
        // Safety check: Teachers shouldn't be here
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

                    // Filter Logic:
                    // Student: Sees only their own.
                    // Warden: Sees all.
                    // Teacher: Sees none (handled by empty list / nav hiding).

                    complaints = if (SessionManager.userType == SessionManager.UserRole.STUDENT) {
                        allComplaints.filter { it.createdByStudentId == SessionManager.userId }
                    } else {
                        // Warden
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
        floatingActionButton = {
            if (SessionManager.userType == SessionManager.UserRole.STUDENT) {
                FloatingActionButton(
                    onClick = onNavigateToCreate,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Complaint", tint = Color.White)
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {

            if (SessionManager.userType == SessionManager.UserRole.TEACHER) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Access Denied")
                }
            } else if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (complaints.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No complaints found.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Complaint #${complaint.id}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )

                // Status Badge
                Surface(
                    color = if (isResolved) Color(0xFFC8E6C9) else Color(0xFFFFE0B2),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = complaint.status ?: "Pending",
                        color = if (isResolved) Color(0xFF1B5E20) else Color(0xFFE65100),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = complaint.message,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (!complaint.createdAt.isNullOrEmpty()) {
                Divider(color = Color.LightGray.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Created: ${complaint.createdAt}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Complaint") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                "Describe your issue",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Complaint Message") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(24.dp))

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
                                status = "Pending" // Default status
                            )
                            val response = RetrofitClient.apiService.createComplaint(dto)
                            if (response.isSuccessful) {
                                Toast.makeText(context, "Complaint Submitted", Toast.LENGTH_SHORT).show()
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
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Submit Complaint")
            }
        }
    }
}