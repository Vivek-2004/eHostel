package com.nshm.hostelout.utils

import com.nshm.hostelout.model.StudentDTO
import com.nshm.hostelout.model.TeacherDTO
import com.nshm.hostelout.model.WardenDTO

object SessionManager {
    var userId: Long = -1
    var userType: UserRole = UserRole.STUDENT

    // Cache for profile details
    var currentStudent: StudentDTO? = null
    var currentTeacher: TeacherDTO? = null
    var currentWarden: WardenDTO? = null

    fun clearSession() {
        userId = -1
        userType = UserRole.STUDENT
        currentStudent = null
        currentTeacher = null
        currentWarden = null
    }

    enum class UserRole {
        STUDENT, TEACHER, WARDEN
    }
}