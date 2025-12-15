package com.nshm.hostelout.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(onClick = onBackClicked) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "eHostel",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(32.dp))
            content()
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

    AuthScreenWrapper(title = "Sign In") {
        // Role Selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SessionManager.UserRole.values().forEach { role ->
                FilterChip(
                    selected = selectedRole == role,
                    onClick = { selectedRole = role },
                    label = { Text(role.name.lowercase().capitalize()) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if(email.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                isLoading = true
                scope.launch {
                    try {
                        val authDto = AuthenticateDTO(email, password)
                        val response = when(selectedRole) {
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
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
            else Text("Sign In")
        }

        Spacer(modifier = Modifier.height(16.dp))
        if(selectedRole == SessionManager.UserRole.STUDENT) {
            TextButton(onClick = onNavigateToSignUp) {
                Text("Student? Create Account")
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

    AuthScreenWrapper(
        title = "Student Registration",
        showBackButton = true,
        onBackClicked = onNavigateToLogin
    ) {
        // Scrollable column if fields are many
        // For brevity, keeping simple
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation(), singleLine = true)
        OutlinedTextField(value = regNo, onValueChange = { regNo = it }, label = { Text("Registration No.") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = dept, onValueChange = { dept = it }, label = { Text("Department") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = room, onValueChange = { room = it }, label = { Text("Room Number") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = guardianPhone, onValueChange = { guardianPhone = it }, label = { Text("Guardian Phone") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

        Spacer(modifier = Modifier.height(24.dp))
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
                            Toast.makeText(context, "Registered Successfully!", Toast.LENGTH_SHORT).show()
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
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
            else Text("Create Account")
        }
    }
}

// Keeping ForgotPassword placeholder for navigation structure
@Composable
fun ForgotPasswordScreen(
    onPasswordResetSent: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    AuthScreenWrapper(title = "Reset Password", showBackButton = true, onBackClicked = onNavigateToLogin) {
        Text("Contact your administrator to reset password.")
    }
}