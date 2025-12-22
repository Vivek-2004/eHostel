package com.nshm.hostelout.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nshm.hostelout.model.AuthenticateDTO
import com.nshm.hostelout.model.StudentDTO
import com.nshm.hostelout.network.RetrofitClient
import com.nshm.hostelout.utils.SessionManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreenWrapper(
    title: String,
    showBackButton: Boolean = false,
    onBackClicked: () -> Unit = {},
    verticalArrangement: Arrangement.Vertical = Arrangement.Center,
    enableScroll: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF667eea),
                        Color(0xFF764ba2),
                        Color(0xFFf093fb)
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                if (showBackButton) {
                    TopAppBar(
                        title = { },
                        navigationIcon = {
                            IconButton(
                                onClick = onBackClicked,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.3f))
                            ) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                }
            }
        ) { paddingValues ->
            var modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)

            if (enableScroll) {
                modifier = modifier.verticalScroll(rememberScrollState())
            }

            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = verticalArrangement
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "eH",
                        style = MaterialTheme.typography.displayMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "eHostel",
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(40.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.95f)
                    ),
                    elevation = CardDefaults.cardElevation(8.dp),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        content()
                    }
                }

                // Extra space for scrollable content to clear bottom
                if (enableScroll) {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(SessionManager.UserRole.STUDENT) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    AuthScreenWrapper(title = "Welcome Back") {
        Text(
            text = "Select Your Role",
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFF424242),
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SessionManager.UserRole.values().forEach { role ->
                FilterChip(
                    selected = selectedRole == role,
                    onClick = { selectedRole = role },
                    label = {
                        Text(
                            text = role.name.lowercase().replaceFirstChar { it.uppercase() },
                            fontWeight = if (selectedRole == role) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedRole == role) Color.White else Color(0xFF424242),
                            fontSize = if (selectedRole == role) 11.sp else 12.sp
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF667eea),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFFF5F5F5),
                        labelColor = Color(0xFF424242)
                    )
                )
            }
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address", color = Color(0xFF757575)) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Email, null, tint = Color(0xFF667eea)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF667eea),
                focusedLabelColor = Color(0xFF667eea)
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password", color = Color(0xFF757575)) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color(0xFF667eea)) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF667eea),
                focusedLabelColor = Color(0xFF667eea)
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                isLoading = true
                scope.launch {
                    try {
                        val authDto = AuthenticateDTO(email, password)
                        val response = when (selectedRole) {
                            SessionManager.UserRole.STUDENT -> RetrofitClient.apiService.authenticateStudent(authDto)
                            SessionManager.UserRole.TEACHER -> RetrofitClient.apiService.authenticateTeacher(authDto)
                            SessionManager.UserRole.WARDEN -> RetrofitClient.apiService.authenticateWarden(authDto)
                        }

                        if (response.isSuccessful && response.body()?.isCorrectPass == true) {
                            val body = response.body()!!
                            SessionManager.userId = body.userId
                            SessionManager.userType = selectedRole
                            onLoginSuccess()
                        } else {
                            Toast.makeText(context, response.body()?.message ?: "Login Failed", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF667eea)),
            shape = MaterialTheme.shapes.medium
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text("Sign In", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        if (selectedRole == SessionManager.UserRole.STUDENT) {
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = onNavigateToSignUp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("New Student? Create Account", color = Color(0xFF667eea), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var regNo by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var dept by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }
    var guardianPhone by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(0xFF667eea),
        focusedLabelColor = Color(0xFF667eea)
    )

    AuthScreenWrapper(
        title = "Create Your Account",
        showBackButton = true,
        onBackClicked = onNavigateToLogin,
        verticalArrangement = Arrangement.Top,
        enableScroll = true
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = name, onValueChange = { name = it }, label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(), singleLine = true, colors = textFieldColors
            )
            OutlinedTextField(
                value = email, onValueChange = { email = it }, label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(), singleLine = true, colors = textFieldColors
            )
            OutlinedTextField(
                value = password, onValueChange = { password = it }, label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation(),
                singleLine = true, colors = textFieldColors
            )
            OutlinedTextField(
                value = regNo, onValueChange = { regNo = it }, label = { Text("Registration No.") },
                modifier = Modifier.fillMaxWidth(), singleLine = true, colors = textFieldColors
            )
            OutlinedTextField(
                value = dept, onValueChange = { dept = it }, label = { Text("Department") },
                modifier = Modifier.fillMaxWidth(), singleLine = true, colors = textFieldColors
            )
            OutlinedTextField(
                value = room, onValueChange = { room = it }, label = { Text("Room Number") },
                modifier = Modifier.fillMaxWidth(), singleLine = true, colors = textFieldColors
            )
            OutlinedTextField(
                value = phone, onValueChange = { phone = it }, label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth(), singleLine = true, colors = textFieldColors
            )
            OutlinedTextField(
                value = guardianPhone, onValueChange = { guardianPhone = it }, label = { Text("Guardian Phone") },
                modifier = Modifier.fillMaxWidth(), singleLine = true, colors = textFieldColors
            )

            Button(
                onClick = {
                    isLoading = true
                    scope.launch {
                        try {
                            val student = StudentDTO(
                                name = name, email = email, password = password,
                                registrationNumber = regNo, department = dept,
                                roomNumber = room, phone = phone, guardianPhone = guardianPhone
                            )
                            val response = RetrofitClient.apiService.registerStudent(student)
                            if (response.isSuccessful) {
                                Toast.makeText(context, "Registered Successfully! Please Login.", Toast.LENGTH_SHORT).show()
                                onSignUpSuccess()
                            } else {
                                Toast.makeText(context, "Registration Failed", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF667eea)),
                shape = MaterialTheme.shapes.medium
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("Create Account", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ForgotPasswordScreen(onPasswordResetSent: () -> Unit, onNavigateToLogin: () -> Unit) {
    AuthScreenWrapper(title = "Reset Password", showBackButton = true, onBackClicked = onNavigateToLogin) {
        Text("Contact your administrator to reset password.", style = MaterialTheme.typography.bodyLarge, color = Color(0xFF424242))
    }
}