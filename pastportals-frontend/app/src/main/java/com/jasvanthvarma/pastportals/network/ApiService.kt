package com.jasvanthvarma.pastportals.network

import com.jasvanthvarma.pastportals.AudioListResponse
import com.jasvanthvarma.pastportals.AudioRequest
import com.jasvanthvarma.pastportals.AudioResponse
import com.jasvanthvarma.pastportals.TtsRequest
import com.jasvanthvarma.pastportals.viewmodel.SignupRequest
import com.jasvanthvarma.pastportals.viewmodel.LoginRequest
import com.jasvanthvarma.pastportals.viewmodel.ApiResponse
import com.jasvanthvarma.pastportals.viewmodel.YearSummary
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("api/users/signup")  // Update with your actual backend endpoint
    fun signup(@Body request: SignupRequest): Call<ApiResponse>

    @POST("/api/users/login")  // Update with your actual backend endpoint
    fun login(@Body request: LoginRequest): Call<ApiResponse>

    @GET("api/Wikipedia/year/{year}")
    suspend fun getYearSummary(@Path("year") year: String): ApiResponse

    @GET("api/wikipedia/day/{month}/{day}")
    suspend fun getDayEvents(@Path("month") month: String, @Path("day") day: String): DayEventsResponse

    @GET("api/wikipedia/query/{query}")
    suspend fun getEventDetails(@Path("query") query: String): EventDetailsResponse

    @POST("api/wikipedia/generate-audio")
    suspend fun generateWikipediaAudio(@Body request: AudioRequest): AudioResponse

    @POST("api/text-to-speech")
    suspend fun convertTextToSpeech(@Body request: TtsRequest): AudioResponse

    @GET("api/audio")
    suspend fun listAudioFiles(): AudioListResponse

    // 🎭 NEW EVENT SEARCH ENDPOINTS
    @GET("api/wikipedia/events/search")
    suspend fun searchEvents(
        @Query("query") query: String,
        @Query("year") year: String? = null
    ): EventSearchResponse

    // Add this to your Retrofit interface
    @POST("video/generate-historical")
    suspend fun generateHistoricalVideo(@Body request: HistoricalVideoRequest): Response<VideoResponse>

    @POST("api/gemini/generate-prompt")
    suspend fun generatePromptFromText(@Body request: GeminiPromptRequest): GeminiPromptResponse


    // Data classes
    data class HistoricalVideoRequest(
        val yearData: YearSummary,
        val customPrompt: String? = null,
        val options: VideoOptions = VideoOptions()
    )

    data class VideoOptions(
        val dimension: String = "16:9",
        val style: String = "historical",
        val duration: String = "medium"
    )

    data class VideoResponse(
        val success: Boolean,
        val message: String?,
        val jobId: String?,
        val videoUrl: String?,
        val error: String?
    )

    // 👤 NEW PEOPLE SEARCH ENDPOINTS
    @GET("api/wikipedia/people/search")
    suspend fun searchPeople(
        @Query("query") query: String,
        @Query("era") era: String? = null,
        @Query("occupation") occupation: String? = null
    ): PeopleSearchResponse

    @GET("api/wikipedia/people/era/{era}")
    suspend fun getPeopleByEra(@Path("era") era: String): PeopleByEraResponse
}

data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val success: Boolean, val message: String)

// 🎭 NEW DATA CLASSES FOR EVENT SEARCH
data class EventSearchResponse(
    val success: Boolean,
    val events: List<HistoricalEvent>,
    val totalFound: Int
)

data class HistoricalEvent(
    val title: String,
    val description: String,
    val snippet: String?,
    val pageUrl: String?,
    val thumbnail: String?,
    val type: String = "event"
)

// 👤 NEW DATA CLASSES FOR PEOPLE SEARCH
data class PeopleSearchResponse(
    val success: Boolean,
    val people: List<HistoricalPerson>,
    val totalFound: Int
)

data class PeopleByEraResponse(
    val success: Boolean,
    val era: String,
    val people: List<HistoricalPerson>,
    val totalFound: Int
)

data class HistoricalPerson(
    val title: String,
    val description: String,
    val snippet: String?,
    val pageUrl: String?,
    val thumbnail: String?,
    val birthYear: String?,
    val deathYear: String?,
    val era: String? = null,
    val type: String = "person"
)


data class GeminiPromptRequest(
    val wikipediaText: String
)

data class GeminiPromptResponse(
    val success: Boolean,
    val prompt: String?,
    val error: String?
)