package com.example.binm.api

import com.example.binm.data.Category
import com.example.binm.data.ContactResponse
import com.example.binm.data.Conversation
import com.example.binm.data.CreateListingRequest
import com.example.binm.data.FavoriteRequest
import com.example.binm.data.FavoriteStatusResponse
import com.example.binm.data.FilterAttribute
import com.example.binm.data.LoginRequest
import com.example.binm.data.LoginResponse
import com.example.binm.data.MessagePage
import com.example.binm.data.PagedListingResponse
import com.example.binm.data.ProductDetail
import com.example.binm.data.ProductPageResponse
import com.example.binm.data.ProductSearchRequest
import com.example.binm.data.RegisterRequest
import com.example.binm.data.ResetPasswordRequest
import com.example.binm.data.UpdateProfileRequest
import com.example.binm.data.UserProfileResponse
import com.example.binm.data.VerifyOtpRequest
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface ApiService {
    @GET("public/category/all")
    suspend fun getCategories(): List<Category>

    @GET("public/category/attributes")
    suspend fun getCategoryAttributes(@Query("categoryId") categoryId: Int): List<FilterAttribute>

    @POST("public/listings/search")
    suspend fun searchListings(@Body request: ProductSearchRequest): ProductPageResponse

    @GET("public/listings/get/{id}")
    suspend fun getListingDetails(@Path("id") id: String): ProductDetail

    @GET("public/listings/random")
    suspend fun getRandomListings(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): PagedListingResponse

    // Zabezpieczone endpointy do zarządzania ogłoszeniami
    @POST("user/listing/create")
    suspend fun createListing(
        @Header("Authorization") token: String,
        @Body listing: CreateListingRequest
    ): Response<Void>

    @GET("user/listing/my")
    suspend fun getMyListings(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("status") status: String? = null
    ): ProductPageResponse

    @GET("user/listing/{publicId}/edit-data")
    suspend fun getListingEditData(
        @Header("Authorization") token: String,
        @Path("publicId") publicId: String
    ): ProductDetail

    @PUT("user/listing/{publicId}/update")
    suspend fun updateListing(
        @Header("Authorization") token: String,
        @Path("publicId") publicId: String,
        @Body listing: CreateListingRequest
    ): Response<Void>

    @DELETE("user/listing/{publicId}/delete")
    suspend fun deleteListing(
        @Header("Authorization") token: String,
        @Path("publicId") publicId: String
    ): Response<Void>

    @POST("user/listing/{publicId}/submit-for-approval")
    suspend fun submitForApproval(
        @Header("Authorization") token: String,
        @Path("publicId") publicId: String
    ): Response<Void>

    @POST("user/listing/{publicId}/finish")
    suspend fun finishListing(
        @Header("Authorization") token: String,
        @Path("publicId") publicId: String
    ): Response<Void>

    @GET("user/listing/{publicId}/contact")
    suspend fun getListingContact(
        @Header("Authorization") token: String,
        @Path("publicId") publicId: String
    ): ContactResponse

    // Logowanie i rejestracja
    @POST("public/register")
    suspend fun register(@Body request: RegisterRequest): Response<Void>

    @POST("public/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    // Reset hasła
    @POST("public/send-reset-otp")
    suspend fun sendResetOtp(@Query("email") email: String): Response<Void>

    @POST("public/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<Void>

    // Profil użytkownika
    @GET("user/profile")
    suspend fun getUserProfile(
        @Header("Authorization") token: String
    ): UserProfileResponse

    @PATCH("user/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Response<Void>

    @Multipart
    @POST("user/upload/profile-image")
    suspend fun uploadProfileImage(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part
    ): ResponseBody

    @Multipart
    @POST("user/upload/media-image")
    suspend fun uploadMediaImage(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part
    ): ResponseBody

    // Obsługa OTP
    @POST("user/verify-otp")
    suspend fun verifyOtp(
        @Header("Authorization") token: String,
        @Body request: VerifyOtpRequest
    ): Response<Void>

    @POST("user/send-otp")
    suspend fun resendOtp(@Header("Authorization") token: String): Response<Void>

    // Ulubione
    @GET("user/interactions/favorites/status")
    suspend fun getFavoriteStatus(
        @Header("Authorization") token: String,
        @Query("entityId") entityId: String,
        @Query("entityType") entityType: String = "LISTING"
    ): FavoriteStatusResponse

    @POST("user/interactions/favorites")
    suspend fun addToFavorites(
        @Header("Authorization") token: String,
        @Body request: FavoriteRequest
    ): Response<Void>

    @HTTP(method = "DELETE", path = "user/interactions/favorites", hasBody = true)
    suspend fun removeFromFavorites(
        @Header("Authorization") token: String,
        @Body request: FavoriteRequest
    ): Response<Void>

    @GET("user/interactions/favorites")
    suspend fun getFavorites(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): ProductPageResponse

    // Konwersacje
    @GET("user/conversations")
    suspend fun getConversations(
        @Header("Authorization") token: String
    ): List<Conversation>

    @GET("user/conversations/{conversationId}/messages")
    suspend fun getMessages(
        @Header("Authorization") token: String,
        @Path("conversationId") conversationId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): MessagePage

    @PATCH("user/conversations/{conversationId}/read")
    suspend fun markConversationAsRead(
        @Header("Authorization") token: String,
        @Path("conversationId") conversationId: Long
    ): Response<Void>
}

object RetrofitInstance {
    private val loggingInterceptor = okhttp3.logging.HttpLoggingInterceptor().apply {
        level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = okhttp3.OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(com.example.binm.BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
