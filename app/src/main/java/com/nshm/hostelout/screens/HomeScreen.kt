package com.nshm.hostelout.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nshm.hostelout.components.LeaveRequestCard

// Dummy data model
data class LeaveRequest(
    val id: String,
    val fromDate: String,
    val toDate: String,
    val reason: String,
    val status: String // "Pending", "Approved", "Rejected"
)

// Dummy data
val dummyRequests = listOf(
    LeaveRequest("1", "2025-11-10", "2025-11-12", "Family function", "Approved"),
    LeaveRequest("2", "2025-11-20", "2025-11-21", "Medical checkup", "Pending"),
    LeaveRequest("3", "2025-10-05", "2025-10-07", "Trip with friends", "Rejected"),
    LeaveRequest("4", "2025-09-15", "2025-09-16", "Personal work", "Approved"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("All", "Pending", "Approved", "Rejected")

    val filteredList = remember(selectedTab) {
        when (tabs[selectedTab]) {
            "All" -> dummyRequests
            "Pending" -> dummyRequests.filter { it.status == "Pending" }
            "Approved" -> dummyRequests.filter { it.status == "Approved" }
            "Rejected" -> dummyRequests.filter { it.status == "Rejected" }
            else -> emptyList()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No requests found.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredList.size) { index ->
                    LeaveRequestCard(request = filteredList[index])
                }
            }
        }
    }
}