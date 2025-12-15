package com.nshm.hostelout.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Leave Request") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Close")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Please fill out the form to request leave.",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = fromDate,
                onValueChange = { fromDate = it },
                label = { Text("From Date (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                placeholder = { Text("2024-01-01") }
            )
            OutlinedTextField(
                value = toDate,
                onValueChange = { toDate = it },
                label = { Text("To Date (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                placeholder = { Text("2024-01-05") }
            )
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("Reason for Leave") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )
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
                    .height(50.dp),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                else Text("Submit Request")
            }
        }
    }
}