package com.nshm.hostelout.model

data class AuthenticateDTO(
    val email: String,
    val password: String
)

data class AuthenticateResponseDTO(
    val message: String,
    val isCorrectPass: Boolean,
    val userType: String,
    val userId: Long
)

data class StudentDTO(
    val id: Long? = null,
    val name: String,
    val registrationNumber: String,
    val department: String,
    val email: String,
    val phone: String,
    val guardianPhone: String,
    val roomNumber: String,
    val password: String? = null
)

data class TeacherDTO(
    val id: Long? = null,
    val name: String,
    val department: String,
    val email: String,
    val phone: String,
    val password: String? = null
)

data class WardenDTO(
    val id: Long? = null,
    val name: String,
    val email: String,
    val phone: String,
    val password: String? = null
)

data class LeaveDTO(
    val id: Long? = null,
    val studentCollegeRegistrationNo: Long, // Maps to Student ID
    val fromDate: String, // format YYYY-MM-DD
    val toDate: String,   // format YYYY-MM-DD
    val reason: String,
    var status: String? = null,
    val approvedByWarden: Long? = null,
    val approvedByTeacher: Long? = null,
    val appliedDate: String? = null
)