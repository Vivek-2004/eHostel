package com.nshm.hostelout.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Title
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
import com.nshm.hostelout.model.NoticeDTO
import com.nshm.hostelout.network.RetrofitClient
import com.nshm.hostelout.utils.SessionManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticeScreen(onNavigateToCreate: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var notices by remember { mutableStateOf<List<NoticeDTO>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val userRole = SessionManager.userType

    fun fetchNotices() {
        isLoading = true
        scope.launch {
            try {
                val response = RetrofitClient.apiService.getAllNotices()
                if (response.isSuccessful) {
                    notices = (response.body() ?: emptyList()).reversed()
                } else {
                    Toast.makeText(context, "Failed to load notices", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchNotices()
    }

    Scaffold(
        containerColor = Color(0xFFF5F7FA),
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Refresh Button for Everyone
                FloatingActionButton(
                    onClick = { fetchNotices() },
                    containerColor = Color.White,
                    contentColor = Color(0xFF667eea),
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(6.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Refresh, "Refresh", modifier = Modifier.size(24.dp))
                }

                // Add Button only for Warden
                if (userRole == SessionManager.UserRole.WARDEN) {
                    FloatingActionButton(
                        onClick = onNavigateToCreate,
                        containerColor = Color(0xFF7B1FA2),
                        contentColor = Color.White,
                        shape = CircleShape,
                        elevation = FloatingActionButtonDefaults.elevation(8.dp)
                    ) {
                        Icon(Icons.Default.Add, "New Notice")
                    }
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
                    CircularProgressIndicator(
                        color = if (userRole == SessionManager.UserRole.WARDEN) Color(0xFF7B1FA2) else Color(0xFF667eea)
                    )
                }
            } else if (notices.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color(0xFFBDBDBD)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No Notices Published",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF757575)
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notices) { notice ->
                        NoticeItemCard(
                            notice = notice,
                            isWarden = userRole == SessionManager.UserRole.WARDEN,
                            onDelete = {
                                scope.launch {
                                    try {
                                        val res = RetrofitClient.apiService.deleteNotice(notice.id!!)
                                        if (res.isSuccessful) {
                                            Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                                            fetchNotices()
                                        } else {
                                            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NoticeItemCard(notice: NoticeDTO, isWarden: Boolean, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = notice.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    if (notice.createdAt != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = notice.createdAt.toString().take(10), // Simple formatting
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF757575)
                        )
                    }
                }

                if (isWarden) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE53935))
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color(0xFFF5F5F5))
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = notice.body,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF424242),
                lineHeight = 20.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticeFormScreen(onBack: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(0xFF7B1FA2),
        focusedLabelColor = Color(0xFF7B1FA2)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Publish Notice") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Title, null) },
                colors = textFieldColors,
                singleLine = true
            )

            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                label = { Text("Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                leadingIcon = { Icon(Icons.Default.Description, null) },
                colors = textFieldColors,
                maxLines = 10
            )

            Button(
                onClick = {
                    if (title.isBlank() || body.isBlank()) {
                        Toast.makeText(context, "Fill all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isSubmitting = true
                    scope.launch {
                        try {
                            val dto = NoticeDTO(title = title, body = body)
                            val res = RetrofitClient.apiService.createNotice(SessionManager.userId, dto)
                            if (res.isSuccessful) {
                                Toast.makeText(context, "Published", Toast.LENGTH_SHORT).show()
                                onBack()
                            } else {
                                Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
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
                    .height(56.dp),
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2))
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Publish", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}