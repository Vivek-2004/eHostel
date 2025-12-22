package com.nshm.hostelout.network

import com.nshm.hostelout.model.AuthenticateDTO
import com.nshm.hostelout.model.AuthenticateResponseDTO
import com.nshm.hostelout.model.ComplaintDTO
import com.nshm.hostelout.model.LeaveDTO
import com.nshm.hostelout.model.NoticeDTO
import com.nshm.hostelout.model.StudentDTO
import com.nshm.hostelout.model.TeacherDTO
import com.nshm.hostelout.model.WardenDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // --- Student ---
    @POST("api/students/login")
    suspend fun authenticateStudent(@Body dto: AuthenticateDTO): Response<AuthenticateResponseDTO>

    @POST("api/students")
    suspend fun registerStudent(@Body dto: StudentDTO): Response<StudentDTO>

    @GET("api/students/{id}")
    suspend fun getStudentById(@Path("id") id: Long): Response<StudentDTO>

    // --- Teacher ---
    @POST("api/teachers/login")
    suspend fun authenticateTeacher(@Body dto: AuthenticateDTO): Response<AuthenticateResponseDTO>

    @GET("api/teachers/{id}")
    suspend fun getTeacherById(@Path("id") id: Long): Response<TeacherDTO>

    // --- Warden ---
    @POST("api/wardens/login")
    suspend fun authenticateWarden(@Body dto: AuthenticateDTO): Response<AuthenticateResponseDTO>

    @GET("api/wardens/{id}")
    suspend fun getWardenById(@Path("id") id: Long): Response<WardenDTO>

    // --- Leaves ---
    @POST("api/leaves/apply")
    suspend fun applyLeave(@Body dto: LeaveDTO): Response<LeaveDTO>

    @GET("api/leaves")
    suspend fun getAllLeaves(): Response<List<LeaveDTO>>

    @GET("api/leaves/student/{studentId}")
    suspend fun getLeavesByStudentId(@Path("studentId") studentId: Long): Response<List<LeaveDTO>>

    @PUT("api/leaves/{id}/teacher/{teacherId}")
    suspend fun updateLeaveStatusByTeacher(
        @Path("id") leaveId: Long,
        @Path("teacherId") teacherId: Long,
        @Query("isApproved") isApproved: Boolean
    ): Response<LeaveDTO>

    @PUT("api/leaves/{id}/warden/{wardenId}")
    suspend fun updateLeaveStatusByWarden(
        @Path("id") leaveId: Long,
        @Path("wardenId") wardenId: Long,
        @Query("isApproved") isApproved: Boolean
    ): Response<LeaveDTO>

    // --- Complaints ---
    @POST("api/complaints")
    suspend fun createComplaint(@Body dto: ComplaintDTO): Response<ComplaintDTO>

    @GET("api/complaints")
    suspend fun getAllComplaints(): Response<List<ComplaintDTO>>

    // --- Notices ---
    @GET("api/notices")
    suspend fun getAllNotices(): Response<List<NoticeDTO>>

    @POST("api/notices/warden/{wardenId}/publish")
    suspend fun createNotice(@Path("wardenId") wardenId: Long, @Body dto: NoticeDTO): Response<NoticeDTO>

    @DELETE("api/notices/warden/{noticeId}")
    suspend fun deleteNotice(@Path("noticeId") noticeId: Long): Response<String>
}