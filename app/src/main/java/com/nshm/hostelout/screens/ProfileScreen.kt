package com.nshm.hostelout.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ContactMail
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nshm.hostelout.network.RetrofitClient
import com.nshm.hostelout.utils.SessionManager
import kotlinx.coroutines.launch

data class ProfileInfoItem(val label: String, val value: String, val icon: ImageVector)

@Composable
fun ProfileScreen(onSignOut: () -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var name by remember { mutableStateOf("Loading...") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var extraInfo by remember { mutableStateOf<List<ProfileInfoItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val userRole = SessionManager.userType

    val headerGradient = when (userRole) {
        SessionManager.UserRole.STUDENT -> listOf(Color(0xFF667eea), Color(0xFF764ba2))
        SessionManager.UserRole.TEACHER -> listOf(Color(0xFF00897B), Color(0xFF00695C))
        SessionManager.UserRole.WARDEN -> listOf(Color(0xFF7B1FA2), Color(0xFF6A1B9A))
    }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                when (userRole) {
                    SessionManager.UserRole.STUDENT -> {
                        val res = RetrofitClient.apiService.getStudentById(SessionManager.userId)
                        res.body()?.let {
                            name = it.name
                            email = it.email
                            phone = it.phone
                            extraInfo = listOf(
                                ProfileInfoItem("Room Number", it.roomNumber, Icons.Default.Home),
                                ProfileInfoItem(
                                    "Department",
                                    it.department,
                                    Icons.Default.Business
                                ),
                                ProfileInfoItem(
                                    "Guardian Phone",
                                    it.guardianPhone,
                                    Icons.Default.ContactPhone
                                )
                            )
                        }
                    }

                    SessionManager.UserRole.TEACHER -> {
                        val res = RetrofitClient.apiService.getTeacherById(SessionManager.userId)
                        res.body()?.let {
                            name = it.name
                            email = it.email
                            phone = it.phone
                            extraInfo = listOf(
                                ProfileInfoItem("Department", it.department, Icons.Default.Business)
                            )
                        }
                    }

                    SessionManager.UserRole.WARDEN -> {
                        val res = RetrofitClient.apiService.getWardenById(SessionManager.userId)
                        res.body()?.let {
                            name = it.name
                            email = it.email
                            phone = it.phone
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load profile", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(headerGradient)
                        )
                        .padding(32.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        color = headerGradient[0],
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Text(
                                        text = name.firstOrNull()?.toString()?.uppercase() ?: "?",
                                        fontSize = 42.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = headerGradient[0]
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            color = Color.White.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = userRole.name.lowercase()
                                    .replaceFirstChar { it.uppercase() },
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProfileInfoCard(
                    title = "Contact Information",
                    titleIcon = Icons.Default.ContactMail,
                    items = listOf(
                        ProfileInfoItem("Email", email, Icons.Default.Email),
                        ProfileInfoItem("Phone", phone, Icons.Default.Phone)
                    ),
                    accentColor = headerGradient[0]
                )

                if (extraInfo.isNotEmpty()) {
                    ProfileInfoCard(
                        title = "Additional Details",
                        titleIcon = Icons.Default.Info,
                        items = extraInfo,
                        accentColor = headerGradient[0]
                    )
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Button(
                        onClick = {
                            SessionManager.clearSession()
                            onSignOut()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF5350)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Sign Out",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun ProfileInfoCard(
    title: String,
    titleIcon: ImageVector,
    items: List<ProfileInfoItem>,
    accentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = titleIcon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            items.forEachIndexed { index, item ->
                ProfileInfoRow(
                    label = item.label,
                    value = item.value,
                    icon = item.icon,
                    accentColor = accentColor
                )

                if (index < items.size - 1) {
                    Divider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = Color(0xFFF0F0F0),
                        thickness = 1.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(
    label: String,
    value: String,
    icon: ImageVector,
    accentColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF8F9FA)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF757575),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color(0xFF212121)
            )
        }
    }
}